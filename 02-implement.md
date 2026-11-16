Read AGENTS.md, joss-backlog.md, joss-papers/paper.md, and joss-papers/submission-readiness.md.
Implement the previously approved plan for the specified JOSS backlog issue.
Follow all applicable project constraints and security requirements defined in AGENTS.md.

Requirements:
- Verify the current repository state before making changes.
- Implement only the approved scope.
- Preserve security and compatibility requirements.
- Create reproducible research artifacts where applicable.
- Include scripts, configuration, fixtures, and execution instructions needed to reproduce experiments.
- Run tests and experiments rather than assuming they succeed.
- Record actual results, environment details, and limitations.
- Update the manuscript only with verified implementation facts and research evidence.
- Verify citations against reliable primary sources.
- Do not fabricate research impact, adoption, results, or references.
- Update joss-backlog.md and joss-papers/submission-readiness.md based on verified completion.

Definition of Done:
- Set a checklist item to [x] only if evidence exists in the repository.
- Cite the exact file(s), test(s), script(s), or artifact(s) supporting completion.
- If evidence is partial, leave the item unchecked and explain what remains.
- Never infer completion from intent, design notes, TODO comments, or roadmap statements.

Claim-Evidence Matrix:
Before modifying paper.md, create a table:
| Claim | Evidence | Source File | Verification Status |

For every technical, security, research, or impact claim:
- verify it
- revise it
- or remove it

Experiment requirements:
Every experiment must include:
- exact command
- prerequisites
- generated fixtures
- expected output
- cleanup procedure
- platform-specific limitations

Performance safeguards:
Do not produce latency, throughput, scalability, reliability, security-strength, or adoption conclusions unless those metrics were explicitly measured and reported.

Traceability:
Map every modified artifact back to backlog tasks.
Create a table:
| Backlog Task | Evidence | Status |

After implementation, produce:
- Summary of completed work.
- Files created or modified.
- Research or publication contribution.
- Tests and experiments executed.
- Reproducibility instructions.
- Manuscript and documentation changes.
- Remaining risks or submission blockers.
- Acceptance criteria satisfied and outstanding.

Do not mark an issue complete unless its acceptance criteria are supported by verifiable evidence.
