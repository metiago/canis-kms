# Private-key exposure boundary

`KeyValueStore` owns generated and loaded RSA private keys. Its public `get` and `list` methods
return immutable `Entry` values containing only a service name and public key. The store
performs RSA-OAEP decryption internally; `ClientHandler` does not receive a `PrivateKey` or
PKCS#8 string. `Entry` is not Java-serializable.

CANISP `|get`, `|get-public`, and `|list` return public metadata. `|decrypt` returns the
decrypted payload over authenticated TLS, never the RSA private key. Key-processing failures use
fixed CANISP responses. Operational logs record validated service identifiers and failure
categories without private-key exception messages or stack traces. Failed authentication logs use
a generic identity instead of the client-supplied username; successful authentication and later
operations record the authenticated identity. Audit events record outcomes without key material. Malformed
service-credential configuration also returns a fixed error without echoing the configured value.

## API and compatibility

The public private-key conversion and file-loading helper was removed. Server-side callers of
the former `Entry` private-key methods, `KeyValueStore.set` or `create(String, Entry)`, and
`AsymmetricGenerator` must use the narrower store operations. The client artifact's unused
`Entry` metadata type likewise contains only name and public key, is not Java-serializable, and
no longer supports its former private-key component.

The encrypted `CANISDB1` record layout and `CANISGCM1` encryption have not changed. Valid
databases load and rewrite without migration, and CANISP command names and response formats
remain the same; the official client needs no update. Startup rejects malformed UTF-8, invalid
service names, malformed RSA encodings, and public/private fields that do not match, without
rewriting the rejected database. Restore a valid backup if this occurs. A database write failure
is reported as an operation failure.

## Remaining limits

This boundary covers supported Java APIs, CANISP output, logs, and exceptions. Code with
arbitrary execution inside the server JVM is outside it. Java key objects and immutable strings
cannot be reliably erased from memory, and prior encrypted database copies may persist in
backups or snapshots. Any authenticated client can request `|decrypt` under any stored service
key; per-service authorization is not yet available.

The deployment [Trusted Computing Base](threat-model.md#trust-assumptions-and-trusted-computing-base)
includes the operating system, host administrators, deployment automation, service account, and
corporate identity systems. This API boundary protects against accidental exposure and access
by non-privileged users or applications; it does not prevent a TCB member from inspecting the
JVM, key files, or backups.

Database writes use two atomic moves with a recovery file, not a single atomic transaction.
Crash durability still needs platform-specific validation. Structured audit schema, integrity
verification, and key rotation remain separate work. Master-key and explicit database
initialization rules are covered in [master key](master-key.md). Changes to serialization or
logging need renewed private-key exposure checks.

`KeyValueStoreTest` checks public metadata, non-serializability, decryption, malformed-key and
save failures, tampering, and loading and rewriting an independently encoded legacy database.
`ClientHandlerTest` checks CANISP responses and operational and audit logs against an injected
exception containing a generated private-key encoding. `ServerEndToEndTest` covers the official
client over mutual TLS, restart, and deletion; TLS suites cover invalid certificates and
credentials. `ServerTlsSecurityTest` also checks that failed login input containing private-key-like
text, long values, control characters, and log-injection text does not enter operational or audit logs.
