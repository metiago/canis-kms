# Research evidence index

The [unified backlog](../../joss-backlog.md) is the only active task checklist.
Files here retain the methods, observed results, claim audits, and sanitized artifacts used
to review the CANIS research and JOSS work. Working-tree observations are not fixed-release
verification.

| Work | Predefined method | Observed results | Claim audit | Reproduction |
| --- | --- | --- | --- | --- |
| CODE-01 integrity prerequisites | [Gap analysis and plan](../code01-integrity.md#gap-analysis-and-implemented-plan) | [Local candidate results](../code01-integrity.md#acceptance-evidence), [JOSS-02 fixed Windows suite](joss02-validation.md#fixed-revision-build-and-tests) | [Limits and compatibility](../code01-integrity.md#operational-and-compatibility-limits) | Fixed-revision Windows tests passed; POSIX permission/symlink and broader packaged-startup gate remain open |
| CODE-02 asynchronous process harness | [Method and commands](code02-method.md) | [Local execution record](code02-results.md) | Security and platform limits in the method | [Windows](run-code02.ps1), [POSIX](run-code02.sh) |
| CODE-03 whole-file memory preflight | [Prespecified method](code03-method.md) | [Windows result and boundary](code03-results.md), [run rows](code03-runs.csv), [file rows](code03-files.csv) | Size and platform limits in the result | [Windows runner](run-code03.ps1), [exporter](export-code03.ps1) |
| Issue #31 representative workload and baselines | [Protocol](issue31-protocol.md), [CANIS baseline method](issue31-baseline-method.md) | [Absolute Windows baseline](issue31-baseline-results.md), [run rows](issue31-baseline-runs.csv), [file rows](issue31-baseline-files.csv) | Acceptance and limits in the baseline result | [Runner](run-issue31-baseline.ps1), [exporter](export-issue31-baseline.ps1), [analysis](analyze-issue31-baseline.py); fixed-revision full study pending |
| Issue #33 delayed multi-service interoperability | [Functional method](issue33-method.md) | [Windows observations and acceptance](issue33-results.md), [final JSON/CSV hashes](issue33-artifacts.sha256), [candidate D smoke](joss02-validation.md#minimal-smoke-reproductions) | Shared-trust and study limits in result | [Windows](run-issue33.ps1), [POSIX](run-issue33.sh), [artifact verifier](verify-issue33.py); full fixed-revision/POSIX series pending |
| Issue #34 controlled storage and audit faults | [Prespecified method](issue34-method.md) | [Windows observations](issue34-results.md), [raw JSON/CSV](issue34-artifacts/), [manifest](issue34-artifacts.sha256), [candidate E smoke](joss02-validation.md#minimal-smoke-reproductions) | Fault interpretation and limits in result | [Windows campaign](run-issue34-campaign.ps1), [single-case Windows](run-issue34.ps1), [POSIX wrapper](run-issue34.sh), [artifact verifier](verify-issue34.py); full backlog matrix and fixed-revision/POSIX series pending |
| Issue #35 consolidated study evidence | [Retrospective synthesis method](issue35-method.md) | [Consolidated report](issue35-results.md), [manuscript claim matrix](issue35-claim-matrix.md), and aligned [paper](../../joss-papers/paper.md) | [Threats and limitations](issue35-results.md#10-threats-to-validity), [acceptance review](issue35-results.md#issue-35-acceptance-review) | Source-study commands and read-only artifact checks in [reproducibility](issue35-results.md#9-reproducibility); fixed-revision execution gate open |
| JOSS-01 shared trust boundary | [Method](joss01-method.md) | [Results](joss01-results.md) | — | [Windows](run-joss01.ps1), [POSIX](run-joss01.sh) |
| JOSS-02 release boundary | [Method](joss02-method.md), [fixed candidate](joss02-candidate-release.md) | [Candidate validation and four smokes](joss02-validation.md), [earlier working-tree results](joss02-results.md) | [Final claim audit](joss02-final-claim-audit.md), [final release boundary](joss02-release-boundary-final.md); [earlier audit](joss02-audit.md) remains historical | `mvn clean verify` and the four commands in [validation](joss02-validation.md#minimal-smoke-reproductions) passed on the candidate; independent reproduction remains open |
| JOSS-03 reviewer workflow | [Method](joss03-method.md) | [Results](joss03-results.md) | [Matrix](joss03-claim-evidence.md) | [Windows](run-joss03.ps1), [POSIX](run-joss03.sh) |
| JOSS-04 bibliography | [Method](joss04-method.md) | [Results](joss04-results.md) | [Matrix](joss04-claim-evidence.md) | Paper build commands in method |
| JOSS-05 manuscript alignment | [Method](joss05-method.md) | [Results](joss05-results.md) | [Matrix](joss05-claim-evidence.md) | [Static check](check-joss05-paper.ps1), [Windows draft](build-joss05-windows.ps1) |
| JOSS-06 metadata and disclosure | [Author-approved audit](joss06-audit.md) | [Committed manuscript](../../joss-papers/paper.md) | [Acceptance review](joss06-audit.md#acceptance-review) | Compare paper YAML/disclosures with the audit; actual submission date is deferred |
| JOSS-07 submission snapshot | [Backlog criteria](../../joss-backlog.md#joss-07--independent-review-and-submission-snapshot-p1) | [Tagged-release snapshot](joss07-snapshot.md), [final pre-submission audit](joss-final-pre-submission-audit.md) | [Requirement matrix](joss07-snapshot.md#requirement-matrix) | Exact-tag Windows verification and adapted paper build passed; independent reproduction and canonical paper build remain open |

The JOSS-02, JOSS-03, and JOSS-05 `*-suite-report.csv` and `*-test-cases.csv` files are
sanitized snapshots tied to their results pages. The [JOSS-05 draft PDF](joss05-draft.pdf)
records an adapted local build. `joss03-posix-report-fixture.xml` and
`VerifyPosixCoverage.java` support the POSIX report gate. The
[JOSS-02 summary exporter](export-joss02-summary.ps1) regenerates its CSV summaries.
Keep generated secrets and raw test logs out of this directory.
The CODE-01 record describes a later local working tree. Earlier JOSS-02 and JOSS-05 results
remain dated snapshots and should not be read as findings about the CODE-01 candidate.
