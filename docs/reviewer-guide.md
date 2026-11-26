# Setup and file-exchange tests

This guide describes the current development tree. CANIS is a Java 21 key-recovery
service and client for asynchronous encrypted-file exchange under a
shared-trust model. Its Maven reactor has `client` and `server` modules. The
supported application API is `JpawClient`; CANISP is an internal wire protocol.
The server and official client use TLS 1.3 with mutual certificate authentication.
The client validates the server certificate and hostname.
The intended deployment is an organization-managed corporate environment, with
the host, administrators, deployment automation, service account, and corporate
identity systems trusted as described in the [threat model](threat-model.md).
For suspected vulnerabilities, follow [SECURITY.md](../SECURITY.md); the public
issue tracker is only for ordinary, non-sensitive reports.

## Clean checkout and tests

Install Git, JDK 21 or newer (including `keytool`), and Maven 3.9 or a
compatible version. Use a JDK TLS provider with at least one approved TLS 1.3
cipher suite. Clone the public source repository, then run from its root:

```sh
git clone https://github.com/metiago/canis-kms.git
cd canis-kms
java -version
mvn --version
mvn --batch-mode --no-transfer-progress clean verify
```

Maven resolves the two modules and test dependencies. The automated tests
generate temporary TLS stores, credentials, and database master keys; no
production credential is required for `mvn clean verify`. Check both module
reports under `client/target/surefire-reports/` and
`server/target/surefire-reports/`. `JpawConsole` is a manual client console
that requires an externally started server; Maven does not run it as a test. On a POSIX host,
verify that `MasterKeyLoaderTest` permission and symbolic-link checks ran;
record any skips rather than treating them as passes. The Ubuntu CI job runs
[`VerifyIssue1PosixCoverage.java`](../tools/ci/VerifyIssue1PosixCoverage.java) for master-key owner, permission, unreadable-file, and
ancestor-link cases and [`VerifyIssue2PosixCoverage.java`](../tools/ci/VerifyIssue2PosixCoverage.java) for database failure cases. Both fail if a required case is absent or skipped. The
Windows job checks its owner-policy and ACL cases with
[`VerifyWindowsCoverage.java`](../tools/ci/VerifyWindowsCoverage.java).
The continuous integration workflow is configured for Ubuntu and Windows, but
a green run on the chosen release commit still has to be retained.

## File-exchange coverage

`mvn clean verify` runs `ServerEndToEndTest`, which encrypts and decrypts a file
through the official TLS client, and `ServerTlsSecurityTest`, which checks
client admission. Both use temporary synthetic credentials and files. Check
their Surefire reports for failures or skips.

## External configuration for a long-running server

The tests' self-signed TLS identities are short-lived fixtures. For a
deployment, obtain identities and trust anchors through the operator's PKI.
Use a dedicated CANIS service account on a managed host and restrict network
reachability to admitted corporate applications. Infrastructure administrators
and the deployment system are part of the Trusted Computing Base; plan host,
identity, backup, and recovery controls accordingly.
Keep the server identity and trust stores separate from the client stores, and
give the server certificate a subject alternative name matching
`CANIS_SERVER_HOST`. Do not copy fixture keys into source control or images.
The required server variables are `CANIS_PORT`, `CANIS_USERNAME`,
`CANIS_PASSWORD`, `CANIS_SECRET_KEY` (a path to a Base64-encoded AES key file),
`CANIS_TLS_KEYSTORE`, `CANIS_TLS_KEYSTORE_PASSWORD`,
`CANIS_TLS_TRUSTSTORE`, and `CANIS_TLS_TRUSTSTORE_PASSWORD`. Optional
`CANIS_SERVICE_CREDENTIALS` adds admitted credential identities. The client
uses `CANIS_SERVER_HOST`, `CANIS_SERVER_PORT`, `CANIS_USERNAME`,
`CANIS_PASSWORD`, `CANIS_CLIENT_TLS_KEYSTORE`,
`CANIS_CLIENT_TLS_KEYSTORE_PASSWORD`, `CANIS_CLIENT_TLS_TRUSTSTORE`, and
`CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD`. Supply all passwords through protected
external configuration, never command arguments or committed files. The
[README](../README.md) and [master-key guide](master-key.md) give the exact
startup and restrictive file-permission requirements.
After starting the server, run `client/src/test/java/io/canis/jpaw/JpawConsole.java`
as a Java main with the client variables above. Type `help` for console commands;
`set` and `delete` change the server database.
The loader rejects a symbolic link at the master-key file path and
checks ownership and access on the file and immediate parent. Higher ancestor access controls
remain a deployment responsibility. Its fail-closed checks
catch unsafe configuration and accidental exposure; they do not isolate key
material from a privileged host administrator or compromised operating system.

Build with `mvn package`, then from a protected working directory run
`java -jar server/target/canis-jar-with-dependencies.jar --init-db` **only for
a new database**. Normal startup uses the same command without `--init-db`.
The database path is `db.dat` relative to that working directory. If an
existing database or matching master key is lost, restore the pair from
protected backups; initialization is not a recovery operation. Follow the
[operational recovery runbook](operational-recovery.md) before returning to service.

## Architecture, failure behavior, and limits

The application calls the official client. The client encrypts file contents
locally and sends the server a wrapped data key for private-key recovery. The
server keeps RSA private keys in an encrypted database and returns the
unwrapped data key to an authenticated client. Read the [threat model](threat-model.md),
[protocol guide](canisp-protocol.md), and [key lifecycle guide](rsa-key-lifecycle.md)
before using this model. Any admitted client can request operations for any
stored key name; key names are not per-service authorization controls.

When the key service is unavailable, a client cannot fetch a public key or
unwrap a file data key through the official workflow. A file already encrypted
remains on disk, but decrypting it through CANIS requires service recovery.
The client reads whole plaintext/envelope files into memory and constructs
additional byte arrays during processing. There is no general safe maximum
file size, throughput, or availability target. Assess the actual workload
before using large files.
The encrypted database loader imposes a 256 MiB input cap; that is not a supported file-size
limit for client envelopes.

Supported in the current working tree: TLS 1.3 client/server transport,
file-envelope encryption/decryption, explicit database initialization,
create/public-key/decrypt/delete operations, and basic audit messages.
Unsupported or unverified: plaintext transport, per-key/per-service
authorization, automated key rotation or expiry, producer authentication by
the envelope, verifiable audit history, reliable erasure, crash durability,
large-file streaming, and production-grade recovery guarantees. These are
implementation boundaries, not claims about the strength of the primitives.
CANIS is not intended to provide public cloud KMS, multi-tenant, zero-trust, or
HSM-level guarantees.
