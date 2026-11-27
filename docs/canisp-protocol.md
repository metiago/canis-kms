# CANISP/1 Protocol Reference

## Overview
CANISP/1 is the internal TCP request/response wire protocol between CANIS and the official Java `client` library (`JpawClient`). It uses line-oriented commands and typed payloads. While Redis-inspired, it is **not** RESP-compatible.

* **Recommended usage:** Use `JpawClient` in applications to handle commands, parsing, socket limits, and file envelopes. Use raw CANISP only for debugging or maintenance.
* **Security model:** Operates inside an organization-managed environment where hosts and administrators are trusted. Authentication grants access to a shared trust domain with no per-key authorization boundaries. For isolated access scopes, deploy separate CANIS instances.

---

## Transport & Limits
1. **TLS 1.3:** Client validates the server certificate chain and hostname; server validates the client certificate via an external truststore (PKCS#12 identity/trust stores, no plaintext fallback).
2. **Authentication:** Client sends a `|login` line and receives a length-prefixed response.
3. **Command Loop:** Client sends newline-terminated (`\n`) commands. Responses begin with a 4-byte signed integer length followed by UTF-8 payload bytes.

| Parameter | Limit |
| :--- | :--- |
| Server socket read timeout | 30 seconds |
| Client connect & read timeout | 30 seconds |
| Max server command line | 1,048,576 characters |
| Max client command | 1,048,576 characters |
| Max client response allocation | 1,048,576 bytes |

---

## Authentication
Send `|login username:password` immediately after the TLS handshake.

**Possible responses:**
* `Authentication successful`
* `Authentication failed`
* `Invalid input format`

---

## Message Format
* **Commands:** Start with `|` and end with `\n`.
* **Service Names:** Must match `[A-Za-z0-9._-]{1,128}`.
* **Response Prefixes:**
  * `|s>` — String (e.g., `|s>value`)
  * `|i>` — Integer (e.g., `|i>100`)
  * `|ms>` — Map entry (e.g., `|ms>name:serviceA|ms>publicKey:base64`)
  * `|a>` — Array element (e.g., `|a>|ms>name:serviceA|ms>publicKey:base64|a>|ms>name:serviceB|ms>publicKey:base64`)

---

## Commands
All commands require an authenticated session. Payloads shown below follow the 4-byte length prefix.

| Command | Description | Success Response |
| :--- | :--- | :--- |
| `|version` | Returns protocol version. | `|s>CANISP/1` |
| `|health` | Checks server health. | `|s>OK` |
| `|set <serviceName>` | Generates and stores an RSA key pair. | `|s>OK` |
| `|get <serviceName>` | Returns entry name and public key. | `|ms>name:<name>|ms>publicKey:<base64>` |
| `|get-public <serviceName>` | Returns only the public key. | `|s><base64>` |
| `|list` | Lists all stored service entries. | `|a>|ms>name:...|ms>publicKey:...` |
| `|decrypt <serviceName> <b64Payload>` | Decrypts payload with server private key. | `|s><base64DecryptedPayload>` |
| `|del <serviceName>` | Deletes the active key. | `|s>OK` |

### Error Responses & Behavior
* **Private keys:** Never returned by the server.
* **Missing key (`|get`, `|decrypt`):** Returns `|s>ERROR: Key not found` (`JpawClient.get` maps this to `null`).
* **Duplicate service (`|set`):** Returns `|s>ERROR: Service already exists` and keeps the original key.
* **Decryption errors (`|decrypt`):** Returns `|s>ERROR: Invalid input format` or `|s>ERROR: Decryption failed`.

---

## File Envelopes & Storage
* **Client-side file encryption (`CANISHYB1`):** `JpawClient` generates an AES key, encrypts the file locally using AES-GCM, and wraps the AES key with the CANIS public key. The `CANISHYB1` envelope stores the wrapped key, nonce, ciphertext, and auth tag.
* **Decryption:** Only the wrapped AES key is sent to CANIS via `|decrypt`. File contents never leave the client.
* **Server storage (`CANISDB1`):** Key metadata is persisted in a `CANISDB1` binary store encrypted with AES-GCM under `CANIS_SECRET_KEY`. Internal encrypted payloads use `CANISGCM1`, `CANISHYB1`, and `CANISDB1`.

---

## Compatibility & Auditing
* **Versioning:** Changes to command names, arguments, response types, or envelope formats require a new protocol version and `JpawClient` update. CANISP is an internal contract, not a public API.
* **Audit Logs (`io.canis.audit`):** Logs key-value events for authentication, key operations, decryption outcomes, and rejected commands. Logs include authenticated identity (or a generic identity on failed login), validated key names, remote addresses, and failure reasons.
* **Log Exclusions & Limits:** Passwords, raw login lines, private keys, and payloads are never logged. Logs lack a formal schema, event IDs, or tamper-evident integrity checks.
