# CANIS-KMS final JOSS pre-submission audit — 2026-11-19

## Decision

**NO-GO — not ready to submit to JOSS.** This is an evidence audit only. No
submission, tag move, release change, code change, or claim of editorial approval
was made. The fixed candidate is annotated tag `v0.1.0-rc.1`, commit
`e1d6998b5c5658750870b39e2a4d511de5bed7f3`. Current HEAD is
`e302964d6ed9208be43849191da9c8f2f302c76b`; the working tree already
contained uncommitted JOSS-07 documentation when this audit began. The tagged
paper and bibliography are byte-identical to the earlier
[JOSS-02 final claim audit](joss02-final-claim-audit.md) version. Among the
client/server source, root POM, and paper paths compared with that revision,
only the root POM version changed. The public release is a prerelease, not a
JOSS submission.

The decision uses the current [JOSS submission requirements](https://joss.readthedocs.io/en/latest/submitting.html),
[paper format](https://joss.readthedocs.io/en/latest/paper.html), and
[review criteria](https://joss.readthedocs.io/en/latest/review_criteria.html).
JOSS requires a software paper focused on the software, a clear scholarly need
and comparison with existing tools, demonstrated research use, more than six
months of active public development, usable software, and open development
signals. Editors decide fit and significance. The record establishes real but
bounded developer research use; it does not establish the full planned study or
an independent reproduction.

## Verification performed and provenance

| Check | Result | Limit |
| --- | --- | --- |
| Tagged source and paper | Local annotated tag and public Release target the same commit. `git diff` found no paper or bibliography change between `cd1ebeb32bbd0b6b2ebd42a29f72e3dac8b01694` and the tag. | The audit document itself is outside the published tag. |
| Project verification | Prior JOSS-07 exact-tag `mvn --batch-mode --no-transfer-progress clean verify` passed on Windows with Maven 3.9.12 and Java 21.0.6: 28 suites, 159 tests, zero failures/errors, eight explained skips. [Snapshot](joss07-snapshot.md#requirement-matrix). | No new Maven run was needed; five skipped cases need a separate server, three need POSIX. The tagged Ubuntu workflow reports a POSIX security gate. |
| Manuscript build | Prior exact-tag Windows Inara fallback produced a 234,369-byte PDF, SHA-256 `a6c1a21eda72d555b61b94a47d9f667e7c7ca2a12b22533a98ddea8118c80d77`; four references rendered. [Snapshot](joss07-snapshot.md#requirement-matrix). | Canonical GNU make/TeX Live/Hack-font build has not passed. Its Windows `make` recipe failed on POSIX environment syntax. |
| Paper static review | Eight required sections; four cited BibTeX keys, four entries, no unmatched keys; 61 relative Markdown links resolve to files. Pandoc plain text is approximately 1,394 words. | The older PowerShell checker falsely reports 0/8 headings on CRLF and inflates the count to 1,892 by counting link URLs. Visual review of a canonical PDF is pending. |
| Research artifacts | `verify-issue32.py`: passed, 24 analysis pairs and 4,160 file rows; `verify-issue33.py`: passed, five scenarios; `verify-issue34.py`: passed, seven cases × three repetitions and 159 artifacts. | Hash/relationship checks of retained artifacts do not recreate dirty-tree experiments or establish independent execution. |
| Public release assets | Downloaded source ZIP and checksum manifest from the [GitHub prerelease](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1); both SHA-256 values match Release API digests and the manifest. ZIP has 977 entries and 921 regular files. Comparing all regular files with a local `git archive` of the tag found no missing or extra files and no content difference after CRLF normalization. | 252 regular files differ in raw bytes because of line endings; byte-for-byte archive reproducibility is not established. |
| Repository screening | Public API: repository created 2024-12-10, currently public, Issues enabled, zero public issues and zero PRs; commit dates occur in Dec 2024, Apr–May 2026, and Nov 2026. | Creation date and current visibility do not prove six months of public development. There is no visible public issue/PR discussion. |
| License, documentation, support | GitHub API identifies SPDX `MIT`; tracked [`LICENSE.txt`](../../LICENSE.txt) contains the MIT text. [`README`](../../README.md), [reviewer guide](../reviewer-guide.md), [CONTRIBUTING](../../CONTRIBUTING.md), and [`SECURITY.md`](../../SECURITY.md) exist. | Installation and ordinary support are documented; `SECURITY.md` says a stable private vulnerability-reporting path still needs confirmation. |

## Backlog items — audited summary

The inventory below covers all **172 checkboxes** in
[`joss-backlog.md`](../../joss-backlog.md): 59 checked and 113 unchecked at this
snapshot. The per-checkbox table at the end preserves each item's line number,
recorded state, and linked evidence. A checked box is not automatically an
independent fixed-release reproduction. Phase 4 product work is included in
the inventory but is outside the current JOSS candidate scope unless a claim
depends on it.

| Item | Status | Evidence |
| --- | --- | --- |
| Phase 0 research fit | Open | [JOSS-01](joss01-results.md) records a narrow developer experiment; [Issue #35](issue35-results.md) retains incomplete original-question evidence. |
| Phase 1 safe research build | Partial | [CODE-01](../code01-integrity.md), [CODE-02](code02-results.md), [CODE-03](code03-results.md), [tag validation](joss07-snapshot.md); broader fixed/POSIX and packaged gates remain. |
| Phase 2 empirical study | Open | [Issue #35 acceptance](issue35-results.md#issue-35-acceptance-review): no planned local controls or service-availability windows, and several dirty-tree runs. |
| Phase 3 JOSS candidate | Open | [JOSS-07 snapshot](joss07-snapshot.md): release exists; independent reproduction, canonical paper build, and screening evidence remain. |
| Phase 4 product hardening | Open; later scope | [Backlog Phase 4](../../joss-backlog.md#phase-4--product-hardening-beyond-the-current-study). Not a blanket JOSS prerequisite or production-readiness claim. |
| JOSS-01 significance | Partial | Question/audience and credential-level boundary test in [results](joss01-results.md); historical letter is not a public source artifact; broader impact still an editorial question. |
| CODE-01 integrity | Partial | Implemented tests and limits in [CODE-01](../code01-integrity.md); tagged Windows suite passed, prior POSIX gate recorded; remaining packaged/permission and durability limits. |
| CODE-02 process harness | Complete for bounded smoke | [Clean Ubuntu CI normal run](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17); Windows negative runs have earlier provenance. |
| CODE-03 size preflight | Complete for bounded preflight | [Windows measurements](code03-results.md) through 4 MiB; not a general maximum or fixed-release campaign. |
| App Issue #7 master-key protection | Partial | [CODE-01](../code01-integrity.md) and [master-key operations](../master-key.md); owner/ACL and runtime-loss coverage remains open in backlog. |
| App Issue #8 database recreation | Partial | [CODE-01](../code01-integrity.md) covers tested failure-safe paths; two-move crash window and cross-filesystem durability remain. |
| App Issue #36 private-key exposure | Open in backlog; research-release subset scoped | All nine Issue #36 checkboxes remain unchecked despite [JOSS-02 release boundary](joss02-release-boundary-final.md) recording implemented checks. Reconcile this mismatch; do not infer full product issue closure. |
| Issue #31 workload/baseline | Protocol complete; full study open | [Protocol](issue31-protocol.md) and [absolute baseline](issue31-baseline-results.md); local controls and paired comparison absent, final run dirty. |
| Issue #32 normal-operation measurements | Complete within fixed Windows scope | [Result](issue32-results.md), [manifest](issue32-artifacts.sha256), passing verifier; no isolated TLS cost or general capacity result. |
| Issue #33 delayed exchange | Complete as local functional record; release reproduction open | [Five scenario result](issue33-results.md), passing verifier, one fixed-candidate D smoke; full series dirty. |
| Issue #34 outages/faults | Open | [21 selected fault runs](issue34-results.md) passed artifact verification; planned outages, key deletion, full admission/envelope matrix, and detection latency absent. |
| Issue #35 study synthesis | Partial | [Synthesis and acceptance](issue35-results.md#issue-35-acceptance-review) map claims and limits; fixed-revision execution and original availability answer remain open. |
| Gate A research fit | Open | [JOSS-01](joss01-results.md) and [Issue #35](issue35-results.md); distinct scholarly contribution and realized impact remain uncertain. |
| Gate B supported release | Complete for bounded candidate | [JOSS-02 final audit](joss02-final-claim-audit.md), [tag verification](joss07-snapshot.md), exact-tag Maven suite. |
| Gate C reproducibility | Open | [JOSS-03](joss03-results.md) and [JOSS-07](joss07-snapshot.md): no non-author install, example, and evaluation reproduction. |
| Gate D submission artifacts | Open | [Paper](../../joss-papers/paper.md) and [bibliography](../../joss-papers/paper.bib) exist; canonical build and software archive reference absent. DOI belongs to post-review archive stage. |
| JOSS-02 release boundary | Complete within stated scope | [Final claim audit](joss02-final-claim-audit.md), [candidate validation](joss02-validation.md), [release record](release-candidate.md). |
| JOSS-03 install/docs/CI | Partial | [Reviewer guide](../reviewer-guide.md), [CI evidence](joss03-results.md), tagged suite; no non-author proof or stable private reporting route. |
| JOSS-04 bibliography | Partial | Four primary entries checked in [JOSS-04](joss04-results.md); canonical tagged paper build and archive reference pending. |
| JOSS-05 manuscript | Partial | [Claim audit](joss02-final-claim-audit.md) finds no unqualified unsupported technical assertion, but the manuscript needs JOSS paper framing, archive reference, and canonical rendering. |
| JOSS-06 disclosures | Complete for candidate preparation | [Author-approved audit](joss06-audit.md) matches paper YAML, acknowledgements, and AI disclosure; actual date is set only when submitting. |
| JOSS-07 snapshot | Partial; open | [Snapshot](joss07-snapshot.md) verifies tag and release but lists remaining independent, manuscript, support, and screening gates. |
| Detailed submission check 1 bibliography | Partial | Four references resolve in adapted build; canonical build open. |
| Detailed submission check 2 TLS workflow | Partial | Client/server TLS and end-to-end tests passed; final release documentation reconciliation box remains unchecked. |
| Detailed submission check 3 significance | Partial | Bounded developer use; full Issues #31–35 question and reproducibility gate open. |
| Detailed submission check 4 disclosures | Complete for preparation | [JOSS-06 audit](joss06-audit.md); date deferred. |
| Detailed submission check 5 snapshot | Open | Release and license verified; independent reproduction, canonical build, public history, and paper completion open. |
| Cross-cutting TLS/fixture maintenance and product Issues #9–30 | Open; later scope | [Backlog Phase 4](../../joss-backlog.md#phase-4--product-hardening-beyond-the-current-study). Their absence is a disclosed product limit, not a reason to claim the candidate is production ready. |

## Paper claims — exact candidate

The tagged `paper.md` is unchanged from the 43-claim
[JOSS-02 final audit](joss02-final-claim-audit.md), so its claim boundaries
apply to this release. The table below retains each material assertion and
direct evidence. **Verified** means consistent with source, tagged tests, or
the cited bounded record. **Partial** marks a historical attribution or a
dirty-tree study whose original execution cannot be independently recreated
from the tag. It does not mean that a limited result has become a general
security or availability claim. No material claim was classified as an
unqualified contradiction in that audit; JOSS fit and paper structure remain
separate questions.

| Claim | Evidence | Verified |
| --- | --- | --- |
| **Title/YAML:** Java KMS and asynchronous file-exchange software | [Maven reactor](../../pom.xml), [CODE-02 method](code02-method.md), candidate normal smoke | Yes, within stated scope |
| **Summary:** Local `CANISHYB1` encryption, shared publication, later key-only recovery over TLS | [Envelope source](../../client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), [client](../../client/src/main/java/io/canis/jpaw/client/JpawClient.java), [CANISP guide](../canisp-protocol.md#file-envelope-model), normal smoke | Yes, within stated scope |
| Separate-process CODE-02 workflow was exercised | [CODE-02 result](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17), candidate normal JSON | Yes, within stated scope |
| Admitted clients can recover under any stored key name; JOSS-01 observed it | [Threat model](../threat-model.md), [JOSS-01 result](joss01-results.md#observed-evaluation), candidate focused report | Yes, within stated scope |
| Completed studies are bounded; original local controls and service-outage windows remain unmeasured | [Issue #31 deviations](issue31-baseline-results.md#reproduction-and-acceptance-review), [Issue #34 limits](issue34-results.md#limits-and-operational-impact), [Issue #35 synthesis](issue35-results.md#11-limitations) | Yes, within stated scope |
| **Need:** Sensitive financial-system exchange is the author's motivation | [JOSS-01 historical account](joss01-results.md#historical-operational-evidence-and-research-motivation) | Partial — attribution is explicit; original letter is not a release artifact |
| A 2024 letter reports predecessor use; the author attests design continuity | [JOSS-01 historical account and limits](joss01-results.md#historical-operational-evidence-and-research-motivation) | Partial — paper attributes both statements and claims no current-code deployment |
| The letter cannot establish deployment or compatibility of this code revision | [JOSS-01 source limits](joss01-results.md#historical-operational-evidence-and-research-motivation) | Yes, within stated scope |
| Evaluations used generated fixtures; sizes, arrivals, and delays are assumptions | [CODE-02 fixture method](code02-method.md#process-and-fixture-layout), [Issue #31 method](issue31-baseline-method.md#execution), [Issue #32 method](issue32-method.md#fixed-workload-and-comparison) | Yes, within stated scope |
| Shared-trust, load, and selected-fault questions have partial observed answers; no availability window | [JOSS-01 result](joss01-results.md#observed-evaluation), [#31 result](issue31-baseline-results.md#result-and-boundary), [#34 scope](issue34-results.md#result-and-scope) | Yes, within stated scope |
| Intended audience is engineers and researchers | [Author-validated JOSS-01 record](joss01-results.md#author-validated-question-audience-and-scope) | Yes, within stated scope |
| **Field:** AES-GCM and RSA-OAEP are established mechanisms, with no primitive novelty claim | [Envelope source](../../client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), [JOSS-04 source review](joss04-results.md#primary-source-verification), [bibliography](../../joss-papers/paper.bib) | Yes, within stated scope |
| Tink/Vault documentation exists; CANIS combines its file client, CANISP service, encrypted persistence, and shared access | [JOSS-04 review](joss04-results.md#primary-source-verification), [protocol](../canisp-protocol.md), candidate workflow | Yes, within stated scope |
| No completed Tink/Vault comparison or comparative advantage claim | [Study inventory](issue35-results.md#evidence-inventory), [Issue #32 comparator scope](issue32-results.md#result-and-scope) | Yes, within stated scope |
| **Design:** Java 21 client/server Maven modules | [Root POM](../../pom.xml), candidate `mvn clean verify` | Yes, within stated scope |
| AES-GCM file key, server RSA public key, RSA wrapping, `CANISHYB1`; official decrypt returns key only | [Envelope](../../client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), [store](../../server/src/main/java/io/canis/store/KeyValueStore.java), [handler](../../server/src/main/java/io/canis/handlers/ClientHandler.java) | Yes, within stated scope |
| File bytes stay on client/shared-storage path; admitted client receives recovered key | [CODE-02 layout](code02-method.md#process-and-fixture-layout), [client source](../../client/src/main/java/io/canis/jpaw/client/JpawClient.java), candidate smoke | Yes, within stated scope |
| Envelope tag authenticates encrypted content but does not establish producer identity | [Envelope source](../../client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), [#33 interpretation](issue33-results.md#interpretation-risks-and-future-work) | Yes, within stated scope |
| Whole-file allocations; CODE-03 exercised through 4 MiB on one Windows host, no general maximum | [CODE-03 memory map/result](code03-results.md#supported-workload-boundary-and-streaming-decision), matched retained CSV hashes | Partial — retained dirty-tree preflight, explicitly bounded |
| AES-GCM-encrypted `CANISDB1` and external master-key file | [Store](../../server/src/main/java/io/canis/store/KeyValueStore.java), [loader](../../server/src/main/java/io/canis/crypto/MasterKeyLoader.java), [CODE-01 record](../code01-integrity.md) | Yes, within stated scope |
| Invalid startup material/records rejected in local checks | [CODE-01 acceptance](../code01-integrity.md#acceptance-evidence), candidate store/server tests | Yes, within stated scope |
| Issue #34 damaged database or mismatched key rejected restart until exact restore; live server cached master key | [#34 observations](issue34-results.md#scenario-observations), [store source](../../server/src/main/java/io/canis/store/KeyValueStore.java), candidate E smoke | Partial — E repeated on candidate; A/B remain dirty-tree historical observations |
| Recovery required operator restoration of exact matching synthetic material; no automatic recovery evaluated | [#34 scenario E](issue34-results.md#scenario-observations), candidate E smoke | Yes, within stated scope |
| Database replacement uses two moves; crash durability unmeasured | [CODE-01 limits](../code01-integrity.md#operational-and-compatibility-limits), [store source](../../server/src/main/java/io/canis/store/KeyValueStore.java) | Yes, within stated scope |
| Official transport requires TLS 1.3/mTLS, certificate and hostname checks, then CANIS credentials | [Client socket](../../client/src/main/java/io/canis/jpaw/client/SocketClient.java), [server](../../server/src/main/java/io/canis/Server.java), fixed TLS suite | Yes, within stated scope |
| Admitted clients share key-name access; no per-service key authorization | [Handler](../../server/src/main/java/io/canis/handlers/ClientHandler.java), candidate JOSS-01 and #33 D smokes | Yes, within stated scope |
| Create/delete exists; managed rotation and revocation of already recovered data keys do not | [Store](../../server/src/main/java/io/canis/store/KeyValueStore.java), [end-to-end tests](../../server/src/test/java/io/canis/ServerEndToEndTest.java) | Yes, within stated scope |
| Issue #32 non-TLS loopback fixture is research-only | [#32 method](issue32-method.md#prespecification-and-scope), [product TLS source](../../server/src/main/java/io/canis/Server.java) | Yes, within stated scope |
| Audit events lack event ID/integrity verifier; #34 external hashes detected changes with continued workflow | [Audit logger](../../server/src/main/java/io/canis/audit/AuditLogger.java), [#34 D](issue34-results.md#scenario-observations) | Partial — D remains dirty-tree historical execution; source absence is clear |
| **Impact:** JOSS-01 recipient/observer byte-matched; changed observer password failed; one shared certificate | [JOSS-01 method](joss01-method.md), candidate focused Surefire report | Yes, within stated scope |
| CODE-02 clean Ubuntu normal smoke; older Windows negatives rejected selected inputs | [CODE-02 result](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17), candidate normal smoke | Partial — normal repeated; historical Windows harness negatives retain dirty provenance |
| CODE-03 two four-file attempts per size plus one 100-file schedule check | [CODE-03 raw table](code03-results.md#raw-observations), matching [run](code03-runs.csv)/[file](code03-files.csv) hashes | Partial — historical dirty-tree preflight only |
| Issue #31 2,000 files in 20 absolute CANIS runs | [#31 result](issue31-baseline-results.md#result-and-boundary), matching [run](issue31-baseline-runs.csv)/[file](issue31-baseline-files.csv) hashes | Partial — historical dirty-tree baseline only |
| Issue #32 2,000 files per arm across 20 clean fixed-revision pairs; +13.334 ms median paired difference with stated range | [#32 result](issue32-results.md#observed-latency-and-throughput), [pair CSV](issue32-artifacts/analysis-pairs.csv), passing artifact verifier | Yes, within stated scope |
| Issue #32 difference includes admission/adapter effects, not isolated TLS cost | [#32 method](issue32-method.md#prespecification-and-scope), [limits](issue32-results.md#risks-and-limitations) | Yes, within stated scope |
| Issue #33 five functional scenarios and their outcomes | [#33 results](issue33-results.md#actual-scenario-observations), [28-artifact manifest](issue33-artifacts.sha256), passing verifier, candidate D smoke | Partial — D repeated; five-case series remains dirty-tree historical evidence |
| Issue #34 21 selected fault runs and their outcomes | [#34 results](issue34-results.md#scenario-observations), [159-artifact manifest](issue34-artifacts.sha256), passing verifier, candidate E smoke | Partial — E repeated; full series remains dirty-tree historical evidence |
| #34 outcomes are not availability or security-strength estimates | [#34 limits](issue34-results.md#limits-and-operational-impact), [#35 synthesis](issue35-results.md#11-limitations) | Yes, within stated scope |
| Resource/fault studies used one Windows loopback host, synthetic fixtures; #31/#33/#34 dirty, #32 clean | [Issue #35 inventory](issue35-results.md#evidence-inventory), individual result environments | Yes, within stated scope |
| Original local controls, outage windows, larger/distributed work, and independent release reproduction remain future work | [#31 deviations](issue31-baseline-results.md#reproduction-and-acceptance-review), [#34 limits](issue34-results.md#reproducibility-and-acceptance-review), [JOSS-03 backlog](../../joss-backlog.md#joss-03--installation-examples-documentation-and-ci-p0p1) | Yes, within stated scope |
| No general scalability, availability, production-readiness, cryptographic-strength, or current-code adoption claim | [Issue #35 limitations](issue35-results.md#11-limitations), [JOSS-01 history limits](joss01-results.md#historical-operational-evidence-and-research-motivation) | Yes, within stated scope |
| **Disclosure/YAML:** Sole author and affiliation; funding, support, conflicts, and AI use as stated | [Author-approved JOSS-06 audit](joss06-audit.md), candidate [paper](../../joss-papers/paper.md) | Yes, within stated scope |
| Four cited references have bibliography entries | [Bibliography](../../joss-papers/paper.bib), [JOSS-04 review](joss04-results.md#primary-source-verification) | Yes, within stated scope |

### Paper-format and bibliography gaps

The paper has the required YAML author/affiliation and eight sections, and its
four keys resolve to the [NIST GCM publication](https://csrc.nist.gov/pubs/sp/800/38/d/final),
[RFC 8017](https://www.rfc-editor.org/info/rfc8017/),
[Google Tink documentation](https://developers.google.com/tink/client-side-encryption),
and [Vault Transit documentation](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption).
The author-approved [JOSS-06 disclosure](joss06-audit.md) covers tools, known
versions or version limits, scope, human review, funding/support, and conflicts.
The source letter behind predecessor-use history is not published in this
repository and cannot independently validate the historical claim. The paper
does not cite or link the released software archive. Its state-of-field section
describes alternatives but does not yet make JOSS's requested explicit
“build vs. contribute” case. The long research-impact section gives many new
study results; JOSS says the paper should focus on the software rather than
new research results. A final author edit and canonical PDF review are needed.

## Release artifacts

“Verified” distinguishes a downloaded public release attachment, a retained
local validation output, and a fresh rebuild. Only the source ZIP and checksum
manifest are attached to GitHub Release. GitHub-generated source downloads are
separate from the attached, checksummed ZIP.

| Artifact | Present | Verified |
|-----------|----------|----------|
| Annotated `v0.1.0-rc.1` tag | Yes, local and public Release target | Current local tag and Release API both identify `e1d6998b5c5658750870b39e2a4d511de5bed7f3`. |
| [Source ZIP](https://github.com/metiago/canis-kms/releases/download/v0.1.0-rc.1/canis-kms-0.1.0-rc.1-src.zip) | Yes, public attachment; downloaded | SHA-256 `81f132bf6b69b482649e6d4e300c7b40c5394954ac858c0ff2b2bf4a49621c8a`; manifest and API agree. 921 regular files match the tag after newline normalization. |
| [Checksum manifest](https://github.com/metiago/canis-kms/releases/download/v0.1.0-rc.1/checksums_sha256.txt) | Yes, public attachment; downloaded | SHA-256 `1000b1ebd86791c94e9d437689be40cd8827a2666d95c70e7ee7d5d28c40d86c`; its source-ZIP digest matches the downloaded ZIP. |
| JReleaser `CHANGELOG.md` | Yes, retained local/CI output; not attached | Local and CI-generated hashes match [release record](release-candidate.md): `c9b8b56c50b33e3dbd107387f781a07887341f22b68fcb50a9da16cb06e2d95f`. |
| `client-0.1.0-rc.1.jar` | Yes, retained local output; not attached | Prior local artifact hash matches record: `3c37830f731da7151c08d631fd61fdd0a57579f33dd3ba652d5243f830ede676`; fresh tag build passed tests but JAR bytes differ. |
| `canis.jar` | Yes, retained local output; not attached | Prior local artifact hash matches record: `dd03c90450d6ed4510dbd45594bdbfbd59eb27f0c8509823d5ed4b37ac21b668`; fresh tag build bytes differ. |
| `canis-jar-with-dependencies.jar` | Yes, retained local output; not attached | Prior local artifact hash matches record: `187ad0be89f467760bba6d0b0e6fcb620e48a4972239463f419830569c98fed1`; fresh tag build bytes differ. |
| Exact-tag Windows fallback `paper.pdf` | Yes, ignored local audit output; not a release attachment | 234,369 bytes and SHA-256 `a6c1a21eda72d555b61b94a47d9f667e7c7ca2a12b22533a98ddea8118c80d77`; four references rendered, canonical styling not proven. |
| Canonical JOSS PDF | No | GNU make/TeX Live/Hack-font build has not passed on the tagged candidate. |

The JReleaser config/dry run generated the expected local outputs. The tagged
Actions build and POSIX gate passed, but its JReleaser publication step failed
on `main` versus `master` branch detection. The subsequent GitHub Releases API
publication produced the public prerelease and two verified attachments. This
is an automation risk, not a missing release.

## Missing items and remaining risks

### Submission blockers

1. **Independent reproduction:** No non-author has documented clean-checkout
   install, separate-process example, and one meaningful evaluation on this
   candidate. This is JOSS-03/JOSS-07 and Gate C acceptance work.
2. **Canonical manuscript:** The tagged paper has only an adapted Windows PDF.
   Run the canonical JOSS/Inara build, inspect metadata and references, add a
   released source archive reference, and make the explicit comparison with
   existing tools. A revised paper requires a new fixed candidate; do not move
   the published tag. Enter the true submission date only at submission.
3. **Research fit and full study:** JOSS-01 provides narrow developer use and
   Issues #31–35 provide bounded records. The original local-control and
   service-availability questions remain unexecuted, several experiments used
   dirty working trees, and no independent release reproduction exists. The
   manuscript's research contribution and “build vs. contribute” case need an
   author-level assessment against JOSS screening. An external adopter is not
   an absolute JOSS requirement.
4. **Public development evidence:** Repository creation was more than six
   months ago, but the date it became public is unverified. The public API
   shows zero issues and zero PRs. JOSS's public-history and open-development
   screening is therefore not evidenced sufficiently for a safe submission
   recommendation.
5. **Repository support and backlog consistency:** `SECURITY.md` still asks a
   maintainer to establish a stable private vulnerability route. JOSS-07 and
   detailed submission checks remain open; Issue #36's unchecked product tasks
   should be reconciled with the narrower JOSS-02 release decision.

### Material risks that remain disclosed

- Every admitted client can request recovery under any stored key name;
  key-specific authorization is absent by policy.
- `CANISDB1` replacement uses two moves; interruption and crash durability
  were not measured. The audit stream has no event ID or built-in integrity
  verification. These are security and operational limits, not claims of
  demonstrated exploitation.
- Performance and fault observations mostly come from one Windows loopback
  host; CODE-03, Issue #31, #33, and #34 historical executions used dirty
  working trees. Verifier passes authenticate retained artifact relationships,
  not independent reproductions.
- The published source archive and Windows tag archive differ in newline
  bytes, and fresh JAR rebuilds differ from retained release-validation JAR
  hashes. Functional verification passed, but reproducible binary bytes were
  not established.
- The JReleaser branch-detection failure and broken CRLF/URL-counting static
  paper checker are process defects to fix before treating automation as a
  final submission gate.

## Go/No-Go recommendation

**NO-GO.** The release itself is fixed and publicly downloadable, but Gate A
(fit), Gate C (independent reproduction), and Gate D (complete manuscript) are
open. The public-development screening record also remains uncertain. A
permanent archive DOI is expected after successful review under JOSS's process
and is not counted as a pre-submission blocker. Phase 4 product features are
outside the paper's supported claims and are not wholesale submission gates.

## Every backlog checkbox — recorded state and evidence

This appendix is an exhaustive line-by-line inventory. “Checked” reproduces
the backlog's state; “Open” means the criterion is not marked complete. For
long wrapped items, the text below condenses the first line; the line number
identifies the exact checklist item in the backlog. Evidence links are the
item's own first cited source where one exists; otherwise the row states that
no acceptance evidence is linked. The audited interpretation and submission
priority are in the summary and blockers above.

| Item | Status | Evidence |
| --- | --- | --- |
| L11: Phase 0 — Research fit: Authors validate the question and provide inspectable actual research-use evidence. | Open (recorded) | No acceptance link in checkbox |
| L12: Phase 1 — Safe research build: Security prerequisites and a repeatable, isolated exchange harness are verified. | Open (recorded) | No acceptance link in checkbox |
| L13: Phase 2 — Empirical study: Predefined workloads, experiments, and reproducible results answer the research question. | Open (recorded) | No acceptance link in checkbox |
| L14: Phase 3 — JOSS candidate: A fixed, independently reproducible release and complete manuscript meet submission gates. | Open (recorded) | No acceptance link in checkbox |
| L15: Phase 4 — Product hardening: Remaining operational, audit, runtime, and deployment issues meet their acceptance criteria. | Open (recorded) | No acceptance link in checkbox |
| L38: Satisfied — question and audience: The author identifies Issues #31–35 as the empirical study, CANIS as the artifact being evaluated, and software engineers, security engineers, and researchers as the a… | Checked (recorded) | [JOSS-01 results](../../docs/research/joss01-results.md) |
| L39: Partially satisfied — actual research use: The executed JOSS-01 boundary experiment documents narrow developer research use of CANIS; later Issues #31–35 provide bounded observations but do not complete… | Open (recorded) | [JOSS-01 evidence record](../../docs/research/joss01-results.md) |
| L40: Partially satisfied — supportable claims: The synthetic method and results support only the observed shared-access behavior and its limits. Recheck claims against a fixed candidate and obtain author app… | Open (recorded) | [method and results](../../docs/research/joss01-results.md) |
| L50: Implement and verify the research-release security parts of app Issues #7 and #8 on the local Windows candidate: supported master-key construction paths reject invalid configuration, malformed stored re… | Checked (recorded) | [CODE-01 integrity evidence](../../docs/code01-integrity.md) |
| L51: Implement the private-key exposure and stored-key validation parts of app Issue #36 on the same candidate. The CODE-01 evidence records the shared-access policy and tests; per-key denial belongs to a di… | Checked (recorded) | [CODE-01 evidence](../../docs/code01-integrity.md) |
| L52: Run local tests for valid `CANISDB1` restart and existing file envelopes, invalid certificates and credentials, database tampering, and failure paths without leaking secrets. See the CODE-01 acceptance… | Checked (recorded) | [CODE-01 acceptance matrix](../../docs/code01-integrity.md#acceptance-evidence) |
| L53: Commit a candidate revision: `6f0099ad8112fe4c582da39d4305a5bc5a6eb987` contains the CODE-01 implementation. The local evidence predates that commit. | Checked (recorded) | [local evidence](../../docs/code01-integrity.md) |
| L54: Rerun the security, compatibility, and packaged-startup checks against the exact committed revision. Run the skipped POSIX permission and symlink cases on POSIX CI and retain sanitized results before cl… | Open (recorded) | No acceptance link in checkbox |
| L60: Implement a documented producer, delayed consumer, and CANIS server run as separate processes against a temporary shared directory. Use externally supplied TLS and master-key fixtures; never place priva… | Checked (recorded) | [method](../../docs/research/code02-method.md#process-and-fixture-layout), [CI result](../../docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) |
| L61: Publish complete encrypted envelopes atomically; have the consumer ignore incomplete files and record byte-equality results only after authenticated decryption. Keep the research harness outside the pro… | Checked (recorded) | [normal and negative runs](../../docs/research/code02-results.md#commands-and-observed-outcomes), [Ubuntu CI run](../../docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) |
| L62: Emit sanitized, machine-readable run metadata: revision, runtime, scenario, file size, concurrency, timing, memory, outcome, and failure category. Define a stable run ID without logging keys or plaintex… | Checked (recorded) | [result format](../../docs/research/code02-method.md#results-cleanup-and-limits), [CI artifact](../../docs/research/code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) |
| L63: Include repeatable setup, teardown, and fixture cleanup on supported platforms. Ensure failure injection cannot alter a real database or deployment. See the Windows negative runs, isolation method, and… | Checked (recorded) | [Windows negative runs](../../docs/research/code02-results.md#commands-and-observed-outcomes), [isolation method](../../docs/research/code02-method.md#process-and-fixture-layout) |
| L109: Measure the existing `Files.readAllBytes` behavior and record a bounded supported workload. The CODE-03 Windows preflight verified 4 KiB, 256 KiB, 1 MiB, and 4 MiB in two four-file runs each with four c… | Checked (recorded) | [CODE-03 Windows preflight](../../docs/research/code03-results.md) |
| L110: The planned synthetic workload fits the observed bound on this host: one supplementary 100-file run verified the Issue #31 size mix, 500 ms arrivals, 60 s delay, and four workers. CODE-03 results docume… | Checked (recorded) | [CODE-03 results](../../docs/research/code03-results.md) |
| L124: Reconcile the earlier public-constructor finding with `Server.verifyConfiguration`; `ServerTest` now covers missing, malformed, mismatched, and changed master-key configurations before binding. | Checked (recorded) | No acceptance link in checkbox |
| L125: Review ancestor-directory permissions and path-replacement resistance; master-key operations require trusted ancestor control and state the remaining race limit. | Checked (recorded) | [master-key operations](../../docs/master-key.md#operational-limits-for-the-code-01-build) |
| L126: Record the decision for runtime loss of the cached master-key file: the running process does not alert automatically, so operators must monitor the file and detect loss before restart. See master-key op… | Checked (recorded) | [master-key operations](../../docs/master-key.md#operational-limits-for-the-code-01-build) |
| L127: Add a sanitized master-key log assertion in `MasterKeyLoaderTest`. | Checked (recorded) | No acceptance link in checkbox |
| L128: Run POSIX permission and symlink tests on POSIX CI; add owner-mismatch and inherited Windows ACL cases. | Open (recorded) | No acceptance link in checkbox |
| L132: No supported server construction path starts with an invalid master-key configuration in the tested Windows paths; constructor tests and packaged smoke evidence support this. POSIX and ACL coverage rema… | Checked (recorded) | [constructor tests and packaged smoke evidence](../../docs/code01-integrity.md#acceptance-evidence) |
| L140: Reject authenticated database records with invalid RSA fields or malformed UTF-8 without rewriting the database; storage tests coordinate this with Issue #36. | Checked (recorded) | [storage tests](../../docs/code01-integrity.md#acceptance-evidence) |
| L141: Close the check-to-move recreation path in `KeyValueStore.saveToFile` by moving the verified old database to recovery before installing the staged file. The operational limits describe the two-move inte… | Checked (recorded) | [operational limits](../../docs/code01-integrity.md#operational-and-compatibility-limits) |
| L142: Add deterministic tests for deletion after verification, interrupted and unsupported atomic moves, symlink rejection, and storage-failure log sanitization. The symlink case was skipped on this Windows h… | Checked (recorded) | No acceptance link in checkbox |
| L143: Exercise the packaged `--init-db` command end to end: the first call created a database, the second failed without changing its hash, and normal startup rejected a missing database. See packaged smoke e… | Checked (recorded) | [packaged smoke evidence](../../docs/code01-integrity.md#acceptance-evidence) |
| L144: Review idle-time database-loss detection, directory metadata durability, and the cost of reading and hashing large databases; master-key operations state the unresolved deployment limits. | Checked (recorded) | [master-key operations](../../docs/master-key.md#operational-limits-for-the-code-01-build) |
| L148: Corrupted or malformed stored records fail safely in the local storage tests. | Checked (recorded) | [local storage tests](../../docs/code01-integrity.md#acceptance-evidence) |
| L149: No tested write path silently recreates a missing cryptographic database; deletion-boundary and recovery tests support this under the documented trusted-directory assumption. Cross-filesystem crash beha… | Checked (recorded) | [deletion-boundary and recovery tests](../../docs/code01-integrity.md#acceptance-evidence) |
| L164: Validate loaded `CANISDB1` public and private key encodings and that they form a matching RSA | Open (recorded) | No acceptance link in checkbox |
| L167: Remove the unused `privateKey` field and Java serialization capability from the client | Open (recorded) | No acceptance link in checkbox |
| L169: Confirm that `\|decrypt` is available only after TLS client-certificate and credential | Open (recorded) | No acceptance link in checkbox |
| L171: Record the cross-service blast radius and audit the requesting identity and key name without | Open (recorded) | No acceptance link in checkbox |
| L174: Add tests for a private-key encoding placed in a stored public-key field, malformed legacy | Open (recorded) | No acceptance link in checkbox |
| L178: Document startup failure behavior and compatibility impact for existing databases and | Open (recorded) | No acceptance link in checkbox |
| L183: No private-key material is returned through Java APIs, serialization, logs, exceptions, or | Open (recorded) | No acceptance link in checkbox |
| L185: The shared-access policy is enforced only after authentication and is tested and documented; | Open (recorded) | No acceptance link in checkbox |
| L187: Valid existing `CANISDB1` databases and authorized file workflows remain usable after restart. | Open (recorded) | No acceptance link in checkbox |
| L199: Identify a permitted real file-exchange workflow or document why a synthetic workload represents it: producer/consumer count, file-size distribution, arrival rate, processing delay, concurrency, retenti… | Checked (recorded) | [Prespecified protocol](../../docs/research/issue31-protocol.md) |
| L200: Specify testable outcomes for correctness, resource use, and availability, with thresholds if claiming operational suitability. Define the baseline (for example, local AES-GCM cost plus CANIS key fetch/… | Checked (recorded) | [Prespecified protocol](../../docs/research/issue31-protocol.md) |
| L201: Record fixture generation, environment, JDK/Maven versions, warm-up, repetitions, measurement tools, and analysis plan before measuring. Avoid secrets and customer data in published fixtures. Prespecifi… | Checked (recorded) | [Prespecified protocol](../../docs/research/issue31-protocol.md) |
| L207: Run repeated file exchanges across the predefined file sizes, consumer worker counts, and processing delays. The method and Windows results report total and component latency, throughput, observed clien… | Checked (recorded) | [method](../../docs/research/issue32-method.md), [Windows results](../../docs/research/issue32-results.md) |
| L208: Separate public-key fetch and unwrap request intervals from local envelope encryption/decryption intervals where the research worker exposes those boundaries. Request intervals combine client, loopback… | Checked (recorded) | No acceptance link in checkbox |
| L209: Keep the non-TLS comparison in a loopback-only research harness compiled outside product artifacts; supported client/server traffic remains TLS 1.3 only. | Checked (recorded) | No acceptance link in checkbox |
| L215: Use separate producer, consumer, and server processes with a shared encrypted-file directory; verify byte equality after a documented delay and after a clean server restart. The test uses a controlled s… | Checked (recorded) | [Method](../../docs/research/issue33-method.md), [Windows observations](../../docs/research/issue33-results.md) |
| L216: Exercise multiple credential identities and distinct generated client certificates. Confirm the documented shared-access behavior, invalid-credential rejection, and file tamper rejection without treatin… | Checked (recorded) | [Scenario D and E evidence](../../docs/research/issue33-results.md#actual-scenario-observations) |
| L217: Record envelope publication/consumption ordering, duplicate and incomplete-file handling, and what happens if a file is present before its consumer starts. Scenario A and event records. | Checked (recorded) | [Scenario A and event records](../../docs/research/issue33-results.md#actual-scenario-observations) |
| L223: Use isolated fixtures to exercise CANIS unavailable before public-key fetch, during or before data-key recovery, and after restart; key deletion before delayed processing; corrupted envelope/tag; invali… | Open (recorded) | No acceptance link in checkbox |
| L224: For each scenario, predefine and report whether encryption/decryption succeeds, whether plaintext is released, time to detect failure, and recovery steps. Distinguish current design limits from defects. | Open (recorded) | No acceptance link in checkbox |
| L225: Add storage corruption, interrupted writes, audit tampering, or longer outages only where they answer the research question. Do not claim tamper-evident audit until app Issues #16–19 are implemented and… | Open (recorded) | No acceptance link in checkbox |
| L239: Publish methods, actual results, analysis, negative results, discussion, and limitations under `docs/research/`; separate observed behavior from inferred implications. | Open (recorded) | No acceptance link in checkbox |
| L240: Provide exact commands, non-secret fixtures, expected outputs, cleanup, revision, environment, platform limits, and artifact hashes for each experiment. | Open (recorded) | No acceptance link in checkbox |
| L241: Revise the proposed research question if the collected evidence cannot answer it; avoid claims of general reliability or security from a single setup. | Open (recorded) | No acceptance link in checkbox |
| L251: Gate A — Research fit (P0): Substantiate the question, audience, actual research use, and distinct need beyond existing KMS or envelope-encryption tools. | Open (recorded) | No acceptance link in checkbox |
| L252: Gate B — Supported release (P0): The fixed JOSS-02 candidate passed Windows validation and four smoke checks; the final claim audit and release boundary reconcile the manuscript and explicitly scope app… | Checked (recorded) | [JOSS-02 candidate](../../docs/research/joss02-candidate-release.md), [Windows validation and four smoke checks](../../docs/research/joss02-validation.md) |
| L253: Gate C — Reproducibility (P0): A non-author can install, test, run the separate-process example, and reproduce at least one meaningful evaluation. | Open (recorded) | No acceptance link in checkbox |
| L254: Gate D — Submission artifacts (P1): Complete manuscript, references, author metadata, AI disclosure, and public candidate snapshot. Plan a permanent archive; JOSS requests the archive DOI after successf… | Open (recorded) | No acceptance link in checkbox |
| L258: On the candidate revision, verify official-client TLS 1.3 mutual authentication, certificate and hostname validation, and no plaintext fallback. The fixed suite passed the client and server TLS cases. | Checked (recorded) | [candidate revision](../../docs/research/joss02-candidate-release.md), [fixed suite](../../docs/research/joss02-validation.md#fixed-revision-build-and-tests) |
| L259: Run the complete file workflow and negative TLS, credential, shared-access, and storage-tampering tests; retain sanitized outcomes and explain skips. The validation record reports 159 tests, zero failur… | Checked (recorded) | [validation record](../../docs/research/joss02-validation.md) |
| L260: Resolve or explicitly scope app Issues #7, #8, and #36, including database recreation, stored-key validation, private-key exposure, and the documented shared-trust blast radius. CODE-01 and the final bo… | Checked (recorded) | [CODE-01](../../docs/code01-integrity.md), [final boundary](../../docs/research/joss02-release-boundary-final.md#release-decision-and-impact) |
| L261: Repeat the claim-evidence audit against the fixed code and committed paper; record commit, OS, JDK/Maven versions, tests, and known limitations. See the final claim audit, candidate, and validation. | Checked (recorded) | [final claim audit](../../docs/research/joss02-final-claim-audit.md), [candidate](../../docs/research/joss02-candidate-release.md) |
| L274: Verify the documented Java 21/Maven build and external TLS/master-key provisioning from a clean checkout, including non-secret fixtures and any operational setup steps. | Open (recorded) | No acceptance link in checkbox |
| L275: Verify the committed Ubuntu/Windows CI workflow runs the full tests; confirm POSIX permission and symlink cases actually execute and the report gate passes. | Open (recorded) | No acceptance link in checkbox |
| L276: Verify public license, issue and contribution routes, a stable private security-reporting route, support expectations, and the required active public development history. | Open (recorded) | No acceptance link in checkbox |
| L284: Build the final paper on the fixed candidate with the canonical JOSS workflow; confirm all citations and references resolve and metadata renders correctly. | Open (recorded) | No acceptance link in checkbox |
| L290: Reframe the statement of need, state of the field, software design, and research impact statement around the validated Phase 2 question and actual JOSS-01 research-use evidence. Include only results pro… | Open (recorded) | No acceptance link in checkbox |
| L291: Check final JOSS sections, word count, formatting, references, and every technical or impact claim against the candidate revision. Keep observed behavior distinct from planned audit, rotation, and conta… | Open (recorded) | No acceptance link in checkbox |
| L297: Confirm all authors, affiliations, and substantive contributions; replace manuscript placeholders. The author-approved values and contribution list are recorded in the JOSS-06 audit and `paper.md` YAML. | Checked (recorded) | [JOSS-06 audit](../../docs/research/joss06-audit.md#author-and-contribution-record) |
| L298: Confirm funding, institutional support, acknowledgements, and any conflicts with the authors. The author-approved negative disclosures are recorded in the JOSS-06 audit and manuscript acknowledgements. | Checked (recorded) | [JOSS-06 audit](../../docs/research/joss06-audit.md#funding-support-acknowledgements-and-conflicts) |
| L299: Document actual AI tools, versions, and use across code, documentation, and paper; obtain human review, validation, and author approval. The JOSS-06 audit records three tools, their individual uses, Cha… | Checked (recorded) | [JOSS-06 audit](../../docs/research/joss06-audit.md#ai-use-inventory-and-human-review) |
| L300: Set the actual submission date only when submitting. | Open (recorded) | No acceptance link in checkbox |
| L315: Have a colleague who was not the primary developer install the candidate, run the example, and reproduce the documented evaluation. | Open (recorded) | No acceptance link in checkbox |
| L316: Fix and tag a public candidate; prepare a permanent archive and citation plan. After successful JOSS review, archive the reviewed version and record its DOI. | Open (recorded) | No acceptance link in checkbox |
| L317: Run all project tests and the canonical JOSS paper build on the exact candidate commit; retain sanitized reports, environment details, and artifact hashes. | Open (recorded) | No acceptance link in checkbox |
| L318: Verify repository public age, active development, actual research use, licensing, issue access, support paths, and other current JOSS screening criteria. | Open (recorded) | No acceptance link in checkbox |
| L319: Reconcile every submission task and evidence link in this single backlog before fixing the candidate release. | Open (recorded) | No acceptance link in checkbox |
| L331: Confirm the final manuscript and bibliography locations. The files are colocated as `paper.md` and `paper.bib`, and YAML uses `bibliography: paper.bib`; see JOSS-04 results. | Checked (recorded) | [`paper.md`](../../joss-papers/paper.md), [`paper.bib`](../../joss-papers/paper.bib) |
| L333: Add a BibTeX entry for every citation used in `paper.md`, including the cryptographic standards and software alternatives discussed there. Four cited keys match four entries in `paper.bib`; see JOSS-04… | Checked (recorded) | [`paper.bib`](../../joss-papers/paper.bib), [JOSS-04 results](../../docs/research/joss04-results.md) |
| L335: Verify each entry against its publisher or official project source, including title, author or organization, available date/version, and stable URL or DOI. The source table records the verification and… | Checked (recorded) | [source table](../../docs/research/joss04-results.md) |
| L337: Check that every bibliography entry is cited and every citation resolves; compile the manuscript with the JOSS toolchain. The JOSS-05 static check and adapted local PDF build found four matching keys an… | Open (recorded) | [JOSS-05 static check and adapted local PDF build](../../docs/research/joss05-results.md), [inspected draft](../../docs/research/joss05-draft.pdf) |
| L343: Review the server's TLS 1.3 mutual-authentication requirements and the client's `SocketClient` implementation against the fixed candidate and passing TLS tests. | Checked (recorded) | [fixed candidate and passing TLS tests](../../docs/research/joss02-validation.md#fixed-revision-build-and-tests) |
| L345: Implement client-side TLS 1.3 using externally supplied trust and identity material, with server certificate and hostname validation. Retain the project's no-plaintext-fallback requirement. Evidence: `S… | Checked (recorded) | [`SocketClient.java`](../../client/src/main/java/io/canis/jpaw/client/SocketClient.java), [`TlsContextFactory.java`](../../client/src/main/java/io/canis/jpaw/config/TlsContextFactory.java) |
| L347: Document required client and server configuration without embedding credentials or secret material. Evidence: reviewer guide, README, and master-key guide. The synthetic JOSS-03 test generates its own f… | Checked (recorded) | [reviewer guide](../../docs/reviewer-guide.md), [README](../../README.md) |
| L349: Add and run client transport tests for a trusted server/client pair, hostname or certificate rejection, untrusted client rejection, TLS-version enforcement, and failure behavior. Evidence: `SocketClient… | Checked (recorded) | [`SocketClientTest.java`](../../client/src/test/java/io/canis/jpaw/client/SocketClientTest.java), [JOSS-02 suite report](../../docs/research/joss02-suite-report.csv) |
| L351: Run an end-to-end test through the supported Java client against the TLS server, covering file encryption, server-side key unwrap, and file decryption. Evidence: `ServerEndToEndTest.java` and the JOSS-0… | Checked (recorded) | [`ServerEndToEndTest.java`](../../server/src/test/java/io/canis/ServerEndToEndTest.java), [JOSS-02 suite report](../../docs/research/joss02-suite-report.csv) |
| L353: Update the paper and deployment documentation to describe only the verified release behavior. | Open (recorded) | No acceptance link in checkbox |
| L359: Identify the specific research question and audience for which this software provides value beyond configuring an established KMS or envelope-encryption library. The author identifies CANIS as the artif… | Checked (recorded) | [`joss01-results.md`](../../docs/research/joss01-results.md) |
| L361: Complete and document developer research use in the planned Issues #31–35 study, including context, software revision, methods, results, and reproducibility artifacts. The executed JOSS-01 boundary test… | Open (recorded) | No acceptance link in checkbox |
| L363: Define the shared trust boundary evaluation protocol before execution in `joss01-method.md`. | Checked (recorded) | [`joss01-method.md`](../../docs/research/joss01-method.md) |
| L365: Publish scripts, generated fixture procedure, environment, and reproduction commands in `docs/research`. | Checked (recorded) | [`docs/research`](../../docs/research/joss01-method.md) |
| L367: Report only the observed categorical result and its limitations in `joss01-results.md`. The synthetic test is not adoption or a performance measurement. | Checked (recorded) | [`joss01-results.md`](../../docs/research/joss01-results.md) |
| L369: Reassess JOSS fit against current screening rules: the executed boundary test documents narrow developer research use, while Phase 2 results and a convincing impact case remain open. External adoption i… | Checked (recorded) | No acceptance link in checkbox |
| L375: Confirm the complete author list and each author's substantive contribution to the software and manuscript. See the JOSS-06 author and contribution record. | Checked (recorded) | [JOSS-06 author and contribution record](../../docs/research/joss06-audit.md#author-and-contribution-record) |
| L377: Replace YAML name and affiliation placeholders with author-approved metadata and valid JOSS formatting. See the paper YAML and JOSS-06 audit; final rendering remains part of the candidate manuscript build. | Checked (recorded) | [paper YAML](../../joss-papers/paper.md), [JOSS-06 audit](../../docs/research/joss06-audit.md#author-and-contribution-record) |
| L379: Ask all authors to confirm funding, institutional support, and acknowledgements; state these accurately. The sole author supplied negative disclosures, including conflicts of interest. | Checked (recorded) | [negative disclosures](../../docs/research/joss06-audit.md#funding-support-acknowledgements-and-conflicts) |
| L381: Record generative AI tools and versions used in software, documentation, and manuscript work, with the scope of each use. The JOSS-06 audit records each tool's use, ChatGPT model families, and the autho… | Checked (recorded) | [JOSS-06 audit](../../docs/research/joss06-audit.md#ai-use-inventory-and-human-review) |
| L383: Have human authors review, edit, and validate AI-assisted content, including technical claims, and confirm primary design decisions were made by the human team. The sole author's validation and decision… | Checked (recorded) | [validation and decision statement](../../docs/research/joss06-audit.md#ai-use-inventory-and-human-review) |
| L385: Replace the current draft disclosure with an accurate final statement and obtain author approval. The author-supplied tool names, scope, historical version limit, and responsibility statement are in `pa… | Checked (recorded) | [JOSS-06 audit](../../docs/research/joss06-audit.md#ai-use-inventory-and-human-review) |
| L391: Choose and establish the final JOSS paper directory/layout, including `paper.md`, `paper.bib`, and any figures, without breaking repository conventions. | Open (recorded) | No acceptance link in checkbox |
| L393: Confirm the submission points to a public, browsable repository with an OSI-approved license, accessible issue tracker, and release/archive suitable for citation. | Open (recorded) | No acceptance link in checkbox |
| L395: Review development history, releases, tests, installation instructions, usage examples, contribution guidance, and support/reporting paths against current JOSS criteria. The JOSS-03 review documents par… | Open (recorded) | [JOSS-03 review](../../docs/research/joss03-results.md) |
| L397: Confirm a colleague who was not the primary developer can install and exercise the documented workflow. | Open (recorded) | No acceptance link in checkbox |
| L399: Run the project verification suite and the JOSS paper build on the exact candidate revision; retain the logs/artifacts. | Open (recorded) | No acceptance link in checkbox |
| L401: Check manuscript word count, section headings, citations, author metadata, AI disclosure, and all statements against that candidate release. | Open (recorded) | No acceptance link in checkbox |
| L403: Record the actual submission date only when submitting; do not use a placeholder or future date. | Open (recorded) | No acceptance link in checkbox |
| L426: Keep the client and server TLS cipher-suite policies aligned when either policy changes; consider a shared policy or cross-module check. | Open (later product scope) | No acceptance link in checkbox |
| L427: Consider a shared TLS test fixture module to reduce duplicated certificate-authority helpers. | Open (later product scope) | No acceptance link in checkbox |
| L437: Rotação | Open (later product scope) | No acceptance link in checkbox |
| L438: Revogação | Open (later product scope) | No acceptance link in checkbox |
| L439: Auditoria correspondente | Open (later product scope) | No acceptance link in checkbox |
| L443: Chaves podem ser revogadas sem perda histórica | Open (later product scope) | No acceptance link in checkbox |
| L453: Backup completo | Open (later product scope) | No acceptance link in checkbox |
| L454: Restore completo | Open (later product scope) | No acceptance link in checkbox |
| L455: Restore após falha | Open (later product scope) | No acceptance link in checkbox |
| L456: Restore após corrupção parcial | Open (later product scope) | No acceptance link in checkbox |
| L460: Dados permanecem descriptografáveis | Open (later product scope) | No acceptance link in checkbox |
| L472: eventId | Open (later product scope) | No acceptance link in checkbox |
| L473: sequenceNumber | Open (later product scope) | No acceptance link in checkbox |
| L474: timestamp | Open (later product scope) | No acceptance link in checkbox |
| L475: identity | Open (later product scope) | No acceptance link in checkbox |
| L476: operation | Open (later product scope) | No acceptance link in checkbox |
| L477: resourceId | Open (later product scope) | No acceptance link in checkbox |
| L478: keyId | Open (later product scope) | No acceptance link in checkbox |
| L479: result | Open (later product scope) | No acceptance link in checkbox |
| L483: Esquema documentado | Open (later product scope) | No acceptance link in checkbox |
| L493: Login bem sucedido | Open (later product scope) | No acceptance link in checkbox |
| L494: Login falho | Open (later product scope) | No acceptance link in checkbox |
| L495: Falha de autorização | Open (later product scope) | No acceptance link in checkbox |
| L499: Eventos gerados automaticamente | Open (later product scope) | No acceptance link in checkbox |
| L509: Criação | Open (later product scope) | No acceptance link in checkbox |
| L510: Rotação | Open (later product scope) | No acceptance link in checkbox |
| L511: Revogação | Open (later product scope) | No acceptance link in checkbox |
| L512: Remoção | Open (later product scope) | No acceptance link in checkbox |
| L516: Todas as operações sensíveis auditadas | Open (later product scope) | No acceptance link in checkbox |
| L526: Remover credenciais | Open (later product scope) | No acceptance link in checkbox |
| L527: Remover plaintext | Open (later product scope) | No acceptance link in checkbox |
| L528: Remover material criptográfico | Open (later product scope) | No acceptance link in checkbox |
| L532: Logs livres de segredos | Open (later product scope) | No acceptance link in checkbox |
| L542: Todos os eventos possuem cobertura | Open (later product scope) | No acceptance link in checkbox |
| L560: Todos os eventos possuem hash encadeado | Open (later product scope) | No acceptance link in checkbox |
| L570: Histórico pode ser validado offline | Open (later product scope) | No acceptance link in checkbox |
| L580: Hashes podem ser comparados externamente | Open (later product scope) | No acceptance link in checkbox |
| L590: Modificação | Open (later product scope) | No acceptance link in checkbox |
| L591: Exclusão | Open (later product scope) | No acceptance link in checkbox |
| L592: Truncamento | Open (later product scope) | No acceptance link in checkbox |
| L593: Inserção | Open (later product scope) | No acceptance link in checkbox |
| L594: Reescrita | Open (later product scope) | No acceptance link in checkbox |
| L598: Todas as fraudes são detectadas | Open (later product scope) | No acceptance link in checkbox |
| L610: Limitar conexões simultâneas | Open (later product scope) | No acceptance link in checkbox |
| L611: Configurar fila de espera | Open (later product scope) | No acceptance link in checkbox |
| L621: Rate limiting | Open (later product scope) | No acceptance link in checkbox |
| L622: Bloqueio temporário | Open (later product scope) | No acceptance link in checkbox |
| L632: Respostas não expõem informações internas | Open (later product scope) | No acceptance link in checkbox |
| L642: CANIS_SECRET_KEY | Open (later product scope) | No acceptance link in checkbox |
| L643: Banco | Open (later product scope) | No acceptance link in checkbox |
| L644: Checksums | Open (later product scope) | No acceptance link in checkbox |
| L654: Sem perda de dados | Open (later product scope) | No acceptance link in checkbox |
| L664: Testes concorrentes | Open (later product scope) | No acceptance link in checkbox |
| L665: Testes de recuperação | Open (later product scope) | No acceptance link in checkbox |
| L677: Build multi-stage funcional | Open (later product scope) | No acceptance link in checkbox |
| L683: Processo principal não executa como root | Open (later product scope) | No acceptance link in checkbox |
| L689: Certificados | Open (later product scope) | No acceptance link in checkbox |
| L690: Truststore | Open (later product scope) | No acceptance link in checkbox |
| L691: CANIS_SECRET_KEY | Open (later product scope) | No acceptance link in checkbox |
| L692: Persistência | Open (later product scope) | No acceptance link in checkbox |
| L696: Nenhum segredo embarcado na imagem | Open (later product scope) | No acceptance link in checkbox |
| L702: Filesystem readonly | Open (later product scope) | No acceptance link in checkbox |
| L703: Menor privilégio possível | Open (later product scope) | No acceptance link in checkbox |
| L713: Ambiente sobe com um único comando | Open (later product scope) | No acceptance link in checkbox |
