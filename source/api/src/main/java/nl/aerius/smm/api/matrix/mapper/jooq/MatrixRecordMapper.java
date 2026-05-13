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
package nl.aerius.smm.api.matrix.mapper.jooq;

import org.springframework.stereotype.Component;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.generated.jooq.tables.records.MatrixRecord;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

@Component
public class MatrixRecordMapper {

  public MatrixDbRow toDbRow(final MatrixRecord record) {
    if (record == null) {
      return null;
    }
    return new MatrixDbRow(
        new MatrixFixedDimensions(record.getCalculationVersionId(), record.getSourceCharacteristicId()),
        new MatrixVariableDimensions(
            record.getSubstanceId(),
            record.getResultTypeId(),
            new Point(record.getSourceX(), record.getSourceY()),
            record.getMeshPointId()),
        record.getValue());
  }

  public MatrixRecord toRecord(final MatrixDbRow row) {
    if (row == null) {
      return null;
    }
    final MatrixRecord matrixRecord = new MatrixRecord();
    matrixRecord.setCalculationVersionId(row.fixedDimensions().calculationVersionId());
    matrixRecord.setSubstanceId(row.variableDimensions().substanceId());
    matrixRecord.setResultTypeId(row.variableDimensions().resultTypeId());
    matrixRecord.setSourceCharacteristicId(row.fixedDimensions().sourceCharacteristicId());
    matrixRecord.setSourceX(row.variableDimensions().sourcePoint().x());
    matrixRecord.setSourceY(row.variableDimensions().sourcePoint().y());
    matrixRecord.setMeshPointId(row.variableDimensions().meshPointId());
    matrixRecord.setValue((float) row.value());
    return matrixRecord;
  }
}
