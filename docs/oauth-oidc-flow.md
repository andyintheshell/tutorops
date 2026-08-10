# TutorOps OAuth 2.0 / OIDC flow

TutorOps uses Keycloak as its OpenID Connect (OIDC) identity provider. The React application is a public browser client, and the Spring Boot API is an OAuth 2.0 resource server. The browser never sends a client secret.

## Components

| Component | Local address | Keycloak client | Responsibility |
| --- | --- | --- | --- |
| Keycloak | `http://localhost:8081` | `tutorops` realm | Authenticates users and issues tokens |
| React frontend | `http://localhost:5173` | `tutorops-web` | Starts the browser login flow |
| Spring API | `http://localhost:8080` | `tutorops-api` | Validates access tokens, persists application users, and enforces authorization |
| PostgreSQL | `localhost:5432` | — | Stores application data; local access is loopback-only |

The realm is imported from `infra/keycloak/import/tutorops-realm.json` when the Keycloak container starts for the first time.

## Login flow

```text
Browser -> React: open application
React -> Keycloak: check-sso using a hidden iframe
Keycloak -> React: existing session state, if present
Browser -> React: click Sign in
React -> Keycloak: authorization-code request with S256 PKCE challenge
Keycloak -> React: authorization code
React -> Keycloak: authorization code plus PKCE verifier
Keycloak -> React: access token and ID token
React -> API: request with Authorization: Bearer <access-token>
API: validate JWT signature, issuer, audience, and authorities
API -> React: protected response
```

## Frontend initialization

`web/src/main.tsx` creates a `keycloak-js` client using these Vite variables:

```env
VITE_KEYCLOAK_URL=http://localhost:8081
VITE_KEYCLOAK_REALM=tutorops
VITE_KEYCLOAK_CLIENT_ID=tutorops-web
VITE_API_CLIENT_ID=tutorops-api
VITE_API_BASE_URL=http://localhost:8080
```

It initializes Keycloak with:

```ts
{
  onLoad: 'check-sso',
  pkceMethod: 'S256',
  checkLoginIframe: false,
  silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
}
```

`check-sso` lets the UI detect an existing Keycloak session without forcing a login. The silent callback page is loaded in a hidden iframe and posts its URL back to the application. It must be available at:

```text
http://localhost:5173/silent-check-sso.html
```

The `tutorops-web` client is configured as a public client with Standard Flow enabled, redirect URI `http://localhost:5173/*`, and web origin `http://localhost:5173`.

PKCE protects the authorization-code flow in a browser. The frontend creates a code verifier, sends only its derived S256 challenge to Keycloak, and later proves possession of the verifier when exchanging the authorization code. No client secret is stored in the frontend.

## Access-token validation

The API is configured as a JWT resource server in `api/src/main/resources/application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8081/realms/tutorops
          audiences:
            - tutorops-api
```

For every bearer-token request, Spring Security:

1. Reads the JWT from the `Authorization: Bearer <token>` header.
2. Uses the issuer metadata to discover Keycloak’s signing keys.
3. Verifies the token signature and issuer.
4. Verifies that the token contains the `tutorops-api` audience.
5. Converts token claims into Spring authorities.

The issuer, audience, API client ID, and CORS origin can be overridden with environment variables:

```env
TUTOROPS_OIDC_ISSUER=http://localhost:8081/realms/tutorops
TUTOROPS_OIDC_AUDIENCE=tutorops-api
TUTOROPS_OIDC_API_CLIENT_ID=tutorops-api
TUTOROPS_CORS_ALLOWED_ORIGIN=http://localhost:5173
```

## Cloud smoke profile

The `cloud-smoke` Spring profile is intended only for a minimal deployment
smoke check when the API cannot reach Keycloak. It does not configure an OAuth2
resource server and therefore does not validate or accept bearer tokens.

With this profile, `/api/public/**`, `/actuator/health`, and `/actuator/info`
are permitted, as are `OPTIONS /**` requests for CORS preflight. All other
requests are denied, including `/api/me` and role-protected endpoints.
Preflight permission does not expose application data. This profile must not
be used for a user-facing or production deployment.

## Roles and authorization

Keycloak client roles are represented in the token under:

```json
{
  "resource_access": {
    "tutorops-api": {
      "roles": ["student", "tutor", "admin"]
    }
  }
}
```

`KeycloakClientRoleConverter` maps these to Spring authorities:

```text
student -> ROLE_student
tutor   -> ROLE_tutor
admin   -> ROLE_admin
```

The API authorization rules are:

| Endpoint | Access |
| --- | --- |
| `/api/public/**` | Public |
| `/actuator/health` | Public |
| `/actuator/info` | Public |
| `/api/me` | Any authenticated user |
| `/api/student/**` | `ROLE_student` |
| `/api/tutor/**` | `ROLE_tutor` |
| `/api/admin/**` | `ROLE_admin` |
| Everything else | Denied by default |

The authorization probes verify the three role paths:

```text
GET /api/student/ping
GET /api/tutor/ping
GET /api/admin/ping
```

Successful responses are role-specific, for example:

```json
{
  "status": "ok",
  "role": "student"
}
```

## Current-user endpoint

The API keeps an application-user record keyed by the validated `(issuer, subject)` pair. This prevents the same subject value from colliding across trusted issuers. Spring Data JPA accesses the repository through the service layer, and Flyway owns schema changes. Hibernate runs in validation mode (`ddl-auto: validate`) and does not mutate the schema.

`GET /api/me` returns a safe DTO only when the authenticated identity already has an application user record:

```json
{
  "id": "<subject>",
  "username": "<preferred_username>",
  "email": "<email>",
  "firstName": "<given_name>",
  "lastName": "<family_name>",
  "roles": ["student"]
}
```

The raw JWT and persistence entities are not returned.

`PUT /api/me` provisions or updates the application user from required claims in the validated access token (`email` and a display-name claim), then returns the same safe DTO. The client cannot supply the issuer, subject, or account identifier. A missing record on `GET /api/me` returns `404`; it is not implicitly created by a read.

The first schema migration is `api/src/main/resources/db/migration/V1__create_app_user.sql`. Local startup reads database settings from the root `.env` file:

```env
POSTGRES_DB=tutorops
POSTGRES_USER=tutorops
POSTGRES_PASSWORD=replace-with-a-local-random-password
TUTOROPS_DB_URL=jdbc:postgresql://localhost:5432/tutorops
```

## CORS

Because the frontend and API use different origins, the browser requires the API to approve cross-origin requests. The API allows the configured frontend origin, including preflight `OPTIONS` requests, and permits the headers needed for bearer-token calls:

- `Authorization`
- `Content-Type`
- `Accept`

Credentials/cookies are disabled. Authentication uses the bearer access token instead.

CSRF protection is ignored for `/api/**` because those endpoints do not
authenticate requests with cookies; state-changing API requests must carry a
validated bearer access token in the `Authorization` header. CSRF protection
remains enabled for non-API endpoints.

## Local startup

1. Create local environment values from the examples. Do not commit real credentials.
2. Start Keycloak from the repository root:

   ```bash
   docker compose up -d keycloak postgres
   ```

3. Start the API from `api/` (Flyway runs during startup):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Start the frontend from `web/`:

   ```bash
   npm run dev
   ```

5. Open `http://localhost:5173` and sign in.

If the realm or client configuration changes, remember that the persistent `keycloak-data` volume can preserve the previous imported configuration.
