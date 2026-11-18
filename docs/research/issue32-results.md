# Issue #32 observed TLS transport-path comparison: Windows, 2026-11-18

## Result and scope

The [prespecified method](issue32-method.md) was executed on one Windows host
using synthetic fixtures and the CODE-03 verified workload. The final campaign
recorded **four 20-file A/B warm-up pairs** and **twenty 100-file measured A/B
pairs**: 8 warm-up runs and 40 measured runs. All 160 warm-up file recoveries
and all **4,000 measured file recoveries** authenticated and independently
byte-matched. Each arm had 2,000 measured successes and zero measured failed
files. Every measured run recorded a positive duration, file-level latency,
throughput, process memory observations, and five repetitions per setting.

A is the current TLS 1.3/mTLS key-service path. B is a separate, loopback-only
research fixture using the same encrypted database snapshot, production
`KeyValueStore`, `ClientHandler`, and `EnvelopeCryptographer`, with research
CANISP framing/client login. B is not a supported CANIS transport, product
configuration, or deployment path. The client/server production sources and
the paper were not changed. These are observed differences between two
instrumented paths on this host, not pure TLS cryptographic cost, scalability,
cryptographic strength, production performance, or a comparison with another
system.

## Fixed source, environment, and execution

Final source revision was `9b01048256a5bfe260aebfe0429f521282f6f475`.
The final [environment record](issue32-artifacts/environment.json) and all
48 run JSON files record `dirty_tree: false`; all use server jar SHA-256
`2aee3f422eb1455eef82e83d6340473b566aa6556a64894dc6a1fe22981c7171`
and Issue32 harness SHA-256
`f29e2a485da75e9d0afb4dd91a630af5aaae33b4d7a6ca2cb4bf7abeea6a141b`.
The final campaign ran from 2026-11-18 20:59:54 to 21:55:04 UTC. Its
[schedule](issue32-artifacts/schedule.csv) records the exact pair order,
pre-run CPU load, available RAM, timestamps, sampler counts, and blank
coordinator exit-code fields observed from Windows `Start-Process`. The runner
required the `ISSUE32_PAIR=passed` marker and both passing arm JSON records;
the worker exit codes in those JSON records are zero. All 20 measured pairs
were serialized, with ten A-first and ten B-first orders. Per-run `source_revision`
and jar hash are constant across the final series.

The host was Windows 11 NT `10.0.26100.0`, 13th Gen Intel Core i7-1355U,
12 logical processors, 16,324,521,984 bytes RAM, and local NTFS. The harness
and children ran Oracle Java `21.0.7+8-LTS-245`; Maven 3.9.12 used Eclipse
Adoptium Java 21.0.6. Server, producer, and consumer JVMs each used
`-Xms128m -Xmx512m`. A used generated, short-lived PKCS#12 identities with
separate producer and recipient credentials. Both clients trusted the
synthetic `localhost` server certificate; the server trusted both client
certificates. The A preflight probe negotiated `TLSv1.3` and
`TLS_AES_128_GCM_SHA256` in all 20 measured runs. It was a separate probe,
not an observation of every measured connection's cipher suite.

The effective Windows firewall profiles were enabled with default inbound
action `Block`; the read-only host query found zero Java program-specific
application filters. B additionally bound only `127.0.0.1` and rejected a
non-loopback peer. A retained the production server's wildcard bind; this
study did not enumerate every possible port-specific firewall exception or
verify network reachability from a separate host. All workload clients used
loopback, temporary encrypted databases, synthetic keys, and private
temporary file roots. The research class was absent from both built product
jars in `jar tf` inspection.

