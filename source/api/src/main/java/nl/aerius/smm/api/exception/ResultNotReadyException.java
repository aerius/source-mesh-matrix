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
package nl.aerius.smm.api.exception;

import java.util.UUID;

import nl.aerius.smm.api.model.QueryStatus;

public class ResultNotReadyException extends RuntimeException {

  private final UUID taskId;
  private final QueryStatus status;

  public ResultNotReadyException(final UUID taskId, final QueryStatus status) {
    super("Query result not ready for task " + taskId + " with status " + status);
    this.taskId = taskId;
    this.status = status;
  }

  public UUID getTaskId() {
    return taskId;
  }

  public QueryStatus getStatus() {
    return status;
  }
}
