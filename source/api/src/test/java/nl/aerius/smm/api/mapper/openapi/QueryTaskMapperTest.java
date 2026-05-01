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

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryStatusResponse;
import nl.aerius.smm.api.TestApplication;
import nl.aerius.smm.api.model.QueryStatus;

@SpringBootTest(classes = TestApplication.class)
class QueryTaskMapperTest {

  @Autowired
  private QueryTaskMapper mapper;

  @Test
  void testMapRoundTrip() {
    final UUID queryId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    final String statusUrl = "/api/v1/matrix/queries/" + queryId;
    final String resultUrl = statusUrl + "/result";

    assertStatus(queryId, QueryStatus.ACCEPTED, RestMatrixQueryStatusResponse.StatusEnum.PROCESSING, statusUrl, null);
    assertStatus(queryId, QueryStatus.PROCESSING, RestMatrixQueryStatusResponse.StatusEnum.PROCESSING, statusUrl, null);
    assertStatus(queryId, QueryStatus.COMPLETED, RestMatrixQueryStatusResponse.StatusEnum.COMPLETED, statusUrl, resultUrl);
    assertStatus(queryId, QueryStatus.REJECTED, RestMatrixQueryStatusResponse.StatusEnum.REJECTED, statusUrl, null);
    assertStatus(queryId, QueryStatus.FAILED, RestMatrixQueryStatusResponse.StatusEnum.FAILED, statusUrl, null);
  }

  private void assertStatus(
      final UUID queryId,
      final QueryStatus domainStatus,
      final RestMatrixQueryStatusResponse.StatusEnum expectedRestStatus,
      final String expectedStatusUrl,
      final String expectedResultUrl) {
    final RestMatrixQueryStatusResponse rest = mapper.toRestMatrixQueryStatusResponse(queryId, domainStatus);
    assertEquals(queryId, rest.getQueryId(),
        "domain -> REST should preserve queryId (" + domainStatus + ")");
    assertEquals(expectedRestStatus, rest.getStatus(),
        "domain -> REST should preserve status (" + domainStatus + ")");
    assertEquals(expectedStatusUrl, rest.getStatusUrl(),
        "domain -> REST should preserve statusUrl (" + domainStatus + ")");
    assertEquals(expectedResultUrl, rest.getResultUrl(),
        "domain -> REST should preserve resultUrl (" + domainStatus + ")");
  }

  @Test
  void testMapNulls() {
    final UUID queryId = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");
    final RestMatrixQueryStatusResponse rest = mapper.toRestMatrixQueryStatusResponse(queryId, null);
    assertEquals(RestMatrixQueryStatusResponse.StatusEnum.PROCESSING, rest.getStatus(),
        "null QueryStatus -> REST should default status to PROCESSING");
    assertNull(rest.getResultUrl(),
        "null QueryStatus -> REST should leave resultUrl null");
    assertEquals(queryId, rest.getQueryId(),
        "null QueryStatus -> REST should preserve queryId");
    assertEquals("/api/v1/matrix/queries/" + queryId, rest.getStatusUrl(),
        "null QueryStatus -> REST should preserve statusUrl");
  }
}
