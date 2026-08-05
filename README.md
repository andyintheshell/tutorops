# tutorops

[![CI](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/ci.yml)
[![CodeQL](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/codeql.yml)
[![Dependency Review](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/dependency-review.yml)
[![Gitleaks](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/gitleaks.yml)
[![SCA](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/sca.yml)
[![Container Security](https://github.com/andyintheshell/tutorops/actions/workflows/container-security.yml/badge.svg)](https://github.com/andyintheshell/tutorops/actions/workflows/container-security.yml)

Security-focused tutoring operations platform for tutor profiles, availability, bookings, and administration. This project demonstrates full-stack delivery across Java, Spring Boot, React, OAuth 2.0/OIDC, Docker, Terraform, AWS, and automated AppSec/DevSecOps testing.

## Technology snapshot

| Area | Technologies and practices |
| --- | --- |
| Backend | Java 21, Spring Boot, Maven, layered REST API architecture |
| Frontend | React, TypeScript, Vite, Keycloak browser client |
| Identity and security | Keycloak, OAuth 2.0/OIDC, PKCE, JWT resource-server validation, role and ownership authorization |
| Containers and cloud | Docker, GHCR, AWS EC2 Graviton ARM64, Terraform, default VPC networking |
| Delivery and AppSec | GitHub Actions, CodeQL, Trivy, CycloneDX SBOM, Gitleaks, Dependabot, dependency review |

## What this demonstrates

- A working vertical slice from browser authentication through an authorized API.
- Security controls designed into the API, with the browser kept outside the authorization boundary.
- Immutable container deployment using a verified ARM64 image pinned by manifest digest.
- Infrastructure-as-code deployment to a real AWS EC2 environment, including a repeatable smoke-test profile.
- Automated build, dependency, secret, code, container, and supply-chain checks in CI.

## Architecture at a glance

The React frontend uses Keycloak’s public-client authorization-code flow with PKCE. The Spring Boot API validates OAuth 2.0 access tokens and enforces authorization server-side. Terraform provisions the temporary AWS smoke environment, where Docker pulls the digest-pinned ARM64 image from GHCR.

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

## AWS smoke deployment

Terraform can deploy the API to a working ARM64 Amazon EC2 smoke environment.
The configuration in [`infra/terraform`](infra/terraform) creates a `t4g.small`
instance in the default VPC, pulls the public GHCR image pinned by immutable
ARM64 manifest digest, and runs the API with the `cloud-smoke` profile.

This deployment is intended for temporary API checks, not as a production
environment. The smoke profile exposes only `/api/public/**`,
`/actuator/health`, and `/actuator/info`; authenticated and role-protected API
endpoints remain unavailable. The default security group allows port 8080 from
the internet, so destroy the environment after testing or restrict
`api_ingress_cidrs` in `infra/terraform/variables.tf`.

From the Terraform directory:

```bash
cd infra/terraform
terraform init
terraform plan
terraform apply

public_ip="$(terraform output -raw public_ip)"
curl "http://${public_ip}:8080/api/public/status"
curl "http://${public_ip}:8080/actuator/health"

terraform destroy
```

See [`infra/terraform/README.md`](infra/terraform/README.md) for AWS
prerequisites, networking assumptions, readiness checks, and cost guidance.

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
