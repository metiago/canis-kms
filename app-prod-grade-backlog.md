# CANIS-KMS product backlog

Detailed product tasks retained from the former unified backlog. These are not blanket JOSS submission prerequisites; the published candidate's supported behavior and limits are assessed separately in the [JOSS publication backlog](joss-backlog.md).

## Product security prerequisites — Issues #7, #8, and #36

The research-release subset of these findings was scoped in the [JOSS-02 final claim audit](docs/research/joss02-final-claim-audit.md). Remaining tasks below are product follow-ups.

### Issue #7 - Protect CANIS_SECRET_KEY

**CODE-01 status:** The local Windows constructor and master-key tests pass; the remaining platform and path-control checks below stay open. See [CODE-01 evidence](docs/code01-integrity.md).

#### Pending work

- [x] Reconcile the earlier public-constructor finding with `Server.verifyConfiguration`; `ServerTest` now covers missing, malformed, mismatched, and changed master-key configurations before binding.
- [x] Review ancestor-directory permissions and path-replacement resistance; [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build) require trusted ancestor control and state the remaining race limit.
- [x] Record the decision for runtime loss of the cached master-key file: the running process does not alert automatically, so operators must monitor the file and detect loss before restart. See [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build).
- [x] Add a sanitized master-key log assertion in `MasterKeyLoaderTest`.
- [ ] Run POSIX permission and symlink tests on POSIX CI; add owner-mismatch and inherited Windows ACL cases.

#### Acceptance criteria

