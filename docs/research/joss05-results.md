# JOSS-05 manuscript alignment and verification results

## Snapshot and executed commands

This is a **dirty working-tree** verification on 2026-11-16, not a tagged or
public candidate release. The repository HEAD was
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81`. The pre-edit
[claim-evidence matrix](joss05-claim-evidence.md) was created before this
issue's `paper.md` edit. Earlier JOSS-01/JOSS-02 paper and research files were
already modified or untracked when this work began. No Java production or
test source was changed for JOSS-05.

From the repository root, these commands were executed on Windows 11 amd64:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss02.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-joss02-summary.ps1
Copy-Item -LiteralPath docs/research/joss02-suite-report.csv -Destination docs/research/joss05-suite-report.csv
Copy-Item -LiteralPath docs/research/joss02-test-cases.csv -Destination docs/research/joss05-test-cases.csv
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/check-joss05-paper.ps1
git diff --check
```

The full-suite, focused experiment, export, and static paper-check commands
exited 0. Maven 3.9.12 used Eclipse Adoptium Java 21.0.6; its OS report was
Windows 11 amd64. The runner's OS API printed Windows 10.0.26100 for that
same host. The full test run completed before the manuscript-only edits; no
Java source changed afterward. The focused experiment was rerun separately.
The exact fixture, prerequisites, expected output, cleanup, and platform
limits are in [JOSS-05 method](joss05-method.md) and
[JOSS-01 method](joss01-method.md).
The JOSS-05 addition to the JOSS-01 method clarifies expected output and
cleanup; it does not change the test procedure or its assertions.

## Observations and boundaries

**Observation:** The fresh full suite reported 24 suites and 149 test cases:
59 client tests (54 passed, five skipped) and 90 server tests (88 passed, two
skipped), with zero failures or errors. `SocketClientTest` passed 14/14,
`ServerEndToEndTest` 4/4, `ServerTlsSecurityTest` 7/7,
`ServerTlsCertificateTest` 3/3, and `KeyValueStoreTest` 12/12.
**Evidence:** The full runner's exit code 0 and sanitized
[suite](joss05-suite-report.csv) and [test-case](joss05-test-cases.csv) CSVs
exported from the local Surefire XML. **Interpretation:** These executed cases
support the paper's bounded working-tree description of the official TLS file
workflow and tested rejection/persistence paths. **Limitation:** This run does
not verify a clean checkout or fixed release; five disabled client integration
tests and two Windows-inapplicable POSIX security checks remain skipped.

**Observation:** The focused `SharedTrustDomainResearchTest` command exited 0;
its Surefire XML reported one test, zero failures, zero errors, and zero skips.
The test asserts that both valid credential identities recover the exact
synthetic bytes and an altered `observer` password fails authentication.
**Evidence:** [test source](../../server/src/test/java/io/canis/SharedTrustDomainResearchTest.java),
[method](joss01-method.md), [case report](joss05-test-cases.csv), and the
focused local Surefire XML. The source SHA-256 was
`71FB34494FEC157CA15EA591278AA7421AD455619DDA1E36560A238F1BBB622C`.
**Interpretation:** The result is consistent with CANIS's documented
credential-level shared trust boundary. **Limitation:** One client TLS
certificate was used across the three credential identities. This is one
synthetic scenario, not a three-instance deployment, research adoption,
performance result, or demonstration of safe cross-service isolation.

