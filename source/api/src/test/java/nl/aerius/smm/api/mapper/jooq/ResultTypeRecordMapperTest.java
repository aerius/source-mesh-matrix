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
import org.mapstruct.factory.Mappers;

import nl.aerius.smm.api.generated.jooq.tables.records.ResultTypesRecord;
import nl.aerius.smm.api.catalog.model.ResultType;

class ResultTypeRecordMapperTest {

  private final ResultTypeRecordMapper mapper = Mappers.getMapper(ResultTypeRecordMapper.class);

  @Test
  void testMapDomainRoundTrip() {
    final ResultType domain = new ResultType((short) 8, "concentration");
    assertEquals(domain, mapper.toDomain(mapper.toRecord(domain)), "domain -> record -> domain should preserve ResultType");
  }

  @Test
  void testMapRecordRoundTrip() {
    final ResultTypesRecord record = new ResultTypesRecord();
    record.setResultTypeId((short) 9);
    record.setName("deposition");
    final ResultTypesRecord back = mapper.toRecord(mapper.toDomain(record));
    assertEquals((short) 9, back.getResultTypeId(), "record -> domain -> record should preserve resultTypeId");
    assertEquals("deposition", back.getName(), "record -> domain -> record should preserve name");
  }

  @Test
  void testMapNulls() {
    assertNull(mapper.toDomain(null), "toDomain(null) should be null");
    assertNull(mapper.toRecord(null), "toRecord(null) should be null");
  }
}
