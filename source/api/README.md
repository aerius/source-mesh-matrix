# API

Java Spring Boot service for source-mesh-matrix.

## Maven

Build, run, generate code, and test from `source/api`.

### Run locally

#### Run the API

```bash
mvn spring-boot:run
```

Requires ClickHouse on `localhost:8123` (database `smm`, user `aerius`; for instance by running the docker [image](../../docker/README.md)).

Flyway applies schema migrations from `src/main/resources/db/migration/` on startup.

Matrix query settings under `aerius.query`: `executor.*` (async thread pool), `task.ended-retention` (default `24h`),
`task.cleanup-interval` (default `15m`). Ended tasks not consumed via `/result` are purged after retention.

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

Requires a running ClickHouse. The `generate-jooq` profile runs Flyway migrate before codegen.
Flyway Maven uses `jdbc:clickhouse://` (`codegen.flyway.url`); the running API uses `jdbc:ch:http://` for the datasource.

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

Surefire runs `*Test` / `*Tests` classes only. No ClickHouse required.

#### Run unit tests + integration tests

```bash
# Optional: make sure to start with a clean database
docker rm -f $(docker ps -q --filter publish=8123)

docker run -d -p 8123:8123 --restart unless-stopped database:latest
mvn verify -Pfailsafe
```

Add profile `failsafe` to run `*IT.java` (Failsafe). Database ITs need a running ClickHouse instance.
