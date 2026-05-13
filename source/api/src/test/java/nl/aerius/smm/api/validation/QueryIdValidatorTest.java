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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;

import nl.aerius.smm.api.exception.InvalidRequestException;

class QueryIdValidatorTest {

  private final QueryIdValidator validator =
      new QueryIdValidator(Validation.buildDefaultValidatorFactory().getValidator());

  @Test
  void testAcceptsUuidString() {
    validator.validateQueryId("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
  }

  @Test
  void testAcceptsShortAlphanumericWithDash() {
    validator.validateQueryId("Ab-09");
  }

  @Test
  void testRejectsNullBlankUnderscoreAndTooLong() {
    assertViolation("queryId is required", () -> validator.validateQueryId(null));
    assertViolation("queryId must not be blank", () -> validator.validateQueryId("   "));
    assertViolation("queryId must not be blank", () -> validator.validateQueryId(""));
    assertViolation(
        "queryId may only contain ASCII letters, digits, and dashes (a-z, A-Z, 0-9, -)",
        () -> validator.validateQueryId("no_underscore"));
    assertViolation(
        "queryId may only contain ASCII letters, digits, and dashes (a-z, A-Z, 0-9, -)",
        () -> validator.validateQueryId("a/b"));
    assertViolation(
        "queryId may only contain ASCII letters, digits, and dashes (a-z, A-Z, 0-9, -)",
        () -> validator.validateQueryId("a%2F"));
    assertViolation("queryId must be at most 256 characters", () -> validator.validateQueryId("a".repeat(257)));
  }

  private static void assertViolation(final String expectedSubstring, final Runnable call) {
    final InvalidRequestException ex = assertThrows(InvalidRequestException.class, call::run);
    assertEquals(InvalidRequestException.INVALID_QUERY_ID, ex.getCode());
    assertTrue(ex.getMessage().contains(expectedSubstring),
        "expected message fragment <" + expectedSubstring + "> in <" + ex.getMessage() + ">");
  }

  @Test
  void testJoinsMultipleViolationsSorted() {
    final InvalidRequestException ex = assertThrows(
        InvalidRequestException.class,
        () -> validator.validateQueryId(" ".repeat(300)));
    assertEquals(InvalidRequestException.INVALID_QUERY_ID, ex.getCode());
    assertTrue(ex.getMessage().contains("queryId must not be blank"));
    assertTrue(ex.getMessage().contains("queryId must be at most 256 characters"));
    assertTrue(ex.getMessage().contains("; "));
  }
}
