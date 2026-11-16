# JOSS-04 bibliography verification results

## Snapshot and scope

This review ran on 2026-11-16 against Git HEAD
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81` plus a dirty working tree.
The [pre-edit claim-evidence matrix](joss04-claim-evidence.md) was created
before changing `joss-papers/paper.md`. The sole JOSS-04 manuscript edit is
`bibliography: paper.bib`; the manuscript body was not changed. The new
`joss-papers/paper.bib` is beside the Markdown paper. This is not a fixed
candidate release or a claim of JOSS submission readiness.
The SHA-256 values for the reviewed paper sources on this host were
`paper.md`: `2DB42DF47900BC198C94F8026B821FCBDDA638A4AFDEF037ABDB8E5340BB1824`
and `paper.bib`: `0406B12BE024238498BB3EE0C1D7AD11A75579A2DF1F9B5F598B66749AE7D198`.

## Primary-source verification

| Key | Observed primary-source metadata | Entry decision and limitation |
| --- | --- | --- |
| `dworkin2007` | [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final): Morris Dworkin; *Recommendation for Block Cipher Modes of Operation: Galois/Counter Mode (GCM) and GMAC*; November 2007; DOI `10.6028/NIST.SP.800-38D`. | `@report` records the named author, title, institution, publication number, date, DOI, and URL. NIST notes a planned revision; this entry identifies the 2007 final publication. |
| `moriarty2016` | [RFC Editor record](https://www.rfc-editor.org/info/rfc8017/) and its [publisher-supplied BibTeX](https://datatracker.ietf.org/doc/rfc8017/bibtex/): K. Moriarty, B. Kaliski, J. Jonsson, A. Rusch; *PKCS #1: RSA Cryptography Specifications Version 2.2*; RFC 8017; November 2016; DOI `10.17487/RFC8017`. | `@report` retains the publisher's initials rather than expanding names without evidence. The paper cites the RSA specification, not a CANIS security evaluation. |
| `tink-envelope` | [Google Tink documentation](https://developers.google.com/tink/client-side-encryption): *I want to use client-side encryption with a cloud KMS*; page marked last updated 2026-11-06. | `@online` names Google, records the displayed date and 2026-11-16 access date. No Tink software version is inferred. |
| `vault-transit` | [HashiCorp Vault Transit documentation](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption): *Use envelope encryption* and documented Transit data-key/decrypt permissions. | `@online` names HashiCorp and records the 2026-11-16 access date. The page does not state a publication date, so none is invented. Product documentation can change. |

The cited standards describe established mechanisms, and the product pages
describe alternatives. They do not establish measured CANIS performance,
security strength, comparative advantage, or research adoption.

## Checks and actual outcomes

**Bibliography layout and keys.** From the repository root, a read-only
PowerShell comparison extracted `@...` keys from `joss-papers/paper.md` and
entry identifiers from `joss-papers/paper.bib`. It found exactly the four
keys above, with no missing or uncited entries. `Test-Path
joss-papers/paper.bib` returned `True`. This verifies static correspondence,
not BibLaTeX parsing or rendered citation quality.

**Java verification.** The exact commands executed on the current working
tree were:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss02.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
```

Both exited 0. Maven used Eclipse Adoptium Java 21.0.6, Maven 3.9.12, on
Windows 11 amd64. After the full suite and focused research rerun, the
Surefire XML under `client/target/surefire-reports/` and
`server/target/surefire-reports/` contained 149 tests, zero failures, zero
errors, and seven skips. `SocketClientTest` had 14 tests, `ServerEndToEndTest`
four, and `SharedTrustDomainResearchTest` one; each had zero failures, errors,
or skips. The focused test generated temporary credentials, TLS stores,
master key, database, and synthetic file fixtures as described in
[JOSS-01 method](joss01-method.md); JUnit cleaned those fixtures. Raw logs,
keys, passwords, and test-generated stores were not copied into this report.
The seven skips and the dirty revision mean these runs are not cross-platform
or fixed-release verification. They make no performance or adoption claim.

