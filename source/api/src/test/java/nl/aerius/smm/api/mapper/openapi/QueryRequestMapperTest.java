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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryRequest;
import nl.aerius.smm.api.TestApplication;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.SourceCharacteristics;

@SpringBootTest(classes = TestApplication.class)
class QueryRequestMapperTest {

  @Autowired
  private QueryRequestMapper mapper;

  @Test
  void testMapRoundTrip() {
    final QueryRequest original = sampleQueryRequest();
    final RestMatrixQueryRequest rest = mapper.toRestMatrixQueryRequest(original);
    final QueryRequest roundTripped = mapper.toQueryRequest(rest);

    assertEquals(original.calculationVersion(), roundTripped.calculationVersion(),
        "domain -> REST -> domain should preserve calculationVersion");
    assertEquals(original.substances(), roundTripped.substances(),
        "domain -> REST -> domain should preserve substances");
    assertEquals(original.resultTypes(), roundTripped.resultTypes(),
        "domain -> REST -> domain should preserve resultTypes");
    assertEquals(original.meshPoints(), roundTripped.meshPoints(),
        "domain -> REST -> domain should preserve meshPoints");
    assertEquals(original.sourcePoints(), roundTripped.sourcePoints(),
        "domain -> REST -> domain should preserve sourcePoints");

    assertEquals(
        original.sourceCharacteristics().height(),
        roundTripped.sourceCharacteristics().height(),
        "domain -> REST -> domain should preserve sourceCharacteristics height");
    assertEquals(
        original.sourceCharacteristics().heatContent(),
        roundTripped.sourceCharacteristics().heatContent(),
        "domain -> REST -> domain should preserve sourceCharacteristics heatContent");
    assertEquals(
        original.sourceCharacteristics().spread(),
        roundTripped.sourceCharacteristics().spread(),
        "domain -> REST -> domain should preserve sourceCharacteristics spread");
    assertEquals(
        original.sourceCharacteristics().emissionDiurnalVariation(),
        roundTripped.sourceCharacteristics().emissionDiurnalVariation(),
        "domain -> REST -> domain should preserve sourceCharacteristics emissionDiurnalVariation");
    assertNull(roundTripped.sourceCharacteristics().sourceCharacteristicId(),
        "domain -> REST -> domain should clear sourceCharacteristicId on SourceCharacteristics");
  }

  @Test
  void testMapNulls() {
    assertNull(mapper.toQueryRequest(null), "null REST -> domain QueryRequest should be null");
    assertNull(mapper.toRestMatrixQueryRequest(null), "null QueryRequest -> REST should be null");
    assertNull(mapper.toSourceCharacteristics(null), "null REST -> domain SourceCharacteristics should be null");
    assertNull(mapper.toRestSourceCharacteristics(null), "null SourceCharacteristics -> REST should be null");
    assertNull(mapper.toPoint(null), "null REST -> domain Point should be null");
    assertNull(mapper.toRestPoint(null), "null Point -> REST should be null");
  }

  private static QueryRequest sampleQueryRequest() {
    final SourceCharacteristics sourceCharacteristics = new SourceCharacteristics(
        (short) 99,
        new BigDecimal("15.5"),
        new BigDecimal("2.25"),
        new BigDecimal("0.75"),
        3);
    return new QueryRequest(
        "2024",
        List.of("NOx", "NH3"),
        List.of("concentration", "deposition"),
        sourceCharacteristics,
        List.of(new Point(1, 2), new Point(3, 4)),
        List.of(new Point(10, 20)));
  }
}
