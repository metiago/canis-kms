# CANIS Reviewer Quickstart

This review uses synthetic data and the repository's isolated CODE-02 runner. Steps 4–9 are one run: the command in step 4 creates temporary secrets, initializes and starts the packaged server, configures the official client, and completes the file exchange. Successful runs remove the private fixtures. This procedure does not verify an operator-provisioned long-running server. Do not use this run's fixtures for a deployment.

CANIS targets organization-managed corporate environments. Read the
[threat model](threat-model.md) and [master-key guide](master-key.md) for the trusted host,
administrator, identity, and key-path assumptions before adapting this example.

## Prerequisites

- Git; JDK 21 or newer (`java`, `javac`, `keytool`); Maven 3.9.x.
- A POSIX shell (`sh`) or Windows PowerShell.
- An available local TCP port, host network controls for the test server, and a filesystem supporting atomic rename and restrictive POSIX permissions or Windows ACLs. The JDK TLS provider must enable an approved TLS 1.3 cipher suite.

## 1. Clone Repository

**Commands**

```sh
git clone https://github.com/metiago/canis-kms.git
cd canis-kms
git status --short
```

**Expected Result:** The checkout opens; `git status --short` prints nothing.

## 2. Verify Environment

**Commands**

```sh
git rev-parse HEAD
java -version
javac -version
keytool -help
mvn --version
```

**Expected Result:** Record the full commit ID. Java and `javac` report version 21 or newer; `keytool` prints its help; Maven reports 3.9.x. Run the remaining commands from the repository root.

## 3. Build and Test

**Commands**

```sh
mvn --batch-mode --no-transfer-progress clean verify
```

**Expected Result:** `BUILD SUCCESS`; zero test failures and errors. The packaged server exists at `server/target/canis-jar-with-dependencies.jar`. The five legacy `JpawIntegrationTest` cases are disabled because they require an externally started server. Windows also skips the POSIX permission and symbolic-link cases; record skips rather than counting them as passes.

## 4. Create Master Key

**Commands — POSIX**

```sh
sh docs/research/run-code02.sh --scenario normal --files 1 --size 4096 --delay-ms 1000
```

