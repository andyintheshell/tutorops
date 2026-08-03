# TutorOps Authorization Model

**Version:** 0.1

**Last updated:** August 2026

## 1. Purpose

This document defines who may perform security-sensitive operations in TutorOps and under which conditions.

It covers:

* application roles;
* resource ownership;
* tutor-workspace isolation;
* endpoint-level permissions;
* object-level permissions;
* administrator privileges;
* authorization failure behaviour;
* authorization testing requirements.

This document describes the intended product-security model. Specific framework annotations, database queries, and identity-provider claim formats will be documented separately as implementation decisions.

## 2. Core authorization principles

TutorOps follows these principles:

1. Authentication and authorization are separate decisions.
2. A valid identity-provider token does not by itself grant access to every application resource.
3. The API is the authoritative enforcement point.
4. The browser interface is not a security boundary.
5. Application roles provide coarse-grained authorization.
6. Resource ownership and relationships provide fine-grained authorization.
7. Authorization decisions use the authenticated identity, not a client-supplied identity.
8. Access is denied unless it is explicitly permitted.
9. Administrative privileges are explicit and auditable.
10. Authorization rules must be covered by automated tests.
11. Security-sensitive responses expose only the fields required by the caller.
12. Failure responses should not unnecessarily reveal the existence of inaccessible resources.

## 3. Authentication context

After validating an access token, the application establishes an authenticated principal containing at least:

* identity-provider issuer;
* identity-provider subject;
* TutorOps internal user ID, once resolved;
* application roles;
* granted scopes where relevant;
* token expiration;
* request or correlation ID.

The stable external identity is the combination of:

```text
issuer + subject
```

Email address is not used as the permanent authorization identity because an email may change, be reassigned, or differ across identity providers.

## 4. Application roles

### 4.1 `STUDENT`

Represents a user who may discover tutors and create bookings.

A student may:

* view public information;
* view public tutor availability;
* create bookings for themselves;
* view bookings belonging to themselves;
* cancel their own bookings when allowed;
* update permitted student-owned account information.

A student may not:

* access another student’s booking;
* act on behalf of another student;
* access tutor-private notes;
* modify tutor profiles or availability;
* grant themselves another role;
* perform administrator operations.

### 4.2 `TUTOR`

Represents a user approved to offer lessons through TutorOps.

A tutor may:

* view public information;
* manage their own tutor profile;
* manage availability belonging to their tutor workspace;
* view bookings assigned to them;
* modify fields tutors are permitted to manage;
* create and view tutor-private notes for their assigned bookings.

A tutor may not:

* modify another tutor’s profile;
* modify another tutor’s availability;
* view or modify another tutor’s bookings;
* view another tutor’s private notes;
* create bookings while impersonating a student unless separately authorized;
* grant or modify application roles;
* perform administrator operations.

### 4.3 `ADMIN`

Represents a trusted platform administrator.

An administrator may:

* manage user status and application roles;
* review and manage tutor profiles;
* investigate bookings;
* perform documented support operations;
* access records required for platform administration;
* review audit information.

Administrator privileges must not be inferred from an email address, frontend route, request parameter, or unvalidated token claim.

Administrator actions must be logged with sufficient context to support later review.

## 5. Role assignment

Application roles are managed through an approved administrative process.

For the MVP:

* new authenticated users receive the `STUDENT` role by default;
* the `TUTOR` role is granted through an administrator-controlled process;
* the `ADMIN` role is granted only through a separate privileged process;
* users cannot modify their own roles through a normal product API;
* identity-provider group membership may be used to communicate roles, but the application must validate and map the relevant claim explicitly.

The exact role source may change between local and cloud identity providers. The product-level meaning of each role must remain the same.

## 6. Resource ownership model

### 6.1 User account

A user account is owned by the authenticated user represented by its issuer and subject.

The user may access only the fields and operations intentionally exposed for self-service.

### 6.2 Tutor profile

A tutor profile is owned by one tutor user.

* Public profile fields may be viewed by anyone.
* Private or operational fields may be viewed or changed only by the owning tutor and authorized administrators.
* A tutor cannot select another tutor’s identifier and thereby modify that profile.

### 6.3 Availability slot

An availability slot belongs to one tutor profile.

* Public availability may be viewed by anyone.
* Creation, modification, and deletion require the owning tutor or an authorized administrator.
* Booked slots may have additional modification restrictions.
* Ownership is derived through the tutor profile relationship.

### 6.4 Booking

A booking is associated with:

* one student;
* one tutor;
* one availability slot;
* one current booking state.

A booking may be accessed by:

* the student who owns the booking;
* the tutor assigned to the booking;
* an authorized administrator.

The operations permitted to the student and tutor are not necessarily the same.

### 6.5 Tutor-private note

A tutor-private note belongs to a booking and the tutor assigned to that booking.

It may be accessed only by:

* the assigned tutor;
* an authorized administrator with a legitimate administrative purpose.

