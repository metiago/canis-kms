# CANIS-KMS — App Production-Grade Backlog

> Scope: engineering, security, operational reliability, deployment, and production readiness. This is a development plan, **not** a certification that CANIS-KMS is production-ready. Original issue IDs, checkboxes, and evidence are preserved from `backlog.md`.
>
> JOSS-specific research and publication work (original Issues #31–35) has moved to [JOSS Backlog](joss-backlog.md). The JOSS release may depend on selected issues here, but publication does not require completing every production feature.

## Production release priorities

- **P0 — Security boundaries:** close #36 (loaded-key validation, private-key exposure, cross-service decrypt authorization), #8 (database corruption validation and silent-recreation race), and #7 (all supported master-key startup paths).
- **P1 — Operational reliability:** #10 backup/restore, #22 safe errors, #23 startup integrity, #24 graceful shutdown, #25 concurrency and recovery tests.
- **P1/P2 — Security operations:** #11–19 audit schema, coverage, tamper evidence, and verification; prioritize according to the product's claims and threat model.
- **P2 — Product maturity:** #9 rotation/revocation, #20–21 resource controls, #26–30 containers and deployment.

**Release boundary:** Completion of an issue in the original roadmap means its listed work was reported as tested in a working tree, not independently verified against a tagged release. Re-run checks on a release candidate.

---

# Milestone 1 - Security Transport

## Completed in this milestone

- **Issue #1 - Migrate CANIS Server from TCP to TLS:** completed in commit `666eba0`. The server uses TLS 1.3, requires client certificates, loads TLS identity/trust material externally, and has tests for plaintext, TLS 1.2, untrusted-client, and invalid-configuration rejection. See `ServerTlsSecurityTest` and `TlsContextFactory`.

---

## Issue #2 - Implement TLS in JpawClient

**Status:** Completed and verified in the working tree.

**JOSS release dependency:** The completed client/server TLS workflow must be re-verified on the submission candidate; track publication evidence in [JOSS Backlog](joss-backlog.md).

### Objetivo

Adicionar suporte TLS ao cliente CANIS.

### Tarefas

- [x] Criar conexão usando `SSLSocket`
- [x] Carregar identidade de cliente e truststore a partir de configuração externa
- [x] Restringir a conexão a TLS 1.3, sem fallback plaintext
- [x] Validar cadeia e hostname do servidor usando a identidade DNS configurada
- [x] Documentar variáveis/configuração sem incluir segredos

### Critérios de Aceitação

- [x] Cliente rejeita certificados não confiáveis e falhas de handshake
- [x] Cliente rejeita hostnames incorretos
- [x] Cliente e servidor completam o fluxo de arquivo fim a fim usando mTLS
- [x] Testes verificam certificado de cliente não confiável, certificado de servidor não confiável, TLS legado, plaintext e falha de handshake/protocolo
- [x] Nenhum caminho de conexão plaintext permanece no cliente oficial

**Evidence:** `mvn -q -pl client test` passed. `mvn -q -pl server -am '-Dtest=ServerEndToEndTest' '-Dsurefire.failIfNoSpecifiedTests=false' test` passed, including the official-client file workflow against the TLS server. Coverage is in `SocketClientTest`, `EnvironmentLoaderTest`, and `ServerEndToEndTest`.

---

## Issue #3 - Harden TLS Configuration

**Status:** Implemented and verified in the working tree.

### Objetivo

Aplicar configuração segura de TLS.

### Tarefas

- [x] Permitir AES-GCM
- [x] Permitir ChaCha20-Poly1305
- [x] Proibir TrustManagers inseguros
- [x] Documentar configuração

### Critérios de Aceitação

- [x] Não existe bypass de validação
- [x] Suites inseguras não são utilizadas

**Evidence:** `mvn test` passed: 55 client tests (5 existing skips) and 60 server tests. `TlsCipherPolicyTest` verifies the allowlist on real sockets, preservation of provider-disabled suites, and rejection when no approved suite is enabled. `SocketClientTest` and `ServerTlsSecurityTest` verify negotiated TLS 1.3 suites and existing certificate, hostname, and protocol rejection paths. Trust managers are created with PKIX from configured trust stores.

**Operational impact:** Client and server allow only provider-enabled TLS 1.3 AES-GCM and ChaCha20-Poly1305 suites. If no approved suite remains enabled, setup fails closed with a sanitized diagnostic before the server binds or the client handshakes. Peers must share an enabled approved suite. CANISP and stored data formats are unchanged.

**Follow-up:** Keep the client and server suite lists aligned if the policy changes; a shared policy or cross-module check would reduce drift risk.

---

## Issue #4 - Create TLS Security Test Suite

**Status:** Completed and verified in the working tree.

**Coordenação:** Cobrir cenários negativos do cliente em Issue #2 e cenários do servidor já não cobertos pelos testes existentes. Evitar duplicar os testes de handshake de servidor.

### Objetivo

Implementar testes negativos de TLS.

### Tarefas

- [x] Certificado expirado
- [x] Certificado inválido
- [x] Certificado não confiável
- [x] Hostname incorreto
- [x] Downgrade attack
- [x] Conexão plaintext

### Critérios de Aceitação

- [x] Todos os cenários falham corretamente

**Evidence:** `mvn -q test` passed: 59 client tests (5 existing skips) and 63 server tests, with no failures or errors. `SocketClientTest` covers client-side rejection before login, including expired and wrong-purpose server certificates, malformed trust material, wrong hostname, untrusted peers, TLS 1.2, and a plaintext endpoint. `ServerTlsCertificateTest` proves a valid CA-signed client can authenticate and that expired or wrong-purpose client certificates receive no CANISP response. Existing `ServerTlsSecurityTest` covers untrusted clients, TLS 1.2, plaintext, invalid credentials, and invalid TLS material. See [TLS security tests](docs/tls-security-tests.md) for the scenario matrix and fixture design.

**Operational and compatibility impact:** Test fixtures and documentation only; runtime TLS configuration, CANISP framing, and stored data are unchanged. Fixtures require JDK `keytool` and an approved TLS 1.3 cipher suite. A shared test fixture module could reduce the duplicated CA issuance helpers later.

---

# Milestone 2 - Key Protection

## Issue #5 - Review RSA Key Lifecycle

**Status:** Lifecycle review completed and verified in the working tree. Security follow-ups remain open.

### Objetivo

Revisar geração, armazenamento e uso das chaves RSA.

### Tarefas

- [x] Revisar criação
- [x] Revisar persistência
- [x] Revisar carregamento
- [x] Revisar destruição

### Critérios de Aceitação

- [x] Fluxo documentado

**Evidence:** [RSA key lifecycle review](docs/rsa-key-lifecycle.md) documents generation, encrypted storage, startup loading, server-side use, logical deletion, recovery, and outstanding risks. `ServerEndToEndTest` verifies that a real RSA key decrypts an encrypted file after restart and remains unavailable after deletion and another restart. `mvn -q test` passed: 59 client tests (5 existing skips) and 77 server tests, with no failures or errors.

**Follow-up:** Issue #6 now prevents private-key export through supported server-side Java APIs and reports persistence failures. A missing database can still start empty, and database writes are not yet atomic. Address these with the remaining key-protection work tracked in Issues #7-#10. No `CANISDB1` or CANISP format was changed.

---

## Issue #6 - Prevent Private Key Exposure

**Status:** Completed and verified in the working tree.

### Objetivo

Garantir que chaves privadas nunca sejam expostas.

### Tarefas

- [x] Revisar APIs públicas
- [x] Revisar serializações
- [x] Revisar logs
- [x] Revisar mensagens de erro

### Critérios de Aceitação

- [x] Nenhuma chave privada é exportada pelos caminhos suportados

**Evidence:** [Private-key exposure boundary](docs/private-key-exposure.md) documents the Java API,
CANISP, persistence, logging, and error boundaries. `Entry` now exposes only immutable public
metadata; private-key operations remain inside `KeyValueStore`. `ClientHandlerTest` checks the
actual generated private-key encoding against CANISP responses and checks that a key-bearing
exception cannot enter operational or audit logs. `KeyValueStoreTest` verifies non-serializability,
legacy `CANISDB1` loading and decryption, and tamper and save-failure paths. `mvn spotless:apply`
and `mvn -q test` passed.

**Operational and compatibility impact:** Existing encrypted databases and CANISP commands remain
compatible. Server-side Java callers of the removed raw-key APIs must migrate to the store's
metadata and decrypt operations. Database write failures now close the operation without a
success response.

**Remaining risks:** In-process arbitrary code, Java memory erasure, historical backups,
cross-service decrypt authorization, master-key initialization, atomic database writes, and
verifiable audit are outside this issue; see the linked boundary review and subsequent issues.

**Review follow-up:** Issue #36 tracks the remaining public API and loaded-record exposure paths
identified after implementation, together with decrypt authorization.

---

## Issue #7 - Protect CANIS_SECRET_KEY

**Status:** Implemented and tested; review follow-up remains open for the public server construction path.

### Objetivo

Fortalecer proteção da chave mestra.

### Tarefas

- [x] Verificar permissões
- [x] Validar existência no startup
- [x] Validar formato
- [x] Documentar requisitos

### Critérios de Aceitação

- [ ] Aplicação não inicia sem chave válida em todos os caminhos públicos de inicialização

**Evidence:** `mvn spotless:apply` and `mvn -q test` passed (143 tests, no failures or errors;
five existing client integration skips and two platform skips on Windows). `MasterKeyLoaderTest`
covers valid legacy and longer AES keys, missing and malformed files, symlinks where supported,
and unsafe POSIX permissions or Windows ACLs.
`ServerTest` verifies missing or malformed keys stop the production-style constructor before
`start` is called. `KeyValueStoreTest` verifies existing database compatibility, wrong-key and
tampering rejection, and that a running store never recreates a missing key file.

**Operational and compatibility impact:** Operators must provision a protected Base64 AES key
file before starting CANIS, including for an empty database. The key is loaded once per process;
changing the file requires a restart. Existing `CANISDB1` and `CANISGCM1` formats are unchanged.
See [master-key requirements](docs/master-key.md). Issue #8 now requires explicit initialization
and rejects a missing database during normal startup.

**Review findings (2026-11-16):**

- **Acceptance gap:** `Server(Environment, KeyValueStore)` is public and does not validate
  `Environment.secretKeyPath` or require a non-null, matching, validated store. A caller can bind
  the TLS listener with an invalid configured key. Make the invariant hold for every supported
  construction path and add a negative startup test before checking this acceptance criterion.
- **Permission boundary:** The loader checks the key file and its immediate directory; it relies
  on operators to protect higher ancestor directories. Review path replacement resistance and
  decide whether code should validate the full path or use a secure open strategy.
- **Runtime key loss:** The store caches the key. If its file disappears after startup, writes can
  still succeed, but a restart will fail. Decide whether to detect and alert on key-file loss.
- **Coverage gap:** POSIX permission and symlink tests were skipped on the Windows verification
  host. Run them on a POSIX CI host and add cases for owner mismatch, inherited Windows ACLs, and
  captured logs that must not contain key-file content or paths.

**Related follow-up:** Issue #8 now owns explicit database initialization and safe recovery behavior.
Cross-service decrypt authorization remains with Issue
#36. Existing `CANISDB1`/`CANISGCM1` formats remain compatible.

---

## Issue #8 - Prevent Silent Database Recreation

**Status:** Implemented and tested; review follow-up remains open before acceptance.

### Objetivo

Impedir recriação automática do banco criptográfico.

### Tarefas

- [x] Detectar chave inválida
- [x] Detectar chave ausente
- [ ] Detectar banco corrompido (authenticated records with invalid key fields still load)
- [x] Entrar em modo seguro

### Critérios de Aceitação

- [ ] Nenhum reset silencioso é executado em todos os caminhos de escrita

**Evidence:** `KeyValueStore` now requires an existing authenticated database during normal
construction. The separate `--init-db` command creates a new empty database only with create-new
semantics and a valid externally provisioned master key. Once a request detects database loss or
change, the store enters a recovery-required state and stops further CANISP processing. Writes use
a staged atomic replacement instead of truncating the current file. The encrypted `CANISDB1` and `CANISGCM1`
formats are unchanged. `mvn spotless:apply` and `mvn -q test` passed on Windows (148 tests,
7 existing skips, no failures or errors); the packaged server jar also built successfully.

**Operational and compatibility impact:** Operators must run `--init-db` once for a new installation
and keep the database working directory protected. Normal startup rejects an absent database, so a
previously used installation must restore its original database and matching key from backup.
Filesystems without atomic move support reject writes. Existing valid databases and CANISP clients
remain compatible; databases larger than 256 MiB now require an explicit size-limit review.
See [master-key and recovery requirements](docs/master-key.md).

**Remaining risks:** Directory control by an untrusted local actor can race application file checks;
protect the working directory with operating-system permissions. Directory metadata is not synced
after an atomic replacement on every platform, so crash durability needs platform-specific review.
Each operation reads and hashes the database, which can increase latency as it grows; future work
should retain runtime tamper detection while reducing this cost.
The existing audit logger still lacks the required schema and integrity verification; Issues #11-#19
own that work. Runtime loss of the cached master-key file remains an Issue #7 follow-up.

**Implementation review (2026-11-16):**

- **Architecture:** `KeyValueStore` now combines initialization, file identity and digest checks,
  atomic replacement, serialization, and RSA operations. A focused persistence component with an
  explicit database configuration would isolate these concerns and allow deterministic fault
  injection. `Server.main` also hardcodes `db.dat`, making the command sensitive to working directory.
- **High — silent recreation race:** `KeyValueStore.saveToFile` checks the database, then performs
  `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`. If the destination disappears after the final
  check, the move can create it again. Close this gap and add a deterministic race test before
  checking the no-silent-reset acceptance criterion.
- **High — incomplete record validation:** Authenticated `CANISDB1` records with malformed RSA
  fields still load; `KeyValueStoreTest.malformedStoredPrivateKeyNeverAppearsInException` exercises
  that behavior. The parser also accepts malformed UTF-8 by replacement. Define and enforce valid
  stored-key fields, coordinating with Issue #36, before marking corruption detection complete.
- **Audit requirement:** Database initialization and recovery-required transitions use operational
  logs only. `AuditLogger` lacks the required `eventId`, `timestamp`, `operation`, `identity`, and
  `result` fields and integrity verification. Coordinate a verifiable event path with Issues #11-#19;
  the current implementation does not satisfy the `AGENTS.md` audit requirements.
- **Coverage gaps:** No test controls deletion in the check-to-move interval, injects an interrupted
  or unsupported atomic move, or executes the packaged `--init-db` command end to end. The
  end-to-end test helper initializes missing databases, so only dedicated startup tests catch
  accidental startup recreation. Tests do not cover a symlinked database path or capture
  storage-failure logs for path or key-material leakage. POSIX master-key permission and symlink
  coverage was skipped on the Windows verification host;
  run it in POSIX CI.
- **Operational risks:** The TLS listener notices idle-time database loss only on a later request.
  Every store operation reads and hashes up to 256 MiB, and directory metadata is not forced after
  replacement. Review detection latency, scale, and power-loss recovery on supported filesystems.

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
