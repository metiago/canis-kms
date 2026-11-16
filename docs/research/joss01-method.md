# JOSS-01 shared trust boundary evaluation method

## Question and scope

For the current CANIS release, can a second authenticated service decrypt an envelope addressed
to another service's key, while an identity with invalid credentials is rejected? This is a
behavioral evaluation of the current credential-level trust boundary. It is not evidence of
research adoption, cryptographic strength, performance, or a safe cross-service authorization
policy.

## Procedure defined before execution

Run `SharedTrustDomainResearchTest` using the scripts in this directory. The JUnit test creates a
temporary directory, a newly generated database master key, an explicitly initialized database,
temporary PKCS#12 TLS identities and trust stores, and three randomly generated service passwords.
The server and all clients use TLS 1.3 and the official Java client. The generated fixture is eight
repetitions of a fixed synthetic text line. No certificate, password, or private key is committed.

1. The `recipient` identity creates its named RSA key.
2. The `producer` identity retrieves its public key through the official client and encrypts the
   fixture into a `CANISHYB1` file.
3. The `recipient` and then the distinct `observer` credential identity each request decryption of
   the same file with the `recipient` key. Compare each recovered file byte for byte with the
   synthetic input.
4. Attempt to connect as `observer` with one character of its password deliberately changed;
   expect authentication failure.

The outcome is categorical: each assertion passes or fails. There is one deterministic scenario
per invocation and no latency, throughput, or memory estimate. Repeat runs are useful for detecting
environment-dependent behavior, but they do not turn this into a performance benchmark. The TLS
certificate is shared among test identities, so this isolates the server's username/password
authorization boundary; it does not test separation by client certificate. The database and all
generated material are local temporary fixtures, never a production database.

## Reproduction

Prerequisites: JDK 21 with `keytool`, Maven 3.9 or compatible, Git, an available loopback port, and a
Java TLS provider supporting one of the project's approved TLS 1.3 cipher suites. From the
repository root, run
`powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1`
on Windows, or `sh docs/research/run-joss01.sh` on POSIX. The PowerShell execution-policy setting
applies only to that process. The scripts run the single JUnit experiment, including its Maven
reactor dependency. They require no externally supplied service secret. Maven writes its test
report under `server/target/surefire-reports/`. On Windows, the test identity must be allowed to
set restrictive ACLs on its temporary directory; a restricted sandbox may deny this operation.

Expected output is a Maven exit code of 0 and a Surefire report for
`SharedTrustDomainResearchTest` containing one test, zero failures, zero errors,
and zero skips. The positive assertions require both valid identities to
recover exactly the synthetic bytes; the negative assertion requires the
altered password to be rejected. The test's JUnit `@TempDir` fixture and TLS
server are cleaned after normal completion. Inspect temporary files before
removing any left by an interrupted process. Maven's local `target/` reports
may be retained for verification and later removed with `mvn clean`; do not
commit raw logs or generated secret material.

Interpret the test result only with the commit, operating system, JDK, Maven version, and test
report recorded. Any future per-key authorization change should make the observer decryption
assertion fail, requiring an explicit revision of this method and the manuscript claim.
