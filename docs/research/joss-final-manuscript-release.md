# JOSS final manuscript revision

## Release traceability correction — 2026-11-21

The live local and remote `v0.1.0-rc.1` Git tag resolves to
`5ea7e87d7f6c0aba0ed5ca32d809d615a88a9ec0`. The published source ZIP,
downloaded again with its recorded SHA-256
`81f132bf6b69b482649e6d4e300c7b40c5394954ac858c0ff2b2bf4a49621c8a`,
embeds `e1d6998b5c5658750870b39e2a4d511de5bed7f3` in its ZIP comment.
The GitHub release page also displays `e1d6998` as its release target. The
archive revision and current tag target are therefore distinct; these records
do not establish when the difference arose. The `paper.bib` note now identifies
both without changing the citation key, version, or archive URL.

The current `paper.bib` SHA-256 is
`B5E944ADABC2EE55866BC5CB5D167D304B99D8DA4316919D23406BE81B35665E`.
The PDF was not regenerated for this documentation correction; the bibliography
hash in the render table below identifies the exact input used for that PDF.
The historical frozen-source hashes below remain tied to their recorded revision.

## Current working-tree PDF render — 2026-11-21

The repository-supported Inara v1.3.1 Windows workflow rendered the current `paper.md` and
unchanged `paper.bib` with Pandoc 3.14 and LuaLaTeX. The direct renderer run exited 0 after
MiKTeX installed missing local packages. The local render logs are
`target/joss-initial-render.log` and `target/joss-final-render.log`; extracted text and page
previews are under `target/joss-final-paper.txt` and `target/joss-final-page-*.png`.

| Artifact at render time | SHA-256 |
| --- | --- |
| `joss-papers/paper.md` | `C3F33AAB07070B69B2CC9D95D2CFF2AB6C80DDBBABAE197F3EBB2B9D9E63041A` |
| `joss-papers/paper.bib` | `9D9C5105848FEED28BA1F15D60D2D885BE066D259E5AACA2ADECECA2D4472B67` |
| `joss-papers/paper.pdf` | `A7289246152E8D96859057A08A52362AE11FB869B9EF1FEEEF11F1256C95B2EA` |

The static paper check passed with 980 body words, all eight required headings, five matched
citations, and no author or affiliation placeholders. The PDF is 214,751 bytes and three A4
pages. Extracted text and visual inspection of all pages show the title, author, affiliation,
all required sections, AI disclosure, acknowledgements, and five resolved references. The
`@canis-rc1` bibliography entry renders as version `0.1.0-rc.1` with the source ZIP URL;
the ZIP returned HTTP 200 with the recorded 1,863,202-byte size. All tagged document paths
linked from the manuscript exist at the tag currently advertised by Git. The NIST, RFC Editor,
Google Tink, and HashiCorp reference URLs also resolved. No manuscript,
bibliography, or software source was changed for this render. Inara's 1970 dates and `DOI: N/A`
remain preview metadata, not manuscript content.

The later archive/tag distinction and bibliography metadata correction are recorded above.
This render did not alter the release citation, tag, or published asset.

## Later working-tree update — 2026-11-20

The first Summary sentence in `paper.md` was copyedited after the frozen manuscript commit below.
`paper.pdf` was regenerated from that working-tree source and the unchanged `paper.bib` with the
documented Inara v1.3.1 Windows fallback. The static paper check passes: 1,002 body words, all
eight required headings, five matched citations, and no author or affiliation placeholders. The
rendered PDF has three A4 pages; extracted text contains the new Summary wording, AI disclosure,
and all five references. All three pages were visually checked for clipping.

| Artifact in the 2026-11-20 working tree | SHA-256 |
| --- | --- |
| `joss-papers/paper.md` | `F3B7AD03261BD278CD971654B52C0AE07FF49BD2F85A0FDCA5659645B69B939F` |
| `joss-papers/paper.bib` | `9D9C5105848FEED28BA1F15D60D2D885BE066D259E5AACA2ADECECA2D4472B67` |
| `joss-papers/paper.pdf` | `60C5F49CB54582CEF80C944CB7792C89B78AA8B9A4684C18829B2AC26832172C` |

The hashes and PDF size in the frozen-source section below describe commit `a018722`, not this
working tree. The regenerated PDF still uses Inara's preview journal metadata; JOSS supplies real
submission and publication metadata during review. This paper revision needs to be committed and
published before submission. JOSS's six-month public-development requirement remains unverified.

## Frozen source

| Field | Value |
| --- | --- |
| Manuscript commit | `a01872258cb6d027512e15f71ed9821b136615c8` |
| Manuscript version | JOSS pre-submission revision 1, identified by the commit and SHA-256 values below; no independent paper version is declared in JOSS YAML |
| Released software version and current tag target | `v0.1.0-rc.1`, current tag commit `5ea7e87d7f6c0aba0ed5ca32d809d615a88a9ec0` |
| Published source ZIP revision | Embedded commit `e1d6998b5c5658750870b39e2a4d511de5bed7f3` |
| Manuscript files | `joss-papers/paper.md`, `joss-papers/paper.bib`, `joss-papers/paper.pdf` |
| Software behavior | Unchanged from the release: `git diff --name-only v0.1.0-rc.1 a018722 -- client server pom.xml` is empty |

The source archive cited by `@canis-rc1` is the published `canis-kms-0.1.0-rc.1-src.zip` attachment documented in the [release candidate record](release-candidate.md#generated-artifacts-and-sha-256). The paper revision is a later commit; this manuscript freeze did not move the release tag. Its current target differs from the archived source revision, as recorded above.

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
