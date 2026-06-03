# API

Java Spring Boot service for source-mesh-matrix.

## Maven

Build, run, generate code, and test from `source/api`.

### Run locally

#### Run the API

```bash
mvn spring-boot:run
```

Requires ClickHouse on `localhost:8123` (database `smm`, user `aerius`). Flyway applies schema migrations from `src/main/resources/db/migration/` on startup.

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
mvn verify -Pintegration-tests
```

`mvn verify` runs unit tests (Surefire) only. Add `-Pintegration-tests` to run `*IT.java` (Failsafe). Database ITs need ClickHouse on `localhost:8123` ([build images](../../docker/README.md); database `smm`, user `aerius`). The Docker image starts only the server. [`MatrixRepositoryIT`](src/test/java/nl/aerius/smm/api/matrix/repository/MatrixRepositoryIT.java) uses `@ActiveProfiles("test")` and `application-test.properties` so Flyway creates the schema on startup; JDBC settings come from main `application.properties`.
