---
title: 'CANIS-KMS: Key Management for Asynchronous Encrypted File Exchange Between Java Services'
tags:
  - key management
  - cryptography
  - distributed systems
  - file encryption
  - security research
authors:
  - name: 'REPLACE WITH AUTHOR NAME'
    affiliation: 1
affiliations:
  - name: 'REPLACE WITH INSTITUTION AND COUNTRY'
    index: 1
date: 14 November 2026
bibliography: paper.bib
---

# Summary

A service can finish writing a file long before another service is ready to read it. Shared storage makes that exchange straightforward, but it also creates an uncomfortable question: who can read the contents while the file is waiting to be processed? CANIS-KMS grew out of this situation. It is a Java-based key management service for applications that exchange sensitive files asynchronously through a shared filesystem.

Rather than passing private keys between applications, CANIS-KMS keeps them on a central server. A producing service retrieves the recipient's public key and encrypts the file locally using a fresh symmetric data key. The symmetric key is then protected using the recipient's public key. When the receiving service processes the file, the CANIS server performs the private-key operation needed to recover the data key; the file itself does not need to pass through the key server. The project provides an official Java client for this workflow and an inspectable client-server protocol called CANISP.

# Statement of need

The motivating environment is not a message broker or an archival repository. It is a collection of web services that write files to shared storage, pick up files produced by other services, and process them at different times. Files normally need to be processed within 24 hours. They may remain on disk for up to three months to satisfy a retention policy, but that later retention period does not imply that their contents must remain decryptable. These are workload assumptions, not retention rules currently enforced by CANIS-KMS.

Filesystem permissions alone do not solve the confidentiality problem. Several applications may need access to the same directory for operational reasons, while the contents should remain encrypted at rest. Equally, copying a private RSA key into every application that might consume a file increases the number of places where sensitive key material can be exposed. CANIS-KMS separates the exchange medium from the cryptographic operation: files stay in shared storage, and private keys stay in one managed service.

This separation is useful as a small, reproducible setting for researchers and developers studying file-based integration between services. It allows experiments to focus on where cryptographic work happens, how clients and key services interact, what happens when the key store is unavailable, and which security guarantees depend on the surrounding infrastructure. It is not intended as a replacement for filesystem access controls or application-level workflow validation.

# State of the field

CANIS-KMS does not introduce a new encryption algorithm. Its file protection approach combines established authenticated encryption and RSA-based key transport techniques [@dworkin2007; @moriarty2016]. Similar building blocks are available in mature software. Google Tink, for example, documents envelope encryption using data-encryption keys and an external key-encryption mechanism [@tink-envelope]. HashiCorp Vault's Transit secrets engine offers centrally managed cryptographic operations, key versions, and configurable access controls [@vault-transit]. These are more feature-rich options and should be preferred when an application needs their operational guarantees.

The reason for developing CANIS-KMS rather than configuring one of those systems was the narrower experimental problem: a Java client and server whose protocol, key store, file envelope, and failure paths can be inspected and changed together. CANISP is deliberately an internal protocol, not a proposed standard or a Redis-compatible interface. Keeping the implementation small makes it possible to study changes to transport protection, persistence, auditing, and fault handling without treating a large production platform as a black box. This is a research and teaching trade-off, not a claim that a custom KMS is more secure than established alternatives.

# Software design

CANIS-KMS has two Maven modules: a server and a Java client. Applications interact with the `JpawClient` API rather than constructing protocol messages. The server maintains named RSA key pairs, makes public keys available to clients, and handles requests that require a private key. Its key records are stored in an encrypted on-disk database; the database encryption key is supplied separately through `CANIS_SECRET_KEY`. Separating the database from its master key creates an explicit backup and recovery dependency: possession of the encrypted database alone is insufficient to restore its private keys.

The file exchange is asymmetric in a useful way. Suppose service A writes a file for service B. A obtains B's public key, generates a data-encryption key, encrypts the file with AES-GCM, and wraps the data key using RSA-OAEP. B subsequently reads the encrypted file and asks CANIS-KMS to unwrap the data key. Only the small wrapped key needs to be processed by the server, not the whole file. This keeps bulk file I/O out of the KMS request path. AES-GCM protects ciphertext integrity, but it does not, by itself, prove that A was the sender; authenticating a producer requires a separate mechanism.

The choice of a shared trust domain is intentional. In the documented access model, clients authenticate to the server, but any authenticated client can request a private-key operation for any stored key name. CANIS-KMS therefore does **not** currently provide tenant isolation or per-key authorization. The security boundary is the trusted deployment and its admitted client identities, not a permissions graph inside the KMS. This is an important limitation for experiments involving compromised clients, and it should not be confused with the stronger isolation offered by systems with per-key access policies.

The current public implementation uses a TCP request-response protocol. CANISP/1 defines text commands, typed responses, message-size limits, and read timeouts. Transport confidentiality must therefore be provided by a protected deployment until mandatory TLS is implemented in the server and client. The server also records events such as authentication outcomes, key operations, and decryption successes or failures. These logs provide a starting point for audit experiments, but they are not yet a cryptographically verifiable audit trail.

This design keeps the boundaries visible: encrypted file contents reside in shared storage; private keys reside in the key service; the master key is an external operational secret; and audit records describe how the key service was used. Each boundary can be examined independently when evaluating failures or changes to the security architecture.

# Research impact statement

CANIS-KMS is publicly available under the MIT license at [github.com/metiago/canis-kms](https://github.com/metiago/canis-kms). The repository contains the Java client and server, a documented CANISP/1 protocol, an explicit threat model, integration guidance, and Maven-based tests. These materials make the implementation available for inspection and provide a concrete starting point for reproducible experiments with encrypted, file-based service integration.

The immediate research value is the ability to test the consequences of identifiable design choices rather than assume that a central KMS makes an entire workflow secure. Relevant experiments include measuring the cost of protected transport, recovering from a missing or corrupted key store, testing simultaneous key operations, and determining how well audit records support investigation after a failure. The planned extension toward hash-chained, externally anchored audit logs would enable experiments in tamper detection. A separate, asynchronous AI-assisted log analyst could then be compared with deterministic detection rules; neither feature is presented here as implemented or experimentally validated.

At the time of this draft, the project does not claim published benchmark results, external research adoption, or demonstrated improvements from AI-assisted incident analysis. The research impact statement will need to be updated with completed, reproducible results before submission to JOSS.

# AI usage disclosure

OpenAI's ChatGPT (GPT-6) assisted with drafting and editing the English text of this manuscript. This is an initial working draft: the human author must review the text, verify all technical statements against the release being submitted, and approve the final wording. Any generative AI used in the software or its documentation must also be disclosed in the submission version; that information was not established while preparing this draft.

# Acknowledgements

<!-- Before submission: acknowledge financial or institutional support, if any. Do not claim that there was no funding without checking. -->

# References
