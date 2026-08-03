# TutorOps Security Invariants

**Version:** 0.1

**Last updated:** August 2026

## 1. Purpose

This document defines security properties that TutorOps is intended to preserve regardless of specific technologies or implementation details.

A security invariant describes a condition that must remain true during normal operation, error handling, maintenance, and future product development.

Examples of implementation choices that are not themselves invariants include:

* using Spring Security;
* using a particular identity provider;
* storing data in PostgreSQL;
* applying a specific framework annotation;
* using a particular AWS service.

Those technologies may help enforce the invariants, but they may be replaced without changing the required security property.

## 2. Status and revision policy

These invariants are defined before the complete system architecture exists.

That is intentional.

At this stage, they serve as:

* product-security requirements;
* threat-modelling inputs;
* design constraints;
* test-planning inputs;
* review criteria for future implementation decisions.

The document is expected to evolve as product requirements become clearer.

An invariant may be changed only when:

1. the product requirement has genuinely changed;
2. the security impact has been reviewed;
3. related authorization rules and tests are updated;
4. the change is recorded in version history;
5. the new rule remains explicit and testable.

An invariant must not be silently weakened because the chosen implementation makes it inconvenient.

## 3. Identity and authentication invariants

### INV-IDENTITY-001: Server-established identity

The application derives the caller’s identity from a successfully validated authentication context.

A user ID, tutor ID, student ID, email address, role, or username supplied in a request is not accepted as proof of identity.

### INV-IDENTITY-002: Stable external identity

An external user is associated with an internal TutorOps account using a stable identity-provider issuer and subject.

Email address alone is not treated as a permanent identity key.

### INV-IDENTITY-003: Protected operations require authentication

Any operation that creates, modifies, deletes, or retrieves non-public data requires valid authentication unless the operation is explicitly documented as public.

### INV-IDENTITY-004: API access uses an access token

The TutorOps API accepts only tokens intended to authorize API access.

An ID token is not treated as an API access token merely because it is correctly signed.

### INV-IDENTITY-005: Token validity is fully evaluated

A token is not considered valid based only on its signature.

The application must also evaluate relevant properties such as:

* trusted issuer;
* token expiration;
* token activation time where applicable;
* intended token purpose;
* expected application client or audience;
* required scopes or roles.

### INV-IDENTITY-006: Untrusted identity providers are rejected

A token issued by an identity provider or security domain not explicitly trusted by TutorOps does not establish a TutorOps identity.

### INV-IDENTITY-007: Authentication secrets are not exposed

Passwords, authorization codes, access tokens, refresh tokens, client secrets, and equivalent authentication material are not exposed through:

* API responses;
* application logs;
* error messages;
* public source code;
* analytics events;
* audit records.

## 4. Role and privilege invariants

### INV-ROLE-001: Roles cannot be self-assigned

A normal user cannot grant themselves the `TUTOR`, `ADMIN`, or any other elevated application role.

### INV-ROLE-002: Administrator access is explicit

Administrative access is granted only through an explicitly approved administrator role or equivalent privileged authorization mechanism.

An email domain, frontend route, request parameter, or display name does not confer administrator access.

### INV-ROLE-003: Default access is unprivileged

A newly authenticated user receives no more privilege than the documented default user role.

Authentication alone does not grant tutor or administrator privileges.

### INV-ROLE-004: Privilege changes are auditable

Successful changes to application roles, account status, or equivalent privilege-bearing attributes produce an audit record identifying the actor, target, action, time, and outcome.

### INV-ROLE-005: UI restrictions are not authorization controls

The absence of a button, route, form, or other browser interface does not replace server-side authorization.

All protected operations are authorized by the API or another trusted server-side component.

## 5. Tutor isolation invariants

### INV-TUTOR-001: Tutor profiles are owner-controlled

A tutor may modify only the tutor profile associated with that tutor, unless an explicitly authorized administrator performs the operation.

### INV-TUTOR-002: Tutor availability is isolated

