# JOSS-02 fixed candidate release

## Selection

| Field | Value |
| --- | --- |
| Candidate revision | `cd1ebeb32bbd0b6b2ebd42a29f72e3dac8b01694` |
| Branch at selection | `master` (local branch, two commits ahead of `origin/master`) |
| Commit date | 2026-11-18 20:46:48 -0300 |
| Selection and validation date | 2026-11-18, America/Sao_Paulo |
| Working tree at selection and during smoke runs | Clean; all three harness JSON records say `dirty_tree: false` and name the candidate revision |

This is a **candidate source and manuscript revision**, identified by its full Git commit. It is not a published tag or a claim of independent release reproduction. The commit was selected because it is the latest clean revision containing the current [manuscript](../../joss-papers/paper.md), author-approved [JOSS-06 disclosures](joss06-audit.md), CODE-01 security changes, the research harnesses, and retained study artifacts. The preceding [JOSS-02 audit](joss02-audit.md) reviewed an earlier snapshot but did not test a fixed candidate. [Validation](joss02-validation.md) records the exact-revision execution; [final claim audit](joss02-final-claim-audit.md) reconciles the manuscript at this commit.

## Release scope and included evidence

The supported software boundary is the Java 21 client and server, official TLS 1.3/mTLS CANISP path, `CANISHYB1` client-side file envelope, encrypted `CANISDB1` persistence, and the documented credential-level shared-trust model. The Issue #32 non-TLS loopback comparator is research tooling, not a supported product transport. Historical study results are included **as records with their original execution provenance**, not as measurements newly made on this candidate. See the [final release boundary](joss02-release-boundary-final.md).

| Item | Status |
| --- | --- |
| JOSS-01 | Included; one shared-certificate, credential-level boundary test passed again on this candidate. Historical significance and predecessor claims retain their source limits. |
| CODE-01 | Implementation included; fixed-revision Maven security and compatibility tests passed. Filesystem crash durability, path races, and POSIX behavior remain limited. |
| CODE-02 | Harness and prior clean Ubuntu smoke included; one normal separate-process exchange passed on this candidate. Historical Windows negatives retain their original provenance. |
| CODE-03 | Bounded Windows memory preflight and matching retained CSV hashes included; no campaign rerun or general size limit. |
| Issue #31 | Absolute Windows CANIS baseline included as a dirty-tree historical observation; original local controls remain unexecuted. |
| Issue #32 | Clean fixed-revision Windows paired transport study and verified retained artifacts included; its loopback comparator is research-only. No campaign rerun. |
| Issue #33 | Five dirty-tree historical functional scenarios and verified artifacts included; candidate smoke repeated scenario D only. |
| Issue #34 | Twenty-one dirty-tree historical storage/audit runs and verified artifacts included; candidate smoke repeated scenario E only. |
| Issue #35 | Bounded synthesis and claim matrix included; its broader original-question acceptance remains open because local controls and service-outage windows were not measured. |
| JOSS-06 | Author-approved metadata and disclosures included in the committed manuscript and audit; actual submission date is deferred to submission. |

## Known limitations

- This candidate is tested locally on Windows 11. The full Maven run skipped five tests needing a separately started integration server and three POSIX-specific cases. Earlier Ubuntu CI evidence belongs to an earlier commit.
- The original local-control comparison, scheduled service-outage/availability windows, larger or distributed workloads, and an independent non-author rerun of this candidate are absent. No new campaign is inferred from the four smoke checks.
- The database replacement uses two moves; interrupted-write durability and hostile path races are not established. The running server caches the master key. Tested recovery required an exact matching synthetic backup.
- An admitted client can recover under any stored key name. There is no per-key authorization, managed key rotation, producer identity in the envelope, or built-in audit-integrity verifier.
- The original predecessor letter is not a tracked publication artifact and does not establish deployment or adoption of this revision. The historical author account is qualified in the paper.
- No release tag, public archive, canonical JOSS paper build, or JOSS-03 independent candidate reproduction is established by this JOSS-02 validation. These are separate submission gates.
