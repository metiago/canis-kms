# Issue #35 consolidated CANIS research evidence

**Evidence cut:** repository records dated 2026-11-16 to 2026-11-18. **Type:** retrospective synthesis; no Issue #35 experiment or new measurement. [Synthesis method](issue35-method.md). The source studies used different revisions and hosts or source states. Their counts are presented separately and are never pooled into a reliability estimate.

Interpret the historical findings within the current [organization-managed deployment and
Trusted Computing Base](../threat-model.md#trust-assumptions-and-trusted-computing-base). The tests
do not establish protection against privileged host administrators, a compromised operating
system, or public cloud KMS threat models; this scope note adds no experimental evidence.

The later [JOSS-02 audit (archived)](repository-cleanup-report.md#archived-files) and [release-boundary matrix (archived)](repository-cleanup-report.md#archived-files) review the subsequently revised manuscript. This Issue #35 synthesis and its original claim matrix retain their historical evidence cut.

## 1. Executive summary

The [prespecified research question](issue31-protocol.md#research-question-and-operational-motivation) asks how correctness, resource use, and availability change when Java services exchange encrypted files asynchronously with CANIS under its documented shared-trust model. The measured workload is synthetic: 100 files per normal run, with 50 × 4 KiB, 35 × 256 KiB, and 15 × 4 MiB; planned 500 ms producer starts; a 0 or 60 s consumer-start delay after first complete publication; and one or four consumer workers. These settings exercise a bounded exchange pattern. They are not measured properties of the historical deployment or an operational service-level target.

For the supported path, the producer encrypts a file locally, publishes a complete encrypted envelope to shared storage, and the later consumer asks the TLS-protected key service to recover its data key. TLS client-certificate and configured credential checks admit clients. Once admitted, clients can request recovery under any stored key name; a key name is not a per-service authorization rule. The observed boundary tests confirm this behavior, including a separately admitted observer's successful recovery of another recipient's file. That outcome is consistent with the stated policy, not evidence of a bypass. [JOSS-01 result](joss01-results.md#observed-evaluation); [Issue #33 result](issue33-results.md#actual-scenario-observations).

The completed evidence includes a reproducible process-harness smoke test, a Windows memory preflight, an absolute CANIS baseline, a fixed-revision Windows TLS-path characterization, five functional multi-service scenarios, and selected storage/audit fault cases. It supports bounded statements about those observed fixtures. The original local AES-GCM/local-envelope controls and scheduled service-outage series were not executed. No general availability, scalability, production-readiness, security-strength, or external-adoption conclusion follows. [Issue #31 protocol](issue31-protocol.md#baselines-and-what-they-establish); [Issue #34 result](issue34-results.md#reproducibility-and-acceptance-review).

### Evidence inventory

| Experiment | Status | Evidence |
| --- | --- | --- |
| JOSS-01 shared-trust boundary | Focused categorical test passed; 2026-11-17 rerun on recorded commit, with unrelated working-tree edits | [Method](joss01-method.md), [result and Surefire record](joss01-results.md#focused-rerun-on-2026-11-17) |
| CODE-02 isolated exchange harness | Functional smoke complete; Windows negative cases and clean-checkout Ubuntu normal CI run | [Method](code02-method.md), [local and CI results](code02-results.md), [CI job cited in result](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) |
| CODE-03 whole-file preflight | Local Windows preflight complete within 4 MiB/four-worker/512 MiB-heap setting; larger sizes untested | [Method](code03-method.md), [result](code03-results.md), [run](code03-runs.csv) and [file](code03-files.csv) rows |
| Issue #31 CANIS baseline | Four warm-ups and 20 measured path-C runs passed on a dirty Windows working tree; original three-path study incomplete | [Protocol](issue31-protocol.md), [method](issue31-baseline-method.md), [result](issue31-baseline-results.md), [run](issue31-baseline-runs.csv) and [file](issue31-baseline-files.csv) rows |
| Issue #32 TLS-path characterization | 20 matched measured A/B pairs passed on clean fixed revision `9b01048256a5bfe260aebfe0429f521282f6f475` | [Method](issue32-method.md), [result](issue32-results.md), [artifact set](issue32-artifacts/), [manifest](issue32-artifacts.sha256) |
| Issue #33 multi-service interoperability | Five final functional scenarios passed on a dirty Windows working tree | [Method](issue33-method.md), [result](issue33-results.md), [final artifact manifest](issue33-artifacts.sha256) |
| Issue #34 selected fault injection | Seven case variants × three repetitions passed their predefined checks on a dirty Windows working tree; full backlog fault matrix remains open | [Method](issue34-method.md), [result](issue34-results.md), [artifact set](issue34-artifacts/), [manifest](issue34-artifacts.sha256) |

## 2. System under study

The verified study path uses the Java client and packaged server as separate processes. The client obtains an RSA public key, generates a fresh AES data key, encrypts a file with AES-GCM, wraps the data key with RSA-OAEP, and writes a `CANISHYB1` envelope. For decryption, the client sends the wrapped data key to CANIS and uses the returned data key locally; bulk file bytes do not traverse the key-service connection in this workflow. The encrypted `CANISDB1` database holds server key material; a matching externally supplied master key is needed on restart. Whole-file reads and copies occur in the client envelope helper. These facts are documented by the [CODE-03 implementation map](code03-results.md#implementation-memory-map), the [CODE-02 process method](code02-method.md#process-and-fixture-layout), and the [Issue #34 restart observations](issue34-results.md#scenario-observations).

The official client/server path uses TLS 1.3, trusted client certificates, server-certificate and hostname validation, and configured credentials. The study's ordinary exchanges use generated identities and the official client. The Issue #32 non-TLS listener is a separately compiled, loopback-only research fixture and is **not** a supported product transport or deployment setting. Neither the file envelope nor the TLS admission check authenticates a file's producer to another admitted client. CANIS has no per-key authorization or built-in audit-log integrity verifier in the studied implementation. [CODE-02 method](code02-method.md#process-and-fixture-layout); [Issue #32 method](issue32-method.md#prespecification-and-scope); [Issue #33 result](issue33-results.md#interpretation-risks-and-future-work); [Issue #34 result](issue34-results.md#scenario-observations).

## 3. Experimental infrastructure

**CODE-02.** A research-only coordinator creates an isolated temporary root with private source/recovery files, an explicitly initialized encrypted database, and a shared directory restricted to complete `.canis` envelopes and transient `.part` files. It starts the packaged server, producer, and delayed consumer as distinct JVMs. Complete publication uses same-directory atomic rename; the consumer authenticates decryption before independent byte comparison. A two-file Windows normal run verified 2/2 files. Separate one-file Windows cases observed tag rejection, ignored incomplete publication, credential rejection, and certificate rejection with no recovered output. Two preliminary certificate-classification attempts failed the harness check and were retained rather than counted as passes. A clean-checkout Ubuntu CI normal run on commit `23a65838dc21bd02f0f4f9297dd222595cc88c97` passed the POSIX wrapper and uploaded an evidence artifact; its archive contents were not independently downloaded in the source review. This is functional infrastructure evidence, not a benchmark. [CODE-02 method](code02-method.md); [results](code02-results.md#commands-and-observed-outcomes); [CI record](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).

**CODE-03.** Source inspection found whole-file `Files.readAllBytes` and multiple file-sized arrays in the client. On one Windows host, one warm-up and two measured four-file runs at each of 4 KiB, 256 KiB, 1 MiB, and 4 MiB all published and verified their files with four consumer workers and 512 MiB child heaps. A separately prespecified supplementary 100-file schedule check verified 100/100 files with the Issue #31 size mix, 500 ms arrivals, and 60 s delay. The largest observed consumer `PeakWorkingSet64` among these runs was 248,823,808 bytes, in the supplementary check; that process value includes fixture and verification work, and it is not a JVM heap peak or a general bound. Files above 4 MiB, other hosts, lower heaps, and higher concurrency were not tested. No streaming change was triggered by this preflight. [CODE-03 method](code03-method.md); [result and raw table](code03-results.md#raw-observations).

## 4. Baseline characterization — Issue #31 only

The Issue #31 **absolute CANIS path-C** characterization used four 20-file warm-ups and 20 measured 100-file runs, five at each delay/worker setting. All 80 warm-up and 2,000 measured files authenticated and byte-matched. The table reproduces the recorded descriptive statistics; five run values per setting and all file rows remain in the [run CSV](issue31-baseline-runs.csv) and [file CSV](issue31-baseline-files.csv). Pooled file percentiles describe these files and are not independent run-level estimates. [Issue #31 result](issue31-baseline-results.md#observed-latency-throughput-and-variance).

| Consumer delay / workers | Median files/s [five-run range] | Processing p50 / p95 (ms) | End-to-end p50 / p95 (ms) |
| --- | ---: | ---: | ---: |
| 0 s / 1 | 2.0175 [2.0161, 2.0185] | 24.717 / 162.808 | 83.217 / 596.317 |
| 0 s / 4 | 2.0151 [1.9999, 2.0165] | 52.574 / 440.935 | 160.386 / 1037.528 |
| 60 s / 1 | 1.4541 [1.4492, 1.5493] | 27.075 / 369.542 | 39,495.842 / 59,184.114 |
| 60 s / 4 | 1.5925 [1.5877, 1.5956] | 27.733 / 172.630 | 37,305.157 / 59,153.990 |

Throughput uses the interval from first planned producer start to last verification, so the arrival schedule and deliberate 60 s delay affect it. The consumer peak working-set ranges by setting are recorded as 166,367,232–184,246,272; 191,537,152–207,089,664; 173,191,168–202,821,632; and 205,897,728–254,959,616 bytes, in the table's order. These are Windows process high-water marks, not simultaneous totals or peak JVM heap. This campaign did not execute local AES-GCM or local-envelope controls, randomized setting order, or all planned CPU, I/O, queue, and JVM memory instruments. It cannot isolate key-service cost or establish unconstrained throughput. Its source tree was dirty. [Issue #31 memory record](issue31-baseline-results.md#memory-observations); [deviations](issue31-baseline-results.md#reproduction-and-acceptance-review).

## 5. TLS characterization — Issue #32 only

The Issue #32 fixed-revision Windows campaign paired the supported TLS 1.3/mTLS path **A** with a loopback-only research plain-CANISP fixture **B** using the same synthetic input hashes and encrypted database snapshot within each pair. Four 20-file warm-up pairs preceded 20 measured 100-file pairs. Each arm verified 2,000/2,000 measured files, with zero measured failed files. The original Issue #31 protocol's A/B labels refer to different local controls; this comparison is a documented change of question, not completion of those controls. [Issue #32 method](issue32-method.md#fixed-workload-and-comparison); [result](issue32-results.md#result-and-scope).

| Delay / workers | A run-median end-to-end latency (ms) | B run-median end-to-end latency (ms) | Median paired A−B latency [range] (ms) | A / B median files/s |
| --- | ---: | ---: | ---: | ---: |
| 0 s / 1 | 163.151 | 153.561 | +13.334 [−60.833, +28.475] | 2.0160 / 2.0163 |
| 0 s / 4 | 162.818 | 123.102 | +4.799 [−29.430, +56.999] | 2.0160 / 2.0165 |
| 60 s / 1 | 40,854.548 | 40,436.082 | +552.244 [−13.715, +913.333] | 1.4236 / 1.4388 |
| 60 s / 4 | 37,696.331 | 37,256.833 | +503.542 [+294.816, +675.945] | 1.5800 / 1.5932 |

These are the recorded medians of five run medians and within-pair differences, not a subtraction of group medians. The measured differences include certificate admission, TLS processing, and adapter effects; they do not isolate TLS handshake or record-encryption cost. The separately recorded producer connection-plus-login paired medians were +538.745, +567.823, +742.185, and +668.839 ms in table order. The server process peak-working-set paired medians were +17.07, +18.37, +14.35, and +14.77 MiB. Connection-plus-login includes more than handshake, and process memory includes startup. Five pairs per setting, one Windows loopback host, short-lived warm-up JVMs, host-load variation, and sampler gaps limit generalization. [Issue #32 timing](issue32-results.md#observed-latency-and-throughput); [memory](issue32-results.md#observed-memory); [limits](issue32-results.md#risks-and-limitations).

## 6. Multi-service interoperability — Issue #33 only

Five final Windows functional scenarios passed with separate packaged server and Java worker processes, generated certificates, and the official TLS client. Across **this series only**, the records contain ten unique complete publications, eleven expected positive recovery operations, three expected failed decrypt attempts, one rejected duplicate publication, and one successful replay recorded separately from unique recovery. [Issue #33 result](issue33-results.md#result-and-evidence-boundary).

| Scenario | Recorded observation | Boundary |
| --- | --- | --- |
| A: delayed exchange | Two unique files recovered after a 60,011 ms observed delay; duplicate final name rejected without changing envelope hash; encrypted `.part` decoy ignored; replay of a valid envelope succeeded | Duplicate and incomplete-file handling belongs to the research harness, not a general delivery service |
| B: multiple routes | Two producers and two recipients with distinct process IDs, certificates, and key names recovered four route-correct files | Java/local-host functional routing only |
| C: controlled restart | Two files published before stop; new server process used the same intact database and key and recovered both after restart and 60,010 ms delay | No crash, interrupted write, or availability-duration measurement |
| D: shared trust | Recipient and separately certificated/credentialed observer recovered one file; altered password and untrusted certificate were rejected without output | Observer success follows the shared-access policy |
| E: tag tampering | Original file verified; a one-byte tag alteration was rejected without recovered output | One tested alteration, not general attack resistance |

The final records were produced on a dirty working tree; POSIX and independent clean-checkout Issue #33 reruns were not recorded. The verifier checks the retained event/worker relationships, not removed private fixtures. [Issue #33 scenario details](issue33-results.md#actual-scenario-observations); [acceptance and limits](issue33-results.md#acceptance-review).

## 7. Fault-injection results — Issue #34 only

The final Windows campaign executed seven storage/audit case variants three times each. Each invocation first created and byte-verified an isolated baseline envelope. All 21 final runs passed their *scenario-specific checks*. A passing scenario may include an intentionally failed request; it does not mean every operation succeeded. All failed recovery attempts in the final series left no output file for that attempt. Client failures were reported with a generic sanitized category; server markers provide narrower observations. [Issue #34 result](issue34-results.md#scenario-observations).

| Fault and repetitions | Observed behavior |
| --- | --- |
| Master-key file removed while running (3) | Live publication and recovery succeeded with the running server's cached key; restart rejected the missing key; exact key restoration allowed recovery. |
| Master-key file replaced while running (3) | Live publication and recovery succeeded; restart rejected the original database under the replacement key; matching key restoration allowed recovery. |
| Encrypted database altered while live (3) or stopped (3) | Live requests failed after a changed-database marker; damaged-database restarts were rejected; exact database restoration allowed recovery. |
| One audit line modified (3) or deleted (3) | Independent file SHA-256 changed, but CANIS restarted and the file workflow continued; no built-in audit-integrity rejection was recorded. The altered audit log was not restored. |
| Database corrupted, then exact backup restored (3) | Damaged restart rejected; restart without reinitialization after matching database/key restoration recovered the original envelope. |

The result establishes no internal fault-detection latency, automatic recovery, crash durability, or deployment backup procedure. In particular, the source protocol's scheduled 30 s service outages, key deletion, envelope/admission matrix, interrupted writes, and longer outages were not run in this campaign. The source tree was dirty, POSIX execution was not recorded, and network isolation beyond local clients was not independently verified. [Issue #34 limits](issue34-results.md#limits-and-operational-impact); [backlog scope (archived)](repository-cleanup-report.md#archived-files).

## 8. Shared-trust boundary evaluation — JOSS-01 only

The predefined JUnit scenario encrypted a synthetic file under the recipient key with the official TLS client. Both the recipient and a distinct authenticated **credential identity** named observer recovered byte-identical content; a deliberately altered observer password was rejected. The focused 2026-11-17 rerun recorded one test, zero failures, errors, or skips at source commit `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91`, with unrelated backlog edits in the working tree. The test used the **same client certificate** for all credential identities, so it isolates credential-level shared access and does not establish certificate-bound separation. No performance, adoption, cryptographic-strength, or broader authorization result follows. Issue #33 separately exercised distinct client certificates but is not a repetition of this JOSS-01 test. [JOSS-01 method](joss01-method.md); [focused rerun](joss01-results.md#focused-rerun-on-2026-11-17).

The [JOSS-01 historical evidence record](joss01-results.md#historical-operational-evidence-and-research-motivation) describes a dated letter reporting production use of a **predecessor** KMS and the authors' statement of continuity. The letter does not name this revision, prove compatibility with that deployment, or document external adoption of this current code. Its untracked source and publication permission remain separate editorial matters.

## 9. Reproducibility

### Recorded environment and source state

| Study | Source state for reported result | Platform and principal limit |
| --- | --- | --- |
| JOSS-01 | Focused rerun at `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91`; unrelated backlog edits | Windows 11; Maven 3.9.12 on Adoptium Java 21.0.6; one certificate across credentials |
| CODE-02 | Local Windows runs had uncommitted harness files; Ubuntu CI normal run checked out `23a65838dc21bd02f0f4f9297dd222595cc88c97` | Windows negative smoke; Ubuntu clean-checkout POSIX normal smoke, not a measurement series |
| CODE-03 | `0be60db575c5952f6cdddede21b20d135d093e07` plus uncommitted research files | Windows 11, i7-1355U, local NTFS, Oracle Java 21.0.7 child JVMs, 512 MiB child heaps |
| Issue #31 baseline | `f00310ee3678f8c96fc1a8681f35896d5163d3de` plus uncommitted research files | Same reported Windows host class, local NTFS/loopback, Oracle Java 21.0.7 children, 512 MiB child heaps |
| Issue #32 | Clean `9b01048256a5bfe260aebfe0429f521282f6f475` | Windows 11 NT 10.0.26100.0, i7-1355U, 12 logical processors, 16,324,521,984 bytes RAM, local NTFS/loopback, Oracle Java 21.0.7 children, Maven 3.9.12 on Adoptium Java 21.0.6, 512 MiB child heaps |
| Issue #33 | `d3e89d7973ab5b936475dd668b780aef03901847` plus uncommitted research files | Windows 11/loopback; Java 21.0.7 children; filesystem type and negotiated suite not recorded for that run |
| Issue #34 | `4f6de0568b5ccc6a2947819333d07aec2ba6a3cb` plus uncommitted research files | Windows 11/loopback; Java 21.0.7 children; current filesystem/RAM details and negotiated suite not recorded |

These entries reproduce the source records' environment fields; they do not imply that all studies used an identical binary. [JOSS-01](joss01-results.md#focused-rerun-on-2026-11-17), [CODE-02](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17), [CODE-03](code03-results.md#measurement-protocol-and-environment), [Issue #31](issue31-baseline-results.md#environment-and-execution-record), [Issue #32](issue32-results.md#fixed-source-environment-and-execution), [Issue #33](issue33-results.md#environment-source-and-execution), [Issue #34](issue34-results.md#environment-and-execution).

### Recorded artifact identifiers

These identifiers are transcribed from the source result records; Issue #35 did not recompute them. Each linked manifest contains per-artifact SHA-256 values. New executions generate new cryptographic fixtures and therefore different run-artifact hashes.

| Study | Retained artifact and recorded identifier |
| --- | --- |
| JOSS-01 | Final test-source SHA-256 `71FB34494FEC157CA15EA591278AA7421AD455619DDA1E36560A238F1BBB622C`; the rerun's Surefire XML was generated under `server/target/`, not retained as a published research artifact. [Record](joss01-results.md#focused-rerun-on-2026-11-17). |
| CODE-02 | Final local normal JSON SHA-256 `4646EBAC91387FA5DB1D7A0F6676001C4A875D4C3520E61C8FD32350EB307E01`; the Ubuntu CI evidence artifact was listed as ID `14381849411`, with archive contents uninspected in the source review. [Local record](code02-results.md#commands-and-observed-outcomes); [CI record](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17). |
| CODE-03 | [Run CSV](code03-runs.csv) SHA-256 `8A591966DAAC7CFFB20CEC7BD732D7CDD2085530915846892747B833D65019A2`; [file CSV](code03-files.csv) SHA-256 `3922169C07BAB80B90B5CE6CF47C4D8C15C10258C104B7801BA6773DBFC51C0F`. [Record](code03-results.md#reproduction-acceptance-and-remaining-risks). |
| Issue #31 | [Run CSV](issue31-baseline-runs.csv) SHA-256 `78ACA18C8B1F44F5484D542259E9DEE4F5DA6F4A464414614E98BD4278B2693B`; [file CSV](issue31-baseline-files.csv) SHA-256 `EDCF1041D262322A2BE932D892BE4C1E20D6183A95FF14A14250E64E5A3D8D8F`. [Record](issue31-baseline-results.md#reproduction-and-acceptance-review). |
| Issue #32 | [438-entry manifest](issue32-artifacts.sha256) SHA-256 `d9d6904f09bc6edf7cca33b50d74e8cada092f079f266be928828756f1328e9b`. [Record](issue32-results.md#reproduction-verification-and-preliminary-attempts). |
| Issue #33 | [28-entry final manifest](issue33-artifacts.sha256) has each final JSON/CSV digest; the result record does not state one digest for the manifest file itself. [Record](issue33-results.md#actual-scenario-observations). |
| Issue #34 | [159-entry final manifest](issue34-artifacts.sha256) SHA-256 `97F030795D15BF6B55B6B128BA5F3C1BF0552DB5A48624F9DFFD653B5010A557`. [Record](issue34-results.md#environment-and-execution). |

### Commands and expected evidence

From the repository root, with the JDK, Maven, Git, permissions, local port, and host network controls specified in each method, an independent reader can use the following **source-record commands**. They are reproduction instructions, not commands executed for Issue #35. Use fresh output directories for Issue #32 and inspect private failed fixtures before cleanup. Some runs need Windows permission to create owner-only ACLs. [JOSS-01](joss01-method.md#reproduction); [CODE-02](code02-method.md#build-and-run); [CODE-03](code03-method.md#execution-expected-outputs-and-cleanup); [Issue #31](issue31-baseline-method.md#reproduction); [Issue #32](issue32-method.md#reproduction-evidence-and-cleanup); [Issue #33](issue33-method.md#reproduction-output-and-cleanup); [Issue #34](issue34-method.md#reproduction-and-cleanup).

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario normal --files 1 --size 4096 --delay-ms 1000
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code03.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue31-baseline.ps1
mvn --batch-mode --no-transfer-progress clean verify
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue32.ps1 -Scope campaign -OutputDirectory server/target/issue32-new-series -ExportDirectory docs/research/issue32-new-artifacts
python docs/research/analyze-issue32.py docs/research/issue32-new-artifacts
python docs/research/verify-issue32.py docs/research/issue32-new-artifacts --scope campaign
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario A
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario B
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario C
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario D
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue33.ps1 --scenario E
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34-campaign.ps1
```

The JOSS-01 runner expects a one-test Surefire pass. CODE-02 normal prints `CODE02_RESULT=passed`; its POSIX wrapper passed once in the recorded Ubuntu CI job. CODE-03 prints its preflight marker and exports [run](code03-runs.csv)/[file](code03-files.csv) CSVs. Issue #31 prints `ISSUE31_BASELINE=passed` and exports [run](issue31-baseline-runs.csv)/[file](issue31-baseline-files.csv) CSVs. Issue #32 exports the [438-file final artifact set](issue32-artifacts/) and a [438-entry manifest](issue32-artifacts.sha256). Issue #33 final records comprise [28 JSON/CSV artifacts](issue33-artifacts.sha256). Issue #34 final records comprise [159 JSON/CSV artifacts](issue34-artifacts.sha256). Source hashes, generated jar hashes, individual run IDs, and cleanup rules are in each study record. Random fixtures make new run-file hashes differ. [CODE-03 result](code03-results.md#reproduction-acceptance-and-remaining-risks); [Issue #31 result](issue31-baseline-results.md#reproduction-and-acceptance-review); [Issue #32 result](issue32-results.md#reproduction-verification-and-preliminary-attempts); [Issue #33 result](issue33-results.md#actual-scenario-observations); [Issue #34 result](issue34-results.md#reproducibility-and-acceptance-review).

### Existing artifact verification performed for this synthesis

The following read-only checks ran on the retained repository artifacts; no workload runner was invoked and no manifest was rewritten:

```text
python docs/research/verify-issue32.py docs/research/issue32-artifacts --manifest docs/research/issue32-artifacts.sha256
  ISSUE32_EVIDENCE=passed pairs=24 file_rows=4160
python docs/research/verify-issue33.py
  ISSUE33_EVIDENCE=passed scenarios=5
python docs/research/verify-issue34.py docs/research/issue34-artifacts
  ISSUE34_EVIDENCE=passed cases=7 repetitions=3 artifacts=159
```

The Issue #32 verifier's `pairs=24` includes four warm-up pairs plus 20 measured pairs; `file_rows=4160` includes warm-up and measured rows. These checks validate artifact structure, relationships, and selected secret markers according to their scripts; they do not independently rerun cryptography. The CODE-03 published CSV hashes, Issue #31 published CSV hashes, and Issue #32/#33/#34 manifest digests are recorded in the respective results and are available for separate hash verification. The CODE-02 CI archive had seven-day retention and was not independently downloaded in the cited review, so the job result and artifact listing, rather than archive contents, are the recorded CI evidence. [Issue #32 verifier and manifest](issue32-results.md#reproduction-verification-and-preliminary-attempts); [Issue #33 verifier](issue33-results.md#actual-scenario-observations); [Issue #34 verifier](issue34-results.md#result-and-scope); [CODE-02 CI limit](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).

## 10. Threats to validity

- **Single-host measurement:** CODE-03, Issue #31, and Issue #32 memory/timing values came from one Windows laptop-class host, local NTFS, loopback networking, and specific JDK/JVM settings. They do not estimate capacity on another host or network. Issue #33 and #34 were also local Windows functional/fault runs. Only CODE-02 has a recorded clean-checkout Ubuntu functional smoke run. [CODE-03 environment](code03-results.md#measurement-protocol-and-environment); [Issue #32 limits](issue32-results.md#risks-and-limitations).
- **Workload limits:** Normal runs used 100 scheduled files, at most 4 MiB each, one producer, one recipient process, one or four consumer workers, and 0/60 s delays. Issue #33 used small functional scenarios; Issue #34 used short selected faults. Arrivals bound observed throughput. No WAN, distributed shared storage, larger files, higher concurrency, long retention, or sustained outage was measured. [Issue #31 protocol](issue31-protocol.md#fixed-synthetic-workload); [Issue #34 limits](issue34-results.md#limits-and-operational-impact).
- **Synthetic fixtures:** Payloads, databases, keys, credentials, certificates, and faults were generated for the studies. The historical predecessor letter supplies motivation, not measured customer traffic or a mapped current-code deployment. [Issue #31 protocol](issue31-protocol.md#research-question-and-operational-motivation); [JOSS-01 historical record](joss01-results.md#historical-operational-evidence-and-research-motivation).
- **Windows bias and instrumentation:** All performance/resource and Issue #33/#34 fault observations are Windows-based. Process peak working set includes startup and fixture activity. Sample gaps, cross-process wall-clock timing, cold measured JVMs, and Issue #31's grouped execution order limit causal attribution. Server JVM peak heap and some planned resource instruments are absent. [Issue #31 limits](issue31-baseline-results.md#reproduction-and-acceptance-review); [Issue #32 limits](issue32-results.md#risks-and-limitations).
- **Trust and comparator assumptions:** Shared trust permits cross-key recovery by an admitted client. JOSS-01 shares a test certificate across credentials; Issue #33 uses separate certificates. The Issue #32 plain loopback research fixture differs from the TLS arm in more than TLS record processing. Neither set tests per-key isolation, producer authenticity, hostile network exposure, or another KMS. [JOSS-01 method](joss01-method.md); [Issue #32 method](issue32-method.md#prespecification-and-scope); [Issue #33 result](issue33-results.md#interpretation-risks-and-future-work).
- **Source-state limits:** Issue #32 alone has a clean fixed revision for its measured campaign. CODE-02 has clean Ubuntu smoke evidence, but the CODE-03, Issue #31, #33, and #34 final local runs used dirty working trees. An artifact verifier cannot establish an independent fixed-release rerun. [Evidence inventory](#evidence-inventory).

## 11. Limitations

The record supports **no scalability claim**, because no unconstrained load or multi-host scale series was run. It supports **no availability claim**, because scheduled service outages and success-over-window measurements from the original protocol were not executed. It supports **no production-readiness claim**, because the tested environments and fixtures are limited and the CODE-01 fixed-release gate remains open. It supports **no security-strength claim**: successful rejection of selected tampering and credentials does not establish attack resistance, and the audit-file fault shows no built-in integrity verification. It supports **no external-adoption claim for the current repository**: the historical letter concerns a predecessor, and no current-code external deployment is evidenced. [Backlog Phase 1/2 gates (archived)](repository-cleanup-report.md#archived-files); [JOSS-01 historical record](joss01-results.md#historical-operational-evidence-and-research-motivation).

The original Issue #31 local AES-GCM/local-envelope controls, paired C-minus-local-envelope comparison, planned CPU/I/O/queue instruments, and scheduled 30 s outage placements remain unexecuted. Issue #34's selected storage/audit faults do not substitute for its full outage, key-deletion, envelope, and admission matrix. The [manuscript](../../joss-papers/paper.md) now describes bounded Issue #31 and #32 results as observed and keeps those missing conditions as future work. The [manuscript claim matrix (archived)](repository-cleanup-report.md#archived-files) maps its assertions to fixed repository evidence artifacts and records the original execution states. This mapping does not turn dirty-tree runs into fixed-revision executions. The manuscript's fixed-release and author-metadata gates remain separate. [Issue #31 protocol](issue31-protocol.md#baselines-and-what-they-establish); [Issue #34 result](issue34-results.md#reproducibility-and-acceptance-review); [backlog Issue #35 (archived)](repository-cleanup-report.md#archived-files).

### Consistency review

The retained observations show no direct outcome contradiction when their conditions are kept distinct. JOSS-01's observer shared a client certificate with the recipient, while Issue #33's observer had a distinct admitted certificate; both recovered under the same documented shared-access policy. Issue #33 restarted against an unchanged encrypted database and matching key, while Issue #34 rejected damaged or mismatched startup material. Issue #34's independent detection of changed audit-file bytes coexists with its observed lack of application audit-integrity rejection. Issue #31's absolute CANIS rates and Issue #32's paired TLS-path rates were collected in different campaigns, so neither validates a cross-campaign effect size. The Issue #32 non-TLS research fixture must never be presented as a supported plaintext mode under the project's TLS-only rule. [JOSS-01 result](joss01-results.md#observed-evaluation); [Issue #33 scenarios](issue33-results.md#actual-scenario-observations); [Issue #34 scenarios](issue34-results.md#scenario-observations); [Issue #32 scope](issue32-results.md#result-and-scope).

### Claim-to-evidence traceability

The table below traces the synthesis's principal conclusions. The [manuscript claim matrix (archived)](repository-cleanup-report.md#archived-files) separately covers every material statement in `paper.md`, with evidence-artifact and study-execution revisions and reproduction routes.

| Claim | Evidence |
| --- | --- |
| The research-only exchange harness completed a delayed separate-process normal exchange and locally rejected selected negative inputs | [CODE-02 method](code02-method.md), [Windows outcomes](code02-results.md#commands-and-observed-outcomes), [Ubuntu CI smoke](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) |
| The planned 4 MiB/four-worker configuration completed a bounded Windows preflight | [CODE-03 method](code03-method.md), [observed run table](code03-results.md#raw-observations), [run CSV](code03-runs.csv) |
| The absolute CANIS baseline verified 2,000 measured synthetic files across 20 runs | [Issue #31 method](issue31-baseline-method.md), [result](issue31-baseline-results.md#result-and-boundary), [run CSV](issue31-baseline-runs.csv), [file CSV](issue31-baseline-files.csv) |
| A fixed-revision Windows TLS-path series observed 20 matched A/B measured pairs with 2,000 verified files per arm | [Issue #32 method](issue32-method.md), [result](issue32-results.md#result-and-scope), [analysis pairs](issue32-artifacts/analysis-pairs.csv), [manifest](issue32-artifacts.sha256) |
| Two routes, delayed recovery, controlled intact-database restart, shared access, and tag rejection passed their five functional scenarios | [Issue #33 method](issue33-method.md), [scenario results](issue33-results.md#actual-scenario-observations), [manifest](issue33-artifacts.sha256) |
| The selected master-key/database faults rejected damaged restarts; external audit hashes changed without a built-in audit-integrity rejection | [Issue #34 method](issue34-method.md), [scenario results](issue34-results.md#scenario-observations), [manifest](issue34-artifacts.sha256) |
| A second admitted credential identity recovered the recipient file in the focused boundary test | [JOSS-01 method](joss01-method.md), [rerun record](joss01-results.md#focused-rerun-on-2026-11-17) |
| The original three-path comparison and scheduled outages are **not** completed results | [Issue #31 protocol](issue31-protocol.md#baselines-and-what-they-establish), [Issue #32 comparator definition](issue32-method.md#prespecification-and-scope), [Issue #34 acceptance limit](issue34-results.md#reproducibility-and-acceptance-review) |

## 12. Conclusions

On the recorded synthetic fixtures, CANIS's official TLS file workflow published complete encrypted envelopes and recovered byte-identical content in the bounded normal and functional scenarios. The focused trust-boundary test and the distinct-certificate functional scenario observed the documented shared-access behavior. The Windows baseline and paired transport series provide descriptive latency, throughput, and process-memory observations for their specific schedules. Selected database/master-key faults prevented damaged restarts or requests while exact fixture restoration permitted recovery; selected audit-line changes were visible to an external hash comparison but did not cause a built-in integrity rejection. These statements apply to their individual studies and source states. They do not answer the original availability question or establish general system suitability. [Issue #31](issue31-baseline-results.md); [Issue #32](issue32-results.md); [Issue #33](issue33-results.md); [Issue #34](issue34-results.md); [JOSS-01](joss01-results.md).

### Issue #35 acceptance review

| Deliverable | Evidence | Status |
| --- | --- | --- |
| Consolidated methods, actual results, interpretation, negative observations, discussion, and limitations | This report and [synthesis method](issue35-method.md), with links to seven final source-study records | Complete as a bounded retrospective synthesis |
| Claim-to-evidence table | [Synthesis matrix](#claim-to-evidence-traceability) above and [manuscript claim matrix (archived)](repository-cleanup-report.md#archived-files) | Complete as an artifact map; dirty-run execution traceability remains open |
| Exact reproduction commands, fixture rules, environment, platform limits, and artifact locations | [Reproducibility](#9-reproducibility) and individual source methods/results | Complete as a guide; several study executions remain working-tree-only |
| Existing artifact integrity and relationship checks | Read-only Issue #32/#33/#34 verifier passes above; source records retain other hashes and validation outcomes | Complete for those retained artifact sets; not an independent rerun |
| Every **paper** claim traced to fixed-revision results and reproduction steps | [Manuscript claim matrix (archived)](repository-cleanup-report.md#archived-files) gives a fixed repository evidence-artifact revision and reproduction route per assertion; only Issue #32 has a clean fixed-revision measured campaign, and several other studies ran on dirty trees | **Partial: artifact traceability complete; fixed-execution acceptance open** |
| Original question's correctness, resource-use, and availability evidence | Bounded correctness/resource observations; no scheduled outage series or availability-window result | **Partial; availability open** |
| Full Issue #34 backlog fault matrix and fixed/POSIX reproduction | Selected 21-run storage/audit campaign only | **Open** |
| Release/manuscript readiness | CODE-01 fixed-revision gate, independent reproduction, canonical paper build, and author metadata/disclosures remain separate backlog work; the claim-alignment draft is updated | **Open** |

This acceptance review does not mark the parent Phase 2 or JOSS submission gate complete. The report documents completed bounded observations; the stronger Issue #35 backlog criterion remains open until fixed-revision execution traceability and the missing original-question evidence exist. [Backlog Issue #35 (archived)](repository-cleanup-report.md#archived-files); [manuscript](../../joss-papers/paper.md).
