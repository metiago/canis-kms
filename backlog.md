# Milestone 1 - Security Transport

## Issue #1 - Migrate CANIS Server from TCP to TLS

### Objetivo
Substituir comunicação TCP pura por TLS obrigatório no servidor CANIS.

### Tarefas

- [ ] Substituir `ServerSocket` por `SSLServerSocket`
- [ ] Configurar `SSLContext`
- [ ] Carregar certificado e chave de forma externa
- [ ] Configurar truststore externa
- [ ] Restringir conexão para TLS 1.3
- [ ] Desabilitar protocolos legados

### Critérios de Aceitação

- [ ] Nenhuma conexão plaintext é aceita
- [ ] Apenas TLS 1.3 é permitido
- [ ] Falhas de configuração impedem inicialização

---

## Issue #2 - Implement TLS in JpawClient

### Objetivo

Adicionar suporte TLS ao cliente CANIS.

### Tarefas

- [ ] Criar conexão usando `SSLSocket`
- [ ] Configurar truststore
- [ ] Validar cadeia de certificados
- [ ] Habilitar verificação de hostname

### Critérios de Aceitação

- [ ] Cliente rejeita certificados inválidos
- [ ] Cliente rejeita hostnames incorretos

---

## Issue #3 - Harden TLS Configuration

### Objetivo

Aplicar configuração segura de TLS.

### Tarefas

- [ ] Permitir AES-GCM
- [ ] Permitir ChaCha20-Poly1305
- [ ] Proibir TrustManagers inseguros
- [ ] Documentar configuração

### Critérios de Aceitação

- [ ] Não existe bypass de validação
- [ ] Suites inseguras não são utilizadas

---

## Issue #4 - Create TLS Security Test Suite

### Objetivo

Implementar testes negativos de TLS.

### Tarefas

- [ ] Certificado expirado
- [ ] Certificado inválido
- [ ] Certificado não confiável
- [ ] Hostname incorreto
- [ ] Downgrade attack
- [ ] Conexão plaintext

### Critérios de Aceitação

- [ ] Todos os cenários falham corretamente

---

# Milestone 2 - Key Protection

## Issue #5 - Review RSA Key Lifecycle

### Objetivo

Revisar geração, armazenamento e uso das chaves RSA.

### Tarefas

- [ ] Revisar criação
- [ ] Revisar persistência
- [ ] Revisar carregamento
- [ ] Revisar destruição

### Critérios de Aceitação

- [ ] Fluxo documentado

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