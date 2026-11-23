# Issue #1: Protect CANIS_SECRET_KEY — implementation report

## Status and scope

Implementation is staged in the working tree based on `bea137cf214d842e3513d6c24b314040fb3e59cc`.
This report is not a closure claim: the implementation and workflow changes are not committed,
POSIX CI has not run on them,
and [Issue #1](https://github.com/metiago/canis-kms/issues/1) remains open. The intended
[threat model](../threat-model.md) trusts the organization-controlled host, administrators,
deployment automation, service account, and corporate identity systems. The controls address
unsafe configuration, accidental exposure, non-privileged access, and fail-closed startup.

## Implemented controls

| Requirement | Implementation and test |
| --- | --- |
| External key; missing, malformed, unreadable, and oversized files | Existing bounded `MasterKeyLoader` behavior remains. `MasterKeyLoaderTest` covers missing, malformed, empty, oversized, and POSIX unreadable files. |
| File ownership and permissions | Existing POSIX owner/mode and Windows owner/ACL policy remains. New POSIX fixtures test a differently owned file and parent directory. Windows tests exercise owner mismatch through the same Java ACL policy used by the loader; an on-disk owner-change case runs when the test identity has the required right. |
| Direct and ancestor symbolic links | The loader checks each directory component from the root to the immediate parent with `NOFOLLOW_LINKS` before checking and opening the key. Direct file and ancestor-link tests reject those paths. |
| Pre-bind validation | `Server.start()` reloads and compares the configured key before creating or binding the TLS listener. A test changes, corrupts, or removes the key after construction and confirms startup rejection. |
| Sanitized failures | The loader retains fixed categories and exceptions without a sensitive cause. Tests check that key contents, a configured password marker, and the key path do not appear in captured startup messages or exceptions. |
| Existing database binding | Existing store tests cover matching-key restart and wrong-key rejection without replacing the database. The new startup test also checks that rejection leaves database bytes unchanged. No key-generation path was added. |

## Local validation

On the local Windows host, `mvn --batch-mode --no-transfer-progress clean verify` passed on the
final staged code: 60 client tests with 5 pre-existing integration skips, and 106 server tests
with 7 platform/privilege skips; no failures or errors. `MasterKeyLoaderTest` reported 12 tests,
no failures or errors, and 6 skips. `ServerTest` passed all 10 cases. The Windows owner-policy
and inherited-ACL tests executed and passed; `VerifyWindowsCoverage.java` reported
`CANIS_WINDOWS_KEY_COVERAGE=PASS`. The on-disk
Windows owner-change test skipped because this local account could not assign a different owner
through Java's ACL view. The POSIX-only tests and symbolic-link creation tests are skipped on
this Windows host. The historical `VerifyPosixCoverage.java` still passed its archived JOSS
fixture. `git diff --check` passed.

The Ubuntu CI workflow provisions two non-privileged owner-mismatch fixtures, runs the full Maven
suite, and rejects missing, failed, or skipped owner, permission, unreadable-file, and direct or
ancestor-link tests. The Windows workflow similarly requires the owner-policy and explicit and
inherited ACL tests. These workflows have not run against this working tree. The release workflow
also requires the expanded POSIX gate.

## Deployment limits and future hardening

The ancestor check is deliberately path-based. It is not a race-resistant traversal guarantee,
and it does not analyze Windows reparse points beyond symbolic links reported by Java. The loader
checks ownership and access settings on the key and immediate parent; deployment automation must
keep higher ancestors under trusted administrative control. The loaded key is cached after
startup and requires external file and service monitoring. Native filesystem APIs, complete NTFS
security-descriptor analysis, macOS-specific ACL engines, and defenses against privileged host
administrators are outside Issue #1's agreed scope.

## Evidence required before closing the issue

1. Run both Ubuntu and Windows CI jobs on the committed revision, with the Issue #1 coverage
   gates passing and no required security case skipped.
2. Record the commit and CI run links here, including the POSIX owner and ancestor-link results.
3. Confirm the release and deployment guides still match the committed behavior, then close
   Issue #1 with a link to this report.
