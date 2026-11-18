# JOSS-02 research-release audit

## Decision and scope

**JOSS-02 remains open.** This audit reconciles the [current manuscript](../../joss-papers/paper.md) with code, tests, completed study records, and retained artifacts. The complete [claim-by-claim release boundary](joss02-release-boundary.md#claim-by-claim-release-boundary) gives each material paper claim an evidence link, execution/artifact revision, and reproduction route. This work did not run a test, execute a research workload, generate a measurement, or create release evidence.

The clean source snapshot inspected before these documentation changes was `5632a8fa563f01a1c03f5a1632cff80165c6c543` (`B`). No release tag or JOSS-02 test report for `B` is recorded. The CODE-01 implementation commit `6f0099ad8112fe4c582da39d4305a5bc5a6eb987` is an ancestor of `B`. Production client/server source is unchanged from evidence-artifact commit `221df74231891f0910bd8a2f59ec392d3ae67376` to `B`, but the paper has changed. The present paper and two JOSS-02 documents are working-tree drafts until committed. Existing [JOSS-02 results](joss02-results.md) are from an earlier dirty tree before CODE-01; they are historical, not a verification of `B`.

The earlier [Issue #35 claim matrix](issue35-claim-matrix.md) maps the pre-JOSS-02 paper to repository artifacts. Its statement that the revised Issue #35 paper and matrix were uncommitted described that earlier review. Both were committed by `B`; this audit supersedes that status statement for JOSS-02 without rewriting the historical record.

## Architecture and behavior review

| Area | Repository evidence | Finding and boundary |
| --- | --- | --- |
| TLS-only official workflow | [Client socket](../../client/src/main/java/io/canis/jpaw/client/SocketClient.java), [client policy](../../client/src/main/java/io/canis/jpaw/config/TlsCipherPolicy.java), [server](../../server/src/main/java/io/canis/Server.java), [server policy](../../server/src/main/java/io/canis/config/TlsCipherPolicy.java), [client tests](../../client/src/test/java/io/canis/jpaw/client/SocketClientTest.java), [server tests](../../server/src/test/java/io/canis/ServerTlsSecurityTest.java) | The official path enables TLS 1.3 suites; the client starts a TLS handshake before sending credentials, and no plaintext fallback is coded. Older tests cover plaintext and TLS 1.2 rejection. No exact-`B` run is recorded. |
| mTLS and hostname | [Client context](../../client/src/main/java/io/canis/jpaw/config/TlsContextFactory.java), [server context](../../server/src/main/java/io/canis/config/TlsContextFactory.java), [server](../../server/src/main/java/io/canis/Server.java), [client socket](../../client/src/main/java/io/canis/jpaw/client/SocketClient.java) | Both contexts use PKIX trust material loaded externally. The server calls `setNeedClientAuth(true)`; the client sets HTTPS endpoint identification before handshake. Earlier tests cover untrusted/expired/wrong-usage certificates and hostname mismatch. The production server's wildcard bind is an operational exposure to control outside this code. |
| Client file behavior | [Client](../../client/src/main/java/io/canis/jpaw/client/JpawClient.java), [envelope helper](../../client/src/main/java/io/canis/jpaw/utils/EnvelopeCryptographer.java), [end-to-end test](../../server/src/test/java/io/canis/ServerEndToEndTest.java), [CODE-02](code02-results.md) | AES-GCM encrypt/decrypt occurs locally; CANIS returns the unwrapped data key, not bulk file bytes. Whole-file reads and copies limit the tested workload. The envelope carries a GCM tag but does not authenticate a named producer to another admitted client. |
| Authorization and private-key boundary | [Handler](../../server/src/main/java/io/canis/handlers/ClientHandler.java), [store](../../server/src/main/java/io/canis/store/KeyValueStore.java), [public metadata](../../server/src/main/java/io/canis/store/Entry.java), [JOSS-01](joss01-results.md#observed-evaluation), [Issue #33 D](issue33-results.md#actual-scenario-observations) | TLS certificate and configured credentials gate commands. No per-key access check binds a caller to a key name. `GET` and `LIST` serialize public metadata, not stored RSA private material; decrypt returns a recovered data key. JOSS-01 observed credential-level shared access with one certificate; #33 observed it with a separately certificated observer. This is the chosen shared-trust policy and its blast radius. |
| Key lifecycle | [Store create/delete/decrypt](../../server/src/main/java/io/canis/store/KeyValueStore.java), [restart/deletion test](../../server/src/test/java/io/canis/ServerEndToEndTest.java) | Create generates an RSA pair, and delete removes a stored pair. There is no managed rotation or mechanism to revoke data keys that clients already recovered. No generalized key-lifecycle assurance is claimed. |
| Persistence and startup | [Store](../../server/src/main/java/io/canis/store/KeyValueStore.java), [master-key loader](../../server/src/main/java/io/canis/crypto/MasterKeyLoader.java), [CODE-01 record](../code01-integrity.md), [replacement tests](../../server/src/test/java/io/canis/store/DatabaseReplacementTest.java) | `CANISDB1` is AES-GCM encrypted with externally loaded key material. Loading checks record shape and RSA pair consistency; missing/invalid state rejects startup. Initialization is explicit. A write uses a recovery marker and two moves, not one transaction. CODE-01 tests preceded its implementation commit; directory durability and hostile path replacement remain limits. |
| Audit | [Audit logger](../../server/src/main/java/io/canis/audit/AuditLogger.java), [Issue #34 D](issue34-results.md#scenario-observations), [AGENTS.md](../../AGENTS.md#audit-requirements) | Key-value events identify operations and identities, but application events lack required event IDs and an integrity verifier. In the selected #34 audit-line changes, external hashes changed while the tested workflow continued. The paper now states this absence explicitly. |
| Recovery | [Store recovery path](../../server/src/main/java/io/canis/store/KeyValueStore.java), [CODE-01 limits](../code01-integrity.md#operational-and-compatibility-limits), [Issue #33 C](issue33-results.md#actual-scenario-observations), [Issue #34 A/B/C/E](issue34-results.md#scenario-observations) | Controlled intact-database restart worked in #33. Selected damaged startup states failed in #34 and exact matching fixture restoration worked. The master key is cached while running; file loss/replacement did not stop tested live requests. No automatic recovery, interrupted-write crash recovery, or availability-window result exists. |

## Research evidence review

| Study | Supported paper statement | Execution qualification |
| --- | --- | --- |
| [JOSS-01](joss01-results.md#focused-rerun-on-2026-11-17) | Recipient and observer credentials recovered identical synthetic bytes; altered observer password failed. | Focused one-test rerun at `0e9c889` with unrelated edits; one client certificate shared among credential identities. |
| [CODE-02](code02-results.md#ubuntu-ci-reproduction-on-2026-11-17) | Separate-process delayed normal exchange; selected local negative rejection. | Clean Ubuntu CI normal smoke at `23a6583`; Windows negative runs were local dirty-tree observations. CI archive contents were not independently inspected in the recorded review. |
| [CODE-03](code03-results.md#raw-observations) | Windows preflight through 4 MiB plus one 100-file schedule check. | Dirty research source at `0be60db`; not a general file-size maximum. |
| [Issue #31](issue31-baseline-results.md#result-and-boundary) | Absolute CANIS path verified 2,000 measured synthetic files over 20 runs. | Dirty research source at `f00310e`; original local AES-GCM/local-envelope controls absent. |
| [Issue #32](issue32-results.md#result-and-scope) | Clean fixed-revision paired TLS-path and research loopback comparator: 2,000 files per arm, 20 measured pairs, bounded latency/throughput/memory descriptions. | Clean fixed `9b01048`, one Windows host. Comparator includes admission/adapter differences, so the paired delta is not pure TLS cost. |
| [Issue #33](issue33-results.md#actual-scenario-observations) | Five local functional scenarios: delay, routes, intact restart, shared trust, changed-tag rejection. | Dirty research source at `d3e89d7`; no independent/POSIX final-series run. |
| [Issue #34](issue34-results.md#scenario-observations) | 21 selected storage/audit fault runs: damaged-state rejection, exact restore, external audit-hash changes without application rejection. | Dirty research source at `4f6de05`; no planned outage series, interrupted-write test, or general availability estimate. |
| [Issue #35](issue35-results.md) | Bounded synthesis and claim traceability. | Retrospective document; no new experiment. Its fixed artifact revision does not fix dirty executions. |

The [release-boundary provenance table](joss02-release-boundary.md#evidence-and-execution-revisions) distinguishes fixed revisions, dirty trees, local observations, and the one clean Ubuntu CI smoke. The #32/#33/#34 manifest files list 438, 28, and 159 retained artifacts respectively; the [Issue #35 synthesis](issue35-results.md#existing-artifact-verification-performed-for-this-synthesis) records prior structural-verifier passes. This audit did not rerun those verifiers or any workload.

### Fixed-revision and reproduction classification

| Evidence | Fixed revision at execution | Dirty working tree at execution | Locally observed only | Independently reproduced by a non-author |
| --- | --- | --- | --- | --- |
| CODE-01 integrity checks | No; changes were committed later as `6f0099a` | Yes | Yes, Windows | No record |
| JOSS-01 focused rerun | Source commit recorded, but unrelated edits existed | Yes | Yes, Windows | No record |
| Earlier JOSS-02 suite | No | Yes | Yes, Windows | No record |
| CODE-02 normal Ubuntu CI smoke | Yes, clean checkout `23a6583` | No | No; CI job on Ubuntu | No non-author record; CI archive contents were not independently inspected in the cited review |
| CODE-02 Windows negative cases | No | Yes | Yes, Windows | No record |
| CODE-03 preflight | No | Yes | Yes, Windows | No record |
| Issue #31 baseline | No | Yes | Yes, Windows | No record |
| Issue #32 paired study | Yes, clean `9b01048` | No | Yes, Windows host | No record |
| Issue #33 functional series | No | Yes | Yes, Windows | No record |
| Issue #34 selected faults | No | Yes | Yes, Windows | No record |
| Issue #35 synthesis | Fixed repository artifact, no study execution | Not an execution | Retrospective document | Not applicable |

“Independently reproducible” here means a recorded successful non-author rerun of the candidate, not merely that scripts and instructions exist. No such rerun is in the repository. The successful CODE-02 Ubuntu job is clean-checkout automation, a narrower result.

## Manuscript consistency review

| Paper Section | Status | Required Action |
| --- | --- | --- |
| YAML title/tags | Supported by source and scope | Retain; recheck on the final fixed paper. |
| Summary | Reconciled with bounded records; no general availability result claimed | Retain current qualifiers and the separate CI/dirty-run provenance. |
| Statement of need | Historical motivation is attributed; original letter is not a repository publication artifact | Keep attribution and no current-code deployment inference; obtain author decision on an inspectable/redacted source separately. |
| State of the field | Standards and product documentation are cited; no comparative experiment is claimed | Retain the absence-of-advantage qualifier; canonical citation build remains separate. |
| Software design | Source-level workflow is supported; recovery, producer identity, key lifecycle, and audit limitations needed sharper wording | **Revised in this JOSS-02 draft** with code and experiment links; exact-candidate behavior still needs tests. |
| Research impact statement | Counts and selected observations match study records and artifact scopes | Retain dirty-tree, Windows, synthetic, comparator, and unmeasured-condition qualifiers; do not pool studies. |
| AI usage disclosure | Draft scope is recorded, complete historical use and author approval unconfirmed | Human authors must validate tools, versions, scope, and wording before final submission. |
| Acknowledgements and YAML authors | Placeholder names/affiliation; support information not established | Complete only from author-provided facts. |
| References | Four keys in the bibliography; earlier adapted draft rendered references | Run the canonical build on the eventual fixed paper under JOSS-04/05. |

### Unsupported or overbroad claims handled

No unsupported benchmark, availability, production-readiness, current-code adoption, or comparative-superiority claim appears in the revised paper. The following previously implicit capabilities are now explicitly bounded in [Software design](../../joss-papers/paper.md#software-design): producer identity is not established by the envelope; the running server caches the master key; observed restoration was manual and exact-copy; key creation/deletion is not managed rotation or revocation of a recovered data key; and audit events do not meet the project's event-ID/integrity requirement. The paper still attributes the predecessor history to the JOSS-01 record because the underlying letter is unavailable as repository evidence. The draft AI disclosure and metadata remain expressly unresolved. No outcome was invented or promoted from an unexecuted protocol step.

## Remaining release blockers

1. Fix a candidate commit containing final source, paper, and audits; retain its tag or immutable revision. The current working tree is not that candidate.
2. Run the full JOSS-02 and focused JOSS-01 checks on that exact revision, including official file workflow, TLS/mTLS/hostname and credential rejection, shared-access behavior, malformed storage, missing database, tampering, compatibility, and reported skips. Record OS, JDK/Maven versions, commands, counts, and sanitized outcomes. The [JOSS-02 method](joss02-method.md) gives commands; this audit did not execute them.
3. Make the research-release decision for CODE-01/app Issues #7, #8, and #36: acknowledge the implemented fixes, rerun the fixed code, and explicitly accept or constrain filesystem durability/path-control and shared-access blast radius. Do not claim per-key isolation or integrity-verifiable audit.
4. Keep separate JOSS submission gates open for independent candidate reproduction, public snapshot, canonical manuscript build, author metadata, and disclosure approval. Historical dirty-tree runs cannot be reconstructed exactly from their Git HEAD alone.

## Acceptance review

| Requirement | Evidence | Status |
| --- | --- | --- |
| Every material paper claim linked to code, test, study, or artifact with revision and reproduction route | [Claim table](joss02-release-boundary.md#claim-by-claim-release-boundary) and manuscript review above | Complete as a repository evidence map; release execution open |
| Unsupported claims revised, qualified, or removed | [Paper Software design](../../joss-papers/paper.md#software-design) adds the five operational/security boundaries above; historical/disclosure limits remain attributed | Complete for this draft; author approval open |
| TLS, mTLS, client, authorization, persistence, audit, and recovery verified against repository source | Architecture table above and cited source/tests | Complete as static review; exact-candidate behavioral verification open |
| Research observations separated by fixed, dirty, local, and clean-checkout evidence | [Provenance table](joss02-release-boundary.md#evidence-and-execution-revisions), study table above | Complete as a historical classification; independent candidate reproduction absent |
| Release-boundary document with required four-column table | [Release boundary](joss02-release-boundary.md#claim-by-claim-release-boundary) | Complete as a draft audit |
| Manuscript consistency table | Table above | Complete as a draft audit |
| Update JOSS-02 backlog status using only repository evidence | [JOSS-02 backlog](../../joss-backlog.md#joss-02--freeze-and-verify-the-research-release-boundary-p0) retains unchecked candidate requirements and links this audit | Partial gate; no completion claimed |
| JOSS-02 acceptance: all material claims match the **tested fixed release**, with no known contradiction | Paper/source reconciliation exists, but no full test record for the final fixed candidate | **Open** |
