# JOSS-03 reviewer workflow method

## Question and scope

Can a producer leave a synthetic encrypted file in a shared directory and
have the intended recipient recover identical bytes through the official TLS
client? Does a new recipient connection fail after the key service stops?
This is a categorical functional demonstration, not a benchmark or a claim
of production availability or research adoption.

## Exact commands and prerequisites

From the repository root, with Git, JDK 21+ including `keytool`, Maven 3.9 or
compatible, and an available local TCP port:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss03.ps1
```

```sh
sh docs/research/run-joss03.sh
```

The scripts print the Git HEAD and Maven/JDK environment and run only
`ReviewerWorkflowTest` with Maven reactor dependencies. No configured CANIS
secret or running server is required. `mvn spotless:apply` may be used before
test execution when Java source was changed; it is not part of the experiment.

## Generated fixtures and procedure

`ReviewerWorkflowTest` creates a JUnit temporary directory. `TlsTestSupport`
uses the JDK `keytool` to create short-lived server and client PKCS#12 identity
stores and reciprocal trust stores. `MasterKeyTestSupport` generates a fresh
AES master key, encodes it in a protected file, and restricts directory/file
permissions. The test explicitly initializes an isolated database. It
generates separate producer and recipient credential passwords from
`SecureRandom` and starts a TLS-only server on an ephemeral loopback port.

The recipient creates its named key. The producer writes a synthetic UTF-8
fixture and encrypts it to `shared/recipient.canis` with the public key. The
test asserts the envelope exists and is not equal to the plaintext bytes.
The recipient requests file-key recovery through the TLS client and the test
compares recovered bytes exactly to the input. The server then stops; opening
a new recipient client must fail with `IOException`.

Expected command exit code: 0. The Surefire report
`server/target/surefire-reports/TEST-io.canis.ReviewerWorkflowTest.xml`
must report one test, zero failures, zero errors, and zero skips. Its console
output or XML `system-out` element must include:

```text
JOSS03_REVIEWER_DEMO=PASS producer->shared-envelope->recipient byte-match
JOSS03_OUTAGE_CHECK=PASS new recipient connection rejected after server stop
```

The test prints neither plaintext nor generated credentials or key material.
The envelope changes between runs because encryption uses fresh random
material; byte equality of the recovered output is the assertion.

## Cleanup and platform limits

The test stops its server and JUnit removes the temporary directory after
normal completion. If interrupted, inspect any remaining `junit*` temporary
directory before removing it. `mvn clean` removes Maven reports and outputs.
Do not publish raw Surefire XML, server logs, or temporary fixtures without
reviewing them for sensitive data; a sanitized outcome summary is sufficient.

On Windows, the test account needs permission to set restrictive ACLs on its
temporary directory. A restricted execution sandbox may deny that operation;
record it as an environment failure and rerun with appropriate filesystem
permission, without weakening master-key validation. On POSIX, the test
account must be able to set owner-only permissions. The POSIX script and CI
job require actual execution evidence before making a POSIX result claim.
All test credential identities use one temporary client certificate, so this
experiment does not test certificate-bound separation. A stopped server
rejecting a new connection is not an outage-duration or reliability measure.

On a POSIX CI runner, `java docs/research/VerifyPosixCoverage.java
server/target/surefire-reports/TEST-io.canis.crypto.MasterKeyLoaderTest.xml`
must print `JOSS03_POSIX_COVERAGE=PASS` after the Maven suite. The check
requires both the unsafe-permissions and symbolic-link cases to pass. A
synthetic positive report fixture is `joss03-posix-report-fixture.xml`;
`java docs/research/VerifyPosixCoverage.java
docs/research/joss03-posix-report-fixture.xml` exercises the check itself
without claiming a POSIX application result.
