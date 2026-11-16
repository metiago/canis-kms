# CANIS-KMS — JOSS Publication Backlog

> Scope: demonstrate a credible research-software contribution and prepare a reproducible, reviewable submission to the Journal of Open Source Software (JOSS). This is separate from the [App Production-Grade Backlog](app-prod-grade-backlog.md).
>
> Source documents: `paper.md`, `submission-readiness.md`, and the original `backlog.md`. The original research Issues #31–35 are retained below with their issue numbers and unchecked status. New publication tasks use **JOSS-01** etc. to avoid collisions with GitHub issue numbers.

## Submission decision gates

- [ ] **Gate A — Research fit (P0):** Identify a concrete research question, audience, actual research use, and a defensible distinction from existing KMS/envelope-encryption tools. If research use cannot be substantiated, reassess JOSS fit before spending effort on paper polish.
- [ ] **Gate B — Supported release (P0):** Select a candidate revision and verify the features claimed in the paper, including the TLS-only official client/server workflow. Critical security findings #7, #8, and #36 in the [production backlog](app-prod-grade-backlog.md) must be resolved or the scope and limitations of the release must be explicitly reconsidered; do not make unsupported security claims.
- [ ] **Gate C — Reproducibility (P0):** A reviewer can install, test, run an example, and reproduce at least one meaningful research evaluation with documented fixtures and commands.
- [ ] **Gate D — Submission artifacts (P1):** Manuscript, bibliography, metadata, AI disclosure, and release/archive are complete and consistent.

## Recommended order

1. **P0:** JOSS-01 research use and fit; select the paper's precise contribution.
2. **P0:** JOSS-02 candidate release verification; reconcile the paper and checklist with tested TLS behavior and the chosen release boundary.
3. **P0:** Issues #31, #33, and a scoped #34; use #32 only when the comparison answers the research question. Complete #35 with reproducibility evidence.
4. **P1:** JOSS-03 installation/CI, JOSS-04 bibliography, JOSS-05 manuscript, JOSS-06 authors/disclosures.
5. **P1:** JOSS-07 independent verification, versioned release, and submission gate.

---

# Milestone J1 — Research eligibility and software evidence

## JOSS-01 — Establish and document research significance (P0)

**Tasks**
- [ ] Define the specific research question and the researcher/developer audience; explain why the inspectable asynchronous file-exchange workflow adds value beyond configuring established systems.
- [ ] Document real research use by the authors or a permitted external pilot: use case, software version, researcher, context, and evidence. Do not represent hypothetical adoption as actual impact.
- [x] Compare CANIS-KMS with relevant existing approaches (for example envelope-encryption libraries and managed transit engines), fairly stating differences and limitations. See the primary-source comparison in [`docs/research/joss01-results.md`](docs/research/joss01-results.md).
- [ ] Decide which security claims are supported by the candidate release; avoid implying production-grade readiness or novelty in cryptographic primitives.

**Acceptance:** The paper can substantiate research use and significance with concrete, inspectable evidence, and the JOSS eligibility criteria have been reviewed.

**Progress on 2026-11-16:** [JOSS-01 method](docs/research/joss01-method.md) defines a
credential-level shared trust experiment, and [results and comparison](docs/research/joss01-results.md)
record its one-test pass and primary-source comparison. The full Maven suite passed on Windows:
59 client tests (five skips) and 90 server tests (two skips), with no failures or errors.
The research question and audience are documented, but author validation of a distinctive
research need and a real research-use record are still missing. A working-tree security-claim
audit is recorded under JOSS-02, but no fixed candidate release has been audited. Current JOSS
pre-review rules require actual research use;
Gate A and JOSS-01 remain open.

## JOSS-02 — Freeze and verify the research release boundary (P0)

