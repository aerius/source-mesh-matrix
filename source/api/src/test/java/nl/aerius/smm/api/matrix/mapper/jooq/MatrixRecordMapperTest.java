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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.generated.jooq.tables.records.MatrixRecord;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

class MatrixRecordMapperTest {

  private final MatrixRecordMapper mapper = new MatrixRecordMapper();

  @Test
  void testMapDomainRoundTrip() {
    final MatrixDbRow domain = new MatrixDbRow(
        new MatrixFixedDimensions((short) 1, (short) 2),
        new MatrixVariableDimensions((short) 3, (short) 4, new Point(10, 20), 100),
        1.5);
    assertEquals(domain, mapper.toDbRow(mapper.toRecord(domain)),
        "domain -> record -> domain should preserve MatrixDbRow");
  }

  @Test
  void testMapRecordRoundTrip() {
    final MatrixRecord record = new MatrixRecord();
    record.setCalculationVersionId((short) 5);
    record.setSubstanceId((short) 6);
    record.setResultTypeId((short) 7);
    record.setSourceCharacteristicId((short) 8);
    record.setSourceX(30);
    record.setSourceY(40);
    record.setMeshPointId(200);
    record.setValue(2.5f);
    final MatrixRecord back = mapper.toRecord(mapper.toDbRow(record));
    assertEquals((short) 5, back.getCalculationVersionId(), "record -> domain -> record should preserve calculationVersionId");
    assertEquals((short) 6, back.getSubstanceId(), "record -> domain -> record should preserve substanceId");
    assertEquals((short) 7, back.getResultTypeId(), "record -> domain -> record should preserve resultTypeId");
    assertEquals((short) 8, back.getSourceCharacteristicId(), "record -> domain -> record should preserve sourceCharacteristicId");
    assertEquals(30, back.getSourceX(), "record -> domain -> record should preserve sourceX");
    assertEquals(40, back.getSourceY(), "record -> domain -> record should preserve sourceY");
    assertEquals(200, back.getMeshPointId(), "record -> domain -> record should preserve meshPointId");
    assertEquals(2.5f, back.getValue(), "record -> domain -> record should preserve value");
  }

  @Test
  void testMapNulls() {
    assertNull(mapper.toDbRow(null), "toDbRow(null) should be null");
    assertNull(mapper.toRecord(null), "toRecord(null) should be null");
  }
}
