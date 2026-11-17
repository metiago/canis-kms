# Issue #31 research protocol: representative workload and baselines

**Protocol status:** Prespecified on 2026-11-17, before collecting Issue #32–35
measurements. This document defines an experiment, not an observed performance or
availability result. Record any amendment with its date, reason, and whether data
had already been seen; do not silently change the workload after seeing results.
The [unified backlog](../../joss-backlog.md) is the task checklist.

## Research question and operational motivation

Under the documented shared-trust model, how do correctness, resource use, and
availability change when Java services exchange encrypted files asynchronously
using CANIS for public-key retrieval and data-key recovery? In particular, what
cost and failure dependence does the current whole-file `CANISHYB1` envelope and
TLS-only key service add to a local file-encryption workflow?

Sensitive files exchanged between financial systems require protection while
waiting for a recipient to process them. A dated 2024 letter reports production
use of a predecessor KMS for this purpose, and the authors identify CANIS as its
open-source continuation. The authors describe an integration involving Java
services and legacy systems. The [JOSS-01 evidence record](joss01-results.md)
separates the letter's claims from the authors' account. Neither source supplies
operational traffic counts, file-size distributions, processing delays, retention
rules, outage durations, or current-code performance. No customer workflow or
data has been approved for measurement or publication. The workload below is
therefore **synthetic and representative of the exchange pattern**, not a replay
or estimate of the historical deployment. The present study concerns the Java
client/server implementation; it does not test COBOL or PowerBuilder clients.

## Study boundary and prerequisites

- Use a fixed source revision, the official `JpawClient`, separate producer,
  consumer, and CANIS server processes, TLS 1.3 with certificate and hostname
  validation, a temporary encrypted database, and a temporary shared directory.
  Use distinct producer and recipient credential identities. The main workload
  uses one recipient key name; a separately admitted observer may be used only
  for the Issue #33 shared-trust boundary check. Key names are not access rules.
- Complete the research-release security gate in CODE-01 and the isolated
  process harness in CODE-02 before treating runs as study results. Record their
  versions and failures. If the fixed build changes, start a new result series.
- The current client reads entire input and envelope files into memory and
  creates additional byte arrays. Before the main runs, perform the CODE-03
  memory preflight at the largest size and concurrency below with the specified
  heap limit. An out-of-memory error, incomplete output, or swapping severe
  enough to invalidate timings stops the main series. Record it as a preflight
  result and revise the protocol or implement and test streaming under CODE-03
  before continuing. Never silently drop the largest size.
- Only research fixtures may be used. The harness must verify that the resolved
  temporary database, master-key, TLS stores, and shared directory are inside
  its newly created run directory before starting or injecting a fault. It must
  refuse a pre-existing database or production-looking path. The producer's
  plaintext and recipient's recovered files stay outside the shared directory.

## Fixed synthetic workload

These numbers are **study assumptions**, chosen to exercise a small-file
majority, a minority of larger files, queueing, and bounded parallel work.
They are not inferred from the historical deployment. One measured run has 100
files; the size schedule contains exactly 50 files of 4 KiB, 35 of 256 KiB, and
15 of 4 MiB. Shuffle this fixed multiset with a recorded non-secret seed for
each repetition. File names contain only run ID and ordinal. The producer
publishes at a planned rate of two files per second (500 ms between scheduled
starts), recording actual start times and any backlog. It publishes completed
envelopes by atomic rename within the shared filesystem. No plaintext is
published there.

| Parameter | Main study setting | Interpretation |
| --- | --- | --- |
| Processes and keys | One producer, one recipient, one CANIS server; one recipient RSA key | Represents a single file route, not a multi-tenant deployment. |
| File sizes | 4 KiB (50%), 256 KiB (35%), 4 MiB (15%) | Binary bytes; no compression or content-dependent claims. |
| Arrivals | 100 planned starts at 2 files/s | Open-loop schedule; missed starts and queue depth are outcomes. |
| Recipient delay | 0 s or 60 s after first successful publication | Consumer is a separate process; a 60 s run starts after files already exist. |
| Consumer concurrency | 1 or 4 workers, one TLS client connection per worker | Producer concurrency stays at 1; use no implicit retries in normal runs. |
| Retention | Keep encrypted envelopes until verified consumption or run teardown, at most 30 min | A harness cleanup rule, not a CANIS expiry feature. Record files left unconsumed at deadline. |
| Outage window | 30 s planned server stop, either after the 50th publication or at recipient start | Fault series in Issue #34 only; the two placements separate producer fetch from consumer unwrap dependence. Record actual stop and restart times. |

