---
title: 'CANIS: Key Management for Asynchronous Encrypted File Exchange Between Java Services'
tags:
  - key management
  - cryptography
  - distributed systems
  - file encryption
authors:
  - name: 'Tiago Souza'
    affiliation: 1
affiliations:
  - name: 'Independent Researcher, Brazil'
    index: 1
bibliography: paper.bib
---

# Summary

CANIS is a Java key-recovery service and client for asynchronous encrypted-file exchange. A producer encrypts a file locally and publishes it to shared storage. A recipient can later recover the data key through a TLS-protected service and decrypt the file locally; file contents do not pass through the service. The released source [@canis-rc1] includes the client, server, protocol, file format, encrypted persistence, and methods for reproducing the exchange. CANIS gives admitted clients shared access to stored key names, making this trust boundary explicit and open to study.

# Statement of need

In a shared-storage integration, a producer may finish before the recipient starts. The recipient must then obtain both the encrypted file and its data key without depending on a live producer. This raises practical questions about where file bytes travel, how clients recover keys, and which clients may access them. CANIS brings these decisions into one Java codebase, with a file client, key-recovery protocol and service, and encrypted key database. Its [separate-process workflow](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/code02-method.md) can be reproduced with generated fixtures, while the [shared-trust study](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss01-results.md) shows the consequences of client admission.

The author's account of a predecessor used in sensitive financial-system integration motivates the work, but does not establish deployment of this revision. CANIS is intended for researchers and software or security engineers studying file-based integration, service dependence, and access-control trade-offs.

# State of the field

Google Tink documents client-side envelope encryption with an external KMS [@tink-envelope]. HashiCorp Vault Transit provides envelope-encryption operations with named-key policies [@vault-transit]. These tools address encryption and key-service needs; CANIS supplies a bounded research artifact for a different question: what happens when a later Java service recovers a file key under a deliberately shared admission policy? Its file client, CANISP protocol and server, file envelope, and encrypted persistence can be examined and modified together. This makes the asynchronous workflow and access boundary reproducible in one implementation. CANIS uses established AES-GCM and RSA-OAEP mechanisms [@dworkin2007; @moriarty2016]; it introduces no new cryptographic primitive. The completed studies do not compare CANIS with Tink, Vault, or another KMS.

# Software design

The client obtains a server-generated RSA public key, generates an AES-GCM data key, encrypts the file locally, and wraps the data key with RSA-OAEP. It publishes the resulting `CANISHYB1` envelope to shared storage. A later client sends the wrapped key to the CANISP service and uses the recovered data key to decrypt the file locally. The [protocol guide](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/canisp-protocol.md) specifies the exchange and file format. Keeping file contents outside the key-service connection limits the service's role in the data path, while returning a data key to an admitted client makes admission a consequential trust decision. The envelope authenticates encrypted content, but does not establish the producer's identity.

The supported client/server path uses TLS 1.3, certificate validation, and configured credentials. The server stores RSA key material in an AES-GCM-encrypted `CANISDB1` database with an externally supplied master key. The released implementation lets readers trace client admission, key recovery, and persistence across the protocol, client, and server. It supports creation and deletion of stored key pairs, but admitted clients have no per-key or per-service authorization. It has no managed rotation, no revocation of data keys already recovered by clients, and no built-in audit-integrity verifier. Whole-file client buffering limits the studied workload, and database replacement has no proven crash-atomicity guarantee. These [release boundaries](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss02-release-boundary-final.md) constrain its use.

# Research impact statement

The author used CANIS to study shared-trust recovery and delayed file exchange. In a [focused test](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss01-results.md), a second admitted credential identity recovered a recipient's file, while an invalid password was rejected. The clients shared one certificate, so this finding concerns credential-level access. Recovery by the second identity follows the documented shared-access policy and illustrates why admission matters for file exchange. A [separate-process exchange](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/code02-results.md) demonstrated later, byte-matched recovery after encrypted publication. The [study synthesis](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/issue35-results.md) records bounded resource and transport observations, multi-service scenarios, and selected storage and audit faults.

Selected damaged database or master-key states prevented restart until matching material was restored; changes to audit lines were detectable through external hashes, but CANIS has no built-in verifier. The transport measurements used a clean fixed revision, whereas several other studies used synthetic fixtures on dirty Windows working trees. The original local-envelope controls and scheduled service-outage series were not run. The evidence does not establish external adoption of this release, general scalability, an availability guarantee, production readiness, cryptographic strength, or comparative advantage.

# AI usage disclosure

OpenAI Codex assisted with code implementation and English grammar review. The author reviewed, edited, and validated all AI-assisted code. Decisions regarding software design, research design, and publication remained solely with the author, who takes full responsibility for the final software, documentation, experiments, and submission.

# Acknowledgements

The author received no funding or institutional support and declares no conflicts of interest.

# References
