# Issue #31 CANIS baseline characterization method

## Scope and implementation plan

This amendment to [the prespecified study protocol](issue31-protocol.md) was recorded on 2026-11-18 at about 13:18 UTC, before any Issue #31 baseline measurements were seen. The reason is to characterize the existing CANIS workflow (path C) alone, as requested, within the measured CODE-03 workload boundary. It does not execute the local controls or the later fault study. The CODE-03 boundary is 4 MiB per file, four consumer workers, a 512 MiB heap per worker process, and one observed 100-file schedule. No run here exceeds those settings. The only source change is in the research-only `Code02Harness.java`: add the specified 20-file warm-up size mix. The runner and analysis are also research-only. Production code, formats, policy, and TLS settings are unchanged.

The security implication is limited to temporary generated test credentials and files. The harness checks private run-root containment, uses TLS 1.3, publishes only encrypted envelopes to the shared directory, verifies recovered bytes, and removes successful fixture roots. Results must contain no plaintext, keys, passwords, or store paths. A failed fixture root is private and must be inspected before cleanup.

## Execution

On the CODE-03 Windows host, build the repository once with Java 21 and Maven 3.9.x using the research runner's `-DskipTests package` build. Run one unmeasured 20-file warm-up for each setting, followed by five 100-file repetitions for each pair of consumer delay (0 or 60,000 ms) and concurrency (1 or 4). Execute groups in this fixed order: 0 ms/one worker, 60,000 ms/four workers, 0 ms/four workers, 60,000 ms/one worker. The producer uses 500 ms planned arrivals. The 20-file warm-up has 10 x 4 KiB, 7 x 256 KiB, and 3 x 4 MiB; the measured schedule has 50 x 4 KiB, 35 x 256 KiB, and 15 x 4 MiB. Both use the existing seeded Fisher-Yates schedule and SHA-256 counter-stream payload. The benchmark serializes invocations, preserves failed raw records, and stops at a failed run. A rerun skips an existing passing result. No measured run overlaps another.

The coordinator starts fresh server, producer, and consumer processes, databases, certificates, and credentials for each invocation. The consumer delay begins after the first complete envelope is published. `Code02Harness` records per-file publication, claim, finish, encryption, decryption, verification, and worker exit heap. The PowerShell runner polls Windows `PeakWorkingSet64` for the three recorded PIDs roughly every 100 ms. Those OS high-water values include fixture generation and verification. Exit heap is not peak JVM heap. The measured interval for throughput is planned first producer start through final verified file; exact UTC times are used with millisecond resolution. Per-file end-to-end time is planned start to verification; processing time is claim to verification. These cross-process wall-clock differences include clock and scheduling uncertainty. `encrypt_ns` and `decrypt_ns` are process-local monotonic intervals but include client and file handling, not isolated cryptographic primitives.

Raw JSON and worker CSVs are written under `server/target/issue31-baseline/`, with a per-run OS-memory JSON and sanitized stdout/stderr. The exporter creates `issue31-baseline-runs.csv` and `issue31-baseline-files.csv` under `docs/research/` only after all 24 invocations finish; a failed run remains in the ignored raw directory for investigation. Memory values absent from OS sampling are blank, never zero. No outlier is removed. Run-to-run variance is the range of the five per-run values for each setting; file latency distributions are descriptive and are never substituted for independent repetitions. Throughput is verified files divided by the observed interval and verified MiB divided by that interval. No operational suitability threshold or general capacity estimate is specified.

## Reproduction

From the repository root in Windows PowerShell, with JDK 21, Maven, Git, a free loopback port, and local NTFS owner-only ACL support:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue31-baseline.ps1
```

The runner builds, compiles the research harness, executes the matrix, and exports CSVs. Passing runs print `CODE02_RESULT=passed scenario=normal verified=20/20` for warm-ups or `verified=100/100` for measurements. It prints `ISSUE31_BASELINE=passed` only after all 24 invocations pass. A failed run leaves its private fixture root for manual review. Raw measurements in `server/target/` are ignored build artifacts; the committed CSVs are the sanitized, inspectable copy.

The original Issue #31 protocol also prescribes local A/B controls and additional JVM memory-pool, CPU, and queue instruments for the later full study. This baseline characterization makes no paired comparison and cannot support claims requiring those instruments. The original CODE-01 fixed-revision release gate also remains separate from these working-tree observations.
