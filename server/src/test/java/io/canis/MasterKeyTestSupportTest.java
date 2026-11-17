package io.canis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.canis.crypto.SymmetricGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MasterKeyTestSupportTest {
    @TempDir
    Path tempDir;

    @Test
    void provisionsUsableWindowsKeyUnderDirectoryWithBroadInheritedAcl() throws Exception {
        assumeTrue(Files.getFileAttributeView(tempDir, PosixFileAttributeView.class) == null);
        AclFileAttributeView rootAcl = Files.getFileAttributeView(tempDir, AclFileAttributeView.class);
        assumeTrue(rootAcl != null);

        var users = tempDir.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("BUILTIN\\Users");
        AclEntry broadAccess = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(users)
                .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA)
                .build();
        var entries = new ArrayList<>(rootAcl.getAcl());
        entries.add(broadAccess);
        rootAcl.setAcl(entries);

        Path fixtureDirectory = Files.createDirectory(tempDir.resolve("fixture"));
        assertTrue(Files.getFileAttributeView(fixtureDirectory, AclFileAttributeView.class).getAcl().stream()
                .anyMatch(entry -> entry.type() == AclEntryType.ALLOW
                        && entry.principal().equals(users)
                        && entry.permissions().contains(AclEntryPermission.WRITE_DATA)));

        Path key = fixtureDirectory.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        var keyAcl = Files.getFileAttributeView(key, AclFileAttributeView.class);
        var directoryAcl = Files.getFileAttributeView(fixtureDirectory, AclFileAttributeView.class);
        var current = key.getFileSystem()
                .getUserPrincipalLookupService()
                .lookupPrincipalByName(ProcessHandle.current().info().user().orElseThrow());
        assertTrue(keyAcl.getOwner().equals(current));
        assertFalse(keyAcl.getAcl().stream()
                .anyMatch(entry -> entry.type() == AclEntryType.ALLOW
                        && entry.principal().equals(users)
                        && entry.permissions().contains(AclEntryPermission.READ_DATA)));
        assertFalse(directoryAcl.getAcl().stream()
                .anyMatch(entry -> entry.type() == AclEntryType.ALLOW
                        && entry.principal().equals(users)
                        && entry.permissions().contains(AclEntryPermission.WRITE_DATA)));
        assertDoesNotThrow(() -> new SymmetricGenerator(key.toString()));
    }
}
