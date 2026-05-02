# CANISP Protocol

CANISP is the internal wire protocol used between the CANIS server and the official Java
`client` Maven artifact. Application code should use `JpawClient` instead of writing raw socket
messages.

CANISP is Redis-inspired in its interaction model: it uses a simple TCP request/response loop,
compact text commands, and typed response payloads. It is not RESP-compatible and should not be
used with Redis clients or Redis servers.

CANISP plays the same role for CANIS that a database wire protocol plays beneath a JDBC driver. It
is part of the client/server implementation contract, not the application-facing API. The public
integration surface for Java applications is the official client library.

The protocol is documented for maintainers, debugging, research, and PoCs. Compatibility work
should keep the CANIS server and official client aligned.

## Version

Current protocol version: `CANISP/1`

Clients can request the server protocol version after authentication:

```text
|version
```

Successful response:

```text
|s>CANISP/1
```

Protocol changes that alter command names, argument order, response types, or envelope formats
should introduce a new protocol version and keep `JpawClient` aligned with server command parsing.

## Connection Flow

1. Client opens a TCP socket.
2. Client sends one login command line.
3. Server returns a length-prefixed authentication response.
4. If authentication succeeds, the same connection can send CANISP commands.
5. Each command is a single newline-terminated text command.
6. Each response is a 4-byte signed integer length followed by the UTF-8 response payload.

## Limits

CANISP/1 applies defensive socket limits on both sides:

- Server sockets use a 30 second read timeout.
- Client sockets use a 30 second connect/read timeout.
- Server command lines are limited to 1,048,576 characters.
- Client commands are limited to 1,048,576 characters.
- Client response payloads are limited to 1,048,576 bytes before allocation.

## Payload Types

CANISP response payloads use typed prefixes:

```text
|s>value
|i>100
|ms>name:serviceA|ms>publicKey:base64
|a>|ms>name:serviceA|ms>publicKey:base64|a>|ms>name:serviceB|ms>publicKey:base64
```

The current Java client owns payload parsing through `Parser`.

## Application Integration Contract

Application developers should not construct CANISP command lines directly. They should use the
official Java client so command construction, response parsing, socket limits, and envelope
operations stay centralized in one implementation.

Raw CANISP messages are useful for protocol tests, debugging, and research, but they are not the
supported application API.

## Service Names

CANISP/1 treats service names as identifiers, not arbitrary free-form text. The intended service
name format is:

```text
[A-Za-z0-9._-]{1,128}
```

This format keeps command construction unambiguous for the current line-oriented protocol. The
official client and server should both validate service names before using them in commands or
stored entries.

## Commands

### Login

```text
|login username:password
```

Returns one of:

```text
Authentication successful
Authentication failed
Invalid input format
```

Login is handled before normal command processing.

### Version

```text
|version
```

Returns:

```text
|s>CANISP/1
```

### Health

```text
|health
```

Returns:

```text
|s>OK
```

### Set

```text
|set serviceName
```

Creates a server-side RSA key pair for the service name and persists the entry.

Returns:

```text
|s>OK
```

### Get

```text
|get serviceName
```

Returns entry metadata without private key material:

```text
|ms>name:serviceName|ms>publicKey:base64
```

### Get Public Key

```text
|get-public serviceName
```

Returns only the public key:

```text
|s>base64
```

### List

```text
|list
```

Returns an array of entry metadata maps:

```text
|a>|ms>name:serviceA|ms>publicKey:base64|a>|ms>name:serviceB|ms>publicKey:base64
```

### Decrypt

```text
|decrypt serviceName base64EncryptedPayload
```

CANIS decrypts the payload server-side using the stored private key for `serviceName`.
Private keys are never returned to clients.

Successful response:

```text
|s>base64DecryptedPayload
```

Error responses:

```text
|s>ERROR: Invalid input format
|s>ERROR: Key not found
|s>ERROR: Decryption failed
```

### Delete

```text
|del serviceName
```

Returns:

```text
|s>OK
```

## File Envelope Model

The official Java client encrypts files using a generated AES file key. The file key is wrapped
with a CANIS public key and stored in a versioned `CANISHYB1` envelope with the AES-GCM nonce,
ciphertext, and authentication tag.

To decrypt a shared file, the client reads the envelope and sends only the wrapped file key to
CANIS through `|decrypt`. CANIS unwraps the file key with the server-side private key and returns
the unwrapped file key. The private key stays on the server.

## Persistence

CANIS persists key metadata in an encrypted `CANISDB1` binary store. The store is encrypted with
AES-GCM using the secret key configured by `CANIS_SECRET_KEY`.
CANIS encrypted data must use the current `CANISGCM1`, `CANISHYB1`, and `CANISDB1` envelopes.

## Audit Events

CANIS emits structured audit log events through the `io.canis.audit` logger for authentication,
key creation, key access, key deletion, decrypt success/failure, and rejected commands. Audit logs
include identities, key names, remote addresses, and failure reasons. They do not include raw
credentials, private keys, encrypted payloads, or decrypted payloads.
