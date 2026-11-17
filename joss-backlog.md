# CANIS-KMS — JOSS Publication Backlog

> Pending work for a reproducible, reviewable Journal of Open Source Software (JOSS) submission. Production engineering is tracked in the [app backlog](app-prod-grade-backlog.md). Completed work and its limitations remain in the linked research reports; a checked task has been removed from this pending-work list.

## Existing evidence and its boundary

- [JOSS-01 method](docs/research/joss01-method.md) and [results](docs/research/joss01-results.md): one synthetic shared-trust boundary test and a primary-source comparison. They do not establish actual research use or adoption.
- [JOSS-02 claim audit](docs/research/joss02-claim-evidence.md) and [results](docs/research/joss02-results.md): working-tree TLS, file workflow, and security-claim checks. Production Issues #7, #8, and #36 still need a release decision.
- [JOSS-03 reviewer guide](docs/reviewer-guide.md), [method](docs/research/joss03-method.md), and [results](docs/research/joss03-results.md): a synthetic producer-to-recipient demonstration and Windows tests. Independent checkout, published CI, and POSIX execution are unverified.
- [JOSS-04 results](docs/research/joss04-results.md) and [bibliography](joss-papers/paper.bib): four verified bibliography entries. [JOSS-05 results](docs/research/joss05-results.md) and [draft PDF](docs/research/joss05-draft.pdf): an adapted Windows build with rendered references, not the canonical build with final metadata.
- The evidence above was produced on earlier working trees and committed in `0648976`. No tagged candidate release, independent reproduction, or demonstrated research use has been established. The [manuscript](joss-papers/paper.md) and [submission checklist](joss-papers/submission-readiness.md) must be checked again against the eventual candidate.

## Submission decision gates

- [ ] **Gate A — Research fit (P0):** Substantiate the research question, audience, actual research use, and a reason this software is needed beyond existing KMS or envelope-encryption tools. Reassess JOSS fit if use cannot be documented.
- [ ] **Gate B — Supported release (P0):** Fix a candidate revision; verify the TLS-only official workflow and every material paper claim. Resolve or explicitly scope production Issues #7, #8, and #36 without unsupported security claims.
- [ ] **Gate C — Reproducibility (P0):** A non-author can install, test, run the example, and reproduce at least one meaningful evaluation from documented commands and fixtures.
- [ ] **Gate D — Submission artifacts (P1):** Complete the manuscript, references, author metadata, AI disclosure, and public candidate snapshot. Plan the permanent archive; JOSS requests the archive DOI after successful review.

