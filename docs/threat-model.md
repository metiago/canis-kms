# CANIS Threat Model

CANIS is an internal key-recovery service for locally encrypted files. The official Java client fetches public keys, encrypts files locally, and sends file keys to the server for decryption. CANIS stores RSA private keys and metadata; shared file storage never accesses plaintext.

## Scope & Security Objectives

CANIS is built for single-organization corporate environments. It is **not** a public cloud KMS, multi-tenant service, HSM, or Vault as a Service.

Its controls are designed to:
- Prevent accidental leaks of cryptographic material.
- Catch unsafe deployment or configuration errors.
- Block unauthorized access from non-privileged users and apps.
- Fail closed if required key material cannot be trusted.

*Note: These goals do not provide isolation between admitted CANIS clients.*

## Trusted Computing Base (TCB)

CANIS trusts the host OS, infrastructure administrators, deployment automation, the CANIS service account, and corporate IAM/PKI systems to act according to company policy.

**Deployment requirements:**
- Run on a managed host within a private corporate network.
- Use a dedicated service account and restricted access controls for keys, the database, and backups.
- Keep all parent directories of the master key and database under trusted administrative control.
- Admit only applications trusted to share the same key namespace.

## Out-of-Scope Threats

CANIS does not protect against:
- Compromised OS, deployment pipelines, or root/Administrator access.
- Memory extraction attacks (no HSM-level isolation).
- Multi-tenant or zero-trust environments.
- Compromised clients using valid credentials to access keys in the shared namespace.

## Trust Boundaries

- **Server & Storage:** Located inside the TCB. Anyone who obtains both the `CANISDB1` database and the master-key file can recover all stored private keys. Store and back them up under separate access controls.
- **Network:** Requires TLS 1.3 with mutual certificate validation (client and server) before processing the internal CANISP protocol. Restrict network access via corporate firewall/network rules.
- **Applications:** Clients authenticate after the TLS handshake. All authenticated clients share access to all stored keys. Use the official `client` Maven artifact rather than calling CANISP directly.
- **Shared Filesystems:** Safe on untrusted storage as long as file envelopes remain encrypted at rest.

## Client Admission & Access Control

CANIS uses a **shared trust domain**. Valid TLS certificates and credentials grant access to the service, not to specific keys.
- Service names are identifiers, not security boundaries.
- Any admitted client can read public keys, decrypt payloads, or delete **any** stored key.
- If applications require isolated key permissions, deploy separate CANIS instances.

## Client Commands

- `|set serviceName`: Creates a 2048-bit RSA key pair if the name is unused (fails if it already exists).
- `|get-public serviceName`: Returns the public key for encrypting file keys.
- `|decrypt serviceName payload`: Decrypts a payload (e.g., a file key) using the server-side private key. Never exposes the private key.
- `|del serviceName`: Deletes the active key.

**Key Rotation & Deletion:** To rotate a key, create a new service name, re-encrypt active envelopes, and delete the old key. Deleting a key does not purge it from JVM memory, backups, snapshots, or disk history.

## Master Key & Recovery

- **Master Key (`CANIS_SECRET_KEY`):** Points to the AES key file that encrypts the database. CANIS validates and loads it before opening the TLS listener. Startup aborts if the file is missing, malformed, a symlink, or has insecure permissions on the file or its immediate parent directory.
- **Database Startup & Failure:** CANIS requires an existing, explicitly initialized database on startup. If the database goes missing or changes at runtime, CANIS enters a recovery-required state instead of creating a blank store.
- **Backups:** Back up the encrypted database and master key under separate access controls. Never back up plaintext keys. Verify backups by loading them and decrypting a test envelope.

## Audit Logs

The `io.canis.audit` logger records authentication attempts, key operations, decrypt outcomes, and rejected commands.
- **Logged:** Authenticated credential identity (generic label on failed logins), validated key names, remote IP addresses, and fixed failure reasons.
- **Never Logged:** Passwords, raw login commands, private keys, or encrypted/decrypted payloads.
