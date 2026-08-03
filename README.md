# tutorops
Security-focused tutoring operations platform for tutor profiles, availability, bookings, and administration. Built incrementally with Java, Spring Boot, PostgreSQL, React, OAuth 2.0/OIDC, Docker, Terraform, AWS, and automated AppSec/DevSecOps testing.

## API development

Requirements:

- Java 21

Run the API:

```bash
cd api
./mvnw spring-boot:run
```

Run tests:

```bash
cd api
./mvnw test
```

Public endpoints:

- `GET /api/public/status`
- `GET /actuator/health`
