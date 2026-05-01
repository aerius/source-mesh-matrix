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
package nl.aerius.smm.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.aerius.smm.api.exception.QueueFullException;
import nl.aerius.smm.api.exception.ResultNotReadyException;
import nl.aerius.smm.api.exception.TaskNotFoundException;
import nl.aerius.smm.api.generated.openapi.api.QueryApi;
import nl.aerius.smm.api.generated.openapi.api.QueryApiController;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryRequest;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryResultResponse;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryStatusResponse;
import nl.aerius.smm.api.generated.openapi.model.RestPoint;
import nl.aerius.smm.api.generated.openapi.model.RestSourceCharacteristics;
import nl.aerius.smm.api.mapper.openapi.QueryRequestMapper;
import nl.aerius.smm.api.mapper.openapi.QueryResultMapper;
import nl.aerius.smm.api.mapper.openapi.QueryTaskMapper;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.QueryResultResponse;
import nl.aerius.smm.api.model.QueryStatus;
import nl.aerius.smm.api.service.QueryProcessingService;
import nl.aerius.smm.api.validation.QueryRequestValidator;

@WebMvcTest(controllers = QueryApiController.class)
@Import(QueryController.class)
class QueryControllerMvcTest {

  private static final String BASE = "/api/v1";

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private QueryProcessingService queryProcessingService;

  @MockitoBean
  private QueryRequestMapper queryRequestMapper;

  @MockitoBean
  private QueryResultMapper queryResultMapper;

  @MockitoBean
  private QueryTaskMapper queryTaskMapper;

  @MockitoBean
  private QueryRequestValidator queryRequestValidator;

  @Test
  void testPostAccepted() throws Exception {
    final UUID queryId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    final QueryRequest domainRequest = domainSampleRequest();
    final RestMatrixQueryStatusResponse body = RestMatrixQueryStatusResponse.builder()
        .queryId(queryId)
        .status(RestMatrixQueryStatusResponse.StatusEnum.PROCESSING)
        .statusUrl(BASE + "/matrix/queries/" + queryId)
        .build();

    when(queryRequestMapper.toQueryRequest(any(RestMatrixQueryRequest.class))).thenReturn(domainRequest);
    doNothing().when(queryRequestValidator).validateComplete(domainRequest);
    when(queryProcessingService.create(domainRequest)).thenReturn(queryId);
    when(queryProcessingService.getStatus(queryId)).thenReturn(QueryStatus.ACCEPTED);
    when(queryTaskMapper.toRestMatrixQueryStatusResponse(queryId, QueryStatus.ACCEPTED)).thenReturn(body);

    final MvcResult res = mockMvc.perform(post(BASE + QueryApi.PATH_CREATE_MATRIX_QUERY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(restSampleRequest())))
        .andReturn();

    assertEquals(HttpStatus.ACCEPTED.value(), res.getResponse().getStatus(),
        "POST /matrix/queries -> response status should be 202 Accepted");
    assertEquals(BASE + "/matrix/queries/" + queryId, res.getResponse().getHeader("Location"),
        "POST /matrix/queries -> Location should reference the query status URL");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals(queryId.toString(), json.get("queryId").asText(),
        "POST /matrix/queries -> JSON body should preserve queryId");
    assertEquals("Processing", json.get("status").asText(),
        "POST /matrix/queries -> JSON body should expose REST status Processing");
  }

  @Test
  void testGetStatusOk() throws Exception {
    final UUID queryId = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");
    final RestMatrixQueryStatusResponse body = RestMatrixQueryStatusResponse.builder()
        .queryId(queryId)
        .status(RestMatrixQueryStatusResponse.StatusEnum.COMPLETED)
        .statusUrl(BASE + "/matrix/queries/" + queryId)
        .resultUrl(BASE + "/matrix/queries/" + queryId + "/result")
        .build();

    when(queryProcessingService.getStatus(queryId)).thenReturn(QueryStatus.COMPLETED);
    when(queryTaskMapper.toRestMatrixQueryStatusResponse(queryId, QueryStatus.COMPLETED)).thenReturn(body);

    final MvcResult res = mockMvc.perform(get(BASE + "/matrix/queries/{queryId}", queryId))
        .andReturn();

    assertEquals(HttpStatus.OK.value(), res.getResponse().getStatus(),
        "GET /matrix/queries/{id} -> response status should be 200 OK");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals(queryId.toString(), json.get("queryId").asText(),
        "GET /matrix/queries/{id} -> JSON body should preserve queryId");
    assertEquals("Completed", json.get("status").asText(),
        "GET /matrix/queries/{id} -> JSON body should expose REST status Completed");
  }

