# CANIS-KMS — Unified Research, JOSS, and Product Backlog

> Single active checklist for the empirical study, a possible Journal of Open Source Software
> (JOSS) submission, and product engineering. Follow phases in order for research work; Phase 4
> holds product work that is not required for the current study. Check an item only when its
> acceptance evidence is linked. Historical observations remain in the
> [research evidence index](docs/research/README.md) and linked reports.

## Phase checkpoints

- [ ] **Phase 0 — Research fit:** Authors validate the question and provide inspectable actual research-use evidence.
- [ ] **Phase 1 — Safe research build:** Security prerequisites and a repeatable, isolated exchange harness are verified.
- [ ] **Phase 2 — Empirical study:** Predefined workloads, experiments, and reproducible results answer the research question.
- [ ] **Phase 3 — JOSS candidate:** A fixed, independently reproducible release and complete manuscript meet submission gates.
- [ ] **Phase 4 — Product hardening:** Remaining operational, audit, runtime, and deployment issues meet their acceptance criteria.

# Phase 0 — Research fit and evidence baseline

## Research objective and scope

**Question:** Under a shared-trust model, how do correctness, resource use, and availability change when Java services exchange encrypted files asynchronously using CANIS for key recovery?

- Define an explicit workload and operating conditions before using the term *realistic*. Use an actual permitted workflow where available; otherwise label the workload representative or synthetic and explain its limits.
- Preserve the current trust policy for this study: authenticated services may request decryption under any stored key name. Record the resulting blast radius; do not describe cross-service decryption as a policy bypass.
- Study the supported client-side AES-GCM file envelope and TLS-only key-service path. Do not claim a new cryptographic primitive, comparative superiority, production readiness, or AI attack detection without separate evidence.
- The proposed daily AI audit analysis is a later research direction. It is not a prerequisite or result of this study; a future evaluation would first need structured, integrity-verifiable, secret-free audit events (app Issues #11–19).

## Existing evidence and its boundary

- [JOSS-01 method](docs/research/joss01-method.md) and [results](docs/research/joss01-results.md): the focused synthetic shared-trust boundary test passed again on 2026-11-17 at `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91` (one test, zero failures, errors, or skips). It confirmed recovery by `recipient` and `observer` and rejection of an altered password. This is narrow developer research use of CANIS as the artifact under study. The JOSS-01 record describes a dated TOTVS recommendation letter reporting production use of a predecessor; the authors identify this repository as its open-source continuation. Subsequent Issues #31–35 produced bounded research records; the original local controls and service-outage series remain unexecuted.
- [JOSS-02 fixed candidate](docs/research/joss02-candidate-release.md), [validation](docs/research/joss02-validation.md), [final claim audit](docs/research/joss02-final-claim-audit.md), and [release boundary](docs/research/joss02-release-boundary-final.md): the committed paper and code passed a Windows full-suite run and four selected smokes. App Issues #7, #8, and #36 are explicitly scoped for this research candidate; broader product work remains.
- [JOSS-03 reviewer guide](docs/reviewer-guide.md), [method](docs/research/joss03-method.md), and [results](docs/research/joss03-results.md): a synthetic producer-to-recipient demonstration and Windows tests. Independent checkout, published CI, and POSIX execution are unverified.
- [JOSS-04 results](docs/research/joss04-results.md) and [bibliography](joss-papers/paper.bib): four verified bibliography entries. [JOSS-05 results](docs/research/joss05-results.md) and [draft PDF](docs/research/joss05-draft.pdf): an adapted Windows build with rendered references, not the canonical build with final metadata.
- Some earlier JOSS records above were produced on working trees and committed in `0648976`; the 2026-11-17 JOSS-01 rerun was a focused test on a later commit with unrelated backlog edits in the working tree. Subsequent [Issue #35 synthesis](docs/research/issue35-results.md) records bounded CODE-02/03 and Issues #31–34 observations, including one clean fixed-revision Issue #32 campaign and several dirty-tree runs. JOSS-02 has now tested and audited the [committed candidate](docs/research/joss02-candidate-release.md) without rerunning those campaigns. No tagged/public candidate release, independent candidate reproduction, or completed original Phase 2 question has been established.

## JOSS-01 — Establish and document research significance (P0)

- [x] **Satisfied — question and audience:** The author identifies Issues #31–35 as the empirical study, CANIS as the artifact being evaluated, and software engineers, security engineers, and researchers as the audience. [JOSS-01 results](docs/research/joss01-results.md) explain why this implementation exposes the shared-trust boundary under study and compare it fairly with Tink and Vault. No separate adopter or unrelated research project is required for this criterion.
- [ ] **Partially satisfied — actual research use:** The executed JOSS-01 boundary experiment documents narrow developer research use of CANIS; later Issues #31–35 provide bounded observations but do not complete the original controls and availability question. The [JOSS-01 evidence record](docs/research/joss01-results.md) describes a dated letter reporting production deployment of a KMS at TOTVS, continued use as of August 2024, and later open-source and presentation activity. The authors identify that KMS as CANIS's predecessor and this repository as its open-source continuation, while acknowledging implementation changes. The letter does not name CANIS or a revision. Retain Phase 2 methods, results, analysis, and fixed-revision reproduction evidence. External adoption is not required.
- [ ] **Partially satisfied — supportable claims:** The synthetic [method and results](docs/research/joss01-results.md) support only the observed shared-access behavior and its limits. Recheck claims against a fixed candidate and obtain author approval; avoid production-readiness, cryptographic-novelty, and comparative-superiority claims.

**Acceptance — partially satisfied:** The [manuscript](joss-papers/paper.md) uses the authors' preferred generic description of the vendor and identifies CANIS as the open-source continuation of the historically deployed predecessor. It states the letter-supported operational motivation, author-attested design continuity, audience, bounded research question, limited developer research use, and limitations. The fair comparison is in [JOSS-01 results](docs/research/joss01-results.md). The historical letter is not a tracked publication artifact pending permission to publish or share a redacted copy. The later [Issue #35 synthesis](docs/research/issue35-results.md) retains bounded Phase 2 observations, while the original availability question and fixed-release research-significance claims remain open. A separate research project, third-party publication, or external adoption is not a JOSS-01 prerequisite.

# Phase 1 — Security and reproducible workload prerequisites

Before each implementation issue, inspect existing code, identify affected files and security implications, and record an implementation plan as required by [AGENTS.md](AGENTS.md). Keep product changes separate from the research harness; every security change needs unit, integration, and security tests, including compatibility checks.

## CODE-01 — Resolve integrity prerequisites for a fixed research build (P0)

- [x] Implement and verify the research-release security parts of app Issues #7 and #8 on the local Windows candidate: supported master-key construction paths reject invalid configuration, malformed stored records fail closed, and a write cannot silently recreate a missing database. Record limits outside the study's claims in the [CODE-01 integrity evidence](docs/code01-integrity.md).
- [x] Implement the private-key exposure and stored-key validation parts of app Issue #36 on the same candidate. The [CODE-01 evidence](docs/code01-integrity.md) records the shared-access policy and tests; per-key denial belongs to a different trust model.
- [x] Run local tests for valid `CANISDB1` restart and existing file envelopes, invalid certificates and credentials, database tampering, and failure paths without leaking secrets. See the [CODE-01 acceptance matrix](docs/code01-integrity.md#acceptance-evidence).
- [x] Commit a candidate revision: `6f0099ad8112fe4c582da39d4305a5bc5a6eb987` contains the CODE-01 implementation. The [local evidence](docs/code01-integrity.md) predates that commit.
- [ ] Rerun the security, compatibility, and packaged-startup checks against the exact committed revision. Run the skipped POSIX permission and symlink cases on POSIX CI and retain sanitized results before closing this gate.

**Acceptance — local candidate verified, fixed-revision gate open:** The hashed local jar and Windows tests support failure-safe behavior for the tested invalid key material and database states, compatibility with valid storage and envelopes, and an [explicit operational-limit list](docs/code01-integrity.md#operational-and-compatibility-limits). The tests predate the commit and POSIX evidence is pending, so this is not a research-release decision.

## CODE-02 — Add an isolated, repeatable asynchronous exchange harness (P0)

- [x] Implement a documented producer, delayed consumer, and CANIS server run as separate processes against a temporary shared directory. Use externally supplied TLS and master-key fixtures; never place private keys or plaintext in shared storage. See the [method](docs/research/code02-method.md#process-and-fixture-layout) and [CI result](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).
- [x] Publish complete encrypted envelopes atomically; have the consumer ignore incomplete files and record byte-equality results only after authenticated decryption. Keep the research harness outside the production protocol path. See the [normal and negative runs](docs/research/code02-results.md#commands-and-observed-outcomes) and [Ubuntu CI run](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).
- [x] Emit sanitized, machine-readable run metadata: revision, runtime, scenario, file size, concurrency, timing, memory, outcome, and failure category. Define a stable run ID without logging keys or plaintext. See the [result format](docs/research/code02-method.md#results-cleanup-and-limits) and [CI artifact](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).
- [x] Include repeatable setup, teardown, and fixture cleanup on supported platforms. Ensure failure injection cannot alter a real database or deployment. See the [Windows negative runs](docs/research/code02-results.md#commands-and-observed-outcomes), [isolation method](docs/research/code02-method.md#process-and-fixture-layout), and [Ubuntu CI run](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17).

**Acceptance — satisfied for CODE-02:** A clean Ubuntu checkout ran the documented POSIX wrapper, with separate producer and delayed consumer processes, and the CI job and evidence upload passed. No separate manual reproduction is required. This is a functional smoke result, not a fixed research-release or performance result.

**Current CODE-02 evidence:** The [research-only harness method](docs/research/code02-method.md) and
[local Windows execution record](docs/research/code02-results.md) show a separate-process
synthetic exchange and negative cases. Two failed certificate-classification attempts are
retained. Those local runs did not establish clean-checkout or POSIX reproduction.
The author also reported a successful [separate source-archive Maven build](docs/research/code02-results.md#separate-source-archive-build-on-2026-11-17)
with 158 tests and eight skips. The author reports a Fedora WSL build, but the available
Surefire XML identifies Windows 11 and skips the POSIX permission and symlink cases; those
files cannot establish the reported Fedora run. The extracted archive lacks `.git`,
and no process-harness run was reported for it; that older evidence did not close the clean-checkout gate.
The earlier Windows Actions failure with 38 `unsafe_permissions` errors and the local
test-fixture ACL correction are recorded in the [result record](docs/research/code02-results.md#github-windows-ci-failure-and-local-fixture-correction-on-2026-11-17).
At commit `e94c93bd5fdd62588b2d3413c1819523f9d8a864`, the
[Ubuntu CI job](https://github.com/metiago/canis-kms/actions/runs/44202807123/job/134460973098)
passed repository checkout, Java 21 setup, `mvn clean verify`, and the POSIX permission/symlink
coverage gate. The [Windows job](https://github.com/metiago/canis-kms/actions/runs/44202807123/job/134460972632)
also passed at this revision. The [committed workflow](.github/workflows/ci.yml)
has no CODE-02 harness step, and neither job lists one. Maven does not invoke the separate-process
`run-code02` wrapper. Therefore this CI run establishes a clean-checkout build and POSIX security
test coverage, but neither clean-checkout nor POSIX reproduction of the CODE-02 exchange.
At commit `23a65838dc21bd02f0f4f9297dd222595cc88c97`,
[CI run 44204796541](https://github.com/metiago/canis-kms/actions/runs/44204796541)
completed successfully on Ubuntu and Windows. The [Ubuntu job](https://github.com/metiago/canis-kms/actions/runs/44204796541/job/134467866501)
passed checkout, Java setup, Maven verification, the POSIX security gate, the documented
`sh docs/research/run-code02.sh` normal run, and the CODE-02 evidence upload. The
`code02-evidence-ubuntu-latest` artifact (ID `14381849411`, 2368 bytes) was published. The
[workflow](.github/workflows/ci.yml) selects the wrapper log, exit-status text, JSON, and
producer/consumer CSV paths; its archive contents were not independently downloaded
because the artifact endpoint requires authentication. The successful wrapper step is the
reproduction gate. The [CI result record](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17)
records the evidence and limits. No separate local run is required.

| CODE-02 requirement | Evidence | Status |
| --- | --- | --- |
| Separate server, producer, and delayed consumer in an isolated temporary run | [Windows execution record](docs/research/code02-results.md#commands-and-observed-outcomes), [method](docs/research/code02-method.md#process-and-fixture-layout), and [Ubuntu CI job](https://github.com/metiago/canis-kms/actions/runs/44204796541/job/134467866501) | Complete |
| Atomic encrypted publication, incomplete-file handling, and authenticated byte comparison | [Normal and negative run records](docs/research/code02-results.md#commands-and-observed-outcomes) and successful Ubuntu normal run | Complete for CODE-02 smoke workload |
| Sanitized machine-readable metadata and stable run ID | [Local acceptance matrix](docs/research/code02-results.md#security-compatibility-and-acceptance) and [CI artifact listing](docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) | Complete for CODE-02 |
| Repeatable setup, teardown, and isolated failure fixtures on supported platforms | [Windows run](docs/research/code02-results.md#security-compatibility-and-acceptance), [POSIX wrapper](docs/research/run-code02.sh), and successful Ubuntu CI job | Complete for CODE-02 |
| Clean-checkout CODE-02 reproduction | Ubuntu job checked out commit `23a6583` and completed the documented wrapper successfully | Complete |
| POSIX CODE-02 reproduction | Same Ubuntu job completed the documented `sh` wrapper on `ubuntu-latest` | Complete |

## CODE-03 — Decide the supported file-size boundary from measurements (conditional)

- [x] Measure the existing `Files.readAllBytes` behavior and record a bounded supported workload. The [CODE-03 Windows preflight](docs/research/code03-results.md) verified 4 KiB, 256 KiB, 1 MiB, and 4 MiB in two four-file runs each with four consumer workers; it does not claim larger-file support.
- [x] The planned synthetic workload fits the observed bound on this host: one supplementary 100-file run verified the Issue #31 size mix, 500 ms arrivals, 60 s delay, and four workers. [CODE-03 results](docs/research/code03-results.md) document the limit and defer streaming. The conditional bounded-processing branch was not triggered.

**Acceptance — local preflight complete:** The chosen workload fits the [documented Windows resource bound](docs/research/code03-results.md) for this implementation and configuration. Fixed-revision and full Issue #32 study measurements remain open; this does not establish a general maximum file size.

## Product security prerequisites — Issues #7, #8, and #36

These detailed product findings are required for the research-release decision in CODE-01.

### Issue #7 - Protect CANIS_SECRET_KEY

**CODE-01 status:** The local Windows constructor and master-key tests pass; the remaining platform and path-control checks below stay open. See [CODE-01 evidence](docs/code01-integrity.md).

#### Pending work

- [x] Reconcile the earlier public-constructor finding with `Server.verifyConfiguration`; `ServerTest` now covers missing, malformed, mismatched, and changed master-key configurations before binding.
- [x] Review ancestor-directory permissions and path-replacement resistance; [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build) require trusted ancestor control and state the remaining race limit.
- [x] Record the decision for runtime loss of the cached master-key file: the running process does not alert automatically, so operators must monitor the file and detect loss before restart. See [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build).
- [x] Add a sanitized master-key log assertion in `MasterKeyLoaderTest`.
- [ ] Run POSIX permission and symlink tests on POSIX CI; add owner-mismatch and inherited Windows ACL cases.

#### Acceptance criteria

- [x] No supported server construction path starts with an invalid master-key configuration in the tested Windows paths; [constructor tests and packaged smoke evidence](docs/code01-integrity.md#acceptance-evidence) support this. POSIX and ACL coverage remain open above.

### Issue #8 - Prevent Silent Database Recreation

**CODE-01 status:** Local Windows validation and write-path tests pass; filesystem crash durability and platform checks remain release limits. See [CODE-01 evidence](docs/code01-integrity.md).

#### Pending work

- [x] Reject authenticated database records with invalid RSA fields or malformed UTF-8 without rewriting the database; [storage tests](docs/code01-integrity.md#acceptance-evidence) coordinate this with Issue #36.
- [x] Close the check-to-move recreation path in `KeyValueStore.saveToFile` by moving the verified old database to recovery before installing the staged file. The [operational limits](docs/code01-integrity.md#operational-and-compatibility-limits) describe the two-move interruption window.
- [x] Add deterministic tests for deletion after verification, interrupted and unsupported atomic moves, symlink rejection, and storage-failure log sanitization. The symlink case was skipped on this Windows host and remains in the POSIX CI gate.
- [x] Exercise the packaged `--init-db` command end to end: the first call created a database, the second failed without changing its hash, and normal startup rejected a missing database. See [packaged smoke evidence](docs/code01-integrity.md#acceptance-evidence).
- [x] Review idle-time database-loss detection, directory metadata durability, and the cost of reading and hashing large databases; [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build) state the unresolved deployment limits.

#### Acceptance criteria

- [x] Corrupted or malformed stored records fail safely in the [local storage tests](docs/code01-integrity.md#acceptance-evidence).
- [x] No tested write path silently recreates a missing cryptographic database; [deletion-boundary and recovery tests](docs/code01-integrity.md#acceptance-evidence) support this under the documented trusted-directory assumption. Cross-filesystem crash behavior remains open.

### Issue #36 - Close Private-Key Exposure Review Findings

**Status:** Open. Follow-up to Issue #6 and its implementation review.

#### Objective

Prevent private-key material from reaching public APIs or CANISP responses through malformed but
decryptable stored records. Preserve and document the current shared trust domain: every authenticated
service may request decryption under any stored key name. Per-key access control would be a separate
policy change and requires its own design, migration, and compatibility decision.

#### Tasks

- [ ] Validate loaded `CANISDB1` public and private key encodings and that they form a matching RSA
      pair before exposing metadata or serving requests. Fail closed on invalid records without
      replacing or rewriting the database.
- [ ] Remove the unused `privateKey` field and Java serialization capability from the client
      artifact's `io.canis.jpaw.pojo.Entry`, or remove that type if it has no supported callers.
- [ ] Confirm that `|decrypt` is available only after TLS client-certificate and credential
      authentication; document that authenticated identities share access to all stored keys.
- [ ] Record the cross-service blast radius and audit the requesting identity and key name without
      exposing key material. If the trust model changes, define per-key authorization and migration
      in a separate issue before changing this behavior.
- [ ] Add tests for a private-key encoding placed in a stored public-key field, malformed legacy
      records, client API/serialization boundaries, permitted cross-service decrypt after valid
      authentication, and rejection of invalid certificates and credentials. Cover the official
      client over TLS as well as the handler in isolation.
- [ ] Document startup failure behavior and compatibility impact for existing databases and
      clients.

#### Acceptance criteria

- [ ] No private-key material is returned through Java APIs, serialization, logs, exceptions, or
      CANISP responses, including when a decryptable database contains malformed key fields.
- [ ] The shared-access policy is enforced only after authentication and is tested and documented;
      decrypt audit records identify the requester and selected key without exposing key material.
- [ ] Valid existing `CANISDB1` databases and authorized file workflows remain usable after restart.

**Coordination:** Issue #8 now covers atomic database writes and missing-database handling. Audit
schema and integrity requirements remain with Issues #11-#19.


# Phase 2 — Research tests and proof of concept

The original research Issues #31–35 retain their numbers. Predefine questions, workloads, expected outcomes, and stopping rules before collecting results. Treat prior working-tree tests as pilot evidence; rerun on a fixed revision. Keep the existing shared-trust test as a boundary check, not an attack-detection result.

## Issue #31 — Define representative workload and baselines (P0)

- [x] Identify a permitted real file-exchange workflow or document why a synthetic workload represents it: producer/consumer count, file-size distribution, arrival rate, processing delay, concurrency, retention, and outage window. Label assumptions and excluded conditions. [Prespecified protocol](docs/research/issue31-protocol.md).
- [x] Specify testable outcomes for correctness, resource use, and availability, with thresholds if claiming operational suitability. Define the baseline (for example, local AES-GCM cost plus CANIS key fetch/unwrap cost) and what each comparison can establish. Compare with another KMS only if making a comparative claim. [Prespecified protocol](docs/research/issue31-protocol.md); no operational-suitability or comparative KMS claim is planned.
- [x] Record fixture generation, environment, JDK/Maven versions, warm-up, repetitions, measurement tools, and analysis plan before measuring. Avoid secrets and customer data in published fixtures. [Prespecified protocol](docs/research/issue31-protocol.md).

**Acceptance — protocol and bounded baseline recorded:** The [Issue #31 protocol](docs/research/issue31-protocol.md) fixes the synthetic workload, controls, analysis, and interpretation. The [Windows CANIS baseline](docs/research/issue31-baseline-results.md) records four passing warm-ups and 20 passing 100-file runs within the CODE-03 bound, with [raw run](docs/research/issue31-baseline-runs.csv) and [file](docs/research/issue31-baseline-files.csv) observations. It is an absolute path-C characterization with documented protocol deviations, not the full paired study. CODE-02 and the local [CODE-03 memory preflight](docs/research/code03-results.md) are complete. The CODE-01 fixed-revision release gate and full Issues #32–35 execution remain open.

## Issue #32 — Measure normal-operation performance and resource bounds (P0)

- [x] Run repeated file exchanges across the predefined file sizes, consumer worker counts, and processing delays. The [method](docs/research/issue32-method.md) and [Windows results](docs/research/issue32-results.md) report total and component latency, throughput, observed client/server process memory, failure counts, and five-run variation for 40 measured runs.
- [x] Separate public-key fetch and unwrap request intervals from local envelope encryption/decryption intervals where the research worker exposes those boundaries. Request intervals combine client, loopback transport, and server work; pure network or server-only time was not isolated. The study stayed within CODE-03's 4 MiB/four-worker/512 MiB preflight and makes no general maximum-file-size claim.
- [x] Keep the non-TLS comparison in a loopback-only research harness compiled outside product artifacts; supported client/server traffic remains TLS 1.3 only.

**Acceptance — bounded Windows characterization recorded:** The [Issue #32 result](docs/research/issue32-results.md) links 438 sanitized artifacts, an [artifact manifest](docs/research/issue32-artifacts.sha256), [analysis code](docs/research/analyze-issue32.py), and a [verifier](docs/research/verify-issue32.py). The final fixed-revision series recorded 20 matched A/B pairs, 4,000/4,000 measured byte-verified files, and zero measured file failures. Its paired differences describe this synthetic loopback workload only; the research transport is not a product feature, and no capacity, security-strength, or production-performance claim follows.

## Issue #33 — Demonstrate delayed multi-service exchange (P0)

- [x] Use separate producer, consumer, and server processes with a shared encrypted-file directory; verify byte equality after a documented delay and after a clean server restart. The test uses a controlled stop between operations; graceful shutdown behavior was not measured. [Method](docs/research/issue33-method.md) and [Windows observations](docs/research/issue33-results.md).
- [x] Exercise multiple credential identities and distinct generated client certificates. Confirm the documented shared-access behavior, invalid-credential rejection, and file tamper rejection without treating permitted cross-service decryption as unauthorized. [Scenario D and E evidence](docs/research/issue33-results.md#actual-scenario-observations).
- [x] Record envelope publication/consumption ordering, duplicate and incomplete-file handling, and what happens if a file is present before its consumer starts. [Scenario A and event records](docs/research/issue33-results.md#actual-scenario-observations).

**Acceptance — local functional evidence recorded:** The [Issue #33 method](docs/research/issue33-method.md) gives reviewer commands and the [result record](docs/research/issue33-results.md) links five passing Windows scenarios, sanitized CSV/JSON evidence, an artifact verifier, and an acceptance matrix. The source tree was dirty during execution; fixed-revision and POSIX reproduction remain open as broader study gates, and no performance or availability claim follows.

## Issue #34 — Evaluate outages and faults (P0)

- [ ] Use isolated fixtures to exercise CANIS unavailable before public-key fetch, during or before data-key recovery, and after restart; key deletion before delayed processing; corrupted envelope/tag; invalid credentials/certificates; and invalid or missing database/master key.
- [ ] For each scenario, predefine and report whether encryption/decryption succeeds, whether plaintext is released, time to detect failure, and recovery steps. Distinguish current design limits from defects.
- [ ] Add storage corruption, interrupted writes, audit tampering, or longer outages only where they answer the research question. Do not claim tamper-evident audit until app Issues #16–19 are implemented and tested.

**Acceptance:** Fault results have exact inputs, expected outcomes, observed outcomes, and documented limits; no test can touch a real deployment.

**Partial Issue #34 evidence (2026-11-18):** The [controlled fault method](docs/research/issue34-method.md)
and [Windows results](docs/research/issue34-results.md) cover three synthetic repetitions each
of master-key loss and replacement, database corruption while running and stopped, audit-line
modification and deletion, and restore/restart after corruption. The 159 final JSON/CSV artifacts
and SHA-256 manifest are linked there. This does not close the full Issue #34 outage, key-deletion,
envelope, and admission matrix. The source tree was dirty, POSIX reproduction is unverified, and
internal fault-detection latency was not measured.

## Issue #35 — Publish reproducible study results (P0)

- [ ] Publish methods, actual results, analysis, negative results, discussion, and limitations under `docs/research/`; separate observed behavior from inferred implications.
- [ ] Provide exact commands, non-secret fixtures, expected outputs, cleanup, revision, environment, platform limits, and artifact hashes for each experiment.
- [ ] Revise the proposed research question if the collected evidence cannot answer it; avoid claims of general reliability or security from a single setup.

**Acceptance:** A reader can trace each paper claim to a fixed-revision result and its reproduction steps.

# Phase 3 — JOSS eligibility, review, and submission

Current [JOSS submission criteria](https://joss.readthedocs.io/en/latest/submitting.html) require demonstrated research use at least by the developers, more than six months of active public development, iterative open-source practice, and reviewable software. The executed JOSS-01 synthetic boundary experiment is limited developer research use; a planned Phase 2 study is not a completed result. Whether this evidence demonstrates sufficient research impact is an editorial judgment. External adoption and third-party publications are not required screening conditions.

## Submission decision gates

- [ ] **Gate A — Research fit (P0):** Substantiate the question, audience, actual research use, and distinct need beyond existing KMS or envelope-encryption tools.
- [x] **Gate B — Supported release (P0):** The fixed [JOSS-02 candidate](docs/research/joss02-candidate-release.md) passed [Windows validation and four smoke checks](docs/research/joss02-validation.md); the [final claim audit](docs/research/joss02-final-claim-audit.md) and [release boundary](docs/research/joss02-release-boundary-final.md) reconcile the manuscript and explicitly scope app Issues #7, #8, and #36. This is a local supported research candidate, not the separate independent-reproduction or public-archive gate.
- [ ] **Gate C — Reproducibility (P0):** A non-author can install, test, run the separate-process example, and reproduce at least one meaningful evaluation.
- [ ] **Gate D — Submission artifacts (P1):** Complete manuscript, references, author metadata, AI disclosure, and public candidate snapshot. Plan a permanent archive; JOSS requests the archive DOI after successful review.

## JOSS-02 — Freeze and verify the research release boundary (P0)

- [x] On the [candidate revision](docs/research/joss02-candidate-release.md), verify official-client TLS 1.3 mutual authentication, certificate and hostname validation, and no plaintext fallback. The [fixed suite](docs/research/joss02-validation.md#fixed-revision-build-and-tests) passed the client and server TLS cases.
- [x] Run the complete file workflow and negative TLS, credential, shared-access, and storage-tampering tests; retain sanitized outcomes and explain skips. The [validation record](docs/research/joss02-validation.md) reports 159 tests, zero failures/errors, eight explained Windows skips, and four selected clean-revision smokes.
- [x] Resolve or explicitly scope app Issues #7, #8, and #36, including database recreation, stored-key validation, private-key exposure, and the documented shared-trust blast radius. [CODE-01](docs/code01-integrity.md) and the [final boundary](docs/research/joss02-release-boundary-final.md#release-decision-and-impact) state the implemented checks and remaining filesystem, audit, and authorization limits.
- [x] Repeat the claim-evidence audit against the fixed code and committed paper; record commit, OS, JDK/Maven versions, tests, and known limitations. See the [final claim audit](docs/research/joss02-final-claim-audit.md), [candidate](docs/research/joss02-candidate-release.md), and [validation](docs/research/joss02-validation.md).

| Requirement | Evidence | Status |
| --- | --- | --- |
| Fixed source and manuscript revision | Candidate `cd1ebeb32bbd0b6b2ebd42a29f72e3dac8b01694`, clean at selection and during smoke executions | Complete for JOSS-02 |
| Exact-revision build, official workflow, and negative security cases | `mvn clean verify` passed; JOSS-01, CODE-02 normal, Issue #33 D, and Issue #34 E smokes passed; Windows skips explained in validation | Complete on tested Windows host |
| App #7/#8/#36 release scope | CODE-01 fixed tests and final supported/not-supported boundary | Complete as an explicit research-candidate decision; broader product hardening remains |
| Every material manuscript claim reconciled | Final five-column claim audit retains historical execution states and qualifications; no unsupported or obsolete material claim remains | Complete for the committed paper |

**JOSS-02 status — complete for the fixed Windows research candidate.** The candidate source and manuscript were committed before validation. Its complete Maven run and four functional smokes passed; every material paper claim is reconciled with candidate source/tests or explicitly qualified retained study evidence. No full research campaign was rerun. Earlier [JOSS-02 audit](docs/research/joss02-audit.md) and [release-boundary draft](docs/research/joss02-release-boundary.md) remain historical pre-validation records. Independent non-author candidate reproduction, POSIX checks for this commit, public snapshot/tag, canonical paper build, original local controls, and availability-window study remain separate open gates; this decision does not assert their completion.

## JOSS-03 — Installation, examples, documentation, and CI (P0/P1)

- [ ] Verify the documented Java 21/Maven build and external TLS/master-key provisioning from a clean checkout, including non-secret fixtures and any operational setup steps.
- [ ] Verify the committed Ubuntu/Windows CI workflow runs the full tests; confirm POSIX permission and symlink cases actually execute and the report gate passes.
- [ ] Verify public license, issue and contribution routes, a stable private security-reporting route, support expectations, and the required active public development history.

**Acceptance:** A non-author follows the [reviewer guide](docs/reviewer-guide.md), runs the separate-process proof of concept and tests, and records no undocumented steps. The current example uses a TLS server in a test thread, so it does not yet meet this acceptance criterion.

## Manuscript and submission artifacts

## JOSS-04 — Restore and verify bibliography (P1)

- [ ] Build the final paper on the fixed candidate with the canonical JOSS workflow; confirm all citations and references resolve and metadata renders correctly.

**Acceptance:** The canonical manuscript build completes without missing references. The adapted [draft PDF](docs/research/joss05-draft.pdf) is partial evidence only.

## JOSS-05 — Align and finalize manuscript (P1)

- [ ] Reframe the statement of need, state of the field, software design, and research impact statement around the validated Phase 2 question and actual JOSS-01 research-use evidence. Include only results produced by Issues #31–35.
- [ ] Check final JOSS sections, word count, formatting, references, and every technical or impact claim against the candidate revision. Keep observed behavior distinct from planned audit, rotation, and container work.

**Acceptance:** All claims in [paper.md](joss-papers/paper.md) have cited evidence from the fixed release or research record; unsupported adoption, performance, and security claims are absent.

## JOSS-06 — Authors, funding, acknowledgements, and AI use (P1)

- [x] Confirm all authors, affiliations, and substantive contributions; replace manuscript placeholders. The author-approved values and contribution list are recorded in the [JOSS-06 audit](docs/research/joss06-audit.md#author-and-contribution-record) and `paper.md` YAML.
- [x] Confirm funding, institutional support, acknowledgements, and any conflicts with the authors. The author-approved negative disclosures are recorded in the [JOSS-06 audit](docs/research/joss06-audit.md#funding-support-acknowledgements-and-conflicts) and manuscript acknowledgements.
- [x] Document actual AI tools, versions, and use across code, documentation, and paper; obtain human review, validation, and author approval. The [JOSS-06 audit](docs/research/joss06-audit.md#ai-use-inventory-and-human-review) records three tools, their individual uses, ChatGPT model families, the author-confirmed limit on reconstructing Codex and Microsoft Copilot versions, and human review and editing.
- [ ] Set the actual submission date only when submitting.

**Acceptance — complete for candidate preparation:** Author, affiliation, contribution, funding, support, acknowledgement, conflict, per-tool AI use, and human review and editing facts are supplied and recorded. The unavailable exact historical Codex and Microsoft Copilot identifiers are explicitly disclosed. The actual submission date is deferred until submission. See the [JOSS-06 acceptance review](docs/research/joss06-audit.md#acceptance-review).

## JOSS-07 — Independent review and submission snapshot (P1)

- [ ] Have a colleague who was not the primary developer install the candidate, run the example, and reproduce the documented evaluation.
- [ ] Fix and tag a public candidate; prepare a permanent archive and citation plan. After successful JOSS review, archive the reviewed version and record its DOI.
- [ ] Run all project tests and the canonical JOSS paper build on the exact candidate commit; retain sanitized reports, environment details, and artifact hashes.
- [ ] Verify repository public age, active development, actual research use, licensing, issue access, support paths, and other current JOSS screening criteria.
- [ ] Reconcile every submission task and evidence link in this single backlog before fixing the candidate release.

**Acceptance:** A fixed, public, independently reproducible candidate and complete manuscript are ready for editorial screening; the final archive DOI is recorded at the review stage.

+## Detailed submission checks

The checked items below have mixed provenance: older working-tree evidence remains historical, while the JOSS-02 items linked to the fixed [candidate validation](docs/research/joss02-validation.md) have exact-revision Windows evidence.
These granular checks correspond to JOSS-02 through JOSS-07 above. All 18 open and 11
completed checks from the former submission-readiness checklist are retained here.

### Submission check 1 — Restore and verify the bibliography

- [x] Confirm the final manuscript and bibliography locations. The files are colocated as [`paper.md`](joss-papers/paper.md) and [`paper.bib`](joss-papers/paper.bib), and YAML uses `bibliography: paper.bib`; see [JOSS-04 results](docs/research/joss04-results.md).

- [x] Add a BibTeX entry for every citation used in `paper.md`, including the cryptographic standards and software alternatives discussed there. Four cited keys match four entries in [`paper.bib`](joss-papers/paper.bib); see [JOSS-04 results](docs/research/joss04-results.md).

- [x] Verify each entry against its publisher or official project source, including title, author or organization, available date/version, and stable URL or DOI. The [source table](docs/research/joss04-results.md) records the verification and notes that the Vault page has no stated publication date.

- [ ] Check that every bibliography entry is cited and every citation resolves; compile the manuscript with the JOSS toolchain. The [JOSS-05 static check and adapted local PDF build](docs/research/joss05-results.md) found four matching keys and four rendered references in an [inspected draft](docs/research/joss05-draft.pdf). The canonical GNU make/TeX Live/Hack-font build and fixed candidate revision remain unverified.

**Done when:** The paper compiles through the JOSS workflow with complete, verified references and no unresolved citation keys.

### Submission check 2 — Make the supported client/server transport coherent

- [x] Review the server's TLS 1.3 mutual-authentication requirements and the client's `SocketClient` implementation against the [fixed candidate and passing TLS tests](docs/research/joss02-validation.md#fixed-revision-build-and-tests).

- [x] Implement client-side TLS 1.3 using externally supplied trust and identity material, with server certificate and hostname validation. Retain the project's no-plaintext-fallback requirement. Evidence: [`SocketClient.java`](client/src/main/java/io/canis/jpaw/client/SocketClient.java), [`TlsContextFactory.java`](client/src/main/java/io/canis/jpaw/config/TlsContextFactory.java), and the [JOSS-02 test results](docs/research/joss02-results.md).

- [x] Document required client and server configuration without embedding credentials or secret material. Evidence: [reviewer guide](docs/reviewer-guide.md), [README](README.md), and [master-key guide](docs/master-key.md). The synthetic [JOSS-03 test](server/src/test/java/io/canis/ReviewerWorkflowTest.java) generates its own fixtures. Fixed-release verification remains open.

- [x] Add and run client transport tests for a trusted server/client pair, hostname or certificate rejection, untrusted client rejection, TLS-version enforcement, and failure behavior. Evidence: [`SocketClientTest.java`](client/src/test/java/io/canis/jpaw/client/SocketClientTest.java) and the [JOSS-02 suite report](docs/research/joss02-suite-report.csv) (14 tests, zero failures, zero errors, zero skips on Windows).

- [x] Run an end-to-end test through the supported Java client against the TLS server, covering file encryption, server-side key unwrap, and file decryption. Evidence: [`ServerEndToEndTest.java`](server/src/test/java/io/canis/ServerEndToEndTest.java) and the [JOSS-02 suite report](docs/research/joss02-suite-report.csv) (four tests, zero failures, zero errors, zero skips on Windows).

- [ ] Update the paper and deployment documentation to describe only the verified release behavior.

**Done when:** The documented Java client completes the file workflow against the TLS-only server and negative transport tests demonstrate fail-closed behavior.

### Submission check 3 — Establish and document JOSS-level research significance

- [x] Identify the specific research question and audience for which this software provides value beyond configuring an established KMS or envelope-encryption library. The author identifies CANIS as the artifact examined by Issues #31–35; the shared-trust question and audience are documented in [`joss01-results.md`](docs/research/joss01-results.md).

- [ ] Complete and document developer research use in the planned Issues #31–35 study, including context, software revision, methods, results, and reproducibility artifacts. The executed JOSS-01 boundary test is limited initial evidence; an external pilot is optional.

- [x] Define the shared trust boundary evaluation protocol before execution in [`joss01-method.md`](docs/research/joss01-method.md).

- [x] Publish scripts, generated fixture procedure, environment, and reproduction commands in [`docs/research`](docs/research/joss01-method.md).

- [x] Report only the observed categorical result and its limitations in [`joss01-results.md`](docs/research/joss01-results.md). The synthetic test is not adoption or a performance measurement.

- [x] Reassess JOSS fit against current screening rules: the executed boundary test documents narrow developer research use, while Phase 2 results and a convincing impact case remain open. External adoption is not mandatory.

**Done when:** The manuscript can cite specific research use or a reproducible evaluation, and its contribution is clearly distinguished from existing tools without unsupported novelty claims.

### Submission check 4 — Complete author, funding, and AI disclosures

- [x] Confirm the complete author list and each author's substantive contribution to the software and manuscript. See the [JOSS-06 author and contribution record](docs/research/joss06-audit.md#author-and-contribution-record).

- [x] Replace YAML name and affiliation placeholders with author-approved metadata and valid JOSS formatting. See the [paper YAML](joss-papers/paper.md) and [JOSS-06 audit](docs/research/joss06-audit.md#author-and-contribution-record); final rendering remains part of the candidate manuscript build.

- [x] Ask all authors to confirm funding, institutional support, and acknowledgements; state these accurately. The sole author supplied [negative disclosures](docs/research/joss06-audit.md#funding-support-acknowledgements-and-conflicts), including conflicts of interest.

- [x] Record generative AI tools and versions used in software, documentation, and manuscript work, with the scope of each use. The [JOSS-06 audit](docs/research/joss06-audit.md#ai-use-inventory-and-human-review) records each tool's use, ChatGPT model families, and the author-confirmed historical version limit for Codex and Microsoft Copilot.

- [x] Have human authors review, edit, and validate AI-assisted content, including technical claims, and confirm primary design decisions were made by the human team. The sole author's [validation and decision statement](docs/research/joss06-audit.md#ai-use-inventory-and-human-review) covers these requirements.

- [x] Replace the current draft disclosure with an accurate final statement and obtain author approval. The author-supplied tool names, scope, historical version limit, and responsibility statement are in `paper.md` and the [JOSS-06 audit](docs/research/joss06-audit.md#ai-use-inventory-and-human-review).

**Done for candidate preparation:** The author-approved metadata and disclosures are complete, including the explicit historical version limit. Record the actual submission date only when submitting.

### Submission check 5 — Prepare and validate the submission snapshot

- [ ] Choose and establish the final JOSS paper directory/layout, including `paper.md`, `paper.bib`, and any figures, without breaking repository conventions.

- [ ] Confirm the submission points to a public, browsable repository with an OSI-approved license, accessible issue tracker, and release/archive suitable for citation.

- [ ] Review development history, releases, tests, installation instructions, usage examples, contribution guidance, and support/reporting paths against current JOSS criteria. The [JOSS-03 review](docs/research/joss03-results.md) documents partial checks and remaining public-history and reporting-path gaps.

- [ ] Confirm a colleague who was not the primary developer can install and exercise the documented workflow.

- [ ] Run the project verification suite and the JOSS paper build on the exact candidate revision; retain the logs/artifacts.

- [ ] Check manuscript word count, section headings, citations, author metadata, AI disclosure, and all statements against that candidate release.

- [ ] Record the actual submission date only when submitting; do not use a placeholder or future date.

**Done when:** A tagged or otherwise fixed candidate revision passes the software and manuscript checks, is independently installable, and contains complete submission metadata and artifacts.


## Scope boundary

Phase 1 lists code needed to make the study reproducible and its security claims supportable.
Issues #7, #8, and #36 require a release decision; per-key authorization is conditional on a
future change to the shared-trust model. Phase 4 tracks other product work, including daily AI
audit prerequisites, rotation, and containers. Full production readiness is not a JOSS
acceptance criterion.

# Phase 4 — Product hardening beyond the current study

These issues remain tracked here even when the current JOSS study does not depend on them.
Prioritize backup and recovery (#10), safe errors and startup/shutdown checks (#22–#25),
and the audit schema and integrity work (#11–#19) before product maturity work such as
rotation (#9), resource controls (#20–#21), and containers (#26–#30). Phase 1 contains
the security boundary findings (#7, #8, and #36).

## Cross-cutting maintenance

- [ ] Keep the client and server TLS cipher-suite policies aligned when either policy changes; consider a shared policy or cross-module check.
- [ ] Consider a shared TLS test fixture module to reduce duplicated certificate-authority helpers.

### Issue #9 - Implement Key Rotation and Revocation

#### Objetivo

Adicionar ciclo de vida completo para chaves.

#### Tarefas

- [ ] Rotação
- [ ] Revogação
- [ ] Auditoria correspondente

#### Critérios de Aceitação

- [ ] Chaves podem ser revogadas sem perda histórica

### Issue #10 - Backup and Recovery Validation

#### Objetivo

Garantir recuperação segura.

#### Tarefas

- [ ] Backup completo
- [ ] Restore completo
- [ ] Restore após falha
- [ ] Restore após corrupção parcial

#### Critérios de Aceitação

- [ ] Dados permanecem descriptografáveis

## Product area 3 — Structured Audit

### Issue #11 - Define Audit Event Schema

#### Objetivo

Criar modelo estável de auditoria.

#### Campos

- [ ] eventId
- [ ] sequenceNumber
- [ ] timestamp
- [ ] identity
- [ ] operation
- [ ] resourceId
- [ ] keyId
- [ ] result

#### Critérios de Aceitação

- [ ] Esquema documentado

### Issue #12 - Audit Authentication Events

#### Objetivo

Registrar eventos de autenticação.

#### Tarefas

- [ ] Login bem sucedido
- [ ] Login falho
- [ ] Falha de autorização

#### Critérios de Aceitação

- [ ] Eventos gerados automaticamente

### Issue #13 - Audit Key Lifecycle Events

#### Objetivo

Auditar todo ciclo das chaves.

#### Tarefas

- [ ] Criação
- [ ] Rotação
- [ ] Revogação
- [ ] Remoção

#### Critérios de Aceitação

- [ ] Todas as operações sensíveis auditadas

### Issue #14 - Audit Log Sanitization

#### Objetivo

Eliminar vazamento de dados sensíveis.

#### Tarefas

- [ ] Remover credenciais
- [ ] Remover plaintext
- [ ] Remover material criptográfico

#### Critérios de Aceitação

- [ ] Logs livres de segredos

### Issue #15 - Audit Automated Tests

#### Objetivo

Criar suíte automatizada de auditoria.

#### Critérios de Aceitação

- [ ] Todos os eventos possuem cobertura

## Product area 4 — Verifiable Audit

### Issue #16 - Implement Audit Hash Chain

#### Objetivo

Criar auditoria verificável.

#### Fórmula

```text
H(i)=SHA256(H(i-1)||E(i))
```

#### Critérios de Aceitação

- [ ] Todos os eventos possuem hash encadeado

### Issue #17 - Implement Audit Integrity Verifier

#### Objetivo

Criar ferramenta independente de verificação.

#### Critérios de Aceitação

- [ ] Histórico pode ser validado offline

### Issue #18 - Implement Audit Anchoring

#### Objetivo

Persistir hashes finais em armazenamento independente.

#### Critérios de Aceitação

- [ ] Hashes podem ser comparados externamente

### Issue #19 - Tamper Detection Test Suite

#### Objetivo

Validar resistência à adulteração.

#### Tarefas

- [ ] Modificação
- [ ] Exclusão
- [ ] Truncamento
- [ ] Inserção
- [ ] Reescrita

#### Critérios de Aceitação

- [ ] Todas as fraudes são detectadas

## Product area 5 — Runtime Hardening

### Issue #20 - Connection Limits

#### Objetivo

Prevenir exaustão de recursos.

#### Tarefas

- [ ] Limitar conexões simultâneas
- [ ] Configurar fila de espera

### Issue #21 - Authentication Rate Limiting

#### Objetivo

Mitigar brute force.

#### Tarefas

- [ ] Rate limiting
- [ ] Bloqueio temporário

### Issue #22 - Secure Error Handling

#### Objetivo

Padronizar tratamento de falhas.

#### Critérios de Aceitação

- [ ] Respostas não expõem informações internas

### Issue #23 - Startup Integrity Verification

#### Objetivo

Validar ambiente durante inicialização.

#### Tarefas

- [ ] CANIS_SECRET_KEY
- [ ] Banco
- [ ] Checksums

### Issue #24 - Graceful Shutdown

#### Objetivo

Encerramento consistente.

#### Critérios de Aceitação

- [ ] Sem perda de dados

### Issue #25 - Concurrency and Recovery Tests

#### Objetivo

Validar comportamento sob carga e falhas.

#### Tarefas

- [ ] Testes concorrentes
- [ ] Testes de recuperação

## Product area 6 — Containerization

### Issue #26 - Create Multi-stage Docker Build

#### Objetivo

Reduzir superfície de ataque e tamanho da imagem.

#### Critérios de Aceitação

- [ ] Build multi-stage funcional

### Issue #27 - Run Container as Non-Root

#### Critérios de Aceitação

- [ ] Processo principal não executa como root

### Issue #28 - Externalize Secrets and Persistence

#### Tarefas

- [ ] Certificados
- [ ] Truststore
- [ ] CANIS_SECRET_KEY
- [ ] Persistência

#### Critérios de Aceitação

- [ ] Nenhum segredo embarcado na imagem

### Issue #29 - Harden Container Runtime

#### Tarefas

- [ ] Filesystem readonly
- [ ] Menor privilégio possível

### Issue #30 - Docker Compose Deployment Guide

#### Objetivo

Documentar implantação local.

#### Critérios de Aceitação

- [ ] Ambiente sobe com um único comando
