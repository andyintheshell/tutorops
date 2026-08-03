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

## Security and supply-chain controls

TutorOps uses automated security controls throughout the development workflow:

| Control                       | Implementation                                       |
| ----------------------------- | ---------------------------------------------------- |
| Build and test                | Java 21, Maven and GitHub Actions                    |
| Static analysis               | CodeQL SAST with extended security queries           |
| Dependency governance         | Dependabot and pull-request dependency review        |
| Software composition analysis | Trivy vulnerability scanning                         |
| Secret detection              | Gitleaks across pull requests and repository history |
| Software bill of materials    | CycloneDX SBOM generated during Maven builds         |
| Merge protection              | Required pull requests and security status checks    |

See [`docs/security-pipeline.md`](docs/security-pipeline.md) for enforcement thresholds, finding triage, exceptions, and deferred controls. Security concerns can be reported according to [`SECURITY.md`](SECURITY.md).
