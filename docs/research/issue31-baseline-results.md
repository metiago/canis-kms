# Issue #31 CANIS baseline characterization: Windows, 2026-11-18

## Result and boundary

The [research-only method](issue31-baseline-method.md) executed four 20-file warm-ups and 20 measured 100-file CANIS exchanges. All 80 warm-up files and 2,000 measured files were published, authenticated, and byte-verified. Each measured run used the specified 50 x 4 KiB, 35 x 256 KiB, 15 x 4 MiB mix at 500 ms planned arrivals. No file exceeded the CODE-03 verified 4 MiB boundary; consumer concurrency was one or four, with a 512 MiB heap for each server and worker process. These are absolute observations from one synthetic workload on one Windows host. They are not capacity, availability, or general scalability estimates.

## Environment and execution record

Source HEAD was `f00310ee3678f8c96fc1a8681f35896d5163d3de`, with a dirty tree containing the research-only harness change and these artifacts. `Code02Harness.java` SHA-256 was `37DC7DCA71289FC963850190663C46823F31D2DA7BAC6AC6022FD92251839B55`. The packaged server jar SHA-256 was `5E2EFE57BB32EEA1B9126BEEB8AC62D157D3DEA388741FEAE0D3EB4691F22280`. The benchmark ran from 2026-11-18 13:19 to 13:48 UTC. The host was Windows 11 Enterprise build 26100, Intel Core i7-1355U, 12 logical processors, 15,941,916 KiB reported physical RAM, local NTFS, and loopback networking. A post-run system query showed 2,570,260 KiB free RAM; this is not a during-run minimum. Oracle Java `21.0.7+8-LTS-245` ran the harness and children. Maven 3.9.12 used Eclipse Adoptium Java 21.0.6. The server, producer, and consumer had `-Xms128m -Xmx512m`; the coordinator used the default heap. TLS remained at the existing TLS 1.3 policy, with generated private fixtures and owner-only Windows ACLs. The server certificate had `CN=localhost` and a localhost DNS SAN; the server trusted the distinct producer and recipient client certificates. Both clients trusted the server certificate. The negotiated cipher suite was not recorded.

The runner built the server and client with `mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package`, compiled the harness with `javac`, and serialized the invocations. Every warm-up and measured run used fresh isolated server, database, generated TLS identities, and credential passwords. The seed was `31000 + repetition` with repetitions 1–5; each warm-up used repetition 1. The four setting groups ran in this order: 0 ms/one worker, 60,000 ms/four workers, 0 ms/four workers, 60,000 ms/one worker. There was no simultaneous measured run. The CODE-02 self-test passed after the campaign.

The first sandboxed warm-up stopped during fixture setup before writing a CODE-02 result. The permitted rerun passed and is the retained warm-up. This setup failure was not a file or measured-run failure. The failed attempt's sanitized stderr said `io_or_authentication_failure`; the exact blocked operation was not exposed. The OS sampler wrote a partial record for that attempt, then the successful rerun replaced it. No failed measured run was excluded.

## Observed latency, throughput, and variance

The [run CSV](issue31-baseline-runs.csv) retains every run's interval and throughput. The [file CSV](issue31-baseline-files.csv) retains every file's planned and actual start, publication, claim, finish, local operation timing, and outcome. Each row below summarizes five independently restarted measured runs and 500 files. Ranges are the minimum and maximum of five run values. Pooled file p95 uses nearest rank; these file samples are descriptive and do not replace the five repetitions. Times are milliseconds. The throughput interval is first planned producer start to last verification, so it includes the deliberate delay.

| Delay / workers | Files/s median [range] | MiB/s median [range] | Processing p50 / p95 | End-to-end p50 / p95 | Median processing latency across runs [range] |
| --- | ---: | ---: | ---: | ---: | ---: |
| 0 ms / 1 | 2.0175 [2.0161, 2.0185] | 1.3910 [1.3900, 1.3916] | 24.717 / 162.808 | 83.217 / 596.317 | 24.336 [22.560, 35.989] |
| 0 ms / 4 | 2.0151 [1.9999, 2.0165] | 1.3893 [1.3788, 1.3903] | 52.574 / 440.935 | 160.386 / 1037.528 | 51.810 [41.104, 54.219] |
| 60,000 ms / 1 | 1.4541 [1.4492, 1.5493] | 1.0025 [0.9991, 1.0681] | 27.075 / 369.542 | 39,495.842 / 59,184.114 | 33.140 [15.849, 34.271] |
| 60,000 ms / 4 | 1.5925 [1.5877, 1.5956] | 1.0980 [1.0947, 1.1001] | 27.733 / 172.630 | 37,305.157 / 59,153.990 | 27.902 [25.973, 30.545] |