**Tasks**
- [ ] Verify the official Java client uses TLS 1.3/mTLS with hostname and certificate validation, with no plaintext fallback, on the candidate revision (production Issues #1–4).
- [ ] Run the complete end-to-end file workflow and negative security tests against the candidate revision; retain test logs.
- [ ] Resolve or explicitly scope the release implications of production Issues #7, #8, and #36 before describing key protection, recovery, and cross-service authorization in the paper.
- [x] Check the actual key sizes, cryptographic algorithms, envelope and database formats, authorization model, audit behavior, and operational limitations against `paper.md`. Evidence: [claim-evidence matrix](docs/research/joss02-claim-evidence.md), [source/test results](docs/research/joss02-results.md), and revised [`paper.md`](joss-papers/paper.md). This audit covers the current working tree; it must be repeated on the fixed candidate revision.
- [ ] Record the candidate commit, test environment, JDK version, and known limitations.

**Acceptance:** Every material capability and limitation in the manuscript matches the tested release; no known contradiction remains.

**Working-tree progress on 2026-11-16:** The [claim-evidence matrix](docs/research/joss02-claim-evidence.md)
was created before the JOSS-02 manuscript edit. The [method](docs/research/joss02-method.md),
[results](docs/research/joss02-results.md), [suite report](docs/research/joss02-suite-report.csv),
and [test-case report](docs/research/joss02-test-cases.csv) record a Windows 11 / Maven 3.9.12 /
Adoptium JDK 21.0.6 run: 149 tests, 142 passed, seven skipped, zero failures, zero errors.
The [official-client end-to-end test](server/src/test/java/io/canis/ServerEndToEndTest.java)
and [client TLS security tests](client/src/test/java/io/canis/jpaw/client/SocketClientTest.java)
passed; the [shared-trust test](server/src/test/java/io/canis/SharedTrustDomainResearchTest.java)
also passed in a focused rerun. These are working-tree results at HEAD
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81` plus uncommitted research and paper files,
not a tested candidate release. The manuscript now limits claims about runtime database
recreation, stored-key validation, cross-service authorization, and audit integrity. The
[production backlog](app-prod-grade-backlog.md) Issue #7 constructor finding needs
reconciliation with `Server.verifyConfiguration`; Issues #8 and #36 remain open. A fixed
revision, independent and POSIX verification, and a final security-scope decision are still
required. Only the current-tree claim audit is checked; all candidate-release verification
checkboxes remain open.

---

# Milestone J2 — Reproducible research evaluation

The following are **original backlog Issues #31–35**, moved here without marking any work complete. Experiments should be scoped to the actual research question; not every optional experiment is required for a JOSS submission.

# Milestone 7 - Security Research

## Issue #31 - Create Baseline Benchmark

### Objetivo

Gerar linha de base da implementação atual.

---

## Issue #32 - TLS Performance Benchmark

### Métricas

- [ ] Latência
- [ ] Throughput
- [ ] Memória

### Comparação

- [ ] Sem TLS
- [ ] Com TLS

---

## Issue #33 - Multi-Service Interoperability Study

### Objetivo

Validar três instâncias compartilhando armazenamento.

---

## Issue #34 - Security Fault Injection Experiments

### Cenários

- [ ] Roubo da chave mestra
- [ ] Corrupção do armazenamento
- [ ] Adulteração da auditoria

---

## Issue #35 - Research Report

### Objetivo

Publicar resultados em `docs/research`.

### Entregáveis

- [ ] Metodologia
- [ ] Resultados
- [ ] Discussão
- [ ] Limitações
- [ ] Reprodutibilidade

---

---

## Evaluation design and safety notes

- [ ] Pre-register or document measurement methods, hardware/JDK, dataset sizes, sample counts, warm-up, and analysis before interpreting results.
- [ ] Keep any no-TLS baseline in an isolated test harness; never reintroduce plaintext as a supported client transport.
- [ ] For multi-service tests, specify identity and key-authorization boundaries; do not equate shared storage with safe cross-service decryption.
- [ ] For fault-injection experiments, use synthetic keys/data and isolated fixtures; distinguish detection, confidentiality, and recoverability claims.
- [ ] Publish scripts, fixtures, expected commands, results, and limitations under `docs/research/`.

---

# Milestone J3 — Reviewer-ready repository

## JOSS-03 — Installation, examples, documentation, and CI (P0/P1)

- [ ] Provide clean-machine Java 21/Maven build and installation instructions and external TLS/master-key provisioning instructions using non-secret fixtures. The [reviewer guide](docs/reviewer-guide.md), [master-key guide](docs/master-key.md), and [JOSS-03 method](docs/research/joss03-method.md) document commands and generated test fixtures; an independent clean checkout and full operational TLS provisioning are not yet verified.
- [x] Supply a reproducible producer → shared encrypted file → recipient demonstration with expected output. The [focused test](server/src/test/java/io/canis/ReviewerWorkflowTest.java), [Windows runner](docs/research/run-joss03.ps1), [POSIX runner](docs/research/run-joss03.sh), [method](docs/research/joss03-method.md), and [observed Windows result](docs/research/joss03-results.md) cover this synthetic workflow. The server runs in a test thread; a separate-process deployment remains unverified.
- [ ] Ensure CI runs unit and integration tests on supported platforms, including POSIX-specific security checks where applicable. The [CI matrix](.github/workflows/ci.yml) now targets Ubuntu and Windows; the [POSIX report gate](docs/research/VerifyPosixCoverage.java) rejects missing or skipped cases, as verified against the [synthetic fixture](docs/research/joss03-posix-report-fixture.xml) and actual Windows report. The [Windows suite result](docs/research/joss03-results.md) records 150 tests with seven skips. The updated workflow has not run on a published commit and POSIX checks remain unobserved.
- [x] Document architecture, threat model, key-service outage behavior, file-size/memory limits, and supported vs. unsupported features. See the [reviewer guide](docs/reviewer-guide.md), [threat model](docs/threat-model.md), [client whole-file implementation](client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), and [narrow outage check](server/src/test/java/io/canis/ReviewerWorkflowTest.java). No numerical client file-size or reliability limit is claimed.
- [ ] Verify license, issue tracker, contribution guidance, support/security reporting, and public development history. The [MIT license](LICENSE.txt), [contribution guide](CONTRIBUTING.md), and [security guide](SECURITY.md) exist locally. Public issue access, a stable private reporting route, and the required public development history need verification; see [results](docs/research/joss03-results.md).

**Acceptance:** A non-author can follow the README and execute the supported workflow and tests without undocumented steps.

**Working-tree progress on 2026-11-16:** The [pre-manuscript claim matrix](docs/research/joss03-claim-evidence.md),
[method](docs/research/joss03-method.md), and [results with task traceability](docs/research/joss03-results.md)
record a passing focused synthetic example and a Windows Maven suite with 150 tests,
143 passed, seven skipped, zero failures and zero errors. The first focused run failed
because the sandbox denied temporary ACL changes; the authorized rerun passed.
The updated CI workflow has not yet run, and no independent clean-checkout or
POSIX verification was completed. JOSS-03 acceptance remains open.

---

# Milestone J4 — Paper and submission

## JOSS-04 — Restore and verify bibliography (P1)

- [x] Confirm final `paper.md` and `paper.bib` layout and bibliography path. Evidence: colocated [`paper.md`](joss-papers/paper.md) and [`paper.bib`](joss-papers/paper.bib), with `bibliography: paper.bib`; [JOSS-04 results](docs/research/joss04-results.md).
- [x] Add BibTeX entries for every cited standard, tool, and related work; verify metadata against primary sources. Evidence: four entries in [`paper.bib`](joss-papers/paper.bib) and the primary-source table in [JOSS-04 results](docs/research/joss04-results.md).
- [ ] Ensure all citation keys resolve and build the paper using the current JOSS workflow.

**Acceptance:** Manuscript compiles with no missing references.

**Working-tree progress on 2026-11-16:** The [pre-edit claim-evidence matrix](docs/research/joss04-claim-evidence.md)
preceded the sole manuscript change, which set the bibliography path. A static key check found
four cited keys, four entries, and no missing or uncited keys. The full Maven run and focused
shared-trust experiment exited 0 on Windows 11 / Adoptium JDK 21.0.6 / Maven 3.9.12; the
Surefire XML reports 149 tests, zero failures, zero errors, and seven skips. The
[JOSS-04 method](docs/research/joss04-method.md) documents the intended Inara build, but
the WSL container engine could not reach Docker Hub to pull Inara 1.3.1, so no PDF was built.
The [results and traceability](docs/research/joss04-results.md) record that earlier limitation.
The later [JOSS-05 adapted Windows build](docs/research/joss05-results.md) produced an
[inspected draft PDF](docs/research/joss05-draft.pdf) with all four references rendered.
The final JOSS-04 checkbox and acceptance criterion remain open until the canonical
JOSS workflow builds the fixed candidate revision with final metadata.

## JOSS-05 — Align and finalize manuscript (P1)

- [x] Update `paper.md` to describe the verified TLS transport and actual key-authorization model; remove stale plaintext-client statements. Evidence: [JOSS-05 pre-edit claim matrix](docs/research/joss05-claim-evidence.md), [manuscript](joss-papers/paper.md), [fresh working-tree results](docs/research/joss05-results.md), and [client TLS](client/src/test/java/io/canis/jpaw/client/SocketClientTest.java), [end-to-end](server/src/test/java/io/canis/ServerEndToEndTest.java), and [shared-trust](server/src/test/java/io/canis/SharedTrustDomainResearchTest.java) tests. Fixed-release verification remains under JOSS-02.
- [ ] Complete the statement of need, state of the field, software design, and research impact statement using actual evidence from JOSS-01 and Issues #31–35.
- [x] Separate observed results from planned features (including tamper-evident audit, rotation, and container hardening). Evidence: limited claims in [paper.md](joss-papers/paper.md), corrected [CANISP audit documentation](docs/canisp-protocol.md) and [README persistence wording](README.md), and the [JOSS-05 result/claim audit](docs/research/joss05-results.md). Production implementation and its security findings remain in the production backlog.
- [ ] Check current JOSS word count, required sections, and formatting.

**Acceptance:** All technical and impact claims are supported by the fixed release and cited evidence.

**Working-tree progress on 2026-11-16:** The [JOSS-05 method](docs/research/joss05-method.md)
and [results](docs/research/joss05-results.md) record a fresh Windows 11 / Maven 3.9.12 /
Adoptium JDK 21.0.6 run: 149 tests, 142 passed, seven skipped, zero failures or errors,
plus a passing focused one-test shared-trust experiment. Sanitized [suite](docs/research/joss05-suite-report.csv)
and [case](docs/research/joss05-test-cases.csv) reports retain the outcomes. The
[static paper check](docs/research/check-joss05-paper.ps1) passed: an approximate
1,263 body words, eight expected headings, and four matching citation keys and
bibliography entries. The [local Windows build script](docs/research/build-joss05-windows.ps1)
used Pandoc 3.14 and portable MiKTeX with the cloned Inara v1.3.1 source to
produce an [inspected three-page draft PDF](docs/research/joss05-draft.pdf).
All four references rendered, including the corrected NIST institution name.
This is partial evidence for the final checkbox: GNU make was unavailable,
TeX Live and the Hack font were unavailable, and the script used documented
Windows overrides. The canonical JOSS workflow and final metadata still need
verification on the candidate revision. No actual research-use record, fixed candidate
release, author-approved metadata, or results for Issues #31–35 were established.
The statement of need/impact checkbox and JOSS-05 acceptance remain open.

## JOSS-06 — Authors, funding, acknowledgements, and AI use (P1)

- [ ] Confirm author list, affiliations, and substantive contributions; replace placeholders.
- [ ] Confirm funding, institutional support, and acknowledgements with all authors.
- [ ] Document actual generative AI tools, versions, and use across code/docs/manuscript; ensure human review and author approval.
- [ ] Set the actual submission date at submission, not in advance.

**Acceptance:** Metadata and disclosures are accurate and approved.

## JOSS-07 — Independent review and submission snapshot (P1)

- [ ] Ask a colleague who was not the primary developer to install, run the example, and reproduce the documented evaluation.
- [ ] Tag a stable candidate release and prepare an appropriate public archive/citation record.
- [ ] Run all project tests and the JOSS manuscript build on the exact tagged commit; retain evidence.
- [ ] Review repository age, research use, licensing, support paths, and other current JOSS screening requirements before submitting.
- [ ] Update `submission-readiness.md` or replace it with this checklist as the single source of publication status.

**Acceptance:** A fixed, public, independently reproducible release and a complete manuscript are ready for editorial screening.

---

## Scope explicitly excluded from this JOSS backlog

Production feature implementation remains in [App Production-Grade Backlog](app-prod-grade-backlog.md): #7–10, #11–19 (structured/verifiable audit), #20–25 (runtime hardening), #26–30 (containerization), and #36 (key exposure/authorization), together with the completed TLS and key-protection history #1–6. JOSS tasks may cite their outcomes but do not duplicate implementation checklists. Full production readiness is **not** a JOSS acceptance criterion.