It must not appear in:

* anonymous responses;
* student-facing responses;
* public tutor responses;
* generic booking DTOs accidentally shared with students.

### 6.6 Audit event

Audit events may be viewed only by authorized administrative or security functions.

Normal students and tutors cannot retrieve the general audit stream.

A user-facing activity history may be built separately and must not expose internal security information.

## 7. Authorization matrix

Legend:

* **Yes:** operation is permitted.
* **Own:** permitted only for a resource owned by the caller.
* **Assigned:** permitted only when the caller is the tutor assigned to the booking.
* **Public:** permitted for intentionally public fields only.
* **Limited:** permitted only for documented administrative purposes.
* **No:** operation is not permitted.

| Resource and operation                   | Anonymous | Student |    Tutor |   Admin |
| ---------------------------------------- | --------: | ------: | -------: | ------: |
| View public tutor list                   |    Public |  Public |   Public |     Yes |
| View public tutor profile                |    Public |  Public |   Public |     Yes |
| Create tutor profile                     |        No |      No |      Own |     Yes |
| View private tutor-profile fields        |        No |      No |      Own |     Yes |
| Update tutor profile                     |        No |      No |      Own |     Yes |
| Delete or deactivate tutor profile       |        No |      No |       No |     Yes |
| View public availability                 |    Public |  Public |   Public |     Yes |
| Create availability                      |        No |      No |      Own |     Yes |
| Update availability                      |        No |      No |      Own |     Yes |
| Delete unbooked availability             |        No |      No |      Own |     Yes |
| Modify another tutor’s availability      |        No |      No |       No |     Yes |
| Create booking                           |        No |     Own |       No |     Yes |
| View booking as student                  |        No |     Own |       No |     Yes |
| View booking as tutor                    |        No |      No | Assigned |     Yes |
| Cancel booking as student                |        No |     Own |       No |     Yes |
| Update tutor-managed booking state       |        No |      No | Assigned |     Yes |
| View student-visible booking information |        No |     Own | Assigned |     Yes |
| View tutor-private note                  |        No |      No | Assigned | Limited |
| Create or update tutor-private note      |        No |      No | Assigned | Limited |
| Manage user roles                        |        No |      No |       No |     Yes |
| View general audit events                |        No |      No |       No |     Yes |
| Change own application role              |        No |      No |       No |      No |

## 8. Planned endpoint rules

The exact paths may change during implementation.

### 8.1 Public endpoints

```text
GET /api/public/status
GET /api/public/tutors
GET /api/public/tutors/{tutorId}
GET /api/public/tutors/{tutorId}/availability
```

These endpoints expose only intentionally public fields.

### 8.2 Authenticated-user endpoints

```text
GET /api/me
PATCH /api/me
```

These endpoints operate on the authenticated user.

They must not accept an arbitrary user ID as proof of ownership.

### 8.3 Student endpoints

```text
POST   /api/student/bookings
GET    /api/student/bookings
GET    /api/student/bookings/{bookingId}
DELETE /api/student/bookings/{bookingId}
```

Requirements:

* the authenticated principal must possess `STUDENT`;
* the student identity is derived from the authenticated principal;
* the student cannot supply a different student identity;
* individual booking operations require booking ownership;
* listing endpoints return only the authenticated student’s bookings.

### 8.4 Tutor endpoints

```text
GET    /api/tutor/profile
PUT    /api/tutor/profile

GET    /api/tutor/availability
POST   /api/tutor/availability
PUT    /api/tutor/availability/{slotId}
DELETE /api/tutor/availability/{slotId}

GET    /api/tutor/bookings
GET    /api/tutor/bookings/{bookingId}
PATCH  /api/tutor/bookings/{bookingId}/status
GET    /api/tutor/bookings/{bookingId}/notes
PUT    /api/tutor/bookings/{bookingId}/notes
```

Requirements:

* the authenticated principal must possess `TUTOR`;
* the tutor identity is derived from the authenticated principal;
* profile and availability operations are restricted to the owning tutor;
* booking operations are restricted to the assigned tutor;
* private-note operations require the assigned-tutor relationship.

### 8.5 Administrator endpoints

```text
GET   /api/admin/users
PATCH /api/admin/users/{userId}/status
PATCH /api/admin/users/{userId}/roles

GET   /api/admin/bookings
GET   /api/admin/audit-events
```

Requirements:

* the authenticated principal must possess `ADMIN`;
* administrator status must be established from a validated and approved role source;
* administrator actions must be audited;
* sensitive administrative responses should support data minimization.

## 9. Server-side enforcement strategy

The planned enforcement model contains multiple layers.

### 9.1 Token validation

The API validates the access token before accepting the identity.

Expected checks include:

* trusted issuer;
* valid signature;
* permitted signing algorithm;
* expiration;
* not-before time where present;
* intended token type;
* expected application client or audience;
* required scopes where applicable.

### 9.2 Route-level authorization

