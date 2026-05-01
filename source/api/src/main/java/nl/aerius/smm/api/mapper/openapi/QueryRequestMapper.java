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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryRequest;
import nl.aerius.smm.api.generated.openapi.model.RestPoint;
import nl.aerius.smm.api.generated.openapi.model.RestSourceCharacteristics;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.SourceCharacteristics;

@Mapper(componentModel = "spring")
public interface QueryRequestMapper {

  QueryRequest toQueryRequest(RestMatrixQueryRequest request);

  RestMatrixQueryRequest toRestMatrixQueryRequest(QueryRequest request);

  @Mapping(target = "sourceCharacteristicId", ignore = true)
  SourceCharacteristics toSourceCharacteristics(RestSourceCharacteristics sourceCharacteristics);

  RestSourceCharacteristics toRestSourceCharacteristics(SourceCharacteristics sourceCharacteristics);

  Point toPoint(RestPoint point);

  RestPoint toRestPoint(Point point);
}