The fixed-revision `mvn --batch-mode --no-transfer-progress clean verify`
exited 0. Surefire XML contains 28 suites, 159 tests, zero failures, zero
errors, and eight skips. `mvn spotless:apply` exited 0 and did not change
production Java. The research Java compiled with `javac`, its self-test
passed, and the one-file parity smoke passed both A and B. The main campaign
command was:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue32.ps1 -Scope campaign -OutputDirectory server/target/issue32-final -ExportDirectory docs/research/issue32-artifacts
```

The method records 20 unmeasured files per arm/setting and five measured
100-file repetitions per arm/setting. Each measured run used 50 x 4 KiB,
35 x 256 KiB, and 15 x 4 MiB, planned 500 ms arrivals, a 0 or 60,000 ms
recipient delay, and one or four recipient workers. No measured file exceeded
4 MiB, no run exceeded four workers or the 512 MiB child heap setting, and
the exact synthetic input sizes and hashes match across A/B arms. This
reuses the bounded CODE-03 workload; it does not establish a safe size limit.

## Scenario observations

| Scenario | Observation | Evidence | Status |
|----------|-------------|----------|--------|
| A: TLS 1.3 + mTLS | 20 measured runs; 2,000/2,000 verified files; zero failed files. Five runs per delay/worker setting. All A preflight probes negotiated TLS 1.3 and the suite stated above. | [Run CSV](issue32-artifacts/analysis-runs.csv), [file CSV](issue32-artifacts/analysis-files.csv), [schedule](issue32-artifacts/schedule.csv), [raw manifest](issue32-artifacts.sha256) | Observed in final campaign |
| B: research non-TLS loopback fixture | 20 measured runs; 2,000/2,000 verified files; zero failed files. Its input hashes and encrypted database snapshot hash matched A within every pair. | [Run CSV](issue32-artifacts/analysis-runs.csv), [file CSV](issue32-artifacts/analysis-files.csv), [pair CSV](issue32-artifacts/analysis-pairs.csv), [raw manifest](issue32-artifacts.sha256) | Observed in final campaign |

The [artifact directory](issue32-artifacts/) contains 438 sanitized files:
244 CSV, 98 JSON, and 96 worker heap-observation text files. It includes
4,160 per-file analysis rows: 160 warm-up and 4,000 measured. The
[analysis summary](issue32-artifacts/analysis-summary.json) preserves each
setting's five-run median, minimum, maximum, sample standard deviation,
size-stratified descriptive p50/p95, and paired A-minus-B differences.
The [pair CSV](issue32-artifacts/analysis-pairs.csv) contains each individual
paired difference. No observed run or file was removed as an outlier.

## Observed latency and throughput

Each A and B latency below is the median of five **run-median** end-to-end
file latencies, from planned producer start to independent byte verification.
The paired column is the median and full range of five within-repetition
A-minus-B run-median differences; it is not computed by subtracting the two
group medians. Throughput uses first planned start to last verified file and
includes the deliberate 60 s delay. All times are milliseconds unless noted.

| Delay / workers | A latency | B latency | Paired A−B latency median [range] | A files/s | B files/s | Paired A−B files/s |
|-----------------|----------:|----------:|----------------------------------:|----------:|----------:|-------------------:|
| 0 s / 1 | 163.151 | 153.561 | +13.334 [−60.833, +28.475] | 2.0160 | 2.0163 | −0.0006 |
| 0 s / 4 | 162.818 | 123.102 | +4.799 [−29.430, +56.999] | 2.0160 | 2.0165 | +0.0001 |
| 60 s / 1 | 40,854.548 | 40,436.082 | +552.244 [−13.715, +913.333] | 1.4236 | 1.4388 | −0.0117 |
| 60 s / 4 | 37,696.331 | 37,256.833 | +503.542 [+294.816, +675.945] | 1.5800 | 1.5932 | −0.0100 |

Observed MiB/s group medians were A/B 1.3899/1.3901 (0 s, one worker),
1.3899/1.3903 (0 s, four workers), 0.9815/0.9920 (60 s, one worker), and
1.0894/1.0984 (60 s, four workers). The corresponding paired A-minus-B
median MiB/s differences were −0.0004, +0.0001, −0.0080, and −0.0069.
The planned two-files-per-second arrival schedule bounds these observed
throughput values; they do not measure unconstrained capacity. The delayed
end-to-end values include the prescribed wait. Median run-median
claim-to-verification processing times ranged from 26.736 to 49.816 ms
across A settings and 27.892 to 47.736 ms across B settings; exact values
and per-size distributions are in the raw analysis files.

Median run durations (first planned start to final verification) were
A/B 49.603/49.596 s (0 s, one worker), 49.603/49.591 s (0 s, four workers),
70.244/69.500 s (60 s, one worker), and 63.289/62.768 s (60 s, four workers).
Every run duration and its range is in the [run CSV](issue32-artifacts/analysis-runs.csv).

The research worker exposed connection-plus-login, public-key request,
local envelope, unwrap request, and local decryption-and-verification intervals.
The values below are medians of five paired A-minus-B run differences. The
connection interval includes TLS context creation, certificate work, network
connect, and CANISP login in A; it is **not** TLS handshake time alone.
Public-key and unwrap intervals include client, loopback transport, and
server work, not pure network time. Local envelope and recovery code is the
same production helper in both arms, but scheduling can still differ.

| Delay / workers | Producer connect+login Δ ms | Consumer connect+login Δ ms | Public-key fetch Δ ms | Unwrap Δ ms |
|-----------------|----------------------------:|----------------------------:|----------------------:|------------:|
| 0 s / 1 | +538.745 | +596.674 | +0.235 | +0.987 |
| 0 s / 4 | +567.823 | +757.708 | +0.784 | +1.033 |
| 60 s / 1 | +742.185 | +582.284 | +0.832 | +0.611 |
| 60 s / 4 | +668.839 | +762.840 | +1.229 | +0.221 |

The connection data, local component intervals, and variance are in the
[run CSV](issue32-artifacts/analysis-runs.csv), [file CSV](issue32-artifacts/analysis-files.csv),
and each run's raw `producer-connections.csv` and `consumer-connections.csv`.

## Observed memory

Windows `PeakWorkingSet64` was sampled by recorded PID about every 100 ms
for server, producer, and consumer. Values include process startup and, for
workers, synthetic fixture generation and independent byte verification.
The table gives the median of five paired A-minus-B **process peak working-set**
differences in MiB. These are observed process-memory differences; they are
not isolated TLS allocations or simultaneous system totals.

| Delay / workers | Server Δ MiB | Producer Δ MiB | Consumer Δ MiB |
|-----------------|-------------:|---------------:|---------------:|
| 0 s / 1 | +17.07 | +10.98 | +12.80 |
| 0 s / 4 | +18.37 | +10.54 | +16.26 |
| 60 s / 1 | +14.35 | +14.45 | +20.87 |
| 60 s / 4 | +14.77 | +11.57 | +24.55 |

Some individual producer or consumer paired differences were negative;
their ranges and each arm's absolute bytes are in the [pair CSV](issue32-artifacts/analysis-pairs.csv)
and [run CSV](issue32-artifacts/analysis-runs.csv). Worker sampled JVM heap
peaks are present there. Server JVM heap peak was not collected, so no server
heap allocation difference is inferred. Measured-pair sampler maximum gaps
ranged from 142 to 1,955 ms; the longest was in `issue32-r3-d60000-c1`.
Although OS process high-water values persist while a process is alive, a
process exiting during a sampling gap could hide an unobserved peak. These
measurements do not define a general memory bound.

## Reproduction, verification, and preliminary attempts

From a clean checkout of the fixed research source revision with the Windows
environment above, an available loopback port, owner-only ACL support, and
host inbound controls, run from repository root:

```powershell
mvn --batch-mode --no-transfer-progress clean verify
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue32.ps1 -Scope campaign -OutputDirectory server/target/issue32-new-series -ExportDirectory docs/research/issue32-new-artifacts
python docs/research/analyze-issue32.py docs/research/issue32-new-artifacts
python docs/research/verify-issue32.py docs/research/issue32-new-artifacts --scope campaign
```

Use new output names; the runner refuses an existing run directory. The final
series used `server/target/issue32-final` and exported
`docs/research/issue32-artifacts`. The actual post-run commands were:

```powershell
python docs/research/analyze-issue32.py docs/research/issue32-artifacts
python docs/research/verify-issue32.py docs/research/issue32-artifacts --manifest docs/research/issue32-artifacts.sha256
```

The verifier printed `ISSUE32_EVIDENCE=passed pairs=24 file_rows=4160`.
An independent PowerShell `Get-FileHash` check found zero mismatches across
all 438 [manifest](issue32-artifacts.sha256) entries. The final manifest's
SHA-256 is `d9d6904f09bc6edf7cca33b50d74e8cada092f079f266be928828756f1328e9b`.
The analysis script was extended **after measurement** to summarize the
already recorded component intervals and MiB/s; it did not change raw
run records. Its current SHA-256 is
`94e83ad688ada69c3010b67607a38763fb51bcc80a11254e4b2885c728f327cd`.
The method's reproduction command was also corrected after execution to
include the runner's required new output and export directory arguments;
the recorded workload and analysis rules were unchanged.

Before the final series, a sandboxed one-file smoke stopped while setting
restrictive Windows fixture ACLs. A permitted one-file smoke then verified
both arms; its first runner invocation misread a blank `Start-Process`
coordinator exit-code field as failure despite passing JSON, and the corrected
runner's smoke passed. A preliminary full-campaign attempt completed two
20-file warm-up A/B pairs, then was stopped because an untracked Python
`__pycache__` made `dirty_tree: true`. That bytecode cache was inspected and
removed. Preliminary output remains under ignored `server/target/issue32-campaign/`
and was **not pooled** with the final clean-revision series. No final measured
run was retried or discarded after a measurement failure.

## Acceptance review

| Acceptance Criterion | Evidence | Status |
|---------------------|----------|--------|
| Method fixed before measurements | [Issue #32 method](issue32-method.md) and research commit `6436212`, before the final campaign | Complete |
| Workload within CODE-03 bound | [Schedule](issue32-artifacts/schedule.csv), input manifests, and verifier confirm 4 KiB/256 KiB/4 MiB mix, at most four workers, 512 MiB child heaps | Complete for this workload |
| Same synthetic workload and paired backend | 24 input manifests; matching file SHA-256 and encrypted database snapshot hashes in A/B raw JSON; verifier | Complete |
| Same host, JVM, hardware, file sizes, and workers | [Environment JSON](issue32-artifacts/environment.json), constant run-record JDK/source/jar fields, [schedule](issue32-artifacts/schedule.csv), and input manifests | Complete for the final series |
| TLS arm and isolated plain fixture | A raw TLS preflight fields, B loopback guard source, product-jar class inspection, [method](issue32-method.md) | Complete for recorded fixture; external host reachability not tested |
| Production behavior and paper unchanged | Only research files, the Issue #32 backlog entry, and `.gitattributes` line-ending rules for research artifacts changed; no research class in product jars | Complete |
| Latency, throughput, run duration, failures, and variance | [Raw per-file/run CSV and JSON](issue32-artifacts/), [analysis summary](issue32-artifacts/analysis-summary.json), five paired runs per setting | Complete |
| Client/server memory observations | 48 OS-memory JSON records, worker heap observations, [run CSV](issue32-artifacts/analysis-runs.csv), sampler gaps | Complete for observed process memory; server JVM heap peak open |
| Reproducible raw evidence and analysis | [Runner](run-issue32.ps1), [analysis code](analyze-issue32.py), [verifier](verify-issue32.py), 438-entry [manifest](issue32-artifacts.sha256), independent hash check | Complete on the documented Windows host |
| Secret-free published artifacts | Restricted temporary fixtures, generated credentials, artifact verifier's secret/path marker scan | Complete for checked artifacts; no claim about arbitrary undetected content |
| General scalability, cryptographic strength, production suitability, or direct external-system comparison | No such measurements were run | Not claimed |

## Risks and limitations

The A/B paths differ in their transport adapters and certificate admission;
even with common backend and envelope code, the paired difference cannot
isolate TLS record encryption, handshake, server processing, or JVM effects
perfectly. The official high-level file methods passed a setup parity smoke,
while timed workers composed the same public key/unwrap APIs and production
envelope helper to expose component intervals. This instrumentation does not
time the high-level methods verbatim. Warm-ups used short-lived JVMs and did
not warm measured worker instances. The study used one Windows host, local
loopback and NTFS, five runs per setting, a scheduled arrival rate, and no
WAN, distributed storage, larger files, or external KMS. Measured pre-run CPU
load ranged from 0% to 31%, so the host was not uniformly idle. Cross-process
UTC timing and 100 ms memory sampling have the stated precision and gaps.
The observed data support only the reported synthetic, fixed-host behavior.
