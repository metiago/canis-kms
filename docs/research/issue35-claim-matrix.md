# Issue #35 manuscript claim traceability

This matrix covers every material assertion in [paper.md](../../joss-papers/paper.md), including implementation, research, limitations, historical context, and disclosure. Closely linked clauses in one manuscript sentence share a row. No experiment or measurement was added for this mapping.

## Revision key and evidence boundary

`A` is the fixed **repository evidence-artifact revision** `221df74231891f0910bd8a2f59ec392d3ae67376`. Each source document linked below, except the newly drafted Issue #35 files, is tracked at `A` and can be read with `git show A:<path>`. This fixes the text and retained artifacts being cited; it does **not** make a dirty-tree experiment a clean fixed-revision execution. The revised paper and this matrix are working-tree files until committed. The table's run codes preserve the source records' execution states:

| Run code | Recorded execution revision and qualification |
| --- | --- |
| `J1` | `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91`; focused JOSS-01 rerun, with unrelated backlog edits |
| `C1` | `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91` plus uncommitted CODE-01 changes; later implementation commit `6f0099ad8112fe4c582da39d4305a5bc5a6eb987` was not the tested snapshot |
| `C2` | `23a65838dc21bd02f0f4f9297dd222595cc88c97`; clean Ubuntu normal smoke; the Windows negative cases were earlier dirty-tree local runs |
| `C3` | `0be60db575c5952f6cdddede21b20d135d093e07` plus uncommitted CODE-03 research files |
| `I31` | `f00310ee3678f8c96fc1a8681f35896d5163d3de` plus uncommitted research files |
| `I32` | clean fixed measured revision `9b01048256a5bfe260aebfe0429f521282f6f475` |
| `I33` | `d3e89d7973ab5b936475dd668b780aef03901847` plus uncommitted Issue #33 research files |
| `I34` | `4f6de0568b5ccc6a2947819333d07aec2ba6a3cb` plus uncommitted Issue #34 research files |
| `J2/J4/J5` | Earlier JOSS-02/JOSS-04/JOSS-05 working-tree reviews based on `b8a48ba4f136dfb7a895dd9465f0fef701c5bd81`; these are documentation and local verification, not a fixed release |

