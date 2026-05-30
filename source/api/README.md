# API

Java Spring Boot service for source-mesh-matrix.

## Maven

Build, run, generate code, and test from `source/api`.

### Run locally

#### Run the API

```bash
mvn spring-boot:run
```

Requires ClickHouse on `localhost:8123` (database `smm`, user `aerius`). Flyway applies baseline from `src/main/resources/db/migration/baseline/` on startup.

And optional catalog dummy data from `src/main/resources/db/migration/dummy-data/` when enabled:

```bash
export AERIUS_FLYWAY_DUMMY_DATA_ENABLED=true
mvn spring-boot:run
```

Or using property: `aerius.flyway.dummy-data.enabled` (default `false`).

#### Example matrix query request (dummy data)

`POST /api/v1/matrix/queries` — body matches `db/migration/dummy-data`:

```json
{
  "calculation_version": "2025.2",
  "substances": ["NOx"],
  "result_types": ["Deposition"],
  "source_characteristics": {
    "height": 10,
    "heat_content": 1050,
    "spread": 10,
    "emission_diurnal_variation": 2
  },
  "mesh_points": [{ "x": 100001, "y": 400001 }],
  "source_points": [{ "x": 100, "y": 200 }]
}
```

Status: `GET /api/v1/matrix/queries/{queryId}`

Result: `GET /api/v1/matrix/queries/{queryId}/result` (one-shot; task removed after 200)

### Code generation

#### Generate OpenAPI code

```bash
mvn generate-resources
```

Generated sources are written to `target/openapi/`.

#### Regenerate jOOQ classes

```bash
mvn -Pgenerate-jooq generate-sources
```

Requires a running ClickHouse. The `generate-jooq` profile runs Flyway migrate before codegen. Flyway Maven uses `jdbc:clickhouse://` (`codegen.flyway.url`); the running API uses `jdbc:ch:http://` for the datasource.

#### Regenerate jOOQ with custom JDBC

```bash
mvn -Pgenerate-jooq generate-sources \
  -Dgenerate-jooq.url=jdbc:ch:http://localhost:8123/smm \
  -Dcodegen.flyway.url=jdbc:clickhouse://localhost:8123/smm \
  -Dgenerate-jooq.username=some-user \
  -Dgenerate-jooq.password=some-password
```

### Build

#### Clean install

```bash
mvn clean install
```

Also runs code generation (OpenAPI, etc.) as part of the build.

### Tests

#### Run unit tests

```bash
mvn test
```

Surefire `test` phase only; `*IT.java` is excluded. No ClickHouse required.

#### Run unit tests + integration tests

```bash
docker run -d -p 8123:8123 --restart unless-stopped database:latest
mvn verify
```

`mvn verify` runs unit tests (Surefire) then integration tests (Failsafe). Database ITs need ClickHouse on `localhost:8123` ([build images](../../docker/README.md); database `smm`, user `aerius`). The Docker image starts only the server. [`MatrixRepositoryIT`](src/test/java/nl/aerius/smm/api/matrix/repository/MatrixRepositoryIT.java) uses `@ActiveProfiles("test")` and `application-test.properties` so Flyway creates the schema on startup; JDBC settings come from main `application.properties`.
