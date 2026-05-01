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
package nl.aerius.smm.api.model;

import java.util.List;
import java.util.UUID;

/** Tracks one asynchronous matrix query. */
public record QueryTask(
    UUID id,
    QueryRequest request,
    QueryStatus status,
    List<MatrixResultRecord> results
) {
  /** Create task. TaskId will be generated and status is set to null. */
  public static QueryTask create(final QueryRequest request) {
    return new QueryTask(
        UUID.randomUUID(),
        request,
        null,
        null);
  }

  /** Task ACCEPTED to picked up */
  public QueryTask accepted() {
    return withStatus(QueryStatus.ACCEPTED);
  }

  /** Task REJECTED */
  public QueryTask rejected() {
    return withStatus(QueryStatus.REJECTED);
  }

  /** Task picked up for PROCESSING */
  public QueryTask processing() {
    return withStatus(QueryStatus.PROCESSING);
  }

  /** Task COMPLETED */
  public QueryTask complete(final List<MatrixResultRecord> result) {
    return new QueryTask(id, request, QueryStatus.COMPLETED, result);
  }

  /** Task FAILED during processing */
  public QueryTask failed() {
    return withStatus(QueryStatus.FAILED);
  }

  private QueryTask withStatus(QueryStatus status) {
    return new QueryTask(id, request, status, results);
  }
}
