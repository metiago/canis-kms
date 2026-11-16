# CANIS Threat Model

CANIS is a database-style KMS service accessed through the official Java client library. It stores
key metadata and private keys server-side, exposes public keys for encryption, and performs private
key operations on behalf of authenticated clients.

## Trust Boundaries

- Applications should use the official `client` Maven artifact instead of writing raw socket
  messages.
- CANISP is an internal client/server protocol, similar to the wire protocol beneath a database
  driver. It is documented for maintainers, debugging, research, and PoCs, but it is not the
  application-facing API.
- The CANIS server process, its host, and its encrypted database file are inside the trusted
  boundary.
- The network between applications and CANIS is outside the process boundary. The server requires
  TLS 1.3 and a trusted client certificate before accepting CANISP commands. Clients must validate
  the server certificate and hostname. A private network remains useful defense in depth.
- Shared filesystems that store encrypted files are untrusted for plaintext confidentiality. Files
  must remain encrypted at rest.

## Authentication And Access Model

- Clients authenticate at connection setup with configured CANIS credentials.
- CANIS uses an authenticated shared-access model: any authenticated client can request decrypt
  operations with stored keys.
- Private keys are never returned to clients. CANIS performs RSA-OAEP unwrap/decrypt operations on
  the server and returns only decrypted payloads such as file data keys.
- Future policy support may add per-key restrictions, but the default model is shared access for
  authenticated applications.

## Key Lifecycle

- `|set serviceName` creates a new RSA key pair only when the service name is unused. A duplicate
  request returns `ERROR: Service already exists` and preserves the existing key pair. To rotate,
  create a new service name and re-encrypt file envelopes that still need to be readable before
  deleting the old service key.
- `|get-public serviceName` returns the public key used by clients to encrypt file keys.
- `|decrypt serviceName payload` performs a server-side private-key operation.
- `|del serviceName` removes the key from the active store. It does not guarantee erasure from
  memory, backups, snapshots, or storage history. See [RSA key lifecycle](rsa-key-lifecycle.md).
- Service names should be treated as identifiers and validated by both the official client and the
  server before command execution.

## Secret Key Storage

- `CANIS_SECRET_KEY` points to the AES key file used to encrypt the CANIS database.
- The secret key file must be protected by host filesystem permissions and backed up separately from
  the encrypted database. See [master-key requirements](master-key.md).
- A missing, malformed, or insecurely accessible key file prevents startup even when the database
  is absent. CANIS will not generate a replacement key. Restore the original key file from a
  protected backup when a database already exists.
- The key is loaded once at startup. A later file replacement does not rotate the running server's
  key. A valid key cannot be matched to an absent database; Issue #8 covers that ambiguity.
- Compromise of both the encrypted database and `CANIS_SECRET_KEY` compromises stored private keys.

## Persistence And Backups

- CANIS persists key metadata in an encrypted `CANISDB1` store.
- Backups must include both the encrypted database and the secret key file, stored with separate
  access controls.
- Restore tests should verify that CANIS can load the database and decrypt a known test envelope.
- Backups should not include plaintext private keys or decrypted file keys.

## Audit Logging

- CANIS logs authentication, key creation, key access, key deletion, decrypt success/failure, and
  rejected commands through `io.canis.audit`.
- Audit logs must not contain raw credentials, private keys, encrypted payloads, or decrypted
  payloads.
- Audit logs may contain service identities, key names, remote addresses, and failure reasons.
