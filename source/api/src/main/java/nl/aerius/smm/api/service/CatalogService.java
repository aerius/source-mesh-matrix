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
package nl.aerius.smm.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nl.aerius.smm.api.model.CalculationVersion;
import nl.aerius.smm.api.model.ResultType;
import nl.aerius.smm.api.model.Substance;

@Service
public class CatalogService {

  private static final List<CalculationVersion> CALCULATION_VERSIONS = List.of(
      new CalculationVersion((short) 1, "v1"));

  private static final List<Substance> SUBSTANCES = List.of(
      new Substance((short) 1, "NOx"),
      new Substance((short) 2, "NH3"));

  private static final List<ResultType> RESULT_TYPES = List.of(
      new ResultType((short) 1, "concentration"),
      new ResultType((short) 2, "deposition"));

  public List<CalculationVersion> getCalculationVersions() {
    return CALCULATION_VERSIONS;
  }

  public List<Substance> getSubstances() {
    return SUBSTANCES;
  }

  public List<ResultType> getResultTypes() {
    return RESULT_TYPES;
  }
}
