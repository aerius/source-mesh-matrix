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
package nl.aerius.smm.api.query.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

import nl.aerius.smm.api.config.QueryProperties;
import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.matrix.service.MatrixService;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.query.model.QueryRequest;
import nl.aerius.smm.api.query.model.QueryResultResponse;
import nl.aerius.smm.api.query.model.QueryStatus;
import nl.aerius.smm.api.query.model.QueryTask;

@Service
public class QueryProcessingService {
  private static final Logger LOG = LoggerFactory.getLogger(QueryProcessingService.class);

  private final Map<String, QueryTask> tasks = new ConcurrentHashMap<>();
  private final Executor requestExecutor;
  private final MatrixService matrixService;
  private final Clock clock;
  private final Duration terminalRetention;

  public QueryProcessingService(
      @Qualifier("requestExecutor") final Executor requestExecutor,
      final MatrixService matrixService,
      final Clock clock,
      final QueryProperties queryProperties) {
    this.requestExecutor = requestExecutor;
    this.matrixService = matrixService;
    this.clock = clock;
    this.terminalRetention = queryProperties.task().terminalRetention();
  }

  public String create(final QueryRequest request) {
    final QueryTask task = QueryTask.create(request);

    MDC.put("taskId", task.id());

    tasks.put(task.id(), task);
    updateTaskInQueue(task.id(), QueryTask::accepted);

    try {
      requestExecutor.execute(() -> process(task));
    } catch (final TaskRejectedException e) {
      updateTaskInQueue(task.id(), current -> current.rejected(clock.instant()));
      throw new QueueFullException(task.id());
    }

    return task.id();
  }

  public QueryStatus getStatus(final String id) {
    final QueryTask task = tasks.get(id);
    if (task == null) {
      throw new TaskNotFoundException(id);
    }
    return task.status();
  }

  public QueryResultResponse getResult(final String id) {
    final AtomicReference<QueryTask> popped = new AtomicReference<>();

    tasks.compute(id, (key, existing) -> {
      if (existing == null) {
        throw new TaskNotFoundException(id);
      }
      if (existing.status() != QueryStatus.COMPLETED) {
        throw new ResultNotReadyException(id, existing.status());
      }

      popped.set(existing);

      return null;
    });

    return new QueryResultResponse(popped.get().request(), popped.get().results());
  }

  /** Removes terminal tasks whose {@link QueryTask#terminalAt()} is older than the configured retention. */
  public void purgeExpiredTerminalTasks() {
    final Instant cutoff = clock.instant().minus(terminalRetention);
    final int removed = purgeTasksOlderThan(cutoff);
    if (removed > 0) {
      LOG.debug("Purged {} expired terminal task(s) (cutoff={})", removed, cutoff);
    }
  }

  private int purgeTasksOlderThan(final Instant cutoff) {
    int removed = 0;
    for (final Map.Entry<String, QueryTask> entry : tasks.entrySet()) {
      final QueryTask task = entry.getValue();
      final Instant terminalAt = task.terminalAt();
      if (terminalAt != null && !terminalAt.isAfter(cutoff) && tasks.remove(entry.getKey(), task)) {
        removed++;
      }
    }
    return removed;
  }

  private void process(final QueryTask task) {
    updateTaskInQueue(task.id(), QueryTask::processing);

    try {
      final List<MatrixCell> result = matrixService.fetchMatrixResults(task.request());
      updateTaskInQueue(task.id(), current -> current.complete(result, clock.instant()));
    } catch (final Exception e) {
      LOG.error("Async task failed while computing result: taskId={}", task.id(), e);
      updateTaskInQueue(task.id(), current -> current.failed(clock.instant()));
    }
  }

  /** Thread-Safe method for updating the tasks in the queue. */
  private void updateTaskInQueue(final String id, final UnaryOperator<QueryTask> updater) {
    tasks.compute(id, (k, oldTask) -> {
      if (oldTask == null) {
        throw new IllegalArgumentException("Task not found: " + id);
      }

      final QueryTask newTask = updater.apply(oldTask);

      LOG.debug("Async task status changed: taskId={}, from={}, to={}",
          id,
          oldTask.status(),
          newTask.status());

      return newTask;
    });
  }
}
