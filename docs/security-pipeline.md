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
| Container image scanning | Trivy | Pull request and main | Blocks fixed high and critical findings |
| Container SBOM | Trivy | Pull request and main | Artifact generated |
| Infrastructure-as-code scanning | Trivy config scanner | Pull request, main, weekly | Report-only baseline |
| Container publishing | Docker CLI / GHCR | Merged main only, after checks | Blocking gate |

The container publishing gate resolves each required workflow by its file
identity and validates the triggering workflow path, rather than trusting
workflow display names.

## Current enforcement policy

The pipeline blocks a change when:

- the application does not compile;
- automated tests fail;
- Gitleaks identifies a probable committed secret;
- Dependency Review identifies a newly introduced high or critical
  known vulnerability;
- CodeQL cannot successfully analyze the application.

Repository SCA and infrastructure-as-code findings from Trivy remain
report-only and are triaged before stronger blocking thresholds are
introduced. Container image scanning is a blocking policy for fixed HIGH and
CRITICAL findings; unfixed image findings remain report-only under the current
configuration.

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

- image signing and provenance attestations;
- Kubernetes policy-as-code;
- dynamic application security testing;
- cloud deployment gates.
