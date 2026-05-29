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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import nl.aerius.smm.api.catalog.model.SourceCharacteristics;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.mapper.MatrixCellExpander;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.query.model.QueryRequest;

class MatrixCellExpanderTest {

  private final MatrixCellExpander expander = new MatrixCellExpander();

  @Test
  void testCellKeyWithValueRoundTrip() {
    final MatrixCell original = sampleCell();

    final MatrixCellKey key = new MatrixCellKey(
        original.sourcePoint(),
        original.meshPoint(),
        original.substance(),
        original.resultType());
    final MatrixCell roundTripped = key.withValue(original.value());

    assertEquals(original, roundTripped,
        "cell -> key -> cell with value should preserve MatrixCell");
  }

  @Test
  void testExpandFromRequest() {
    final QueryRequest request = sampleQueryRequest();

    final List<MatrixCellKey> keys = expander.expand(request);

    assertEquals(8, keys.size(),
        "request -> keys should expand 2 source points × 2 substances × 2 result types × 1 mesh point");
    assertEquals(2, keys.stream().map(MatrixCellKey::sourcePoint).distinct().count(),
        "request -> keys should contain 2 distinct source points");
    assertEquals(2, keys.stream().map(MatrixCellKey::substance).distinct().count(),
        "request -> keys should contain 2 distinct substances");
    assertEquals(2, keys.stream().map(MatrixCellKey::resultType).distinct().count(),
        "request -> keys should contain 2 distinct result types");
    assertEquals(1, keys.stream().map(MatrixCellKey::meshPoint).distinct().count(),
        "request -> keys should contain 1 distinct mesh point");
  }

  @Test
  void testExpandNullRequest() {
    assertNull(expander.expand(null),
        "null QueryRequest -> keys should be null");
  }

  private static MatrixCell sampleCell() {
    return new MatrixCell(new Point(1, 2), new Point(3, 4), "NOx", "concentration", 12.34);
  }

  private static QueryRequest sampleQueryRequest() {
    return new QueryRequest(
        "v1",
        List.of("NOx", "NH3"),
        List.of("concentration", "deposition"),
        new SourceCharacteristics(null, 1, 10, 2, 1),
        List.of(new Point(3, 4)),
        List.of(new Point(1, 2), new Point(5, 6)));
  }
}
