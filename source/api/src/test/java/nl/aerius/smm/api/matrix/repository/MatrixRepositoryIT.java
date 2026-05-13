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

import static nl.aerius.smm.api.generated.jooq.tables.CalculationVersions.CALCULATION_VERSIONS;
import static nl.aerius.smm.api.generated.jooq.tables.Matrix.MATRIX;
import static nl.aerius.smm.api.generated.jooq.tables.MeshPoints.MESH_POINTS;
import static nl.aerius.smm.api.generated.jooq.tables.ResultTypes.RESULT_TYPES;
import static nl.aerius.smm.api.generated.jooq.tables.SourceCharacteristics.SOURCE_CHARACTERISTICS;
import static nl.aerius.smm.api.generated.jooq.tables.Substances.SUBSTANCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.aerius.smm.api.SMMApiApplication;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

@SpringBootTest(classes = SMMApiApplication.class)
class MatrixRepositoryIT {

  private static final short CALCULATION_VERSION_ID = 9001;
  private static final short SUBSTANCE_ID = 9001;
  private static final short RESULT_TYPE_ID = 9001;
  private static final short SOURCE_CHARACTERISTIC_ID = 9001;
  private static final int MESH_POINT_ID = 9_000_001;

  @Autowired
  private DSLContext dsl;

  @Autowired
  private MatrixRepository matrixRepository;

  @BeforeEach
  void seedReferenceData() {
    assumeClickHouseAvailable();
    dsl.execute(
        "ALTER TABLE matrix DELETE WHERE calculation_version_id = {0}",
        CALCULATION_VERSION_ID);

    insertIfAbsent(
        CALCULATION_VERSIONS,
        CALCULATION_VERSIONS.CALCULATION_VERSION_ID.eq(CALCULATION_VERSION_ID),
        () -> dsl.insertInto(CALCULATION_VERSIONS)
            .columns(CALCULATION_VERSIONS.CALCULATION_VERSION_ID, CALCULATION_VERSIONS.NAME)
            .values(CALCULATION_VERSION_ID, "it-version"));
    insertIfAbsent(
        SUBSTANCES,
        SUBSTANCES.SUBSTANCE_ID.eq(SUBSTANCE_ID),
        () -> dsl.insertInto(SUBSTANCES)
            .columns(SUBSTANCES.SUBSTANCE_ID, SUBSTANCES.NAME)
            .values(SUBSTANCE_ID, "IT-NOx"));
    insertIfAbsent(
        RESULT_TYPES,
        RESULT_TYPES.RESULT_TYPE_ID.eq(RESULT_TYPE_ID),
        () -> dsl.insertInto(RESULT_TYPES)
            .columns(RESULT_TYPES.RESULT_TYPE_ID, RESULT_TYPES.NAME)
            .values(RESULT_TYPE_ID, "it-concentration"));
    insertIfAbsent(
        SOURCE_CHARACTERISTICS,
        SOURCE_CHARACTERISTICS.SOURCE_CHARACTERISTIC_ID.eq(SOURCE_CHARACTERISTIC_ID),
        () -> dsl.insertInto(SOURCE_CHARACTERISTICS)
            .columns(
                SOURCE_CHARACTERISTICS.SOURCE_CHARACTERISTIC_ID,
                SOURCE_CHARACTERISTICS.HEAT_CONTENT,
                SOURCE_CHARACTERISTICS.HEIGHT,
                SOURCE_CHARACTERISTICS.SPREAD,
                SOURCE_CHARACTERISTICS.EMISSION_DIURNAL_VARIATION)
            .values(SOURCE_CHARACTERISTIC_ID, 10f, 1f, 2f, (short) 1));
    insertIfAbsent(
        MESH_POINTS,
        MESH_POINTS.MESH_POINT_ID.eq(MESH_POINT_ID),
        () -> dsl.insertInto(MESH_POINTS)
            .columns(MESH_POINTS.MESH_POINT_ID, MESH_POINTS.X, MESH_POINTS.Y)
            .values(MESH_POINT_ID, 100, 200));
  }

  private void assumeClickHouseAvailable() {
    try {
      dsl.fetchOne("SELECT 1");
    } catch (final Exception exception) {
      Assumptions.assumeTrue(
          false,
          "ClickHouse is required for MatrixRepositoryIT: " + exception.getMessage());
    }
  }

  private void insertIfAbsent(
      final org.jooq.Table<?> table,
      final org.jooq.Condition condition,
      final Runnable insert) {
    if (dsl.fetchCount(table, condition) == 0) {
      insert.run();
    }
  }

  @Test
  void testFindExistingReturnsMatchingRowsAndStorePersistsMissing() {
    final MatrixFixedDimensions fixedDimensions = sampleFixedDimensions();
    final List<MatrixVariableDimensions> cellDbKeys = sampleCellDbKeys();
    assertEquals(2, cellDbKeys.size(), "sample request -> two source points and one mesh point");

    final MatrixVariableDimensions firstDbKey = cellDbKeys.getFirst();
    final MatrixDbRow cached = new MatrixDbRow(fixedDimensions, firstDbKey, 42.0);
    matrixRepository.store(List.of(cached));

    final List<MatrixDbRow> found = matrixRepository.findExisting(fixedDimensions, cellDbKeys);

    assertEquals(1, found.size(),
        "findExisting -> should return one cached row");
    assertEquals(cached, found.getFirst(),
        "findExisting -> should return cached db row");

    final MatrixVariableDimensions missingDbKey = cellDbKeys.get(1);
    final MatrixDbRow missingRow = new MatrixDbRow(fixedDimensions, missingDbKey, 7.5);
    matrixRepository.store(List.of(missingRow));

    final List<MatrixDbRow> allFound = matrixRepository.findExisting(fixedDimensions, cellDbKeys);

    assertEquals(2, allFound.size(),
        "findExisting -> should return all rows after store");
    assertTrue(allFound.contains(cached),
        "findExisting -> should still contain previously stored row");
    assertTrue(allFound.contains(missingRow),
        "findExisting -> should contain newly stored row");
  }

  private static List<MatrixVariableDimensions> sampleCellDbKeys() {
    return List.of(
        new MatrixVariableDimensions(SUBSTANCE_ID, RESULT_TYPE_ID, new Point(1, 2), MESH_POINT_ID),
        new MatrixVariableDimensions(SUBSTANCE_ID, RESULT_TYPE_ID, new Point(3, 4), MESH_POINT_ID));
  }

  private static MatrixFixedDimensions sampleFixedDimensions() {
    return new MatrixFixedDimensions(CALCULATION_VERSION_ID, SOURCE_CHARACTERISTIC_ID);
  }
}
