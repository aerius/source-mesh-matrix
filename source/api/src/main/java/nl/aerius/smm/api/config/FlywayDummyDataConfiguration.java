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
package nl.aerius.smm.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayDummyDataConfiguration {

  private static final String BASELINE_LOCATION = "classpath:db/migration/baseline";
  private static final String DUMMY_DATA_LOCATION = "classpath:db/migration/dummy-data";

  @Bean
  @ConditionalOnProperty(name = "aerius.flyway.dummy-data.enabled", havingValue = "false", matchIfMissing = true)
  public FlywayConfigurationCustomizer flywayBaselineOnlyCustomizer() {
    return configuration -> configuration.locations(BASELINE_LOCATION);
  }

  @Bean
  @ConditionalOnProperty(name = "aerius.flyway.dummy-data.enabled", havingValue = "true")
  public FlywayConfigurationCustomizer flywayWithDummyDataCustomizer() {
    return configuration -> configuration.locations(BASELINE_LOCATION, DUMMY_DATA_LOCATION);
  }
}
