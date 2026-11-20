# CANIS-KMS JOSS submission readiness

**Can the project be submitted today? No.** The current [publication backlog](../joss-backlog.md) is limited to JOSS screening, review, manuscript, and release reproducibility. Its [open-item triage](../docs/research/joss-backlog-triage.md) accounts for every unchecked item in the former unified backlog. Product tasks are preserved in the [product backlog](../app-prod-grade-backlog.md).

Current candidate: public prerelease [v0.1.0-rc.1](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1), commit e1d6998b5c5658750870b39e2a4d511de5bed7f3. The [release record](../docs/research/release-candidate.md) confirms the archive and checksum. The [JOSS-07 snapshot](../docs/research/joss07-snapshot.md) verifies exact-tag Windows tests and an adapted paper render. The later [independent review](../docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) used a clean later commit with the same executable source, but not the tag itself.

Current criteria: [JOSS submission](https://joss.readthedocs.io/en/latest/submitting.html), [review](https://joss.readthedocs.io/en/latest/review_criteria.html), [checklist](https://joss.readthedocs.io/en/latest/review_checklist.html), and [paper format](https://joss.readthedocs.io/en/latest/paper.html). Editorial judgments about research impact and open development cannot be replaced by a checklist assertion.

# Required for JOSS Submission

| Area | Status | Minimum remaining work |
| --- | --- | --- |
| Active public development history | Open; JOSS screening requirement | Establish when the repository became public and whether active, iterative public development spans more than six months. The creation date and current public status in the [snapshot](../docs/research/joss07-snapshot.md#requirement-matrix) do not prove this. The public repository currently shows no issues or pull requests; a single-author project can use multiple other real open-development indicators, but retrospective paperwork cannot create missing history. |
| Research use and significance | Bounded developer use evidenced; editorial judgment remains | The revised paper presents [JOSS-01](../docs/research/joss01-results.md), [Issue #32](../docs/research/issue32-results.md), and [Issue #35](../docs/research/issue35-results.md) within their limits. The full original study is not required merely to submit; editors decide sufficiency. |
| Software-focused paper | Frozen in manuscript commit `a01872258cb6d027512e15f71ed9821b136615c8`; not yet a fixed public submission revision | The [final validation](../docs/research/joss-final-paper-validation.md) records software framing, field comparison, design trade-offs, bounded impact, and the released-source citation. The [release record](../docs/research/joss-final-manuscript-release.md) records source and PDF hashes. Publish the commit without moving the release tag. |
| Final paper metadata and render | JOSS-compatible preview rendered and inspected | The 900-token manuscript and five references rendered in the documented Windows Inara fallback. The preview displays Inara's default 1970/DOI placeholders; provide the actual submission date and journal metadata in the JOSS submission/review workflow. |
| Public repository, MIT license, issue route, contribution and support guidance | Satisfied for the current candidate | [License](../LICENSE.txt), [CONTRIBUTING](../CONTRIBUTING.md), [README](../README.md), and [SECURITY](../SECURITY.md) document the public routes and support limits; the [snapshot](../docs/research/joss07-snapshot.md#requirement-matrix) verifies the public repository and Issues route. |
| Reviewable software | Satisfied for the current executable tree | Exact-tag [Windows tests](../docs/research/joss07-snapshot.md#requirement-matrix) passed; the later [non-author clean-checkout review](../docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) passed 159 tests and a separate-process example. Recheck changed code if the submission revision changes executable behavior. |

# Required for JOSS Acceptance

| Area | Status | Minimum remaining work |
| --- | --- | --- |
| Final claim traceability and rendered paper | Prepared for the current working-tree revision | [Final paper validation](../docs/research/joss-final-paper-validation.md) reconciles changed prose with fixed source and bounded evidence, and inspects the three-page PDF, citations, links, and preview metadata. |
| Public review response | Future review stage | Address substantiated editor and reviewer findings through the JOSS review and repository issue routes. |
| Final release and archive DOI | Future post-review stage | After successful review, tag the reviewed version, archive it with a data-archiving service, and report the version and DOI. The DOI is not required before submission. |

| Specific area | Classification | Evidence-based status |
| --- | --- | --- |
| Active public development history | Required by JOSS | Open: public period and distributed activity not established. |
| Private security reporting route | Project-specific pre-submission commitment | Closed: the author-approved email, private GitHub alternative, disclosure process, and response targets are documented in [SECURITY.md](../SECURITY.md). The [JOSS-07 snapshot](../docs/research/joss07-snapshot.md#requirement-matrix) records the older 2026-11-19 status. |
| Independent review evidence | Reviewability evidence, not an exact-tag mandate | Completed on a clean later commit with the same executable source; exact-tag non-author execution remains optional. |
| Release candidate | Useful fixed revision; final release required after review | Public v0.1.0-rc.1 and exact-tag software checks verified. |
| Canonical paper build | Verification method, not a prescribed local command | Revised paper rendered through the repository-supported adapted Inara JOSS workflow with five references; canonical POSIX build remains optional. |
| GitHub release | Open-development signal; not a separate submission form prerequisite | Public prerelease and source archive verified. |
| Claim traceability | Required for manuscript accuracy and review | Tagged paper audited; revised prose checked in [final validation](../docs/research/joss-final-paper-validation.md). |
| Author metadata | Required by JOSS | Author and affiliation are approved and present; actual submission date remains. |
| AI disclosure | Required by JOSS | Author-approved disclosure is present, including known historical version limits. |

## Security reporting confirmation

| Requirement | Evidence | Status |
| --- | --- | --- |
| Private security contact | Author-approved `metiago.work@gmail.com` in [SECURITY.md](../SECURITY.md); GitHub private reporting remains an alternative when available. | Configured |
| Responsible disclosure | [SECURITY.md](../SECURITY.md) directs private reports, prohibits public posting of sensitive details, and asks reporters to coordinate disclosure timing. | Documented |
| Acknowledgement and initial response | [SECURITY.md](../SECURITY.md) sets a 7-calendar-day communication target. | Documented |
| Follow-up | [SECURITY.md](../SECURITY.md) asks for private follow-up after 30 calendar days without a substantive update. | Documented |
| Scope | [SECURITY.md](../SECURITY.md) retains proof-of-concept limits and no production support or remediation deadline. | Preserved |

# Optional Improvements

The original local controls, scheduled outage series, full fault matrix, additional fixed-revision reruns, exact-tag non-author rerun, operator-provisioned long-running server exercise, static paper-checker repair, and JReleaser publication-path repair can improve future evidence or operations. They do not block this bounded software paper unless a revised claim or a reviewer request makes one material. The author-approved private reporting route now closes the project's separate security-contact commitment.

# Post-Publication Product Work

[app-prod-grade-backlog.md](../app-prod-grade-backlog.md) now holds detailed Issues #7–#30 and #36, including rotation, backup/recovery, audit integrity, runtime limits, and container deployment. Their absence remains a disclosed implementation limit; this publication plan makes no production-readiness claim.

**Minimum blockers today:** establish more than six months of active public development and publish the frozen manuscript commit as an identifiable submission revision. Actual JOSS submission metadata is supplied when submitting; the local PDF retains Inara's preview placeholders as documented in the [manuscript release record](../docs/research/joss-final-manuscript-release.md). The [final paper validation](../docs/research/joss-final-paper-validation.md) closes the manuscript-content, citation, and preview-render checks; the private security route is documented in [SECURITY.md](../SECURITY.md). No new experiment or product code change is prescribed.
