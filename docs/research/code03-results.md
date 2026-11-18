# CODE-03 whole-file memory preflight: Windows, 2026-11-18

## Decision and evidence scope

The existing client completed two measured four-file exchanges at each of
4 KiB, 256 KiB, 1 MiB, and 4 MiB with four consumer workers and a 512 MiB
heap per worker process. Each attempt published and authenticated all four
files and verified byte equality. A supplementary single 100-file check using
the Issue #31 50/35/15 size mix, 500 ms arrival interval, 60 s consumer delay,
and four workers verified 100/100 files. The largest individually tested file
was **4 MiB**. This establishes a bounded preflight for the planned synthetic
campaign on this host; it does not establish a safe maximum or general
large-file scalability. CODE-03 does not call for streaming on this evidence.

The [protocol](code03-method.md) was written before executing workloads. The
100-file check was added in a dated amendment before the 4 MiB measured
repetitions and before that check was executed. Source HEAD was
`0be60db575c5952f6cdddede21b20d135d093e07`; the research documents and
runner were uncommitted during execution (`dirty_tree: true`). Production
Java and the existing `Code02Harness.java` were unchanged. The harness source
SHA-256 was `A4CF649F29B1A23BF9B0AC8910335AEE5299369D85644D5C0CE1DEADC8C31B3D`;
the packaged server jar SHA-256 was
`5C1D96B04F7AB4B2583E938DFD309B20E448603DCAD909639F6CB0D4AF9CC113`.

## Implementation memory map

| Component | File | Memory-relevant behavior |
| --- | --- | --- |
| Official client entry points | `client/src/main/java/io/canis/jpaw/client/JpawClient.java` | `encryptFile` fetches a public key, then invokes the envelope helper; `decryptFile` invokes the helper with a TLS unwrap callback. The service exchanges a wrapped data key, not file bytes. Base64 command/response copies are limited to that wrapped key. |
| Client whole-file encryption | `client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java:55` | `Files.readAllBytes` loads the plaintext. AES-GCM `doFinal` allocates ciphertext plus tag; `Arrays.copyOf` creates another full ciphertext; `ByteArrayOutputStream` holds the envelope and `toByteArray()` copies it again before `Files.write`. Several file-sized arrays can coexist. |
| Client whole-file decryption | `client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java:63` | `Files.readAllBytes` loads the full envelope. Parsing copies ciphertext to another array, then copies it with its tag to a third array. GCM `doFinal` allocates plaintext. Output is written only after authentication succeeds. |
| Legacy server-side file helpers | `server/src/main/java/io/canis/crypto/Cryptographer.java:51` | `encryptFile`/`decryptFile` use `Files.readAllBytes`, whole-array GCM, envelope assembly/parsing, and file-sized copies analogous to the client. These helpers are not used by the official CODE-02 exchange path. |
| Server encrypted database | `server/src/main/java/io/canis/store/KeyValueStore.java:119`, `:213` | Startup, integrity checks on requests, and writes read or serialize the encrypted database into memory; `readDatabase` caps input at 256 MiB. This depends on database size, not exchanged file size. It is not a client envelope limit. |
| Database AES-GCM | `server/src/main/java/io/canis/crypto/SymmetricGenerator.java:44`, `:63` | Whole-array `doFinal` creates encrypted or decrypted database copies. It is independent of exchanged file size. |
| Wrapped-key protocol buffers | `client/src/main/java/io/canis/jpaw/client/SocketClient.java:119`, `server/src/main/java/io/canis/handlers/ClientHandler.java:239` | Allocate request/response bytes for the wrapped data key, not for the file payload, in the official workflow. |
| Master-key fixture loading | `server/src/main/java/io/canis/crypto/MasterKeyLoader.java:67` | Uses bounded `readNBytes` for the externally supplied key file; not file-payload-sized. |
| Research producer and verifier | `docs/research/Code02Harness.java:323`, `:419` | Producer first creates a full synthetic payload array, then the client loads it again. Consumer generates an independent full expected array and calls `Files.readAllBytes` on recovered output for byte equality. These make the observed worker peaks conservative for the client path. The harness's tamper-only branch also reads an entire envelope, but was not used in normal preflight runs. |

Tests have additional `readAllBytes` calls on test
fixtures; they are not part of the measured process exchange.

## Measurement protocol and environment

