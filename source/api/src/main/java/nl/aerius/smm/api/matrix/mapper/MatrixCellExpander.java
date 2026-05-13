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
package nl.aerius.smm.api.matrix.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.query.model.QueryRequest;

@Component
public class MatrixCellExpander {

  public List<MatrixCellKey> expand(final QueryRequest request) {
    if (request == null) {
      return null;
    }
    return request.sourcePoints().stream()
        .flatMap(sourcePoint ->
            request.substances().stream()
                .flatMap(substance ->
                    request.resultTypes().stream()
                        .flatMap(resultType ->
                            request.meshPoints().stream()
                                .map(meshPoint ->
                                    new MatrixCellKey(sourcePoint, meshPoint, substance, resultType)
                                )
                        )
                )
        )
        .toList();
  }
}
