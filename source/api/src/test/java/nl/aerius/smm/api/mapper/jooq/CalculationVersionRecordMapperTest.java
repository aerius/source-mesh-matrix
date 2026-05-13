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
package nl.aerius.smm.api.catalog.mapper.jooq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.aerius.smm.api.TestApplication;
import nl.aerius.smm.api.generated.jooq.tables.records.CalculationVersionsRecord;
import nl.aerius.smm.api.catalog.model.CalculationVersion;

@SpringBootTest(classes = TestApplication.class)
class CalculationVersionRecordMapperTest {

  @Autowired
  private CalculationVersionRecordMapper mapper;

  @Test
  void testMapDomainRoundTrip() {
    final CalculationVersion domain = new CalculationVersion((short) 3, "2024");
    assertEquals(domain, mapper.toDomain(mapper.toRecord(domain)), "domain -> record -> domain should preserve CalculationVersion");
  }

  @Test
  void testMapRecordRoundTrip() {
    final CalculationVersionsRecord record = new CalculationVersionsRecord();
    record.setCalculationVersionId((short) 100);
    record.setName("2030");
    final CalculationVersionsRecord back = mapper.toRecord(mapper.toDomain(record));
    assertEquals((short) 100, back.getCalculationVersionId(), "record -> domain -> record should preserve calculationVersionId");
    assertEquals("2030", back.getName(), "record -> domain -> record should preserve name");
  }

  @Test
  void testMapNulls() {
    assertNull(mapper.toDomain(null), "toDomain(null) should be null");
    assertNull(mapper.toRecord(null), "toRecord(null) should be null");
  }
}
