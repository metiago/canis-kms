# RSA key lifecycle review

This document describes the current implementation of Issue #5. It records observed behavior and
the limits of its guarantees. The on-disk `CANISDB1` format and the CANISP commands are unchanged.

## Creation

After mutual TLS and CANIS credential authentication, `|set serviceName` validates the service
name and checks whether it already exists. `KeyValueStore.create` uses the JDK provider to create a
2048-bit RSA key pair. The public key is Base64-encoded X.509 SubjectPublicKeyInfo; the private key
is Base64-encoded PKCS#8 and retained in a private store record. The public `Entry` type contains
only service name and public key. `KeyValueStore.create` checks for duplicates again under a write
lock, preventing concurrent requests from replacing an existing entry. A losing concurrent
request may still generate a pair that is discarded.

## Persistence and loading

Each create or delete operation calls `saveToFile`. The store serializes `CANISDB1`, an entry count,
and each entry's service name, name, public key, and private key. `SymmetricGenerator` encrypts this
payload with AES-GCM under the key loaded from the file named by `CANIS_SECRET_KEY`. The resulting
`CANISGCM1` envelope is written to the configured database file.

At startup, `KeyValueStore` reads an existing database, authenticates and decrypts the AES-GCM
envelope, checks the `CANISDB1` marker, and reconstructs entries. A missing or wrong master key,
corrupt encrypted data, or invalid store marker prevents loading. The loader does not currently
parse each reconstructed RSA key or verify that its public and private parts match. If the database
file is absent, it starts with an empty map, even if an older database was expected.

The database and `CANIS_SECRET_KEY` file are both required for recovery. Back them up separately
with access controls, restore the matching pair, and verify that a known encrypted file can be
decrypted before serving clients. If either file is unexpectedly missing, stop and investigate;
issuing `|set` could create new key material that cannot decrypt earlier file envelopes.

## Use

`|get` and `|list` expose the name and public key; `|get-public` exposes only the public key. The
official client creates a fresh AES file key, encrypts the file with AES-GCM, and wraps the file key
with RSA-OAEP using SHA-256 and MGF1-SHA-256. The wrapped key is stored in a `CANISHYB1` envelope.
For decryption, the client sends the wrapped file key through `|decrypt`. The store reconstructs
the RSA private key internally from the stored PKCS#8 string, unwraps the file key, and the handler
returns that file key over TLS. It does not send the RSA private key. The current access model permits any authenticated
CANIS client to request a decrypt operation for any stored service key.

## Deletion and destruction limits

`|del serviceName` removes the entry from the active map and rewrites the encrypted database. The
current implementation reports success even if the name was absent. Deletion is logical: immutable
Java strings and key objects are not reliably zeroized, and earlier database copies may remain in
backups, snapshots, or storage history. Deleting a key makes existing envelopes that depend on it
unreadable through the active store. Preserve old keys until required envelopes are re-encrypted or
no longer needed; rotation and revocation policy belong to Issue #9.

## Review findings and follow-up

| Finding | Effect | Follow-up |
| --- | --- | --- |
| `saveToFile` writes directly to the database file. Issue #6 now propagates write failure and rolls back the in-memory mutation. | An interrupted write can still damage the database file. | Make persistence atomic while preserving `CANISDB1` compatibility. |
| A missing database starts as empty; a missing master key may be generated on a later save. | Loss of files can be mistaken for a new installation. | Require explicit initialization and fail safely on ambiguous state (Issues #7 and #8). |
| Server-side Java APIs previously exposed private-key strings through `Entry` and `AsymmetricGenerator`. | Issue #6 closes this export path for supported APIs: `Entry` contains public metadata, and the store performs key generation and decryption internally. | Keep the API boundary and output-path tests in place; review new serializers and logs. |
| Private-key strings and historical encrypted database copies cannot be reliably erased by `|del`. | Logical deletion does not guarantee physical destruction. | Define retention, rotation, revocation, and backup policies (Issues #9 and #10). |

Issue #5 documented the original behaviors. Issue #6 changed the Java API boundary and error
handling; the other follow-up risks still require separate work.

## Verification

`ServerEndToEndTest` exercises create, file encryption, server restart, decryption with the
reloaded RSA key, deletion, and another restart over mutual TLS. Existing `KeyValueStoreTest`
cases check the versioned encrypted format and rejection of missing or wrong master keys and
corrupt data. Other security suites cover invalid certificates, invalid credentials, and database
tampering. These tests demonstrate logical deletion and restart recovery, not secure physical
erasure or durability under an injected write failure.
