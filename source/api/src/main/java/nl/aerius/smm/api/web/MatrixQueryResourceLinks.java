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
package nl.aerius.smm.api.web;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import nl.aerius.smm.api.config.AppEndpointsConfig.EndpointsProperties;

/**
 * Builds root-relative matrix query paths from {@link EndpointsProperties#basePath()} ({@code app.endpoints.base-path}).
 * When unset or {@code /}, defaults to {@code /api/v1} so self-links match the OpenAPI server URL.
 * Used by {@link nl.aerius.smm.api.mapper.openapi.QueryTaskMapper}.
 */
@Component
public class MatrixQueryResourceLinks {

  private static final String DEFAULT_BASE = "/api/v1";

  private final String normalizedApiBasePath;

  public MatrixQueryResourceLinks(final EndpointsProperties endpoints) {
    this.normalizedApiBasePath = linkBasePath(endpoints.basePath());
  }

  /**
   * Normalized root-relative API prefix (no trailing slash), e.g. {@code /api/v1}.
   */
  public String apiBasePath() {
    return normalizedApiBasePath;
  }

  /**
   * Path to the query status resource, e.g. {@code /api/v1/matrix/queries/{id}}.
   */
  public String relativeStatusPath(final UUID queryId) {
    return UriComponentsBuilder.fromPath(normalizedApiBasePath)
        .pathSegment("matrix", "queries", queryId.toString())
        .build()
        .toUriString();
  }

  /**
   * Path to the query result resource, e.g. {@code /api/v1/matrix/queries/{id}/result}.
   */
  public String relativeResultPath(final UUID queryId) {
    return UriComponentsBuilder.fromPath(normalizedApiBasePath)
        .pathSegment("matrix", "queries", queryId.toString(), "result")
        .build()
        .toUriString();
  }

  /**
   * Same rules as the instance constructor: trim, leading slash, no trailing slash, fallback {@code /api/v1}.
   */
  public static String normalizeApiBasePath(final String basePath) {
    return linkBasePath(basePath);
  }

  /**
   * Trims, adds a leading slash, strips trailing slashes, and falls back to {@code /api/v1} when the path is empty or {@code /}.
   */
  private static String linkBasePath(final String basePath) {
    if (basePath == null || basePath.isBlank()) {
      return DEFAULT_BASE;
    }
    String s = basePath.trim();
    if ("/".equals(s)) {
      return DEFAULT_BASE;
    }
    if (!s.startsWith("/")) {
      s = "/" + s;
    }
    while (s.length() > 1 && s.endsWith("/")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
