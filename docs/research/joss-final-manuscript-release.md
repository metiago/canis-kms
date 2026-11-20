# JOSS final manuscript revision

## Frozen source

| Field | Value |
| --- | --- |
| Manuscript commit | `a01872258cb6d027512e15f71ed9821b136615c8` |
| Manuscript version | JOSS pre-submission revision 1, identified by the commit and SHA-256 values below; no independent paper version is declared in JOSS YAML |
| Released software version | `v0.1.0-rc.1`, commit `e1d6998b5c5658750870b39e2a4d511de5bed7f3` |
| Manuscript files | `joss-papers/paper.md`, `joss-papers/paper.bib`, `joss-papers/paper.pdf` |
| Software behavior | Unchanged from the release: `git diff --name-only v0.1.0-rc.1 a018722 -- client server pom.xml` is empty |

The source archive cited by `@canis-rc1` is the published `canis-kms-0.1.0-rc.1-src.zip` attachment documented in the [release candidate record](release-candidate.md#generated-artifacts-and-sha-256). The paper revision is a later commit; the release tag was not moved.

| Artifact | Status |
| --- | --- |
| `joss-papers/paper.md` | Frozen in manuscript commit; SHA-256 `C3A132CC756E8E4A335C52391C31BBAC0B5829A0328BC78B491868B18FF6208A`. No source placeholder tokens found. |
| `joss-papers/paper.bib` | Existing tracked bibliography; SHA-256 `9D9C5105848FEED28BA1F15D60D2D885BE066D259E5AACA2ADECECA2D4472B67`. Five entries match five cited keys; no missing or uncited entry. |
| `@canis-rc1` release reference | Resolves in the PDF to version `0.1.0-rc.1` and the published source archive URL. The release record verifies the attachment and SHA-256. |
| Author metadata and AI disclosure | Tiago Souza, Independent Researcher, Brazil, and the disclosure match the [author-approved JOSS-06 record](joss06-audit.md); all appear in the PDF. |
| Manuscript claims | [Final paper validation](joss-final-paper-validation.md) and [JOSS-02 claim audit](joss02-final-claim-audit.md) found no unqualified unsupported material claim. Bounded research results and implementation limits remain explicit. |
| Manuscript links | All six direct tagged repository document targets exist in `v0.1.0-rc.1`; cited standards and product pages were checked in the [final validation](joss-final-paper-validation.md#bibliography-and-link-check). |
| `joss-papers/paper.pdf` | Corresponds to the frozen `paper.md` and `paper.bib` hashes recorded in the [render log (archived)](repository-cleanup-report.md#archived-files). Inara v1.3.1 Windows fallback exited 0; 3 A4 pages, 215,678 bytes, SHA-256 `C0DAED6360C2B1DDE95B7A47E4B13083CCF8A347CBF3F3490FC1E6F6B42DAB6C`. Extracted text includes the final author wording, title, affiliation, AI disclosure, all five references, and source ZIP. |
| PDF journal metadata and generated links | **Pre-submission preview only.** Inara's default article-info injects 1970 submission/publication dates, `DOI: N/A`, volume/page placeholders, and sidebar links including `https://doi.org/N/A` and `https://doi.org/10.5281`. These are not in the manuscript sources. They require real JOSS submission/review metadata; the preview cannot be asserted placeholder-free or used as a published article PDF. |

The release candidate is a prerelease software artifact, not a final JOSS article archive. The actual submission date must be supplied when submitting; a reviewed archive DOI belongs after review. No software, CI, or experiments were changed to freeze this manuscript. The source revision is fixed locally; this record does not assert that the commit has been pushed or that JOSS's public-development screening gate is satisfied.
