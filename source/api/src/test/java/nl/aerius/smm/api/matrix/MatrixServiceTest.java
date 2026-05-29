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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.aerius.smm.api.catalog.model.SourceCharacteristics;
import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.mapper.MatrixCellExpander;
import nl.aerius.smm.api.matrix.mapper.MatrixCellMapper;
import nl.aerius.smm.api.matrix.model.MatrixCell;
import nl.aerius.smm.api.matrix.model.MatrixCellKey;
import nl.aerius.smm.api.matrix.model.db.MatrixDbRow;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;
import nl.aerius.smm.api.matrix.model.db.MatrixVariableDimensions;
import nl.aerius.smm.api.matrix.repository.MatrixRepository;
import nl.aerius.smm.api.matrix.service.CalculationService;
import nl.aerius.smm.api.matrix.service.MatrixQueryResolver;
import nl.aerius.smm.api.matrix.service.MatrixService;
import nl.aerius.smm.api.matrix.model.ResolvedMatrixQuery;
import nl.aerius.smm.api.query.model.QueryRequest;

@ExtendWith(MockitoExtension.class)
class MatrixServiceTest {

  private static final MatrixFixedDimensions SAMPLE_FIXED_DIMENSIONS = new MatrixFixedDimensions((short) 1, (short) 1);
  private static final ResolvedMatrixQuery SAMPLE_RESOLVED = new ResolvedMatrixQuery(
      SAMPLE_FIXED_DIMENSIONS,
      Map.of("NOx", (short) 1),
      Map.of("concentration", (short) 1),
      Map.of(new Point(1, 2), 100));

  @Mock
  private CalculationService calculationService;

  @Mock
  private MatrixQueryResolver matrixQueryResolver;

  @Mock
  private MatrixCellExpander matrixCellExpander;

  @Mock
  private MatrixRepository matrixRepository;

  @Spy
  private MatrixCellMapper matrixCellMapper = new MatrixCellMapper();

  @InjectMocks
  private MatrixService matrixService;

  @BeforeEach
  void setUp() {
    when(matrixQueryResolver.resolve(any())).thenReturn(SAMPLE_RESOLVED);
  }

  @Test
  void testFetchUsesCacheWhenAllPresent() {
    final QueryRequest request = sampleRequest();
    final MatrixCellKey key = sampleKey();
    final MatrixVariableDimensions dbKey = sampleDbKey(key);
    final MatrixDbRow cachedDbRow = sampleDbRow(dbKey, 1.5);

    when(matrixCellExpander.expand(request)).thenReturn(List.of(key));
    when(matrixRepository.findExisting(eq(SAMPLE_FIXED_DIMENSIONS), anyList())).thenReturn(List.of(cachedDbRow));

    final List<MatrixCell> results = matrixService.fetchMatrixResults(request);

    assertEquals(List.of(sampleCell()), results,
        "fetchMatrixResults -> should return cached records when all keys exist");
    verify(calculationService, never()).calculate(any(), any());
    verify(matrixRepository, never()).store(any());
  }

  @Test
  void testFetchCalculatesAndStoresMissingKeys() {
    final QueryRequest request = sampleRequest(List.of(new Point(3, 4), new Point(5, 6)));
    final MatrixCellKey cachedKey = sampleKey();
    final MatrixCellKey missingKey = new MatrixCellKey(new Point(5, 6), new Point(1, 2), "NOx", "concentration");
    final MatrixVariableDimensions missingDbKey = sampleDbKey(missingKey);
    final MatrixDbRow cachedDbRow = sampleDbRow(sampleDbKey(cachedKey), 1.5);
    final MatrixDbRow calculatedDbRow = new MatrixDbRow(SAMPLE_FIXED_DIMENSIONS, missingDbKey, 2.5);

    when(matrixCellExpander.expand(request)).thenReturn(List.of(cachedKey, missingKey));
    when(matrixRepository.findExisting(eq(SAMPLE_FIXED_DIMENSIONS), anyList())).thenReturn(List.of(cachedDbRow));
    when(calculationService.calculate(SAMPLE_FIXED_DIMENSIONS, List.of(missingDbKey))).thenReturn(List.of(calculatedDbRow));

    final List<MatrixCell> results = matrixService.fetchMatrixResults(request);

    assertEquals(List.of(
            sampleCell(),
            new MatrixCell(new Point(5, 6), new Point(1, 2), "NOx", "concentration", 2.5)), results,
        "fetchMatrixResults -> should merge cached and calculated records in key order");
    verify(matrixRepository).store(List.of(calculatedDbRow));
  }

  @Test
  void testFetchCalculatesAllWhenNothingCached() {
    final QueryRequest request = sampleRequest();
    final MatrixCellKey key = sampleKey();
    final MatrixVariableDimensions dbKey = sampleDbKey(key);
    final MatrixDbRow calculatedDbRow = sampleDbRow(dbKey, 1.5);

    when(matrixCellExpander.expand(request)).thenReturn(List.of(key));
    when(matrixRepository.findExisting(eq(SAMPLE_FIXED_DIMENSIONS), anyList())).thenReturn(List.of());
    when(calculationService.calculate(SAMPLE_FIXED_DIMENSIONS, List.of(dbKey))).thenReturn(List.of(calculatedDbRow));

    final List<MatrixCell> results = matrixService.fetchMatrixResults(request);

    assertEquals(List.of(sampleCell()), results,
        "fetchMatrixResults -> should calculate all keys when cache is empty");
    verify(matrixRepository).store(eq(List.of(calculatedDbRow)));
  }

  private static QueryRequest sampleRequest() {
    return sampleRequest(List.of(new Point(3, 4)));
  }

  private static QueryRequest sampleRequest(final List<Point> sourcePoints) {
    return new QueryRequest(
        "v1",
        List.of("NOx"),
        List.of("concentration"),
        new SourceCharacteristics((short) 1, 1d, 10d, 2d, 1),
        List.of(new Point(1, 2)),
        sourcePoints);
  }

  private static MatrixCellKey sampleKey() {
    return new MatrixCellKey(new Point(3, 4), new Point(1, 2), "NOx", "concentration");
  }

  private static MatrixCell sampleCell() {
    return new MatrixCell(new Point(3, 4), new Point(1, 2), "NOx", "concentration", 1.5);
  }

  private static MatrixVariableDimensions sampleDbKey(final MatrixCellKey key) {
    return new MatrixVariableDimensions((short) 1, (short) 1, key.sourcePoint(), 100);
  }

  private static MatrixDbRow sampleDbRow(final MatrixVariableDimensions key, final double value) {
    return new MatrixDbRow(SAMPLE_FIXED_DIMENSIONS, key, value);
  }
}
