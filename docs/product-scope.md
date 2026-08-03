# TutorOps Product Scope

**Version:** 0.1

**Last updated:** August 2026

## 1. Product summary

TutorOps is a web-based tutoring operations platform for independent language tutors and their students.

The platform allows tutors to publish professional profiles, manage their availability, receive lesson bookings, and maintain private operational notes. Students can discover tutors, view available lesson times, create bookings, and manage their own appointments. Platform administrators can support users and manage the service.

TutorOps is also intended to demonstrate how a small SaaS product can be designed and built using secure-by-default product-development practices.

## 2. Problem statement

Independent tutors often depend on a collection of disconnected tools for:

* public profiles;
* lesson scheduling;
* student management;
* appointment notes;
* administrative support;
* payment-status tracking.

This creates fragmented workflows and can result in inconsistent access controls, unnecessary exposure of personal information, and difficulty maintaining a reliable record of tutoring activity.

TutorOps will provide a small, coherent system for managing these workflows while maintaining clear boundaries between public, student-owned, tutor-owned, and administrator-only information.

## 3. Product goals

The MVP should:

1. Allow tutors to create and maintain public tutor profiles.
2. Allow tutors to publish available lesson slots.
3. Allow authenticated students to book available lesson slots.
4. Allow students to view and manage their own bookings.
5. Allow tutors to view bookings assigned to them.
6. Allow tutors to create private notes associated with their own bookings.
7. Allow administrators to manage users and investigate operational issues.
8. Enforce authentication, role-based permissions, and resource ownership on the server.
9. Maintain an audit trail for security-sensitive and business-critical actions.
10. Demonstrate secure OAuth 2.0 and OpenID Connect integration.
11. Include automated tests for important authorization and business-logic requirements.
12. Remain inexpensive or free to build and operate as a portfolio project.

## 4. Security and engineering goals

TutorOps should demonstrate practical experience with:

* Java and Spring Boot;
* Spring Security;
* REST API design;
* PostgreSQL;
* database migrations;
* Docker-based local development;
* OAuth 2.0;
* OpenID Connect;
* Authorization Code flow with PKCE;
* JWT access-token validation;
* role-based access control;
* object-level authorization;
* BOLA/IDOR prevention;
* secure multi-user data isolation;
* threat modelling;
* abuse-case analysis;
* security regression testing;
* AWS identity and infrastructure;
* Terraform;
* CI/CD security controls.

These are engineering goals for the project, not guarantees that every technology will be included in the first implementation milestone.

## 5. Target users

### 5.1 Anonymous visitor

An unauthenticated visitor who can:

* view public tutor profiles;
* view publicly available tutor information;
* view available lesson times;
* begin the registration or login process.

An anonymous visitor cannot create or modify bookings or access non-public user information.

### 5.2 Student

An authenticated user who can:

* maintain their own basic account information;
* browse public tutor profiles;
* view tutor availability;
* create bookings;
* view their own bookings;
* cancel their own bookings when permitted;
* view information intentionally shared with them by their tutor.

A student cannot access another student’s bookings or tutor-private information.

### 5.3 Tutor

An authenticated user approved to operate as a tutor who can:

* maintain their own public tutor profile;
* create and manage their own availability;
* view bookings assigned to them;
* update the operational status of their assigned bookings;
* add and view private tutor notes for their assigned bookings.

A tutor cannot modify another tutor’s profile, availability, bookings, or private notes.

### 5.4 Platform administrator

An authenticated user entrusted with platform administration who can:

* manage user status and role assignments;
* view and manage tutor profiles;
* investigate booking problems;
* assist with account and operational issues;
* access information required for legitimate administrative functions;
* review relevant audit activity.

Administrator access is privileged and must be explicitly authorized, auditable, and limited to legitimate platform operations.

## 6. Tenant and ownership model

TutorOps is a single platform containing multiple tutor workspaces.

For the MVP:

* each tutor controls one tutor workspace;
* public profile information belongs to the tutor but is intentionally visible to all users;
* tutor availability belongs to the tutor workspace;
* a booking is shared between the student who created it and the tutor with whom it was created;
* private tutor notes belong to the assigned tutor workspace;
* students may create bookings with multiple tutors;
* tutors must remain isolated from other tutors’ private data;
* platform administrators operate across workspaces only through explicit administrator permissions.

The initial implementation does not need a separate `tenant` entity if tutor ownership can be represented clearly through the authenticated tutor identity. The authorization model must nevertheless preserve tutor-to-tutor data isolation.

## 7. MVP capabilities

### 7.1 Public tutor profiles

The system will support:

* tutor display name;
* profile photograph or avatar reference;
* short biography;
* languages taught;
* supported proficiency levels;
* lesson description;
* lesson duration options;
* general availability status.

The MVP will not include advanced search ranking or recommendations.

### 7.2 Availability management

Tutors will be able to:

* create an available lesson slot;
* view their own slots;
* update an unbooked slot;
* remove an unbooked slot;
* see whether a slot is available, booked, cancelled, or unavailable.

The system must prevent invalid or conflicting availability.

### 7.3 Booking management

Students will be able to:

* select an available tutor slot;
* create one booking for that slot;
* view their bookings;
* cancel a booking subject to the MVP cancellation rules.

Tutors will be able to:

* view bookings assigned to them;
* view relevant student and lesson information;
* update permitted booking fields;
* add private operational notes.

