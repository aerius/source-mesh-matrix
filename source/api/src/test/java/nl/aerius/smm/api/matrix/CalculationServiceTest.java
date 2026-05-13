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
package nl.aerius.smm.api.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.service.CalculationService;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

class CalculationServiceTest {

  private final CalculationService calculationService = new CalculationService();

  @Test
  void testCalculateAddsValueToEachKey() {
    final MatrixFixedDimensions fixedDimensions = new MatrixFixedDimensions((short) 1, (short) 1);
    final MatrixVariableDimensions variableDimensions1 = new MatrixVariableDimensions((short) 1, (short) 1, new Point(1, 2), 10);
    final MatrixVariableDimensions variableDimensions2 = new MatrixVariableDimensions((short) 2, (short) 2, new Point(5, 6), 11);

    final List<MatrixDbRow> rows = calculationService.calculate(fixedDimensions, List.of(variableDimensions1, variableDimensions2));

    assertEquals(2, rows.size(),
        "calculate -> should return one row per key");
    assertEquals(fixedDimensions, rows.get(0).fixedDimensions(),
        "calculate -> first row should use fixed dimensions");
    assertEquals(variableDimensions1, rows.get(0).variableDimensions(),
        "calculate -> first row should preserve variable dimensions");
    assertEquals(variableDimensions2, rows.get(1).variableDimensions(),
        "calculate -> second row should preserve variable dimensions");
  }
}