**Observation:** The edited manuscript's static check reported 1,263 body
words by a simple Unicode token count, all eight expected headings, four
citation keys, four bibliography entries, zero missing keys, and zero uncited
entries. Author/affiliation placeholders remain. **Evidence:**
[`check-joss05-paper.ps1`](check-joss05-paper.ps1),
[`paper.md`](../../joss-papers/paper.md), and
[`paper.bib`](../../joss-papers/paper.bib). The manuscript SHA-256 after this
issue's edit was
`997CB0BDA0E9BF3E17EA989F149E7065E2ED97E3E5B7C68735644CDF5D2BA9AE`.
**Interpretation:** The draft meets the static section and recommended-length
checks in the current [JOSS paper format](https://joss.readthedocs.io/en/latest/paper.html).
**Limitation:** Approximate word counting and key matching do not establish
rendered formatting, resolved PDF references, author approval, or a software
archive citation.

**Observation:** The paper now explains the reason for keeping bulk file
processing in the client, the availability and trust trade-offs of returning
data keys, and the bounded purpose of inspecting this implementation. It
continues to distinguish current audit messages and key lifecycle from
planned integrity verification and rotation. The CANISP guide's former
"structured audit" wording and the README's former "periodically persisted"
claim were corrected. **Evidence:** [paper](../../joss-papers/paper.md),
[protocol guide](../canisp-protocol.md), [README](../../README.md), and the
[pre-edit matrix](joss05-claim-evidence.md). **Interpretation:** These edits
bring the working-tree manuscript and adjacent documentation closer to the
inspected implementation. **Limitation:** They cannot close production Issues
#8/#36, prove an authorized release, or establish a distinct research need.

**Observation:** No actual research-use record, published CANIS study,
external adoption, performance measurement, or three-instance/fault-injection
study was supplied in this repository. **Evidence:**
[JOSS-01 results](joss01-results.md), [publication backlog](../../joss-backlog.md),
and the current [manuscript](../../joss-papers/paper.md).
**Interpretation:** The research impact statement remains explicitly limited
to a reproducible engineering case study. **Limitation:** Repository absence
does not rule out use elsewhere; authors must supply verifiable use and assess
the paper's significance before JOSS submission. No Issues #31–35 results
were invented or marked complete.

## Primary-source citation check

| Paper key | Primary source checked on 2026-11-16 | Supported claim | Limit |
| --- | --- | --- | --- |
| `dworkin2007` | [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final) | Published GCM/GMAC standard, Morris Dworkin, November 2007. | Not an evaluation of CANIS's implementation or strength. |
| `moriarty2016` | [RFC Editor RFC 8017](https://www.rfc-editor.org/info/rfc8017/) | PKCS #1 RSA specification, November 2016. | Not a CANIS security assessment. |
| `tink-envelope` | [Google Tink client-side encryption](https://developers.google.com/tink/client-side-encryption) | Its KMS envelope AEAD performs local data encryption with a KMS call to protect the data key. | No Tink policy or comparative outcome was tested. |
| `vault-transit` | [HashiCorp Transit envelope encryption](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption) and [Transit overview](https://developer.hashicorp.com/vault/docs/secrets/transit) | Transit supplies envelope data keys; ACLs can restrict named-key operations. | Product documentation may change; no Vault deployment or comparison was run. |

The `dworkin2007` entry's `institution` value was brace-protected after an
initial rendered draft split the name incorrectly. The rebuilt reference now
reads “National Institute of Standards and Technology” across a line wrap.
No new citation or unverified source was introduced. The updated bibliography
SHA-256 is
`8C0DB7FFE91E5E1219CAE5508BC6557B197236C712580AA1C266074BF897C910`.

## Local paper-build attempt

**Observation:** At the user's direction, no Docker build was performed. An
earlier image inspection found no cached Inara image and an image pull was
stopped when the user selected the local route; no container was run. The
official Inara repository was cloned with
`git clone --depth 1 --branch v1.3.1 https://github.com/openjournals/inara.git C:\Users\TSIG\AppData\Local\Temp\canis-joss05-inara`
into the local temporary directory at
tag `v1.3.1`, commit `88ee5d656f147d801448ce77ad8375619d383df6`.
Its [Makefile](https://github.com/openjournals/inara/blob/v1.3.1/Makefile)
uses `ARTICLE` for the manuscript path and `INARA_DATA_PATH`
for its own Pandoc data directory. A local command from the clone root was
attempted:

```powershell
make pdf JOURNAL=joss ARTICLE=C:/Users/TSIG/workspace/projects/ieee/Conference-Templates-Word/canis-kms/joss-papers/paper.md
```

**Evidence:** The command exited 1 with PowerShell's `make` not found error.
That failed attempt preceded installation of Pandoc. A later PATH refresh
found `pandoc 3.14` at
`C:\Users\TSIG\AppData\Local\Programs\pandoc\pandoc.exe`; GNU make remained
unavailable. This host has portable MiKTeX 26.5, not TeX Live, and its
`latexmk` could not run without Perl. **Interpretation:** The literal local
`make pdf` command was unavailable, so the user-authorized direct Pandoc and
LaTeX fallback was needed. **Limitation:** The temporary clone is a build
tool checkout, not a submission artifact.

**Observation:** The checked-in [Windows build script](build-joss05-windows.ps1)
exited 0 using the pinned Inara v1.3.1 commit, Pandoc 3.14, and MiKTeX
LuaLaTeX. It retained a 216,668-byte, three-page A4
[draft PDF](joss05-draft.pdf) with SHA-256
`CADD5425B7979F360A1F23FE5BAE6131C769BE9EA8C780B794259EB44A622893`.
The script uses temporary Windows defaults for an absolute footer CSL and
direct LuaLaTeX, and substitutes Latin Modern Mono for the unavailable Hack
font. **Evidence:** The exact command, prerequisites, overrides, and cleanup
are in the [method](joss05-method.md) and the script. The executed command was:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/build-joss05-windows.ps1
```

**Interpretation:** The current working-tree manuscript can render through
Inara's JOSS defaults, Lua filters, and an adapted local template. **Limitation:**
This does not verify Inara's unmodified GNU make/TeX Live/Hack-font path, a
fixed candidate release, or submission metadata. The build may install
MiKTeX packages locally, and PDF bytes may vary with build time or tool state.

**Observation:** `pdfinfo` reported three A4 pages and `Tagged: no`; a
`pdftotext -layout` extraction showed all four bibliography entries, complete
DOI/URLs, and the corrected NIST institution name. All three rendered pages
were visually inspected for gross clipping and reference legibility. The PDF
shows `REPLACE WITH AUTHOR NAME`, 1970 submission/publication dates, `N/A`
DOI, and an unresolved self-citation footer. **Evidence:** The retained
[PDF](joss05-draft.pdf), the bibliography, and the local `pdfinfo`,
`pdftotext`, and page-image inspection. **Interpretation:** Rendered reference
resolution and draft page layout were verified for this adapted build.
**Limitation:** Visual inspection does not certify final typesetting,
accessibility, author metadata, or the canonical JOSS build. The placeholder
metadata must be replaced after author confirmation and submission decisions.

## Artifact integrity and traceability

The sanitized suite CSV SHA-256 is
`954D97DF3CC5908EB614567EFCDF38D9CF657404F188C69611957664C47355D2`;
the case CSV SHA-256 is
`BB46F9C9FD32DD9E7688923F0B001A365736A7110560F2D5EBA616F229F23822`.
The CSVs contain counts, case names, and outcomes only. Raw test logs,
generated TLS material, passwords, master keys, and database fixtures were
not added to the repository. `git diff --check` exited 0; the working tree
remains dirty because both prior work and JOSS-05 artifacts are uncommitted.

| Backlog Task | Evidence | Status |
| --- | --- | --- |
| JOSS-05 TLS and authorization wording | `joss-papers/paper.md`; `joss05-claim-evidence.md`; `joss05-suite-report.csv`; `joss05-test-cases.csv`; `SocketClientTest`; `ServerEndToEndTest`; `SharedTrustDomainResearchTest` | Working-tree wording and tests verified; fixed-release check remains JOSS-02. |
| JOSS-05 statement of need, field, design, impact | `joss-papers/paper.md`; `docs/research/joss01-results.md` (stale bibliography sentence corrected); `joss05-claim-evidence.md`; this report's source check | Partial: design rationale and bounded result documented; actual research use and distinct need missing. |
| JOSS-05 observed versus planned features | `joss-papers/paper.md`; `docs/canisp-protocol.md`; `README.md`; `server/src/main/java/io/canis/audit/AuditLogger.java`; production backlog Issues #8/#36 | Working-tree wording reconciled; production findings remain open. |
| JOSS-05 word count, sections, citations, format | `check-joss05-paper.ps1`; `joss-papers/paper.md`; `joss-papers/paper.bib`; `build-joss05-windows.ps1`; `joss05-draft.pdf` | Static checks and adapted draft PDF/reference inspection passed; canonical build and final metadata pending. |
| JOSS-05 reproducible research evidence | `joss05-method.md`; `joss01-method.md` (expected output/cleanup clarified); `run-joss01.ps1`; `run-joss01.sh`; `run-joss02.ps1`; `run-joss02.sh`; `export-joss02-summary.ps1`; refreshed intermediate `joss02-suite-report.csv`/`joss02-test-cases.csv`; retained `joss05-suite-report.csv`/`joss05-test-cases.csv` | One synthetic categorical experiment and suite rerun; no performance, adoption, or multi-instance claim. |
| JOSS-05 publication status | `joss-backlog.md`; `joss-papers/submission-readiness.md`; this report | Issue acceptance open pending research use, fixed release, canonical build, and author approval. |

## Acceptance and remaining work

The technical descriptions and categorical outcome above have current-tree
source/test support. JOSS-05's acceptance criterion remains **open** because
the manuscript has not been checked against a fixed candidate release, actual
research use and a distinct scholarly need are not documented, and only an
adapted draft PDF has been inspected. A canonical Inara build, JOSS-06 author/AI
approval, and JOSS-07 independent and release verification also remain open.
The local clone should be retained only as needed to repeat the build.
