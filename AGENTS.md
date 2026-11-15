# CANIS KMS - Engineering Guidelines

## Project Context

CANIS is a proof-of-concept Key Management System (KMS).

The project stores and manages cryptographic key material used for:

- Encryption
- Decryption
- Key lifecycle management
- Secure data exchange through CANISP

Security is the primary non-functional requirement.

---

## Technology Constraints

### Java

- Use Java 21+
- Prefer standard JDK APIs
- Avoid unnecessary dependencies
- Avoid introducing frameworks unless explicitly approved

### Networking

- Prefer JSSE
- Prefer SSLContext
- Prefer SSLServerSocket and SSLSocket
- Do not introduce Netty, Spring, Vert.x or similar frameworks

### Persistence

- Preserve compatibility with existing storage whenever possible
- Never silently recreate cryptographic databases
- Fail safely whenever integrity cannot be verified

---

## Security Principles

### TLS

- TLS is mandatory
- TLS 1.3 only
- Never accept plaintext connections
- Never disable certificate validation
- Never disable hostname validation
- Never use insecure TrustManagers

### Private Keys

Private keys must never appear in:

- Logs
- Exceptions
- Public APIs
- CANISP responses
- Debug output

### Secrets

Secrets must never be hardcoded.

Examples:

- Certificates
- Truststores
- CANIS_SECRET_KEY
- Passwords

All secrets must be loaded externally.

---

## Audit Requirements

Audit logs must:

- Be structured
- Be deterministic
- Not contain secrets
- Support integrity verification

Required fields:

- eventId
- timestamp
- operation
- identity
- result

---

## Coding Standards

### Design

Prefer:

- Composition over inheritance
- Immutable objects when practical
- Explicit configuration objects
- Small focused classes

### Error Handling

- Fail closed
- Fail securely
- Do not expose internal details
- Produce actionable operational logs

### Logging

Log:

- Security events
- Operational failures
- Configuration failures

Never log:

- Keys
- Passwords
- Plaintext data

---

## Testing Requirements

Every security change must include:

### Unit Tests

Validate:

- Happy path
- Failure path
- Input validation

### Integration Tests

Validate:

- End-to-end behavior
- Backward compatibility

### Security Tests

Validate:

- Invalid certificates
- Invalid credentials
- Unauthorized operations
- Data tampering

---

## Pull Request Expectations

For every implementation:

1. Explain architecture changes.
2. Explain security implications.
3. Explain operational impact.
4. Explain backward compatibility impact.
5. List risks.
6. List future improvements.

---

## Commit Strategy

Prefer multiple small commits.

Each commit should:

- Build successfully
- Pass tests
- Be independently reviewable
- mvn spotless:apply

Avoid large mixed-purpose commits.

---

## Backlog Execution Rules

Before implementing any issue:

1. Analyze existing code.
2. Identify impacted files.
3. Identify security implications.
4. Create an implementation plan.
5. Then implement.

Do not directly start coding without first analyzing the codebase.