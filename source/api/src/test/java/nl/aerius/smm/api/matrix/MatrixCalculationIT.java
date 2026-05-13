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
package nl.aerius.smm.api.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import nl.aerius.smm.api.catalog.model.CalculationVersion;
import nl.aerius.smm.api.catalog.model.ResultType;
import nl.aerius.smm.api.catalog.model.SourceCharacteristics;
import nl.aerius.smm.api.catalog.model.Substance;
import nl.aerius.smm.api.catalog.repository.CalculationVersionRepository;
import nl.aerius.smm.api.catalog.repository.MeshPointRepository;
import nl.aerius.smm.api.catalog.repository.ResultTypeRepository;
import nl.aerius.smm.api.catalog.repository.SourceCharacteristicsRepository;
import nl.aerius.smm.api.catalog.repository.SubstanceRepository;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.mapper.MatrixCellExpander;
import nl.aerius.smm.api.matrix.mapper.MatrixCellMapper;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.matrix.repository.MatrixRepository;
import nl.aerius.smm.api.matrix.service.CalculationService;
import nl.aerius.smm.api.matrix.service.MatrixQueryResolver;
import nl.aerius.smm.api.matrix.service.MatrixService;
import nl.aerius.smm.api.query.model.QueryRequest;

@SpringBootTest(classes = MatrixCalculationIT.TestConfiguration.class)
class MatrixCalculationIT {

  @Configuration
  @Import({
      CalculationService.class,
      MatrixService.class,
      MatrixQueryResolver.class,
      MatrixCellExpander.class,
      MatrixCellMapper.class
  })
  static class TestConfiguration {}

  @Autowired
  private MatrixService matrixService;

  @Autowired
  private MatrixCellExpander matrixCellExpander;

  @MockitoBean
  private CalculationVersionRepository calculationVersionRepository;

  @MockitoBean
  private SubstanceRepository substanceRepository;

  @MockitoBean
  private ResultTypeRepository resultTypeRepository;

  @MockitoBean
  private MeshPointRepository meshPointRepository;

  @MockitoBean
  private SourceCharacteristicsRepository sourceCharacteristicsRepository;

  @MockitoBean
  private MatrixRepository matrixRepository;

  @BeforeEach
  void stubIdResolution() {
    when(calculationVersionRepository.findAll()).thenReturn(List.of(
        new CalculationVersion((short) 1, "v1")));
    when(substanceRepository.findAll()).thenReturn(List.of(
        new Substance((short) 1, "NOx"),
        new Substance((short) 2, "NH3")));
    when(resultTypeRepository.findAll()).thenReturn(List.of(
        new ResultType((short) 1, "concentration"),
        new ResultType((short) 2, "deposition")));
    when(meshPointRepository.findMeshPointIdsByCoordinates(any())).thenReturn(Map.of(new Point(3, 4), 10));
    when(sourceCharacteristicsRepository.findId(any())).thenReturn(Optional.of((short) 1));
  }

  @Test
  void testReturnsValueForEveryRequestedKey() {
    final QueryRequest request = sampleRequest();
    final List<MatrixCellKey> expectedKeys = matrixCellExpander.expand(request);

    when(matrixRepository.findExisting(any(), anyList())).thenReturn(List.of());

    final List<MatrixCell> results = matrixService.fetchMatrixResults(request);

    assertEquals(expectedKeys.size(), results.size(),
        "matrix fetch -> one result per requested key");

    final Set<MatrixCellKey> keysWithValues = results.stream()
        .filter(cell -> !Double.isNaN(cell.value()))
        .map(cell -> new MatrixCellKey(cell.sourcePoint(), cell.meshPoint(), cell.substance(), cell.resultType()))
        .collect(Collectors.toSet());

    assertEquals(Set.copyOf(expectedKeys), keysWithValues,
        "matrix fetch -> every requested key must be returned with a value");
    assertTrue(results.stream().noneMatch(cell -> Double.isNaN(cell.value())),
        "matrix fetch -> no result may have a missing (NaN) value");
    assertFalse(results.isEmpty(),
        "matrix fetch -> sample request must produce at least one result");

    verify(matrixRepository).store(any());
  }

  private static QueryRequest sampleRequest() {
    return new QueryRequest(
        "v1",
        List.of("NOx", "NH3"),
        List.of("concentration", "deposition"),
        new SourceCharacteristics((short) 1, 1d, 10d, 2d, 1),
        List.of(new Point(3, 4)),
        List.of(new Point(1, 2), new Point(5, 6)));
  }
}
