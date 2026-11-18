# Reviewer installation and executable example

This guide describes the current development tree. Recheck its commands on the
chosen release commit before citing a release. CANIS is a Java 21 Maven reactor
with `client` and `server` modules. The supported application API is
`JpawClient`; CANISP is an internal wire protocol. The server and official
client use TLS 1.3 with mutual certificate authentication. The client validates
the server certificate and hostname.

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
`server/target/surefire-reports/`. Five legacy `JpawIntegrationTest` cases are
disabled because they require an externally started server. On a POSIX host,
verify that `MasterKeyLoaderTest` permission and symbolic-link checks ran;
record any skips rather than treating them as passes. The Ubuntu CI job runs
`VerifyPosixCoverage.java` against its Surefire XML and fails if either case
is absent or skipped. The continuous integration workflow is configured for
Ubuntu and Windows, but a green run on the chosen release commit still has
to be retained.

## Producer to recipient demonstration

From the repository root, run one of:

```sh
sh docs/research/run-joss03.sh
```

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss03.ps1
```

The focused `ReviewerWorkflowTest` creates a temporary master key, initialized
database, server/client PKCS#12 identities and trust stores, and random
credential passwords. It starts a TLS server in a test thread. The `recipient`
creates a key, the `producer` writes an encrypted envelope into a temporary
`shared/` directory, and the `recipient` recovers the original synthetic bytes
through the official TLS client. It then stops the service and checks that a
new recipient connection is refused. All material is synthetic and local; the
test neither publishes nor persists any real secret.

Expected outcome: command exit code 0, one test with zero failures/errors/skips
in `server/target/surefire-reports/`, and these markers in console output or
the Surefire XML `system-out` element:

```text
JOSS03_REVIEWER_DEMO=PASS producer->shared-envelope->recipient byte-match
JOSS03_OUTAGE_CHECK=PASS new recipient connection rejected after server stop
```

The test uses one temporary TLS client certificate for both credential
identities; it verifies the file workflow and credential roles, not separate
certificate identities. It does not measure outage duration, performance, or
reliability. `@TempDir` removes normal-run fixtures. Inspect any leftover test
directory after an interrupted run before deleting it; `mvn clean` removes
generated Maven outputs. See [the method](research/joss03-method.md) and
[the recorded result](research/joss03-results.md).

For a producer, delayed consumer, and packaged CANIS server in three separate
processes, use the [CODE-02 research harness](research/code02-method.md). Its
temporary server database and shared directory are isolated from this focused
JUnit example, and its result files contain only sanitized metadata. On POSIX,
run `sh docs/research/run-code02.sh --scenario normal --files 1 --size 4096
--delay-ms 1000` from the repository root; the Ubuntu CI job uses this same
wrapper and workload. A passing run prints `CODE02_RESULT=passed
scenario=normal verified=1/1` and writes sanitized JSON and CSV records.

## External configuration for a long-running server

The demo's self-signed TLS identities are short-lived test fixtures. For a
deployment, obtain identities and trust anchors through the operator's PKI.
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

Build with `mvn package`, then from a protected working directory run
`java -jar server/target/canis-jar-with-dependencies.jar --init-db` **only for
a new database**. Normal startup uses the same command without `--init-db`.
The database path is `db.dat` relative to that working directory. If an
existing database or matching master key is lost, restore the pair from
protected backups; initialization is not a recovery operation.

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
remains on disk; the example verifies only that a new connection fails after
server shutdown. The client reads whole plaintext/envelope files into memory
and constructs additional byte arrays during processing. There is no general
safe maximum file size, throughput, or availability target. The
[CODE-03 Windows preflight](research/code03-results.md) verified the planned
synthetic study's 4 MiB maximum file size with four consumer workers and a
512 MiB heap per process; it does not establish support for larger files or
different hosts. Avoid larger files until the actual workload has been assessed.
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
