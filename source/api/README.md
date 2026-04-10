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

Clean and build the project (also triggers code generation)
```bash
mvn clean install
```

Run tests
```bash
mvn test
```