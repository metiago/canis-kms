# CANIS database master key

`CANIS_SECRET_KEY` is a path to an externally provisioned file containing the raw AES database key
encoded as Base64. It is not the key value itself. The server reads and validates this file before
opening its TLS listener. A missing or rejected key stops startup, even when the database does not
exist. The runtime never generates or replaces this key file. The database itself must be explicitly
initialized once; ordinary startup rejects a missing database.

## Provisioning and access

Generate 16 cryptographically random bytes for compatibility with existing CANIS keys, or 24 or 32
bytes for AES-192 or AES-256, and Base64-encode those bytes. The file may contain line breaks and a
final newline; other whitespace is not supported. The file must be a regular file, no larger than
256 bytes, and must not be a symbolic link. Never put the key in source control, a container image,
logs, process arguments, or a shared configuration file.

Create the file under the identity that runs CANIS. On POSIX systems, that identity must own the
file, the file must have no group or other permissions (for example, mode `0600`), and its immediate
directory must not be writable by group or other users. A protected directory such as mode `0700`
is recommended. On Windows, the service identity must own the file. The file ACL may grant sensitive
access only to that identity, `NT AUTHORITY\SYSTEM`, and `BUILTIN\Administrators`. The immediate
directory ACL must not grant untrusted identities write, delete, or permission-changing access.
Keep all ancestor directories under trusted administrative control as well. If the filesystem
cannot provide POSIX permissions or ACLs, CANIS rejects the key.

For a new POSIX installation, one possible provisioning procedure is:

```sh
install -d -m 700 /secure/canis
umask 077
openssl rand -base64 16 > /secure/canis/master.key
chmod 600 /secure/canis/master.key
export CANIS_SECRET_KEY=/secure/canis/master.key
```

Run these commands as the intended CANIS service identity. Do not repeat key generation for an
existing database. On Windows, generate bytes with a cryptographically secure random generator,
write Base64 as ASCII or UTF-8 without a byte-order mark, and set the file and directory ACLs as
described above before starting CANIS. Protect the environment variable and the key file path in
deployment configuration.

## Startup, recovery, and rotation

CANIS loads the validated key once per process. Changing the file does not rotate the key in a
running server; restart is required, and replacing the file without re-encrypting the database will
make that database unreadable. Existing `CANISDB1` databases and `CANISGCM1` encrypted envelopes
remain unchanged. Authenticated decryption verifies that the key matches an existing database.
A valid but wrong key cannot be detected when the database is absent; initialization is an explicit
operator action and must never be used to replace a lost database. With the server stopped and all
server configuration present, run `java -jar server/target/canis-jar-with-dependencies.jar --init-db`
once from the intended working directory. This creates only a new `db.dat` and refuses to overwrite
an existing path. Keep that working directory under the service identity's control and deny
untrusted users write or delete access; file replacement checks cannot protect against an actor who
controls the directory between checks.

If startup reports that database recovery is required, stop and investigate the configured working
directory, storage access, and protected backups. Restore the original database and its matching
master key, then start CANIS normally. Do not initialize an empty replacement for a lost database.
If a database disappears or changes while the server runs, operations fail closed and the server
stops accepting connections. Database writes stage encrypted data in the same directory and require
an atomic replacement; a filesystem without atomic move support rejects the write. The encrypted
database must be at most 256 MiB so a damaged or hostile file cannot force unbounded startup reads.

Back up the database and its matching key separately with restricted access. If the key is lost,
restore the original key from a protected backup. If startup rejects a key, investigate the reported
failure category (`missing_file`, `unsafe_permissions`, `invalid_format`, or `unreadable_file`) and
correct the cause. CANIS does not print the key or file contents. Do not create a replacement key
for an existing database.
