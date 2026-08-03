# tutorops

[![CI](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml)
[![CodeQL](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml)
[![Dependency Review](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml)
[![Gitleaks](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml)
[![SCA](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml)

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
./mvnw verify
```

Public endpoints:

- `GET /api/public/status`
- `GET /actuator/health`