Administrators will be able to investigate and manage bookings where necessary.

### 7.4 Identity and account linking

Users will authenticate through an OpenID Connect identity provider.

The application will associate the external identity with an internal application user using the stable issuer and subject identity, rather than relying on email as the permanent identifier.

The application will support the following roles:

* `STUDENT`
* `TUTOR`
* `ADMIN`

A user may hold more than one role only when there is a defined product requirement and the combination has been reviewed.

### 7.5 Audit events

The MVP will record security-relevant events such as:

* account first seen or provisioned;
* tutor-profile changes;
* availability creation, modification, or deletion;
* booking creation or cancellation;
* private-note modification;
* administrator actions;
* role or account-status changes;
* important authorization failures.

Audit records must not contain passwords, access tokens, refresh tokens, authorization headers, or unnecessary sensitive content.

## 8. Data classification

### 8.1 Public data

Examples:

* public tutor name;
* public biography;
* languages taught;
* lesson descriptions;
* public availability.

Public data may be available without authentication.

### 8.2 Internal account data

Examples:

* identity-provider subject;
* email address;
* application roles;
* account status;
* internal user identifiers.

Internal account data must not be publicly exposed unless a specific field is intentionally included in a public profile.

### 8.3 Student and booking data

Examples:

* student identity;
* booking history;
* lesson time;
* booking status;
* communication or operational details.

This information is visible only to the relevant student, assigned tutor, and authorized administrators.

### 8.4 Tutor-private data

Examples:

* tutor-private booking notes;
* internal tutor workflow information;
* information not intentionally shared with the student.

Tutor-private data is visible only to the assigned tutor and authorized administrators.

### 8.5 Security-sensitive data

Examples:

* access tokens;
* refresh tokens;
* client configuration;
* cryptographic material;
* database credentials;
* cloud credentials;
* administrative security events.

Security-sensitive data must not be returned through normal product APIs or written into application logs.

## 9. MVP business rules

1. A lesson slot may have at most one active booking.
2. A student may book only a currently available slot.
3. A tutor may manage only availability belonging to that tutor.
4. A tutor may access only bookings assigned to that tutor.
5. A student may access only bookings created for that student.
6. Tutor-private notes are never included in student-facing API responses.
7. Ownership and permissions are determined by the server.
8. Client-supplied user, tutor, or role identifiers are not trusted as proof of identity.
9. Administrative actions require an authenticated administrator role.
10. Booking-state transitions must follow explicitly defined rules.
11. Requests must be validated before creating or changing persistent data.
12. Relevant state changes must be auditable.

## 10. Explicit non-goals for the initial MVP

The initial MVP will not include:

* real payment processing;
* subscription billing;
* refunds;
* automatic tutor payouts;
* calendar-provider synchronization;
* email or SMS delivery;
* video conferencing;
* group lessons;
* recurring bookings;
* reviews and ratings;
* student-tutor chat;
* file uploads;
* AI-generated lesson materials;
* advanced analytics;
* a mobile application;
* multiple tutoring businesses or enterprise organizations;
* production-grade high availability;
* production Kubernetes hosting;
* comprehensive regulatory certification.

Some of these capabilities may be considered after the core security and authorization model is working.

## 11. Technical boundaries

The planned MVP consists of:

* a browser-based React and TypeScript client;
* a Java and Spring Boot REST API;
* a PostgreSQL database;
* an external OAuth 2.0/OpenID Connect identity provider;
* a Docker-based local development environment.

The identity provider is responsible for authenticating the user and issuing tokens.

TutorOps remains responsible for:

* validating access tokens;
* mapping authenticated identities to application users;
* enforcing application roles;
* enforcing resource ownership;
* validating business rules;
* protecting sensitive data;
* recording audit activity.

Authentication by an identity provider does not automatically authorize a user to access a TutorOps resource.

## 12. MVP completion criteria

The MVP is complete when:

1. Anonymous users can view public tutor profiles and availability.
2. Students, tutors, and administrators can authenticate through OIDC.
3. The browser uses Authorization Code with PKCE.
4. The API validates access tokens and rejects invalid token use.
5. Tutors can manage their own profiles and availability.
6. Students can create and manage their own bookings.
7. Tutors can manage bookings assigned to them.
8. Tutor-private notes remain inaccessible to students.
9. Tutor-to-tutor and student-to-student access attempts are rejected.
10. Object-level authorization tests run automatically.
11. Important state-changing actions generate audit records.
12. The complete local environment can be started from documented repository instructions.
13. CI runs build, test, and initial security checks.
14. The deployed demonstration can be recreated without undocumented manual configuration.

## 13. Success indicators

The initial project should provide evidence that:

* the core tutor-booking workflow works end to end;
* authorization is based on authenticated identity and resource relationships;
* common access-control failures have automated regression tests;
* security requirements are visible in design documents and code;
* the application can be demonstrated without exposing real customer data;
* the infrastructure remains within the project’s defined cost limits.

## 14. Future possibilities

Possible post-MVP extensions include:

* calendar synchronization;
* payment integration;
* email notifications;
* file uploads;
* lesson-material management;
* organization-level tenants;
* tutor assistants or delegated access;
* consent and retention management;
* an AI lesson-material service;
* production-grade managed database hosting;
* managed Kubernetes deployment.

Future capabilities must be reviewed against the authorization model and security invariants before implementation.
