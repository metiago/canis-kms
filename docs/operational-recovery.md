# CANIS operational recovery runbook

CANIS is an internal service on organization-managed infrastructure. Recovery is an
operator-controlled process. CANIS provides no automatic failover, distributed recovery, or
cloud-KMS-style replication. Use the [master-key guide](master-key.md) for key-file validation
details and the [threat model](threat-model.md) for the trusted-host assumptions.

## Database recovery

1. Stop CANIS and keep application traffic away from the recovery instance. Record the working
   directory, `CANIS_SECRET_KEY` path, observed error, and time of failure.
2. Preserve the current `db.dat`, master-key file, any `db.dat.canis-recovery`, relevant logs,
   and backup metadata in protected storage before changing live paths. Keep the database and
   key under separate restricted access controls. Do not overwrite or discard a recovery
   candidate during investigation.
3. Determine the root cause: check storage availability and path changes, recent deployments,
   interrupted writes, file ownership and permissions, and master-key replacement. Resolve the
   underlying fault before restarting.
4. Identify a valid encrypted database and its matching master-key file from verified backups
   or preserved recovery candidates. Check their provenance and restore point. A key file that
   passes format and permission checks is not necessarily the key that encrypted the database.
5. Restore the verified pair to the intended working directory and configured key path with the
   required ownership and access settings. Preserve any live `db.dat.canis-recovery` first, then
   move it out of the live database directory: CANIS refuses startup while it exists there.
   Never combine a database with an unrelated key or initialize an empty database as a shortcut.
6. Start CANIS with application traffic still restricted. Confirm that startup accepts and
   authenticates the database. Through the official client, decrypt an **existing encrypted test
   envelope** created before the incident and compare the recovered plaintext with its known
   expected value. Only after that succeeds should operators return the service to normal traffic.
   If validation fails, stop the service and investigate the pair; do not treat startup alone as
   proof of recovery.

## Missing database

Normal startup rejects a missing `db.dat`. `--init-db` is for a genuinely new installation:
it creates an empty database and cannot recover stored service keys or make existing envelopes
decryptable. Do not use it to recover a missing database, even if the master-key file remains.

Investigate `db.dat.canis-recovery` when `db.dat` is missing after an interrupted write, or when
startup reports recovery required and the recovery file exists. During a write, CANIS moves the
previous database to that path before moving the new one into place. The recovery file may be a
candidate for the previous committed state, not proof that it is current or valid. Preserve it,
establish its matching key, and follow the database recovery sequence above. If no valid pair is
available, existing encrypted envelopes cannot be recovered through CANIS.

## Master-key problems

| Symptom | Operator action |
| --- | --- |
| Missing key file or `missing_file` | Check the configured path, mount, and protected backups. Restore the original key matched to the database; do not generate a replacement. |
| Incorrect key file | A file may pass loader checks but fail database authentication. Find the key paired with the selected database and restore that verified pair. |
| Replaced key file | Stop the service, preserve both versions if available, determine when replacement occurred, and restore the key paired with the database. Restart to load the corrected file. |
| Permission or ownership failure, including `unsafe_permissions` or `unreadable_file` | Check the service identity, file and immediate-parent ownership, POSIX mode or Windows ACL, and path accessibility. Correct the deployment configuration under trusted administrative control; do not weaken access restrictions. |

After any master-key correction, perform the same existing-envelope decryption check before
returning to service. A missing, wrong, or irretrievable key cannot be reconstructed from the
encrypted database.
