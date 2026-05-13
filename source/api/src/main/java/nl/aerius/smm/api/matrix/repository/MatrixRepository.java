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
package nl.aerius.smm.api.matrix.repository;

import static nl.aerius.smm.api.generated.jooq.tables.Matrix.MATRIX;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.val;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Row5;
import org.springframework.stereotype.Repository;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.generated.jooq.tables.records.MatrixRecord;
import nl.aerius.smm.api.matrix.mapper.jooq.MatrixRecordMapper;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

@Repository
public class MatrixRepository {

  private final DSLContext dsl;
  private final MatrixRecordMapper matrixRecordMapper;

  public MatrixRepository(final DSLContext dsl, final MatrixRecordMapper matrixRecordMapper) {
    this.dsl = dsl;
    this.matrixRecordMapper = matrixRecordMapper;
  }

  public List<MatrixDbRow> findExisting(
      final MatrixFixedDimensions fixedDimensions,
      final List<MatrixVariableDimensions> variableDimensionsList) {
    if (variableDimensionsList.isEmpty()) {
      return List.of();
    }
    final List<Row5<Short, Short, Integer, Integer, Integer>> lookupRows = variableDimensionsList.stream()
        .map(this::toRow)
        .toList();

    return dsl.selectFrom(MATRIX)
        .where(MATRIX.CALCULATION_VERSION_ID.eq(fixedDimensions.calculationVersionId()))
        .and(MATRIX.SOURCE_CHARACTERISTIC_ID.eq(fixedDimensions.sourceCharacteristicId()))
        .and(row(
            MATRIX.SUBSTANCE_ID,
            MATRIX.RESULT_TYPE_ID,
            MATRIX.SOURCE_X,
            MATRIX.SOURCE_Y,
            MATRIX.MESH_POINT_ID)
            .in(lookupRows))
        .fetch(matrixRecordMapper::toDbRow);
  }

  public void store(final List<MatrixDbRow> rows) {
    if (rows.isEmpty()) {
      return;
    }

    final var insertStep = dsl.insertInto(
        MATRIX,
        MATRIX.CALCULATION_VERSION_ID,
        MATRIX.SUBSTANCE_ID,
        MATRIX.RESULT_TYPE_ID,
        MATRIX.SOURCE_CHARACTERISTIC_ID,
        MATRIX.SOURCE_X,
        MATRIX.SOURCE_Y,
        MATRIX.MESH_POINT_ID,
        MATRIX.VALUE);

    final List<org.jooq.Row8<Short, Short, Short, Short, Integer, Integer, Integer, Float>> insertRows = new ArrayList<>();
    for (final MatrixDbRow row : rows) {
      final MatrixRecord dbRecord = matrixRecordMapper.toRecord(row);
      insertRows.add(row(
          val(dbRecord.getCalculationVersionId()),
          val(dbRecord.getSubstanceId()),
          val(dbRecord.getResultTypeId()),
          val(dbRecord.getSourceCharacteristicId()),
          val(dbRecord.getSourceX()),
          val(dbRecord.getSourceY()),
          val(dbRecord.getMeshPointId()),
          val(dbRecord.getValue())));
    }
    insertStep.valuesOfRows(insertRows).execute();
  }

  private Row5<Short, Short, Integer, Integer, Integer> toRow(final MatrixVariableDimensions variableDimensions) {
    final Point sourcePoint = variableDimensions.sourcePoint();
    return row(
        val(variableDimensions.substanceId()),
        val(variableDimensions.resultTypeId()),
        val(sourcePoint.x()),
        val(sourcePoint.y()),
        val(variableDimensions.meshPointId()));
  }
}
