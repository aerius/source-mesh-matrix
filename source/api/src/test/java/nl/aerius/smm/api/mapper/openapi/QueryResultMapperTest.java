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

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryResultResponse;
import nl.aerius.smm.api.TestApplication;
import nl.aerius.smm.api.model.MatrixResultRecord;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.QueryResultResponse;
import nl.aerius.smm.api.model.SourceCharacteristics;

@SpringBootTest(classes = TestApplication.class)
class QueryResultMapperTest {

  @Autowired
  private QueryResultMapper queryResultMapper;

  @Test
  void testMapQueryResultResponseToRest() {
    final QueryRequest request = new QueryRequest(
        "2030",
        List.of("SO2"),
        List.of("concentration"),
        new SourceCharacteristics(null, new BigDecimal("5"), new BigDecimal("1.1"), new BigDecimal("0.2"), 1),
        List.of(new Point(0, 0)),
        List.of(new Point(1, 1)));
    final MatrixResultRecord r1 = new MatrixResultRecord(
        new Point(1, 1),
        new Point(2, 3),
        "NOx",
        "concentration",
        1.25);
    final MatrixResultRecord r2 = new MatrixResultRecord(
        new Point(4, 5),
        new Point(6, 7),
        "NH3",
        "deposition",
        null);
    final QueryResultResponse response = new QueryResultResponse(request, List.of(r1, r2));

    final RestMatrixQueryResultResponse rest = queryResultMapper.toRestMatrixQueryResultResponse(response);

    assertEquals("2030", rest.getQuery().getCalculationVersion(),
        "domain -> REST nested query should preserve calculationVersion");
    assertEquals(List.of("SO2"), rest.getQuery().getSubstances(),
        "domain -> REST nested query should preserve substances");
    assertEquals(List.of("concentration"), rest.getQuery().getResultTypes(),
        "domain -> REST nested query should preserve resultTypes");
    assertEquals(new BigDecimal("5"), rest.getQuery().getSourceCharacteristics().getHeight(),
        "domain -> REST nested query should preserve sourceCharacteristics height");
    assertEquals(new BigDecimal("1.1"), rest.getQuery().getSourceCharacteristics().getHeatContent(),
        "domain -> REST nested query should preserve sourceCharacteristics heatContent");
    assertEquals(new BigDecimal("0.2"), rest.getQuery().getSourceCharacteristics().getSpread(),
        "domain -> REST nested query should preserve sourceCharacteristics spread");
    assertEquals(1, rest.getQuery().getSourceCharacteristics().getEmissionDiurnalVariation(),
        "domain -> REST nested query should preserve sourceCharacteristics emissionDiurnalVariation");
    assertEquals(1, rest.getQuery().getMeshPoints().size(),
        "domain -> REST nested query should preserve meshPoints size");
    assertEquals(0, rest.getQuery().getMeshPoints().get(0).getX(),
        "domain -> REST nested query should preserve meshPoint x");
    assertEquals(0, rest.getQuery().getMeshPoints().get(0).getY(),
        "domain -> REST nested query should preserve meshPoint y");
    assertEquals(1, rest.getQuery().getSourcePoints().size(),
        "domain -> REST nested query should preserve sourcePoints size");
    assertEquals(1, rest.getQuery().getSourcePoints().get(0).getX(),
        "domain -> REST nested query should preserve sourcePoint x");
    assertEquals(1, rest.getQuery().getSourcePoints().get(0).getY(),
        "domain -> REST nested query should preserve sourcePoint y");

    assertEquals(2, rest.getRecords().size(), "domain -> REST should preserve result record count");
    assertEquals(1.25f, rest.getRecords().get(0).getValue(), 0.0001f,
        "domain -> REST should map first record value (Double to Float)");
    assertNull(rest.getRecords().get(1).getValue(),
        "domain -> REST should map second record null value to null Float");
    assertEquals("NOx", rest.getRecords().get(0).getSubstance(),
        "domain -> REST should preserve first record substance");
    assertEquals("deposition", rest.getRecords().get(1).getResultType(),
        "domain -> REST should preserve second record resultType");
  }

  @Test
  void testMapNulls() {
    assertNull(queryResultMapper.toRestMatrixQueryResultResponse(null),
        "null QueryResultResponse -> REST should be null");
    assertNull(queryResultMapper.toRestMatrixResultRecord(null),
        "null MatrixResultRecord -> REST should be null");
  }
}
