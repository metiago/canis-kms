# Private-key exposure boundary (Issue #6)

## Architecture and security behavior

`KeyValueStore` owns generated and loaded RSA private-key material. Its public `get` and `list`
methods return immutable `Entry` metadata containing only a service name and public key. The
store performs RSA-OAEP decryption internally; `ClientHandler` never receives a `PrivateKey` or
PKCS#8 string. The former public private-key conversion and file-loading helper was removed.
`Entry` is not Java-serializable. CANISP `|get`, `|get-public`, and `|list` continue to return
public metadata only, and `|decrypt` returns the decrypted payload over authenticated TLS.

Key-processing errors use fixed CANISP responses. Operational logs record failure categories and
safe service identifiers, without exception messages or stack traces from private-key processing.
Audit events record operation outcomes without private-key material. These restrictions cover the
supported Java API, CANISP output, logs, and exceptions; code with arbitrary execution inside the
server JVM is outside this API boundary.

Malformed service-credential configuration also produces a fixed error without echoing the
configured value, preventing a startup exception from printing a password.

## Operational and compatibility impact

The encrypted `CANISDB1` record layout and `CANISGCM1` encryption remain unchanged. Existing valid
databases can be loaded and rewritten without migration. Startup rejects malformed UTF-8, invalid
service names, malformed RSA encodings, and public/private fields that do not form a matching pair;
it does not rewrite the rejected database. Operators must restore a valid database from backup.
CANISP command names and response formats
remain unchanged, so the official client needs no update. Server-side callers of the former
`Entry` private-key methods, `KeyValueStore.set` or `create(String, Entry)`, and
`AsymmetricGenerator` must migrate to the narrow store operations. A database write failure is
reported as an operation failure instead of being acknowledged as success.

## Verification

`KeyValueStoreTest` verifies the public metadata boundary, non-serializability, decryption,
rejection of malformed keys, failure to save, database tampering, and loading and rewriting an
independently encoded legacy database. `ClientHandlerTest` verifies CANISP responses and captures
both operational and audit logs while an injected exception contains a generated private-key
encoding. `ServerEndToEndTest` exercises the official client over mutual TLS, including restart
and deletion. Existing TLS security suites cover invalid certificates and credentials.

## Remaining risks and future improvements

Java key objects and immutable strings cannot be reliably erased from memory. Earlier encrypted
database copies may remain in backups or snapshots. Any authenticated CANIS client can currently
request `|decrypt` with any stored service key; per-service authorization remains to be designed.
The master-key and explicit database initialization rules are covered in Issues #7 and #8. Database
writes use two atomic moves with a recovery file; the sequence is not one atomic transaction, and
crash durability still needs platform-specific validation.
Structured audit schema and integrity verification, and key rotation remain separate follow-up
work. The client artifact's unused `Entry` metadata type now contains only the name and public key
and is not Java-serializable; callers that used its removed private-key component must update.
Changes to serialization or logging need renewed private-key exposure tests.
