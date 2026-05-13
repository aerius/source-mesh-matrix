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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import nl.aerius.smm.api.matrix.mapper.MatrixCellExpander;
import nl.aerius.smm.api.matrix.mapper.MatrixCellMapper;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;
import nl.aerius.smm.api.matrix.repository.MatrixRepository;
import nl.aerius.smm.api.query.model.QueryRequest;

@Service
public class MatrixService {

  private static final Logger LOG = LoggerFactory.getLogger(MatrixService.class);

  private final CalculationService calculationService;
  private final MatrixQueryResolver matrixQueryResolver;
  private final MatrixCellExpander matrixCellExpander;
  private final MatrixCellMapper matrixCellMapper;
  private final MatrixRepository matrixRepository;

  public MatrixService(
      final CalculationService calculationService,
      final MatrixQueryResolver matrixQueryResolver,
      final MatrixCellExpander matrixCellExpander,
      final MatrixCellMapper matrixCellMapper,
      final MatrixRepository matrixRepository) {
    this.calculationService = calculationService;
    this.matrixQueryResolver = matrixQueryResolver;
    this.matrixCellExpander = matrixCellExpander;
    this.matrixCellMapper = matrixCellMapper;
    this.matrixRepository = matrixRepository;
  }

  public List<MatrixCell> fetchMatrixResults(final QueryRequest request) {
    // Prep bidirectional keys lookup
    final ResolvedMatrixQuery resolved = matrixQueryResolver.resolve(request);
    final List<MatrixCellKey> cellKeys = matrixCellExpander.expand(request);
    final List<MatrixVariableDimensions> dbKeys = cellKeys.stream()
        .map(cellKey -> matrixCellMapper.toDbKey(resolved, cellKey))
        .toList();

    // Get cached results
    final List<MatrixDbRow> cached = matrixRepository.findExisting(resolved.fixedDimensions(), dbKeys);
    final Map<MatrixVariableDimensions, MatrixDbRow> cachedByKey =
        cached.stream()
            .collect(Collectors.toMap(
                MatrixDbRow::variableDimensions,
                Function.identity(),
                (existing, duplicate) -> existing
            ));

    // Get missing results
    final List<MatrixVariableDimensions> missing = dbKeys.stream()
        .filter(v -> !cachedByKey.containsKey(v))
        .toList();

    // Combine the cached and missing results
    if (!missing.isEmpty()) {
      final List<MatrixDbRow> calculated = calculationService.calculate(resolved.fixedDimensions(), missing);
      matrixRepository.store(calculated);
      for (int index = 0; index < missing.size(); index++) {
        cachedByKey.put(missing.get(index), calculated.get(index));
      }
    }

    // Map all results
    final List<MatrixCell> results = cellKeys.stream()
        .map(cellKey -> matrixCellMapper.toDbKey(resolved, cellKey))
        .map(cachedByKey::get)
        .map(row -> matrixCellMapper.toCell(resolved, row))
        .toList();

    LOG.debug(
        "Matrix query is returning {} record(s) ({} cached, {} calculated) for {} source point(s), {} substance(s), {} mesh point(s)",
        results.size(),
        cached.size(),
        missing.size(),
        request.sourcePoints().size(),
        request.substances().size(),
        request.meshPoints().size());

    return results;
  }
}
