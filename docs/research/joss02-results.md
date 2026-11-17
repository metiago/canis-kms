# JOSS-02 working-tree verification results

**Later CODE-01 update:** This report records an earlier working tree. The [CODE-01 local
candidate evidence](../code01-integrity.md) records subsequent master-key, database-write, and
stored-key validation changes with tests. Those changes have since been committed, but the exact
revision has not been rerun as a clean research release. The observations below remain historical
and the JOSS-02 release gate remains open.

## Snapshot and commands

This is verification of a **dirty local working tree**, not a tagged or public candidate release.
The Git HEAD reported by both scripts was
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81`. The research test and the JOSS documents
were untracked or modified. The exact executed commands from the repository root on 2026-11-16
were:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss02.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-joss02-summary.ps1
```

The first full-suite attempt inside the command sandbox failed because Mockito's inline Byte
Buddy mock maker could not attach its Java agent. That attempt was interrupted after the
environmental failure; its reports are **not** counted below. The same JOSS-02 command was
rerun outside the sandbox and exited 0. The focused JOSS-01 command was also run outside the
sandbox and exited 0. These executions used Microsoft Windows 11 amd64, Maven 3.9.12, and
Eclipse Adoptium Java 21.0.6 as reported by Maven. The script's operating-system API printed
`Microsoft Windows 10.0.26100`; Maven identified the same host as Windows 11. Both commands
used loopback sockets and generated local test fixtures.

## Retained test outcome artifacts

The local Surefire XML reports under each module's `target/surefire-reports/` were parsed after
the successful full run and focused rerun. The sanitized [suite report](joss02-suite-report.csv)
retains each suite's test, failure, error, and skip counts. The sanitized
[test-case report](joss02-test-cases.csv) retains each case name and outcome without stack
traces, system properties, logs, paths, credentials, or generated key material. The
`export-joss02-summary.ps1` command exited 0 and reported 24 suites and 149 test cases.

| Module | Suites | Tests | Passed | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| client | 9 | 59 | 54 | 0 | 0 | 5 |
| server | 15 | 90 | 88 | 0 | 0 | 2 |
| total | 24 | 149 | 142 | 0 | 0 | 7 |

Relevant suite counts in those artifacts: `SocketClientTest` 14/14 passed;
`ServerEndToEndTest` 4/4 passed; `ServerTlsSecurityTest` 7/7 passed;
`ServerTlsCertificateTest` 3/3 passed; `KeyValueStoreTest` 12/12 passed; and the focused
`SharedTrustDomainResearchTest` 1/1 passed. The focused run overwrote only its own report
after the full run. The five skipped client cases belong to the explicitly disabled
`JpawIntegrationTest`, which requires a separately started server. The two skipped server
cases are platform-dependent master-key permission and symlink checks on this Windows host.
The passing `ServerEndToEndTest` uses the official Java client against an in-process TLS server,
compares recovered synthetic bytes with input, and includes restart and database-loss cases.
The focused shared-trust scenario again observed recipient and observer decryption with valid
credentials and rejection of an altered observer password. It does not test TLS certificate
identity separation because all three credential identities use one temporary client certificate.

The SHA-256 of the untracked `SharedTrustDomainResearchTest.java` at execution was
`71FB34494FEC157CA15EA591278AA7421AD455619DDA1E36560A238F1BBB622C`. The JOSS-02
PowerShell runner SHA-256 was
`720BF4FEB86F4FD024F0971EC1A1EE2827AB07BC8E94BB8775864B4ACB1EA500`. The tracked
source hashes were `Server.java`:
`5AB36AC2EDEA0BF3D1BCDC06094CB144F51755DFCF8D6D1449CF9A577679B0F1`,
`KeyValueStore.java`:
`1EE51F0510D9D22CD47C1D5E88CC0CBD0D817C8AE8578884B5017CAC1C73ABC9`, and
`SocketClient.java`:
`5210ACD768ECEA8F2F733FF49443688182B128C59418EEFFEC25DA18B1744749`.
The report CSV files are evidence of test outcomes, not a complete archive of the test
process or a substitute for rerunning a fixed release.

## Claim and limitation assessment

**Observation:** The test suite includes positive official-client TLS file exchange and
negative transport, authentication, startup, and tampering cases, and all executed cases in
this run passed. **Evidence:** The two sanitized reports above, source tests, and the exact
commands in [joss02-method.md](joss02-method.md). **Interpretation:** The current working tree
supports a narrow claim that those tested behaviors worked on this Windows/JDK environment.
**Limitation:** This run does not certify a future tag, a clean checkout, POSIX permissions, or
the absence of untested security faults.

