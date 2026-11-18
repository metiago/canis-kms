# Issue #34 controlled fault-injection method

## Scope and prespecification

This method was written before the Issue #34 experiments. It studies observed behavior of the
current CANIS build under synthetic storage and audit faults. It does not evaluate penetration
resistance, cryptographic strength, compliance, availability guarantees, or production readiness.
The approved design is the preceding Issue #34 plan. Only research files are added:
`Issue34Harness.java`, the `run-issue34` wrappers, this method, the result record, and sanitized
artifacts. Production Java, TLS requirements, algorithms, CANISP, database and envelope formats,
and shared-trust policy are unchanged.

## Environment and fixture controls

Run from the repository root with Git, JDK 21+ including `keytool`, Maven 3.9.x, Python 3 for
artifact verification, a free local port,
and a filesystem supporting restrictive file access and atomic moves. Every invocation creates a
new restricted `canis-issue34-*` root below the OS temporary directory with `private/`, `server/`,
`shared/`, and `worker/`. The server runs from `server/`; its only database is `server/db.dat`.
The harness creates a random 128-bit AES master key, short-lived synthetic PKCS#12 identities,
random credential passwords, and deterministic synthetic payloads. The baseline envelope is 4 KiB;
the later ordinal-1 publication attempt is 256 KiB. A producer publishes a
`CANISHYB1` envelope via the official TLS 1.3 client. The recipient decrypts and checks byte
equality. Only the encrypted envelope enters `shared/`.

Before any fault, each invocation must explicitly initialize the isolated database, start a
separate packaged server, create `recipient-a`, publish one envelope, recover it, and make private
backup copies of the encrypted database and matching master key. A failed baseline stops the run.
The coordinator uses the Issue #33 worker for producer and recipient operations. The worker deletes
a failed recovered output. The coordinator records output presence independently. Raw server and
worker logs, plaintext, keys, TLS stores, and backups remain private; published evidence contains
only categorical log markers, hashes, process exit states, and operation outcomes.

The experiment must run with host network controls allowing only local loopback clients because
the current server binds available interfaces. No external service participates. Do not weaken
filesystem permissions to overcome an environment error. The temporary-root marker and containment
checks guard successful cleanup; a failed root is retained for private inspection.

## Matrix and expected checks

Run each listed case three times with fresh fixtures; never reuse a passing result path.

| Case | Fault | Workflow and prespecified check |
| --- | --- | --- |
| A | Remove the master-key file while the server is running. | Attempt live encryption and decryption, then stop and restart. Record whether the cached key permits live work. Restart must not serve the old database without the key. Restore the original key and verify recovery. |
| B | Replace the master-key file with a different generated valid key while running. | Attempt live work, then restart. Record live behavior. Restart must not serve the original database with the replacement key. Restore the matching key and verify recovery. |
| C/live | Flip the final byte of the encrypted database while running. | Attempt a new public-key fetch and recovery; record the first observed refusal. Restart must reject the damaged database. Restore the original database and verify recovery. |
| C/stopped | Stop the server, then flip the final database byte. | Attempt restart, public-key fetch, and recovery. Restore the original database and verify recovery. |
| D/modified | Stop the server and change one fixture `event=decrypt_success` audit line to `event=decrypt_failure`. | Compare before/after file SHA-256 externally, restart, and attempt encryption and decryption. Record whether CANIS itself reports the change; no audit verifier is assumed. |
| D/deleted | Stop the server and delete one fixture `event=decrypt_success` audit line. | Apply the same independent hash, restart, and workflow checks. |
| E | Stop the server and flip the final encrypted-database byte. | Confirm rejection of the damaged state, restore the exact private database backup and matching key, restart without `--init-db`, and byte-verify the original envelope. |

Every worker CSV row reports `published`, `verified`, or `failed` and a sanitized category. The
event CSV records setup, startup, encryption, decryption, fault, and recovery phases. JSON records
scenario, variant, repetition, revision, dirty-tree flag, source and jar hashes, environment,
process IDs, startup exit, output presence, database and audit hashes, and completion status.
For A/B, a live success with a cached key is an observation, not a failure. For D, an external
hash mismatch is evidence of altered bytes, not evidence of an application audit-integrity check.
No plaintext release is inferred from a failed worker alone: inspect the per-attempt output flag.

Preserve unexpected observations and failed runs. A passing harness status means its baseline,
fault guard, and restore controls passed; interpretation must use the actual JSON and CSV fields.
Do not remove failed runs or convert them into expected outcomes. Times in event CSVs are wall-clock
timestamps for ordering; no timing threshold or reliability estimate is prescribed.

## Reproduction and cleanup

First record `git rev-parse HEAD`, `git status --short`, `java -version`, and `mvn --version`, and
run `mvn --batch-mode --no-transfer-progress clean verify`. On Windows, use:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario A --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario B --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario C --variant live --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario C --variant stopped --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario D --variant modified --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario D --variant deleted --repetition 1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34.ps1 --scenario E --repetition 1
```

Repeat the same matrix with `--repetition 2` and `3`. On POSIX, substitute
`sh docs/research/run-issue34.sh`. Each wrapper packages server/client with tests skipped only for
that invocation, compiles the research coordinator and existing fixture helpers under
`server/target/issue34-classes`, and runs one case. The separate initial `clean verify` is the
test gate. Output defaults to new `server/target/issue34/<case>-r<n>.json` and sibling CSVs.
The harness refuses an existing JSON path. A passing invocation prints
`ISSUE34_RESULT=passed scenario=<letter> variant=<variant> repetition=<n>` and deletes its private
root. A failed invocation prints `ISSUE34_PRIVATE_EVIDENCE=<path>`; verify its marker and resolved
path below the OS temporary directory before manual removal. Never publish private roots or raw
logs. After copying sanitized records, `mvn clean` removes generated build output.

For published artifacts, use a SHA-256 manifest and a verifier to check the seven case/repetition
matrix, JSON/CSV consistency, no-output assertions, hash comparisons, and absence of selected
secret markers. New attempts have different keys, certificates, PIDs, timestamps, and database
ciphertexts; compare categorical outcomes and invariants, not artifact byte equality.

The Windows campaign runner `run-issue34-campaign.ps1` builds and compiles once, then runs the
seven cases three times each into new `server/target/issue34/issue34-run-*.json` paths. It stops on
the first nonzero run. The single-case wrappers remain available for reproduction.

In `issue34-v1` JSON, `restored_and_verified` means restored fixture and verified envelope for
A/B/C/E. For D, where the altered audit line is deliberately **not** restored, it means the
post-fault file workflow was verified. Use the audit hashes and event rows to identify D's state.
