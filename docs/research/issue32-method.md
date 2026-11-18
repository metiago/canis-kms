# Issue #32 TLS transport-path characterization method

## Prespecification and scope

This method is recorded before Issue #32 measurements. The approved design is
the preceding Issue #32 analysis. This is a synthetic, single-host comparison
of the current TLS 1.3/mTLS CANIS workflow (A) with a loopback-only, research
CANISP fixture without TLS (B). B is not a supported transport, deployment
option, or production code path. The paired difference includes mTLS admission,
TLS record processing, and any residual difference in the research client and
server adapter. It is not a measurement of TLS cryptographic strength, pure
network time, scalability, production performance, or another KMS.

Only `docs/research/` source, scripts, method, results, and sanitized evidence
are in scope. The client/server modules, CANISP commands, cryptographic
algorithms, `CANISHYB1` and `CANISDB1` formats, TLS policy, and shared-trust
model remain unchanged. The research classes are compiled under ignored
`server/target/issue32-classes/` and are absent from production jars. The plain
listener binds only `127.0.0.1`, requires a newly created restricted temporary
root and marker, and checks that its peer is loopback. Run A under host inbound
network controls because the production server binds available interfaces.

## Fixed workload and comparison

The CODE-03 Windows preflight verified the largest planned file (4 MiB), four
consumer workers, a 512 MiB child JVM heap, and one 100-file Issue #31 mix.
Do not exceed any of those bounds. Each measured run has one producer, one
consumer process, one server, 100 synthetic files (50 x 4 KiB, 35 x 256 KiB,
15 x 4 MiB), planned 500 ms arrivals, and a 0 or 60,000 ms consumer-start delay
after the first complete publication. Consumer workers are 1 or 4. Each worker
opens one connection and processes assigned ordinals without a key cache. The
producer fetches the recipient public key for each file; the consumer requests
one unwrap per file. Bulk file bytes remain in local or shared storage, not on
the CANISP connection. `EnvelopeCryptographer` is the same production helper in
both arms. A uses `JpawClient` for key requests. B uses a research-only client
with the same CANISP line requests and length-prefixed responses and a separate
loopback server that reuses production `KeyValueStore` and `ClientHandler`.

Payload generation and size ordering follow Issue #31: SHA-256 counter stream
with `CANIS-I31-v1`, repetition 1..5, ordinal 0..99, and Fisher-Yates shuffle
with `SplittableRandom(31000 + repetition)`. The 20-file warm-up mix is
10 x 4 KiB, 7 x 256 KiB, 3 x 4 MiB. A and B in a pair receive the same bytes,
sizes, seed, worker count, delay, and JVM flags. The pair's fresh synthetic
database is initialized through the packaged TLS server, a recipient RSA key
is created, and the encrypted database is copied into two isolated server
directories before either arm measures. The matching synthetic master key and
generated PKCS#12 stores stay in the private pair root. Copying the encrypted
database is a prespecified amendment to Issue #31's independent local-control
key pairs; no private key is exported from the database.

The matrix has four delay-by-worker settings and five measured repetitions,
giving 20 A/B pairs and 40 measured runs. Run one 20-file unmeasured A/B pair
per setting before its five measured pairs: eight warm-up runs total. Warm-ups
use fresh short-lived JVMs and therefore warm the host/code path, not the later
measured JVM instances. This matches the existing Issue #31 baseline's warm-up
approach; cold-JVM effects remain a limitation. Within each repetition,
shuffle the four settings with a recorded fixed seed. Balance A-first and
B-first order (two each per repetition). No two measured invocations overlap.
All runs use `-Xms128m -Xmx512m` for server, producer, and consumer.

## Preflight and stopping rules

Before measured execution, run a clean Maven `verify`, compile only research
classes, run the research self-test, and run a one-file parity smoke. The smoke
checks A and B CANISP health, public-key retrieval, recipient data-key recovery,
`CANISHYB1` publication, and independent byte equality. It also checks that
the official high-level `JpawClient.encryptFile`/`decryptFile` works with the
same synthetic setup. The research worker calls the production public key and
unwrap APIs plus `EnvelopeCryptographer` directly to expose timing boundaries;
the high-level method's orchestration is therefore not timed verbatim.

Each file operation has a 30-second deadline and a run stops no later than
30 minutes after its first planned publication. A byte mismatch, failed
authentication, unexpected plaintext or shared file, missing result row,
fixture-guard failure, out-of-memory error, process failure, or timeout is a
recorded failure. Keep the raw attempt and its sanitized category. Do not
silently retry, replace, interpolate, or omit failures. If a warm-up fails,
its setting is not labelled measured until a documented new attempt succeeds.
If one arm of a pair fails, report both arms and mark the paired difference
unavailable. Stop the campaign if the preflight shows the CODE-03 bound no
longer holds on the measurement host.

