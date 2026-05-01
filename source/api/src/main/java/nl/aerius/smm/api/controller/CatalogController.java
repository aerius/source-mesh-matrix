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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import nl.aerius.smm.api.generated.openapi.api.CatalogApiDelegate;
import nl.aerius.smm.api.generated.openapi.model.RestCalculationVersionCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestResultTypeCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestSubstanceCatalogResponse;
import nl.aerius.smm.api.mapper.openapi.CatalogResponseMapper;
import nl.aerius.smm.api.service.CatalogService;

@RestController
public class CatalogController implements CatalogApiDelegate {

  private final CatalogService catalogService;
  private final CatalogResponseMapper catalogResponseMapper;

  @Autowired
  public CatalogController(final CatalogService catalogService, final CatalogResponseMapper catalogResponseMapper) {
    this.catalogService = catalogService;
    this.catalogResponseMapper = catalogResponseMapper;
  }

  @Override
  public ResponseEntity<RestCalculationVersionCatalogResponse> getCalculationVersions() {
    return ResponseEntity.ok(
        catalogResponseMapper.toRestCalculationVersionCatalogResponse(catalogService.getCalculationVersions()));
  }

  @Override
  public ResponseEntity<RestSubstanceCatalogResponse> getSubstances() {
    return ResponseEntity.ok(catalogResponseMapper.toRestSubstanceCatalogResponse(catalogService.getSubstances()));
  }

  @Override
  public ResponseEntity<RestResultTypeCatalogResponse> getResultTypes() {
    return ResponseEntity.ok(catalogResponseMapper.toRestResultTypeCatalogResponse(catalogService.getResultTypes()));
  }
}
