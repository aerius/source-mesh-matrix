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

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryResultResponse;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixResultRecord;
import nl.aerius.smm.api.model.MatrixResultRecord;
import nl.aerius.smm.api.model.QueryResultResponse;

@Mapper(componentModel = "spring", uses = QueryRequestMapper.class)
public interface QueryResultMapper {

  @Mapping(target = "query", source = "request")
  @Mapping(target = "records", source = "results")
  RestMatrixQueryResultResponse toRestMatrixQueryResultResponse(QueryResultResponse response);

  RestMatrixResultRecord toRestMatrixResultRecord(MatrixResultRecord record);

  default Float map(final Double value) {
    return value == null ? null : value.floatValue();
  }
}
