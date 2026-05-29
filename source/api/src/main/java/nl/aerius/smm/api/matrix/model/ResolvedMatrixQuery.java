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
package nl.aerius.smm.api.matrix.model;

import java.util.Map;
import java.util.stream.Collectors;

import nl.aerius.smm.api.common.Point;
import nl.aerius.smm.api.matrix.model.db.MatrixFixedDimensions;

/** Resolved request context with fixed dimensions and lookup maps for domain/DB translation. */
public record ResolvedMatrixQuery(
    MatrixFixedDimensions fixedDimensions,
    Map<String, Short> substanceIdsByName,
    Map<String, Short> resultTypeIdsByName,
    Map<Point, Integer> meshPointIdsByPoint,
    Map<Short, String> substanceNamesById,
    Map<Short, String> resultTypeNamesById,
    Map<Integer, Point> meshPointsById) {

  public ResolvedMatrixQuery(
      final MatrixFixedDimensions fixedDimensions,
      final Map<String, Short> substanceIdsByName,
      final Map<String, Short> resultTypeIdsByName,
      final Map<Point, Integer> meshPointIdsByPoint) {
    this(
        fixedDimensions,
        substanceIdsByName,
        resultTypeIdsByName,
        meshPointIdsByPoint,
        invertMap(substanceIdsByName),
        invertMap(resultTypeIdsByName),
        invertMap(meshPointIdsByPoint));
  }

  private static <K, V> Map<V, K> invertMap(final Map<K, V> map) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
  }
}
