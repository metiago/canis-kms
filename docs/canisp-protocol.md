# CANISP/1 protocol reference

## Overview

CANISP is the internal wire protocol between CANIS and the official Java `client` Maven
artifact. It uses a TCP request/response loop with line-oriented commands and typed payloads.
Its interaction model is Redis-inspired, but it is not RESP-compatible. Redis clients and
servers cannot use it.

Applications should use `JpawClient` for command construction, response parsing, socket limits,
and file envelopes. Raw CANISP is useful for maintenance, debugging, research, and PoCs;
compatibility changes must keep the server and client aligned.

## Transport

1. The client opens a TLS 1.3 connection, validating the server certificate chain and configured
   hostname. The server validates the client certificate against an external truststore. The
   official client uses external PKCS#12 identity and trust stores and has no plaintext
   fallback.
2. After the handshake, the client sends one `|login` line. The server returns a length-prefixed
   authentication response.
3. On success, the connection accepts repeated newline-terminated command lines. Each response
   starts with a 4-byte signed integer length followed by that many UTF-8 payload bytes.

TLS does not change CANISP/1 command or response framing.

| Limit | Value |
| --- | --- |
| Server socket read timeout | 30 seconds |
| Client connect and read timeout | 30 seconds |
| Server command line | 1,048,576 characters |
| Client command | 1,048,576 characters |
| Client response payload before allocation | 1,048,576 bytes |

## Authentication

Send `|login username:password` after TLS succeeds, before normal commands. The length-prefixed
response is one of `Authentication successful`, `Authentication failed`, or `Invalid input
format`. An authenticated connection may then use the commands below.

## Message format

Command lines contain the leading `|` and end with a newline. Response payloads use these
prefixes:

```text
|s>value
|i>100
|ms>name:serviceA|ms>publicKey:base64
|a>|ms>name:serviceA|ms>publicKey:base64|a>|ms>name:serviceB|ms>publicKey:base64
```

The official client's `Parser` owns response parsing. Service names are identifiers matching
`[A-Za-z0-9._-]{1,128}`; the client and server validate them to keep line-oriented commands
unambiguous.

## Commands

All commands below require authentication. Examples show response payloads after the 4-byte
length.

- `|version` returns `|s>CANISP/1`.
- `|health` returns `|s>OK`.
- `|set serviceName` creates and persists a server-side RSA key pair; returns `|s>OK`.
- `|get serviceName` returns public entry metadata:
  `|ms>name:serviceName|ms>publicKey:base64`.
- `|get-public serviceName` returns only the public key: `|s>base64`.
- `|list` returns an array of public entry maps, for example:
  `|a>|ms>name:serviceA|ms>publicKey:base64|a>|ms>name:serviceB|ms>publicKey:base64`.
- `|decrypt serviceName base64EncryptedPayload` decrypts with the server-side private key and
  returns `|s>base64DecryptedPayload`.
- `|del serviceName` removes the active key; returns `|s>OK`.

For a missing name, `|get` returns `|s>ERROR: Key not found`; `JpawClient.get` translates this
to `null`. `|decrypt` may return `|s>ERROR: Invalid input format`, `|s>ERROR: Key not found`, or
`|s>ERROR: Decryption failed`. `|set` rejects a duplicate with
`|s>ERROR: Service already exists` and preserves the original key. The server does not return
RSA private keys.

## File envelopes

The official client generates an AES file key, encrypts the file locally with AES-GCM, and wraps
the file key with the CANIS public key. The versioned `CANISHYB1` envelope contains the wrapped
file key, AES-GCM nonce, ciphertext, and authentication tag.

To decrypt a shared file, the client sends only the wrapped file key through `|decrypt`. CANIS
unwraps it with the server-side private key and returns the file key; file content stays outside
the server. Key metadata is stored in an encrypted `CANISDB1` binary store, protected by AES-GCM
under the key named by `CANIS_SECRET_KEY`. CANIS encrypted data uses the `CANISGCM1`,
`CANISHYB1`, and `CANISDB1` formats.

## Compatibility notes

Command names, argument order, response types, or envelope format changes require a new protocol
version and corresponding `JpawClient` parsing changes. CANISP is a client/server implementation
contract, not a supported direct application API.

`io.canis.audit` emits key-value log messages for authentication, key creation, access and
deletion, decrypt success or failure, and rejected commands. They can include identities, key
names, remote addresses, and failure reasons, but not raw credentials, private keys, encrypted
payloads, or decrypted payloads. The messages have no defined record schema, event IDs, or
integrity verification required by `AGENTS.md`; they are not a tamper-evident audit trail.
