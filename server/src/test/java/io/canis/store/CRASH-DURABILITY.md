# Database crash durability drill

`CrashDurabilityDrill` is a manual test for a disposable VM. It pauses the real
`KeyValueStore` write after each atomic move, or after the write returns. Run it on every
filesystem and deployment layout that CANIS will support. Keep the Maven checkout on a
different filesystem from the database volume so build output does not flush the volume
under test.

For each stage, start with a fresh directory on the test volume. From the repository root
inside the VM, run `prepare` (replace the path with your disposable directory):

```sh
mvn -q -pl server -am "-Dtest=CrashDurabilityDrill" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dcanis.crash.action=prepare" "-Dcanis.crash.dir=/mnt/canis-drill/old-moved" test
```

Shut down and restart the VM normally once. This makes the database, matching master key,
and known encrypted envelope a stable baseline. Then start the write in a guest console:

```sh
mvn -q -pl server -am "-Dtest=CrashDurabilityDrill" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dcanis.crash.action=write" "-Dcanis.crash.stage=old-moved" "-Dcanis.crash.dir=/mnt/canis-drill/old-moved" test
```

As soon as `CANIS_CRASH_READY` appears, force the VM off **from its host**, without a guest
shutdown or `sync`. With libvirt, use `virsh destroy <vm-name>` on the host. Restart the VM,
preserve its disk image, then inspect the result:

```sh
mvn -q -pl server -am "-Dtest=CrashDurabilityDrill" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dcanis.crash.action=verify" "-Dcanis.crash.stage=old-moved" "-Dcanis.crash.dir=/mnt/canis-drill/old-moved" test
```

Repeat with fresh directories and `canis.crash.stage=new-installed` and `returned`.
The verifier checks that the original envelope remains decryptable through a surviving
database or recovery file. If a recovery file exists, normal store startup and explicit
initialization must refuse it. For `returned`, the acknowledged new key must survive and
the recovery file must be absent. The verifier copies candidates into temporary files;
preserve the disk image before running it. Follow the operational recovery runbook to
restore a surviving candidate to the live database path and confirm normal startup.

To rehearse the process-crash path without powering off a VM, add
`"-Dcanis.crash.mode=halt"` to the `write` command. Maven's nonzero exit is expected;
this rehearsal does **not** establish power-loss durability.

Record the OS, filesystem, mount/volume options, VM disk configuration, stage, observed
`db.dat` and `db.dat.canis-recovery` state, verifier result, and any sanitized failure logs.
Repeat each stage several times. A VM hard-off tests the guest's sudden-loss behavior;
validate physical host and storage power-loss behavior separately if the deployment
requires that guarantee.