The reproducibility links in the final column point to the exact commands and constraints below. They describe how to repeat a study; no repeat was performed for this follow-up. A reader cannot reconstruct every earlier dirty working tree from its Git HEAD alone. The source records retain hashes for those research sources, jars, CSVs, or manifests where available. The historical letter is not a tracked repository artifact; the paper attributes that history to the authors and the [JOSS-01 record](joss01-results.md#historical-operational-evidence-and-research-motivation), and does not claim a verified deployment of this revision.

## Claim-by-claim matrix

| Claim | Evidence | Revision | Reproduction Steps |
| --- | --- | --- | --- |
| **Title/tags:** CANIS is a Java key-service and file-exchange artifact | [Maven reactor](../../pom.xml), [CODE-02 method](code02-method.md), [CODE-02 result](code02-results.md) | `A`; run `C2` | [Static](#static-repository-inspection); [CODE-02](#code-02) |
| **Summary 1:** The client publishes a `CANISHYB1` envelope to shared storage | [CANISP file model](../canisp-protocol.md#file-envelope-model), [CODE-02 method](code02-method.md#process-and-fixture-layout), [CODE-02 result](code02-results.md#commands-and-observed-outcomes) | `A`; run `C2` | [Static](#static-repository-inspection); [CODE-02](#code-02) |
| **Summary 2:** An authenticated later client recovers the wrapped data key without file contents traversing CANIS | [CANISP file model](../canisp-protocol.md#file-envelope-model), [CODE-02 method](code02-method.md#process-and-fixture-layout) | `A`; run `C2` | [Static](#static-repository-inspection); [CODE-02](#code-02) |
| **Summary 3:** The separate-process CODE-02 workflow was exercised | [CODE-02 Windows outcomes](code02-results.md#commands-and-observed-outcomes), [Ubuntu CI record](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) | `A`; run `C2` | [CODE-02](#code-02) |
| **Summary 4:** Admitted clients can recover by any stored key name; JOSS-01 observed observer recovery | [JOSS-01 method](joss01-method.md), [focused rerun](joss01-results.md#focused-rerun-on-2026-11-17), [threat model](../threat-model.md) | `A`; run `J1` | [JOSS-01](#joss-01); [Static](#static-repository-inspection) |
| **Summary 5:** Completed work is bounded; service outages and the original local controls remain unmeasured | [Issue #31 protocol](issue31-protocol.md#baselines-and-what-they-establish), [baseline limitations](issue31-baseline-results.md#reproduction-and-acceptance-review), [Issue #34 acceptance](issue34-results.md#reproducibility-and-acceptance-review) | `A`; runs `I31`, `I34` | [Scope check](#scope-and-negative-evidence) |
| **Need 1:** Sensitive financial-system exchange is author-described motivation | [JOSS-01 historical record](joss01-results.md#historical-operational-evidence-and-research-motivation) | `A`; author account, no current-code run | [Historical limit](#historical-and-disclosure-limits) |
| **Need 2:** The JOSS-01 record describes a 2024 letter about a predecessor and author-attested continuity | [JOSS-01 historical record](joss01-results.md#historical-operational-evidence-and-research-motivation) | `A`; source letter untracked | [Historical limit](#historical-and-disclosure-limits) |
| **Need 3:** The letter does not establish deployment or compatibility of this revision | [JOSS-01 source limits](joss01-results.md#historical-operational-evidence-and-research-motivation) | `A`; no current-code deployment revision | [Historical limit](#historical-and-disclosure-limits) |
| **Need 4:** Evaluations used generated data and credentials; workload parameters are study assumptions | [Issue #31 protocol](issue31-protocol.md#research-question-and-operational-motivation), [baseline result](issue31-baseline-results.md#result-and-boundary), [CODE-02 fixture method](code02-method.md#process-and-fixture-layout) | `A`; runs `C2`, `I31` | [Issue #31](#issue-31); [Static](#static-repository-inspection) |
| **Need 5:** The shared-trust, scheduled-load, and selected-fault questions have observed partial answers | [JOSS-01 result](joss01-results.md#observed-evaluation), [Issue #31 result](issue31-baseline-results.md#result-and-boundary), [Issue #34 result](issue34-results.md#result-and-scope) | `A`; runs `J1`, `I31`, `I34` | [JOSS-01](#joss-01); [Issue #31](#issue-31); [Issue #34](#issue-34) |
| **Need 6:** No service-availability window was measured | [Issue #31 protocol](issue31-protocol.md#fixed-synthetic-workload), [Issue #34 acceptance](issue34-results.md#reproducibility-and-acceptance-review) | `A`; run `I34` excludes outage series | [Scope check](#scope-and-negative-evidence) |
| **Need 7:** The intended audience is software/security engineers and researchers | [Author-validated JOSS-01 question and audience](joss01-results.md#author-validated-question-audience-and-scope) | `A`; author statement | [Static](#static-repository-inspection) |
| **Field 1:** CANIS uses established AES-GCM/RSA-OAEP rather than a new primitive | [CANISP file model](../canisp-protocol.md#file-envelope-model), [JOSS-04 source review](joss04-results.md#primary-source-verification), [bibliography](../../joss-papers/paper.bib) | `A`; run `J4` for source review | [Static](#static-repository-inspection); [Bibliography](#bibliography) |
| **Field 2:** Repository review records Tink and Vault envelope-encryption documentation | [JOSS-04 bibliography review](joss04-results.md#primary-source-verification), [bibliography](../../joss-papers/paper.bib) | `A`; review `J4` | [Bibliography](#bibliography) |
| **Field 3:** CANIS combines Java file client, CANISP recovery service, encrypted persistence, and shared access | [CODE-02 method](code02-method.md), [CANISP guide](../canisp-protocol.md), [threat model](../threat-model.md) | `A`; run `C2` for workflow | [Static](#static-repository-inspection); [CODE-02](#code-02) |
| **Field 4:** No completed experiment compares CANIS against Tink, Vault, or another KMS | [JOSS-01 scope](joss01-results.md#author-validated-question-audience-and-scope), [Issue #32 comparison scope](issue32-results.md#result-and-scope) | `A`; runs `J1`, `I32` | [Scope check](#scope-and-negative-evidence) |
| **Design 1:** Java 21 client/server Maven modules | [Root POM](../../pom.xml), [CODE-02 build method](code02-method.md#build-and-run) | `A`; run `C2` | [Static](#static-repository-inspection); [CODE-02](#code-02) |
| **Design 2:** AES-GCM file key, RSA wrapping, `CANISHYB1`, and key-only recovery | [CANISP file model](../canisp-protocol.md#file-envelope-model), [CODE-03 implementation map](code03-results.md#implementation-memory-map) | `A`; run `C3` for observed path | [Static](#static-repository-inspection); [CODE-03](#code-03) |
| **Design 3:** File bytes stay on client/shared-storage path and admitted client receives the recovered data key | [CODE-02 process method](code02-method.md#process-and-fixture-layout), [JOSS-01 result](joss01-results.md#observed-evaluation) | `A`; runs `C2`, `J1` | [CODE-02](#code-02); [JOSS-01](#joss-01) |
| **Design 4:** Whole-file reads/copies and the tested 4 MiB Windows boundary are not a general maximum | [CODE-03 memory map and result](code03-results.md#supported-workload-boundary-and-streaming-decision) | `A`; run `C3` dirty | [CODE-03](#code-03) |
| **Design 5:** RSA material is stored in AES-GCM-encrypted `CANISDB1`; master key comes from external `CANIS_SECRET_KEY` | [CODE-01 implementation record](../code01-integrity.md#gap-analysis-and-implemented-plan), [CANISP persistence guide](../canisp-protocol.md#persistence) | `A`; local verification `C1` dirty | [CODE-01](#code-01); [Static](#static-repository-inspection) |
| **Design 6:** Tested invalid startup material/records failed, and selected Issue #34 damaged states rejected restart until exact restore | [CODE-01 acceptance](../code01-integrity.md#acceptance-evidence), [Issue #34 scenarios](issue34-results.md#scenario-observations) | `A`; runs `C1` dirty, `I34` dirty | [CODE-01](#code-01); [Issue #34](#issue-34) |
| **Design 7:** Database write uses two moves; crash durability unmeasured | [CODE-01 operational limits](../code01-integrity.md#operational-and-compatibility-limits) | `A`; local verification `C1` dirty | [Static](#static-repository-inspection); [Scope check](#scope-and-negative-evidence) |
| **Design 8:** Official transport uses TLS 1.3, certificate/hostname checks, and credentials | [CODE-02 fixture method](code02-method.md#process-and-fixture-layout), [JOSS-02 verification](joss02-results.md#claim-and-limitation-assessment) | `A`; runs `C2`, `J2` | [CODE-02](#code-02); [JOSS-02](#joss-02) |
| **Design 9:** Admitted clients share key-name access; no per-service authorization | [JOSS-01 result](joss01-results.md#observed-evaluation), [Issue #33 scenario D](issue33-results.md#actual-scenario-observations) | `A`; runs `J1`, `I33` dirty | [JOSS-01](#joss-01); [Issue #33](#issue-33) |
| **Design 10:** Issue #32 non-TLS loopback fixture is research-only | [Issue #32 method](issue32-method.md#prespecification-and-scope), [result](issue32-results.md#result-and-scope) | `A`; run `I32` clean | [Issue #32](#issue-32) |
| **Design 11:** Tested audit alterations changed external hashes without a built-in integrity rejection | [Issue #34 scenario D](issue34-results.md#scenario-observations), [CANISP audit guide](../canisp-protocol.md#audit-events) | `A`; run `I34` dirty | [Issue #34](#issue-34); [Static](#static-repository-inspection) |
| **Impact 1:** JOSS-01 recipient and observer recovered identical synthetic bytes; altered password failed | [JOSS-01 focused rerun](joss01-results.md#focused-rerun-on-2026-11-17) | `A`; run `J1` | [JOSS-01](#joss-01) |
| **Impact 2:** JOSS-01 shared one client certificate; observer recovery matches the policy | [JOSS-01 method](joss01-method.md), [interpretation](joss01-results.md#observed-evaluation) | `A`; run `J1` | [JOSS-01](#joss-01) |
| **Impact 3:** CODE-02 clean Ubuntu normal smoke passed; Windows negatives rejected selected invalid inputs | [CODE-02 CI record](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17), [Windows outcomes](code02-results.md#commands-and-observed-outcomes) | `A`; run `C2` clean for normal; Windows negatives dirty | [CODE-02](#code-02) |
| **Impact 4:** CODE-03 verified two four-file attempts per size through 4 MiB and one 100-file check | [CODE-03 raw observations](code03-results.md#raw-observations), [published run CSV](code03-runs.csv) | `A`; run `C3` dirty | [CODE-03](#code-03) |
| **Impact 5:** Issue #31 verified 2,000 measured files in 20 scheduled runs | [Issue #31 result](issue31-baseline-results.md#result-and-boundary), [run CSV](issue31-baseline-runs.csv), [file CSV](issue31-baseline-files.csv) | `A`; run `I31` dirty | [Issue #31](#issue-31) |
| **Impact 6:** Issue #32 verified 2,000 files in each arm across 20 paired runs | [Issue #32 result](issue32-results.md#result-and-scope), [pair CSV](issue32-artifacts/analysis-pairs.csv), [manifest](issue32-artifacts.sha256) | `A`; run `I32` clean | [Issue #32](#issue-32) |
| **Impact 7:** The zero-delay/one-worker Issue #32 paired latency median was +13.334 ms, range −60.833 to +28.475 ms | [Issue #32 latency table](issue32-results.md#observed-latency-and-throughput), [pair CSV](issue32-artifacts/analysis-pairs.csv) | `A`; run `I32` clean | [Issue #32](#issue-32) |
| **Impact 8:** Issue #32 paired difference is not isolated TLS cost | [Issue #32 method](issue32-method.md#prespecification-and-scope), [limits](issue32-results.md#risks-and-limitations) | `A`; run `I32` clean | [Issue #32](#issue-32) |
| **Impact 9:** Five Issue #33 scenarios observed delay, routes, intact restart, shared access, and tag rejection | [Issue #33 scenarios](issue33-results.md#actual-scenario-observations), [manifest](issue33-artifacts.sha256) | `A`; run `I33` dirty | [Issue #33](#issue-33) |
| **Impact 10:** 21 Issue #34 runs observed selected startup rejection, exact restore, and audit-hash changes without application rejection | [Issue #34 scenarios](issue34-results.md#scenario-observations), [manifest](issue34-artifacts.sha256) | `A`; run `I34` dirty | [Issue #34](#issue-34) |
| **Impact 11:** Selected fault outcomes are not availability or security-strength estimates | [Issue #34 scope and limits](issue34-results.md#limits-and-operational-impact) | `A`; run `I34` dirty | [Scope check](#scope-and-negative-evidence) |
| **Impact 12:** Resource studies used one Windows loopback host and synthetic fixtures; I31/I33/I34 were dirty, I32 clean | [Issue #31 environment](issue31-baseline-results.md#environment-and-execution-record), [Issue #32 environment](issue32-results.md#fixed-source-environment-and-execution), [Issue #33 environment](issue33-results.md#environment-source-and-execution), [Issue #34 environment](issue34-results.md#environment-and-execution) | `A`; runs `I31`, `I33`, `I34` dirty; `I32` clean | [Static](#static-repository-inspection) |
| **Impact 13:** Original local controls, service-outage windows, larger/distributed workloads, and independent release reproduction remain future work | [Issue #31 protocol](issue31-protocol.md#baselines-and-what-they-establish), [Issue #31 deviations](issue31-baseline-results.md#reproduction-and-acceptance-review), [Issue #34 acceptance](issue34-results.md#reproducibility-and-acceptance-review), [backlog](../../joss-backlog.md#phase-2--research-tests-and-proof-of-concept) | `A`; no executed run for those conditions | [Scope check](#scope-and-negative-evidence) |
| **Impact 14:** No general scalability, availability, production, strength, or current-code adoption claim follows | [JOSS-01 limits](joss01-results.md#limitations-and-next-work), [Issue #32 limits](issue32-results.md#risks-and-limitations), [Issue #34 limits](issue34-results.md#limits-and-operational-impact) | `A`; bounded runs as above | [Scope check](#scope-and-negative-evidence) |
| **AI disclosure:** Draft records ChatGPT (GPT-6) assistance; full history and approval need author confirmation | [Earlier draft matrix](joss05-claim-evidence.md), [JOSS-05 result](joss05-results.md), [backlog disclosure gate](../../joss-backlog.md#joss-06--authors-funding-acknowledgements-and-ai-use-p1) | `A` for earlier record; present draft is uncommitted | [Disclosure limit](#historical-and-disclosure-limits) |
| **Acknowledgements/YAML:** Names and affiliations are placeholders; funding/support and AI approval unresolved | [Paper YAML](../../joss-papers/paper.md), [JOSS-05 result](joss05-results.md#acceptance-and-remaining-work), [backlog JOSS-06](../../joss-backlog.md#joss-06--authors-funding-acknowledgements-and-ai-use-p1) | Prior paper at `A`; revised draft uncommitted | [Disclosure limit](#historical-and-disclosure-limits) |
| **References:** Four bibliography keys identify standards and project documentation; no comparative experiment follows | [Bibliography](../../joss-papers/paper.bib), [JOSS-04 review](joss04-results.md#primary-source-verification) | `A`; review `J4` dirty | [Bibliography](#bibliography) |

## Reproduction steps

### Static repository inspection

At artifact revision `A`, use `git show 221df74231891f0910bd8a2f59ec392d3ae67376:<path>` for a cited tracked file. Inspect the Maven reactor, `docs/canisp-protocol.md`, `docs/threat-model.md`, and the cited method/result records. This reproduces the **documented source claim**, not a historical dirty working tree. The source records state their code, jar, CSV, and manifest hashes where retained.

### JOSS-01

Follow [joss01-method.md](joss01-method.md#reproduction): `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1` on Windows or `sh docs/research/run-joss01.sh` on POSIX. The expected focused Surefire report is one passing test. The published result records the earlier rerun; a new run is not evidence that the earlier working tree was clean.

### CODE-01

Follow the [CODE-01 acceptance record](../code01-integrity.md#acceptance-evidence) for the Windows unit, integration, security, and packaged-startup checks. Its local verification used `mvn -q test` and a temporary packaged-startup fixture. The exact later implementation commit was not rerun in that record; no fixed-release claim is made.

### CODE-02

Follow [code02-method.md](code02-method.md#build-and-run): `sh docs/research/run-code02.sh --scenario normal --files 1 --size 4096 --delay-ms 1000` on POSIX, or the PowerShell wrapper on Windows. The same method lists separate tamper, incomplete-file, invalid-credential, and invalid-certificate commands and their expected no-output categories. The retained CI record covers only the clean Ubuntu normal smoke; its archive contents were not independently inspected in that review.

### CODE-03

Follow [code03-method.md](code03-method.md#execution-expected-outputs-and-cleanup): `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code03.ps1`, then its exporter into a fresh directory. Compare reported outcomes and the retained [run](code03-runs.csv)/[file](code03-files.csv) CSV hashes. This is a Windows preflight, not an independent fixed-release result.

### Issue #31

Follow [issue31-baseline-method.md](issue31-baseline-method.md#reproduction): `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue31-baseline.ps1`; `python docs/research/analyze-issue31-baseline.py` recomputes descriptive statistics from the retained CSVs. The original local A/B controls are not implemented by this command.

### Issue #32

Follow [issue32-method.md](issue32-method.md#reproduction-evidence-and-cleanup): run `mvn --batch-mode --no-transfer-progress clean verify`, then the documented `run-issue32.ps1 -Scope campaign` command with new output/export directories, followed by `analyze-issue32.py` and `verify-issue32.py`. The recorded clean series used revision `I32`; its [manifest](issue32-artifacts.sha256) fixes the published raw set. The loopback plain fixture is research-only and is not a product setting.

### Issue #33

Follow [issue33-method.md](issue33-method.md#reproduction-output-and-cleanup): after `mvn clean verify`, invoke `run-issue33.ps1 --scenario A` through `E`, each with a fresh output path, and run `python docs/research/verify-issue33.py`. POSIX wrapper commands are in the method but were not recorded as executed for the final series.

### Issue #34

Follow [issue34-method.md](issue34-method.md#reproduction-and-cleanup): after `mvn clean verify`, run `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34-campaign.ps1`, then `python docs/research/verify-issue34.py docs/research/issue34-artifacts`. The method also gives each individual scenario/variant command. Only the selected storage/audit matrix was executed.

### JOSS-02

Follow [joss02-method.md](joss02-method.md) and the [recorded commands](joss02-results.md#snapshot-and-commands) for TLS/client verification. This is an earlier working-tree result; CODE-01 changed related code later, so it is not a fixed-release check.

### Bibliography

Read the fixed [paper.bib](../../joss-papers/paper.bib) at `A` and [JOSS-04 primary-source table](joss04-results.md#primary-source-verification). The record gives the static citation-key check and its limits. This follow-up did not browse or reverify external pages.

### Scope and negative evidence

Compare the [Issue #31 protocol](issue31-protocol.md) with the executed [baseline](issue31-baseline-results.md#reproduction-and-acceptance-review), [Issue #32 comparator](issue32-method.md#prespecification-and-scope), and [Issue #34 limits](issue34-results.md#reproducibility-and-acceptance-review). An absent planned control or outage is not a negative experimental outcome; it is unmeasured.

### Historical and disclosure limits

The [JOSS-01 historical record](joss01-results.md#historical-operational-evidence-and-research-motivation) is the fixed repository artifact for the attributed predecessor account. Its source letter is untracked and publication permission is unresolved; there is no repository-only reproduction of the letter itself or of historical source continuity. The AI and author/funding statements remain draft disclosures requiring human confirmation under [JOSS-06](../../joss-backlog.md#joss-06--authors-funding-acknowledgements-and-ai-use-p1); they are not experimental results.

## Acceptance boundary

The mapping supplies a fixed repository artifact and a reproduction or inspection route for each manuscript assertion. It does **not** establish that dirty-tree studies were executed on a fixed candidate, that the historical letter is a public artifact, or that author metadata and disclosures are approved. Those distinct [Issue #35 and JOSS gates](../../joss-backlog.md#issue-35--publish-reproducible-study-results-p0) remain open.
