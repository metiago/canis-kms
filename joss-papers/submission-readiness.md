# CANIS-KMS JOSS Submission Readiness

This checklist tracks the work needed to turn the current manuscript into a supportable JOSS submission. Update each item with links to issues, pull requests, releases, and evidence as it progresses. Keep the manuscript aligned with the released implementation; do not treat planned work as a current capability.

## 1. Restore and verify the bibliography

- [x] Confirm the final manuscript and bibliography locations. The files are colocated as [`paper.md`](paper.md) and [`paper.bib`](paper.bib), and YAML uses `bibliography: paper.bib`; see [JOSS-04 results](../docs/research/joss04-results.md).
- [x] Add a BibTeX entry for every citation used in `paper.md`, including the cryptographic standards and software alternatives discussed there. Four cited keys match four entries in [`paper.bib`](paper.bib); see [JOSS-04 results](../docs/research/joss04-results.md).
- [x] Verify each entry against its publisher or official project source, including title, author or organization, available date/version, and stable URL or DOI. The [source table](../docs/research/joss04-results.md) records the verification and notes that the Vault page has no stated publication date.
- [ ] Check that every bibliography entry is cited and every citation resolves; compile the manuscript with the JOSS toolchain. The [JOSS-05 static check and adapted local PDF build](../docs/research/joss05-results.md) found four matching keys and four rendered references in an [inspected draft](../docs/research/joss05-draft.pdf). The canonical GNU make/TeX Live/Hack-font build and fixed candidate revision remain unverified.

**Done when:** The paper compiles through the JOSS workflow with complete, verified references and no unresolved citation keys.

The [JOSS-04 method](../docs/research/joss04-method.md) gives the WSL and POSIX build commands,
prerequisites, expected PDF, cleanup, and platform limits. The [pre-edit claim-evidence matrix](../docs/research/joss04-claim-evidence.md)
and [results](../docs/research/joss04-results.md) cover the current dirty working tree. A build on
the fixed candidate revision remains part of the submission snapshot review below.
The [JOSS-05 local Inara method](../docs/research/joss05-method.md) records the
standard local command and the executed Windows direct-Pandoc fallback.

## 2. Make the supported client/server transport coherent

- [ ] Review the server's TLS 1.3 mutual-authentication requirements and the client's current TLS `SocketClient` implementation against a fixed candidate revision.
- [x] Implement client-side TLS 1.3 using externally supplied trust and identity material, with server certificate and hostname validation. Retain the project's no-plaintext-fallback requirement. Evidence: [`SocketClient.java`](../client/src/main/java/io/canis/jpaw/client/SocketClient.java), [`TlsContextFactory.java`](../client/src/main/java/io/canis/jpaw/config/TlsContextFactory.java), and the [JOSS-02 test results](../docs/research/joss02-results.md).
- [x] Document required client and server configuration without embedding credentials or secret material. Evidence: [reviewer guide](../docs/reviewer-guide.md), [README](../README.md), and [master-key guide](../docs/master-key.md). The synthetic [JOSS-03 test](../server/src/test/java/io/canis/ReviewerWorkflowTest.java) generates its own fixtures. Fixed-release verification remains open.
- [x] Add and run client transport tests for a trusted server/client pair, hostname or certificate rejection, untrusted client rejection, TLS-version enforcement, and failure behavior. Evidence: [`SocketClientTest.java`](../client/src/test/java/io/canis/jpaw/client/SocketClientTest.java) and the [JOSS-02 suite report](../docs/research/joss02-suite-report.csv) (14 tests, zero failures, zero errors, zero skips on Windows).
- [x] Run an end-to-end test through the supported Java client against the TLS server, covering file encryption, server-side key unwrap, and file decryption. Evidence: [`ServerEndToEndTest.java`](../server/src/test/java/io/canis/ServerEndToEndTest.java) and the [JOSS-02 suite report](../docs/research/joss02-suite-report.csv) (four tests, zero failures, zero errors, zero skips on Windows).
- [ ] Update the paper and deployment documentation to describe only the verified release behavior.

**Done when:** The documented Java client completes the file workflow against the TLS-only server and negative transport tests demonstrate fail-closed behavior.

The checked implementation and test items above describe the 2026-11-16 working tree, not a
fixed candidate release. The [JOSS-02 claim matrix](../docs/research/joss02-claim-evidence.md),
[method](../docs/research/joss02-method.md), and [results](../docs/research/joss02-results.md)
record the evidence and platform limits. The fixed-revision review, configuration documentation
audit, and release-aligned paper/deployment documentation items remain unchecked. Production
Issues #8 and #36 and the Issue #7 constructor follow-up still need a release decision.

## 3. Establish and document JOSS-level research significance

- [ ] Identify the specific research question and audience for which this software provides value beyond configuring an established KMS or envelope-encryption library. A candidate question and audience are documented in [`joss01-results.md`](../docs/research/joss01-results.md); the distinct research need still needs author validation.
- [ ] Seek a real pilot use by a researcher or an integration in a research workflow; record the context, software version, and permission to describe it.
- [x] Define the shared trust boundary evaluation protocol before execution in [`joss01-method.md`](../docs/research/joss01-method.md).
- [x] Publish scripts, generated fixture procedure, environment, and reproduction commands in [`docs/research`](../docs/research/joss01-method.md).
- [x] Report only the observed categorical result and its limitations in [`joss01-results.md`](../docs/research/joss01-results.md). The synthetic test is not adoption or a performance measurement.
- [x] Reassess JOSS fit against current screening rules: without an actual research-use record, the submission is not yet eligible; keep the research-impact gate open.