The zero-delay per-run measured interval was 49.543–50.003 s across both worker settings. The delayed intervals were 62.674–69.006 s. Planned arrivals at 500 ms bound the observed end-to-end files/s; these values do not measure unconstrained service throughput. Queue-wait p50/p95 was 6.601/90.606 ms (0 ms, one worker), 4.054/15.808 ms (0 ms, four workers), 39,389.796/58,938.811 ms (60 s, one worker), and 37,202.133/58,787.676 ms (60 s, four workers). This keeps the intentional consumer delay separate from processing latency.

## Memory observations

The values below are the range of **OS-reported process peak working sets** across five runs, in bytes. They are not simultaneous totals or JVM heap peaks. Windows `Get-Process PeakWorkingSet64` was polled roughly every 100 ms; the largest observed sampler gap among measured runs was 211 ms. The server peak includes startup. Producer fixture generation and consumer independent byte verification contribute to their process peaks. Exact per-run peaks and JVM **heap used at worker exit** are in the run CSV.

| Delay / workers | Server peak range | Producer peak range | Consumer peak range |
| --- | ---: | ---: | ---: |
| 0 ms / 1 | 123,953,152–136,155,136 | 171,827,200–182,530,048 | 166,367,232–184,246,272 |
| 0 ms / 4 | 121,012,224–127,463,424 | 172,511,232–191,221,760 | 191,537,152–207,089,664 |
| 60,000 ms / 1 | 124,334,080–127,594,496 | 171,765,760–185,143,296 | 173,191,168–202,821,632 |
| 60,000 ms / 4 | 122,175,488–137,519,104 | 171,593,728–189,210,624 | 205,897,728–254,959,616 |

## Reproduction and acceptance review

Run `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue31-baseline.ps1` from the repository root on the Windows environment described above. Existing passing raw run records are skipped. For a fresh series, use a clean `server/target/issue31-baseline/` after preserving any prior evidence. Then `python docs/research/analyze-issue31-baseline.py` regenerates the descriptive statistics from the published CSVs. The exporter alone can be rerun with `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-issue31-baseline.ps1`. The published run CSV SHA-256 is `78ACA18C8B1F44F5484D542259E9DEE4F5DA6F4A464414614E98BD4278B2693B`; the file CSV SHA-256 is `EDCF1041D262322A2BE932D892BE4C1E20D6183A95FF14A14250E64E5A3D8D8F`. Each run row also hashes its underlying ignored JSON, producer CSV, consumer CSV, and OS-memory JSON. A scan of both published CSVs found no password assignment, master-key marker, PKCS#12 filename, or private-key delimiter.

| Criterion | Review |
| --- | --- |
| CODE-03 boundary | Met for this host: maximum 4 MiB, at most four workers, 512 MiB child heaps, and the verified 100-file mix. |
| Warm-up and repetitions | Four 20-file warm-ups passed; five 100-file repetitions passed in each of four settings. |
| Latency, throughput, memory, variance, raw data | Reported above; 24 run rows and 2,080 file rows are published with analysis code. |
| Correctness | 2,000/2,000 measured files authenticated and byte-matched; no reported unexpected plaintext output. |
| Security and compatibility | Research-only harness change; no production API, envelope, database, TLS policy, or shared-trust change. Existing CODE-02 self-test passed. Post-campaign `mvn -q test` exited 0 with 159 tests, zero failures/errors, and eight skips; `mvn spotless:apply` passed. |
| Fixed research-release / full study gate | Open. The source was dirty, CODE-01 fixed-revision gate remains separate, and this is a path-C absolute baseline only. |

This campaign did not execute the original protocol's local control paths, randomized setting order, input-hash manifest, clean `verify` build before timing, per-run CPU-load check, JDK memory-pool peaks, CPU sampling, byte I/O, or queue-depth sampling. The four warm-ups used separate short-lived processes, so they warmed the host path but not the measured JVM instances. Grouped order may confound setting differences with host changes over time. The cross-process UTC intervals depend on one host clock and include scheduling error; processing nanoseconds use each worker's monotonic clock. These missing instruments prohibit claims that require isolated key-service cost, JVM peak heap, CPU attribution, or a safe operating maximum. There is no comparison with another transport or service. Future work is to run the remaining prespecified controls and instruments on a fixed build if the broader study needs them; no such work is claimed here.
