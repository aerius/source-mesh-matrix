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
package nl.aerius.smm.api.matrix.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;

@Service
public class CalculationService {

  private static final Logger LOG = LoggerFactory.getLogger(CalculationService.class);

  public List<MatrixDbRow> calculate(
      final MatrixFixedDimensions fixedDimensions,
      final List<MatrixVariableDimensions> variableDimensionsList) {
    final ThreadLocalRandom random = ThreadLocalRandom.current();
    final List<MatrixDbRow> rows = variableDimensionsList.stream()
        .map(variableDimensions -> new MatrixDbRow(fixedDimensions, variableDimensions, random.nextDouble()))
        .toList();

    LOG.debug("Calculation produced {} row(s) from {} variable dimensions", rows.size(), variableDimensionsList.size());

    return rows;
  }
}
