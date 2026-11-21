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

The software contribution of CANIS is an inspectable, reproducible Java implementation for studying trust boundaries across the protocol, encrypted persistence, and key recovery in asynchronous encrypted-file exchange.

In a shared-storage integration, a producer may finish before the recipient starts. The recipient must then obtain the encrypted file and its data key without depending on a live producer. CANIS brings the file client, key-recovery protocol and service, and encrypted key database into one Java codebase. For controlled integrations whose services already share a trust perimeter, its shared-admission policy keeps service access rules simple and makes cross-service key recovery explicit. The [separate-process workflow](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/code02-method.md) reproduces delayed exchange with generated fixtures, and the [shared-trust study](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss01-results.md) tests the resulting admission boundary. Together, they let engineers examine whether per-key ACLs or an external KMS are needed for a particular integration.

The design draws on the author's account of a predecessor used in sensitive financial-system integration; this paper studies the current open-source implementation, whose deployment has not been documented. CANIS serves researchers and software or security engineers studying file-based integration, service dependence, and access-control trade-offs.

# State of the field

Google Tink documents client-side envelope encryption with an external KMS [@tink-envelope]. HashiCorp Vault Transit provides envelope-encryption operations with named-key policies [@vault-transit]. These tools address encryption and key-service needs. CANIS supplies a bounded research artifact for examining how a later Java service recovers a file key under a deliberately shared admission policy. Its file client, CANISP protocol and server, file envelope, and encrypted persistence can be examined and modified together. This makes the asynchronous workflow and access boundary reproducible in one implementation. CANIS uses established AES-GCM and RSA-OAEP mechanisms [@dworkin2007; @moriarty2016]; it introduces no new cryptographic primitive. The completed studies do not compare CANIS with Tink, Vault, or another KMS.

# Software design

CANIS provides an end-to-end Java stack: the CANISP wire protocol, TLS 1.3 transport with mutual certificate authentication, local hybrid file encryption, encrypted key persistence, and automated separate-process test workflows. The client obtains a server-generated RSA public key, generates an AES-GCM data key, encrypts the file locally, and wraps the data key with RSA-OAEP. It publishes the `CANISHYB1` envelope to shared storage; a later client sends the wrapped key to CANISP and decrypts the file locally with the recovered data key. The [protocol guide](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/canisp-protocol.md) specifies this exchange and file format. File contents stay outside the key-service connection, and the envelope authenticates encrypted content without establishing the producer's identity.

The client/server path validates certificates and configured credentials. The server stores RSA key material in an AES-GCM-encrypted `CANISDB1` database under an externally supplied master key. The implementation lets readers trace admission, logged key recovery, and persistence across the client, protocol, and server without an external KMS in the workflow. Its shared-access scope includes key-pair creation and deletion, with no per-key or per-service authorization, managed rotation, revocation of recovered data keys, or built-in audit-integrity verifier. Whole-file client buffering bounds the studied workload, and crash-atomic database replacement remains unproven. These [release boundaries](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss02-release-boundary-final.md) define further research and engineering work.

# Research impact statement

Using CANIS's generated fixtures and automated workflows, the author demonstrated delayed, byte-matched recovery after encrypted publication by independent producer, server, and recipient processes in a [separate-process exchange](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/code02-results.md). A [focused test](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/joss01-results.md) showed that a second admitted credential identity could recover the recipient's file while an invalid password was rejected. Because the clients shared one certificate, this result concerns credential-level access under the intentional shared-access policy. The [study synthesis](https://github.com/metiago/canis-kms/blob/v0.1.0-rc.1/docs/research/issue35-results.md) brings together bounded resource and transport observations, multi-service scenarios, and selected storage and audit faults.

Selected damaged database or master-key states prevented restart until matching material was restored, and external hashes detected changes to audit lines. CANIS has no built-in audit-integrity verifier. Transport measurements came from a clean fixed revision; several other studies used synthetic fixtures on dirty Windows working trees. The original local-envelope controls and scheduled service-outage series remain unrun. The studies describe these workflows and faults under the stated conditions; general scalability, availability guarantees, production readiness, cryptographic strength, comparative advantage, and external adoption remain outside their scope.

# AI usage disclosure

OpenAI Codex assisted with code implementation and English grammar review. The author reviewed, edited, and validated all AI-assisted code. Decisions regarding software design, research design, and publication remained solely with the author, who takes full responsibility for the final software, documentation, experiments, and submission.

# Acknowledgements

The author received no funding or institutional support and declares no conflicts of interest.

# References
