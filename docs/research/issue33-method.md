# Issue #33 multi-service interoperability method

## Scope, prior plan, and implementation boundary

This is the functional Issue #33 supplement to the [Issue #31 protocol](issue31-protocol.md). Its study design was approved in the preceding review before these executions. The Issue #31 measured baseline remains the one-producer, one-recipient workload; these functional scenarios are not pooled with its measurements. The [CODE-03 preflight](code03-results.md) had already verified 4 KiB and 256 KiB files with four consumer workers and 512 MiB child heaps on the Windows host. Issue #33 uses only those two file sizes, one worker per consumer process, and the same 512 MiB child heap limit. It does not introduce streaming or measure throughput, scalability, or availability.

The affected files are research-only: `Issue33Harness.java`, the two `run-issue33` wrappers, `verify-issue33.py`, this method, the result record, and sanitized artifacts. `Code02Harness.java` exposes package-local fixture helpers to the new coordinator and accepts a list of trusted test identities. Its existing CODE-02 fixture set remains its default. No production Java, CANISP message, `CANISHYB1` envelope, `CANISDB1` database, TLS policy, or shared-trust policy changes.

The security implications are temporary certificate stores, generated credential passwords, a generated master-key file, source and recovered synthetic bytes, and an encrypted test database. The coordinator creates a fresh run root in the OS temporary directory with restricted `private/`, `server/`, `shared/`, and `worker/` subdirectories. It refuses an existing output path. The server is launched from the run's `server/` directory, explicitly initializes only its fresh `db.dat`, and never points at an external database. The shared directory receives only `CANISHYB1` envelopes and, in A, an encrypted `.part` decoy. Passwords travel through child environment variables, not command arguments or published records. Private fixture roots are removed after a passing run only after marker and temp-root containment checks. A failing run retains its private root for inspection.

## Fixture and process design

Use the official `JpawClient` in separate Java producer and consumer processes. The coordinator launches the packaged CANIS server as another process over loopback. The server uses TLS 1.3 and requires a trusted client certificate; each client validates the `localhost` server certificate and hostname. All test PKCS#12 stores and credentials are freshly generated per scenario using JDK `keytool`; an extra `untrusted` certificate is excluded from the server truststore. The coordinator records SHA-256 fingerprints of public certificates, worker PIDs, and server PIDs, never private keys or passwords.

| Identity | Function | Key name |
| --- | --- | --- |
| `producer-a` | Producer P1 | Fetches `recipient-a` public key |
| `producer-b` | Producer P2 in B | Fetches `recipient-b` public key |
| `recipient-a` | Consumer R1 | Creates and recovers `recipient-a` |
| `recipient-b` | Consumer R2 in B | Creates and recovers `recipient-b` |
| `observer` | Additional admitted consumer in D | Recovers `recipient-a` under shared trust |
| `untrusted` | Rejection fixture in D | Certificate absent from server truststore |

Payload bytes follow the Issue #31 SHA-256 counter stream: concatenate `SHA-256(ASCII("CANIS-I31-v1") || uint32be(1) || uint32be(fileOrdinal) || uint64be(blockIndex))` and truncate to the file size. Even ordinals are 4,096 bytes; odd ordinals are 262,144 bytes. The producer creates source bytes in `private/`, asks CANIS for the named public key, writes a `CANISHYB1` envelope to `shared/<run-id>-<ordinal>.part`, checks its magic header, and publishes by same-directory `ATOMIC_MOVE` to `.canis`. Consumers open only the expected complete `.canis` names, call `JpawClient.decryptFile`, and compare recovered bytes with independently generated expected bytes. Any failed recovery deletes its output before recording a sanitized category.

The requested delay is 60,000 ms from the first complete publication for A and C, and 1,000 ms for B, D, and E. All planned producer processes exit and all expected complete envelopes exist before a consumer starts. The coordinator records actual first-publication and consumer-start UTC times; their difference is `observed_delay_ms`. `run_duration_ms` is coordinator start through process teardown, before artifact export and fixture deletion. These are descriptive workflow intervals, not calibrated performance measurements.

## Prespecified scenarios and pass rules

