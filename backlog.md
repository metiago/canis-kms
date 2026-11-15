# Milestone 1 - Security Transport

## Completed in this milestone

- **Issue #1 - Migrate CANIS Server from TCP to TLS:** completed in commit `666eba0`. The server uses TLS 1.3, requires client certificates, loads TLS identity/trust material externally, and has tests for plaintext, TLS 1.2, untrusted-client, and invalid-configuration rejection. See `ServerTlsSecurityTest` and `TlsContextFactory`.

---

## Issue #2 - Implement TLS in JpawClient

**Status:** Completed and verified in the working tree.

**JOSS tracking:** See [submission-readiness.md](joss-papers/submission-readiness.md), item 2. Keep implementation tasks here; the JOSS checklist records the submission dependency and evidence.

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

**Follow-up:** Persistence errors can still be acknowledged as success, a missing database can start empty, and server-side Java APIs can expose private-key material. Address these with fail-closed persistence and the key-protection work tracked in Issues #6-#10. No `CANISDB1`, CANISP, or runtime behavior was changed by this review.

---

## Issue #6 - Prevent Private Key Exposure

### Objetivo

Garantir que chaves privadas nunca sejam expostas.

### Tarefas

- [ ] Revisar APIs públicas
- [ ] Revisar serializações
- [ ] Revisar logs
- [ ] Revisar mensagens de erro

### Critérios de Aceitação

- [ ] Nenhuma chave privada é exportada

---

## Issue #7 - Protect CANIS_SECRET_KEY

### Objetivo

Fortalecer proteção da chave mestra.

### Tarefas

- [ ] Verificar permissões
- [ ] Validar existência no startup
- [ ] Validar formato
- [ ] Documentar requisitos

### Critérios de Aceitação

- [ ] Aplicação não inicia sem chave válida

---

## Issue #8 - Prevent Silent Database Recreation

### Objetivo

Impedir recriação automática do banco criptográfico.

### Tarefas

- [ ] Detectar chave inválida
- [ ] Detectar chave ausente
- [ ] Detectar banco corrompido
- [ ] Entrar em modo seguro

### Critérios de Aceitação

- [ ] Nenhum reset silencioso é executado

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

# Milestone 7 - Security Research

## Issue #31 - Create Baseline Benchmark

### Objetivo

Gerar linha de base da implementação atual.

---

## Issue #32 - TLS Performance Benchmark

### Métricas

- [ ] Latência
- [ ] Throughput
- [ ] Memória

### Comparação

- [ ] Sem TLS
- [ ] Com TLS

---

## Issue #33 - Multi-Service Interoperability Study

### Objetivo

Validar três instâncias compartilhando armazenamento.

---

## Issue #34 - Security Fault Injection Experiments

### Cenários

- [ ] Roubo da chave mestra
- [ ] Corrupção do armazenamento
- [ ] Adulteração da auditoria

---

## Issue #35 - Research Report

### Objetivo

Publicar resultados em `docs/research`.

### Entregáveis

- [ ] Metodologia
- [ ] Resultados
- [ ] Discussão
- [ ] Limitações
- [ ] Reprodutibilidade
