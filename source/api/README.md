# API

The API is a Java Spring Boot application.

## Maven

The project uses Maven for building, running, and generating code.

### Common commands

Run the API locally (requires ClickHouse running)

```bash
mvn spring-boot:run
```

Generate API code from the OpenAPI specification. The generated code is placed in `target/openapi`

```bash
mvn generate-resources
```

Regenerate jOOQ classes from ClickHouse. The `generate-jooq` profile runs Flyway migrate before codegen.

```bash
mvn -Pgenerate-jooq generate-sources
```

Override JDBC settings if needed, for example:

```bash
mvn -Pgenerate-jooq generate-sources \
  -Dgenerate-jooq.url=jdbc:ch:http://localhost:8123/smm \
  -Dcodegen.flyway.url=jdbc:clickhouse://localhost:8123/smm \
  -Dgenerate-jooq.username=some-user \
  -Dgenerate-jooq.password=some-password
```

Clean and build the project (also triggers code generation)

```bash
mvn clean install
```

Run tests

```bash
mvn test
```

Integration tests need a local ClickHouse ([build images](../../docker/README.md); `localhost:8123`, database `smm`, user `aerius`). The Docker image starts only the server; Flyway in this project creates the schema when ITs run.

```bash
docker run -d -p 8123:8123 --restart unless-stopped database:latest
mvn -Pwith-integration-tests test
```
