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
package nl.aerius.smm.api.mapper.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.aerius.smm.api.TestApplication;
import nl.aerius.smm.api.model.CalculationVersion;
import nl.aerius.smm.api.model.ResultType;
import nl.aerius.smm.api.model.Substance;

@SpringBootTest(classes = TestApplication.class)
class CatalogResponseMapperTest {

  @Autowired
  private CatalogResponseMapper mapper;

  @Test
  void testMapCalculationVersions() {
    final List<CalculationVersion> versions = List.of(
        new CalculationVersion((short) 1, "2024"),
        new CalculationVersion((short) 2, "2030"));
    assertEquals(
        List.of("2024", "2030"),
        mapper.toRestCalculationVersionCatalogResponse(versions).getCalculationVersions(),
        "versions -> REST should preserve CalculationVersion");
  }

  @Test
  void testMapSubstances() {
    final List<Substance> substances = List.of(
        new Substance((short) 10, "NOx"),
        new Substance((short) 11, "NH3"));
    assertEquals(
        List.of("NOx", "NH3"),
        mapper.toRestSubstanceCatalogResponse(substances).getSubstances(),
        "substances -> REST should preserve Substance");
  }

  @Test
  void testMapResultTypes() {
    final List<ResultType> resultTypes = List.of(
        new ResultType((short) 20, "concentration"),
        new ResultType((short) 21, "deposition"));
    assertEquals(
        List.of("concentration", "deposition"),
        mapper.toRestResultTypeCatalogResponse(resultTypes).getResultTypes(),
        "resultTypes -> REST should preserve ResultType");
  }

  @Test
  void testMapNullCalculationVersions() {
    assertTrue(
        mapper.toRestCalculationVersionCatalogResponse(null).getCalculationVersions().isEmpty(),
        "null versions -> REST names should be empty");
  }

  @Test
  void testMapNullSubstances() {
    assertTrue(
        mapper.toRestSubstanceCatalogResponse(null).getSubstances().isEmpty(),
        "null substances -> REST names should be empty");
  }

  @Test
  void testMapNullResultTypes() {
    assertTrue(
        mapper.toRestResultTypeCatalogResponse(null).getResultTypes().isEmpty(),
        "null resultTypes -> REST names should be empty");
  }
}