**Done when:** The manuscript can cite specific research use or a reproducible evaluation, and its contribution is clearly distinguished from existing tools without unsupported novelty claims.

## 4. Complete author, funding, and AI disclosures

- [ ] Confirm the complete author list and each author's substantive contribution to the software and manuscript.
- [ ] Replace YAML name and affiliation placeholders with author-approved metadata and valid JOSS formatting.
- [ ] Ask all authors to confirm funding, institutional support, and acknowledgements; state these accurately.
- [ ] Record generative AI tools and versions used in software, documentation, and manuscript work, with the scope of each use.
- [ ] Have human authors review, edit, and validate AI-assisted content, including technical claims, and confirm primary design decisions were made by the human team.
- [ ] Replace the current draft disclosure with an accurate final statement and obtain author approval.

**Done when:** No placeholders or unresolved disclosure questions remain, and all authors approve the metadata, acknowledgements, and AI statement.

## 5. Prepare and validate the submission snapshot

- [ ] Choose and establish the final JOSS paper directory/layout, including `paper.md`, `paper.bib`, and any figures, without breaking repository conventions.
- [ ] Confirm the submission points to a public, browsable repository with an OSI-approved license, accessible issue tracker, and release/archive suitable for citation.
- [ ] Review development history, releases, tests, installation instructions, usage examples, contribution guidance, and support/reporting paths against current JOSS criteria. The [JOSS-03 review](../docs/research/joss03-results.md) documents partial checks and remaining public-history and reporting-path gaps.
- [ ] Confirm a colleague who was not the primary developer can install and exercise the documented workflow.
- [ ] Run the project verification suite and the JOSS paper build on the exact candidate revision; retain the logs/artifacts.
- [ ] Check manuscript word count, section headings, citations, author metadata, AI disclosure, and all statements against that candidate release.
- [ ] Record the actual submission date only when submitting; do not use a placeholder or future date.

**Done when:** A tagged or otherwise fixed candidate revision passes the software and manuscript checks, is independently installable, and contains complete submission metadata and artifacts.

**JOSS-05 working-tree evidence:** The [pre-edit claim matrix](../docs/research/joss05-claim-evidence.md),
[method](../docs/research/joss05-method.md), and [results](../docs/research/joss05-results.md)
support updated TLS/shared-authorization wording and a clear separation of
observed behavior from planned audit and rotation work. The full Windows suite
reported 149 tests with zero failures/errors and seven skips; the focused
shared-trust test passed. The static manuscript check found approximately
1,263 body words, all expected headings, and four matched citation keys;
author/affiliation placeholders remain. A [three-page adapted draft](../docs/research/joss05-draft.pdf)
was built with Pandoc 3.14, portable MiKTeX, and Inara v1.3.1, and its four
references were inspected. GNU make, TeX Live, and the Hack font were unavailable,
so the canonical build is still pending. Actual research use, release-level claim
validation, final metadata and formatting, and author approval remain open; the combined
submission-snapshot checkbox above stays unchecked.

**JOSS-03 working-tree evidence:** The [pre-manuscript claim matrix](../docs/research/joss03-claim-evidence.md),
[method](../docs/research/joss03-method.md), [focused reviewer example](../server/src/test/java/io/canis/ReviewerWorkflowTest.java),
and [results](../docs/research/joss03-results.md) document generated non-secret fixtures,
expected output, one passing Windows producer-to-recipient workflow, and a full Windows
suite of 150 tests (143 passed, seven skipped). The [reviewer guide](../docs/reviewer-guide.md)
and [CI matrix](../.github/workflows/ci.yml) are present locally. A clean non-author
checkout, actual CI/POSIX runs, fixed-release verification, public development-history
review, and confirmed private security-reporting route remain open. These results do
not establish research use or submission readiness.

## Suggested order and dependencies

1. Start item 3's research-use assessment early; it may determine whether a JOSS submission is supportable at all.
2. Verify item 2 against a fixed release before claiming an integrated deployment or collecting end-to-end performance measurements. The TLS implementation history is tracked in [the production backlog](../app-prod-grade-backlog.md); use this checklist to record completion evidence for JOSS.
3. Complete item 1 alongside the manuscript evidence work; citations should support the final comparison and methods.
4. Complete item 4 with the author team once the software and paper scope are stable.
5. Complete item 5 last against a fixed candidate release, then submit only if all prior completion criteria are met.

## Current known blockers

- The bibliography has four primary-source-verified entries and all four render in the [adapted draft PDF](../docs/research/joss05-draft.pdf). The [JOSS-05 results](../docs/research/joss05-results.md) document its visual/reference inspection and font/TeX substitutions. A canonical build on the fixed candidate revision with final metadata remains open.
- The [JOSS-02 Windows working-tree results](../docs/research/joss02-results.md) show a passing TLS 1.3 official-client file workflow and negative transport tests; fixed-release verification, POSIX permission coverage, and final checklist reconciliation remain open.
- The [JOSS-03 working-tree result](../docs/research/joss03-results.md) adds a reproducible synthetic reviewer example, but updated CI, POSIX security coverage, independent installation, and public-history checks remain unverified.
- No external adoption, actual research use, publication, or measured performance result was established from the repository materials reviewed. A synthetic shared-trust boundary evaluation is documented separately.
- Author and affiliation placeholders remain, and project funding and AI use outside this manuscript still require author confirmation.
