---
title: 'CANIS-KMS: Key Management for Asynchronous Encrypted File Exchange Between Java Services'
tags:
  - key management
  - cryptography
  - distributed systems
  - file encryption
authors:
  - name: 'REPLACE WITH AUTHOR NAME'
    affiliation: 1
affiliations:
  - name: 'REPLACE WITH INSTITUTION AND COUNTRY'
    index: 1
date: 14 November 2026
bibliography: ../paper.bib
---

# Summary

CANIS-KMS is a Java key management service for applications that exchange sensitive files through shared storage. A producer encrypts a file locally for its intended recipient. Later, the recipient asks CANIS-KMS to recover the file's data key; the file contents remain in the shared filesystem and do not pass through the key service. This arrangement suits workflows in which services write and process files at different times and should not distribute private key files among application hosts.

# Statement of need

The project arose from a practical integration problem: several Java web services exchange files asynchronously through a shared filesystem. Files are normally processed within 24 hours and may be kept for up to three months to meet a retention policy. These are properties of the motivating workload, not timers or deletion policies implemented by CANIS-KMS. The retained file need not remain decryptable after its processing period.

Filesystem permissions can control which processes access a shared directory, but they do not encrypt its contents. Copying recipient private keys to every service that may process a file also expands the number of hosts on which those keys must be protected. CANIS-KMS keeps RSA private keys in a central, encrypted database and leaves bulk file encryption to the producer. The intended users are developers and researchers examining encrypted, file-based service integration, including the operational consequences of delayed processing, key-service outages, and shared trust assumptions.

# State of the field

The software uses established cryptographic primitives rather than proposing a new encryption method [@dworkin2007; @moriarty2016]. Libraries such as Google Tink provide envelope-encryption building blocks [@tink-envelope], while HashiCorp Vault's Transit engine provides centrally managed cryptographic operations and policy controls [@vault-transit]. Those systems are more mature and should be considered when their operational features are required.

CANIS-KMS was built as a small Java client-server system whose file envelope, wire protocol, persistence, and failure paths can be inspected together. That makes it a concrete subject for experiments on this particular filesystem-mediated workflow, but does not establish that a bespoke KMS is preferable to existing services. CANISP is an internal protocol, not a standard, and the current client/server transport mismatch limits the integrated workflow described here (see Software design).

# Software design

The repository has Maven modules for the server and Java client and targets Java 21. The server creates 2048-bit RSA key pairs. The official client generates a fresh 256-bit AES data key for each file, encrypts the file with AES-GCM using a 96-bit nonce and 128-bit authentication tag, and wraps the data key with RSA-OAEP using SHA-256 and MGF1-SHA-256. A versioned `CANISHYB1` envelope stores the wrapped key, nonce, ciphertext, and tag. The recipient sends only the wrapped key to CANIS-KMS; the server returns the recovered data key so the client can authenticate and decrypt the file locally. AES-GCM detects changes to ciphertext but does not authenticate the producer. The implementation reads whole files into memory, which bounds its suitability for large files.

The server stores RSA private-key material and metadata in a `CANISDB1` database encrypted with AES-GCM. The 128-bit database key is held separately in the externally configured `CANIS_SECRET_KEY` file. Existing database load failures stop startup instead of silently creating an empty replacement. Backups therefore need both files, protected separately. Key lifecycle is basic: a name can be created once, explicitly deleted, and replaced only by choosing another name; there is no automated rotation, expiry, or retention-aware deletion.

CANIS-KMS has a shared trust-domain model. The server authenticates configured usernames and passwords, and any authenticated client can request private-key operations for any stored key name. This is intentional for the current deployment model; the software does not implement per-service or per-key authorization. A compromised admitted client can therefore request decryption under any key. The authenticated client is also trusted to use returned data keys appropriately.

Transport is an operational limitation in the current repository. The server requires TLS 1.3 and a client certificate trusted by its configured truststore before it processes the CANISP login and commands. The Java client's `SocketClient` still uses a plaintext TCP socket and has no certificate or hostname validation. Consequently, that client cannot connect to the TLS-only server as currently implemented; deployment documentation calls for coordinating a client TLS migration. The design does not provide an operationally complete secure client-server deployment until that mismatch is resolved.

The CANISP/1 protocol is a line-oriented command request followed by a four-byte length-prefixed UTF-8 response. The server bounds command length, applies socket timeouts, and supports key creation, public-key retrieval, listing, deletion, and RSA decryption operations. Audit events are emitted through a dedicated logger for authentication, key operations, decrypt outcomes, and rejected commands. They are key-value formatted log messages, not a defined structured record schema or tamper-evident audit trail: there are no event identifiers, integrity chain, or external anchoring mechanism. Timestamp formatting comes from the general Logback pattern. Audit logging is therefore useful for operational inspection but does not establish verifiable history.

# Research impact statement

The repository provides source code, a threat model, CANISP documentation, deployment guidance, and Maven tests that exercise cryptographic envelopes, client command handling, server authentication, TLS rejection cases, persistence across restart, and database tampering. These materials support local inspection and regression experiments, including testing the consequence of a corrupted store or unavailable key service. The protocol and persistence formats make several design boundaries observable.

No published study, external adoption, integration deployment, or measured performance result is documented in the repository. The work's research relevance is therefore currently a testable engineering case study, not a demonstrated research impact. Before submission, the authors should provide evidence that the software is being used in a research context or otherwise establish the credible significance required by JOSS. Potential work on verifiable audit logs and asynchronous AI-assisted log analysis remains future research; neither is implemented or evaluated in this release.

# AI usage disclosure

OpenAI ChatGPT (GPT-6) was used to review repository materials and draft and edit this manuscript. It was not used to modify the software. A human author must check the final manuscript against the submitted release, edit it, and take responsibility for its claims; this draft has not yet received that approval. Any generative AI use in software development or other project documentation must be separately verified by the authors and disclosed if applicable.

# Acknowledgements

Author names, affiliations, and any financial or institutional support must be completed by the authors before submission. No funding information was established from the repository materials reviewed for this draft.

# References