- [x] No supported server construction path starts with an invalid master-key configuration in the tested Windows paths; [constructor tests and packaged smoke evidence](docs/code01-integrity.md#acceptance-evidence) support this. POSIX and ACL coverage remain open above.

### Issue #8 - Prevent Silent Database Recreation

**CODE-01 status:** Local Windows validation and write-path tests pass; filesystem crash durability and platform checks remain release limits. See [CODE-01 evidence](docs/code01-integrity.md).

#### Pending work

- [x] Reject authenticated database records with invalid RSA fields or malformed UTF-8 without rewriting the database; [storage tests](docs/code01-integrity.md#acceptance-evidence) coordinate this with Issue #36.
- [x] Close the check-to-move recreation path in `KeyValueStore.saveToFile` by moving the verified old database to recovery before installing the staged file. The [operational limits](docs/code01-integrity.md#operational-and-compatibility-limits) describe the two-move interruption window.
- [x] Add deterministic tests for deletion after verification, interrupted and unsupported atomic moves, symlink rejection, and storage-failure log sanitization. The symlink case was skipped on this Windows host and remains in the POSIX CI gate.
- [x] Exercise the packaged `--init-db` command end to end: the first call created a database, the second failed without changing its hash, and normal startup rejected a missing database. See [packaged smoke evidence](docs/code01-integrity.md#acceptance-evidence).
- [x] Review idle-time database-loss detection, directory metadata durability, and the cost of reading and hashing large databases; [master-key operations](docs/master-key.md#operational-limits-for-the-code-01-build) state the unresolved deployment limits.

#### Acceptance criteria

- [x] Corrupted or malformed stored records fail safely in the [local storage tests](docs/code01-integrity.md#acceptance-evidence).
- [x] No tested write path silently recreates a missing cryptographic database; [deletion-boundary and recovery tests](docs/code01-integrity.md#acceptance-evidence) support this under the documented trusted-directory assumption. Cross-filesystem crash behavior remains open.

### Issue #36 - Close Private-Key Exposure Review Findings

**Status:** Open. Follow-up to Issue #6 and its implementation review.

#### Objective

Prevent private-key material from reaching public APIs or CANISP responses through malformed but
decryptable stored records. Preserve and document the current shared trust domain: every authenticated
service may request decryption under any stored key name. Per-key access control would be a separate
policy change and requires its own design, migration, and compatibility decision.

#### Tasks

- [ ] Validate loaded `CANISDB1` public and private key encodings and that they form a matching RSA
      pair before exposing metadata or serving requests. Fail closed on invalid records without
      replacing or rewriting the database.
- [ ] Remove the unused `privateKey` field and Java serialization capability from the client
      artifact's `io.canis.jpaw.pojo.Entry`, or remove that type if it has no supported callers.
- [ ] Confirm that `|decrypt` is available only after TLS client-certificate and credential
      authentication; document that authenticated identities share access to all stored keys.
- [ ] Record the cross-service blast radius and audit the requesting identity and key name without
      exposing key material. If the trust model changes, define per-key authorization and migration
      in a separate issue before changing this behavior.
- [ ] Add tests for a private-key encoding placed in a stored public-key field, malformed legacy
      records, client API/serialization boundaries, permitted cross-service decrypt after valid
      authentication, and rejection of invalid certificates and credentials. Cover the official
      client over TLS as well as the handler in isolation.
- [ ] Document startup failure behavior and compatibility impact for existing databases and
      clients.

#### Acceptance criteria

- [ ] No private-key material is returned through Java APIs, serialization, logs, exceptions, or
      CANISP responses, including when a decryptable database contains malformed key fields.
- [ ] The shared-access policy is enforced only after authentication and is tested and documented;
      decrypt audit records identify the requester and selected key without exposing key material.
- [ ] Valid existing `CANISDB1` databases and authorized file workflows remain usable after restart.

**Coordination:** Issue #8 now covers atomic database writes and missing-database handling. Audit
schema and integrity requirements remain with Issues #11-#19.

# Phase 4 — Product hardening beyond the current study

These issues remain tracked here even when the current JOSS study does not depend on them.
Prioritize backup and recovery (#10), safe errors and startup/shutdown checks (#22–#25),
and the audit schema and integrity work (#11–#19) before product maturity work such as
rotation (#9), resource controls (#20–#21), and containers (#26–#30). Phase 1 contains
the security boundary findings (#7, #8, and #36).

## Cross-cutting maintenance

- [ ] Keep the client and server TLS cipher-suite policies aligned when either policy changes; consider a shared policy or cross-module check.
- [ ] Consider a shared TLS test fixture module to reduce duplicated certificate-authority helpers.

### Issue #9 - Implement Key Rotation and Revocation

#### Objetivo

Adicionar ciclo de vida completo para chaves.

#### Tarefas

- [ ] Rotação
- [ ] Revogação
- [ ] Auditoria correspondente

#### Critérios de Aceitação

- [ ] Chaves podem ser revogadas sem perda histórica

### Issue #10 - Backup and Recovery Validation

#### Objetivo

Garantir recuperação segura.

#### Tarefas

- [ ] Backup completo
- [ ] Restore completo
- [ ] Restore após falha
- [ ] Restore após corrupção parcial

#### Critérios de Aceitação

- [ ] Dados permanecem descriptografáveis

## Product area 3 — Structured Audit

### Issue #11 - Define Audit Event Schema

#### Objetivo

Criar modelo estável de auditoria.

#### Campos

- [ ] eventId
- [ ] sequenceNumber
- [ ] timestamp
- [ ] identity
- [ ] operation
- [ ] resourceId
- [ ] keyId
- [ ] result

#### Critérios de Aceitação

- [ ] Esquema documentado

### Issue #12 - Audit Authentication Events

#### Objetivo

Registrar eventos de autenticação.

#### Tarefas

- [ ] Login bem sucedido
- [ ] Login falho
- [ ] Falha de autorização

#### Critérios de Aceitação

- [ ] Eventos gerados automaticamente

### Issue #13 - Audit Key Lifecycle Events

#### Objetivo

Auditar todo ciclo das chaves.

#### Tarefas

- [ ] Criação
- [ ] Rotação
- [ ] Revogação
- [ ] Remoção

#### Critérios de Aceitação

- [ ] Todas as operações sensíveis auditadas

### Issue #14 - Audit Log Sanitization

#### Objetivo

Eliminar vazamento de dados sensíveis.

#### Tarefas

- [ ] Remover credenciais
- [ ] Remover plaintext
- [ ] Remover material criptográfico

#### Critérios de Aceitação

- [ ] Logs livres de segredos

### Issue #15 - Audit Automated Tests

#### Objetivo

Criar suíte automatizada de auditoria.

#### Critérios de Aceitação

- [ ] Todos os eventos possuem cobertura

## Product area 4 — Verifiable Audit

### Issue #16 - Implement Audit Hash Chain

#### Objetivo

Criar auditoria verificável.

#### Fórmula

```text
H(i)=SHA256(H(i-1)||E(i))
```

#### Critérios de Aceitação

- [ ] Todos os eventos possuem hash encadeado

### Issue #17 - Implement Audit Integrity Verifier

#### Objetivo

Criar ferramenta independente de verificação.

#### Critérios de Aceitação

- [ ] Histórico pode ser validado offline

### Issue #18 - Implement Audit Anchoring

#### Objetivo

Persistir hashes finais em armazenamento independente.

#### Critérios de Aceitação

- [ ] Hashes podem ser comparados externamente

### Issue #19 - Tamper Detection Test Suite

#### Objetivo

Validar resistência à adulteração.

#### Tarefas

- [ ] Modificação
- [ ] Exclusão
- [ ] Truncamento
- [ ] Inserção
- [ ] Reescrita

#### Critérios de Aceitação

- [ ] Todas as fraudes são detectadas

## Product area 5 — Runtime Hardening

### Issue #20 - Connection Limits

#### Objetivo

Prevenir exaustão de recursos.

#### Tarefas

- [ ] Limitar conexões simultâneas
- [ ] Configurar fila de espera

### Issue #21 - Authentication Rate Limiting

#### Objetivo

Mitigar brute force.

#### Tarefas

- [ ] Rate limiting
- [ ] Bloqueio temporário

### Issue #22 - Secure Error Handling

#### Objetivo

Padronizar tratamento de falhas.

#### Critérios de Aceitação

- [ ] Respostas não expõem informações internas

### Issue #23 - Startup Integrity Verification

#### Objetivo

Validar ambiente durante inicialização.

#### Tarefas

- [ ] CANIS_SECRET_KEY
- [ ] Banco
- [ ] Checksums

### Issue #24 - Graceful Shutdown

#### Objetivo

Encerramento consistente.

#### Critérios de Aceitação

- [ ] Sem perda de dados

### Issue #25 - Concurrency and Recovery Tests

#### Objetivo

Validar comportamento sob carga e falhas.

#### Tarefas

- [ ] Testes concorrentes
- [ ] Testes de recuperação

## Product area 6 — Containerization

### Issue #26 - Create Multi-stage Docker Build

#### Objetivo

Reduzir superfície de ataque e tamanho da imagem.

#### Critérios de Aceitação

- [ ] Build multi-stage funcional

### Issue #27 - Run Container as Non-Root

#### Critérios de Aceitação

- [ ] Processo principal não executa como root

### Issue #28 - Externalize Secrets and Persistence

#### Tarefas

- [ ] Certificados
- [ ] Truststore
- [ ] CANIS_SECRET_KEY
- [ ] Persistência

#### Critérios de Aceitação

- [ ] Nenhum segredo embarcado na imagem

### Issue #29 - Harden Container Runtime

#### Tarefas

- [ ] Filesystem readonly
- [ ] Menor privilégio possível

### Issue #30 - Docker Compose Deployment Guide

#### Objetivo

Documentar implantação local.

#### Critérios de Aceitação

- [ ] Ambiente sobe com um único comando
