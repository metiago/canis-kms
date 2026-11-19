# CANIS-KMS JOSS submission readiness

**Can the project be submitted today? No.** The current [publication backlog](../joss-backlog.md) is limited to JOSS screening, review, manuscript, and release reproducibility. Its [open-item triage](../docs/research/joss-backlog-triage.md) accounts for every unchecked item in the former unified backlog. Product tasks are preserved in the [product backlog](../app-prod-grade-backlog.md).

Current candidate: public prerelease [v0.1.0-rc.1](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1), commit e1d6998b5c5658750870b39e2a4d511de5bed7f3. The [release record](../docs/research/release-candidate.md) confirms the archive and checksum. The [JOSS-07 snapshot](../docs/research/joss07-snapshot.md) verifies exact-tag Windows tests and an adapted paper render. The later [independent review](../docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) used a clean later commit with the same executable source, but not the tag itself.

Current criteria: [JOSS submission](https://joss.readthedocs.io/en/latest/submitting.html), [review](https://joss.readthedocs.io/en/latest/review_criteria.html), [checklist](https://joss.readthedocs.io/en/latest/review_checklist.html), and [paper format](https://joss.readthedocs.io/en/latest/paper.html). Editorial judgments about research impact and open development cannot be replaced by a checklist assertion.

# Required for JOSS Submission

| Area | Status | Minimum remaining work |
| --- | --- | --- |
| Active public development history | Open; JOSS screening requirement | Establish when the repository became public and whether active, iterative public development spans more than six months. The creation date and current public status in the [snapshot](../docs/research/joss07-snapshot.md#requirement-matrix) do not prove this. The public repository currently shows no issues or pull requests; a single-author project can use multiple other real open-development indicators, but retrospective paperwork cannot create missing history. |
| Research use and significance | Partially evidenced; JOSS screening requirement | Present the bounded developer research use from [JOSS-01](../docs/research/joss01-results.md), [Issue #32](../docs/research/issue32-results.md), and [Issue #35](../docs/research/issue35-results.md) as a credible software-impact case. The full original study is not required merely to submit. Editors decide sufficiency. |
| Software-focused paper | Open; JOSS submission and review requirement | Revise [paper.md](paper.md) to foreground the software, scholarly need, field comparison, design trade-offs, and research impact; trim the results-heavy narrative and preserve the limits of dirty-tree studies. Add a reference or direct link to the existing released source archive. |
| Final paper metadata and render | Open for the revised paper | Set the actual submission date in JOSS format at submission. Recheck the required sections, 750–1750 word guidance, citations, references, links, and rendered author/date metadata. A particular local Inara command is not itself a JOSS requirement. |
| Public repository, MIT license, issue route, contribution and support guidance | Satisfied for the current candidate | [License](../LICENSE.txt), [CONTRIBUTING](../CONTRIBUTING.md), [README](../README.md), and [SECURITY](../SECURITY.md) document the public routes and support limits; the [snapshot](../docs/research/joss07-snapshot.md#requirement-matrix) verifies the public repository and Issues route. |
| Reviewable software | Satisfied for the current executable tree | Exact-tag [Windows tests](../docs/research/joss07-snapshot.md#requirement-matrix) passed; the later [non-author clean-checkout review](../docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) passed 159 tests and a separate-process example. Recheck changed code if the submission revision changes executable behavior. |

# Required for JOSS Acceptance

| Area | Status | Minimum remaining work |
| --- | --- | --- |
| Final claim traceability and rendered paper | Open only for changes after the [tagged-paper audit](../docs/research/joss02-final-claim-audit.md) | Reconcile revised statements with fixed source, tests, and bounded research evidence; render the final paper and inspect references and metadata. |
| Public review response | Future review stage | Address substantiated editor and reviewer findings through the JOSS review and repository issue routes. |
| Final release and archive DOI | Future post-review stage | After successful review, tag the reviewed version, archive it with a data-archiving service, and report the version and DOI. The DOI is not required before submission. |

| Specific area | Classification | Evidence-based status |
| --- | --- | --- |
| Active public development history | Required by JOSS | Open: public period and distributed activity not established. |
| Private security reporting route | Project-specific pre-submission commitment | Open in [SECURITY.md](../SECURITY.md); JOSS requires issue/problem and support routes, not a separate private vulnerability channel. The maintainer must confirm the route or explicitly revise that project commitment. |
| Independent review evidence | Reviewability evidence, not an exact-tag mandate | Completed on a clean later commit with the same executable source; exact-tag non-author execution remains optional. |
| Release candidate | Useful fixed revision; final release required after review | Public v0.1.0-rc.1 and exact-tag software checks verified. |
| Canonical paper build | Verification method, not a prescribed local command | Exact-tag adapted PDF rendered four references; render and inspect the revised paper before submission. |
| GitHub release | Open-development signal; not a separate submission form prerequisite | Public prerelease and source archive verified. |
| Claim traceability | Required for manuscript accuracy and review | Tagged paper audited; recheck revised claims. |
| Author metadata | Required by JOSS | Author and affiliation are approved and present; actual submission date remains. |
| AI disclosure | Required by JOSS | Author-approved disclosure is present, including known historical version limits. |

# Optional Improvements

The original local controls, scheduled outage series, full fault matrix, additional fixed-revision reruns, exact-tag non-author rerun, operator-provisioned long-running server exercise, static paper-checker repair, and JReleaser publication-path repair can improve future evidence or operations. They do not block this bounded software paper unless a revised claim or a reviewer request makes one material. The stable private vulnerability route is not a stated JOSS rule, but [SECURITY.md](../SECURITY.md) still makes it a project pre-submission commitment pending a maintainer decision.

# Post-Publication Product Work

[app-prod-grade-backlog.md](../app-prod-grade-backlog.md) now holds detailed Issues #7–#30 and #36, including rotation, backup/recovery, audit integrity, runtime limits, and container deployment. Their absence remains a disclosed implementation limit; this publication plan makes no production-readiness claim.

**Minimum blockers today:** evidence of sufficient active public development; a software-focused paper with a convincing bounded research-impact case and released-source reference; and final paper metadata/render verification. No new experiment or product code change is prescribed by the current JOSS criteria for these blockers.
