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

import static nl.aerius.smm.api.generated.jooq.tables.SourceCharacteristics.SOURCE_CHARACTERISTICS;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import nl.aerius.smm.api.catalog.model.SourceCharacteristics;

@Repository
public class SourceCharacteristicsRepository {

  private final DSLContext dsl;

  public SourceCharacteristicsRepository(final DSLContext dsl) {
    this.dsl = dsl;
  }

  public Optional<Short> findId(final SourceCharacteristics sourceCharacteristics) {
    if (sourceCharacteristics.sourceCharacteristicId() != null) {
      return Optional.of(sourceCharacteristics.sourceCharacteristicId());
    }
    return dsl.select(SOURCE_CHARACTERISTICS.SOURCE_CHARACTERISTIC_ID)
        .from(SOURCE_CHARACTERISTICS)
        .where(SOURCE_CHARACTERISTICS.HEIGHT.eq((float) sourceCharacteristics.height()))
        .and(SOURCE_CHARACTERISTICS.HEAT_CONTENT.eq((float) sourceCharacteristics.heatContent()))
        .and(SOURCE_CHARACTERISTICS.SPREAD.eq((float) sourceCharacteristics.spread()))
        .and(SOURCE_CHARACTERISTICS.EMISSION_DIURNAL_VARIATION.eq((short) sourceCharacteristics.emissionDiurnalVariation()))
        .fetchOptional(SOURCE_CHARACTERISTICS.SOURCE_CHARACTERISTIC_ID);
  }
}