Current [JOSS submission criteria](https://joss.readthedocs.io/en/latest/submitting.html) require demonstrated research use, more than six months of active public development, iterative open-source practice, and reviewable software. The final tagged release and archive DOI are requested after review; preparing a fixed, tested candidate before submission is this project's reproducibility gate.

## Recommended order

1. Establish research use and publication fit (JOSS-01, Gate A).
2. Decide the release security boundary and verify it on a fixed revision (JOSS-02, Gate B).
3. Complete only the evaluations needed for the research question (Issues #31–35, Gate C).
4. Finish installation and CI evidence, paper, bibliography, and author disclosures (JOSS-03–06).
5. Obtain independent reproduction and finalize the submission snapshot (JOSS-07).

# Milestone J1 — Research eligibility and software evidence

## JOSS-01 — Establish and document research significance (P0)

- [ ] Have the authors validate the specific research question, audience, and distinct need for the inspectable asynchronous file-exchange workflow.
- [ ] Document actual research use by the authors or a permitted external pilot: researcher, project, context, software revision, dates, and shareable or editor-permitted evidence. Do not present the synthetic test as adoption.
- [ ] Decide which significance and security claims the candidate release supports; avoid claims of production readiness or novelty in cryptographic primitives.

**Acceptance:** The manuscript supports research use and significance with inspectable evidence and a fair comparison to existing approaches. The existing comparison is in [JOSS-01 results](docs/research/joss01-results.md).

## JOSS-02 — Freeze and verify the research release boundary (P0)

- [ ] On the candidate revision, verify official-client TLS 1.3 mutual authentication, certificate and hostname validation, and no plaintext fallback.
- [ ] Run the complete file workflow and negative TLS, credential, authorization, and storage-tampering tests on that revision; retain sanitized outcomes and explain skips.
- [ ] Resolve or explicitly scope the release implications of production Issues #7, #8, and #36, including database recreation, stored-key validation, and cross-service decryption.
- [ ] Repeat the [claim-evidence audit](docs/research/joss02-claim-evidence.md) against the fixed code and final paper; record the commit, OS, JDK/Maven versions, tests, and known limitations.

**Acceptance:** Every material capability and limitation in the paper matches the tested release, with no known contradiction. A working-tree audit does not close this gate.

# Milestone J2 — Reproducible research evaluation

The original research Issues #31–35 retain their numbers. Scope experiments to the research question; a performance comparison or every fault scenario is not automatically required for JOSS.

## Issue #31 — Create baseline benchmark

- [ ] Define a relevant baseline workload, fixture sizes, measurement method, environment, warm-up, sample count, and analysis before running it.
- [ ] Run the baseline and publish results and limits without inventing performance conclusions.

## Issue #32 — TLS performance benchmark (conditional)

- [ ] Only if it answers the research question, measure latency, throughput, and memory for a documented TLS comparison.
- [ ] Keep any no-TLS baseline in an isolated research harness; never add plaintext to the supported client or server.

## Issue #33 — Multi-service interoperability study

- [ ] Define and test three instances sharing storage, including service identities, key-access boundaries, failure behavior, and expected outcomes. Do not equate shared storage with safe cross-service authorization.

## Issue #34 — Security fault-injection experiments (scoped)

- [ ] Select justified scenarios from master-key theft, storage corruption, and audit tampering.
- [ ] Use synthetic keys and isolated fixtures; report detection, confidentiality, and recovery as separate outcomes. Do not claim tamper-evident audit until it exists and is tested.

## Issue #35 — Research report

- [ ] Publish methods, actual results, discussion, limitations, and reproduction steps under `docs/research/`.
- [ ] Provide exact commands, fixtures, expected outputs, cleanup, environment, and platform-specific limits for each experiment.

# Milestone J3 — Reviewer-ready repository

## JOSS-03 — Installation, examples, documentation, and CI (P0/P1)

- [ ] Verify the documented Java 21/Maven build and external TLS/master-key provisioning from a clean checkout, including non-secret fixtures and any operational setup steps.
- [ ] Verify the committed Ubuntu/Windows CI workflow runs the full tests; confirm POSIX permission and symlink cases actually execute and the report gate passes.
- [ ] Verify public license, issue and contribution routes, a stable private security-reporting route, support expectations, and the required active public development history.

**Acceptance:** A non-author follows the [reviewer guide](docs/reviewer-guide.md), runs the supported synthetic workflow and tests, and records no undocumented steps. The current example uses a TLS server in a test thread; separate-process deployment needs verification only if claimed.

# Milestone J4 — Paper and submission

## JOSS-04 — Restore and verify bibliography (P1)

- [ ] Build the final paper on the fixed candidate with the canonical JOSS workflow; confirm all citations and references resolve and metadata renders correctly.

**Acceptance:** The canonical manuscript build completes without missing references. The adapted [draft PDF](docs/research/joss05-draft.pdf) is partial evidence only.

## JOSS-05 — Align and finalize manuscript (P1)

- [ ] Complete the statement of need, state of the field, software design, and research impact statement using actual JOSS-01 evidence and only those Issues #31–35 that produce relevant results.
- [ ] Check final JOSS sections, word count, formatting, references, and every technical or impact claim against the candidate revision. Keep observed behavior distinct from planned audit, rotation, and container work.

**Acceptance:** All claims in [paper.md](joss-papers/paper.md) have cited evidence from the fixed release or research record; unsupported adoption, performance, and security claims are absent.

## JOSS-06 — Authors, funding, acknowledgements, and AI use (P1)

- [ ] Confirm all authors, affiliations, and substantive contributions; replace manuscript placeholders.
- [ ] Confirm funding, institutional support, acknowledgements, and any conflicts with the authors.
- [ ] Document actual AI tools, versions, and use across code, documentation, and paper; obtain human review, validation, and author approval.
- [ ] Set the actual submission date only when submitting.

**Acceptance:** Metadata and disclosures are complete, accurate, and author-approved.

## JOSS-07 — Independent review and submission snapshot (P1)

- [ ] Have a colleague who was not the primary developer install the candidate, run the example, and reproduce the documented evaluation.
- [ ] Fix and tag a public candidate; prepare a permanent archive and citation plan. After successful JOSS review, archive the reviewed version and record its DOI.
- [ ] Run all project tests and the canonical JOSS paper build on the exact candidate commit; retain sanitized reports, environment details, and artifact hashes.
- [ ] Verify repository public age, active development, actual research use, licensing, issue access, support paths, and other current JOSS screening criteria.
- [ ] Make [submission-readiness.md](joss-papers/submission-readiness.md) or this backlog the single authoritative publication-status checklist and reconcile the other file.

**Acceptance:** A fixed, public, independently reproducible candidate and complete manuscript are ready for editorial screening; the final archive DOI is recorded at the review stage.

## Scope boundary

Production feature implementation remains in the [app backlog](app-prod-grade-backlog.md), especially Issues #7, #8, and #36. JOSS release decisions must account for those findings, but full production readiness is not a JOSS acceptance criterion.