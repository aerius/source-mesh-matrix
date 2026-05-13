# API

The API is a Java Spring Boot application.

## Maven

The project uses Maven for building, running, and generating code.

### Common commands

Run the API locally

```bash
mvn spring-boot:run
```

Generate API code from the OpenAPI specification. The generated code is placed in `target/openapi`

```bash
mvn generate-resources
```

Regenerate jOOQ classes from ClickHouse (requires a running server). The generated code should be committed when changed.

```bash
mvn -Pgenerate-jooq generate-sources
```

Override JDBC settings if needed, for example:

```bash
mvn -Pgenerate-jooq generate-sources \
  -Dgenerate-jooq.url=jdbc:ch:http://localhost:8123/some-database \
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

Run integration tests against the **database** image from [Docker: Build all images](../../docker/README.md) (HTTP on `localhost:8123`, database
`smm`, user `aerius` — same as `src/test/resources/application.properties`). After building images, start ClickHouse, then from `source`:

```bash
docker run -d -p 8123:8123 --restart unless-stopped database:latest
mvn -Pwith-integration-tests test
```
