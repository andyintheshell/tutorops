# tutorops

[![CI](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml)
[![CodeQL](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml)
[![Dependency Review](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml)
[![Gitleaks](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml)
[![SCA](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml)
[![Container Security](https://github.com/andyintheshell/tutorops/actions/workflows/container-security.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/container-security.yml)

Security-focused tutoring operations platform for tutor profiles, availability, bookings, and administration. Built incrementally with Java, Spring Boot, React, OAuth 2.0/OIDC, Docker, and automated AppSec/DevSecOps testing.

## Local development

The local stack consists of a Keycloak container, a containerized Spring Boot API image, and a Vite-powered React frontend:

| Component | Local address | Start/build command |
| --- | --- | --- |
| Keycloak | `http://localhost:8081` | `docker compose up -d keycloak` |
| API | `http://localhost:8080` | `cd api && ./mvnw spring-boot:run` |
| Frontend | `http://localhost:5173` | `cd web && npm run dev` |

### Prerequisites

- Docker with Compose
- Node.js and npm
- Java 21, only when running the API or its tests outside a container

### Configure local services

From the repository root, create the local environment files. These files are ignored by Git and must not contain production credentials:

```bash
cp .env.example .env
cp web/.env.example web/.env
```

The Keycloak realm and clients are imported from [`infra/keycloak/import/tutorops-realm.json`](infra/keycloak/import/tutorops-realm.json) on first startup.

### Start Keycloak

```bash
docker compose up -d keycloak
```

Keycloak is exposed on port `8081`; its container listens on port `8080`. The Compose project persists its data in the `keycloak-data` volume.

### Run the API

For the fastest local development feedback, run the API with the Maven wrapper:

```bash
cd api
./mvnw spring-boot:run
```

The API Dockerfile performs a full Maven verification during the image build and produces a small non-root runtime image:

```bash
docker build --tag tutorops-api:local api
```

The current `compose.yaml` starts Keycloak only; use the Maven command above to run the API against the local OIDC configuration.

Run the API verification lifecycle:

```bash
cd api
./mvnw verify
```

### Run the frontend

Install dependencies and start the Vite development server:

```bash
cd web
npm ci
npm run dev
```

Open [`http://localhost:5173`](http://localhost:5173) and sign in through Keycloak. The frontend uses the `tutorops-web` public client and the API uses the `tutorops-api` audience. To create a production frontend bundle, run `npm run build` from `web/`.

Public API endpoints:

- `GET /api/public/status`
- `GET /actuator/health`

For the complete OIDC flow, configuration overrides, and troubleshooting notes, see [`docs/oauth-oidc-flow.md`](docs/oauth-oidc-flow.md).

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
