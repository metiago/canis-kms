# JOSS-03 working-tree results and traceability

## Snapshot and execution

Observed on 2026-11-16 at Git HEAD
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81` plus uncommitted and
untracked work. This is not a tagged or public candidate release. The host
was Windows 11 amd64. Maven 3.9.12 used Eclipse Adoptium JDK 21.0.6;
`java -version` separately reported Oracle JDK 21.0.7. Maven's JDK is the
relevant runtime for these tests.
SHA-256 of the new test source at execution was
`CDAEE38B55BBDC8304AD712C5D578A7211BB03ED1CED0D78862C79B8A00E4BA3`;
the PowerShell runner was
`FCCDAF83F4B53FEB1763AEFDB3B8492CFA2DF46503D38F8C0B620FE21C9AC7EF`.
The manuscript after the narrow JOSS-03 edit was
`E454412A678CBEF0F711649584A38CD62C371A9AFDAFAA7F201C175C70B41A31`.

Executed from the repository root:

```powershell
mvn spotless:apply
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss03.ps1
mvn -q test
mvn -q -DskipTests package
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/check-joss05-paper.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-joss02-summary.ps1 -OutputPrefix joss03
java docs/research/VerifyPosixCoverage.java server/target/surefire-reports/TEST-io.canis.crypto.MasterKeyLoaderTest.xml
java docs/research/VerifyPosixCoverage.java docs/research/joss03-posix-report-fixture.xml
```

The two `VerifyPosixCoverage.java` commands preserve the paths used for this
historical run. For current checks, use the paths in the [method](joss03-method.md).

Spotless exited 0 and formatted the new Java test. The focused demo's first
attempt in a restricted filesystem sandbox failed before server startup when
Windows denied setting a restrictive ACL on the temporary master-key
directory. The identical command was rerun with the necessary filesystem
permission and exited 0. That first failure is an environment limitation, not
a passing application test. The successful run printed both expected markers
from [the method](joss03-method.md). Its Surefire XML reports one test, zero
failures, zero errors, and zero skips.

The subsequent `mvn -q test` exited 0. A read-only aggregation of the two
modules' `TEST-*.xml` reports found 25 suites, 150 tests, zero failures,
zero errors, and seven skips: 143 passed. The focused `ReviewerWorkflowTest`
had 1/1 pass; `ServerEndToEndTest` 4/4;
`SharedTrustDomainResearchTest` 1/1; and client `SocketClientTest` 14/14.
`MasterKeyLoaderTest` had six cases with two skips on this Windows host.
The five other skips are the disabled `JpawIntegrationTest` cases, which
require a separately started server. The raw XML is local build output and
is not committed because it can contain logs, paths, and system properties.
The generalized exporter exited 0 and retained a sanitized
[suite report](joss03-suite-report.csv) and [test-case report](joss03-test-cases.csv)
with 25 suites and 150 case outcomes. These contain names and outcomes only.
The package command exited 0. The static manuscript check exited 0 with
approximately 1,310 body words, eight expected headings, four citation keys
matching four bibliography entries, and author/affiliation placeholders still
present. That check does not compile the paper. Git Bash `bash -n` also exited
0 for `docs/research/run-joss03.sh`; the POSIX runner itself was not executed.
The JDK-only POSIX report gate was checked against the actual Windows
`MasterKeyLoaderTest` XML and exited 1 because the expected POSIX permission
case was skipped. It exited 0 on the synthetic
[`joss03-posix-report-fixture.xml`](../../tests/fixtures/ci/joss03-posix-report-fixture.xml). These
gate checks validate the report parser; they are not POSIX application tests.

## Observations and limits

**Observation:** The focused test created temporary TLS and database fixtures,
the producer wrote a `CANISHYB1` envelope into a shared directory, and the
recipient recovered exactly the synthetic input bytes. A new recipient
connection failed after server shutdown. **Evidence:**
`server/src/test/java/io/canis/ReviewerWorkflowTest.java`, the focused
runner's exit code 0, and its one-test Surefire XML and expected output
markers; see the sanitized [test-case report](joss03-test-cases.csv).
**Interpretation:** This working tree supports a reproducible,
synthetic official-client file workflow and a narrow stopped-service failure
observation. **Limitation:** The server runs in a test thread, not as a
separate production process; all client credential identities share one TLS
certificate, and this does not measure outage duration or reliability.

**Observation:** The full Windows test run passed the executed cases, with
seven skips. **Evidence:** `mvn -q test` exit code 0 and the retained sanitized
[suite](joss03-suite-report.csv) and [case](joss03-test-cases.csv) reports.
**Interpretation:** The checked
working tree passed the available Windows unit and integration tests.
**Limitation:** This does not verify a clean clone, a fixed release, POSIX
permissions, or CI execution. The current CI matrix targets Ubuntu and
Windows, but no new GitHub Actions run has occurred for these uncommitted
changes. The local WSL `podman-machine-default` environment lacked `java`,
so POSIX tests were not run here.

**Observation:** The repository has an MIT `LICENSE.txt`, a public GitHub
repository page, and new contribution/security guidance in this working
tree. **Evidence:** `LICENSE.txt`, `CONTRIBUTING.md`, `SECURITY.md`, and
the public repository page reviewed on 2026-11-16. **Interpretation:** Basic
open-source artifacts are available locally. **Limitation:** The public page
showed older README content; this JOSS-03 work is not published. Public issue
creation, private vulnerability reporting, support response, and six months
of public active development were not established by this review. A stable
private security-reporting route needs maintainer confirmation.

**Observation:** Four existing manuscript citations remain unchanged.
**Evidence:** `joss-papers/paper.bib`, the JOSS-04 primary-source table, and
fresh inspection of the [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final),
[RFC 8017](https://www.rfc-editor.org/info/rfc8017/),
[Google Tink](https://developers.google.com/tink/client-side-encryption),
and [HashiCorp Vault](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption)
publisher pages. **Interpretation:** The unchanged bibliography identifies
the cited standards and related tools. **Limitation:** No canonical JOSS
paper build or new comparative experiment was run for JOSS-03.

## Backlog traceability

| Backlog Task | Evidence | Status |
| --- | --- | --- |
| Clean-machine build and external provisioning | `docs/reviewer-guide.md`; `docs/master-key.md`; `README.md`; JOSS-03 scripts | Partial: commands and generated test fixtures exist; clean non-author checkout and operational TLS provisioning remain unverified. |
| Producer to shared file to recipient demonstration | `ReviewerWorkflowTest.java`; `run-joss03.ps1`; `run-joss03.sh`; `joss03-method.md`; focused output and Surefire result | Completed for the synthetic in-process reviewer demo on Windows; separate-process deployment remains unverified. |
| CI unit, integration, and POSIX checks | `.github/workflows/ci.yml`; `VerifyPosixCoverage.java`; `joss03-posix-report-fixture.xml`; `joss03-suite-report.csv`; `joss03-test-cases.csv`; `MasterKeyLoaderTest.java` | Partial: Windows suite passed and the gate detects skips; updated matrix and POSIX run are unverified. |
| Architecture, threat model, outage, memory and feature boundaries | `docs/reviewer-guide.md`; `docs/threat-model.md`; `paper.md`; `EnvelopeCryptographer.java` | Documented with one narrow outage check; no numeric capacity or reliability result. |
| License, issue, contribution, support/security paths, history | `LICENSE.txt`; `CONTRIBUTING.md`; `SECURITY.md`; public repository page | Partial: public workflow, private reporting route, and public-history gate not fully verified. |

The JOSS-03 acceptance criterion remains open until a non-author follows the
published README on a fixed revision and reproduces the workflow and tests.
No research adoption, comparative advantage, performance metric, or
production security-strength conclusion follows from this result.

## Independent non-author review on 2026-11-19

This section records a non-author execution in a clean, separate Windows
checkout at commit
`e302964d6ed9208be43849191da9c8f2f302c76b`. The path supplied to the
reviewer used `canis-reviewer`, while the existing checkout directory was
`canis-reviwer`; the reviewer recorded the mismatch and used the existing
checkout. `git status --short` was empty before and after execution. Java 21,
`javac`, `keytool`, and Maven 3.9.12 were available.

The documented `mvn --batch-mode --no-transfer-progress clean verify` exited 0
with `BUILD SUCCESS`: client 60 tests, zero failures/errors, five skips; server
99 tests, zero failures/errors, three skips. The reviewer ran the PowerShell
JOSS-03 demo separately; it exited 0 and printed both the byte-match and
post-stop connection-rejection markers. Its Surefire XML reported one test,
zero failures/errors/skips.

Following the guide's linked [CODE-02 method](code02-method.md), the reviewer
ran the Windows normal wrapper with one 4096-byte file and a 1000 ms delay.
It exited 0, reported `CODE02_RESULT=passed scenario=normal verified=1/1`,
and produced sanitized JSON and producer/consumer CSV records. The JSON
recorded `outcome=passed`, `verified_files=1`, and distinct server, producer,
and consumer process IDs (28380, 10992, and 22532). The producer CSV recorded
`published`; the consumer CSV recorded `verified,none`. The two documented
negative wrappers also exited 0. Their JSON records reported `outcome=passed`
and `verified_files=0`; the consumer CSVs recorded failure categories
`credentials_rejected` and `io_or_authentication_failure`, respectively.
The observed result files were named
`code02-2026-11-19T16-37-30.084291500Z-b1772c25.json` (normal),
`code02-2026-11-19T16-38-19.777096900Z-ec15cadc.json` (invalid credentials),
and `code02-2026-11-19T16-38-59.533428400Z-b426fe1b.json` (invalid
certificate), each under `server/target/` in the review checkout. Their
producer and consumer CSVs shared the same basenames. The normal JSON recorded
`dirty_tree=false` and the reviewed commit; both negative JSON records did too.

The reviewer inspected `.github/workflows/ci.yml` and checked the public
[GitHub Actions run](https://github.com/metiago/canis-kms/actions/runs/44434664908)
for the reviewed commit. The [Windows job](https://github.com/metiago/canis-kms/actions/runs/44434664908/job/135260393886)
and [Ubuntu job](https://github.com/metiago/canis-kms/actions/runs/44434664908/job/135260394233)
both had `completed/success` conclusions. The Ubuntu workflow requires the
POSIX permission/symlink coverage command and CODE-02 normal wrapper; the
successful job supports those gates, although the reviewer did not run the
POSIX command locally or download CI logs and artifacts.

**Acceptance review:** The non-author completed the guide's clean-checkout
tests and separate-process synthetic proof of concept without an undocumented
tutorial command. This supersedes the historical working-tree-only conclusion
above for the reviewer workflow. The review did not execute a long-running
server with operator-provisioned TLS and master-key files, verify a stable
private vulnerability-reporting route, or establish six months of active
public development. The reviewed commit is later than the tagged
`v0.1.0-rc.1` candidate, so it is not independent reproduction of that exact
tag. JOSS-03 remains open on those checklist gaps; the JOSS-07 and broader
submission gates remain separate.
