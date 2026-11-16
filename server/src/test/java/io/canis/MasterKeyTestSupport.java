package io.canis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import javax.crypto.KeyGenerator;

public final class MasterKeyTestSupport {
    private MasterKeyTestSupport() {}

    public static void provision(Path path) throws Exception {
        Files.createDirectories(path.getParent());
        restrict(path.getParent(), true);
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        byte[] raw = generator.generateKey().getEncoded();
        try {
            Files.writeString(path, Base64.getEncoder().encodeToString(raw), StandardCharsets.US_ASCII);
            restrict(path, false);
        } finally {
            Arrays.fill(raw, (byte) 0);
        }
    }

    public static void restrict(Path path, boolean directory) throws IOException {
        PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (posix != null) {
            Files.setPosixFilePermissions(
                    path,
                    directory
                            ? EnumSet.of(
                                    PosixFilePermission.OWNER_READ,
                                    PosixFilePermission.OWNER_WRITE,
                                    PosixFilePermission.OWNER_EXECUTE)
                            : EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            return;
        }
        AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (acl == null) {
            throw new IOException("ACL test support unavailable");
        }
        AclEntry owner = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(acl.getOwner())
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .build();
        acl.setAcl(List.of(owner));
    }
}
