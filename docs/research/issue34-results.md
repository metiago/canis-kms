# Issue #34 controlled synthetic fault observations: Windows, 2026-11-18

## Result and scope

The final campaign executed seven case variants three times each: **21 isolated invocations**.
All 21 final JSON records report `status: passed` under the [prespecified method](issue34-method.md).
Each run initialized a synthetic database, published a 4 KiB encrypted envelope through the
official TLS client, and byte-verified baseline recovery before injecting a fault. The campaign
produced [159 sanitized JSON/CSV artifacts](issue34-artifacts/) and a [SHA-256 manifest](issue34-artifacts.sha256).
The [artifact verifier](verify-issue34.py) reported
`ISSUE34_EVIDENCE=passed cases=7 repetitions=3 artifacts=159`; an independent PowerShell
`Get-FileHash` check of all 159 manifest entries found zero mismatches. These are observations
under local synthetic faults, not evidence of attack resistance, cryptographic strength,
compliance, general availability, or production readiness.

## Environment and execution

Source HEAD was `4f6de0568b5ccc6a2947819333d07aec2ba6a3cb`; every final JSON records
`dirty_tree: true` because Issue #34 research files were uncommitted. All 21 records report
the same `Issue34Harness.java` SHA-256
`DC24F8221C35E4561BA249FA496BCAB8B37B94DD24FA5748FD449B9060133843` and packaged
server jar SHA-256 `230659DF50B2AC0CA2B147136ACF70D85E419E5C954F0724796D28E97FDEC1CA`.
The host reported Windows 11 NT `10.0.26100.0`, 13th Gen Intel Core i7-1355U, and 12 logical
processors. Oracle Java 21.0.7 ran the harness and children; Maven 3.9.12 used Eclipse Adoptium
Java 21.0.6. Each server and worker used `-Xms128m -Xmx512m`. Current filesystem and RAM details
were not successfully queried in this run; the earlier CODE-03 report documents this host's
local NTFS preflight, but is not a fresh Issue #34 measurement.

