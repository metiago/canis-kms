# CANIS-KMS — App Production-Grade Backlog

> Pending engineering, security, reliability, and deployment work. This is not a claim that CANIS-KMS is production-ready. JOSS publication tasks are tracked in [JOSS Backlog](joss-backlog.md).

## Production release priorities

- **P0 — Security boundaries:** #36 (stored-key validation, private-key exposure, cross-service decrypt authorization), #8 (corruption detection and silent-recreation race), and #7 (master-key validation on every supported startup path).
- **P1 — Operational reliability:** #10 backup/restore, #22 safe errors, #23 startup integrity, #24 graceful shutdown, and #25 concurrency/recovery tests.
- **P1/P2 — Security operations:** #11–19 audit schema, coverage, tamper evidence, and verification.
- **P2 — Product maturity:** #9 rotation/revocation, #20–21 resource controls, and #26–30 containers and deployment.

## Other pending maintenance

- [ ] Keep the client and server TLS cipher-suite policies aligned when either policy changes; consider a shared policy or cross-module check.
- [ ] Consider a shared TLS test fixture module to reduce duplicated certificate-authority helpers.

# Milestone 2 - Key Protection

## Issue #7 - Protect CANIS_SECRET_KEY

### Pending work

- [ ] Reconcile the earlier public-constructor finding with the current `Server.verifyConfiguration` path; verify that every supported server construction path rejects a missing, malformed, or mismatched master key and store before listening.
- [ ] Review ancestor-directory permissions and path-replacement resistance; define the required operator controls or a secure-open approach.
- [ ] Decide whether runtime loss of the cached master-key file must be detected and alerted before restart.
- [ ] Run POSIX permission and symlink tests on POSIX CI; add owner-mismatch, inherited Windows ACL, and sanitized-log cases.

### Acceptance criteria

- [ ] No supported server construction path starts with an invalid master-key configuration.

## Issue #8 - Prevent Silent Database Recreation

### Pending work

- [ ] Reject authenticated database records with invalid RSA fields or malformed UTF-8 without rewriting the database; coordinate stored-key validation with Issue #36.
- [ ] Close the check-to-move race in `KeyValueStore.saveToFile` so a missing destination cannot be silently recreated.
- [ ] Add deterministic tests for deletion between check and move, interrupted or unsupported atomic moves, a symlinked database path, and storage-failure log sanitization.
- [ ] Exercise the packaged `--init-db` command end to end and confirm normal startup never treats a missing database as a new installation.
- [ ] Review idle-time database-loss detection, directory metadata durability after replacement, and the cost of reading and hashing large databases on supported filesystems.

### Acceptance criteria

- [ ] Corrupted or malformed stored records fail safely.
- [ ] No write path silently recreates a missing cryptographic database.

---

## Issue #9 - Implement Key Rotation and Revocation

### Objetivo

Adicionar ciclo de vida completo para chaves.

### Tarefas

- [ ] Rotação
- [ ] Revogação
- [ ] Auditoria correspondente

### Critérios de Aceitação

- [ ] Chaves podem ser revogadas sem perda histórica

---

## Issue #10 - Backup and Recovery Validation

### Objetivo

Garantir recuperação segura.

### Tarefas

- [ ] Backup completo
- [ ] Restore completo
- [ ] Restore após falha
- [ ] Restore após corrupção parcial

### Critérios de Aceitação

- [ ] Dados permanecem descriptografáveis

---

# Milestone 3 - Structured Audit

## Issue #11 - Define Audit Event Schema

### Objetivo

Criar modelo estável de auditoria.

### Campos

- [ ] eventId
- [ ] sequenceNumber
- [ ] timestamp
- [ ] identity
- [ ] operation
- [ ] resourceId
- [ ] keyId
- [ ] result

### Critérios de Aceitação

- [ ] Esquema documentado

---

## Issue #12 - Audit Authentication Events

### Objetivo

Registrar eventos de autenticação.

### Tarefas

- [ ] Login bem sucedido
- [ ] Login falho
- [ ] Falha de autorização

### Critérios de Aceitação

- [ ] Eventos gerados automaticamente

---

## Issue #13 - Audit Key Lifecycle Events

### Objetivo

Auditar todo ciclo das chaves.

### Tarefas

- [ ] Criação
- [ ] Rotação
- [ ] Revogação
- [ ] Remoção

### Critérios de Aceitação

- [ ] Todas as operações sensíveis auditadas

---

## Issue #14 - Audit Log Sanitization

### Objetivo

Eliminar vazamento de dados sensíveis.

### Tarefas

- [ ] Remover credenciais
- [ ] Remover plaintext
- [ ] Remover material criptográfico

### Critérios de Aceitação

- [ ] Logs livres de segredos

---

## Issue #15 - Audit Automated Tests

### Objetivo

Criar suíte automatizada de auditoria.

### Critérios de Aceitação

- [ ] Todos os eventos possuem cobertura

