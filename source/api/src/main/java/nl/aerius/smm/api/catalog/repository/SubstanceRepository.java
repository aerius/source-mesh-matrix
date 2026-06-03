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
package nl.aerius.smm.api.catalog.repository;

import static nl.aerius.smm.api.generated.jooq.tables.Substances.SUBSTANCES;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import nl.aerius.smm.api.catalog.mapper.jooq.SubstanceRecordMapper;
import nl.aerius.smm.api.catalog.model.Substance;

@Repository
public class SubstanceRepository {

  private final DSLContext dsl;
  private final SubstanceRecordMapper mapper;

  public SubstanceRepository(final DSLContext dsl, final SubstanceRecordMapper mapper) {
    this.dsl = dsl;
    this.mapper = mapper;
  }

  public List<Substance> findAll() {
    return dsl.selectFrom(SUBSTANCES)
        .orderBy(SUBSTANCES.SUBSTANCE_ID)
        .fetch(mapper::toDomain);
  }
}