From the repository root, `mvn --batch-mode --no-transfer-progress clean verify` exited 0:
60 client tests (five skipped), 99 server tests (three skipped), zero failures or errors. Then:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-issue34-campaign.ps1
```

The campaign exited 0 and printed `ISSUE34_CAMPAIGN=passed cases=7 repetitions=3`. It built the
packaged server with tests skipped for that invocation, compiled the research harness, and ran
the matrix sequentially. Final artifacts were copied from ignored `server/target/issue34/` to
`docs/research/issue34-artifacts/`. The verifier and manifest command was:

```powershell
python docs/research/verify-issue34.py docs/research/issue34-artifacts --write-manifest docs/research/issue34-artifacts.sha256
```

It checked the exact matrix, baseline/recovery rows, worker/event CSV agreement, output presence,
database/audit hash relationships, source/jar consistency, and selected secret/path markers. The manifest covers
every final artifact. Its own SHA-256 is
`97F030795D15BF6B55B6B128BA5F3C1BF0552DB5A48624F9DFFD653B5010A557`.

## Scenario observations

Each variant has three separate passing run records. `r1` links below are examples; `r2` and
`r3` use the same name pattern and are in the manifest. All 21 baselines had successful startup,
publication, and authenticated byte verification. Failed worker rows report the sanitized client
category `io_or_authentication_failure`; it does **not** identify a specific TLS, network, or
database exception. The server-log markers supply narrower startup or live-store observations.
No failed recovery attempt left its own output file.

| Scenario | Observation | Evidence | Status |
| --- | --- | --- | --- |
| A — master-key loss | In 3/3 runs, removing the file while the server ran was followed by successful live publication and recovery. Restart exited 1, did not become ready, and logged `missing_file`. A new publication and recovery failed without output. Restoring the original synthetic key permitted byte-verified recovery; database hashes matched. | [A r1 JSON](issue34-artifacts/issue34-run-A-standard-r1.json), [events](issue34-artifacts/issue34-run-A-standard-r1.events.csv), [manifest](issue34-artifacts.sha256) | Observed 3/3 |
| B — master-key replacement | In 3/3 runs, live publication and recovery succeeded after replacing the file with another valid synthetic key. Restart exited 1, did not become ready, and logged `database_startup_rejected`. New publication and recovery failed without output. Restoring the matching original key permitted byte-verified recovery; database hashes matched. | [B r1 JSON](issue34-artifacts/issue34-run-B-standard-r1.json), [events](issue34-artifacts/issue34-run-B-standard-r1.events.csv), [manifest](issue34-artifacts.sha256) | Observed 3/3 |
| C — database corruption | In 3/3 live runs, a final-byte change was followed by failed publication and recovery, and the server log recorded `changed_database`. In 3/3 stopped runs, restart of the changed database exited 1 and logged `database_startup_rejected`. Live runs also rejected restart. Faulted restart served no recovery output in either variant. Restoring the exact database backup permitted byte-verified recovery, with the original database hash restored. | [live r1 JSON](issue34-artifacts/issue34-run-C-live-r1.json), [stopped r1 JSON](issue34-artifacts/issue34-run-C-stopped-r1.json), [live events](issue34-artifacts/issue34-run-C-live-r1.events.csv), [manifest](issue34-artifacts.sha256) | Observed 3/3 per variant |
| D — audit modification or deletion | In 3/3 modifications and 3/3 deletions, the external before/after log SHA-256 differed. The server then started, logged `tls_listener_started`, and completed publication and byte-verified recovery. No application audit-integrity rejection was recorded in these runs. The altered/deleted line was **not restored**. | [modified r1 JSON](issue34-artifacts/issue34-run-D-modified-r1.json), [deleted r1 JSON](issue34-artifacts/issue34-run-D-deleted-r1.json), [modified events](issue34-artifacts/issue34-run-D-modified-r1.events.csv), [manifest](issue34-artifacts.sha256) | Observed 3/3 per variant |
| E — recovery and restart | In 3/3 runs, restart with the changed database exited 1 and logged `database_startup_rejected`; subsequent publication and recovery failed without output. Restoring the untouched database and matching key, then starting without `--init-db`, permitted byte-verified recovery. Restored database SHA-256 equaled the pre-fault hash. | [E r1 JSON](issue34-artifacts/issue34-run-E-standard-r1.json), [events](issue34-artifacts/issue34-run-E-standard-r1.events.csv), [manifest](issue34-artifacts.sha256) | Observed 3/3 |

**Confidentiality observation:** A and B released recovered bytes to the admitted recipient while
the original key remained cached in the running server; D released recovered bytes after audit-file
alteration. Their worker rows report `verified`, meaning authenticated decryption and independent
byte equality. Faulted restarts in A/B/C/E yielded no new recovered output in the tested attempts.
No conclusion follows about earlier outputs, server memory, or other clients.

**Integrity observation:** The tested database changes prevented startup; the C/live server also
logged a changed-database state during operation. Independent D log hashes detected changed
bytes, while the observed CANIS restart and file workflow continued. This does not establish an
application audit-integrity verifier. Restore comparisons cover exact synthetic backup copies,
not filesystem crash durability.

**Recovery observation:** A/B/C/E required explicit fixture restoration before the original
envelope verified again. D had no audit-log restoration. The `issue34-v1` field
`restored_and_verified` means *post-fault workflow verified* in D only; see the method's schema
note. No automatic repair or deployment backup procedure was tested.

## Pilot and failed-attempt record

The first sandboxed A attempt stopped before fixture provisioning because restrictive Windows
ACL setup was denied; it produced no Issue #34 JSON. Its empty partial temporary root was
inspected and removed. The first permitted A attempt completed baseline, live recovery, and
missing-key restart refusal, then failed its recovery control because copying the synthetic key
back did not preserve the required Windows ACL. Its [failed JSON](issue34-pilot/A-standard-r1.json)
and [events](issue34-pilot/A-standard-r1.events.csv) remain in the separate pilot set; the private
fixture was inspected and removed. The research-only harness now reapplies the restrictive ACL
after restoring the synthetic key. A subsequent seven-case first pilot series passed, then the
final source added categorical live-server and recovery-startup event rows. The pilot set is
[separately hashed](issue34-pilot-artifacts.sha256), was not pooled with the final 21 runs, and
has a different harness source hash. No production Java was changed.

## Reproducibility and acceptance review

The [method](issue34-method.md) gives single-case Windows and POSIX commands, fixture setup,
expected categorical checks, and cleanup. The Windows campaign script reproduces the exact final
matrix into fresh output names. It refuses pre-existing JSON paths. A new run generates different
ciphertexts, credentials, process IDs, timestamps, and hashes. Compare outcomes and invariants,
not artifact byte equality. To recheck published artifacts without rerunning:

```powershell
python docs/research/verify-issue34.py docs/research/issue34-artifacts
```

| Acceptance criterion | Evidence | Status |
| --- | --- | --- |
| Prespecified synthetic, isolated fixture workflow | Method, research-only coordinator, private temporary-root guards, 21 baseline rows | Complete for fixture isolation; host network isolation unverified |
| A/B master-key loss and replacement | Six final JSON/event pairs with live, restart, failed-request, and restore phases | Complete for tested faults |
| C database corruption while running and stopped | Six final JSON/event pairs; `changed_database` and startup-rejection markers | Complete for tested faults |
| D audit-line modification and deletion | Six final JSON/event pairs; independent before/after hashes and continued workflow | Complete as observation; built-in integrity verification absent |
| E restore and restart after corruption | Three final JSON/event pairs and equal pre-fault/restored database hashes | Complete for exact-copy synthetic restore |
| Startup, encryption, decryption, recovery, errors, logs, output presence | Phase event CSVs and worker CSVs for every final run | Complete at recorded categorical granularity |
| Raw evidence, reproduction scripts, artifact integrity | 159 final artifacts, manifest, verifier pass, independent 159/159 hash check | Complete for local Windows campaign |
| Full backlog Issue #34 outage, key deletion, envelope, admission matrix | This campaign covers the five requested storage/audit scenarios only | Open |
| Fixed-revision and POSIX reproduction | All JSONs say `dirty_tree: true`; POSIX wrapper was not executed here | Open |

## Limits and operational impact

All inputs, master keys, credentials, TLS stores, databases, and envelopes were generated for the
study. No real secret or production database was used; published files contain no selected key,
password, PKCS#12, or private temporary-path markers. Successful fixture roots were deleted, and
the two early failed roots were removed after inspection. The fault harness configured only
localhost CANIS connections and no external KMS. The separate post-campaign `mvn spotless:apply` formatting command fetched
Maven plugin metadata from Maven Central; it was not part of a fault run. The harness uses localhost
clients, but the packaged server binds available interfaces;
this run did not independently verify host firewall or network-namespace isolation. That is an
environmental limit, not a claim of exposure or compromise.

The study used one Windows host, a 4 KiB baseline file and a 256 KiB ordinal-1 publication attempt
where applicable, short fault windows, exact backup copies,
and three repetitions per case. Event CSV timestamps order completed actions; they do not measure
internal fault-detection latency or an availability objective. Client failures were classified
generically. No POSIX campaign, independent clean-checkout rerun, interrupted two-move database
write, crash, long outage, key deletion, envelope fault, or invalid-admission matrix was executed
here. This research-only harness changes no production behavior, TLS rule, algorithm, protocol, or
shared-trust policy. Future work should address remaining Issue #34 backlog faults and fixed
revision reproduction before broader study claims.
