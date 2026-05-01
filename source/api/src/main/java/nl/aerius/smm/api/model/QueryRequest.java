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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Domain model for a matrix query. */
public record QueryRequest(
    @NotBlank(message = "calculationVersion is required") String calculationVersion,

    @NotEmpty(message = "substances must be a non-empty list")
    List<@NotBlank(message = "each substance must be a non-blank string") String> substances,

    @NotEmpty(message = "resultType must be a non-empty list")
    List<@NotBlank(message = "each resultType must be a non-blank string") String> resultTypes,

    @NotNull(message = "sourceCharacteristics is required") @Valid SourceCharacteristics sourceCharacteristics,

    @NotEmpty(message = "meshPoints must be a non-empty list")
    List<@NotNull(message = "meshPoint must not be null") @Valid Point> meshPoints,

    @NotEmpty(message = "sourcePoints must be a non-empty list")
    List<@NotNull(message = "sourcePoint must not be null") @Valid Point> sourcePoints) {}