| Scenario | Setup and actions | Expected observations and failure rule |
| --- | --- | --- |
| A: delayed exchange | P1 publishes two files for R1. Attempt publication of ordinal 0 again, place an encrypted `.part` decoy, wait 60 s, let R1 recover both, and replay ordinal 0 in a new consumer process. | Two unique authenticated byte matches; existing envelope hash unchanged by duplicate publication; decoy never selected; replay also byte-matches but is counted separately. Any early read, overwrite, byte mismatch, or shared-storage violation fails. |
| B: multiple routes | R1 and R2 create distinct key names. P1 and P2 run as separate processes, each publishing two files for its corresponding recipient. R1 and R2 later run as separate processes. | Four route-correct byte matches, distinct service PIDs and certificate fingerprints, and no unexpected shared file. Any route or byte mismatch fails. |
| C: server restart | P1 publishes two files, then the coordinator stops the server with no in-flight producer or consumer work. It waits for exit and starts a new server process with the same database, master key, TLS settings, and port; it does not reinitialize. R1 starts after readiness and the 60 s delay. | New server PID, unchanged encrypted database SHA-256 across restart, two byte matches. Failed restart or recovery fails. This is a controlled lifecycle check, not crash durability or outage testing. |
| D: shared trust | P1 publishes one file for `recipient-a`. R1 and then admitted `observer` recover it. An observer connection with a changed password is attempted, followed by a connection with an untrusted client certificate. | Two successful byte verifications under the same key name. The invalid password is rejected; a new server-side TLS rejection is observed for the certificate attempt. Both rejected attempts leave no recovered output. Observer success is permitted by the documented shared-access model. |
| E: tampering | P1 publishes one file for R1, which verifies it. The coordinator copies the encrypted envelope to ordinal 1 and flips the final tag byte. A new consumer attempts recovery of the altered copy. | Original control byte-matches; altered copy has `authentication_tag_rejected` and no recovered output. Any accepted alteration or failed control fails. |

`files_published` counts unique complete producer publications. `files_recovered` counts successful authenticated byte-verification operations, including the observer's second recovery in D; A's deliberate replay is recorded separately from its two unique recoveries. `files_rejected` counts failed decrypt attempts in D and E; A's duplicate publication rejection is a separate event. `byte_verification_success` means every expected positive recovery matched bytes. Rejection rows contain their own failure categories even when the overall scenario passed. The worker CSV column named `identity` is the process label. Labels `duplicate`, `replay`, `bad-password`, `untrusted`, and `tampered` identify attempt variants; the configured credential identity for each is specified by the coordinator, and the bad-password and untrusted attempts both use the `observer` username.

The envelope does not provide delivery deduplication or producer authentication. A duplicate final name is rejected by this harness before encryption; a repeated valid envelope remains decryptable. The `.part` check demonstrates the harness selects only expected final names, not a general filesystem watcher or queue implementation. In E, the tampered copy uses ordinal 1 as a distinct file name but contains an altered copy of ordinal 0's envelope; its worker row's ordinal-1 expected-input hash is not used to classify the tag rejection.

## Reproduction, output, and cleanup

From a clean checkout with Git, Java 21+ (`java`, `javac`, `keytool`), Maven 3.9.x, a free loopback port, and a filesystem supporting atomic rename and owner-only fixture permissions, run a full build/test first. Run scenarios sequentially, each into a new output name. Existing paths are refused.

```powershell
java -version
mvn --version
git rev-parse HEAD
git status --short
mvn --batch-mode --no-transfer-progress clean verify
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario A
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario B
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario C
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario D
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario E
```

On POSIX, substitute `sh docs/research/run-issue33.sh --scenario A` through `E`. Each wrapper packages the server with tests skipped for that wrapper invocation, compiles the two research classes into ignored `server/target/issue33-classes`, and runs the selected scenario. The separate initial `clean verify` command is the test gate. Each passing run prints `ISSUE33_RESULT=passed scenario=<letter> published=<n> recovered=<n> rejected=<n>` and creates a unique JSON under `server/target/issue33/` plus sibling event and worker CSVs. A nonzero exit means the scenario did not pass; inspect its sanitized JSON/CSV and private root before any retry. The private root path is printed as `ISSUE33_PRIVATE_EVIDENCE=...` on a scenario failure. A host that refuses restrictive ACLs is an environment failure; do not weaken them.

The published Windows records use `docs/research/issue33-final-<letter>.json` and sibling CSVs. Run `python docs/research/verify-issue33.py` from the repository root to cross-check them. From `docs/research/`, `sha256sum -c issue33-artifacts.sha256` verifies all published final artifacts on a POSIX host. A PowerShell reviewer can compare each manifest digest with `Get-FileHash -Algorithm SHA256`. The verifier reports `ISSUE33_EVIDENCE=passed scenarios=5` only when all five records satisfy its count, route, ordering, identity, restart, rejection, and sanitization checks. A new reproduction will have different attempt IDs, certificates, PIDs, timestamps, and randomized envelope hashes; compare outcome categories and invariants, not artifact byte equality.

Passing runs remove their private temporary fixture root. If a run fails, inspect the printed root, verify its `.code02-run` marker and that its resolved path is below the OS temporary directory, record the cause, and then remove it manually. Never publish the root or raw server/worker logs. After copying sanitized results, `mvn clean` removes generated `target/` output. No script removes an unverified path.

## Interpretation limits

These scenarios use one Windows loopback host, synthetic bytes, local temporary shared storage, and fresh short-lived credentials. They do not test distributed storage, longer retention, interrupted database writes, crash recovery, cross-language clients, external KMS products, production credentials, or external services. The client still reads whole files into memory. Audit lines can corroborate requester and key name but do not have a verifiable integrity chain. No throughput, scalability, availability, comparative security, or production-readiness conclusion follows from these runs.
