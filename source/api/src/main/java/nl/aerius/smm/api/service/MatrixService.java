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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import nl.aerius.smm.api.model.MatrixResultRecord;
import nl.aerius.smm.api.model.Point;
import nl.aerius.smm.api.model.QueryRequest;

@Service
public class MatrixService {

  private static final Logger LOG = LoggerFactory.getLogger(MatrixService.class);

  /**
   * Returns one synthetic matrix value per (source point, substance, result type, mesh point) combination.
   *
   * <p>Expects {@link nl.aerius.smm.api.validation.QueryRequestValidator} (or equivalent) to have validated
   * the {@link QueryRequest} already.
   */
  public List<MatrixResultRecord> fetchMatrixResults(final QueryRequest request) {
    final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    final List<MatrixResultRecord> records = new ArrayList<>();
    for (final Point sourcePoint : request.sourcePoints()) {
      for (final String substanceName : request.substances()) {
        for (final String resultTypeName : request.resultTypes()) {
          for (final Point meshPoint : request.meshPoints()) {
            records.add(new MatrixResultRecord(sourcePoint, meshPoint, substanceName, resultTypeName, rnd.nextDouble()));
          }
        }
      }
    }

    LOG.debug("Matrix query produced {} record(s) for {} source point(s), {} substance(s), {} mesh point(s)",
        records.size(),
        request.sourcePoints().size(),
        request.substances().size(),
        request.meshPoints().size());

    return records;
  }
}
