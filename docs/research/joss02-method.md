# JOSS-02 release-boundary verification method

## Scope and expected observations

The full Maven reactor suite checks the current Java client and server, including the official
client's TLS 1.3 file workflow, TLS and credential rejection cases, encrypted database loading,
legacy format compatibility, and tampering cases. The focused JOSS-01 script separately reruns
the categorical shared-trust experiment. These are behavioral checks, not measurements of
performance, reliability, security strength, or adoption. A passing working-tree run is not a
verified release: compare the recorded revision and dirty status with any proposed tag.

## Exact commands and prerequisites

Use JDK 21 or later with `keytool`, Maven 3.9 or compatible, Git, and a Java TLS provider with an
enabled approved TLS 1.3 cipher suite. Use a local account permitted to create temporary files
and apply the master-key permission restrictions. From the repository root on Windows:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss02.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-joss02-summary.ps1
```

On POSIX:

```sh
sh docs/research/run-joss02.sh
sh docs/research/run-joss01.sh
```

The JOSS-02 scripts print the Git commit and dirty status, operating system, Maven version and
Maven Java runtime, then run `mvn -q test`. Maven writes individual XML and text reports under
`client/target/surefire-reports/` and `server/target/surefire-reports/`. The JOSS-01 scripts run
the single research scenario separately. The PowerShell exporter retains only suite counts and
test-case outcomes in two CSV files under `docs/research/`; it omits raw logs and system
properties. On POSIX, the Surefire XML is the direct result; the CSV exporter is optional if
PowerShell (`pwsh`) is installed. Record the exact command, commit, any dirty files,
environment, report counts, failures, errors, skips, and limitations. Do not copy raw logs or
test-generated key material into a publication artifact without screening for secrets.

Expected success is Maven exit code zero, no failures or errors in the reports, and a passing
`SharedTrustDomainResearchTest` focused run. The official-client workflow must recover bytes
identical to its synthetic input. Negative tests must reject plaintext, TLS 1.2-only peers,
untrusted or invalid certificates, wrong hostname, and invalid credentials. A successful test
run does not remove the known Issue #8 database write race or Issue #36 authorization and
stored-record findings.

## Generated fixtures and cleanup

JUnit tests generate temporary master-key files, explicit test databases, PKCS#12 identity and
trust stores, random credentials, and synthetic input and envelope files under `@TempDir`.
`SharedTrustDomainResearchTest` uses eight repetitions of a fixed synthetic line and three
credential identities; its TLS certificate is shared among them. JUnit cleans its temporary
fixtures after the tests. Maven reports remain under each module's `target/` directory. After
recording a sanitized summary, an operator may remove only generated build output with
`mvn clean` from the repository root; this is optional and must not be run before preserving
the needed report evidence. No production database or master key is used.

Fixture configuration is in `server/src/test/java/io/canis/TlsTestSupport.java`,
`server/src/test/java/io/canis/MasterKeyTestSupport.java`, and
`server/src/test/java/io/canis/SharedTrustDomainResearchTest.java`. The client TLS fixtures
are defined in `client/src/test/java/io/canis/jpaw/client/ClientTlsTestSupport.java`.
These test helpers generate their own identities and stores using JDK `keytool`; no
externally supplied certificate, truststore, password, or master key is required for the
experiment.

## Platform limits and decision rule

The Windows master-key test fixture may fail to set restrictive ACLs in a sandbox. If so,
record the environmental failure and rerun the same command in a Windows account with the
needed ACL rights. Windows runs may skip POSIX permission checks; run those on a POSIX host
before claiming cross-platform coverage. Results apply to the recorded code and environment
only. Do not check off a fixed-release JOSS-02 task until the exact candidate revision is
tested and the source and manuscript are audited together.
