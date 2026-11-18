# Research evidence index

The [unified backlog](../../joss-backlog.md) is the only active task checklist.
Files here retain the methods, observed results, claim audits, and sanitized artifacts used
to review the CANIS research and JOSS work. Working-tree observations are not fixed-release
verification.

| Work | Predefined method | Observed results | Claim audit | Reproduction |
| --- | --- | --- | --- | --- |
| CODE-01 integrity prerequisites | [Gap analysis and plan](../code01-integrity.md#gap-analysis-and-implemented-plan) | [Local candidate results](../code01-integrity.md#acceptance-evidence) | [Limits and compatibility](../code01-integrity.md#operational-and-compatibility-limits) | Maven commands and packaged smoke outcome in the [evidence record](../code01-integrity.md); fixed-revision rerun pending |
| CODE-02 asynchronous process harness | [Method and commands](code02-method.md) | [Local execution record](code02-results.md) | Security and platform limits in the method | [Windows](run-code02.ps1), [POSIX](run-code02.sh) |
| CODE-03 whole-file memory preflight | [Prespecified method](code03-method.md) | [Windows result and boundary](code03-results.md), [run rows](code03-runs.csv), [file rows](code03-files.csv) | Size and platform limits in the result | [Windows runner](run-code03.ps1), [exporter](export-code03.ps1) |
| Issue #31 representative workload and baselines | [Protocol](issue31-protocol.md) | Planned under Issues #32–35 | — | [CODE-02 harness](code02-method.md) exists; fixed-revision study execution pending |
| JOSS-01 shared trust boundary | [Method](joss01-method.md) | [Results](joss01-results.md) | — | [Windows](run-joss01.ps1), [POSIX](run-joss01.sh) |
| JOSS-02 release boundary | [Method](joss02-method.md) | [Results](joss02-results.md) | [Matrix](joss02-claim-evidence.md) | [Windows](run-joss02.ps1), [POSIX](run-joss02.sh) |
| JOSS-03 reviewer workflow | [Method](joss03-method.md) | [Results](joss03-results.md) | [Matrix](joss03-claim-evidence.md) | [Windows](run-joss03.ps1), [POSIX](run-joss03.sh) |
| JOSS-04 bibliography | [Method](joss04-method.md) | [Results](joss04-results.md) | [Matrix](joss04-claim-evidence.md) | Paper build commands in method |
| JOSS-05 manuscript alignment | [Method](joss05-method.md) | [Results](joss05-results.md) | [Matrix](joss05-claim-evidence.md) | [Static check](check-joss05-paper.ps1), [Windows draft](build-joss05-windows.ps1) |

The JOSS-02, JOSS-03, and JOSS-05 `*-suite-report.csv` and `*-test-cases.csv` files are
sanitized snapshots tied to their results pages. The [JOSS-05 draft PDF](joss05-draft.pdf)
records an adapted local build. `joss03-posix-report-fixture.xml` and
`VerifyPosixCoverage.java` support the POSIX report gate. The
[JOSS-02 summary exporter](export-joss02-summary.ps1) regenerates its CSV summaries.
Keep generated secrets and raw test logs out of this directory.
The CODE-01 record describes a later local working tree. Earlier JOSS-02 and JOSS-05 results
remain dated snapshots and should not be read as findings about the CODE-01 candidate.
