# Social Backend

Spring Boot REST API with Keycloak authentication for a social media application.

## Tech Stack

- **Java 17** + **Spring Boot 3.x**
- **Spring Security** with Keycloak OIDC
- **JPA / Hibernate** with **MySQL** (dev) / **PostgreSQL** (production)
- **Liquibase** for schema migrations

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL (local) or PostgreSQL (Docker)

### Local Development

```bash
# Run with MySQL (default profile)
mvn spring-boot:run

# Run with PostgreSQL
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

### Run Tests

```bash
mvn clean install          # unit tests only
mvn clean install -Pintegration  # include integration tests
```

## API Endpoints

| Endpoint | Method | Auth | Purpose |
|---|---|---|---|
| `/api/auth/test` | GET | No | Health check (returns "OK") |
| `/api/auth/register` | POST | No | Register new user |
| `/api/auth/login` | POST | No | Login with credentials |
| `/api/**` | Various | JWT | Protected resources |

## Deployment

See [DEPLOYMENT.md](../docs/DEPLOYMENT.md) for the full deployment guide covering Docker, CI/CD, and Render configuration.

## Project Structure

```
src/main/java/com/example/social/app/
├── config/security/     # Spring Security, CORS, Keycloak
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── model/               # JPA entities
├── repository/          # Spring Data repositories
├── service/             # Business logic
└── SocialApplication.java
```

## CI/CD

- **GitHub Actions** runs tests on PR → build + deploy on merge to `master`
- **Docker** multi-stage build (`Dockerfile`)
- **Render Blueprint** (`render.yaml`) defines all services
- See [deploy.yml](.github/workflows/deploy.yml) for workflow configuration
