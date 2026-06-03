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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;
import nl.aerius.smm.api.matrix.model.ResolvedMatrixQuery;

class MatrixCellMapperTest {

  private static final MatrixFixedDimensions FIXED_DIMENSIONS = new MatrixFixedDimensions((short) 1, (short) 1);
  private static final ResolvedMatrixQuery RESOLVED = new ResolvedMatrixQuery(
      FIXED_DIMENSIONS,
      Map.of("NOx", (short) 1),
      Map.of("concentration", (short) 2),
      Map.of(new Point(1, 2), 100));

  private final MatrixCellMapper mapper = new MatrixCellMapper();

  @Test
  void testToDbKey() {
    final MatrixCellKey cellKey = new MatrixCellKey(new Point(3, 4), new Point(1, 2), "NOx", "concentration");
    assertEquals(new MatrixVariableDimensions((short) 1, (short) 2, new Point(3, 4), 100),
        mapper.toDbKey(RESOLVED, cellKey),
        "toDbKey -> should resolve names to IDs");
  }

  @Test
  void testToCell() {
    final MatrixDbRow row = new MatrixDbRow(
        FIXED_DIMENSIONS,
        new MatrixVariableDimensions((short) 1, (short) 2, new Point(3, 4), 100),
        1.5);
    assertEquals(new MatrixCell(new Point(3, 4), new Point(1, 2), "NOx", "concentration", 1.5),
        mapper.toCell(RESOLVED, row),
        "toCell -> should resolve IDs to names and point");
  }

  @Test
  void testToDbKeyUnknownSubstance() {
    final MatrixCellKey cellKey = new MatrixCellKey(new Point(3, 4), new Point(1, 2), "NH3", "concentration");
    assertThrows(IllegalStateException.class, () -> mapper.toDbKey(RESOLVED, cellKey),
        "toDbKey -> unknown substance should throw");
  }

  @Test
  void testToCellScopeMismatch() {
    final MatrixDbRow row = new MatrixDbRow(
        new MatrixFixedDimensions((short) 99, (short) 1),
        new MatrixVariableDimensions((short) 1, (short) 2, new Point(3, 4), 100),
        1.5);
    assertThrows(IllegalStateException.class, () -> mapper.toCell(RESOLVED, row),
        "toCell -> scope mismatch should throw");
  }

  @Test
  void testToCellUnresolvableRow() {
    final MatrixDbRow row = new MatrixDbRow(
        FIXED_DIMENSIONS,
        new MatrixVariableDimensions((short) 99, (short) 2, new Point(3, 4), 100),
        1.5);
    assertThrows(IllegalStateException.class, () -> mapper.toCell(RESOLVED, row),
        "toCell -> unknown substance ID should throw");
  }
}
