/*
 * Copyright the State of the Netherlands
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package nl.aerius.smm.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.model.MatrixResultRecord;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.QueryResultResponse;
import nl.aerius.smm.api.model.QueryStatus;
import nl.aerius.smm.api.model.QueryTask;

@Service
public class QueryProcessingService {
    private static final Logger LOG = LoggerFactory.getLogger(QueryProcessingService.class);

    private final Map<UUID, QueryTask> queue = new ConcurrentHashMap<>();
    private final Executor requestExecutor;
    private final MatrixService matrixService;

    public QueryProcessingService(@Qualifier("requestExecutor") final Executor requestExecutor, final MatrixService matrixService) {
        this.requestExecutor = requestExecutor;
        this.matrixService = matrixService;
    }

    public UUID create(final QueryRequest request) {
        QueryTask task = QueryTask.create(request);

        // Add trace ID
        MDC.put("taskId", task.id().toString());

        queue.put(task.id(), task);
        updateTaskInQueue(task.id(), QueryTask::accepted);

        try {
            requestExecutor.execute(() -> process(task));
        } catch (TaskRejectedException e) {
            updateTaskInQueue(task.id(), QueryTask::rejected);
            throw new QueueFullException(task.id());
        }

        return task.id();
    }

    public QueryStatus getStatus(final UUID id) {
        QueryTask task = queue.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task.status();
    }

    public QueryResultResponse getResult(final UUID id) {
        AtomicReference<QueryTask> popped = new AtomicReference<>();

        queue.compute(id, (key, existing) -> {
            if (existing == null) {
                throw new TaskNotFoundException(id);
            }
            if (existing.status() != QueryStatus.COMPLETED) {
                throw new ResultNotReadyException(id, existing.status());
            }

            popped.set(existing);

            // remove from queue
            return null;
        });

        return new QueryResultResponse(popped.get().request(), popped.get().results());
    }

    private void process(final QueryTask task) {
        updateTaskInQueue(task.id(), QueryTask::processing);

        try {
            List<MatrixResultRecord> result = matrixService.fetchMatrixResults(task.request());
            updateTaskInQueue(task.id(), currentTask -> currentTask.complete(result));
        } catch (Exception e) {
            LOG.error("Async task failed while computing result: taskId={}", task.id(), e);
            updateTaskInQueue(task.id(), QueryTask::failed);
        }
    }

    /** Thread-Safe method for updating the tasks in the queue. */
    private void updateTaskInQueue(UUID id, UnaryOperator<QueryTask> updater) {
        queue.compute(id, (k, oldTask) -> {
            if (oldTask == null) {
                throw new IllegalArgumentException("Task not found: " + id);
            }

            QueryTask newTask = updater.apply(oldTask);

            LOG.debug("Async task status changed: taskId={}, from={}, to={}",
                id,
                oldTask.status(),
                newTask.status());

            return newTask;
        });
    }
}

