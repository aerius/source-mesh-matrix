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
package nl.aerius.smm.api.query.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import nl.aerius.smm.api.matrix.model.MatrixCell;

/** Tracks one asynchronous matrix query. */
public record QueryTask(
    String id,
    QueryRequest request,
    QueryStatus status,
    List<MatrixCell> results,
    Instant endedAt
) {
  /** Create task. TaskId will be generated and status is set to null. */
  public static QueryTask create(final QueryRequest request) {
    return new QueryTask(
        UUID.randomUUID().toString(),
        request,
        null,
        null,
        null);
  }

  /** Task ACCEPTED to picked up */
  public QueryTask accepted() {
    return new QueryTask(id, request, QueryStatus.ACCEPTED, results, endedAt);
  }

  /** Task REJECTED */
  public QueryTask rejected(final Instant endedAt) {
    return new QueryTask(id, request, QueryStatus.REJECTED, results, endedAt);
  }

  /** Task picked up for PROCESSING */
  public QueryTask processing() {
    return new QueryTask(id, request, QueryStatus.PROCESSING, results, endedAt);
  }

  /** Task COMPLETED */
  public QueryTask complete(final List<MatrixCell> results, final Instant endedAt) {
    return new QueryTask(id, request, QueryStatus.COMPLETED, results, endedAt);
  }

  /** Task FAILED during processing */
  public QueryTask failed(final Instant endedAt) {
    return new QueryTask(id, request, QueryStatus.FAILED, results, endedAt);
  }
}