A tutor may create, modify, or delete only availability belonging to that tutor’s workspace, unless an explicitly authorized administrator performs the operation.

### INV-TUTOR-003: Tutor booking access is assignment-bound

A tutor may access a booking only when that tutor is assigned to the booking, unless an explicitly authorized administrator performs the operation.

Possession of the `TUTOR` role alone is insufficient.

### INV-TUTOR-004: Tutor-private notes remain private

Tutor-private notes are not exposed to:

* anonymous users;
* students;
* unassigned tutors;
* public APIs.

They are accessible only to the assigned tutor and explicitly authorized administrators.

### INV-TUTOR-005: Tutor identifiers do not establish ownership

Changing a tutor identifier in a path, query parameter, request body, or client state does not allow a tutor to act on another tutor’s resources.

Ownership is established from trusted server-side relationships.

## 6. Student isolation invariants

### INV-STUDENT-001: Student booking access is owner-bound

A student may access only bookings associated with that authenticated student, unless an explicitly authorized administrator performs the operation.

Possession of the `STUDENT` role alone is insufficient.

### INV-STUDENT-002: Students cannot act for other students

A student cannot create, modify, cancel, or retrieve a booking on behalf of another student by supplying a different student identifier.

The student associated with a normal booking operation is derived from the authenticated identity.

### INV-STUDENT-003: Student identifiers do not establish ownership

Changing a student ID, booking ID, email address, or related identifier in a request does not grant access to another student’s resources.

### INV-STUDENT-004: Student responses exclude tutor-private data

No student-facing response includes tutor-private notes or other information classified as tutor-private.

## 7. Booking and business-logic invariants

### INV-BOOKING-001: One active booking per slot

An availability slot may be associated with at most one active booking.

Concurrent requests must not result in multiple active bookings for the same slot.

### INV-BOOKING-002: Only available slots may be booked

A student can create a booking only for a slot that exists, is bookable, and remains available when the booking is committed.

### INV-BOOKING-003: Booking relationships are server-controlled

The application validates and establishes the relationships between:

* student;
* tutor;
* availability slot;
* booking.

A caller cannot arbitrarily create a booking that associates unrelated or unauthorized resources.

### INV-BOOKING-004: Booking ownership is immutable through ordinary updates

A normal booking update cannot change the owning student or assigned tutor.

Any exceptional reassignment process must be separately authorized and audited.

### INV-BOOKING-005: Booking state transitions are constrained

A booking may transition only between explicitly permitted states.

A caller cannot set an arbitrary status simply because the status value is syntactically valid.

### INV-BOOKING-006: Cancelled or completed bookings follow defined modification rules

Once a booking reaches a terminal or restricted state, subsequent modifications are limited to explicitly permitted operations.

### INV-BOOKING-007: Authorization is evaluated against current state

Authorization and availability decisions use current server-side data.

A stale browser view or previously observed resource state does not guarantee that an operation remains permitted.

### INV-BOOKING-008: Concurrency does not bypass business rules

Concurrent requests must not bypass:

* one-booking-per-slot restrictions;
* ownership requirements;
* state-transition rules;
* uniqueness constraints;
* authorization checks.

## 8. Data exposure invariants

### INV-DATA-001: Public endpoints expose only public fields

An endpoint classified as public returns only data explicitly classified for public disclosure.

The existence of a field in the database or persistent entity does not make it public.

### INV-DATA-002: Responses are purpose-specific

Student, tutor, administrator, and public callers receive representations appropriate to their permissions and purpose.

The application does not expose all persistent fields by default.

### INV-DATA-003: Persistent entities are not automatically public contracts

Database entities are not serialized directly as unrestricted API responses.

API response models intentionally define which fields are exposed.

### INV-DATA-004: Denied requests reveal no protected content

A failed authentication or authorization decision does not return protected resource data, private metadata, internal stack traces, or raw security context.

### INV-DATA-005: Resource existence is not unnecessarily disclosed

