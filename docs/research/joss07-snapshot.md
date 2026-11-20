# JOSS-07 submission snapshot — 2026-11-19

**Later security-route update (2026-11-20):** The author-approved private contact,
disclosure process, and response targets are now in [SECURITY.md](../../SECURITY.md).
Security-route findings below describe the 2026-11-19 snapshot, not current
submission readiness. The later [paper validation](joss-final-paper-validation.md#remaining-backlog-items-and-decision)
records the remaining gate; the former readiness checklist is [archived](repository-cleanup-report.md#archived-files).

## Decision and scope

**Not ready for JOSS submission. JOSS-07 remains open.** This is a review of the
published `v0.1.0-rc.1` candidate, not a new release or a JOSS submission. The
tag fixes source and manuscript commit `e1d6998b5c5658750870b39e2a4d511de5bed7f3`.
The audit started from clean HEAD `e302964d6ed9208be43849191da9c8f2f302c76b`;
`git diff --name-only v0.1.0-rc.1 HEAD` listed only
`docs/research/release-candidate.md`. The verification below used a local
`git archive` extraction of the tag, so its source and paper bytes are from
the candidate itself. Generated outputs are under ignored `target/joss07/`.

The current [JOSS submission rules](https://joss.readthedocs.io/en/latest/submitting.html)
require public development for more than six months, demonstrated research use,
open development practices, a browsable repository and issue tracker, and
reviewable software and paper. The [paper format](https://joss.readthedocs.io/en/latest/paper.html)
calls for 750–1750 words and the required sections; the
[review criteria](https://joss.readthedocs.io/en/latest/review_criteria.html)
also call for a link to the software archive in the paper's references. These
criteria govern the assessment below; this audit does not predict an editorial
decision.

## Requirement matrix

| Requirement | Evidence | Status |
|-------------|----------|--------|
| Fixed candidate and tag | Local annotated `v0.1.0-rc.1` resolves to `e1d6998b5c5658750870b39e2a4d511de5bed7f3`; root, client, and server POMs use `0.1.0-rc.1`. [Release record](release-candidate.md). | Verified |
| Public GitHub Release | [Prerelease](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1) API returned `draft=false`, `prerelease=true`, target commit above, published 2026-11-19 01:05:38 UTC. | Verified |
| Release artifacts and checksums | Public release API lists `canis-kms-0.1.0-rc.1-src.zip` (1,863,202 bytes, SHA-256 `81f132bf6b69b482649e6d4e300c7b40c5394954ac858c0ff2b2bf4a49621c8a`) and `checksums_sha256.txt` (95 bytes, SHA-256 `1000b1ebd86791c94e9d437689be40cd8827a2666d95c70e7ee7d5d28c40d86c`). The [release record](release-candidate.md#generated-artifacts-and-sha-256) records prior download and hash verification. GitHub-generated archives are separate from this attached ZIP. | Verified existence and published digests; no new download in this audit |
| JReleaser outputs and release path | [Release record](release-candidate.md#validation) reports successful JReleaser config and dry run, archive/checksum generation, tagged Ubuntu build, and POSIX security gate. The final tagged JReleaser publication step failed on branch detection; the prerelease was then published through the GitHub Releases API. | Published candidate verified; workflow defect open |
| Paper layout and metadata | Tagged [`paper.md`](../../joss-papers/paper.md) and [`paper.bib`](../../joss-papers/paper.bib) are colocated; YAML names Tiago Souza, `Independent Researcher, Brazil`, and `bibliography: paper.bib`. All eight expected headings are present. The submission date is absent, consistent with the backlog's instruction to set the actual date when submitting. [JOSS-06 audit](joss06-audit.md). | Candidate metadata verified; submission date deferred |
| AI, funding, and conflict disclosures | Tagged paper matches the author-approved [JOSS-06 record](joss06-audit.md): per-tool AI uses, known model families and historical version limits, human review/responsibility, no funding or institutional support, no acknowledgements, and no declared conflicts. | Verified against author record |
| Bibliography and citations | Four citation keys match four BibTeX entries, with no missing or uncited keys. Pandoc citeproc rendered Dworkin/NIST, Moriarty/RFC, Google Tink, and HashiCorp Vault references. [JOSS-04 source review (archived)](repository-cleanup-report.md#archived-files). No software release/archive reference appears in the tagged paper or bibliography. | Four citations resolve; archive citation open |
| Paper length and content | Pandoc plain-text extraction of tagged Markdown counted 1,394 Unicode word tokens. The repository's static script reported 1,892 because it counts Markdown link URLs; its CRLF-sensitive heading comparison falsely reported 0/8. The manuscript devotes substantial space to new experiment results, although JOSS says the paper should focus on the software rather than new results. | Length appears within guidance; editorial framing needs review; static script needs repair |
| Canonical JOSS manuscript build | Pinned Inara v1.3.1, commit `88ee5d656f147d801448ce77ad8375619d383df6`: `make pdf JOURNAL=joss ARTICLE=...` failed on Windows because its recipe starts with POSIX `INARA_ARTIFACTS_PATH=...`. No canonical POSIX build was completed. | Open |
| Adapted manuscript build | The documented Windows Inara/Pandoc/MiKTeX fallback built the exact tagged paper with a local font substitution. PDF: `target/joss07/tagged-paper.pdf`, 234,369 bytes, SHA-256 `a6c1a21eda72d555b61b94a47d9f667e7c7ca2a12b22533a98ddea8118c80d77`. Citeproc loaded `paper.bib` and produced four references. MiKTeX needed permission outside the sandbox for its local cache/log. | Passed as a draft rendering check; not canonical styling |
| Exact-source project verification | `git archive` extraction of the tag; Windows, Maven 3.9.12 with Eclipse Adoptium Java 21.0.6; `mvn --batch-mode --no-transfer-progress clean verify` passed Spotless, compilation, tests, and packaging. Surefire XML: 28 suites, 159 tests, zero failures/errors, eight skips. `SharedTrustDomainResearchTest` 1/1, `ReviewerWorkflowTest` 1/1, `ServerEndToEndTest` 4/4. Five skipped tests need a separate integration server; three are POSIX-specific. The tagged [Ubuntu workflow](https://github.com/metiago/canis-kms/actions/runs/44433044373) supplies the earlier POSIX gate evidence. | Passed on Windows tag extraction; skips documented |
| Repository age and public development | Public [repository](https://github.com/metiago/canis-kms) API: created 2024-12-10 19:11:06 UTC, currently public, default branch `master`. Local commit dates span Dec 2024, Apr–May 2026, and Nov 2026. Creation date and current visibility do not prove when it became public; the long gap and concentrated recent research activity warrant editorial review. | Calendar age verified; six months of public, active development not independently established |
| Issue tracker and open workflows | GitHub API reports `has_issues=true`; the public repository shows an Issues tab, but `issues?state=all` returned zero. `/issues/31` through `/issues/35` returned 404: these are [backlog study labels (archived)](repository-cleanup-report.md#archived-files), not public GitHub issues. [CONTRIBUTING](../../CONTRIBUTING.md) describes issue/PR routes, but there is no public issue discussion in this repository. | Tracker available; public issue history absent |
| License, archive, documentation | Tracked [`LICENSE.txt`](../../LICENSE.txt) contains MIT terms and GitHub identifies SPDX `MIT`. The tagged source ZIP is downloadable and cited in the [release record](release-candidate.md). [`README`](../../README.md), [research index](README.md), [reviewer guide](../reviewer-guide.md), and [contribution guide](../../CONTRIBUTING.md) cover build, use, tests, and research limits. | Verified for candidate |
| Support and vulnerability reporting | At this snapshot, [CONTRIBUTING](../../CONTRIBUTING.md) sent ordinary support to public issues and the then-current [`SECURITY.md`](../../SECURITY.md) did not name a stable private contact. A later author-approved update added `metiago.work@gmail.com` and response targets. | Historical gap closed by the later policy update |
| Independent reproduction and research use | [JOSS-01](joss01-results.md) documents a narrow developer shared-trust experiment; [Issues #31–35](issue35-results.md) document bounded synthetic studies. Only Issue #32 has a clean fixed-revision measured campaign; Issue #31, #33, and #34 final runs used dirty trees, and the planned service-outage/local-control work remains unexecuted. [JOSS-03](joss03-results.md) has no non-author install/example/evaluation record. | Independent JOSS-07 acceptance open; research impact case remains limited |

## Final blockers and next evidence

1. Obtain a non-author's clean-checkout installation, documented example run,
   and evaluation reproduction, with sanitized commands and outcomes. This is
   an explicit [JOSS-07 acceptance item (archived)](repository-cleanup-report.md#archived-files).
2. Run the canonical Inara/JOSS build on a supported POSIX toolchain against
   the candidate revision; inspect the rendered title, author, affiliation,
   date, and four references. Repair the static paper check so it handles CRLF
   and counts rendered text rather than link URLs.
3. Add a citation or direct reference to the released source archive, set the
   actual submission date when submitting, and review the results-heavy paper
   against JOSS's software-paper guidance. A paper edit after this tag needs a
   new fixed candidate, not movement of `v0.1.0-rc.1`.
4. At this snapshot, establish a stable private vulnerability-reporting route
   and demonstrate open issue/feedback activity. The security contact was
   subsequently documented in `SECURITY.md`; the public-history question
   remains. Verify when the repository became public; `created_at` alone
   cannot certify JOSS's public-history gate.
5. Resolve the research-use/significance and Phase 2 limits honestly. The
   developer experiments are real but narrow; the original availability and
   local-control questions remain unmeasured, and several results lack
   fixed-release or independent execution. Seek an author/editor assessment
   of whether the bounded evidence meets the current JOSS screening bar.

The release's JReleaser branch-detection failure is an operational automation
risk, not an absence of a published candidate. A permanent archive DOI is
planned for the successful-review stage under JOSS's submission process; its
absence is not treated as a pre-submission blocker. No architecture, protocol,
storage, or TLS implementation changed in this audit. The security implications
are confined to accurate disclosure of the shared-key-access blast radius,
audit-integrity gap, and private reporting path. The snapshot adds no
backward-compatibility impact or deployment requirement.

**Readiness assessment:** fixed public prerelease and exact-source Windows
verification are established; complete manuscript and independent screening
evidence are not. **JOSS-07 status: open / partially evidenced.**
