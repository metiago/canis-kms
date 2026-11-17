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
exited 0 with `verified=2/2`; its [run metadata](code02-wrapper-final.json) and sanitized
[producer](code02-wrapper-final.producer.csv)/[consumer](code02-wrapper-final.consumer.csv) rows are retained.

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
category rather than a certificate-specific exception. The [first failed JSON](code02-invalid-certificate-verified.json)
and [consumer row](code02-invalid-certificate-verified.consumer.csv) record that result.
A first classifier adjustment also exited 1 for the same reason; its
[failed JSON](code02-invalid-certificate-recheck.json) and
[consumer row](code02-invalid-certificate-recheck.consumer.csv) remain. Both had zero verified
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

**CODE-02 remains open.** Two invalid-certificate validation attempts failed before the
final check passed. A fresh clean checkout containing the implementation, a POSIX execution,
and independent reproduction have not been recorded. CODE-01's exact-revision/POSIX gate and
CODE-03's largest-size memory preflight also remain prerequisites before Issues #32–35 use
these runs as study evidence.

Operational impact is limited to a new research command and temporary local processes. Risks
include the server's current wildcard bind, dependence on same-filesystem atomic rename and
Windows ACL privileges, whole-file memory allocation, and limited client error categories.
Future work for Issues #32–35 is the protocol's warm-up, baselines, repetitions, precise
component timings, peak RSS/heap sampling, outage/restart series, and analysis artifacts.
