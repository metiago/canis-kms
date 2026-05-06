# Backlog

## High

### Prevent silent key rotation on duplicate `|set`

Re-running `|set` on an existing service currently overwrites the stored keypair without warning.
`ClientHandler` accepts every `|set` as success and always generates a fresh RSA keypair before
persisting it. That can strand already-encrypted data, because files wrapped with the old public key
become undecryptable as soon as the old private key is replaced.

Relevant code:
- `server/src/main/java/io/canis/handlers/ClientHandler.java`
- `docs/threat-model.md`

Follow-up:
- Decide whether `|set` means create-only or rotate.
- If create-only, reject duplicates with an explicit error.
- If rotation is supported, make it an explicit operation with clear migration semantics.

### Fail fast when the database exists but `CANIS_SECRET_KEY` is missing or wrong

When the configured secret-key file is missing, `SymmetricGenerator` creates a brand-new AES key
file automatically. If an encrypted database already exists, `KeyValueStore` then fails to decrypt
it, logs the error, and continues with an empty in-memory store. That hides a fatal recovery
problem and allows the server to come up looking healthy while effectively losing access to all
existing keys.

Relevant code:
- `server/src/main/java/io/canis/crypto/SymmetricGenerator.java`
- `server/src/main/java/io/canis/store/KeyValueStore.java`
- `docs/threat-model.md`

Follow-up:
- Treat “database exists but secret key is missing/invalid” as startup failure.
- Do not auto-generate a replacement key in that case.
- Surface the failure clearly to operators.

## Medium

### Make missing-key behavior consistent for `get`

`get` currently returns an empty record when the key is missing. The server creates `new Entry()`,
serialization drops null fields, and the client ends up with an empty `Map`. That is inconsistent
with the client interface documentation, which says `get` should return `null` when the key does
not exist, and it is also inconsistent with `get-public` and `decrypt`, which return explicit
`Key not found` errors.

Relevant code:
- `server/src/main/java/io/canis/handlers/ClientHandler.java`
- `server/src/main/java/io/canis/store/Entry.java`
- `server/src/main/java/io/canis/protocol/Converter.java`
- `client/src/main/java/io/canis/jpaw/utils/Parser.java`
- `client/src/main/java/io/canis/jpaw/client/JpawClient.java`
- `client/src/main/java/io/canis/jpaw/client/Jpaw.java`

Follow-up:
- Decide whether missing `get` should return `null` or an explicit protocol error.
- Align server behavior, client parsing, and client interface docs.