**Paper build attempt.** Docker Desktop was unavailable on this Windows host,
so the documented [JOSS-04 method](joss04-method.md) targets the Docker-
compatible engine in WSL `podman-machine-default` (server version 29.4.2).
No Inara image was cached in either its Docker-compatible or Podman image
store. The command
`wsl -d podman-machine-default -- docker pull
docker.io/openjournals/inara:1.3.1` exited 1: the WSL engine timed out
contacting `registry-1.docker.io` for the image manifest. A separate WSL
`curl -I --max-time 15 https://registry-1.docker.io/v2/` also timed out. A
direct `podman pull` reached the same registry timeout and was stopped after
its first failed attempt to avoid repeated identical retries. Therefore the
Inara container did not start, no `paper.pdf` was generated, and no claim of
a successful JOSS manuscript build or resolved rendered references is made.
The official [Inara v1.3.1 release](https://github.com/openjournals/inara/releases/tag/v1.3.1)
identifies the intended tool version, but the image ID/digest could not be
recorded without a pull.

## Interpretation and limits

**Observation:** The manuscript's four citation keys match four entries with
metadata checked against primary sources. **Evidence:** `joss-papers/paper.md`,
`joss-papers/paper.bib`, and the source table above. **Interpretation:** The
bibliography content and relative path are prepared for the JOSS builder.
**Limitation:** Static matching cannot establish BibLaTeX parsing or PDF
rendering; the JOSS-04 build acceptance criterion remains open.

**Observation:** The existing full and focused tests passed on this working
tree. **Evidence:** the executed commands and Surefire counts above.
**Interpretation:** The manuscript body was left unchanged under JOSS-04,
consistent with its existing narrow working-tree claims. **Limitation:**
This does not verify a future release, POSIX permission checks, or unknown
research adoption.

## Backlog traceability

| Backlog Task | Evidence | Status |
| --- | --- | --- |
| JOSS-04 confirm `paper.md`/`paper.bib` layout and path | `joss-papers/paper.md` YAML, colocated `joss-papers/paper.bib`, static `Test-Path` result | Complete on this working tree; fixed-release check remains JOSS-07. |
| JOSS-04 add entries and verify metadata | Four BibLaTeX entries in `joss-papers/paper.bib`; primary-source table above | Complete for the four current manuscript citations. |
| JOSS-04 resolve references and build with JOSS workflow | Static key comparison passed; earlier image pull timed out; later JOSS-05 adapted PDF rendered four references | Open: run the canonical local build and inspect it on the fixed candidate revision with final metadata. |
| JOSS-04 claim audit before manuscript edit | `docs/research/joss04-claim-evidence.md`; only YAML bibliography path changed | Complete for this issue's manuscript scope; JOSS-02 fixed-release audit remains open. |

No new experimental method, performance metric, security-strength result, or
research impact was produced by this bibliography task. A future canonical
local build should follow the pinned Inara workflow described in the
[JOSS-05 method](joss05-method.md), then update the open checklist item with
its actual exit code and reference inspection.

## Later JOSS-05 local-build addendum (2026-11-16)

**Observation:** After the earlier attempt above, the user selected a local
build. The [JOSS-05 direct Pandoc method](joss05-method.md) produced an
[adapted three-page draft PDF](joss05-draft.pdf) from Inara v1.3.1. Four
references rendered, including the full NIST institution name after the
`dworkin2007` BibTeX value was brace-protected. **Evidence:** The executable
[build script](build-joss05-windows.ps1), the PDF, and the
[JOSS-05 results](joss05-results.md) with its hash and inspection record.
**Interpretation:** The current working-tree bibliography resolves in this
adapted local draft. **Limitation:** The canonical GNU make, TeX Live, and
Hack-font workflow and a fixed candidate revision remain unverified, so the
JOSS-04 final checklist item remains open. The earlier attempt above remains
a historical record of its own outcome.
