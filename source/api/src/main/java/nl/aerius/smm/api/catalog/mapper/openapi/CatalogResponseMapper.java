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
package nl.aerius.smm.api.catalog.mapper.openapi;

import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;

import nl.aerius.smm.api.catalog.model.CalculationVersion;
import nl.aerius.smm.api.catalog.model.ResultType;
import nl.aerius.smm.api.catalog.model.Substance;
import nl.aerius.smm.api.generated.openapi.model.RestCalculationVersionCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestResultTypeCatalogResponse;
import nl.aerius.smm.api.generated.openapi.model.RestSubstanceCatalogResponse;

@Mapper(componentModel = "spring")
public interface CatalogResponseMapper {

  default RestCalculationVersionCatalogResponse toRestCalculationVersionCatalogResponse(final List<CalculationVersion> calculationVersions) {
    return new RestCalculationVersionCatalogResponse()
        .calculationVersions(
            Optional.ofNullable(calculationVersions)
                .map(this::mapCalculationVersionNames)
                .orElseGet(List::of)
        );
  }

  List<String> mapCalculationVersionNames(List<CalculationVersion> calculationVersions);

  default RestSubstanceCatalogResponse toRestSubstanceCatalogResponse(final List<Substance> substances) {
    return new RestSubstanceCatalogResponse()
        .substances(
            Optional.ofNullable(substances)
                .map(this::mapSubstanceNames)
                .orElseGet(List::of)
        );
  }

  List<String> mapSubstanceNames(List<Substance> substances);

  default RestResultTypeCatalogResponse toRestResultTypeCatalogResponse(final List<ResultType> resultTypes) {
    return new RestResultTypeCatalogResponse()
        .resultTypes(
            Optional.ofNullable(resultTypes)
                .map(this::mapResultTypeNames)
                .orElseGet(List::of)
        );
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
