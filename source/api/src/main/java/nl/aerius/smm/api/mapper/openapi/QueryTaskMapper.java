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
package nl.aerius.smm.api.mapper.openapi;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryStatusResponse;
import nl.aerius.smm.api.model.QueryStatus;
import nl.aerius.smm.api.web.MatrixQueryResourceLinks;

@Mapper(componentModel = "spring")
public abstract class QueryTaskMapper {

  @Autowired
  protected MatrixQueryResourceLinks matrixQueryResourceLinks;

  public RestMatrixQueryStatusResponse toRestMatrixQueryStatusResponse(final UUID queryId, final QueryStatus status) {
    return RestMatrixQueryStatusResponse.builder()
        .queryId(queryId)
        .status(map(status))
        .statusUrl(matrixQueryResourceLinks.relativeStatusPath(queryId))
        .resultUrl(status == QueryStatus.COMPLETED ? matrixQueryResourceLinks.relativeResultPath(queryId) : null)
        .build();
  }

  protected RestMatrixQueryStatusResponse.StatusEnum map(final QueryStatus status) {
    if (status == null) {
      return RestMatrixQueryStatusResponse.StatusEnum.PROCESSING;
    }

    return switch (status) {
      case ACCEPTED, PROCESSING -> RestMatrixQueryStatusResponse.StatusEnum.PROCESSING;
      case COMPLETED -> RestMatrixQueryStatusResponse.StatusEnum.COMPLETED;
      case REJECTED -> RestMatrixQueryStatusResponse.StatusEnum.REJECTED;
      case FAILED -> RestMatrixQueryStatusResponse.StatusEnum.FAILED;
    };
  }
}