Authorization failure behaviour does not provide an unnecessary oracle for discovering private booking, user, note, or tutor-workspace resources.

### INV-DATA-006: Sensitive data is minimized

TutorOps collects, stores, displays, and logs only the personal and operational data required for the defined product functions.

### INV-DATA-007: Production data is not used in insecure environments

Real user data is not copied into public demonstrations, test fixtures, local development environments, or public repositories without an approved and safe process.

### INV-DATA-008: Secrets remain outside source control

Database credentials, cloud credentials, token-signing material, private keys, and equivalent secrets are not committed to source control.

## 9. Administrative-access invariants

### INV-ADMIN-001: Administrative actions are attributable

Every security-sensitive administrator action is attributable to a specific authenticated administrator identity.

Shared or anonymous administrator identities are not used for normal administrative operations.

### INV-ADMIN-002: Administrative actions are auditable

Administrative access that changes users, roles, tutor status, bookings, or protected data creates an audit event.

### INV-ADMIN-003: Administrative APIs remain server-protected

An administrator frontend or hidden route does not itself protect administrator functionality.

Administrator APIs independently verify administrator authorization.

### INV-ADMIN-004: Administrator access remains purpose-limited

Administrator access does not automatically justify returning every field from every resource.

Administrator responses should still follow data-minimization and legitimate-purpose principles.

### INV-ADMIN-005: Users cannot approve their own privilege elevation

A user cannot approve, complete, or directly cause their own promotion to tutor or administrator through a normal user-controlled workflow.

## 10. Audit and logging invariants

### INV-AUDIT-001: Security-sensitive changes are recorded

The system records sufficient information to investigate important changes involving:

* roles;
* account status;
* tutor profiles;
* availability;
* bookings;
* tutor-private notes;
* administrator activity.

### INV-AUDIT-002: Audit events identify actor and target

Where applicable, an audit event identifies:

* acting identity;
* action;
* affected resource;
* time;
* outcome;
* request or correlation identifier.

### INV-AUDIT-003: Audit data excludes authentication secrets

Audit records do not contain passwords, access tokens, refresh tokens, authorization headers, client secrets, or private cryptographic material.

### INV-AUDIT-004: Logs do not become an alternate data leak

Application and infrastructure logs do not contain unnecessary private booking contents, tutor-private notes, or full sensitive request bodies.

### INV-AUDIT-005: Log failure does not silently authorize an operation

A failure in audit or telemetry processing must not cause the application to bypass an authorization requirement.

The exact fail-open or fail-closed behaviour for required audit records must be defined for each operation.

## 11. Error-handling invariants

### INV-ERROR-001: Errors do not expose internals

Client-visible errors do not expose:

* stack traces;
* database queries;
* database credentials;
* filesystem paths;
* raw tokens;
* secret configuration;
* internal service addresses;
* unnecessary identity-provider details.

### INV-ERROR-002: Authentication and authorization failures are distinguishable where appropriate

The system uses appropriate response semantics for:

* missing or invalid authentication;
* insufficient general privileges;
* inaccessible or non-existent resources.

The selected behaviour must be consistent and must not leak protected information.

### INV-ERROR-003: Failure does not partially commit protected changes

A failed authorization, validation, or business-rule check does not leave behind a partially applied protected state change.

## 12. Infrastructure and deployment invariants

### INV-INFRA-001: Workloads do not depend on embedded cloud credentials

Application workloads use approved temporary credentials or workload identities where available rather than long-lived cloud access keys embedded in code or configuration.

### INV-INFRA-002: The database is not publicly exposed

The application database is not directly accessible from the public internet.

### INV-INFRA-003: Management access is restricted

Administrative infrastructure access is limited to approved management paths and is not exposed through unnecessary public ports.

### INV-INFRA-004: Sensitive configuration is externally managed

Sensitive configuration is supplied through an approved secret or configuration-management mechanism rather than source code or public container images.