---

# Milestone 4 - Verifiable Audit

## Issue #16 - Implement Audit Hash Chain

### Objetivo

Criar auditoria verificável.

### Fórmula

```text
H(i)=SHA256(H(i-1)||E(i))
```

### Critérios de Aceitação

- [ ] Todos os eventos possuem hash encadeado

---

## Issue #17 - Implement Audit Integrity Verifier

### Objetivo

Criar ferramenta independente de verificação.

### Critérios de Aceitação

- [ ] Histórico pode ser validado offline

---

## Issue #18 - Implement Audit Anchoring

### Objetivo

Persistir hashes finais em armazenamento independente.

### Critérios de Aceitação

- [ ] Hashes podem ser comparados externamente

---

## Issue #19 - Tamper Detection Test Suite

### Objetivo

Validar resistência à adulteração.

### Tarefas

- [ ] Modificação
- [ ] Exclusão
- [ ] Truncamento
- [ ] Inserção
- [ ] Reescrita

### Critérios de Aceitação

- [ ] Todas as fraudes são detectadas

---

# Milestone 5 - Runtime Hardening

## Issue #20 - Connection Limits

### Objetivo

Prevenir exaustão de recursos.

### Tarefas

- [ ] Limitar conexões simultâneas
- [ ] Configurar fila de espera

---

## Issue #21 - Authentication Rate Limiting

### Objetivo

Mitigar brute force.

### Tarefas

- [ ] Rate limiting
- [ ] Bloqueio temporário

---

## Issue #22 - Secure Error Handling

### Objetivo

Padronizar tratamento de falhas.

### Critérios de Aceitação

- [ ] Respostas não expõem informações internas

---

## Issue #23 - Startup Integrity Verification

### Objetivo

Validar ambiente durante inicialização.

### Tarefas

- [ ] CANIS_SECRET_KEY
- [ ] Banco
- [ ] Checksums

---

## Issue #24 - Graceful Shutdown

### Objetivo

Encerramento consistente.

### Critérios de Aceitação

- [ ] Sem perda de dados

---

## Issue #25 - Concurrency and Recovery Tests

### Objetivo

Validar comportamento sob carga e falhas.

### Tarefas

- [ ] Testes concorrentes
- [ ] Testes de recuperação

---

# Milestone 6 - Containerization

## Issue #26 - Create Multi-stage Docker Build

### Objetivo

Reduzir superfície de ataque e tamanho da imagem.

### Critérios de Aceitação

- [ ] Build multi-stage funcional

---

## Issue #27 - Run Container as Non-Root

### Critérios de Aceitação

- [ ] Processo principal não executa como root

---

## Issue #28 - Externalize Secrets and Persistence

### Tarefas

- [ ] Certificados
- [ ] Truststore
- [ ] CANIS_SECRET_KEY
- [ ] Persistência

### Critérios de Aceitação

- [ ] Nenhum segredo embarcado na imagem

---

## Issue #29 - Harden Container Runtime

### Tarefas

- [ ] Filesystem readonly
- [ ] Menor privilégio possível

---

## Issue #30 - Docker Compose Deployment Guide

### Objetivo

Documentar implantação local.

### Critérios de Aceitação

- [ ] Ambiente sobe com um único comando

---

# Milestone 7 - Security Review Follow-up (original Milestone 8)

## Issue #36 - Close Private-Key Exposure Review Findings

**Status:** Open. Follow-up to Issue #6 and its implementation review.

### Objective

Prevent private-key material from reaching public APIs or CANISP responses through malformed but
decryptable stored records, and prevent one authenticated service from using another service's
private key without authorization.

### Tasks

- [ ] Validate loaded `CANISDB1` public and private key encodings and that they form a matching RSA
      pair before exposing metadata or serving requests. Fail closed on invalid records without
      replacing or rewriting the database.
- [ ] Remove the unused `privateKey` field and Java serialization capability from the client
      artifact's `io.canis.jpaw.pojo.Entry`, or remove that type if it has no supported callers.
- [ ] Define and enforce authorization for `|decrypt` by authenticated identity, including an
      explicit policy for administrative access and existing service credentials.
- [ ] Add tests for a private-key encoding placed in a stored public-key field, malformed legacy
      records, client API/serialization boundaries, and cross-service decrypt denial. Cover the
      official client over TLS as well as the handler in isolation.
- [ ] Document startup failure behavior and compatibility impact for existing databases and
      clients.

### Acceptance criteria

- [ ] No private-key material is returned through Java APIs, serialization, logs, exceptions, or
      CANISP responses, including when a decryptable database contains malformed key fields.
- [ ] Unauthorized cross-service decryption is rejected and audited without exposing key material.
- [ ] Valid existing `CANISDB1` databases and authorized file workflows remain usable after restart.

**Coordination:** Issue #8 now covers atomic database writes and missing-database handling. Audit
schema and integrity requirements remain with Issues #11-#19.
