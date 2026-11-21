# RSA key lifecycle

CANIS keeps RSA private keys inside `KeyValueStore`. The `CANISDB1` store format and CANISP
commands remain unchanged.

## Create

After mutual TLS and CANIS credential authentication, `|set serviceName` validates the name and
creates a 2048-bit RSA key pair with the JDK provider if the name is unused. The public key is
Base64-encoded X.509 SubjectPublicKeyInfo; the private key is Base64-encoded PKCS#8. Public
`Entry` metadata contains only the name and public key.

Creation checks for duplicates again under a write lock, so concurrent requests cannot replace
an existing pair. A losing request may generate a pair before discarding it. A duplicate command
returns `ERROR: Service already exists`.

## Store and load

Create and delete operations save the store. The plaintext `CANISDB1` structure contains a
marker, entry count, and each entry's service name, name, public key, and private key.
`SymmetricGenerator` encrypts it with AES-GCM using the key from `CANIS_SECRET_KEY`; the
database file contains a `CANISGCM1` envelope.

At startup, CANIS validates the master-key file, authenticates and decrypts the database, checks
the `CANISDB1` marker, and reconstructs entries. Loading rejects a wrong key, corrupt
ciphertext, malformed UTF-8, invalid service names, malformed or mismatched RSA keys, duplicate
records, oversized fields, and trailing data. A missing database rejects normal startup and
requires explicit `--init-db` for a new installation. See the
[master-key requirements](master-key.md).

Recovery needs the encrypted database and its matching key file. Back them up separately with
restricted access. If either disappears unexpectedly, investigate and restore the matching pair;
do not initialize a replacement database. Verify a known encrypted file after restoration.

## Use

`|get` and `|list` return names and public keys; `|get-public` returns only the public key. The
official client generates a fresh AES file key, encrypts the file with AES-GCM, wraps the file
key with RSA-OAEP using SHA-256 and MGF1-SHA-256, and writes a `CANISHYB1` envelope.

For decryption, the client sends the wrapped file key with `|decrypt`. The store reconstructs
the RSA private key internally from PKCS#8, unwraps the file key, and returns that file key over
TLS. It does not send the private key. Any authenticated CANIS client can request decryption
under any stored service key.

## Delete and rotate

`|del serviceName` removes the entry from the active map and rewrites the encrypted database. It
reports success even when the name is absent. Deletion makes dependent envelopes unreadable
through the active store, but it is logical deletion: immutable Java strings and key objects
cannot be reliably zeroized, and old encrypted database copies may remain in backups, snapshots,
or storage history.

To rotate, create a new service name and re-encrypt envelopes that must remain readable before
deleting the old key. Retention, rotation, revocation, and backup policy remain operator
responsibilities.

## Operational limits and checks

Database writes stage and force encrypted bytes before replacing the file through two atomic
moves. Filesystems without atomic-move support reject writes; directory metadata sync,
interrupted moves, and power-loss durability need platform-specific validation. A failed write
is reported as failure rather than success.

`ServerEndToEndTest` covers creation, local file encryption, restart, decryption with a reloaded
key, deletion, and another restart over mutual TLS. `KeyValueStoreTest` covers the encrypted
format and rejection of missing or wrong master keys and corrupt data. Other security suites
cover invalid certificates, invalid credentials, and database tampering. These tests establish
logical deletion and restart recovery, not physical erasure or durability under an injected
write failure. Keep private-key output, serializer, and logging checks in place when changing
the store API.
