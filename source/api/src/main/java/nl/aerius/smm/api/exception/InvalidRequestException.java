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

public final class InvalidRequestException extends RuntimeException {

  public static final String INVALID_QUERY_ID = "INVALID_QUERY_ID";
  public static final String INVALID_QUERY_REQUEST = "INVALID_QUERY_REQUEST";

  private final String code;

  public InvalidRequestException(final String code, final String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
