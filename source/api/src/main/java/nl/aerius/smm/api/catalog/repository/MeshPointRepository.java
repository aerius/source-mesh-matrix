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
package nl.aerius.smm.api.catalog.repository;

import static nl.aerius.smm.api.generated.jooq.tables.MeshPoints.MESH_POINTS;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import nl.aerius.smm.api.common.Point;

@Repository
public class MeshPointRepository {

  private final DSLContext dsl;

  public MeshPointRepository(final DSLContext dsl) {
    this.dsl = dsl;
  }

  public Map<Point, Integer> findMeshPointIdsByCoordinates(final Collection<Point> points) {
    if (points.isEmpty()) {
      return Map.of();
    }
    final List<org.jooq.Row2<Integer, Integer>> coordinates = points.stream()
        .map(point -> row(val(point.x()), val(point.y())))
        .toList();
    return dsl.select(MESH_POINTS.MESH_POINT_ID, MESH_POINTS.X, MESH_POINTS.Y)
        .from(MESH_POINTS)
        .where(row(MESH_POINTS.X, MESH_POINTS.Y).in(coordinates))
        .fetchStream()
        .collect(Collectors.toMap(
            record -> new Point(record.get(MESH_POINTS.X), record.get(MESH_POINTS.Y)),
            record -> record.get(MESH_POINTS.MESH_POINT_ID)));
  }
}