  @Test
  void testGetResultOk() throws Exception {
    final UUID queryId = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33");
    final QueryResultResponse domainResult = new QueryResultResponse(domainSampleRequest(), List.of());
    final RestMatrixQueryResultResponse rest = new RestMatrixQueryResultResponse();

    when(queryProcessingService.getResult(queryId)).thenReturn(domainResult);
    when(queryResultMapper.toRestMatrixQueryResultResponse(domainResult)).thenReturn(rest);

    final MvcResult res = mockMvc.perform(get(BASE + "/matrix/queries/{queryId}/result", queryId))
        .andReturn();

    assertEquals(HttpStatus.OK.value(), res.getResponse().getStatus(),
        "GET /matrix/queries/{id}/result -> response status should be 200 OK");
  }

  @Test
  void testGetStatusMissing() throws Exception {
    final UUID queryId = UUID.fromString("d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44");
    when(queryProcessingService.getStatus(queryId)).thenThrow(new TaskNotFoundException(queryId));

    final MvcResult res = mockMvc.perform(get(BASE + "/matrix/queries/{queryId}", queryId))
        .andReturn();

    assertEquals(HttpStatus.NOT_FOUND.value(), res.getResponse().getStatus(),
        "GET /matrix/queries/{id} when task missing -> should return 404 Not Found");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals("TASK_NOT_FOUND", json.get("code").asText(),
        "TaskNotFoundException -> JSON error code should be TASK_NOT_FOUND");
    assertEquals("No task found with id: " + queryId, json.get("message").asText(),
        "TaskNotFoundException -> JSON message should describe missing task");
  }

  @Test
  void testGetResultPending() throws Exception {
    final UUID queryId = UUID.fromString("e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55");
    final RestMatrixQueryStatusResponse body = RestMatrixQueryStatusResponse.builder()
        .queryId(queryId)
        .status(RestMatrixQueryStatusResponse.StatusEnum.PROCESSING)
        .statusUrl(BASE + "/matrix/queries/" + queryId)
        .build();

    when(queryProcessingService.getResult(queryId)).thenThrow(new ResultNotReadyException(queryId, QueryStatus.PROCESSING));
    when(queryTaskMapper.toRestMatrixQueryStatusResponse(queryId, QueryStatus.PROCESSING)).thenReturn(body);

    final MvcResult res = mockMvc.perform(get(BASE + "/matrix/queries/{queryId}/result", queryId))
        .andReturn();

    assertEquals(HttpStatus.ACCEPTED.value(), res.getResponse().getStatus(),
        "GET /matrix/queries/{id}/result when not ready -> should return 202 Accepted");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals(queryId.toString(), json.get("queryId").asText(),
        "ResultNotReadyException -> JSON body should preserve queryId from status payload");
  }

  @Test
  void testPostQueueFull() throws Exception {
    final UUID queryId = UUID.fromString("f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66");
    final QueryRequest domainRequest = domainSampleRequest();

    when(queryRequestMapper.toQueryRequest(any(RestMatrixQueryRequest.class))).thenReturn(domainRequest);
    doNothing().when(queryRequestValidator).validateComplete(domainRequest);
    when(queryProcessingService.create(domainRequest)).thenThrow(new QueueFullException(queryId));

    final MvcResult res = mockMvc.perform(post(BASE + QueryApi.PATH_CREATE_MATRIX_QUERY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(restSampleRequest())))
        .andReturn();

    assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), res.getResponse().getStatus(),
        "POST /matrix/queries when queue full -> should return 429 Too Many Requests");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals("TOO_MANY_REQUEST", json.get("code").asText(),
        "QueueFullException -> JSON error code should be TOO_MANY_REQUEST");
  }

  @Test
  void testPostBadInput() throws Exception {
    final QueryRequest domainRequest = domainSampleRequest();

    when(queryRequestMapper.toQueryRequest(any(RestMatrixQueryRequest.class))).thenReturn(domainRequest);
    doThrow(new IllegalArgumentException("meshPoints invalid")).when(queryRequestValidator).validateComplete(domainRequest);

    final MvcResult res = mockMvc.perform(post(BASE + QueryApi.PATH_CREATE_MATRIX_QUERY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(restSampleRequest())))
        .andReturn();

    assertEquals(HttpStatus.BAD_REQUEST.value(), res.getResponse().getStatus(),
        "POST /matrix/queries when validation fails -> should return 400 Bad Request");
    final JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
    assertEquals("INVALID_QUERY_REQUEST", json.get("code").asText(),
        "invalid query request -> JSON error code should be INVALID_QUERY_REQUEST");
    assertEquals("meshPoints invalid", json.get("message").asText(),
        "invalid query request -> JSON message should echo validation failure");
  }

  private static QueryRequest domainSampleRequest() {
    return new QueryRequest(
        "v1",
        List.of("NOx"),
        List.of("concentration"),
        new nl.aerius.smm.api.model.SourceCharacteristics(null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(2), 1),
        List.of(new Point(1, 2)),
        List.of(new Point(3, 4)));
  }

  private static RestMatrixQueryRequest restSampleRequest() {
    return new RestMatrixQueryRequest(
        "v1",
        List.of("NOx"),
        List.of("concentration"),
        new RestSourceCharacteristics(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(2), 1),
        List.of(new RestPoint(1, 2)),
        List.of(new RestPoint(3, 4)));
  }
}
