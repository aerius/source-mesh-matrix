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
package nl.aerius.smm.api.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nl.aerius.smm.api.catalog.model.CalculationVersion;
import nl.aerius.smm.api.catalog.model.ResultType;
import nl.aerius.smm.api.catalog.model.Substance;
import nl.aerius.smm.api.catalog.repository.CalculationVersionRepository;
import nl.aerius.smm.api.catalog.repository.ResultTypeRepository;
import nl.aerius.smm.api.catalog.repository.SubstanceRepository;

@Service
public class CatalogService {

  private final CalculationVersionRepository calculationVersionRepository;
  private final SubstanceRepository substanceRepository;
  private final ResultTypeRepository resultTypeRepository;

  public CatalogService(final CalculationVersionRepository calculationVersionRepository,
      final SubstanceRepository substanceRepository,
      final ResultTypeRepository resultTypeRepository) {
    this.calculationVersionRepository = calculationVersionRepository;
    this.substanceRepository = substanceRepository;
    this.resultTypeRepository = resultTypeRepository;
  }

  public List<CalculationVersion> getCalculationVersions() {
    return calculationVersionRepository.findAll();
  }

  public List<Substance> getSubstances() {
    return substanceRepository.findAll();
  }

  public List<ResultType> getResultTypes() {
    return resultTypeRepository.findAll();
  }
}
