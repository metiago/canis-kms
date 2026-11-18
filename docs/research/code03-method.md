# CODE-03 memory preflight protocol

**Prespecified 2026-11-18 before CODE-03 workload execution.** This is a research-only
preflight for the existing `CANISHYB1` client and the Issue #31 study, not an Issue #32
performance result or a general large-file limit. Source analysis precedes this protocol;
no production cryptography, transport, storage format, or trust policy is changed.

## Host and scope

The primary host is Windows 11 Enterprise, build 26100.9911, amd64, with a 13th Gen
Intel Core i7-1355U, 12 logical processors, and 16,324,521,984 bytes of physical RAM
(JDK operating-system bean). Available physical RAM at protocol capture was
2,426,916,864 bytes. The workspace is on local `C:` NTFS with 24,351,444,992 bytes
free at capture. Fixture storage uses the Windows temporary directory on this volume;
the network path is loopback. Oracle Java 21.0.7+8-LTS-245 executes the harness,
workers, and server; Maven 3.9.12 runs on Eclipse Adoptium Java 21.0.6. The source
HEAD before changes is `0be60db575c5952f6cdddede21b20d135d093e07`; result JSON
records the revision and dirty-tree state for each run. The server, producer, and
consumer each use `-Xms128m -Xmx512m`; the coordinator uses the JVM default heap.
Windows owner-only ACLs protect generated fixtures. Exact version commands are below.

## Workload and stopping rules

Use the existing CODE-02 separate-process normal scenario with a fresh isolated
database, master key, TLS identities, and credentials per invocation. Each run has
four same-size synthetic files, one producer, four consumer workers, a 1,000 ms
consumer-start delay, and zero scheduled interval between producer files. The
existing Issue #31 SHA-256 counter-stream fixture generator creates each input.
The consumer verifies byte equality after authenticated decryption. The shared
directory contains encrypted envelopes only.

| Size in bytes | Label | Reason |
| ---: | --- | --- |
| 4,096 | 4 KiB | Issue #31 small size |
| 262,144 | 256 KiB | Issue #31 middle size |
| 1,048,576 | 1 MiB | Intermediate memory check |
| 4,194,304 | 4 MiB | Issue #31 maximum; four-way consumer concurrency |

Run one **unmeasured** four-file, 4 MiB warm-up followed by two measured attempts
at each size, in ascending size order. Warm-up uses the same heap and TLS setup.
All measured attempts, including failures, remain in the raw record. This is a
bounded preflight, not the Issue #31 twenty-run study. Sizes above 4 MiB are
outside the predefined study and outside the CODE-02 harness input cap; classify
them `not tested` rather than inferring support.

Stop the series after an out-of-memory error, process timeout, incomplete
publication or recovered output, failed authenticated byte comparison, or
resource exhaustion that invalidates timing. Retain the failure record and do
not label subsequent sizes supported. A failure of fixture provisioning or
host permissions is an environment failure, not a client size limit. No swap
counter is available in this preflight; prolonged stalls or a severe drop in
available RAM must be reported as an interpretation limit. No average, safe
maximum, or throughput claim is derived from these runs.

## Measurements and classification

For each run, retain CODE-02 JSON and producer/consumer CSVs. Their per-file
`encrypt_ns` and `decrypt_ns`, worker elapsed times, `verified_files`, and
failure categories provide the observed operation outcomes and times. The
coordinator JSON's `producer_heap_used_bytes_at_exit` and
`consumer_heap_used_bytes_at_exit` are **exit observations**, not heap peaks.
The CODE-03 PowerShell runner polls `Get-Process java` approximately every
100 ms and retains the OS `PeakWorkingSet64` value for the three process IDs
in the CODE-02 JSON. These are observed OS-reported process high-water marks;
the sampler's count and maximum gap are retained. A missing PID observation is
recorded as `null`, never zero. The server's peak includes startup. The
producer's fixture generation and the consumer's independent byte comparison
also contribute to their peaks, so these values are conservative for the
exchange path. The runner records elapsed wall time for each invocation.

`verified supported` means both measured attempts passed publication,
authenticated recovery, and byte equality for all four files with memory
observed and no resource warning. `conditionally supported` means all files
verified but memory data or host headroom is insufficient to accept the
bound without qualification. `unsupported` requires a repeatable workload
failure under this exact configuration after environmental causes are ruled
out. `not tested` means no completed measurement. These labels apply only to
this host, heap, concurrency, and workload shape.

## Execution, expected outputs, and cleanup

From the repository root, build and compile the existing research harness,
then run `run-code03.ps1` once. It creates `server/target/code03-preflight/` with
one JSON, two CSVs, a sanitized console record, and one OS-memory JSON per
attempt. A passing attempt prints `CODE02_RESULT=passed scenario=normal
verified=4/4`; expected CSV outcomes are four `published` and four `verified`.
The runner stops on the first failure. It does not write payloads or secrets
into research results. The CODE-02 coordinator removes each successful private
run root after checking its marker and containment. If a run fails, it prints
`CODE02_PRIVATE_EVIDENCE=<temporary path>` and preserves that private root for
inspection; remove it only after verifying the marker and path. Generated
`server/target/` outputs may be removed with `mvn clean` after copying the
sanitized results. `export-code03.ps1` validates the per-run records and
exports `code03-runs.csv` and `code03-files.csv` to a new output directory.
The research index links the retained published CSVs.

```powershell
java -version
mvn --version
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code03.ps1
New-Item -ItemType Directory -Path server/target/code03-export | Out-Null
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-code03.ps1 -OutputDirectory server/target/code03-export
```

The Windows `Get-Process` peak-working-set field is the OS memory instrument.
This runner does not supply POSIX memory measurements. A future fixed-revision
Issue #32 series must follow the full Issue #31 protocol, including its
20-file warm-up, complete 100-file schedule, baselines, and separate JVM
memory-pool and Linux/Windows resource instruments.

## Prespecified supplementary schedule check

At 2026-11-18 12:37 UTC, after the warm-up and initial small-size passes but
before the 4 MiB measured repetitions or this supplementary run, add one
**unmeasured campaign-shape check**: `--files 100 --size 0 --concurrency 4
--interval-ms 500 --delay-ms 60000 --repetition 1`. This uses the exact
Issue #31 size multiset, arrival interval, longest planned consumer delay,
and maximum planned consumer concurrency. It is required to judge whether
the *schedule*, rather than only four concurrent maximum-size files, fits
the present heap and 90-second consumer deadline. Record all 100 producer
and consumer rows and the three process peaks. A pass supports preflight
feasibility of this schedule on the current host; it does not replace the
Issue #31 measured campaign or its 20-file warm-up. The reason for this
addition is that four-file tests alone cannot establish 100-file behavior.
