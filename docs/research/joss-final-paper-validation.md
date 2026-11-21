# JOSS final paper validation — 2026-11-20

**Subsequent freeze:** The audited paper and PDF were committed unchanged in
`a01872258cb6d027512e15f71ed9821b136615c8`; see the
[manuscript release record](joss-final-manuscript-release.md). Working-tree
language below describes this audit before that commit. A later Summary copyedit
and regenerated PDF are recorded in the [manuscript release record](joss-final-manuscript-release.md).

## Scope and revision

This audit covers the current working-tree `joss-papers/paper.md` and `paper.bib` after a software-focused copyedit, against HEAD `9cdfc0b179496590adc6a9590fe680f730ac9bce` and the fixed software release `v0.1.0-rc.1` (`e1d6998b5c5658750870b39e2a4d511de5bed7f3`). The paper revision is not in the release tag or a new committed submission revision. No software, CI, research method, experiment, or measurement changed. The applicable JOSS [submission](https://joss.readthedocs.io/en/latest/submitting.html), [paper](https://joss.readthedocs.io/en/latest/paper.html), [review criteria](https://joss.readthedocs.io/en/latest/review_criteria.html), and [checklist](https://joss.readthedocs.io/en/latest/review_checklist.html) were checked on this date.

## Final manuscript audit

| Section | Status | Action |
| --- | --- | --- |
| YAML, title, author, affiliation | Verified against [JOSS-06 author approval](joss06-audit.md); title matches the release artifact | Retain. Add the real JOSS submission date only when submitting. |
| Summary | Software function and file/key path are stated without an adoption or strength claim; source archive is cited | Retain. |
| Statement of need | Audience and asynchronous file-exchange need are clear; predecessor account remains attributed and disclaims deployment of this revision | Retain. |
| State of the field | Tink and Vault are cited from their own documentation; revised text identifies CANIS's distinct, inspectable shared-admission study purpose without claiming comparative superiority | Retain revised text. |
| Software design | Envelope, TLS, key-only recovery, encrypted database, shared access, and important limits match [JOSS-02 claim audit](joss02-final-claim-audit.md) and [release boundary](joss02-release-boundary-final.md) | Retain. No software behavior changed. |
| Research impact statement | Bounded developer use from [JOSS-01](joss01-results.md), [CODE-02](code02-results.md), and [Issue #35](issue35-results.md); source-state limits remain explicit | Use singular “author”; retain the distinction between observed results, dirty-tree records, and unrun work. |
| AI disclosure and acknowledgements | Match [author-approved JOSS-06 record](joss06-audit.md) | Retain. |
| References | Five cited entries, including the released source archive, appear in the PDF | Retain. Final archive DOI is a post-review step. |

Pandoc `--from markdown --to plain` extraction yielded approximately **900 word tokens**, within JOSS's 750–1750 word guidance. The copyedit adds no new empirical claim. It does not present original local-envelope controls or service-outage series as completed, or claim external adoption, production readiness, general scalability, cryptographic strength, or a Tink/Vault performance advantage. The historical predecessor account is an author-attributed motivation, not evidence of use of this release. The research impact case is real but bounded; its editorial sufficiency remains for JOSS to decide.

## Bibliography and link check

All five `@...` citation keys in `paper.md` match exactly five entries in `paper.bib`; there are no missing or uncited entries. Current primary pages and DOI targets were opened where the browser supports them. The attached ZIP is a binary download that the browser did not fetch; its published existence, download, SHA-256, and tag are established by the [release record](release-candidate.md#generated-artifacts-and-sha-256) and [JOSS-07 snapshot](joss07-snapshot.md#requirement-matrix).

| Citation | Status |
| --- | --- |
| `dworkin2007` | Resolved in PDF; [NIST DOI](https://doi.org/10.6028/NIST.SP.800-38D) opens and identifies Dworkin, SP 800-38D, November 2007. |
| `moriarty2016` | Resolved in PDF; [RFC DOI](https://doi.org/10.17487/RFC8017) opens and identifies RFC 8017, its authors, and November 2016. |
| `tink-envelope` | Resolved in PDF; [Google Tink page](https://developers.google.com/tink/client-side-encryption) opens, describes client-side envelope encryption with a cloud KMS, and displays 2026-11-06 update. |
| `vault-transit` | Resolved in PDF; [Vault Transit page](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption) opens and describes envelope operations and ACL permissions. |
| `canis-rc1` | Resolved in PDF with version `0.1.0-rc.1` and a clickable [published source ZIP](https://github.com/metiago/canis-kms/releases/download/v0.1.0-rc.1/canis-kms-0.1.0-rc.1-src.zip). The [release page](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1) identifies `e1d6998`; the repository release record verifies the attachment's full commit and digest. |

All six direct `blob/v0.1.0-rc.1/...` paper-link targets were checked with `git cat-file -e` and exist in the tag. The bibliography's release URL is an archive attachment, not a DOI. The DOI deposit belongs after successful JOSS review under the [current backlog (archived)](repository-cleanup-report.md#archived-files).

## Rendering and inspection

The repository-supported [Windows fallback](build-joss05-windows.ps1), pinned to Inara v1.3.1 at `88ee5d656f147d801448ce77ad8375619d383df6`, used Pandoc 3.14, Inara JOSS defaults, LuaLaTeX, and its documented Latin Modern Mono substitution. The first sandboxed attempt failed with MiKTeX log access denied (Pandoc exit 43); the same renderer succeeded after its normal user-cache access was allowed. The final successful output is [paper.pdf](../../joss-papers/paper.pdf); the [render log (archived)](repository-cleanup-report.md#archived-files) records its exit output. The renderer's historical `joss05-draft.pdf` was restored after copying the new output to `joss-papers/paper.pdf`.

| Check | Result |
| --- | --- |
| PDF existence, pages, bytes | Passed: 3 A4 pages, 215,678 bytes; SHA-256 `C0DAED6360C2B1DDE95B7A47E4B13083CCF8A347CBF3F3490FC1E6F6B42DAB6C`. |
| Title and author | Passed in PDF text and visual page 1: exact title, Tiago Souza, Independent Researcher, Brazil. |
| Citations and references | Passed: Dworkin, Moriarty, Google, HashiCorp, and Souza resolve; all five bibliography entries are visible on page 3. |
| Release reference and manuscript links | Passed: version and source ZIP visible in the bibliography; PDF URL annotations include that ZIP, cited DOIs, product documentation, and tagged repository links. The renderer-generated sidebar “Archive” link is a placeholder, not the source ZIP. |
| Visual layout | Passed for pages 1 and 3; text, headings, and references are legible and not clipped. |
| Journal metadata | Preview only: the local Inara default article-info file displays `Submitted: 01 January 1970`, `Published: 01 January 1970`, `DOI: N/A`, volume/page placeholders, and a sidebar “Archive” annotation to `https://doi.org/10.5281`. These are builder defaults, not manuscript claims or real submission metadata. Replace through JOSS's actual submission/review workflow; do not cite this preview as a published article or use its sidebar links. |
| Toolchain equivalence | This is the repository's documented adapted JOSS-compatible render, with a font and TeX engine substitution. A canonical POSIX Inara build was not completed; JOSS does not require that particular local command. |

## Remaining backlog items and decision

| Item | Status | Blocking Submission? |
| --- | --- | --- |
| More than six months of active public development | **Unverified.** [JOSS-07](joss07-snapshot.md#requirement-matrix) establishes a 2024 repository creation date and current visibility, but not when it became public. Local commits are distributed across Dec 2024, Apr–May 2026, and Nov 2026; the snapshot found no public issues or pull requests. Retrospective text cannot establish a missing public interval. | **Yes: JOSS pre-review hard gate.** |
| Demonstrated research use and significance | Bounded developer research use is documented in JOSS-01, Issue #32, and Issue #35 and stated in the paper. No external adoption or comparative superiority is claimed; editors decide significance. | No additional experiment is specified as a prerequisite; editorial judgment remains. |
| Software-focused manuscript, citations, archive link, final render | Prepared and validated above. The PDF is a preview with default journal metadata; the actual submission date and journal fields belong to the submission process. | No manuscript-content blocker; the revised paper must be committed/published in an identifiable submission revision. |
| Author, funding, conflict, AI metadata | Author-approved and present per JOSS-06. | No; actual submission date is set when submitting. |
| Public source, MIT license, contribution/support routes | Satisfied for candidate per JOSS-07. | No. |
| Reviewable build, tests, example | Exact-tag 159-test Windows verification and Ubuntu POSIX gate in [release record](release-candidate.md#validation); later [non-author review](joss03-results.md#independent-non-author-review-on-2026-11-19) passed 159 tests and the separate-process example on unchanged executable source. | No new software test is required by this paper-only edit. |
| Stable private vulnerability-reporting route | The author-approved contact, private reporting process, and response targets are now documented in [SECURITY.md](../../SECURITY.md). This closes the project commitment; it is not a distinct JOSS rule. | No. |
| Fixed revised submission revision | The tag remains fixed and the manuscript/PDF are committed in `a01872258cb6d027512e15f71ed9821b136615c8`. | Publish the committed revision before submission without moving the tag. |
| Reviewer response and final DOI archive | Future review and post-review stages. | No. |
| Optional research controls, outage study, product hardening, canonical local build | Unperformed or unverified as documented; no current manuscript claim depends on their completion. | No. |

**Can CANIS be submitted today? No.** The public-history hard gate is not established, and the frozen paper commit has not been published to the public repository. The private-reporting commitment is now closed in [SECURITY.md](../../SECURITY.md). No new research, experiment, measurement, software change, or CI change is necessary for the manuscript itself. The paper is ready for review as a draft, with honest limits; submission readiness is not established by this render.

Architecture, security behavior, operations, and compatibility are unchanged. The manuscript makes the shared-access and audit-integrity limits explicit. Residual risks are JOSS screening of public development and bounded impact, the draft renderer's placeholder journal metadata, and the need to publish an identifiable revised paper. Later improvements may include genuine public issue/PR engagement, a reviewed final archive DOI, and optional wider studies if editors ask for them.
