# JOSS-02 fixed-revision validation and smoke record

## Environment and revision

| Field | Recorded value |
| --- | --- |
| Candidate commit | `cd1ebeb32bbd0b6b2ebd42a29f72e3dac8b01694` |
| Branch | `master`; working tree clean before validation and each smoke run |
| OS | Windows 11, NT `10.0.26100.0`, amd64 (`mvn --version` reports Windows 11, version `10.0`) |
| Maven | Apache Maven 3.9.12 |
| Maven JDK used for the full suite | Eclipse Adoptium Java 21.0.6 |
| Shell `java -version` used by research wrappers | Oracle Java 21.0.7 |
| Validation date | 2026-11-18 local time, America/Sao_Paulo; some smoke JSON timestamps cross into 2026-11-19 UTC |

The initial sandboxed `mvn clean verify` was stopped after Mockito's inline Byte Buddy agent could not attach to the test JVM. The same command was rerun with normal host permissions and **passed**; the sandbox error is an environment failure, not a passing or failing software test. No source or manuscript file changed during the successful run or four smoke executions.

## Fixed-revision build and tests

Command: `mvn clean verify` from the repository root. The permitted run exited 0 with `BUILD SUCCESS` at 2026-11-18 20:55:49 -0300. The reactor's client and server modules passed. Surefire XML under `client/target/surefire-reports/` and `server/target/surefire-reports/` contains 28 suites, **159 tests, zero failures, zero errors, eight skips** (60 client tests with five skips; 99 server tests with three skips). The `SharedTrustDomainResearchTest` report contains one pass and no skip. The smoke wrappers subsequently package with `-DskipTests`; those wrapper builds are not counted as additional full-suite runs.

| Validation Step | Result |
| --- | --- |
| Fixed source and manuscript | Clean `master` HEAD matched the candidate commit; the three harness JSONs record that same revision and `dirty_tree: false`. |
| Maven clean, Spotless check, compilation, packaging | Passed in `mvn clean verify`; server packaged jar built. |
| Client TLS 1.3, certificate/hostname validation, plaintext/TLS 1.2 rejection | Client `SocketClientTest` 14/14 passed; `TlsCipherPolicyTest` 4/4 passed. See [client tests](../../client/src/test/java/io/canis/jpaw/client/SocketClientTest.java). |
| Server mTLS, credential admission, plaintext/TLS 1.2 rejection, tampering | `ServerTlsSecurityTest` 7/7 and `ServerTlsCertificateTest` 3/3 passed. See [server TLS tests](../../server/src/test/java/io/canis/ServerTlsSecurityTest.java). |
| Official file workflow and valid persistence/restart | `ServerEndToEndTest` 4/4 passed; the client file test and server storage tests also passed. See [end-to-end tests](../../server/src/test/java/io/canis/ServerEndToEndTest.java). |
| Missing/changed database, malformed records, replacement safety | `KeyValueStoreTest` and `DatabaseReplacementTest` passed their executed cases; one store test is POSIX-only and skipped here. See [CODE-01 acceptance](../code01-integrity.md#acceptance-evidence). |
| Full test totals | 159 tests, 0 failures, 0 errors, 8 skips; `BUILD SUCCESS`. |
| Skip explanation | Five `JpawIntegrationTest` cases need a separately started server; three permission/symlink cases require POSIX. They were not treated as passes on this Windows candidate. |
| Retained historical artifact checks | Issue #32 verifier: `passed pairs=24 file_rows=4160`; Issue #33 verifier: `passed scenarios=5`; Issue #34 verifier: `passed cases=7 repetitions=3 artifacts=159`. CODE-03 and Issue #31 published CSV SHA-256 values matched their result records. Read-only CSV checks found eight measured CODE-03 attempts and 20 measured Issue #31 runs with 2,000 verified files and 2,000 measured file rows. These were not workload reruns. |

A read-only check of the five measured Issue #32 `0 ms / 1 worker` rows in `analysis-pairs.csv` gave a paired latency median of `13.333500000000015` ms and a range of `-60.83349999999999` to `28.474500000000035` ms; formatting those stored values to three decimals matches the paper's `+13.334`, `−60.833`, and `+28.475` ms. This recalculates existing evidence and creates no new workload measurement.

## Minimal smoke reproductions

Each runner used generated, restricted temporary fixtures and the official TLS client. The categorical outcomes below are release-validation evidence only; automatically emitted timing and memory fields are not analyzed as research measurements. The raw sanitized JSON/CSV and Surefire XML are local, ignored `target/` output. The commands and categorical outcomes here are the durable record; a later `mvn clean` removes the raw local output. A marker scan across 17 generated smoke JSON/CSV files found no `CANIS_SECRET_KEY`, `PRIVATE KEY`, `.p12`, `password=`, or private run-root marker.

| Scenario | Result | Evidence |
| --- | --- | --- |
| A. JOSS-01 shared trust | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1` exited 0. One focused test passed: recipient and observer byte-matched; altered observer password rejected. Shared test certificate limits interpretation to credential-level access. | Local `server/target/surefire-reports/TEST-io.canis.SharedTrustDomainResearchTest.xml`; [test and method](joss01-method.md#reproduction). |
| B. CODE-02 normal | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario normal --files 1 --size 4096 --delay-ms 1000 --output server/target/joss02-code02-normal.json` exited 0: `CODE02_RESULT=passed scenario=normal verified=1/1`; JSON says candidate commit and `dirty_tree: false`. | Local `server/target/joss02-code02-normal.json` and sibling producer/consumer CSVs; [method](code02-method.md#build-and-run). |
| C. Issue #33 scenario D | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario D --output server/target/joss02-issue33-D.json` exited 0: one publication, two verified recoveries, two rejected attempts. CSVs classify bad password as `credentials_rejected` and untrusted certificate as `tls_certificate_rejected`. | Local `server/target/joss02-issue33-D.json` and sibling event/worker CSVs; [method](issue33-method.md#reproduction-output-and-cleanup). |
| D. Issue #34 scenario E | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario E --repetition 1 --output server/target/issue34/joss02-E-r1.json` exited 0: baseline verified; tampered database restart exited 1 and did not become ready; no faulted output; exact backup restore verified. | Local `server/target/issue34/joss02-E-r1.json` and sibling event/worker CSVs; [method](issue34-method.md#reproduction-and-cleanup). |

One preparatory Issue #34 command used `--output server/target/joss02-issue34-E.json`, outside the harness-required `server/target/issue34/` directory. The harness rejected that path before a fixture or result was created (`ISSUE34_FAILURE=io_or_authentication_failure`); the corrected command above is the one executed scenario. This command error is not counted as a fault-run outcome.

The local SHA-256 digests of the principal ignored evidence files were `0E8AB9532AEE88869A500D32DF7819AE6A99ED110CD0B09EBB90E7BABAC2B8A1` (JOSS-01 Surefire XML), `815C1E58542BB72EF914664AC92035D1C30731EE372550B50FAF364E0FAAA2D4` (CODE-02 JSON), `E8E28F88CF8B8C1F50B881DBC574A3029109D24692B7157D7293ADC8CD2BF098` (Issue #33 D JSON), and `52F3A21CE64776B9A0C03094028AF6CB0E8649A89FEA13AB91ADF8E932A3FED0` (Issue #34 E JSON). The digest is a local evidence identifier, not a claim that the ignored raw output is part of the Git candidate.

The smokes confirm only these selected workflows on this candidate. They do not repeat the CODE-03 or Issue #31/#32 campaigns, the other Issue #33/#34 cases, or any availability-window measurement.
