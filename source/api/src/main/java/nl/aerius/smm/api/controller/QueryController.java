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

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import nl.aerius.smm.api.generated.openapi.api.QueryApiDelegate;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryRequest;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryResultResponse;
import nl.aerius.smm.api.generated.openapi.model.RestMatrixQueryStatusResponse;
import nl.aerius.smm.api.mapper.openapi.QueryRequestMapper;
import nl.aerius.smm.api.mapper.openapi.QueryResultMapper;
import nl.aerius.smm.api.mapper.openapi.QueryTaskMapper;
import nl.aerius.smm.api.model.QueryRequest;
import nl.aerius.smm.api.model.QueryResultResponse;
import nl.aerius.smm.api.model.QueryStatus;
import nl.aerius.smm.api.service.QueryProcessingService;
import nl.aerius.smm.api.validation.QueryRequestValidator;

@RestController
public class QueryController implements QueryApiDelegate {

  private final QueryProcessingService queryProcessingService;
  private final QueryRequestMapper queryRequestMapper;
  private final QueryResultMapper queryResultMapper;
  private final QueryTaskMapper queryTaskMapper;
  private final QueryRequestValidator queryRequestValidator;

  @Autowired
  public QueryController(final QueryProcessingService queryProcessingService, final QueryRequestMapper queryRequestMapper,
      final QueryResultMapper queryResultMapper, final QueryTaskMapper queryTaskMapper,
      final QueryRequestValidator queryRequestValidator) {
    this.queryProcessingService = queryProcessingService;
    this.queryRequestMapper = queryRequestMapper;
    this.queryResultMapper = queryResultMapper;
    this.queryTaskMapper = queryTaskMapper;
    this.queryRequestValidator = queryRequestValidator;
  }

  @Override
  public ResponseEntity<RestMatrixQueryStatusResponse> createMatrixQuery(RestMatrixQueryRequest restMatrixQueryRequest) {
    final QueryRequest request = queryRequestMapper.toQueryRequest(restMatrixQueryRequest);
    queryRequestValidator.validateComplete(request);
    final UUID queryId = queryProcessingService.create(request);
    final QueryStatus status = queryProcessingService.getStatus(queryId);

    final RestMatrixQueryStatusResponse response = queryTaskMapper.toRestMatrixQueryStatusResponse(queryId, status);
    return ResponseEntity
        .accepted()
        .header("Location", response.getStatusUrl())
        .body(response);
  }

  @Override
  public ResponseEntity<RestMatrixQueryResultResponse> getMatrixQueryResult(UUID queryId) {
    final QueryResultResponse result = queryProcessingService.getResult(queryId);
    return ResponseEntity.ok(queryResultMapper.toRestMatrixQueryResultResponse(result));
  }

  @Override
  public ResponseEntity<RestMatrixQueryStatusResponse> getMatrixQueryStatus(UUID queryId) {
    final QueryStatus status = queryProcessingService.getStatus(queryId);
    return ResponseEntity
        .ok()
        .body(queryTaskMapper.toRestMatrixQueryStatusResponse(queryId, status));
  }
}
