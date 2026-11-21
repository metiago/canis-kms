# CANIS threat model

CANIS is a key-recovery service for locally encrypted files. The official Java client fetches
public keys, encrypts files outside CANIS, and asks the server to unwrap file keys. CANIS stores
RSA private keys and key metadata; shared file storage does not need access to plaintext.

## Trust boundaries

- **Server and storage:** The CANIS process, host, encrypted `CANISDB1` database, and database
  master key are trusted. An attacker with both the database and `CANIS_SECRET_KEY` can recover
  stored private keys. Protect the key file and database with separate access controls and
  backups.
- **Network:** The server requires TLS 1.3 and a trusted client certificate before processing
  CANISP. The client validates the server certificate and hostname. A private network adds
  defense in depth.
- **Applications:** Clients authenticate with CANIS credentials after the TLS handshake. Every
  authenticated client can request decryption under every stored service key. A compromised
  client can therefore recover file keys across services. Decrypt audit events record the
  credential identity and key name, without key material.
- **Shared filesystems:** Encrypted files may be stored on untrusted shared storage; plaintext
  confidentiality depends on keeping their file envelopes encrypted at rest.

Applications should use the official `client` Maven artifact. CANISP is the internal
client/server protocol, documented for maintenance, debugging, research, and PoCs rather than as
an application API.

## What clients can do

`|set serviceName` creates a 2048-bit RSA key pair only if the name is unused. A duplicate
returns `ERROR: Service already exists` and preserves the existing pair. The
`|get-public serviceName` command exposes the public key for encrypting file keys.
`|decrypt serviceName payload` uses the private key on the server and returns the decrypted
payload, such as a file key; it never returns the RSA private key. `|del serviceName` removes
the active key.

Service names are identifiers validated by both the client and server. To rotate a key, create a
new service name and re-encrypt envelopes that must remain readable before deleting the old key.
Deletion cannot erase key material from JVM memory, backups, snapshots, or storage history. See
[RSA key lifecycle](rsa-key-lifecycle.md).

## Master key and recovery

`CANIS_SECRET_KEY` names the AES key file used to encrypt the database. CANIS validates and
loads it before opening the TLS listener; a missing, malformed, or insecurely accessible file
stops startup, even if the database is absent. The server does not generate a replacement key.
Changing the file after startup does not rotate the loaded key.

Normal startup rejects a missing database. A new database requires explicit initialization; a
valid key cannot be matched to a database that is absent. Database authentication and parsing
finish before the listener binds. If the database disappears or changes during operation, CANIS
enters a recovery-required state instead of creating an empty store. Restore the original
database with its matching master key from protected backups. See the
[master-key requirements](master-key.md).

Backups should contain the encrypted database and matching key file under separate access
controls, never plaintext private keys or decrypted file keys. Test restores by loading the
database and decrypting a known test envelope.

## Logs

`io.canis.audit` records authentication, key creation, access and deletion, decrypt success or
failure, and rejected commands. Events may include credential identities, key names, remote
addresses, and failure reasons. They must not contain raw credentials, private keys, encrypted
payloads, or decrypted payloads.
