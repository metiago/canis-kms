# CODE-02 isolated exchange harness

This research-only harness exercises the official `JpawClient` and packaged CANIS server. It
does not change CANISP, `CANISHYB1`, `CANISDB1`, TLS policy, or the shared-trust access model.
The smoke runs below are functional checks, not Issue #32 performance measurements.

## Build and run

From a clean checkout with Git, JDK 21 or newer (`java`, `javac`, `keytool`), Maven 3.9.x,
an available local TCP port, and a filesystem supporting atomic rename and restrictive
permissions, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario normal --files 1 --size 4096 --delay-ms 1000
```

```sh
sh docs/research/run-code02.sh --scenario normal --files 1 --size 4096 --delay-ms 1000
```

Each wrapper packages the server, compiles `Code02Harness.java` into ignored `server/target/`
output, and launches the coordinator. `Code02Harness self-test` runs the synthetic fixture and
path-guard checks without starting CANIS. An optional `--output` gives a *new* JSON path;
without it, each run creates a unique result name under `server/target/`. The coordinator
prints `CODE02_RESULT=passed scenario=normal verified=1/1` on success and exits 0. It writes
the run JSON plus `*.producer.csv` and `*.consumer.csv` sanitized file records next to it.
Use different output names for separate runs. Do not publish raw JVM or server logs.

The Ubuntu CI job uses the same POSIX wrapper and normal scenario from this section, with
`--output server/target/code02-ci.json`. It uploads `code02-evidence-ubuntu-latest` containing
the sanitized JSON and producer/consumer CSVs, the wrapper's console log, and an
`exit_status` text file. The job fails when the wrapper exits nonzero. Its artifact selection
does not include the private temporary run root, generated TLS stores, master key, plaintext,
or raw server/worker logs. CI artifacts are retained for seven days; cite the job and download
the artifact while available when recording a reproduction result.

The negative smoke cases use one file and require exit 0 only when the expected operation
fails closed, no recovered output is released, and the shared-directory invariant holds:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario tamper
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario incomplete
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario invalid-credentials
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario invalid-certificate
```

On POSIX, substitute `sh docs/research/run-code02.sh`. The normal path accepts `--files`
(1–100), `--size` (1–4194304 bytes), `--concurrency` (1–4), `--interval-ms` (0–60000),
`--delay-ms` (0–60000), and `--repetition` (1–5). `--files 100 --size 0` selects the
Issue #31 shuffled multiset (50 × 4 KiB, 35 × 256 KiB, 15 × 4 MiB); other positive sizes
apply to every file. `--size 0` is invalid with any other file count. The producer schedules
starts at the selected interval. The coordinator starts the consumer after the selected
delay from the first completed publication. One TLS connection is opened per consumer worker.
These options enable later study runs, but do not by themselves fulfill the Issue #31 warm-up,
baselines, repetitions, resource instruments, or CODE-03 memory preflight.

## Process and fixture layout

Each invocation creates a new restricted directory under the OS temporary directory. It refuses
pre-existing publication names and never points the packaged server at a deployment directory.
The server starts with this run's `server/` as its working directory, explicitly initializes
`server/db.dat`, then runs as its own process. The producer and consumer are separate JVMs.
The coordinator creates the recipient key through the official client only after a TLS health
check. It records all three process IDs in the result JSON.

```text
temporary run root/
  private/  master key, TLS stores, synthetic source and recovered bytes, private logs
  server/   isolated encrypted db.dat and private server logs
  shared/   CANISHYB1 .canis envelopes and transient .part files only
  worker/   process working directory
```

All passwords are fresh random values passed to child processes through private environment
variables; none appear in command arguments or metadata. The master key is generated with JDK
`KeyGenerator`, placed in a restricted file, and supplied by path through `CANIS_SECRET_KEY`.
JDK `keytool` generates new, short-lived PKCS#12 server, producer, recipient, and untrusted
test identities. Store passwords are supplied using `keytool`'s environment-password option.
The server certificate identifies `localhost`. Producer and recipient have distinct certificates
and credentials. Their trust stores contain the server certificate; the server trust store
contains the producer and recipient certificates. The untrusted certificate is absent from the
server trust store. All TLS remains TLS 1.3 with the existing JSSE validation and hostname check.

Payloads use the SHA-256 counter-stream algorithm and shuffle seed defined in the
[Issue #31 protocol](issue31-protocol.md). The source files stay under `private/`. The producer
encrypts into a `.part` file in `shared/`, verifies its `CANISHYB1` header, and publishes by
same-directory `ATOMIC_MOVE` without a fallback. The consumer opens only expected `.canis`
names, ignores `.part`, decrypts into `private/`, then compares bytes with the independently
generated expected input. Duplicate or unexpected shared names fail the run.

## Results, cleanup, and limits

The `code02-v1` JSON record contains the logical run ID, unique attempt ID, source revision,
dirty-tree flag, timestamp, OS and JDK, scenario, size/schedule, count, concurrency, delay,
process IDs, producer start, first publication and consumer start times, elapsed times,
heap used at worker exit, server CPU time where
available, verified count, outcome, and failure category. The two CSV files contain ordinal,
size, planned/actual publication or claim times, process-local duration, outcome, and the
consumer's sanitized failure category. These
are functional timings and memory observations, not calibrated peak RSS or benchmark data.
No raw exception message, plaintext, password, private key, certificate content, master-key
content, or absolute fixture path is placed in a result file.

After writing the result and CSVs, a passing run verifies its root marker and containment under
the OS temporary directory, then deletes that run root. A failing run retains its private root
for inspection and prints its path to stderr. Inspect retained evidence and remove it manually
after the cause is recorded; the harness never recursively removes an unverified path.
`server/target/` is generated build output and may be removed with `mvn clean` after copying
sanitized records. The server currently binds its configured port on available interfaces;
run this local experiment with host network controls appropriate to that behavior.

Windows execution needs permission to set restrictive ACLs; POSIX execution needs owner-only
mode support. A sandbox that blocks ACL changes must be reported as an environment failure,
not bypassed by weakening the fixture permissions. The client still reads whole files into
memory. A `--files 100 --size 0` run is not a valid Issue #32 result until the protocol's
CODE-01 and CODE-03 gates, complete instrumentation, warm-up, and repeated-run schedule are met.
