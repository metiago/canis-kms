# CANIS documentation

Use the [JOSS publication backlog](../joss-backlog.md) for submission work and the
[product backlog](../app-prod-grade-backlog.md) for later engineering work.
The documents here describe current behavior, operational setup, tests, and recorded evidence.

## Start here

- [Reviewer guide](reviewer-guide.md): build, install, and run the synthetic example.
- [Research index](research/README.md): JOSS methods, results, scripts, and artifacts.

## Security and operations

- [GitHub Release setup](research/jreleaser-setup.md): source archive, checksums, release notes, workflow, and rollback.
- [Threat model](threat-model.md): trust boundaries and current shared-access policy.
- [Master key](master-key.md): external key provisioning and database recovery requirements.
- [CODE-01 integrity evidence](code01-integrity.md): local acceptance mapping, test results, and release limits.
- [Private-key exposure](private-key-exposure.md): review of the private-key boundary.
- [RSA key lifecycle](rsa-key-lifecycle.md): key generation and lifecycle limits.
- [TLS security tests](tls-security-tests.md): transport test coverage.

## Protocol and client

- [CANISP protocol](canisp-protocol.md): wire commands and responses.
- [Java client package](client-package.md): client API and packaging.
- [Client deployment](client-package-deploy.md): client configuration and deployment.