The [method](code03-method.md) fixes the host, source, heap settings, synthetic
fixtures, matrix, one four-file warm-up, two measured repetitions per size,
100-file supplementary check, memory instruments, and stopping rules. The host
reported Windows 11 Enterprise build 26100.9911, Intel Core i7-1355U, 12
logical processors, 16,324,521,984 bytes physical RAM, and local NTFS on `C:`.
Available physical RAM was 2,426,916,864 bytes and free disk space was
24,351,444,992 bytes at protocol capture; these are point observations, not
minimum headroom during the run. Oracle Java 21.0.7 executed the harness and
children; Maven 3.9.12 used Eclipse Adoptium Java 21.0.6. Each server,
producer, and consumer used `-Xms128m -Xmx512m`. TLS stayed at the existing
TLS 1.3 policy; generated certificates and passwords stayed in isolated
private temporary roots. The workload was synthetic, with no customer data.

The OS high-water mark is Windows `Get-Process PeakWorkingSet64`, selected by
the server, producer, and consumer PIDs recorded in each CODE-02 JSON. The
runner sampled at approximately 100 ms; the observed maximum sample gap was
465 ms in the warm-up and 241 ms among measured attempts. The OS field is a
process working-set peak, not a JVM heap peak. The JVM fields are **heap used
at worker exit**, not peaks. The observed `Start-Process` object did not
provide a coordinator exit code (`null` in the OS records); the result JSON,
per-file rows, and runner completion establish the stated outcomes.

## Raw observations

The retained [run CSV](code03-runs.csv) contains the ten individual run records,
process peaks in bytes, exit heap observations, elapsed times, sampler counts,
and SHA-256 hashes of each underlying JSON and worker CSV. The [file CSV](code03-files.csv)
contains all 136 producer and consumer file rows, including planned/actual
publication times, encryption and decryption nanoseconds, outcomes, and
failure categories. No averages are reported. The table below is an index to
those raw rows; elapsed times are process intervals reported by CODE-02 and
exclude fixture setup.

| Run | Encrypt | Decrypt and byte-match | Producer / consumer elapsed ms | Producer / consumer peak working set bytes | Producer / consumer JVM heap used at exit bytes |
| --- | ---: | ---: | ---: | ---: | ---: |
| Warm-up 4 MiB | 4/4 | 4/4 | 2,791 / 1,979 | 176,631,808 / 235,712,512 | 39,306,160 / 50,125,912 |
| 4 KiB r1 | 4/4 | 4/4 | 2,572 / 1,526 | 110,915,584 / 118,349,824 | 16,777,216 / 13,729,792 |
| 4 KiB r2 | 4/4 | 4/4 | 2,421 / 1,668 | 108,609,536 / 119,996,416 | 16,777,216 / 10,723,328 |
| 256 KiB r1 | 4/4 | 4/4 | 2,614 / 1,892 | 118,603,776 / 121,634,816 | 10,813,408 / 23,648,304 |
| 256 KiB r2 | 4/4 | 4/4 | 2,673 / 1,796 | 116,551,680 / 124,497,920 | 10,794,328 / 25,026,432 |
| 1 MiB r1 | 4/4 | 4/4 | 2,806 / 1,926 | 147,030,016 / 150,978,560 | 64,318,088 / 82,196,936 |
| 1 MiB r2 | 4/4 | 4/4 | 2,600 / 1,969 | 139,698,176 / 147,951,616 | 64,290,784 / 83,886,080 |
| 4 MiB r1 | 4/4 | 4/4 | 2,770 / 2,170 | 176,033,792 / 242,638,848 | 39,185,760 / 75,924,112 |
| 4 MiB r2 | 4/4 | 4/4 | 2,681 / 2,061 | 176,902,144 / 238,411,776 | 39,280,704 / 75,896,544 |
| 100-file schedule check | 100/100 | 100/100 | 61,194 / 2,277 | 173,432,832 / 248,823,808 | 42,596,520 / 51,164,528 |

All measured per-file producer outcomes were `published`; all measured
consumer outcomes were `verified`, with `none` as failure category. The
100-file check contained exactly 50 files of 4 KiB, 35 of 256 KiB, and 15 of
4 MiB. Its 60 s delayed consumer started after the producer's first
publication. It completed in 81,609 ms wall time including isolated fixture
provisioning and startup. The maximum observed server peak working set among
these ten runs was 138,805,248 bytes (warm-up). The maximum producer and
consumer values were 176,902,144 and 248,823,808 bytes, respectively, in
different runs. These separate process high-water marks are not a simultaneous
total. Exact JVM exit heap values, per-file times, and all sampler gaps are in
the raw CSVs.

An initial sandboxed warm-up failed before generating a run JSON with the
sanitized category `io_or_authentication_failure`. The same isolated command
passed 4/4 outside the sandbox, consistent with the sandbox blocking a
fixture or process operation; the harness did not expose the exact failing
operation. The retained measured warm-up also passed 4/4.
The first runner invocation outside the sandbox incorrectly treated a `null`
PowerShell process exit-code property as failure after the warm-up had passed.
The runner was corrected to check the CODE-02 result and resumed without
overwriting that warm-up. Neither setup failure is counted as a file-size
failure. The passing run records and CSVs remain the primary evidence.

