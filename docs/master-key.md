# CANIS database master key

`CANIS_SECRET_KEY` must point to a file containing a Base64-encoded AES key—not the key value itself. CANIS validates and loads this file before accepting connections and never generates or replaces it automatically. If the key or database is missing or invalid, startup fails closed.

These checks prevent accidental exposure and misconfiguration on corporate-managed hosts. They do not protect against trusted administrators, the OS, or the CANIS service account (see the [threat model](threat-model.md)).

## Create and protect the key

Generate 16 bytes (AES-128, compatible with existing keys), 24 bytes (AES-192), or 32 bytes (AES-256) of cryptographically random data. Save the Base64 output to a regular file up to 256 bytes. Only line breaks and a trailing newline are permitted as whitespace.

**Security requirements:**
* **No symlinks:** CANIS rejects symlinks at the key path and reads using `NOFOLLOW_LINKS`.
* **Parent directories:** CANIS checks the key file and its immediate parent directory, but operators must ensure all higher-level directories are owned by trusted administrators with no untrusted write access.
* **POSIX permissions:** The CANIS service account must own the key file (`0600`) and its immediate directory (`0700`), denying all group and other access.
* **Windows ACLs:** The service account must own the file. Restrict sensitive access solely to the service account, `NT AUTHORITY\SYSTEM`, and `BUILTIN\Administrators`. Deny untrusted users write, delete, and permission-change rights on the parent directory.
* **Storage rules:** Never store the key in source control, container images, logs, process arguments, or shared configs.

**POSIX setup example** (run as the CANIS service user):
```sh
install -d -m 700 /secure/canis
umask 077
openssl rand -base64 16 > /secure/canis/master.key
chmod 600 /secure/canis/master.key
export CANIS_SECRET_KEY=/secure/canis/master.key
```
On Windows, generate the key using a cryptographically secure RNG, save as ASCII or UTF-8 without a BOM, and apply the required ACLs.

## Control responsibility boundary

| Control | Enforced by CANIS | Operator responsibility |
| :--- | :---: | :---: |
| Key file type and size (≤ 256 bytes) | Yes | No |
| Key format (Base64 AES: 16, 24, or 32 bytes) | Yes | No |
| Immediate parent directory type, owner, and permissions | Yes | No |
| Key file ownership and permissions/ACLs | Yes | No |
| Ancestor directory ownership, ACLs, and path integrity | No | Yes |
| Host, OS, and filesystem integrity | No | Yes |
| Administrator trustworthiness | No | Yes |

## Initialize and recover the database

To initialize a brand-new database, stop the server and run the following command from your working directory:
```sh
java -jar server/target/canis-jar-with-dependencies.jar --init-db
```
This creates `db.dat` and will not overwrite existing files. Keep the working directory restricted to the service account.

**Rules for existing databases:**
* **Do not rotate keys in place:** Replacing the key without re-encrypting the database renders it unreadable. A server restart is required to load any key changes.
* **Never re-initialize a lost database:** Running `--init-db` creates an empty database without verifying the key. Restore lost databases from backups instead.
* **Troubleshooting:** If startup fails with `missing_file`, `unsafe_permissions`, `invalid_format`, or `unreadable_file`, fix the key file permissions or path. For database corruption or loss, restore both the database and matching key from separate, restricted backups, verify decryption on a known envelope, and follow the [operational recovery runbook](operational-recovery.md).

## Database writes, limits, and monitoring

* **Atomic updates:** Every write stages new data, moves the verified `db.dat` to `db.dat.canis-recovery`, and moves the staged file to `db.dat`. If a crash leaves a `.canis-recovery` file behind, startup will block until you inspect the files and manually restore the verified database.
* **Size limit:** The encrypted database is capped at **256 MiB**. Every write operation hashes the entire database to detect tampering, which increases latency on larger files.
* **Runtime monitoring:** CANIS checks the database on every connection or write, and fails closed if it is altered or missing. However, key file changes after startup are not detected until the next restart or explicit configuration check. Monitor the key file, database directory, backups, and service health externally.
