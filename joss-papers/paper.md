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
bibliography: paper.bib
---

# Summary

CANIS-KMS is a Java key management service for applications that exchange sensitive files through shared storage. A producer encrypts a file locally for a named recipient key. Later, an authenticated client asks CANIS-KMS to recover the file's data key; the encrypted file can remain in shared storage, and its contents do not pass through the key service. This arrangement supports workflows in which services write and process files at different times without distributing private key files among application hosts. Under the current shared-access model, any admitted client may request data-key recovery by key name.

# Statement of need

The motivating scenario is asynchronous file exchange among Java services through shared storage. The repository does not establish a deployed workflow, processing window, or retention policy, and CANIS-KMS does not enforce file deletion or expiry. The present research question is how admission to a shared key service affects which authenticated clients can recover files addressed to another service's key. This question has been exercised with synthetic data, but a broader research need and actual use by researchers remain to be established.

Filesystem permissions can control which processes access a shared directory, but they do not encrypt its contents. Copying recipient private keys to every service that may process a file also expands the number of hosts on which those keys must be protected. CANIS-KMS keeps RSA private keys in a central, encrypted database and leaves bulk file encryption to the producer. The intended users are developers and researchers examining encrypted, file-based service integration, including the operational consequences of delayed processing, key-service outages, and shared trust assumptions.

# State of the field

The software uses established cryptographic primitives rather than proposing a new encryption method [@dworkin2007; @moriarty2016]. Libraries such as Google Tink provide envelope-encryption building blocks [@tink-envelope], while HashiCorp Vault's Transit engine provides centrally managed cryptographic operations and policy controls [@vault-transit].

CANIS-KMS combines a Java file client, key service, wire protocol, and encrypted persistence in one inspectable codebase. This design allows a local test to trace a file from encryption through key recovery without configuring a separate KMS product; that is a reason for studying this implementation, not evidence that building a new KMS was necessary or preferable. CANISP is an internal protocol, not a standard. Tink's documented envelope-encryption primitive calls an external KMS; this study did not assess any access policy configured with Tink. Vault Transit also supports envelope encryption and named-key access policies; CANIS's current model instead grants admitted clients shared access to stored keys. The repository has no comparative experiment establishing a security or performance advantage.

# Software design

The repository has Maven modules for the server and Java client and targets Java 21. The design keeps bulk file processing in the client: for a file, the server unwraps only its data key, so file contents need not traverse the key service in the official workflow. The server creates 2048-bit RSA key pairs. The client generates a fresh 256-bit AES data key for each file, encrypts with AES-GCM using a 96-bit nonce and 128-bit authentication tag, and wraps the data key with RSA-OAEP using SHA-256 and MGF1-SHA-256. A versioned `CANISHYB1` envelope stores the wrapped key, nonce, ciphertext, and tag. To decrypt, the client sends only the wrapped key to CANIS-KMS and uses the returned data key locally. This choice makes decryption depend on an available key service and entrusts the authenticated client with the recovered data key. AES-GCM detects changes to ciphertext but does not authenticate the producer. Whole-file reads into memory limit suitability for large files; no size or throughput limit has been measured.

The server stores RSA private-key material and metadata in a `CANISDB1` database encrypted with AES-GCM. Its AES database key is held separately in the externally configured `CANIS_SECRET_KEY` file; the loader accepts 128-, 192-, or 256-bit keys. Separating these files requires protecting and backing up both for recovery. Normal startup rejects a missing or invalid database instead of initializing an empty replacement. A known check-to-move race in database writes prevents a broader claim that runtime database loss can never cause recreation. The loader also does not verify that authenticated stored public and private key fields form a matching RSA pair. Key lifecycle is basic: a name can be created once, explicitly deleted, and replaced only by choosing another name; there is no automated rotation, expiry, or retention-aware deletion.

CANIS-KMS has a documented shared trust-domain model: TLS client-certificate validation and configured username/password authentication admit clients, after which any authenticated client can request private-key operations for any stored key name. Key names select cryptographic material, not per-service access rights. The software does not implement per-service or per-key authorization. An admitted client with access to another service's encrypted file can therefore request decryption under its key; this is a consequence of the chosen trust boundary, including when an admitted client is compromised. The authenticated client is also trusted to use returned data keys appropriately.

The server requires TLS 1.3 and a client certificate trusted by its configured truststore before it processes CANISP login and commands. The official Java client uses TLS 1.3 with externally supplied identity and trust stores, validates the server certificate and hostname, and has no plaintext fallback. The official-client workflow and negative transport cases passed in a Windows 11 working-tree run documented in `docs/research/joss05-results.md`. A fixed release has not yet been verified.

CANISP/1 uses line-oriented requests and length-prefixed responses, making the request path inspectable; the server bounds command length and applies socket timeouts. Audit messages cover authentication, key operations, decrypt outcomes, and rejected commands. They are key-value log lines, without the required event identifiers, defined structured schema, or integrity verification. Verifiable audit history remains planned work, not a capability evaluated here.

# Research impact statement

The repository provides source code, a threat model, CANISP documentation, deployment guidance, and Maven tests for envelopes, authentication, TLS rejection, persistence, and database tampering. A documented synthetic-data test of the shared trust boundary, rerun for this manuscript revision, found that a second authenticated credential identity could decrypt a file encrypted under another service's key, while an altered password was rejected. The outcome matches the documented shared-access model; it is not a bypass of an implemented per-key policy. The test used one client certificate for all credential identities and does not establish certificate-bound service separation. A separate working-tree reviewer example passed a producer-to-shared-envelope-to-recipient byte comparison on Windows and rejected a new connection after server shutdown; its TLS server ran in a test thread. The methods, commands, environment, results, and limitations are recorded under `docs/research/`. Neither test establishes a deployed research workflow or a measured reliability result.

No published study, external adoption, research integration deployment, or measured performance result is documented in the repository. The current evidence supports a reproducible engineering case study, not demonstrated research use. Before submission, the authors must document actual research use and establish why this software is needed for that work. The project has not yet established a JOSS research-impact case.

# AI usage disclosure

OpenAI ChatGPT (GPT-6) was used to review repository materials, draft and edit this manuscript, assist with a research test and its scripts, and prepare the JOSS-02 and JOSS-05 claim audits and verification documentation. A human author must review and validate these AI-assisted materials, confirm the full history of generative AI use across the project, and take responsibility for the final submission. This draft has not yet received that approval.

# Acknowledgements

Author names, affiliations, and any financial or institutional support must be completed by the authors before submission. No funding information was established from the repository materials reviewed for this draft.

# References