**Commands — Windows PowerShell**

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-code02.ps1 --scenario normal --files 1 --size 4096 --delay-ms 1000
```

**Expected Result:** Exit code 0 and `CODE02_RESULT=passed scenario=normal verified=1/1`. The runner generates a random AES master key in a restricted temporary `private/master.key` and passes its path as `CANIS_SECRET_KEY`. It does not print the key or retain it after a successful run. Run **one** platform command above; steps 5–9 describe this same invocation.

## 5. Generate TLS Materials

**Commands:** Included in the step 4 runner; no second invocation.

**Expected Result:** The runner uses JDK `keytool` to create short-lived PKCS#12 server, producer, recipient, and untrusted identities and the corresponding trust stores in its restricted temporary `private/` directory. The server certificate identifies `localhost`; the producer and recipient have distinct certificates. Store passwords are generated and passed through private environment variables. Success is included in the step 4 exit code.

## 6. Initialize Database

**Commands:** Included in the step 4 runner; no second invocation.

**Expected Result:** The packaged JAR runs once with `--init-db` in the isolated temporary `server/` working directory and creates a new encrypted `db.dat`. Initialization must never be used to replace a missing existing database.

## 7. Start Server

**Commands:** Included in the step 4 runner; no second invocation.

**Expected Result:** The packaged CANIS server starts as its own process from the same temporary `server/` directory. A TLS health check succeeds before the exchange. The runner stops the server after the run.

**Environment Variables Required:** The runner supplies `CANIS_PORT`, `CANIS_USERNAME`, `CANIS_PASSWORD`, `CANIS_SECRET_KEY`, `CANIS_TLS_KEYSTORE`, `CANIS_TLS_KEYSTORE_PASSWORD`, `CANIS_TLS_TRUSTSTORE`, and `CANIS_TLS_TRUSTSTORE_PASSWORD`. It also supplies `CANIS_SERVICE_CREDENTIALS` for the producer identity. Do not export or print the generated values.

## 8. Configure Client

**Commands:** Included in the step 4 runner; no second invocation.

**Expected Result:** The runner configures the official `JpawClient` for `localhost`, the selected server port, separate producer and recipient credentials, and their TLS identity and trust stores. The client validates the server certificate and hostname using TLS 1.3.

**Environment Variables Required:** The runner supplies `CANIS_SERVER_HOST`, `CANIS_SERVER_PORT`, `CANIS_USERNAME`, `CANIS_PASSWORD`, `CANIS_CLIENT_TLS_KEYSTORE`, `CANIS_CLIENT_TLS_KEYSTORE_PASSWORD`, `CANIS_CLIENT_TLS_TRUSTSTORE`, and `CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD` for each worker. No manual client configuration is needed for this run.

## 9. Execute File Exchange

**Commands:** The step 4 command already executed this exchange. Do not rerun it to complete this step.

**Expected Result:** A recipient key is created through `JpawClient`; a separate producer JVM writes a 4096-byte `CANISHYB1` envelope atomically to temporary `shared/`; a delayed, separate recipient JVM decrypts it through CANIS and compares the recovered bytes with the synthetic source. The server, producer, and consumer have distinct process IDs in the result JSON. Only encrypted envelopes enter `shared/`.

## 10. Verify Outputs

**Commands — POSIX**

```sh
ls server/target/code02-*.json server/target/code02-*.producer.csv server/target/code02-*.consumer.csv
cat server/target/code02-*.json
cat server/target/code02-*.producer.csv server/target/code02-*.consumer.csv
```

**Commands — Windows PowerShell**

```powershell
Get-ChildItem server/target/code02-*.json,server/target/code02-*.producer.csv,server/target/code02-*.consumer.csv
Get-Content server/target/code02-*.json
Get-Content server/target/code02-*.producer.csv,server/target/code02-*.consumer.csv
```

**Expected Result:** One JSON result and two CSV records from this run. The JSON reports `outcome=passed`, `verified_files=1`, the checkout commit, `dirty_tree=false`, and distinct server/producer/consumer process IDs. The producer record reports `published`; the consumer record reports `verified` with failure category `none`. The console marker reports `verified=1/1`. A successful run deletes its private temporary root.

**Artifacts To Inspect:** The sanitized `server/target/code02-*.json`, `*.producer.csv`, and `*.consumer.csv` from this run; the Surefire XML under `client/target/surefire-reports/` and `server/target/surefire-reports/`. Do not publish raw server logs, credentials, key material, or plaintext.

## 11. Validate CI

**Commands or URLs**

```sh
git rev-parse HEAD
```

- [GitHub Actions runs](https://github.com/metiago/canis-kms/actions): find a run for the recorded commit.
- [Recorded JOSS-03 run for `e302964d6ed9208be43849191da9c8f2f302c76b`](https://github.com/metiago/canis-kms/actions/runs/44434664908): historical evidence, not a substitute for a run on another commit.

**Expected Result:** Both Ubuntu and Windows `Build and Test` jobs show `completed/success` for the same commit. The Ubuntu job includes `Require POSIX security cases to execute` and `Reproduce CODE-02 asynchronous exchange`; the latter reports `CODE02_RESULT=passed scenario=normal verified=1/1`. Its `code02-evidence-ubuntu-latest` artifact contains the sanitized JSON/CSV, console log, and exit status while retained (seven days). Record a missing or expired artifact as such; do not infer that you inspected it.

## 12. Record Independent Review

**Commands**

```sh
git rev-parse HEAD
git status --short
```

**Expected Result:** The full commit ID matches the workflow JSON and CI run. `git status --short` prints nothing. Copy the sanitized results and fill in:

```text
Reviewer:
Date:
Commit:
OS:
Java:
Maven:
Build Result:
Workflow Result:
Observed Issues:
```

For ordinary, non-sensitive bugs, questions, and support, use the [public issue tracker](https://github.com/metiago/canis-kms/issues) with revision, OS, Java/Maven versions, steps, expected result, and sanitized actual result. For suspected vulnerabilities, follow [SECURITY.md](../SECURITY.md); do not report security issues in public issues.

## Troubleshooting

- `unsafe_permissions` or failure to create restrictive ACLs: use a filesystem and account that permit the required owner-only permissions. A restrictive sandbox failure is an environment failure; do not weaken the fixture permissions.
- `missing_file`, `invalid_format`, or `unreadable_file` for `CANIS_SECRET_KEY`: check the protected file and its path. Never generate a replacement key for an existing database.
- Recovery-required database error or `db.dat.canis-recovery`: follow the [operational recovery runbook](operational-recovery.md); `--init-db` is only for a new database.
- TLS certificate or hostname rejection: confirm the server certificate identifies the configured `CANIS_SERVER_HOST` and the trust stores contain the intended certificates. Do not bypass validation.
- POSIX permission or symbolic-link tests skipped on Windows: use the Ubuntu CI gate for that coverage; record the local skips. If a POSIX environment lacks Java, install JDK 21 or newer before running the POSIX wrapper.
