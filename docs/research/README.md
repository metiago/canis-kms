# Research evidence index

The [repository cleanup report](repository-cleanup-report.md) locates archived
publication and product planning. Future work belongs in GitHub Issues.
Files here retain the methods, observed results, claim audits, and sanitized artifacts used
to review the CANIS research and JOSS work. Working-tree observations are not fixed-release
verification.

Interpret historical security results within the current [CANIS threat model](../threat-model.md):
CANIS assumes an organization-managed corporate host and trusted operating system,
administrators, deployment automation, service account, and identity systems. Tests of key-file
permissions, ACLs, transport, and fail-closed startup assess accidental exposure, unsafe
configuration, and access by non-privileged identities. They do not demonstrate resistance to
root or Administrator access, a compromised host, HSM-level extraction, or a public cloud KMS
threat model. The dated results below remain unchanged; follow their stated platform and
revision limits rather than treating this scope note as new test evidence.

| Work | Predefined method | Observed results | Claim audit | Reproduction |
| --- | --- | --- | --- | --- |
| CODE-01 integrity prerequisites | [Gap analysis and plan](../code01-integrity.md#gap-analysis-and-implemented-plan) | [Local candidate results](../code01-integrity.md#acceptance-evidence), [JOSS-02 fixed Windows suite](joss02-validation.md#fixed-revision-build-and-tests) | [Limits and compatibility](../code01-integrity.md#operational-and-compatibility-limits) | Fixed-revision Windows tests passed; POSIX permission/symlink and broader packaged-startup gate remain open |
| CODE-02 asynchronous process harness | [Method and commands](code02-method.md) | [Local execution record](code02-results.md) | Security and platform limits in the method | [Windows](run-code02.ps1), [POSIX](run-code02.sh) |
| CODE-03 whole-file memory preflight | [Prespecified method](code03-method.md) | [Windows result and boundary](code03-results.md), [run rows](code03-runs.csv), [file rows](code03-files.csv) | Size and platform limits in the result | [Windows runner](run-code03.ps1), [exporter](export-code03.ps1) |
| Issue #31 representative workload and baselines | [Protocol](issue31-protocol.md), [CANIS baseline method](issue31-baseline-method.md) | [Absolute Windows baseline](issue31-baseline-results.md), [run rows](issue31-baseline-runs.csv), [file rows](issue31-baseline-files.csv) | Acceptance and limits in the baseline result | [Runner](run-issue31-baseline.ps1), [exporter](export-issue31-baseline.ps1), [analysis](analyze-issue31-baseline.py); fixed-revision full study pending |
| Issue #33 delayed multi-service interoperability | [Functional method](issue33-method.md) | [Windows observations and acceptance](issue33-results.md), [final JSON/CSV hashes](issue33-artifacts.sha256), [candidate D smoke](joss02-validation.md#minimal-smoke-reproductions) | Shared-trust and study limits in result | [Windows](run-issue33.ps1), [POSIX](run-issue33.sh), [artifact verifier](verify-issue33.py); full fixed-revision/POSIX series pending |
| Issue #34 controlled storage and audit faults | [Prespecified method](issue34-method.md) | [Windows observations](issue34-results.md), [raw JSON/CSV](issue34-artifacts/), [manifest](issue34-artifacts.sha256), [candidate E smoke](joss02-validation.md#minimal-smoke-reproductions) | Fault interpretation and limits in result | [Windows campaign](run-issue34-campaign.ps1), [single-case Windows](run-issue34.ps1), [POSIX wrapper](run-issue34.sh), [artifact verifier](verify-issue34.py); full backlog matrix and fixed-revision/POSIX series pending |
| Issue #35 consolidated study evidence | [Retrospective synthesis method](issue35-method.md) | [Consolidated report](issue35-results.md), [manuscript claim matrix (archived)](repository-cleanup-report.md#archived-files), and aligned [paper](../../joss-papers/paper.md) | [Threats and limitations](issue35-results.md#10-threats-to-validity), [acceptance review](issue35-results.md#issue-35-acceptance-review) | Source-study commands and read-only artifact checks in [reproducibility](issue35-results.md#9-reproducibility); fixed-revision execution gate open |
| JOSS-01 shared trust boundary | [Method](joss01-method.md) | [Results](joss01-results.md) | — | [Windows](run-joss01.ps1), [POSIX](run-joss01.sh) |
| JOSS-02 release boundary | [Method](joss02-method.md), [fixed candidate (archived)](repository-cleanup-report.md#archived-files) | [Candidate validation and four smokes](joss02-validation.md), [earlier working-tree results (archived)](repository-cleanup-report.md#archived-files) | [Final claim audit](joss02-final-claim-audit.md), [final release boundary](joss02-release-boundary-final.md); [earlier audit (archived)](repository-cleanup-report.md#archived-files) remains historical | `mvn clean verify` and the four commands in [validation](joss02-validation.md#minimal-smoke-reproductions) passed on the candidate; independent reproduction remains open |
| JOSS-03 reviewer workflow | [Method](joss03-method.md) | [Results](joss03-results.md) | [Matrix (archived)](repository-cleanup-report.md#archived-files) | [Windows](run-joss03.ps1), [POSIX](run-joss03.sh) |
| JOSS-04 bibliography | Earlier method, results, and matrix [archived](repository-cleanup-report.md#archived-files) | [Current bibliography check](joss-final-paper-validation.md#bibliography-and-link-check) | Five cited entries checked | Bibliography in [paper.bib](../../joss-papers/paper.bib) |
| JOSS-05 manuscript alignment | Earlier method, results, and matrix [archived](repository-cleanup-report.md#archived-files) | [Final paper validation](joss-final-paper-validation.md) | Current manuscript and render inspected | [Static check](check-joss05-paper.ps1), [Windows preview builder](build-joss05-windows.ps1) |
| JOSS-06 metadata and disclosure | [Author-approved audit](joss06-audit.md) | [Committed manuscript](../../joss-papers/paper.md) | [Acceptance review](joss06-audit.md#acceptance-review) | Compare paper YAML/disclosures with the audit; actual submission date is deferred |
| JOSS-07 submission snapshot | [Backlog criteria (archived)](repository-cleanup-report.md#archived-files) | [Tagged-release snapshot](joss07-snapshot.md), [final pre-submission audit (archived)](repository-cleanup-report.md#archived-files) | [Requirement matrix](joss07-snapshot.md#requirement-matrix) | Exact-tag Windows verification and adapted paper build passed; independent reproduction and canonical paper build remain open |

The JOSS-03 `*-suite-report.csv` and `*-test-cases.csv` files are
sanitized snapshots tied to its results page. The earlier JOSS-02 CSV summaries
are [archived](repository-cleanup-phase2.md#archived-files). The [JOSS-05 draft PDF (archived)](repository-cleanup-report.md#archived-files)
records an adapted local build. `joss03-posix-report-fixture.xml` and
`VerifyPosixCoverage.java` support the historical POSIX report gate. The
[Issue #1 POSIX gate](VerifyIssue1PosixCoverage.java) and
[Windows gate](VerifyWindowsCoverage.java) additionally require master-key
owner, permission, and ACL cases to execute on their respective CI platforms.
The [JOSS-02 summary exporter](export-joss02-summary.ps1) regenerates its CSV summaries.
Keep generated secrets and raw test logs out of this directory.
The CODE-01 record describes a later local working tree. Archived JOSS-02 and JOSS-05
results are dated snapshots and should not be read as findings about the CODE-01 candidate.
