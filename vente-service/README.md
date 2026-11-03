# Vente Service

The Vente Service is a Spring Boot REST API for recording pharmacy sales. It persists data in the existing MySQL `ventes` and `ligne_ventes` tables, registers with Eureka, and calls the other services through OpenFeign.

## Technology

- Java 21 and Spring Boot 3.3
- Spring MVC for the REST API
- Spring Data JPA and Hibernate for persistence
- MySQL
- Eureka client and OpenFeign
- Springdoc OpenAPI and Swagger UI

## REST endpoints

All endpoints use the `/api/ventes` base path. The API Gateway routes them on port `8081`.

| Method | Path | Success response | Description |
| --- | --- | --- | --- |
| `POST` | `/api/ventes` | `201 Created` | Create a sale |
| `GET` | `/api/ventes/{id}` | `200 OK` | Get a sale and its billing lines |

Swagger UI is available directly on the service at `http://localhost:8084/swagger-ui/index.html`.

Create requests use this JSON shape. `ordonnanceId` is optional:

```json
{
  "ordonnanceId": 5,
  "lignes": [
    { "medicamentId": 1, "quantite": 2 }
  ]
}
```

Prices always come from `medicament-service`. Lines for the same medicament are merged.

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Empty cart, missing field, or a quantity that is not positive |
| `404 Not Found` | Unknown sale, medicament, or prescription |
| `409 Conflict` | Insufficient stock, or the prescription is not `EN_ATTENTE` |
| `503 Service Unavailable` | A required service is down |

## How a sale is processed

1. If `ordonnanceId` is given, `GET /api/ordonnances/{id}` must return the status `EN_ATTENTE`.
2. For each line, `GET /api/medicaments/{id}` provides the price and `GET /api/stocks/disponible/{id}` confirms the quantity.
3. In one local transaction, the sale is written and `PUT /api/stocks/deduire-multiple` deducts all lines. That endpoint is all-or-nothing, and if it fails the sale is rolled back.
4. If `ordonnanceId` is given, `PUT /api/ordonnances/{id}/servir` closes the prescription. If this fails the sale is kept, the error is logged, and the response has an `avertissement` field.

## Known limits

- If `stock-service` deducts the stock but its response times out (5 seconds), the sale is rolled back while the stock stays deducted. Such cases appear in the logs as remote call failures.
- The schema has no prescription lines, so the medicaments sold are not compared with the prescription.

## Contract with ordonnance-service

`ordonnance-service` is expected to return JSON with a `statut` field from `GET /api/ordonnances/{id}`, using `EN_ATTENTE` for a prescription that can be served.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build vente-service medicament-service stock-service api-gateway
```

## Build and test

Tests do not need MySQL or Eureka. From the repository root:

```bash
mvn -f vente-service/pom.xml test
```