Routes require the relevant high-level role.

Examples:

* student routes require `STUDENT`;
* tutor routes require `TUTOR`;
* administrator routes require `ADMIN`.

Route-level authorization is necessary but insufficient for resource access.

### 9.3 Method or service-level authorization

Business operations verify ownership and resource relationships.

Examples:

* a tutor may update a slot only when the slot belongs to that tutor;
* a student may retrieve a booking only when the booking belongs to that student;
* a tutor may retrieve a booking only when assigned to it.

### 9.4 Data-access restrictions

Where practical, database queries should include the expected ownership relationship rather than loading an arbitrary object and checking it much later.

Preferred conceptual operations include:

```text
find booking by booking ID and student ID
find booking by booking ID and assigned tutor ID
find availability by slot ID and tutor ID
```

This reduces the risk of accidentally returning a resource before authorization is evaluated.

### 9.5 Response-field authorization

Different callers may receive different representations of the same booking.

For example:

* a student booking response excludes tutor-private notes;
* a tutor booking response may include tutor-operational fields;
* an administrator response may include additional investigation fields.

The application must not rely solely on JSON serialization of persistent entities.

## 10. Denial behaviour

TutorOps follows a deny-by-default approach.

Expected responses:

* `401 Unauthorized` when the request lacks valid authentication;
* `403 Forbidden` when the caller is authenticated but lacks a required general permission;
* `404 Not Found` may be used for inaccessible object identifiers when revealing existence would provide unnecessary information.

The same object-access policy should be applied consistently to avoid creating a resource-existence oracle.

Authorization failures must not reveal:

* another user’s identity;
* private resource contents;
* internal database identifiers beyond what the caller already possesses;
* details of role-mapping implementation;
* raw token contents;
* stack traces.

## 11. Administrator-access requirements

Administrator access is intentionally powerful and therefore requires additional controls.

The MVP should require:

* an explicit administrator role;
* no self-service administrator-role assignment;
* audit logging of administrator actions;
* identification of the acting administrator;
* identification of the affected resource;
* operation outcome;
* request correlation information.

Future versions may add:

* stronger authentication requirements;
* separate administrator clients;
* just-in-time privilege;
* approval workflows;
* reason-for-access capture;
* privileged-session monitoring.

These are future considerations rather than MVP requirements.

## 12. Authorization test requirements

Every protected resource type must include positive and negative tests.

### 12.1 Authentication tests

Test that:

* an anonymous caller cannot access protected endpoints;
* a malformed token is rejected;
* an expired token is rejected;
* a token from an untrusted issuer is rejected;
* an inappropriate token type is rejected.

### 12.2 Role tests

Test that:

* a student cannot call tutor operations;
* a tutor cannot call administrator operations;
* a user without a required role is denied;
* an administrator can perform explicitly authorized administrator operations.

### 12.3 Object-ownership tests

For each applicable resource, test that:

* the owner can access it;
* another user with the same role cannot access it;
* a user with a different role cannot access it unless explicitly permitted;
* an administrator can access it only through an administrator-authorized operation.

### 12.4 Response-data tests

Test that:

* public responses contain only public fields;
* student responses do not contain tutor-private notes;
* tutor responses do not expose unrelated student data;
* denied responses do not contain protected resource data.

### 12.5 Business-logic tests

Test that:

* a student cannot book a slot for another student;
* a student cannot book an unavailable slot;
* a tutor cannot modify another tutor’s availability;
* a tutor cannot access another tutor’s booking;
* a tutor cannot add notes to another tutor’s booking;
* a caller cannot change ownership by modifying an identifier;
* a user cannot change their own role;
* invalid booking-state transitions are rejected.

## 13. Audit requirements

The application should record:

* authenticated actor;
* operation;
* affected resource;
* success or failure;
* timestamp;
* request or correlation ID;
* administrator status where applicable.

The audit system must not record:

* access tokens;
* refresh tokens;
* passwords;
* authorization headers;
* unnecessary private-note contents;
* database credentials.

Authorization denials should be logged selectively. Logging every routine denial may create noise or store unnecessary information, while repeated or sensitive denials may be security-relevant.

## 14. Open authorization questions

The following decisions may be refined during implementation:

1. Can one account simultaneously hold `STUDENT` and `TUTOR`?
2. Should administrator actions use the same frontend client as normal product actions?
3. Which booking fields may students and tutors modify after creation?
4. Which cancellation states and time limits should the MVP support?
5. Should inaccessible object identifiers return `403` or `404`?
6. Which administrator operations require an explicit reason?
7. Should tutor approval be represented as a role, account state, or separate tutor-profile state?
8. Should authorization roles be stored only in the identity provider, only in the application database, or reconciled between both?
9. Which audit events should be available in a user-facing activity history?
10. What is the minimum student information a tutor requires before a lesson?

These questions do not prevent implementation of the initial authentication and role model. They should be resolved before the affected workflows are considered complete.
