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

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;

import nl.aerius.smm.api.generated.openapi.model.RestCalculationVersionCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestResultTypeCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestSubstanceCatalogResponse;
import nl.aerius.smm.api.model.CalculationVersion;
import nl.aerius.smm.api.model.ResultType;
import nl.aerius.smm.api.model.Substance;

@Mapper(componentModel = "spring")
public interface CatalogResponseMapper {

  default RestCalculationVersionCatalogResponse toRestCalculationVersionCatalogResponse(
      final List<CalculationVersion> calculationVersions) {
    final RestCalculationVersionCatalogResponse response = new RestCalculationVersionCatalogResponse();
    final List<String> names = calculationVersions == null ? Collections.emptyList() : mapCalculationVersionNames(calculationVersions);
    response.setCalculationVersions(names);
    return response;
  }

  List<String> mapCalculationVersionNames(List<CalculationVersion> calculationVersions);

  default RestSubstanceCatalogResponse toRestSubstanceCatalogResponse(final List<Substance> substances) {
    final RestSubstanceCatalogResponse response = new RestSubstanceCatalogResponse();
    final List<String> names = substances == null ? Collections.emptyList() : mapSubstanceNames(substances);
    response.setSubstances(names);
    return response;
  }

  List<String> mapSubstanceNames(List<Substance> substances);

  default RestResultTypeCatalogResponse toRestResultTypeCatalogResponse(final List<ResultType> resultTypes) {
    final RestResultTypeCatalogResponse response = new RestResultTypeCatalogResponse();
    final List<String> names = resultTypes == null ? Collections.emptyList() : mapResultTypeNames(resultTypes);
    response.setResultTypes(names);
    return response;
  }

  List<String> mapResultTypeNames(List<ResultType> resultTypes);

  default String map(final CalculationVersion version) {
    return version.name();
  }

  default String map(final Substance substance) {
    return substance.name();
  }

  default String map(final ResultType resultType) {
    return resultType.name();
  }
}
