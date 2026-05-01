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
package nl.aerius.smm.api;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import nl.aerius.smm.api.config.AppEndpointsConfig;
import nl.aerius.smm.api.web.MatrixQueryResourceLinks;

/**
 * Lightweight test bootstrap for mapper tests. Uses {@link Configuration} instead of
 * {@link org.springframework.boot.autoconfigure.SpringBootApplication} so this class can live in
 * {@code nl.aerius.smm.api} next to {@link SMMApiApplication} without a second
 * {@link org.springframework.boot.SpringBootConfiguration} (which would break {@code @WebMvcTest} discovery).
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "nl.aerius.smm.api.mapper")
@Import({ MatrixQueryResourceLinks.class, AppEndpointsConfig.class })
public class TestApplication {
}
