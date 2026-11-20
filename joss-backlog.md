# CANIS-KMS JOSS publication backlog

This is the active publication checklist for the Journal of Open Source Software (JOSS). It tracks only work material to editorial screening, review, manuscript correctness, and reproducibility. The detailed [product backlog](app-prod-grade-backlog.md) and [research evidence index](docs/research/README.md) remain separate. The [per-item triage](docs/research/joss-backlog-triage.md) classifies every item that was open in the former unified backlog.

Current public candidate: [v0.1.0-rc.1](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1), commit e1d6998b5c5658750870b39e2a4d511de5bed7f3. Its source and paper are fixed; the tag must not be moved. A later paper revision can be submitted from a new commit without pretending it was part of this tag. The candidate's [release record](docs/research/release-candidate.md), [exact-tag snapshot](docs/research/joss07-snapshot.md), and [independent review](docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) have distinct provenance.

Criteria used: [JOSS submission requirements](https://joss.readthedocs.io/en/latest/submitting.html), [review criteria](https://joss.readthedocs.io/en/latest/review_criteria.html), [review checklist](https://joss.readthedocs.io/en/latest/review_checklist.html), and [paper format](https://joss.readthedocs.io/en/latest/paper.html). JOSS requires more than six months of active public development, demonstrated research use, open development practices, reviewable software, and a concise software paper. A full completion of the original CANIS empirical study, production readiness, a private vulnerability channel, or a particular local paper-build command is not a JOSS rule.

# Required for JOSS Submission

| Task | Blocking Submission? | Blocking Acceptance? | Status and smallest remaining action |
| --- | --- | --- | --- |
| Establish sufficient active public and iterative development history | Yes | Yes | Open. Verify when the repository became public and whether visible development spans more than six months. The [JOSS-07 snapshot](docs/research/joss07-snapshot.md#requirement-matrix) establishes creation date and current visibility, not the public period; commit activity is sparse and there are no public issues or pull requests. If the history is insufficient, it cannot be repaired by a retrospective statement. |
| Show research use and a credible software contribution | Yes | Yes | Bounded developer use in [JOSS-01](docs/research/joss01-results.md) and [Issue #35 synthesis](docs/research/issue35-results.md) is now presented with audience, distinct design need, and limits in the revised paper. Editorial significance remains a JOSS judgment; a full new experiment is not prescribed. |
| Finalize the software-focused paper and its references | Yes | Yes | Prepared in the working tree. [Final paper validation](docs/research/joss-final-paper-validation.md) records a software-focused 900-token manuscript, five resolved citations including the source archive, valid tagged document targets, and a rendered PDF. Commit and publish this revision before submitting; do not move the release tag. |
| Complete submission metadata | Yes, at submission | Yes | Author, affiliation, funding, conflict, acknowledgement, and AI disclosures are [approved and present](docs/research/joss06-audit.md). Set the actual submission date in JOSS format when submitting; do not invent an earlier date. |
| Public source, license, issue/contribution/support routes | Yes | Yes | Satisfied for the current candidate: [MIT license](LICENSE.txt), public repository and Issues route, [CONTRIBUTING](CONTRIBUTING.md), [README](README.md), and [SECURITY](SECURITY.md). The author-approved private security contact and response targets are now documented; this route is a project commitment, not a separate JOSS rule. |
| Reviewable installation, example, and tests | Yes | Yes | Satisfied for the current executable tree. The non-author [clean-checkout review](docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) passed 159 tests and the separate-process exchange; the [tagged snapshot](docs/research/joss07-snapshot.md#requirement-matrix) passed the exact-tag Windows suite and records Ubuntu POSIX coverage. Recheck only if submitted software changes. |
| Public fixed source and release | No separate prerelease is mandated, but a stable review revision is needed | Yes | Satisfied for candidate v0.1.0-rc.1. The [release record](docs/research/release-candidate.md) verifies the public tag, source archive, checksum, and exact-tag tests. A later paper edit requires a new committed submission revision, not a tag move. |

## JOSS-01 — Establish and document research significance (P0)

The [focused shared-trust experiment](docs/research/joss01-results.md) is actual, narrow developer research use; [Issue #32](docs/research/issue32-results.md) supplies a fixed-revision bounded evaluation. The historical predecessor letter is attributed and does not establish current-code adoption. The open publication task is the JOSS research-impact and scholarly-need case above. External adoption and completion of every planned Phase 2 experiment are not automatic prerequisites.

## JOSS-02 — Freeze and verify the research release boundary (P0)

Complete for the tested candidate. The [validation](docs/research/joss02-validation.md) and [final claim audit](docs/research/joss02-final-claim-audit.md) record tested TLS, file exchange, negative cases, compatibility limits, and qualified paper claims. Product Issues #7, #8, and #36 are scoped to the documented release, with further product work in the [product backlog](app-prod-grade-backlog.md).

## JOSS-03 — Installation, examples, documentation, and CI (P0/P1)

Complete for the current executable tree. The [independent review](docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) confirms a clean Java 21/Maven build and the documented CODE-02 wrapper with generated TLS and master-key fixtures, database initialization, and a separate packaged server process. The [CI record](docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19) reports matching successful Windows and Ubuntu jobs. An operator-provisioned long-running deployment is outside the stated reviewer workflow. Public-history screening is tracked above; the private security route is documented in [SECURITY.md](SECURITY.md).

## JOSS-06 — Authors, funding, acknowledgements, and AI use (P1)

Author-approved identity, affiliation, contribution, funding/conflict statements, and AI tool-use disclosure are [recorded](docs/research/joss06-audit.md) and present in [paper.md](joss-papers/paper.md). Only the actual date and final rendered-paper check remain at submission.

# Required for JOSS Acceptance

| Task | Blocking Submission? | Blocking Acceptance? | Status and smallest remaining action |
| --- | --- | --- | --- |
| Make the submitted paper render correctly and answer review checks | Yes for a valid, reviewable paper | Yes | The revised paper rendered in the repository-supported JOSS-compatible Windows fallback, with title, author, affiliation, five references, release link, and layout checked in [final validation](docs/research/joss-final-paper-validation.md). The local preview's 1970/DOI placeholders are JOSS article-info defaults; actual submission metadata must be supplied through the submission/review workflow. |
| Keep the submitted code and paper revision identifiable and reproducible | Yes | Yes | The existing tag and software tests are verified. The revised paper/PDF need a new committed and published submission revision; the release tag stays fixed. The final validation records current hashes and confirms the same executable source. |
| Respond to reviewer and editor issues | No | Yes | Future review work. Address substantiated findings in the public review and issue tracker; do not pre-create issues merely to populate history. |
| Make the final reviewed release and archive deposit | No | Yes, after successful review | Follow JOSS's post-review step: tag the reviewed version, deposit it in an archive, and provide its DOI and version in the review thread. The archive DOI is not a pre-submission blocker. |

## JOSS-04 — Restore and verify bibliography (P1)

Four cited entries were checked against primary sources. The [final paper validation](docs/research/joss-final-paper-validation.md#bibliography-and-link-check) checks the added release-archive entry and all five rendered references.

## JOSS-05 — Align and finalize manuscript (P1)

The [final claim audit](docs/research/joss02-final-claim-audit.md) found no unqualified contradiction in the tagged paper. The [revised paper and validation](docs/research/joss-final-paper-validation.md#final-manuscript-audit) now foreground the software and cite the source archive; changed prose adds no new empirical claim. Missing original outage and local-control experiments remain stated limits, not new JOSS tasks.

## JOSS-07 — Independent review and submission snapshot (P1)

The [public prerelease snapshot](docs/research/joss07-snapshot.md) predates the later [non-author review](docs/research/joss03-results.md#independent-non-author-review-on-2026-11-19). Both support reviewability, but they do not prove six months of public development. The [revised paper validation](docs/research/joss-final-paper-validation.md) closes the current manuscript checks; reproduce the final reviewed release and archive it at the JOSS post-review stage.

# Optional Improvements

These are useful only if they strengthen an actual paper claim or address reviewer feedback. They are not conditions for submitting the bounded current software paper.

## Phase 2 — Research tests and proof of concept

The [research evidence index](docs/research/README.md) and [Issue #35 synthesis](docs/research/issue35-results.md) retain the original workload, controls, outage, and fault plans and their execution limits.

## Issue #34 — Evaluate outages and faults (P0)

The unexecuted service-outage windows, key-deletion matrix, and broader fault campaign are optional future research. The current paper expressly makes no availability-window or general security-strength claim.

## Issue #35 — Publish reproducible study results (P0)

Additional fixed-revision reruns and completion of the original correctness/resource/availability question are optional future research. Keep the already published bounded results and dirty-tree provenance accurate in the current paper.

- Independent rerun of the exact v0.1.0-rc.1 tag, operator-managed long-running TLS/master-key setup, additional OS/ACL cases, static paper-checker repair, and JReleaser branch-detection repair are optional verification or maintenance work.
- The author-approved private vulnerability-reporting route and response targets are documented in [SECURITY.md](SECURITY.md). This closes the project's separate pre-submission confirmation commitment; JOSS does not prescribe a private channel or production support commitment.

# Post-Publication Product Work

## Phase 4 — Product hardening beyond the current study

The [product backlog](app-prod-grade-backlog.md) retains detailed Issues #7–#30 and #36: key lifecycle, backup and recovery, structured and verifiable audit, runtime controls, containerization, and remaining platform-specific security checks. None is a blanket JOSS publication prerequisite for the current bounded claims. If a future paper or review makes a stronger claim, evaluate the relevant task against that claim before moving it onto the publication path.
