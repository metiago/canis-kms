# Service Name Validation Backlog

CANISP is an internal driver-style protocol. Application developers should use the official Java
client, but the client and server should still validate service names so normal API usage cannot
produce malformed command lines or ambiguous protocol payloads.

## Target Contract

Service names should be identifiers with this format:

```text
[A-Za-z0-9._-]{1,128}
```

Reject names that are blank, too long, contain whitespace, contain line breaks, or contain protocol
delimiter characters such as `|` and `:`.

## Backlog

1. Done: Add a shared service-name validator in the client package.
   - Scope: client module.
   - Suggested location: `io.canis.jpaw.client` or a small internal validation package.
   - Behavior: reject invalid service names before command strings are built.
   - Affected methods: `set`, `get`, `getPublicKey`, `decrypt`, `encryptFile`, `decryptFile`,
     and `delete`.

2. Add server-side service-name validation before store operations.
   - Scope: server module.
   - Suggested location: protocol or store boundary.
   - Behavior: reject invalid service names for `set`, `get`, `get-public`, `decrypt`, and `del`.
   - Response shape: return a typed error string such as `|s>ERROR: Invalid service name`.

3. Add focused client tests.
   - Verify valid names such as `serviceA`, `service-a`, `service_a`, `service.a`, and `svc123`.
   - Verify invalid names with spaces, tabs, newlines, `|`, `:`, empty strings, and names longer
     than 128 characters.
   - Verify invalid names do not call `SocketClient.sendCommand`.

4. Add focused server handler tests.
   - Verify invalid names are rejected for every key-oriented command.
   - Verify invalid names do not call `KeyValueStore.set`, `get`, or `delete`.
   - Verify audit logging records rejected commands without logging raw payloads.

5. Document the validation contract in client package documentation.
   - Update the client README with accepted service-name characters.
   - Mention that raw CANISP is unsupported for application integrations.

6. Consider typed error constants.
   - Add protocol constants for common errors, including invalid service name.
   - Keep client error handling consistent across all key-oriented operations.

7. Consider a compatibility note.
   - If older deployments already contain names outside the new format, decide whether to migrate,
     grandfather, or reject them on access.
   - For a research/PoC project, rejecting new invalid names may be enough.
