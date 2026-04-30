# CANIS Threat Model

CANIS is a database-style KMS service accessed through the official Java client library. It stores
key metadata and private keys server-side, exposes public keys for encryption, and performs private
key operations on behalf of authenticated clients.

## Trust Boundaries

- Applications should use the official `client` Maven artifact instead of writing raw socket
  messages.
- The CANIS server process, its host, and its encrypted database file are inside the trusted
  boundary.
- The TCP network between applications and CANIS is outside the process boundary. Deployments should
  place CANIS on a trusted private network or behind transport protection such as TLS termination or
  a service mesh.
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

- `|set serviceName` creates a new RSA key pair for the named service and persists it in the CANIS
  store.
- `|get-public serviceName` returns the public key used by clients to encrypt file keys.
- `|decrypt serviceName payload` performs a server-side private-key operation.
- `|del serviceName` removes the stored key entry.
- Rotation should be handled by creating a new service key name or replacing the existing key and
  re-encrypting file envelopes that still need to be readable.

## Secret Key Storage

- `CANIS_SECRET_KEY` points to the AES key file used to encrypt the CANIS database.
- The secret key file must be protected by host filesystem permissions and backed up separately from
  the encrypted database.
- Losing the secret key file makes the encrypted CANIS store unreadable.
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
