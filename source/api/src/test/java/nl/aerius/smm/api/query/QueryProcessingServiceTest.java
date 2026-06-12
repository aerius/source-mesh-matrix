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
package nl.aerius.smm.api.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import nl.aerius.smm.api.config.QueryProperties;
import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.service.MatrixService;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.query.service.QueryProcessingService;
import nl.aerius.smm.api.query.model.QueryRequest;
import nl.aerius.smm.api.query.model.QueryResultResponse;
import nl.aerius.smm.api.query.model.QueryStatus;
import nl.aerius.smm.api.catalog.model.SourceCharacteristics;

@ExtendWith(MockitoExtension.class)
class QueryProcessingServiceTest {

  private static final Duration RETENTION = Duration.ofHours(1);
  private static final Instant START = Instant.parse("2026-05-30T10:00:00Z");

  @Mock
  private MatrixService matrixService;

  @Mock
  private Clock clock;

  private Instant currentTime;
  private QueryProcessingService service;

  @BeforeEach
  void setUp() {
    currentTime = START;
    lenient().when(clock.instant()).thenAnswer(invocation -> currentTime);
    service = new QueryProcessingService(
        Runnable::run,
        matrixService,
        clock,
        testQueryProperties());
  }

  /** Moves the mocked clock forward for retention/purge tests. */
  private void advance(final Duration duration) {
    currentTime = currentTime.plus(duration);
  }

  @Test
  void testCreateCompletes() {
    final QueryRequest request = sampleRequest();
    final List<MatrixCell> results = List.of(sampleRecord());
    when(matrixService.fetchMatrixResults(any())).thenReturn(results);

    final String id = service.create(request);

    assertEquals(QueryStatus.COMPLETED, service.getStatus(id),
        "Query create with synchronous executor -> getStatus should return COMPLETED");
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
    assertThrows(TaskNotFoundException.class, () -> service.getStatus("00000000-0000-0000-0000-000000000099"),
        "unknown task id -> getStatus should throw TaskNotFoundException");
  }

  @Test
  void testResultMissing() {
    assertThrows(TaskNotFoundException.class, () -> service.getResult("00000000-0000-0000-0000-000000000099"),
        "unknown task id -> getResult should throw TaskNotFoundException");
  }

  @Test
  void testResultNotReady() {
    final List<Runnable> pending = new ArrayList<>();
    final Executor deferred = pending::add;
    service = new QueryProcessingService(deferred, matrixService, clock, testQueryProperties());

    final String id = service.create(sampleRequest());

    final ResultNotReadyException exception = assertThrows(
        ResultNotReadyException.class,
        () -> service.getResult(id),
        "getResult before async work -> should throw ResultNotReadyException");
    assertEquals(QueryStatus.ACCEPTED, exception.getStatus(),
        "ResultNotReadyException -> should expose current task status ACCEPTED");

    when(matrixService.fetchMatrixResults(any())).thenReturn(List.of(sampleRecord()));
    pending.getFirst().run();
    assertEquals(QueryStatus.COMPLETED, service.getStatus(id),
        "after queued runnable runs -> getStatus should return COMPLETED");
  }

  @Test
  void testQueueFull() {
    final Executor rejecting = command -> {
      throw new TaskRejectedException("simulated full queue");
    };
    service = new QueryProcessingService(rejecting, matrixService, clock, testQueryProperties());

    final QueryRequest request = sampleRequest();
    final QueueFullException exception = assertThrows(
        QueueFullException.class,
        () -> service.create(request),
        "executor rejects submission -> create should throw QueueFullException");
    final String taskId = exception.getTaskId();
    assertEquals(QueryStatus.REJECTED, service.getStatus(taskId),
        "queue full -> task should remain REJECTED in queue");
  }

  @Test
  void testMatrixErrorFailed() {
    when(matrixService.fetchMatrixResults(any())).thenThrow(new IllegalStateException("matrix error"));

    final String id = service.create(sampleRequest());

    assertEquals(QueryStatus.FAILED, service.getStatus(id),
        "matrixService.fetchMatrixResults throws -> task should end FAILED");
  }

  @Test
  void testPurgeRemovesFailedTaskAfterRetention() {
    when(matrixService.fetchMatrixResults(any())).thenThrow(new IllegalStateException("matrix error"));

    final String id = service.create(sampleRequest());
    assertEquals(QueryStatus.FAILED, service.getStatus(id),
        "matrixService.fetchMatrixResults throws -> task should end FAILED before purge");

    advance(RETENTION.plusSeconds(1));
    service.purgeExpiredTerminalTasks();

    assertThrows(TaskNotFoundException.class, () -> service.getStatus(id),
        "failed task past retention -> getStatus should throw TaskNotFoundException");
  }

  @Test
  void testPurgeRemovesRejectedTaskAfterRetention() {
    final Executor rejecting = command -> {
      throw new TaskRejectedException("simulated full queue");
    };
    service = new QueryProcessingService(rejecting, matrixService, clock, testQueryProperties());

    final QueryRequest request = sampleRequest();
    final QueueFullException exception = assertThrows(
        QueueFullException.class,
        () -> service.create(request),
        "executor rejects submission -> create should throw QueueFullException");
    final String taskId = exception.getTaskId();

    advance(RETENTION.plusSeconds(1));
    service.purgeExpiredTerminalTasks();

    assertThrows(TaskNotFoundException.class, () -> service.getStatus(taskId),
        "rejected task past retention -> getStatus should throw TaskNotFoundException");
  }

  @Test
  void testPurgeRemovesCompletedTaskNotFetchedAfterRetention() {
    when(matrixService.fetchMatrixResults(any())).thenReturn(List.of(sampleRecord()));

    final String id = service.create(sampleRequest());
    assertEquals(QueryStatus.COMPLETED, service.getStatus(id),
        "completed task -> getStatus should return COMPLETED before purge");

    advance(RETENTION.plusSeconds(1));
    service.purgeExpiredTerminalTasks();

    assertThrows(TaskNotFoundException.class, () -> service.getStatus(id),
        "completed task not fetched past retention -> getStatus should throw TaskNotFoundException");
  }

  @Test
  void testPurgeKeepsTerminalTaskBeforeRetention() {
    when(matrixService.fetchMatrixResults(any())).thenThrow(new IllegalStateException("matrix error"));

    final String id = service.create(sampleRequest());
    assertEquals(QueryStatus.FAILED, service.getStatus(id),
        "failed task -> getStatus should return FAILED before retention expires");

    advance(RETENTION.minusSeconds(1));
    service.purgeExpiredTerminalTasks();

    assertEquals(QueryStatus.FAILED, service.getStatus(id),
        "failed task within retention -> getStatus should still return FAILED");
  }

  private static QueryRequest sampleRequest() {
    return new QueryRequest(
        "v1",
        List.of("NOx"),
        List.of("concentration"),
        new SourceCharacteristics(null, 1, 10, 2, 1),
        List.of(new Point(1, 2)),
        List.of(new Point(3, 4)));
  }

  private static MatrixCell sampleRecord() {
    return new MatrixCell(new Point(3, 4), new Point(1, 2), "NOx", "concentration", 1.5);
  }

  private static QueryProperties testQueryProperties() {
    return new QueryProperties(
        new QueryProperties.ExecutorProperties(3, 10, 100, "req-"),
        new QueryProperties.TaskProperties(RETENTION, Duration.ofMinutes(15)));
  }
}
