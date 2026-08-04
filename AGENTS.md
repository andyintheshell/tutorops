# TutorOps agent instructions

TutorOps is a security-oriented tutoring operations platform. Preserve the
security model and keep changes narrowly scoped.

## Project structure

- `api/`: Java 21 Spring Boot API and Maven wrapper.
- `web/`: React, TypeScript, Vite, and Keycloak browser client.
- `infra/keycloak/`: imported local Keycloak realm configuration.
- `compose.yaml`: local Keycloak container.
- `docs/security-invariants.md`: authoritative application security invariants.
- `docs/security-pipeline.md`: security-control and exception policy.
- `docs/oauth-oidc-flow.md`: authentication and authorization flow.

## Verification

Before reporting completion, run the relevant checks:

```bash
(cd api && ./mvnw verify)
(cd web && npm ci && npm run lint && npm run build)
docker build --pull --tag tutorops-api:local api
```

- Do not skip, disable, weaken, or make tests conditional merely to obtain a
  passing build.
- Add or update tests for behavior changes, especially authorization and data
  exposure changes.
- Keep generated build output, local environment files, credentials, and
  private keys out of commits.

## Architecture

- Use controller -> service -> repository layering in the API.
- Controllers must not access repositories directly.
- Keep business authorization decisions in the service layer.
- API DTOs must not expose persistence entities.
- Derive identity from the validated server-side security context; never trust
  client-supplied user, tutor, student, owner, or account identifiers.
- Keep frontend presentation and route visibility separate from server-side
  authorization.
- Do not introduce a persistence or infrastructure shortcut that bypasses the
  established layers.

## Authentication and authorization

- Deny access by default. Every new endpoint must have an explicit security
  rule.
- Keep public endpoints limited to intentionally public data and health/status
  information.
- Use Spring Security and Keycloak's standard OIDC/OAuth2 behavior.
- Do not implement custom JWT, OAuth, or OIDC token parsing or validation.
- Do not accept an ID token as an API access token.
- Preserve issuer, signature, expiry, activation-time, audience, and token
  purpose validation.
- Treat browser authentication as a public-client flow using PKCE. Never add a
  client secret to the frontend or expose secrets through `VITE_*` variables.
- Enforce authorization on the API, including object ownership and same-role
  cross-user isolation.
- Add negative tests for unauthenticated, wrong-role, wrong-owner, and
  same-role cross-user access where applicable.
- Do not rely on hidden frontend controls as an authorization mechanism.
- Update `docs/security-invariants.md` and `docs/oauth-oidc-flow.md` when
  security behavior or the threat model changes.

## Data protection and error handling

- Never commit credentials, tokens, passwords, private keys, certificates,
  real personal data, or production data.
- Use synthetic identities and data in tests and documentation.
- Treat email addresses, account identifiers, subject identifiers, and
  authentication claims as non-public.
- Do not log passwords, authorization headers, access tokens, refresh tokens,
  client secrets, private keys, or sensitive personal data.
- Do not return persistence entities, raw claims, security contexts, stack
  traces, internal configuration, or sensitive identifiers in API responses.
- Keep error responses generic and prevent information disclosure.
- Validate and bound untrusted input at API boundaries.
- Do not introduce wildcard CORS, credentialed cross-origin requests, or
  broadened origins without an explicit security review.
- If cookie-based authentication is introduced, document and test CSRF
  protection before enabling it.

## Dependencies and supply chain

- Do not add Maven, npm, or other runtime dependencies without explicit
  approval.
- Keep dependency lockfiles synchronized when an approved dependency changes.
- Prefer maintained, minimal dependencies with a clear security benefit.
- Do not weaken dependency review, CodeQL, Gitleaks, Trivy, SBOM generation,
  or high/critical vulnerability enforcement.
- Review dependency and action changes for transitive risk and license impact.

## Containers and local infrastructure

- Keep API images multi-stage, minimal, pinned to reviewed base-image digests,
  and running as a non-root user.
- Do not add shells, package managers, debugging tools, credentials, or local
  environment files to runtime images.
- Do not broaden published ports or bind local development services publicly
  without explicit approval.
- Keep Keycloak realm imports and local credentials synthetic.
- Preserve the separation between browser-visible URLs and container-internal
  service configuration.
- Review changes to `compose.yaml`, Dockerfiles, realm imports, ports, volumes,
  health endpoints, and security-related environment variables for
  information disclosure and privilege escalation.

## CI and GitHub Actions

- Use least-privilege workflow permissions.
- Preserve fork-pull-request safety and do not expose secrets to untrusted code.
- Pin newly added third-party GitHub Actions to immutable commit SHAs.
- Do not remove security gates or change their severity thresholds without
  documenting the decision in `docs/security-pipeline.md`.
- Keep security-sensitive workflow changes separately reviewable.

## Git and change scope

- Keep changes limited to the requested issue.
- Do not combine feature work with unrelated formatting or refactoring.
- Do not rewrite history or use destructive Git commands.
- Use small logical commits when commits are requested.
- Do not create commits, tags, releases, pull requests, or external messages
  unless explicitly requested.
- Include verification commands and any security considerations in the PR
  description.
