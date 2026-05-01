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
package nl.aerius.smm.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.generated.openapi.api.QueryApi;
import nl.aerius.smm.api.generated.openapi.model.RestErrorMessage;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryStatusResponse;
import nl.aerius.smm.api.mapper.openapi.QueryTaskMapper;

/**
 * Maps query API exceptions to HTTP responses. Lives alongside {@link QueryController} because
 * OpenAPI's generated {@code QueryApiController} delegates to {@link nl.aerius.smm.api.generated.openapi.api.QueryApiDelegate};
 * exceptions raised in the delegate are not handled by {@code @ExceptionHandler} methods on the delegate class itself.
 */
@RestControllerAdvice(assignableTypes = QueryApi.class)
public class QueryApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(QueryApiExceptionHandler.class);

  private final QueryTaskMapper queryTaskMapper;

  public QueryApiExceptionHandler(final QueryTaskMapper queryTaskMapper) {
    this.queryTaskMapper = queryTaskMapper;
  }

  @ExceptionHandler(QueueFullException.class)
  public ResponseEntity<RestErrorMessage> handleQueueFull(final QueueFullException ex) {
    LOG.warn("Queue full exception caught.", ex);

    return ResponseEntity
        .status(HttpStatus.TOO_MANY_REQUESTS)
        .body(new RestErrorMessage().toBuilder()
            .code("TOO_MANY_REQUEST")
            .message("The system is currently processing the maximum number of requests. Please retry later.")
            .build());
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<RestErrorMessage> handleTaskNotFound(final TaskNotFoundException ex) {
    LOG.debug("Task not found exception caught.", ex);

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new RestErrorMessage().toBuilder()
            .code("TASK_NOT_FOUND")
            .message("No task found with id: " + ex.getTaskId())
            .build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<RestErrorMessage> handleBadRequest(final IllegalArgumentException ex) {
    LOG.debug("Invalid matrix query request.", ex);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new RestErrorMessage().toBuilder()
            .code("INVALID_QUERY_REQUEST")
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<RestErrorMessage> handleIncompleteMatrix(final IllegalStateException ex) {
    LOG.error("Matrix query incomplete (data mismatch).", ex);

    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new RestErrorMessage().toBuilder()
            .code("MATRIX_QUERY_INCOMPLETE")
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(ResultNotReadyException.class)
  public ResponseEntity<RestMatrixQueryStatusResponse> handleQueryResultNotReady(final ResultNotReadyException ex) {
    LOG.debug("Query result not ready yet exception caught.", ex);

    return ResponseEntity
        .accepted()
        .body(queryTaskMapper.toRestMatrixQueryStatusResponse(ex.getTaskId(), ex.getStatus()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<RestErrorMessage> handleGeneralException(final Exception ex) {
    LOG.info("Internal exception caught.", ex);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new RestErrorMessage().toBuilder()
            .code("INTERNAL_SERVER_ERROR")
            .build());
  }
}
