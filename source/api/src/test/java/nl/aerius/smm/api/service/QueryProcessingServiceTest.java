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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.model.MatrixResultRecord;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.QueryResultResponse;
import nl.aerius.smm.api.model.QueryStatus;
import nl.aerius.smm.api.model.SourceCharacteristics;

@ExtendWith(MockitoExtension.class)
class QueryProcessingServiceTest {

  @Mock
  private MatrixService matrixService;

  private QueryProcessingService service;

  @BeforeEach
  void setUp() {
    service = new QueryProcessingService(Runnable::run, matrixService);
  }

  @Test
  void testCreateCompletes() {
    final QueryRequest request = sampleRequest();
    final List<MatrixResultRecord> results = List.of(sampleRecord());
    when(matrixService.fetchMatrixResults(any())).thenReturn(results);

    final UUID id = service.create(request);

    assertEquals(QueryStatus.COMPLETED, service.getStatus(id),
        "create with sync executor -> getStatus should return COMPLETED");
    final QueryResultResponse response = service.getResult(id);
    assertEquals(request, response.request(),
        "completed task -> getResult should return original request");
    assertEquals(results, response.results(),
        "completed task -> getResult should return matrix service results");

    final ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
    verify(matrixService).fetchMatrixResults(captor.capture());
    assertEquals(request, captor.getValue(),
        "matrix fetch -> should receive same QueryRequest as create()");

    assertThrows(TaskNotFoundException.class, () -> service.getStatus(id),
        "task removed after getResult -> getStatus should throw TaskNotFoundException");
    assertThrows(TaskNotFoundException.class, () -> service.getResult(id),
        "task removed after getResult -> getResult should throw TaskNotFoundException");
  }

  @Test
  void testStatusMissing() {
    assertThrows(TaskNotFoundException.class, () -> service.getStatus(UUID.randomUUID()),
        "unknown task id -> getStatus should throw TaskNotFoundException");
  }

  @Test
  void testResultMissing() {
    assertThrows(TaskNotFoundException.class, () -> service.getResult(UUID.randomUUID()),
        "unknown task id -> getResult should throw TaskNotFoundException");
  }

  @Test
  void testResultNotReady() {
    final List<Runnable> pending = new ArrayList<>();
    final Executor deferred = pending::add;
    service = new QueryProcessingService(deferred, matrixService);

    final UUID id = service.create(sampleRequest());

    final ResultNotReadyException notReady = assertThrows(
        ResultNotReadyException.class,
        () -> service.getResult(id),
        "getResult before async work -> should throw ResultNotReadyException");
    assertEquals(QueryStatus.ACCEPTED, notReady.getStatus(),
        "ResultNotReadyException -> should expose current task status ACCEPTED");

    when(matrixService.fetchMatrixResults(any())).thenReturn(List.of(sampleRecord()));
    pending.get(0).run();
    assertEquals(QueryStatus.COMPLETED, service.getStatus(id),
        "after queued runnable runs -> getStatus should return COMPLETED");
  }

  @Test
  void testQueueFull() {
    final Executor rejecting = command -> {
      throw new TaskRejectedException("simulated full queue");
    };
    service = new QueryProcessingService(rejecting, matrixService);

    final QueueFullException qf = assertThrows(
        QueueFullException.class,
        () -> service.create(sampleRequest()),
        "executor rejects submission -> create should throw QueueFullException");
    final UUID taskId = qf.getTaskId();
    assertEquals(QueryStatus.REJECTED, service.getStatus(taskId),
        "queue full -> task should remain REJECTED in queue");
  }

  @Test
  void testMatrixErrorFailed() {
    when(matrixService.fetchMatrixResults(any())).thenThrow(new IllegalStateException("matrix error"));

    final UUID id = service.create(sampleRequest());

    assertEquals(QueryStatus.FAILED, service.getStatus(id),
        "matrixService.fetchMatrixResults throws -> task should end FAILED");
  }

  private static QueryRequest sampleRequest() {
    return new QueryRequest(
        "v1",
        List.of("NOx"),
        List.of("concentration"),
        new SourceCharacteristics(null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(2), 1),
        List.of(new Point(1, 2)),
        List.of(new Point(3, 4)));
  }

  private static MatrixResultRecord sampleRecord() {
    return new MatrixResultRecord(new Point(3, 4), new Point(1, 2), "NOx", "concentration", 1.5);
  }
}