## Instruments and calculations

Worker-local intervals use `System.nanoTime()`. Producer planned/actual start,
publication, consumer claim, and byte-verification events use UTC timestamps.
Cross-process latency is reported in milliseconds; no sub-millisecond
cross-process attribution is claimed. The producer CSV records input size and
hash, planned/actual start, publication, public-key fetch time, local envelope
time, and outcome. Consumer CSV records claim/finish, unwrap interval, local
decryption-and-verification interval, outcome, and sanitized failure category.
Connection CSV records one combined connect/TLS-handshake/CANISP-login interval
per client connection; it cannot split those three stages. The TLS arm uses the
official client. A separately labelled TLS probe may identify a negotiated
suite; it must not be presented as the measured connection's suite.

End-to-end file latency is planned producer start to independent byte
verification. Processing latency is consumer claim to verification. Queue
wait is publication to claim. Report connection, key-request, and local
envelope intervals separately; a key-request interval includes client,
network, and server work and is not pure network time. Run duration is first
planned start to last verification or run stop. For complete runs, throughput
is verified files and verified MiB divided by that duration. Incomplete-run
throughput is not compared as a success rate; retain its count and stop time.

The Windows runner samples each server, producer, and consumer process's
`WorkingSet64` and `PeakWorkingSet64` approximately every 100 ms, keyed by
recorded PID. `PeakWorkingSet64` is a process high-water value that includes
startup; sampled working set is a time-limited observation. Record sample
count and largest gap. Worker JVMs additionally record sampled heap-used
peaks; server JVM heap peak is not available without adding instrumentation
to the production process and is therefore not inferred. Memory is reported
per process and never summed from non-simultaneous peaks.

For each setting and arm, retain all five run values, success/failure counts,
run medians, range, and sample standard deviation. Pooled per-file p50/p95 by
size are descriptive only. Pair A-minus-B values by repetition and setting;
show each difference and its median/range. Do not remove outliers or interpret
five runs as a population estimate. The planned 500 ms arrivals constrain
throughput, and the 60 s delay dominates end-to-end latency. Report those
effects separately from transport timing.

## Reproduction, evidence, and cleanup

Run on one documented host with JDK 21+, Maven 3.9.x, Python 3, local storage
supporting atomic rename and restrictive permissions, and blocked non-loopback
inbound access. Record source revision/dirty state, source and jar hashes,
OS/build, CPU/logical cores, RAM, filesystem, JVM and Maven versions, JVM
flags, certificate trust topology, command schedule, and clock/sampler limits.
The Windows campaign command from repository root is:

```powershell
mvn --batch-mode --no-transfer-progress clean verify
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue32.ps1 -Scope campaign -OutputDirectory server/target/issue32-new-series -ExportDirectory docs/research/issue32-new-artifacts
python docs/research/analyze-issue32.py docs/research/issue32-new-artifacts
python docs/research/verify-issue32.py docs/research/issue32-new-artifacts --scope campaign
```

**Post-execution command correction:** The original prespecified command
omitted the runner's explicit new output and export directories. This text
supplies the required arguments for a fresh reproduction. It does not amend
the workload, matrix, instrumentation, failure criteria, or interpretation.

The runner refuses existing final artifact names, executes all warm-ups and
measured pairs in the prespecified order, and exports sanitized JSON/CSV into
the chosen `-ExportDirectory` only after each attempt ends. The recorded final
series used `docs/research/issue32-artifacts/`. Its summary
marker reports recorded runs, not a performance pass. The analysis script
recomputes per-run and paired metrics from raw CSV/JSON. The verifier checks
the matrix, fixture counts, hashes, record relationships, and banned secret
markers; it does not recreate deleted private fixtures.

Each pair uses a new restricted OS temporary directory with a marker,
`private/`, `provision/`, and separate `A/` and `B/` server/shared/worker
subdirectories. Source and recovered bytes stay in private areas; only
encrypted `.canis` envelopes and transient `.part` files enter shared storage.
Passwords are passed via private process environment, never arguments or raw
results. No real secrets, production keys, customer data, or external service
is used. On success, export and verify sanitized records, check the marker and
resolved root remains under the temporary directory, then remove the private
root. On failure, retain it for inspection and record the cleanup action.
Do not publish raw private logs.

The research comparison does not change the paper or deployment documentation.
Any reported difference is confined to this synthetic, loopback, fixed-host
configuration. It cannot establish attack resistance, security strength,
capacity, general memory limits, production readiness, or superiority over
other systems.
