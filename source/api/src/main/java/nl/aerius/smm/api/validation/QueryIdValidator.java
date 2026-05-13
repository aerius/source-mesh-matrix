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
package nl.aerius.smm.api.validation;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Component;

import nl.aerius.smm.api.exception.InvalidRequestException;
import nl.aerius.smm.api.model.QueryId;

@Component
public class QueryIdValidator {

  private final Validator validator;

  public QueryIdValidator(final Validator validator) {
    this.validator = validator;
  }

  public void validateQueryId(final String queryId) {
    if (queryId == null) {
      throw new InvalidRequestException(InvalidRequestException.INVALID_QUERY_ID, "queryId is required");
    }
    final Set<ConstraintViolation<QueryId>> violations = validator.validate(new QueryId(queryId));
    if (!violations.isEmpty()) {
      final String message = violations.stream()
          .map(ConstraintViolation::getMessage)
          .sorted()
          .collect(Collectors.joining("; "));
      throw new InvalidRequestException(InvalidRequestException.INVALID_QUERY_ID, message);
    }
  }
}