Run the four delay-by-concurrency combinations, five independent measured
repetitions each, for 20 normal runs and 2,000 planned files per baseline path. The producer
schedule and file-size multiset are the same across combinations for a given
repetition seed. Randomize the order of the four combinations within each
repetition to reduce time-of-day and thermal bias. Do not run two measured
combinations at once. Stop a run at 30 min after its first scheduled producer
start, even if publication never succeeds; record
every unfinished file as a failure and retain its sanitized state. An unavailable
key service may block new public-key fetches or data-key recovery. The
scheduled 30 s outages are measured separately on path C with the same workload
at one consumer worker and 60 s delay: five repetitions with the server stopped
after the 50th publication and five with it stopped at recipient start. Do not
pool either fault series with normal operation. Issue #34 defines additional
isolated faults and their expected
failure categories before execution.

Excluded conditions: WAN latency, distributed filesystems, customer data,
legacy-language interoperability, many producers, key rotation, automatic
expiry, disk-full stress, long-term durability, and production availability.
The 30 min harness retention and 30 s injected outage do not describe real
service levels. Existing files remain encrypted during a service outage; the
study must not infer indefinite recoverability from a short outage test.

## Fixtures and execution environment

Generate deterministic, non-compressible payload bytes as a SHA-256 counter
stream: concatenate `SHA-256(ASCII("CANIS-I31-v1") || uint32be(repetition) ||
uint32be(fileOrdinal) || uint64be(blockIndex))` blocks from index zero and
truncate to the scheduled file size. Number repetitions 1–5 and files 0–99.
Shuffle the size multiset for each repetition with Fisher–Yates using Java 21
`SplittableRandom(31000 + repetition)`; record the resulting order and SHA-256
of each input in a manifest outside the shared directory. Hashes supplement
the byte-equality check; they do not replace it. Use a fresh, randomly generated
AES database master key, TLS identities/truststores, and credential passwords
for each run; provision them externally to the processes in the private run
directory and never write values to results, logs, command lines, or source
control. Generate a new file data key and nonce per envelope as the client
normally does. State the TLS certificate subject, trust topology, and whether
credential identities share a certificate, without publishing private material.

Primary measurement host: one otherwise idle machine, loopback network, local
filesystem, Java 21, Maven 3.9.x, fixed JVM heap `-Xms128m -Xmx512m` for each
Java process, and the same JDK provider and cipher-suite configuration across
all paired trials. At execution, record source commit and dirty-tree status,
OS/build, CPU model and logical cores, RAM, filesystem type, JDK vendor and
exact `java -version`, Maven `mvn --version` including its JVM, JVM flags,
selected TLS suite, CPU load before each run, and tool versions. Record whether
Windows or POSIX permissions were used. A different host or JDK is a separate
replication series, not an extra repetition in the primary series. Build once
from the fixed revision with `mvn --batch-mode --no-transfer-progress clean
verify`; archive module test summaries and a hash of the built artifacts.

Warm up each process/control implementation with 20 unmeasured files: ten 4 KiB,
seven 256 KiB, three 4 MiB, using the same TLS and heap settings. Exclude
fixture generation, build, server startup, and warm-up from measured throughput.
Reinitialize the isolated database and restart all processes between measured
runs. Preserve the exact warm-up outcome and any failure; if warm-up fails,
do not label the subsequent run measured. Clean fixtures only after retaining
the sanitized manifest and result record.

## Baselines and what they establish

Use the same generated input bytes, filesystem, JVM settings, size schedule,
and run order for three paired paths. Keep timing instrumentation outside
production APIs and do not disable TLS or validation in the CANIS path.
Use independent, fresh RSA pairs with the same size and provider for paths B
and C; never export the server's private key for pairing. In path B, generate
the pair inside the isolated control area, make only its public key available
to the producer, and allow only the local consumer control to read the private
key. Pair comparisons by file bytes, size order, and repetition seed.

| Path | Operation | Valid interpretation |
| --- | --- | --- |
| A: local AES-GCM | JDK AES-256-GCM over each file's bytes with fresh 96-bit nonce and 128-bit tag, including local read/write. | Lower-bound local bulk-crypto plus I/O cost; not an end-to-end substitute for the envelope. |
| B: local envelope | Current `CANISHYB1` envelope code with a temporary local 2048-bit RSA pair and local unwrap callback; private key stays in the isolated control process. | Adds envelope assembly, RSA wrap/unwrap, and copies to A; it does not measure a remote service. |
| C: CANIS workflow | Official client fetches public key for each encryption and asks CANIS to unwrap each file key over authenticated TLS; no key cache. | End-to-end behavior and cost under the documented shared-trust policy. |

