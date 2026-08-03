# TutorOps agent instructions

## Build
- Run `./mvnw verify` before reporting completion.
- Do not skip or disable tests.
- Do not add Maven dependencies without explicit approval.

## Architecture
- Use controller -> service -> repository layering.
- Controllers must not access repositories directly.
- API DTOs must not expose persistence entities.
- Keep authorization checks in the service layer unless documented otherwise.

## Security
- Deny access by default.
- Never commit credentials, tokens or real personal data.
- Do not implement custom OAuth/OIDC token parsing.
- Add negative tests for every authorization rule.
- Treat email addresses and account identifiers as non-public.

## Git
- Keep changes limited to the requested issue.
- Do not combine feature work with unrelated formatting.
- Use small logical commits.
- Include verification commands in the PR description.
