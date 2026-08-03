# TutorOps Security Pipeline

**Status:** Implemented baseline

**Version:** 0.1

## Purpose

The TutorOps security pipeline provides automated build validation,
static analysis, dependency review, vulnerability detection, secret
scanning, and software-component inventory.

Automated tools supplement rather than replace threat modelling,
authorization testing, secure code review, and manual vulnerability
triage.

## Active controls

| Control | Tool | Trigger | Enforcement |
|---|---|---|---|
| Build and tests | Maven / GitHub Actions | Pull request and main | Blocking |
| SAST | CodeQL | Pull request, main, weekly | Findings reviewed; required check |
| New dependency review | GitHub Dependency Review | Pull request | Blocks high and critical vulnerabilities |
| Dependency monitoring | Dependabot | Continuous / weekly | Alerts and update PRs |
| Repository SCA | Trivy | Main and weekly | Report-only baseline |
| Secret scanning | Gitleaks | Pull request, main, weekly | Blocking |
| Application SBOM | CycloneDX Maven Plugin | Every package build | Artifact generated |

## Current enforcement policy

The pipeline blocks a change when:

- the application does not compile;
- automated tests fail;
- Gitleaks identifies a probable committed secret;
- Dependency Review identifies a newly introduced high or critical
  known vulnerability;
- CodeQL cannot successfully analyze the application.

Trivy baseline findings are initially report-only. Findings are triaged
before stronger blocking thresholds are introduced.

## Finding lifecycle

1. Validate the finding.
2. Determine whether the affected component or code is reachable.
3. Assess severity and exploitability.
4. Identify an owner and remediation.
5. Apply and test the fix.
6. Verify that the scanner or regression test confirms remediation.
7. Document any accepted or suppressed finding with justification and
   an expiration or review condition.

## Exceptions

Exceptions must:

- identify the exact finding;
- explain why remediation is not currently practical;
- document compensating controls;
- identify a review condition or expiration;
- avoid broad scanner exclusions.

## Deferred controls

The following controls will be added when the corresponding artifact
exists:

- container image scanning;
- container SBOM generation;
- image signing and provenance attestations;
- infrastructure-as-code scanning;
- Kubernetes policy-as-code;
- dynamic application security testing;
- cloud deployment gates.
