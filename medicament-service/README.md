# Medicament Service

The Medicament Service is a Spring Boot REST API for managing medicaments. It persists data in the existing MySQL `medicaments` table and registers with Eureka for service discovery.

## Technology

- Java 21 and Spring Boot 3.3
- Spring MVC for the REST API
- Spring Data JPA and Hibernate for persistence
- MySQL
- Eureka client
- Springdoc OpenAPI and Swagger UI

## API documentation

When the service is running, open Swagger UI at:

```text
http://localhost:8082/swagger-ui/index.html
```

The generated OpenAPI document is available at:

```text
http://localhost:8082/v3/api-docs
```

Swagger UI is hosted by the medicament service. The API Gateway currently routes `/api/medicaments/**`; access the Swagger UI directly on port `8082`.

## REST endpoints

All endpoints use the `/api/medicaments` base path.

| Method | Path | Success response | Description |
| --- | --- | --- | --- |
| `GET` | `/api/medicaments` | `200 OK` | List all medicaments |
| `GET` | `/api/medicaments/{id}` | `200 OK` | Get a medicament by ID |
| `POST` | `/api/medicaments` | `201 Created` | Create a medicament |
| `PUT` | `/api/medicaments/{id}` | `200 OK` | Replace a medicament's fields |
| `DELETE` | `/api/medicaments/{id}` | `204 No Content` | Delete a medicament |

Create and update requests use this JSON shape:

```json
{
  "nomCommercial": "Paracetamol",
  "dci": "Paracetamol",
  "dosage": "500 mg",
  "forme": "Tablet",
  "prixUnitaire": 4.25
}
```

`nomCommercial` is required and limited to 150 characters. `prixUnitaire` is required and must be zero or greater. Optional text fields have maximum lengths matching the database schema.

Missing IDs return `404 Not Found`; invalid request data returns `400 Bad Request`.

## Run locally

Start the MySQL and Eureka containers from the repository root:

```bash
docker compose up -d pharmacy-db discovery-service
```

Then, from the repository root, run:

```bash
mvn -f medicament-service/pom.xml spring-boot:run
```

By default the service listens on port `8082`, connects to `pharmacy_db` on `localhost:3306`, and registers with Eureka at `http://localhost:8761/eureka/`. These settings can be overridden with:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`

Hibernate is configured with `ddl-auto: validate`: it checks the schema against the entity but does not create or modify tables. The MySQL schema is initialized by `mysql-init/01-schema.sql`.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build medicament-service
```

Compose starts the database and discovery server as dependencies. The service is available directly on port `8082`; requests can also go through the API Gateway on port `8081`.

## Build and test

From the repository root:

```bash
mvn -f medicament-service/pom.xml test
```
