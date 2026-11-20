# CODE-02 local implementation and execution record

## Snapshot and environment

This is a **working-tree Windows result**, not a fixed-revision or clean-checkout result.
The source HEAD during execution was `786e03e6e66e463d5b7dc996cab05a14b3bd3407` with
CODE-02 files uncommitted; every run JSON records `dirty_tree: true`. The final research
source SHA-256 was `A4CF649F29B1A23BF9B0AC8910335AEE5299369D85644D5C0CE1DEADC8C31B3D`.
The host reported Windows 11 amd64 (`10.0`), Oracle Java 21.0.7 for `java`/`javac`, and
Maven 3.9.12 using Eclipse Adoptium Java 21.0.6. The final runs used loopback clients,
a local temporary filesystem, distinct generated client certificates and credentials,
and `-Xms128m -Xmx512m` for the three Java processes. No CPU model, RAM total,
filesystem type, selected TLS suite, or calibrated peak RSS was captured; these remain
required for an Issue #32 measurement series.

## Separate source-archive build on 2026-11-17

The author reported `mvn --batch-mode --no-transfer-progress clean verify` exiting with
`BUILD SUCCESS` at `2026-11-17T20:03:21-03:00` after 5 min 36 s in a separately extracted
`canis-kms-master` directory. Read-only inspection of that directory found 27 Surefire XML
suites with 158 tests, zero failures, zero errors, and eight skips. The packaged server jar
SHA-256 was `8A076928B80B2FD26EFF3373AD9D320EF5BC0C39ACEBEF25679485639DF61EC8`.
`Code02Harness.java` SHA-256 was
`A4CF649F29B1A23BF9B0AC8910335AEE5299369D85644D5C0CE1DEADC8C31B3D`, matching
the source inspected for the local runs above. This verifies the build and executed tests for
that extracted tree. It has no `.git` directory, so its exact Git revision and dirty-tree
state cannot be established there; the harness's current revision collection also requires
Git metadata. The author reports running a build in Fedora WSL, but the Surefire XML currently
present in this archive records `os.name=Windows 11`, Oracle Java 21.0.7, and skips for both
`rejectsUnsafePosixFileAndDirectoryPermissions` and `rejectsSymbolicLink`. These files cannot
verify the reported Fedora run. The later [Ubuntu CI run](#ubuntu-ci-reproduction-on-2026-11-17)
provides POSIX evidence for the committed harness. No separate-process harness run was reported
for this extracted tree.

## GitHub Windows CI failure and local fixture correction on 2026-11-17

The author reported that the Ubuntu Actions build passed, while the Windows job failed at
`2026-11-17T22:38:50Z`. The Windows server test summary was 95 tests, one failure, 38
errors, and one skip. The listed errors were `Invalid CANIS_SECRET_KEY configuration
(unsafe_permissions)` across master-key, store, TLS, and end-to-end tests; the separate
failure was not identified in the supplied excerpt. The job's commit and complete reports
were not supplied, so this is a reported CI failure, not a fixed-revision pass.

Inspection identified a Windows test-fixture assumption: `MasterKeyTestSupport` granted
access to the file owner's principal rather than the account running the tests. The fixture
helper now assigns the current process account as owner and ACL principal, gives only that
account inheritable directory access for later synthetic TLS files, and restricts the key
file before writing generated key bytes. Production key validation was not changed. A new
Windows test provisions a key below a directory with a broad inherited ACL and verifies
that the key and its immediate directory have no broad access.

The corrected working tree passed `mvn --batch-mode --no-transfer-progress clean verify`
on local Windows 11 at `2026-11-17T20:47:47-03:00`: 28 Surefire suites, 159 tests, zero
failures, zero errors, and eight skips. This local result alone did not establish that the
GitHub Windows runner passed; the later [CI rerun](#ubuntu-ci-reproduction-on-2026-11-17)
passed on both platforms.

## Commands and observed outcomes

Executed from the repository root:

```text
mvn spotless:apply
mvn -q test
javac -cp client/target/classes -d server/target/code02-classes docs/research/Code02Harness.java
java -cp 'server/target/code02-classes;client/target/classes' Code02Harness self-test
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario normal --files 2 --size 4096 --concurrency 2 --delay-ms 1000 --output docs/research/code02-wrapper-final.json
```

Spotless exited 0 without changing production Java. The Maven test command exited 0;
Surefire XML contained 27 suites, 158 tests, zero failures, zero errors, and eight skips.
The known skips include five disabled client integration cases and platform-dependent
filesystem checks on Windows. `Code02Harness self-test` exited 0 and printed
`CODE02_SELF_TEST=PASS`. The documented Windows wrapper built the packaged server and
exited 0 with `verified=2/2`; its [run metadata (archived)](repository-cleanup-report.md#archived-files) and sanitized
[producer (archived)](repository-cleanup-report.md#archived-files)/[consumer (archived)](repository-cleanup-report.md#archived-files) rows remain in the external archive.

After the final harness changes, `javac` and the self-test again exited 0. The following
commands each launched fresh server, producer, and consumer processes with a new synthetic
temporary fixture. Each exited 0 and wrote the linked sanitized JSON and CSV records.

| Scenario and command suffix | Result | File evidence |
| --- | --- | --- |
| `--scenario normal --files 2 --size 4096 --concurrency 2 --delay-ms 1000 --output docs/research/code02-evidence-normal-corrected.json` | 2/2 authenticated byte matches; three distinct process IDs; first publication precedes consumer start | [JSON](code02-evidence-normal-corrected.json), [producer](code02-evidence-normal-corrected.producer.csv), [consumer](code02-evidence-normal-corrected.consumer.csv) |
| `--scenario tamper --output docs/research/code02-evidence-tamper-corrected.json` | 0/1 verified; `authentication_tag_rejected` | [JSON](code02-evidence-tamper-corrected.json), [producer](code02-evidence-tamper-corrected.producer.csv), [consumer](code02-evidence-tamper-corrected.consumer.csv) |
| `--scenario incomplete --output docs/research/code02-evidence-incomplete-corrected.json` | 0/1 verified; `.part` ignored, `incomplete_or_missing` | [JSON](code02-evidence-incomplete-corrected.json), [producer](code02-evidence-incomplete-corrected.producer.csv), [consumer](code02-evidence-incomplete-corrected.consumer.csv) |
| `--scenario invalid-credentials --output docs/research/code02-evidence-invalid-credentials-corrected.json` | 0/1 verified; `credentials_rejected` | [JSON](code02-evidence-invalid-credentials-corrected.json), [producer](code02-evidence-invalid-credentials-corrected.producer.csv), [consumer](code02-evidence-invalid-credentials-corrected.consumer.csv) |
| `--scenario invalid-certificate --output docs/research/code02-evidence-invalid-certificate-corrected.json` | 0/1 verified; client connection failed and a new server TLS rejection was observed | [JSON](code02-evidence-invalid-certificate-corrected.json), [producer](code02-evidence-invalid-certificate-corrected.producer.csv), [consumer](code02-evidence-invalid-certificate-corrected.consumer.csv) |

The final scenario commands used `java -cp 'server/target/code02-classes;client/target/classes' Code02Harness` before the listed suffixes.
These are categorical functional results. The numeric time and heap fields in the artifacts
are raw observations of these smoke runs, not Issue #32 latency or memory estimates.
The final normal record shows first publication at `2026-11-17T21:56:51.832818400Z` and
consumer start at `2026-11-17T21:56:52.848309100Z`; the two producer rows record planned,
actual, and publication times. An earlier successful pilot exposed a clock-origin mismatch
in the producer schedule, which was corrected before the retained final series.

SHA-256 of the final JSON records, in the table's scenario order: normal
`4646EBAC91387FA5DB1D7A0F6676001C4A875D4C3520E61C8FD32350EB307E01`, tamper
`D40A38CD3C585288CC2CC834D50174B415EE7DDC7B5A9585ACED030F1F0FD12B`, incomplete
`E17043BDC5C6C392632891B99C805CF8344057938F68380CC4A195D636A1FB8B`, invalid
credentials `AABED7D68BE273C6BEC970A487E772DBFE50B92BA1D1D316A9893120F1CFBA83`, and
invalid certificate `87063AB747CE78E59DD20981E4E25689A6B5029845EB4C39DF220B23CC12C250`.

## Failed attempts retained

The first strict invalid-certificate run exited 1 because the client reported a generic I/O
category rather than a certificate-specific exception. The [first failed JSON (archived)](repository-cleanup-report.md#archived-files)
and [consumer row (archived)](repository-cleanup-report.md#archived-files) record that result.
A first classifier adjustment also exited 1 for the same reason; its
[failed JSON (archived)](repository-cleanup-report.md#archived-files) and
[consumer row (archived)](repository-cleanup-report.md#archived-files) remain. Both had zero verified
files and no recovered output. Their private fixture/log directories were deliberately
retained under the host's `%TEMP%` as `canis-code02-18309522917517251578` and
`canis-code02-13512776243294652823`; they contain generated secret material and must not be
published. The server log for each recorded a TLS handshake or connection failure. The final
harness requires a *new* server-side TLS rejection after consumer start and records the generic
client error separately. The failed attempts are not counted as successful validation.

## Security, compatibility, and acceptance

Static inspection and the executed normal/negative runs support the local behavior: source and
recovered bytes stayed in the private run area; the shared directory contained only expected
encrypted `.canis` files or, for the incomplete case, a `.part` file; complete publication used
`ATOMIC_MOVE`; failed authentication and tampering yielded no recovered file. A result-artifact
scan found no `CANIS_SECRET_KEY`, private-key/certificate PEM delimiters, or password assignment.
Only generated, short-lived test credentials were used. The harness changes no production Java,
CANISP response, envelope format, database format, or shared-access policy. Normal startup still
uses the isolated, explicitly initialized `db.dat` in the server process's temporary working
directory. The existing TLS, storage, and file-compatibility suite passed as counted above.

| CODE-02 requirement | Repository evidence | Status |
| --- | --- | --- |
| Separate producer, delayed consumer, and CANIS server processes | Distinct PIDs and start times in final normal JSON; process launch in `Code02Harness.java` | Locally verified on Windows |
| Temporary shared directory with no plaintext or private keys | Run-root layout and path guards in source; normal and fault results; private fixture cleanup | Locally verified for executed cases |
| Atomic complete publication and ignored incomplete files | `ATOMIC_MOVE` implementation, self-test, incomplete run | Locally verified on the tested filesystem |
| Authenticated decryption before byte-equality success | Official-client consumer and 2/2 normal result; tamper and authentication fault rows | Locally verified |
| Sanitized machine-readable metadata and stable run ID | `code02-v1` JSON plus per-file CSV; artifact scan | Locally verified for required CODE-02 fields |
| Repeatable setup, teardown, and fixture cleanup | Windows wrapper and method; no normal-run `canis-code02-*` root remained | Windows local run verified; POSIX and clean checkout unverified |
| Failure injection isolated from production persistence | New temporary root and dedicated server working directory; failed fixture roots retained | Local path review and executed faults verified; hostile path-race behavior not established |

At the time of these local runs, CODE-02 remained open. Two invalid-certificate validation
attempts failed before the final check passed. Clean-checkout and POSIX reproduction were then
unverified. The later [Ubuntu CI result](#ubuntu-ci-reproduction-on-2026-11-17) closes those
CODE-02 reproduction requirements. CODE-01's fixed-revision release gate and CODE-03's
largest-size memory preflight remain prerequisites before Issues #32–35 use these runs as
study evidence.

Operational impact is limited to a new research command and temporary local processes. Risks
include the server's current wildcard bind, dependence on same-filesystem atomic rename and
Windows ACL privileges, whole-file memory allocation, and limited client error categories.
Future work for Issues #32–35 is the protocol's warm-up, baselines, repetitions, precise
component timings, peak RSS/heap sampling, outage/restart series, and analysis artifacts.

## Ubuntu CI reproduction on 2026-11-17

The [GitHub Actions run](https://github.com/metiago/canis-kms/actions/runs/44204796541)
completed successfully at commit `23a65838dc21bd02f0f4f9297dd222595cc88c97`.
Both [Ubuntu](https://github.com/metiago/canis-kms/actions/runs/44204796541/job/134467866501)
and [Windows](https://github.com/metiago/canis-kms/actions/runs/44204796541/job/134467866129)
build/test jobs passed. The Ubuntu job passed checkout, Java 21 setup, `mvn clean verify`, the
POSIX security coverage gate, `Reproduce CODE-02 asynchronous exchange`, and
`Upload CODE-02 evidence`. The reproduction step invoked the documented
`sh docs/research/run-code02.sh --scenario normal --files 1 --size 4096 --delay-ms 1000`
command, adding only `--output server/target/code02-ci.json`. The workflow writes the
wrapper's exit status and exits with that same status, so a successful step means the harness
reported a passing normal exchange and zero exit status. The harness defines normal success
as one authenticated byte match after delayed consumer startup and fixture cleanup.

The Actions artifact listing shows `code02-evidence-ubuntu-latest` (artifact ID
`14381849411`, 2368 bytes, seven-day retention). The workflow selects the wrapper console
log, exit-status text, JSON result, and producer/consumer CSV records from `server/target/`.
It does not select the private temporary fixture root or raw server/worker logs. The artifact
download endpoint returned `401 Requires authentication` in this review, so the archive's
individual files and JSON fields were not independently inspected. Job-step success and the
published artifact listing are the observed CI evidence; details of the exchange outcome
follow from the committed wrapper and harness exit behavior.

This successful Ubuntu execution is both a clean-checkout and POSIX CODE-02 reproduction.
The earlier Windows negative runs remain the evidence for tamper, incomplete publication,
invalid credentials, and invalid certificates. This CI smoke run does not establish a
production release, the Issue #32 measured workload, or CODE-03's memory bound.
