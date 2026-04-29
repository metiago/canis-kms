# CANIS KMS Backlog

## P0 - Security and Correctness

- [x] Replace IP-based authentication with connection-bound sessions or signed bearer tokens.
  - Current behavior trusts any future connection from an authenticated IP.
  - Commands should require explicit authentication state that cannot be shared accidentally across clients behind the same NAT or host.

- [x] Stop returning private keys from `get`.
  - Decide whether CANIS is a key distributor or a KMS.
  - Prefer server-side decrypt/sign operations so private keys never leave the server.

- [x] Add server-side decrypt support for the KMS model.
  - Private keys must remain inside CANIS.
  - A service should send encrypted data or an encrypted file key to CANIS and receive only the decrypted result it is authorized to receive.
  - Start with decrypting small RSA-encrypted payloads, then migrate to envelope encryption for real files.

- Fix malformed login handling.
  - `|login` without `username:password` should return an error and stop processing the request.
  - Add tests for missing credentials, empty username, empty password, and passwords containing separators.

- Replace insecure cipher defaults.
  - Use AES-GCM with a random nonce for persisted store encryption.
  - Use RSA-OAEP if asymmetric encryption remains necessary.
  - Avoid `Cipher.getInstance("AES")` and `Cipher.getInstance("RSA")` without explicit modes and padding.

- Replace direct RSA file encryption with hybrid encryption.
  - Generate a random data encryption key per file.
  - Encrypt file content with AES-GCM.
  - Encrypt the data encryption key with RSA-OAEP.
  - Store nonce, encrypted key, ciphertext, and authentication tag in a versioned envelope.

- Define service identity and authorization for private-key operations.
  - A service should only be able to decrypt with its own private key unless an explicit policy allows otherwise.
  - Authentication credentials should map to a service identity, not only a shared server username.
  - Add authorization checks before decrypt, sign, unwrap, or future private-key operations.

## P1 - Storage and Protocol

- Add multi-recipient envelope encryption support.
  - Encrypt file content once with a generated symmetric data encryption key.
  - Wrap that data encryption key separately with each recipient service public key.
  - Store recipient key id, wrapped key, nonce, ciphertext, tag, and envelope version.
  - Allow each recipient service to ask CANIS to unwrap only its own wrapped key.

- Add public-key lookup command.
  - Make the public-key retrieval path explicit, for example `|get-public serviceName`.
  - Keep `get` semantics narrow so it does not imply private key retrieval.

- Make `KeyValueStore` a shared server dependency.
  - Avoid creating a new store instance per command.
  - Use one shared lock for all reads and writes.
  - Prevent concurrent requests from overwriting changes with stale file state.

- Implement `list` using persisted store values.
  - Replace the placeholder `List.of(new Entry())`.
  - Add client API support for parsing and returning all entries.

- Replace the custom string protocol or add escaping.
  - Current parsing breaks when values contain `:`, `|m`, or `|a>`.
  - Prefer length-prefixed JSON messages for commands and responses.
  - Version the protocol before adding more commands.

- Add socket timeouts and bounded request sizes.
  - Prevent clients from holding threads indefinitely.
  - Reject oversized command or response payloads.

- Use a configurable server host in the client.
  - Replace hard-coded `0.0.0.0` with `CANIS_SERVER_HOST`.
  - Default to `localhost` for local development.

## P2 - Reliability and Operations

- Fix Logback rolling configuration.
  - Use `SizeAndTimeBasedRollingPolicy` if `%i` is required.
  - Confirm logs rotate during tests without startup errors.

- Replace `System.exit` in validators with exceptions.
  - Let callers and tests handle configuration failures cleanly.
  - Add unit tests for missing and invalid environment variables.

- Add graceful server shutdown.
  - Make socket tests deterministic.
  - Close the `ServerSocket` and executor service cleanly.

- Improve persistence format.
  - Avoid Java object serialization for persisted data.
  - Use a versioned JSON or binary format with explicit schema evolution.

- Add audit logging.
  - Log authentication events, key creation, key access, deletion, and failed commands.
  - Avoid logging secrets, private keys, or raw credentials.

## P3 - Developer Experience and Tests

- Align README prerequisites with Maven configuration.
  - The README says Java 8+, but the project compiles for Java 17.

- Add end-to-end client/server tests.
  - Start the server on an ephemeral port.
  - Authenticate, set, get, list, delete, and verify persistence across restart.

- Add protocol parser tests for edge cases.
  - Values containing delimiters.
  - Empty fields.
  - Unknown message types.
  - Invalid integers.

- Add store corruption tests.
  - Missing secret key file.
  - Invalid secret key file content.
  - Corrupt encrypted database file.
  - Wrong encryption key.

- Add command result validation in the client.
  - `delete` should return false or throw when the server reports failure.
  - Authentication should validate the server response instead of accepting any positive-length response.

- Document threat model and deployment assumptions.
  - Network trust boundaries.
  - Expected key lifecycle.
  - Secret key storage requirements.
  - Backup and rotation strategy.