### INV-INFRA-005: Transport protection is used for external authenticated traffic

Authenticated production or demonstration traffic crossing an untrusted network uses HTTPS or an equivalent protected transport.

### INV-INFRA-006: Deployment environments are reproducible

Security-relevant cloud and deployment configuration is documented or represented as code sufficiently to identify and review changes.

### INV-INFRA-007: Destruction and cleanup are deliberate

Removing a development environment does not unintentionally leave behind public resources, reusable credentials, sensitive snapshots, or forgotten billable infrastructure.

## 13. Development and testing invariants

### INV-DEV-001: Authorization rules have negative tests

For every security-sensitive resource operation, tests cover at least one caller who must be denied.

Positive owner tests alone are insufficient.

### INV-DEV-002: Same-role cross-user access is tested

Where resources are user- or tutor-owned, automated tests verify that another user with the same role cannot access the resource.

This specifically addresses BOLA and IDOR risks.

### INV-DEV-003: Security regressions receive permanent tests

A discovered authorization, validation, or data-exposure vulnerability receives an automated regression test when technically practical.

### INV-DEV-004: Test identities contain no real user secrets

Test users, tokens, passwords, and data are synthetic and safe to store or regenerate according to repository policy.

### INV-DEV-005: Security checks do not replace design review

Automated scanning supports but does not replace:

* threat modelling;
* authorization review;
* business-logic testing;
* secure code review;
* architecture review.

### INV-DEV-006: Dependency and build provenance are reviewable

The project maintains sufficient dependency and build information to identify the application components used in a deployed artifact.

The exact SBOM or signing implementation may be added in a later milestone.

## 14. Availability and abuse-resistance invariants

These invariants are intentionally limited for the MVP.

### INV-ABUSE-001: Untrusted input is bounded

Requests enforce reasonable limits on:

* field length;
* collection size;
* date ranges;
* pagination size;
* request-body size;
* other attacker-controlled resource consumption.

### INV-ABUSE-002: Repeated sensitive operations can be constrained

Authentication, booking creation, and other abuse-sensitive operations can be rate-limited or otherwise constrained when exposed publicly.

The exact mechanism may be introduced after the initial local implementation.

### INV-ABUSE-003: User input is not executed as code

User-controlled profile, booking, and note content is treated as data and is not executed as application code, database code, template code, or operating-system commands.

### INV-ABUSE-004: Output is safely represented

User-controlled content is encoded or rendered appropriately for its output context to prevent script or markup injection.

## 15. Deferred invariants

The following areas are outside the initial MVP and will require additional invariants before implementation:

* payment authorization and refund integrity;
* file-upload isolation and malware handling;
* calendar-provider authorization;
* email and SMS abuse;
* delegated tutor assistants;
* organization-level tenants;
* AI prompt and retrieval isolation;
* video-conferencing integration;
* data-retention and deletion workflows;
* account recovery;
* refresh-token rotation;
* elevated administrator authentication.

Deferring these invariants does not authorize implementation without security review. It means the corresponding capability is not yet part of the approved product scope.

## 16. Review checklist

When reviewing a new feature or design, ask:

1. Which authenticated identity performs the operation?
2. Which role is required?
3. Which resource relationship or ownership condition is required?
4. Can another user with the same role access the resource?
5. Can the caller manipulate an identifier to change ownership?
6. Which fields may this caller receive?
7. Does the operation create or change sensitive state?
8. Must the action be audited?
9. What happens during concurrent requests?
10. What negative authorization tests are required?
11. Does the feature introduce a new kind of sensitive data?
12. Does it require a new or revised invariant?

## 17. Initial validation plan

Each invariant will eventually be linked to one or more of:

* architecture documentation;
* source-code enforcement;
* database constraints;
* automated unit or integration tests;
* security tests;
* infrastructure-as-code checks;
* CI/CD policies;
* manual review procedures.

Until an invariant is implemented and tested, it remains a documented requirement rather than a verified property of the running system.