**Observation:** The public `Server(Environment, KeyValueStore)` constructor now calls
`store.verifyConfiguration`, which checks the key path, file content, and store health.
`ServerTest` rejects construction after the configured master-key file is removed.
**Evidence:** `Server.java`, `KeyValueStore.java`, and `ServerTest.java` at the recorded HEAD.
**Interpretation:** The Issue #7 backlog description of an entirely unchecked public
constructor is stale. **Limitation:** The existing test does not exhaust every injected-store
construction and path replacement case; #7 should not be closed solely on this finding.

**Observation:** `saveToFile` checks database health before `Files.move` with
`REPLACE_EXISTING`, and loaded `CANISDB1` records are not validated as matching RSA key pairs.
The decrypt handler has no per-key authorization check. **Evidence:** `KeyValueStore.java`,
`ClientHandler.java`, and [production Issues #8 and #36](../../app-prod-grade-backlog.md).
**Interpretation:** The paper must describe normal startup rejection and the current shared
trust model without claiming complete prevention of silent write-time recreation, robust
stored-key validation, or per-service isolation. **Limitation:** This JOSS-02 run did not
reproduce a deterministic check-to-move race or malformed-record exposure test. Those remain
production issue work, not completed JOSS-02 experiments.

**Observation:** `AuditLogger` emits operational key-value messages but no required event ID
or integrity verifier. **Evidence:** `server/src/main/java/io/canis/audit/AuditLogger.java`
and `AGENTS.md`. **Interpretation:** It does not meet the project's structured,
integrity-verifiable audit requirement. **Limitation:** The JOSS-02 suite does not convert
these logs into a verifiable audit trail.

## Citation and publication boundary

The cited source identities were checked against the primary
[NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final),
[RFC 8017](https://www.rfc-editor.org/info/rfc8017/),
[Google Tink KMS Envelope AEAD](https://developers.google.com/tink/client-side-encryption),
and [Vault Transit envelope encryption](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption)
pages on 2026-11-16. This confirms the referenced standards and comparison topics; it does not
create the absent `paper.bib`, resolve citation keys, or build the JOSS paper.

JOSS-02 acceptance remains open: no fixed candidate revision containing the manuscript and
research artifacts was tested, no independent reproduction or POSIX run was recorded, and the
Issue #8 and #36 findings remain unresolved. **Release decision for this review:** do not
nominate this dirty working tree as the research release. The known database write race,
stored-key validation and private-key exposure concern, and project audit gap prevent
unqualified key-protection or recovery claims. A later candidate needs either fixes with
security and compatibility tests or an author-approved, explicit release scope consistent
with `AGENTS.md`. No
performance, security-strength, reliability, scalability, or research-adoption measurement was
made. The author-supplied motivating workload and research-use claims remain unverified.

## Backlog traceability

| Backlog Task | Evidence | Status |
| --- | --- | --- |
| JOSS-02 client TLS verification | [`run-joss02.ps1`](run-joss02.ps1), [`run-joss02.sh`](run-joss02.sh), [`export-joss02-summary.ps1`](export-joss02-summary.ps1), [method](joss02-method.md), [suite report](joss02-suite-report.csv), [test-case report](joss02-test-cases.csv), `client/src/test/java/io/canis/jpaw/client/SocketClientTest.java`, `server/src/test/java/io/canis/ServerTlsSecurityTest.java` | Working-tree verification passed; fixed candidate release pending. |
| JOSS-02 file workflow and negative security tests | `server/src/test/java/io/canis/ServerEndToEndTest.java`, `client/src/test/java/io/canis/jpaw/client/SocketClientTest.java`, `server/src/test/java/io/canis/ServerTlsSecurityTest.java`, `server/src/test/java/io/canis/ServerTlsCertificateTest.java`, and the sanitized reports above | Windows working-tree run passed; POSIX and fixed-revision runs pending. |
| JOSS-02 issue #7/#8/#36 release implications | [claim matrix](joss02-claim-evidence.md), this result, and the limited claim wording in `joss-papers/paper.md` | Findings documented; production issues and final release decision open. |
| JOSS-02 manuscript claim audit | [claim matrix](joss02-claim-evidence.md) was created before the `joss-papers/paper.md` edit; this result records the observed run | Current-tree claims reviewed; fixed-release cross-check pending. |
| JOSS-02 environment, limitations, and checklist status | This result, `joss-backlog.md`, and `joss-papers/submission-readiness.md` | Working-tree environment recorded; candidate commit and acceptance open. |
