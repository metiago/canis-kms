# Open-item triage for the former unified backlog

Snapshot: pre-refactor joss-backlog.md at working HEAD 09aa7b51689dc8dff10bb6ba8862cc2f0931685e (2026-11-19). Each row corresponds to one unchecked checkbox in that file; line numbers identify the original item. Mixed items are explained in the justification.

JOSS sources: [submission criteria](https://joss.readthedocs.io/en/latest/submitting.html), [review criteria](https://joss.readthedocs.io/en/latest/review_criteria.html), [review checklist](https://joss.readthedocs.io/en/latest/review_checklist.html), and [paper format](https://joss.readthedocs.io/en/latest/paper.html). Candidate evidence: [JOSS-07 snapshot](joss07-snapshot.md), [independent review](joss03-results.md#independent-non-author-review-on-2026-11-19), and [release record](release-candidate.md).

| Item | Required by JOSS | Helpful but Optional | Not Relevant | Justification |
| --- | --- | --- | --- | --- |
| L11 — **Phase 0 — Research fit:** Authors validate the question and provide inspectable actual research-use evidence. | Yes |  |  | Current JOSS submission, review, or paper criteria address this outcome. |
| L12 — **Phase 1 — Safe research build:** Security prerequisites and a repeatable, isolated exchange harness are verified. |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L13 — **Phase 2 — Empirical study:** Predefined workloads, experiments, and reproducible results answer the research question. |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L14 — **Phase 3 — JOSS candidate:** A fixed, independently reproducible release and complete manuscript meet submission gates. | Yes |  |  | Current JOSS submission, review, or paper criteria address this outcome. |
| L15 — **Phase 4 — Product hardening:** Remaining operational, audit, runtime, and deployment issues meet their acceptance criteria. |  |  | Yes | The present JOSS paper does not claim this unperformed research or deployment capability. |
| L39 — **Partially satisfied — actual research use:** The executed JOSS-01 boundary experiment documents narrow developer research use of CANIS; late… | Yes |  |  | Research use is a screening gate; completing the entire original Phase 2 study is not. |
| L40 — **Partially satisfied — supportable claims:** The synthetic method and results support only the observed sh… | Yes |  |  | Every paper claim must match evidence and stated limits. |
| L54 — Rerun the security, compatibility, and packaged-startup checks against the exact committed revision. Run the skipped POSIX permission and syml… |  | Yes |  | The exact-tag suite and POSIX gate already passed; repeating this older CODE-01 gate adds no required result. |
| L128 — Run POSIX permission and symlink tests on POSIX CI; add owner-mismatch and inherited Windows ACL cases. |  |  | Yes | Product security follow-up; the research candidate already scopes tested behavior and limits. |
| L164 — Validate loaded `CANISDB1` public and private key encodings and that they form a matching RSA |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L167 — Remove the unused `privateKey` field and Java serialization capability from the client |  |  | Yes | Product security follow-up; the research candidate already scopes tested behavior and limits. |
| L169 — Confirm that `\|decrypt` is available only after TLS client-certificate and credential |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L171 — Record the cross-service blast radius and audit the requesting identity and key name without |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L174 — Add tests for a private-key encoding placed in a stored public-key field, malformed legacy |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L178 — Document startup failure behavior and compatibility impact for existing databases and |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L183 — No private-key material is returned through Java APIs, serialization, logs, exceptions, or |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L185 — The shared-access policy is enforced only after authentication and is tested and documented; |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L187 — Valid existing `CANISDB1` databases and authorized file workflows remain usable after restart. |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L223 — Use isolated fixtures to exercise CANIS unavailable before public-key fetch, during or before data-key recovery, and after restart; key deleti… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L224 — For each scenario, predefine and report whether encryption/decryption succeeds, whether plaintext is released, time to detect failure, and rec… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L225 — Add storage corruption, interrupted writes, audit tampering, or longer outages only where they answer the research question. Do not claim tamp… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L239 — Publish methods, actual results, analysis, negative results, discussion, and limitations under `docs/research/`; separate observed behavior fr… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L240 — Provide exact commands, non-secret fixtures, expected outputs, cleanup, revision, environment, platform limits, and artifact hashes for each e… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L241 — Revise the proposed research question if the collected evidence cannot answer it; avoid claims of general reliability or security from a singl… |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L251 — **Gate A — Research fit (P0):** Substantiate the question, audience, actual research use, and distinct need beyond existing KMS or envelope-en… | Yes |  |  | Research impact and scholarly need are screening criteria. |
| L253 — **Gate C — Reproducibility (P0):** A non-author can install, test, run the separate-process example, and reproduce at least one meaningful eva… |  | Yes |  | Reviewer installability matters; a separate non-author rerun of every study is not prescribed and the clean checkout workflow was reviewed. |
| L254 — **Gate D — Submission artifacts (P1):** Complete manuscript, references, author metadata, AI disclosure, and public candidate snapshot. Plan a… | Yes |  |  | Paper, disclosures, and public source are submission requirements; DOI follows successful review. |
| L274 — Verify the documented Java 21/Maven build and external TLS/master-key provisioning from a clean checkout, including non-secret fixtures and an… | Yes |  |  | JOSS checks installation and functionality; the non-author clean-checkout build and fixture workflow already passed. The exact TLS fixture method is project-specific. |
| L276 — Verify public license, issue and contribution routes, a stable private security-reporting route, support expectations, and the required active… | Yes |  |  | License, issue/contribution/support paths and public history are required; a private vulnerability route is project-specific. |
| L284 — Build the final paper on the fixed candidate with the canonical JOSS workflow; confirm all citations and references resolve and metadata rende… |  | Yes |  | Rendering/citation correctness matters; the exact local Inara command is one verification method. |
| L290 — Reframe the statement of need, state of the field, software design, and research impact statement around the validated Phase 2 question and ac… | Yes |  |  | The paper needs JOSS sections and credible significance; completing the original Phase 2 question is not required. |
| L291 — Check final JOSS sections, word count, formatting, references, and every technical or impact claim against the candidate revision. Keep observ… | Yes |  |  | Current JOSS submission, review, or paper criteria address this outcome. |
| L300 — Set the actual submission date only when submitting. | Yes |  |  | JOSS paper metadata specifies a date format; set the actual date at submission. |
| L315 — Have a colleague who was not the primary developer install the candidate, run the example, and reproduce the documented evaluation. |  | Yes |  | A non-author already ran the same executable tree; rerunning the exact tag is helpful provenance, not an explicit rule. |
| L316 — Fix and tag a public candidate; prepare a permanent archive and citation plan. After successful JOSS review, archive the reviewed version and … | Yes |  |  | A public tag/release exists; final archive DOI is required after successful review, not before submission. |
| L317 — Run all project tests and the canonical JOSS paper build on the exact candidate commit; retain sanitized reports, environment details, and art… |  | Yes |  | Exact-tag Maven tests passed; canonical local rendering is useful verification rather than a mandated command. |
| L318 — Verify repository public age, active development, actual research use, licensing, issue access, support paths, and other current JOSS screenin… | Yes |  |  | Public history, research use, license, issues, and open practices are screening criteria. |
| L319 — Reconcile every submission task and evidence link in this single backlog before fixing the candidate release. |  | Yes |  | Useful research or verification, but the current paper can state its bounded evidence and limitations without this additional work. |
| L337 — Check that every bibliography entry is cited and every citation resolves; compile the manuscript with the JOSS toolchain. The [JOSS-05 static … | Yes |  |  | Citations and rendered references must be correct; a specific local toolchain is not mandated. |
| L353 — Update the paper and deployment documentation to describe only the verified release behavior. | Yes |  |  | Paper statements must match supported release behavior. |
| L361 — Complete and document developer research use in the planned Issues #31–35 study, including context, software revision, methods, results, and r… |  | Yes |  | Developer research use is required; the full planned Issues #31–35 study is not. |
| L391 — Choose and establish the final JOSS paper directory/layout, including `paper.md`, `paper.bib`, and any figures, without breaking repository co… | Yes |  |  | Paper and bibliography must accompany source in a Git repository; layout already exists. |
| L393 — Confirm the submission points to a public, browsable repository with an OSI-approved license, accessible issue tracker, and release/archive su… | Yes |  |  | Public source, OSI license, and accessible issue tracker are explicit submission requirements. |
| L395 — Review development history, releases, tests, installation instructions, usage examples, contribution guidance, and support/reporting paths aga… | Yes |  |  | Review criteria include development history, documentation, tests, contribution and support paths. |
| L397 — Confirm a colleague who was not the primary developer can install and exercise the documented workflow. |  | Yes |  | Independent trial is recommended for new software; the reviewer workflow was completed on the same executable tree. |
| L399 — Run the project verification suite and the JOSS paper build on the exact candidate revision; retain the logs/artifacts. |  | Yes |  | Software verification is required in substance; exact-tag tests passed and a specific local paper build is not mandated. |
| L401 — Check manuscript word count, section headings, citations, author metadata, AI disclosure, and all statements against that candidate release. | Yes |  |  | Final paper metadata, references, sections, and claims must be correct. |
| L403 — Record the actual submission date only when submitting; do not use a placeholder or future date. | Yes |  |  | Set the submission date when submitting. |
| L426 — Keep the client and server TLS cipher-suite policies aligned when either policy changes; consider a shared policy or cross-module check. |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L427 — Consider a shared TLS test fixture module to reduce duplicated certificate-authority helpers. |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L437 — Rotação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L438 — Revogação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L439 — Auditoria correspondente |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L443 — Chaves podem ser revogadas sem perda histórica |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L453 — Backup completo |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L454 — Restore completo |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L455 — Restore após falha |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L456 — Restore após corrupção parcial |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L460 — Dados permanecem descriptografáveis |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L472 — eventId |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L473 — sequenceNumber |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L474 — timestamp |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L475 — identity |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L476 — operation |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L477 — resourceId |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L478 — keyId |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L479 — result |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L483 — Esquema documentado |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L493 — Login bem sucedido |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L494 — Login falho |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L495 — Falha de autorização |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L499 — Eventos gerados automaticamente |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L509 — Criação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L510 — Rotação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L511 — Revogação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L512 — Remoção |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L516 — Todas as operações sensíveis auditadas |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L526 — Remover credenciais |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L527 — Remover plaintext |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L528 — Remover material criptográfico |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L532 — Logs livres de segredos |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L542 — Todos os eventos possuem cobertura |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L560 — Todos os eventos possuem hash encadeado |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L570 — Histórico pode ser validado offline |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L580 — Hashes podem ser comparados externamente |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L590 — Modificação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L591 — Exclusão |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L592 — Truncamento |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L593 — Inserção |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L594 — Reescrita |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L598 — Todas as fraudes são detectadas |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L610 — Limitar conexões simultâneas |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L611 — Configurar fila de espera |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L621 — Rate limiting |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L622 — Bloqueio temporário |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L632 — Respostas não expõem informações internas |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L642 — CANIS_SECRET_KEY |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L643 — Banco |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L644 — Checksums |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L654 — Sem perda de dados |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L664 — Testes concorrentes |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L665 — Testes de recuperação |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L677 — Build multi-stage funcional |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L683 — Processo principal não executa como root |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L689 — Certificados |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L690 — Truststore |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L691 — CANIS_SECRET_KEY |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L692 — Persistência |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L696 — Nenhum segredo embarcado na imagem |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L702 — Filesystem readonly |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L703 — Menor privilégio possível |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |
| L713 — Ambiente sobe com um único comando |  |  | Yes | Post-publication product capability or operations; no present paper claim depends on completing it. |

Items classified as required may already be satisfied; the minimal publication backlog records only outstanding outcomes. Optional and product tasks retain their original detail in the research protocols/results and [product backlog](../../app-prod-grade-backlog.md).
