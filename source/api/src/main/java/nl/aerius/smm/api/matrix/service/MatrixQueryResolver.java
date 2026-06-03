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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import nl.aerius.smm.api.catalog.model.CalculationVersion;
import nl.aerius.smm.api.catalog.model.ResultType;
import nl.aerius.smm.api.catalog.model.Substance;
import nl.aerius.smm.api.catalog.repository.CalculationVersionRepository;
import nl.aerius.smm.api.catalog.repository.MeshPointRepository;
import nl.aerius.smm.api.catalog.repository.ResultTypeRepository;
import nl.aerius.smm.api.catalog.repository.SourceCharacteristicsRepository;
import nl.aerius.smm.api.catalog.repository.SubstanceRepository;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.exception.InvalidQueryRequestException;
import nl.aerius.smm.api.matrix.model.ResolvedMatrixQuery;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.query.model.QueryRequest;

@Component
public class MatrixQueryResolver {

  private final CalculationVersionRepository calculationVersionRepository;
  private final SubstanceRepository substanceRepository;
  private final ResultTypeRepository resultTypeRepository;
  private final MeshPointRepository meshPointRepository;
  private final SourceCharacteristicsRepository sourceCharacteristicsRepository;

  public MatrixQueryResolver(
      final CalculationVersionRepository calculationVersionRepository,
      final SubstanceRepository substanceRepository,
      final ResultTypeRepository resultTypeRepository,
      final MeshPointRepository meshPointRepository,
      final SourceCharacteristicsRepository sourceCharacteristicsRepository) {
    this.calculationVersionRepository = calculationVersionRepository;
    this.substanceRepository = substanceRepository;
    this.resultTypeRepository = resultTypeRepository;
    this.meshPointRepository = meshPointRepository;
    this.sourceCharacteristicsRepository = sourceCharacteristicsRepository;
  }

  public ResolvedMatrixQuery resolve(final QueryRequest request) {
    final Map<String, CalculationVersion> calculationVersionsByName = calculationVersionRepository.findAll().stream()
        .collect(Collectors.toMap(CalculationVersion::name, Function.identity()));
    final CalculationVersion calculationVersion = calculationVersionsByName.get(request.calculationVersion());
    if (calculationVersion == null) {
      throw new InvalidQueryRequestException("Unknown calculation version: " + request.calculationVersion());
    }

    final Map<String, Substance> substancesByName = substanceRepository.findAll().stream()
        .collect(Collectors.toMap(Substance::name, Function.identity()));
    final Map<String, Short> substanceIdsByName = new HashMap<>();
    for (final String substanceName : request.substances()) {
      final Substance substance = substancesByName.get(substanceName);
      if (substance == null) {
        throw new InvalidQueryRequestException("Unknown substance: " + substanceName);
      }
      substanceIdsByName.put(substanceName, substance.substanceId());
    }

    final Map<String, ResultType> resultTypesByName = resultTypeRepository.findAll().stream()
        .collect(Collectors.toMap(ResultType::name, Function.identity()));
    final Map<String, Short> resultTypeIdsByName = new HashMap<>();
    for (final String resultTypeName : request.resultTypes()) {
      final ResultType resultType = resultTypesByName.get(resultTypeName);
      if (resultType == null) {
        throw new InvalidQueryRequestException("Unknown result type: " + resultTypeName);
      }
      resultTypeIdsByName.put(resultTypeName, resultType.resultTypeId());
    }

    final Map<Point, Integer> meshPointIdsByPoint = meshPointRepository.findMeshPointIdsByCoordinates(request.meshPoints());
    for (final Point meshPoint : request.meshPoints()) {
      if (!meshPointIdsByPoint.containsKey(meshPoint)) {
        throw new InvalidQueryRequestException("Unknown mesh point: (" + meshPoint.x() + ", " + meshPoint.y() + ")");
      }
    }

    final short sourceCharacteristicId = sourceCharacteristicsRepository.findId(request.sourceCharacteristics())
        .orElseThrow(() -> new InvalidQueryRequestException("Unknown source characteristics"));

    return new ResolvedMatrixQuery(
        new MatrixFixedDimensions(calculationVersion.calculationVersionId(), sourceCharacteristicId),
        substanceIdsByName,
        resultTypeIdsByName,
        meshPointIdsByPoint);
  }
}
