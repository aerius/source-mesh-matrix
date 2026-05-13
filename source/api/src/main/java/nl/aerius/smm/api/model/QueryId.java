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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Domain model for a the queryId. Used for input validation. */
public record QueryId(
    @NotBlank(message = "queryId must not be blank")
    @Size(max = 256, message = "queryId must be at most 256 characters")
    @jakarta.validation.constraints.Pattern(
        regexp = "^[A-Za-z0-9-]+$",
        message = "queryId may only contain ASCII letters, digits, and dashes (a-z, A-Z, 0-9, -)")
    String value
) {}