Report each path's absolute results. Paired C–B differences estimate the cost of
remote key operations and their transport/authentication in this setup; they
are not a pure network-latency estimate because implementation paths and
scheduling can differ. Measure connection/authentication time and the
`get-public` and `decrypt` request intervals separately where instrumentable;
do not subtract them twice from end-to-end time. A–B describes local envelope
overhead only approximately, because the two paths allocate and format data
differently. No alternative KMS comparison or superiority claim is planned.
Adding one requires a separately specified fair configuration and protocol
amendment before measuring.

## Outcomes, instruments, and analysis

Use monotonic `System.nanoTime()` timestamps for process-local intervals and
UTC wall-clock timestamps for cross-process events; record clock source and
avoid claiming sub-millisecond cross-process attribution without clock
synchronization. Capture one sanitized machine-readable row per file and
one per run. Required fields are revision, run ID, repetition, path, scenario,
size, planned/actual publication and consumer-start times, connection time,
public-key fetch time, local encryption time, unwrap request time, local
decryption time, queue wait, end-to-end time, outcome, and failure category.
Use `not_applicable` where a path has no service operation. Define end-to-end
latency from scheduled producer start to successful byte verification, and
processing latency from consumer claim to verification. Report queue wait
separately so the planned 60 s delay is not mistaken for crypto overhead.

Correctness is the count of published, authenticated envelopes whose
recovered bytes exactly match their source, divided by all planned files;
also report attempted, unpublished, rejected, duplicate, timed-out, and
unconsumed counts. A failed authentication, altered tag, or wrong key must
never yield a published plaintext file. The primary expected normal outcome
is 100/100 verified files per run and zero unexpected plaintext releases;
any deviation is a result requiring investigation, not a run to discard.

Resource outcomes are producer, consumer, and server peak resident memory,
peak JVM heap, process CPU time, bytes read/written, throughput in verified
files/s and MiB/s during the measured producer-to-last-verification interval,
and maximum queue depth. Use OS peak working-set size on Windows or
`/proc/<pid>/status` `VmHWM` on Linux, plus JDK memory-pool peak usage; sample
CPU and queue depth at 100 ms intervals and record sampler gaps. On other
platforms, specify an equivalent peak-memory source before running. Do not
infer a safe maximum file size from `-Xmx` alone.

Availability is measured as successful file operations divided by scheduled
file operations in each stated window, with counts and exact outage/restart
timestamps. Report fetch and unwrap success separately, time from server stop
to first detected failure, time from restart to first successful new request,
pending-file recovery count, and any plaintext released during faults. Use a
30 s operation deadline in the harness and report client timeouts separately
from immediate rejection. Do not label the single-host result a service-level
availability estimate. No latency, throughput, or uptime suitability threshold
is asserted because the historical workflow supplies none; suitability for a
particular deployment needs its own requirement and measurements. The
correctness and fail-closed expectations above are categorical acceptance
conditions for the experiment, not an operational service-level promise.

For each size, delay, concurrency, and path, report all five run values,
median and range of run medians, pooled p50/p95 processing and end-to-end
latency with sample counts, peak memory per process and run, throughput per
run, and all failures. Compute paired C–B differences by repetition and
scenario; show individual differences and median/range, without significance
claims from five runs. Do not average censored or failed operations into
success latency; include their counts and detection times alongside it.
No outlier removal is planned. Keep raw sanitized CSV or JSON, analysis code,
artifact hashes, exact commands, and platform limitations so every table can
be regenerated. An observed bottleneck is an interpretation, not proof of
general performance or cryptographic security.

## Security and compatibility interpretation

Never record payloads, plaintext, data keys, private keys, passwords, or TLS
store contents in raw results, exceptions, or published fixtures. The
temporary local RSA key in control B must not enter a public API or log.
Clean a run only after verifying the result manifest and that all paths are
inside its run directory. Do not test against a real database or bypass TLS.
The study changes no production API, envelope format, database format, or
shared-access policy. Valid existing `CANISDB1` records and `CANISHYB1`
envelopes need separate compatibility checks under CODE-01/CODE-03. A
successful synthetic run cannot establish compatibility with the historically
deployed predecessor, authorization between admitted services, or a
tamper-evident audit log.

## Gate for interpreting later results

Issues #32–35 may cite this protocol only after they record the fixed revision,
prerequisite status, execution environment, actual schedule versus planned
schedule, all run outcomes, deviations, and analysis artifacts. If an
instrument or implementation cannot produce a required field, mark it missing
with a reason and amend the protocol before making a claim that depends on it.
The earlier JOSS-01 and JOSS-03 tests are pilot evidence, not repetitions of
this workload.
