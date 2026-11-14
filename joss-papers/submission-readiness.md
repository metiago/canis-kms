# CANIS-KMS JOSS Submission Readiness

This checklist tracks the work needed to turn the current manuscript into a supportable JOSS submission. Update each item with links to issues, pull requests, releases, and evidence as it progresses. Keep the manuscript aligned with the released implementation; do not treat planned work as a current capability.

## 1. Restore and verify the bibliography

- [ ] Confirm the final manuscript and bibliography locations. JOSS expects `paper.md` and `paper.bib` to be available together in the submission repository; choose a layout and make the YAML path accurate.
- [ ] Add a BibTeX entry for every citation used in `paper.md`, including the cryptographic standards and software alternatives discussed there.
- [ ] Verify each entry against its publisher or official project source, including title, author or organization, date/version, and stable URL or DOI.
- [ ] Check that every bibliography entry is cited and every citation resolves; compile the manuscript with the JOSS toolchain.

**Done when:** The paper compiles through the JOSS workflow with complete, verified references and no unresolved citation keys.

## 2. Make the supported client/server transport coherent

- [ ] Review the server's TLS 1.3 mutual-authentication requirements and the client's current plaintext `SocketClient` implementation.
- [ ] Implement client-side TLS 1.3 using externally supplied trust and identity material, with server certificate and hostname validation. Retain the project's no-plaintext-fallback requirement.
- [ ] Document required client and server configuration without embedding credentials or secret material.
- [ ] Add and run client transport tests for a trusted server/client pair, hostname or certificate rejection, untrusted client rejection, TLS-version enforcement, and failure behavior.
- [ ] Run an end-to-end test through the supported Java client against the TLS server, covering file encryption, server-side key unwrap, and file decryption.
- [ ] Update the paper and deployment documentation to describe only the verified release behavior.

**Done when:** The documented Java client completes the file workflow against the TLS-only server and negative transport tests demonstrate fail-closed behavior.

## 3. Establish and document JOSS-level research significance

- [ ] Identify the specific research question and audience for which this software provides value beyond configuring an established KMS or envelope-encryption library.
- [ ] Seek a real pilot use by a researcher or an integration in a research workflow; record the context, software version, and permission to describe it.
- [ ] If a reproducible evaluation is appropriate, define the protocol before collecting results. Candidate questions include file-size/latency behavior, recovery after key-store failure, or properties of the shared trust-domain model.
- [ ] Publish the scripts, environment, input data or generated fixtures, and procedures needed to reproduce any reported results.
- [ ] Report only observed results, with limitations. Do not claim impact from planned work, hypothetical users, or unrun experiments.
- [ ] Reassess JOSS fit if there is no evidence of research use or other credible significance after this work.

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
- [ ] Review development history, releases, tests, installation instructions, usage examples, contribution guidance, and support/reporting paths against current JOSS criteria.
- [ ] Confirm a colleague who was not the primary developer can install and exercise the documented workflow.
- [ ] Run the project verification suite and the JOSS paper build on the exact candidate revision; retain the logs/artifacts.
- [ ] Check manuscript word count, section headings, citations, author metadata, AI disclosure, and all statements against that candidate release.
- [ ] Record the actual submission date only when submitting; do not use a placeholder or future date.

**Done when:** A tagged or otherwise fixed candidate revision passes the software and manuscript checks, is independently installable, and contains complete submission metadata and artifacts.

## Suggested order and dependencies

1. Start item 3's research-use assessment early; it may determine whether a JOSS submission is supportable at all.
2. Complete item 2 in [`backlog.md`, Issue #2](../backlog.md) before claiming an integrated deployment or collecting measurements of the end-to-end workflow. Keep implementation and transport tests in that backlog issue; use this checklist to record completion evidence for JOSS.
3. Complete item 1 alongside the manuscript evidence work; citations should support the final comparison and methods.
4. Complete item 4 with the author team once the software and paper scope are stable.
5. Complete item 5 last against a fixed candidate release, then submit only if all prior completion criteria are met.

## Current known blockers

- The repository currently has `joss-papers/paper.md`, but no bibliography file was found during manuscript review.
- The server enforces TLS 1.3 and client-certificate authentication; the official Java client currently uses a plaintext TCP socket.
- No external adoption, research deployment, publication, or measured performance result was established from the repository materials reviewed.
- Author and affiliation placeholders remain, and project funding and AI use outside this manuscript still require author confirmation.
