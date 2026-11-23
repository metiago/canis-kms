# CODE-01 integrity gate: local candidate evidence

This dated test record reports observed implementation behavior; read its security claims within
the current [Trusted Computing Base and deployment scope](threat-model.md#trust-assumptions-and-trusted-computing-base).
It does not establish protection against privileged host administrators or fully compromised
operating systems. The [master-key guide](master-key.md) distinguishes the current loader's
ancestor-link checks and file/immediate-parent access checks from the deployment responsibility
for higher ancestor access controls. The tests below describe the earlier CODE-01 revision.

This records local CODE-01 verification before the changes were committed. The Git HEAD at
verification was `0e9c889e49b2928c2c9cd84fadaf0368d7c41c91`, with related and unrelated
work modified or untracked. The changes were subsequently committed as
`6f0099ad8112fe4c582da39d4305a5bc5a6eb987` (`fix: close CODE-01 integrity gaps`). The
packaged server jar built before that commit had SHA-256
`2DFEEC556095482E9FD40A65875F93A9BC33DB31DC4A58E4996B85C43C12B146`. The jar is under
`server/target/` and is not a published artifact. The exact commit has not yet been rerun on a
clean checkout or POSIX CI, so this is not a fixed-revision research-release result.

## Gap analysis and implemented plan

1. Existing `Server` constructors already load a store or verify an injected store before binding
   TLS. `ServerTest` now exercises mismatched and changed master-key files through both explicit
   construction paths. The default constructor delegates to the path constructor; the packaged
   normal-startup smoke test rejects a missing database.
2. Loaded `CANISDB1` records previously accepted malformed UTF-8 and unverified RSA fields.
   `KeyValueStore` now validates strict UTF-8, matching service names, RSA encodings, and a
   cryptographic proof that each public/private pair matches before exposing metadata.
   Invalid records stop startup without rewriting the encrypted file.
3. Atomic replacement previously allowed a missing destination to be recreated after the health
   check. A write now first moves the verified old database to a recovery path. If the old path
   has vanished, that move fails and the staged file is not installed. An interrupted replacement
   leaves the old database available for explicit recovery; startup and `--init-db` refuse to
   proceed while the recovery path exists.
4. The client artifact's unused `Entry` type carried a private-key field and Java serialization.
   It now contains only public metadata and is not serializable. The shared-access policy remains
   explicit; the server dispatches decrypt commands only after TLS client-certificate and CANIS
   credential authentication.

## Acceptance evidence

| CODE-01 requirement | Evidence | Result |
| --- | --- | --- |
| Invalid master key or database state fails safely | `ServerTest`, `MasterKeyLoaderTest`, `KeyValueStoreTest`, `DatabaseReplacementTest`; packaged normal startup against a missing `db.dat` exited 1 and created no database | Passed on Windows |
| Malformed authenticated records and private-key exposure fail closed | `KeyValueStoreTest` covers malformed UTF-8, invalid and mismatched RSA fields, private encoding in the public field, non-rewrite, and sanitized startup logs; `ClientHandlerTest` and client `EntryTest` cover responses, audit fields, and serialization | Passed |
| Missing database cannot be silently recreated by a write | `DatabaseReplacementTest` covers deletion after verification, unsupported atomic move, and interrupted replacement; `KeyValueStoreTest` covers failed write without replacement | Passed for tested filesystem behavior |
| Valid existing storage and envelopes remain usable | `KeyValueStoreTest` loads and rewrites an independently encoded valid `CANISDB1` record; `ServerEndToEndTest` verifies the official client's TLS file workflow and decrypt after restart | Passed |
| Invalid certificates and credentials are rejected before decrypt | `ServerTlsSecurityTest`, `ServerTlsCertificateTest`, and `SharedTrustDomainResearchTest` exercise the TLS and credential gates; `ClientHandlerTest` checks requester and selected key in a secret-free decrypt audit event | Passed |

Verification commands: `mvn -q -pl server,client spotless:apply`, `mvn -q test`, and
`mvn -q -pl server package -DskipTests`. The final Surefire reports contained 27 suites and
158 tests: 0 failures, 0 errors, and 8 skipped. Five skipped cases require a separately started
client integration server. Three skipped cases require filesystem permission or symlink behavior
unavailable on this Windows host. The master-key log-sanitization test was rerun after its final
assertion and passed. `git diff --check` reported no whitespace errors.

The packaged smoke test used a temporary, access-restricted master key and required environment
variables. Normal startup with no `db.dat` exited 1 without creating one. `--init-db` exited 0 and
created the database. Repeating `--init-db` exited 1; the database SHA-256 was unchanged.

## Operational and compatibility limits

- The write sequence uses two atomic moves, not one atomic transaction. An interruption can leave
  the recovery file and no active database; an operator must inspect and restore it. Directory
  metadata durability and unsupported filesystems need deployment validation.
- The master key is cached. Runtime loss of its file is not detected until a later configuration
  check or restart. Database loss while idle is detected at the next connection or operation.
- The service identity or trusted administrators must control all ancestor directories.
  Path-based checks cannot defend against a non-privileged actor mistakenly given
  replacement rights on those directories.
- Each operation reads and hashes the encrypted database. Large-database performance has not been
  measured. The database limit is 256 MiB.
- A compromised authenticated identity can decrypt under every stored key name. Per-key policy is
  a separate trust-model change. Audit schema and integrity verification remain with Issues #11–19.
- The removed client `Entry.privateKey` component changes that Java type's API for any caller that
  used it. The CANISP format and valid `CANISDB1`/file-envelope formats are unchanged.
- POSIX permission and symlink behavior, Windows owner mismatch and inherited ACL behavior, and
  crash recovery across supported filesystems need independent validation before a broad release.