## Supported workload boundary and streaming decision

| Workload | Classification | Evidence and limit |
| --- | --- | --- |
| 4 KiB, four files, four consumer workers | Verified supported | Two measured attempts, 8/8 verified. |
| 256 KiB, same shape | Verified supported | Two measured attempts, 8/8 verified. |
| 1 MiB, same shape | Verified supported | Two measured attempts, 8/8 verified. |
| 4 MiB, same shape | Verified supported | Two measured attempts, 8/8 verified; largest measured individual size. |
| Issue #31 100-file schedule at four workers and 60 s delay | Conditionally supported | One supplementary 100/100 check; the full Issue #32 repetition and baseline schedule has not run. |
| Above 4 MiB per file, other concurrency, lower heap, different hosts or filesystems | Not tested | No observation; the current harness caps a positive single-file size at 4 MiB. |

Issues #31–35 prescribe at most 4 MiB per file and at most four recipient
workers, with a 100-file size mix. The preflight includes their maximum file
size and concurrency, and the supplementary check exercises their longest
normal delay and arrival schedule. This supports proceeding to the fixed-build
study **without CODE-03 streaming implementation**. A deployment needing
larger files, lower memory, or more concurrency needs a separately measured
bound. If a later workload exceeds the bound, the smallest candidate change
would be bounded client-side envelope processing while withholding plaintext
release until GCM authentication succeeds, preserving existing envelope
reading; it would require compatibility, tamper, interrupted-output, and
large-file tests before adoption. No such change was made here.

## Reproduction, acceptance, and remaining risks

From the repository root on a Windows host with JDK 21, Maven, Git, local
atomic rename, owner-only ACL support, and a free loopback port:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code03.ps1
New-Item -ItemType Directory -Path server/target/code03-export | Out-Null
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-code03.ps1 -OutputDirectory server/target/code03-export
```

The first command packages the server, compiles the unchanged CODE-02 harness,
and runs the prespecified attempts. It retains sanitized per-run JSON, CSV,
console output, and OS-memory JSON under `server/target/code03-preflight/`.
It prints `CODE03_PREFLIGHT=passed measured_attempts=8 warmup=1
campaign_shape_checks=1` after all passes. Existing passing results are skipped
on rerun; choose a clean build output directory for a fresh series. The export
command requires a destination without existing `code03-runs.csv` or
`code03-files.csv`. Successful private fixture roots are deleted by the
CODE-02 harness. A failed run leaves a private temporary root whose printed
path and marker must be inspected before manual removal. `mvn clean` removes
generated build outputs after sanitized evidence is copied.

The published `code03-runs.csv` SHA-256 is
`8A591966DAAC7CFFB20CEC7BD732D7CDD2085530915846892747B833D65019A2`;
`code03-files.csv` is
`3922169C07BAB80B90B5CE6CF47C4D8C15C10258C104B7801BA6773DBFC51C0F`.
The exporter's run rows carry hashes for the underlying ignored artifacts.
A second export to a fresh generated directory reproduced both published CSV
SHA-256 hashes exactly.
A scan of the two published CSVs found no master-key path, PKCS#12 path,
private-key delimiter, or password assignment.

| CODE-03 criterion | Evidence | Status |
| --- | --- | --- |
| Inspect and measure existing whole-file behavior | Memory map, prespecified method, eight passing measured runs, and raw CSVs | Complete for current Windows implementation |
| Record a bounded supported workload without claiming larger-file support | Size classifications and exact peak working sets above | Complete for tested host/heap/concurrency |
| If planned workload exceeds bound, implement compatible bounded processing; otherwise defer | Maximum-size repetitions and 100-file schedule check fit; no streaming code changed | Complete: conditional implementation branch not triggered |
| Chosen research workload fits a documented resource bound | 4 MiB/four-worker runs and one 100-file schedule check | Complete as CODE-03 preflight; full Issue #32 study remains pending |

The result is a Windows working-tree preflight, not a fixed-release or POSIX
measurement. Two measured repetitions per size and one 100-file check cannot
establish tail behavior, swap absence, a safe maximum, or production capacity.
The OS working-set peak includes fixture generation and verification and does
not isolate client-only allocations; exit heap is not peak heap. The harness
currently has a 4 MiB positive-size cap, so larger sizes were not attempted.
The CODE-01 fixed-revision gate and the full Issue #31 protocol remain
prerequisites for interpreting Issues #32–35 as study results. No cryptographic,
TLS, file-format, or shared-trust behavior changed. Future improvement is a
fixed-revision study with the protocol's full JVM memory-pool and platform
resource instrumentation; streaming should be reconsidered only if an actual
target workload fails the bound.
