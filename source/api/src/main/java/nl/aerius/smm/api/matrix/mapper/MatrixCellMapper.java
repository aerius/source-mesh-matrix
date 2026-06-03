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

import org.springframework.stereotype.Component;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.model.ResolvedMatrixQuery;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

/**
 * Maps domain cell models and DB variable dimensions. The fixed dimensions are separated from the variable dimensions.
 */
@Component
public class MatrixCellMapper {

  public MatrixVariableDimensions toDbKey(final ResolvedMatrixQuery resolved, final MatrixCellKey cellKey) {
    final Short substanceId = resolved.substanceIdsByName().get(cellKey.substance());
    if (substanceId == null) {
      throw new IllegalStateException("Unknown substance in this query: " + cellKey.substance());
    }

    final Short resultTypeId = resolved.resultTypeIdsByName().get(cellKey.resultType());
    if (resultTypeId == null) {
      throw new IllegalStateException("Unknown result type in this query: " + cellKey.resultType());
    }

    final Integer meshPointId = resolved.meshPointIdsByPoint().get(cellKey.meshPoint());
    if (meshPointId == null) {
      throw new IllegalStateException("Unknown mesh point in this query: (" + cellKey.meshPoint().x() + ", " + cellKey.meshPoint().y() + ")");
    }

    return new MatrixVariableDimensions(
        substanceId,
        resultTypeId,
        cellKey.sourcePoint(),
        meshPointId);
  }

  public MatrixCell toCell(final ResolvedMatrixQuery resolved, final MatrixDbRow row) {
    if (!row.fixedDimensions().equals(resolved.fixedDimensions())) {
      throw new IllegalStateException("MatrixDbRow fixed dimensions do not match this query: " + row);
    }
    final String substance = resolved.substanceNamesById().get(row.variableDimensions().substanceId());
    if (substance == null) {
      throw new IllegalStateException("Unknown substance id in this query: " + row.variableDimensions().substanceId());
    }

    final String resultType = resolved.resultTypeNamesById().get(row.variableDimensions().resultTypeId());
    if (resultType == null) {
      throw new IllegalStateException("Unknown result type id in this query: " + row.variableDimensions().resultTypeId());
    }

    final Point meshPoint = resolved.meshPointsById().get(row.variableDimensions().meshPointId());
    if (meshPoint == null) {
      throw new IllegalStateException("Unknown mesh point id in this query: " + row.variableDimensions().meshPointId());
    }
    return new MatrixCell(
        row.variableDimensions().sourcePoint(),
        meshPoint,
        substance,
        resultType,
        row.value());
  }

}
