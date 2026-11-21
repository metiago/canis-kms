# Repository cleanup report — 2026-11-20

The archive root is `C:\Users\TSIG\workspace\projects\ieee\one-drive`. Every archived
file retains its repository-relative path below that root. The published
`v0.1.0-rc.1` tag was not changed. This report describes a current-branch cleanup,
not a revision of the historical release.

## Archived Files

| Repository-relative file or exact set | Files | Destination and reason |
| --- | ---: | --- |
| `joss-backlog.md`, `app-prod-grade-backlog.md`, `joss-papers/submission-readiness.md` | 3 | Same paths under the archive root. Publication and product planning; the unresolved public-development-history question remains recorded in the retained JOSS validation and snapshot. |
| `docs/research/joss-backlog-triage.md`, `joss-final-pre-submission-audit.md` | 2 | Same paths under the archive root. Dated planning and readiness snapshots. |
| `docs/research/joss02-audit.md`, `joss02-candidate-release.md`, `joss02-claim-evidence.md`, `joss02-release-boundary.md`, `joss02-results.md` | 5 | Same paths under the archive root. Earlier JOSS-02 review and working-tree records; retained final audit, boundary, validation, and release record remain in the repository. |
| `docs/research/joss03-claim-evidence.md` | 1 | Same path under the archive root. Earlier reviewer matrix. |
| `docs/research/joss04-claim-evidence.md`, `joss04-method.md`, `joss04-results.md` | 3 | Same paths under the archive root. Earlier bibliography review; final paper validation covers the current bibliography. |
| `docs/research/joss05-claim-evidence.md`, `joss05-method.md`, `joss05-results.md` | 3 | Same paths under the archive root. Earlier manuscript and draft-render review; the current paper, builder, and final validation remain. |
| `docs/research/issue35-claim-matrix.md` | 1 | Same path under the archive root. Matrix for an earlier manuscript version. |
| `docs/research/joss05-draft.pdf`, `joss-final-render.log`, `issue34-pilot-artifacts.sha256` | 3 | Same paths under the archive root. Historical preview, local render log, and pilot manifest. |
| `docs/research/issue33-[A-E].*` | 28 | Same paths under the archive root. Preliminary A–E records, excluded from final acceptance counts. The `issue33-final-*` series remains. |
| `docs/research/issue34-pilot/*` | 61 | Same paths under the archive root. Failed attempt and pilot series, excluded from the final campaign. The pilot manifest moved with them. |
| `docs/research/code02-wrapper-final.*`, `code02-invalid-certificate-recheck.*`, `code02-invalid-certificate-verified.*` | 9 | Same paths under the archive root. Intermediate wrapper and failed classifier runs. The five corrected scenario sets remain. |
| **Total** | **119** | Each moved file was checked against its source SHA-256 after the move. |

Historical reports in the archive may contain links relative to their former
repository paths. Use the matching tagged Git revision for their original
context. The current repository indexes point to retained evidence and this
archive mapping.

## Deleted Files

| File | Exact copy at the time of this first cleanup |
| --- | --- |
| `docs/research/joss05-suite-report.csv` | `docs/research/joss02-suite-report.csv` (subsequently [archived in phase 2](repository-cleanup-phase2.md#archived-files)) |
| `docs/research/joss05-test-cases.csv` | `docs/research/joss02-test-cases.csv` (subsequently [archived in phase 2](repository-cleanup-phase2.md#archived-files)) |

The matching pairs had identical SHA-256 values. No retained documentation or
script referenced the duplicate names when they were deleted.
`.vscode/settings.json` was left in place: no second repository copy exists,
so the requested deletion precondition was not met.

## Updated References

The root README, documentation indexes, and retained research records now link
to retained evidence or to this archive mapping where they formerly linked to
moved files. Sixty Markdown links to archive candidates were removed or
redirected; links to the deleted duplicate CSV names were absent from retained
documents. Nineteen tracked files were edited for references and archive path
cleanup. The Windows paper preview builder writes future output under
`target/`; the historical draft PDF remains in the archive. Obsolete pilot
entries were removed from `.gitattributes`.

## Retained JOSS Evidence

The manuscript and bibliography; root README, license, contribution and
security guidance; reviewer guide and quickstart; final claim audit, release
boundary, candidate validation, JOSS-03 results, JOSS-06 audit, JOSS-07 snapshot,
final paper validation, release record, manuscript release record, release setup,
and release notes remain. Source, tests, Maven configuration, and CI/release
workflows remain. The final research evidence remains:

| Evidence | Retained material |
| --- | --- |
| JOSS-01; CODE-02; CODE-03; Issue #31 | Methods, results, runners, and final data or validation rows |
| Issue #32 | 438 raw/analysis artifacts, SHA-256 manifest, method, result, runner, analyzer, verifier |
| Issue #33 | 28 final artifacts, SHA-256 manifest, method, result, runner, verifier |
| Issue #34 | 159 final artifacts, SHA-256 manifest, method, result, runner, verifier |

## Validation Results

| Check | Result |
| --- | --- |
| Archive move | 119 original paths absent from the working tree; 119 corresponding destination files present. Each destination SHA-256 matched its source immediately after moving. |
| Retained-file check | All 19 specifically requested manuscript, policy, review, validation, and release files present. |
| Final research artifacts | Issue #32: 438; Issue #33 final: 28; Issue #34 final: 159. Their manifests and verifiers remain. |
| Artifact verification | `ISSUE32_EVIDENCE=passed pairs=24 file_rows=4160`; `ISSUE33_EVIDENCE=passed scenarios=5`; `ISSUE34_EVIDENCE=passed cases=7 repetitions=3 artifacts=159`. |
| Build and tests | `mvn --batch-mode --no-transfer-progress clean verify`: BUILD SUCCESS; 28 suites, 159 tests, zero failures, zero errors, eight documented skips. |
| Paper-preview script | PowerShell parser reported zero syntax errors. The external Inara render was not rerun as part of this cleanup. |
| Link scan | 651 local Markdown links across 49 retained Markdown files; zero missing targets. An older link to an ignored `server/target/` output was changed to plain text during this scan. |
| Git review | `git status` shows 119 archived paths and two duplicate CSVs as deletions, 19 tracked reference/path edits, one new report, and the retained `.vscode/settings.json`. `git diff --check` passed. |
| Published release | `v0.1.0-rc.1` still resolves to `e1d6998b5c5658750870b39e2a4d511de5bed7f3`. No tag or release asset was modified. |

The current paper PDF remains a pre-submission preview with placeholder JOSS
journal metadata. The public-development-history screening question remains
unresolved. Moving internal planning to the local archive does not establish
that requirement. Historical reports in OneDrive are not directly accessible
to public reviewers; the retained current-branch records and fixed release tag
carry the claims and reproducibility routes used for JOSS review.
