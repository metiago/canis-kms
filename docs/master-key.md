# CANIS database master key

`CANIS_SECRET_KEY` is a path to an externally provisioned file containing the raw AES key encoded
as Base64; it is not the key value. CANIS validates and loads the file before opening its TLS
listener. The runtime never generates or replaces it. A missing or rejected key stops startup
even if the database is absent; normal startup also rejects a missing database.

The purpose of these checks is to prevent provisioning mistakes and accidental exposure, detect
unsafe access settings, and fail closed at startup when the key cannot be trusted. CANIS is
designed for an organization-managed corporate host. The operating system, infrastructure
administrators, deployment automation, CANIS service account, and corporate identity systems
are trusted; the file checks are not a barrier against those privileged actors. See the
[threat model](threat-model.md) for the security objectives and excluded threats.

## Create and protect the key

Generate 16 cryptographically random bytes for compatibility with existing CANIS keys, or 24 or
32 bytes for AES-192 or AES-256. Base64-encode the bytes in a regular file of at most 256 bytes.
Line breaks and a final newline are accepted; other whitespace is not. The loader rejects a
symbolic link at the key-file path and reads the key with `NOFOLLOW_LINKS`. It does not walk and
validate every ancestor directory; higher-level path components remain an operator responsibility.
These checks do not guarantee safety against an actor who can replace path components during the
check. Provision the path beneath directories controlled by the service account or trusted
administrators, without untrusted writable ancestors.

On POSIX, the CANIS service identity must own the file. Deny all group and other permissions
(for example, `0600`) and group or other write access to its immediate directory; `0700` is a
suitable directory mode. On Windows, the service identity must own the file. Only that identity,
`NT AUTHORITY\SYSTEM`, and `BUILTIN\Administrators` may have sensitive file access. The
immediate directory ACL must deny untrusted identities write, delete, and permission-changing
access. Keep ancestor directories under trusted administrative control. CANIS rejects
filesystems that cannot provide the required POSIX permissions or ACLs.

The loader checks ownership and access settings on the key and its immediate parent. It does
not verify the ownership or ACLs of every higher directory. On systems with
additional ACL mechanisms, deployment automation must also verify the effective access that
non-privileged identities have to the key and path. Treat a path with untrusted writable
ancestors as unsafe even if the loader accepts the key. Host administrators and the service
account can access or change the key by design.

## Control responsibility boundary

The table describes what the master-key loader validates. Operators still provision the key
and its path; "Operator responsibility" identifies controls outside CANIS validation.

| Control | Enforced by CANIS | Operator responsibility |
| --- | --- | --- |
| Key file type and size validation | Yes | No |
| Key format validation (Base64-encoded AES key of 16, 24, or 32 bytes) | Yes | No |
| Immediate parent directory type, ownership, and access validation | Yes | No |
| Key file ownership validation | Yes | No |
| Key file permission or ACL validation | Yes | No |
| Higher-level directory ownership, access, and path integrity | No | Yes |
| Host and filesystem integrity | No | Yes |
| Administrator trustworthiness | No | Yes |
| Operating-system security | No | Yes |

CANIS detects and rejects known unsafe conditions within this implemented validation scope.
It does not establish trust in the entire operating system, protect against privileged
administrators, or guarantee filesystem integrity. Deployment automation and operating
procedures remain responsible for host security and the unvalidated path components.

For a new POSIX installation, run as the CANIS service identity:

```sh
install -d -m 700 /secure/canis
umask 077
openssl rand -base64 16 > /secure/canis/master.key
chmod 600 /secure/canis/master.key
export CANIS_SECRET_KEY=/secure/canis/master.key
```

On Windows, use a cryptographically secure random generator, write Base64 as ASCII or UTF-8
without a byte-order mark, then set the file and directory ACLs. Keep the key out of source
control, container images, logs, process arguments, and shared configuration files. Protect the
environment variable and key path in deployment configuration.

## Initialize and recover the database

Initialize a new database once, with the server stopped and its normal configuration present:

```sh
java -jar server/target/canis-jar-with-dependencies.jar --init-db
```

Run this from the intended working directory. The command creates only a new `db.dat` and
refuses to overwrite an existing path. Keep the directory under the service identity's control
and deny untrusted write or delete access. A non-privileged actor with write or delete access
to a path component can race path-based checks; such access violates the deployment assumptions.

For an existing database, keep the original key. Replacing the file without re-encrypting the
database makes the database unreadable; restarting is required to load any changed file.
Existing `CANISDB1` databases and `CANISGCM1` envelopes remain unchanged. Authentication of an
existing database verifies that its key matches. An absent database offers no such check, so
never use `--init-db` to replace a lost database.

If startup reports `missing_file`, `unsafe_permissions`, `invalid_format`, or `unreadable_file`,
correct the key-file problem. CANIS does not print its contents. If database recovery is
required, investigate the working directory and storage, then restore the original database and
matching key from protected backups. Test the restored pair by decrypting a known envelope. Back
up the database and key separately with restricted access.
Use the [operational recovery runbook](operational-recovery.md) for the full sequence, including
handling `db.dat.canis-recovery`.

## Database replacement and limits

Each write stages encrypted data in the database directory, atomically moves the verified old
database to `db.dat.canis-recovery`, then atomically moves the new file to `db.dat`. A missing
old database cannot be recreated by a write. An interrupted replacement can leave the recovery
file and no `db.dat`; startup refuses to run while the recovery file exists. Inspect both files
before restoring the verified old database. Both moves require atomic-move support, but together
they are not one atomic transaction. Directory metadata durability depends on the filesystem;
validate crash recovery and POSIX permission behavior on the deployment filesystem.

The encrypted database is limited to 256 MiB to bound startup reads. Each store operation also
reads and hashes it to detect unexpected changes, which can add substantial latency for large
databases; no supported throughput or database-size performance claim has been measured.
Database loss while idle is detected on the next connection or store operation. Loss or
replacement of the cached key file is not detected until the next startup or explicit store
configuration check. Server startup performs that configuration check again immediately before
binding its listener; changes after binding still require external monitoring. Monitor the key
file, database directory, backups, and service availability
externally. If the database disappears or changes during operation, CANIS fails closed and stops
accepting connections.
