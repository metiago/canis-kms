package io.canis.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads an externally provisioned database key before the server can accept connections. */
final class MasterKeyLoader {
    private static final Logger logger = LoggerFactory.getLogger(MasterKeyLoader.class);
    private static final int MAX_FILE_BYTES = 256;
    private static final Set<AclEntryPermission> FILE_SENSITIVE_PERMISSIONS = EnumSet.of(
            AclEntryPermission.READ_DATA,
            AclEntryPermission.WRITE_DATA,
            AclEntryPermission.APPEND_DATA,
            AclEntryPermission.DELETE,
            AclEntryPermission.WRITE_ACL,
            AclEntryPermission.WRITE_OWNER);
    private static final Set<AclEntryPermission> DIRECTORY_SENSITIVE_PERMISSIONS = EnumSet.of(
            AclEntryPermission.WRITE_DATA,
            AclEntryPermission.APPEND_DATA,
            AclEntryPermission.DELETE_CHILD,
            AclEntryPermission.DELETE,
            AclEntryPermission.WRITE_ACL,
            AclEntryPermission.WRITE_OWNER);

    private MasterKeyLoader() {}

    static SecretKey load(String location) {
        try {
            if (location == null || location.isBlank()) {
                throw new KeyFileFailure("missing_path");
            }
            Path path = Path.of(location).toAbsolutePath().normalize();
            checkAncestorDirectories(path);
            BasicFileAttributes attributes =
                    Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile()) {
                throw new KeyFileFailure("not_regular_file");
            }
            if (attributes.size() == 0 || attributes.size() > MAX_FILE_BYTES) {
                throw new KeyFileFailure("invalid_format");
            }
            checkPermissions(path);
            byte[] content;
            try (InputStream input = Files.newInputStream(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
                content = input.readNBytes(MAX_FILE_BYTES + 1);
            }
            try {
                BasicFileAttributes after =
                        Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (!after.isRegularFile()
                        || after.size() != content.length
                        || (attributes.fileKey() != null
                                && !attributes.fileKey().equals(after.fileKey()))) {
                    throw new KeyFileFailure("file_changed");
                }
                checkPermissions(path);
                if (content.length == 0 || content.length > MAX_FILE_BYTES) {
                    throw new KeyFileFailure("invalid_format");
                }
                return decode(content);
            } finally {
                Arrays.fill(content, (byte) 0);
            }
        } catch (KeyFileFailure e) {
            return fail(e.category);
        } catch (NoSuchFileException e) {
            return fail("missing_file");
        } catch (InvalidPathException e) {
            return fail("invalid_path");
        } catch (IOException | SecurityException e) {
            return fail("unreadable_file");
        }
    }

    private static void checkAncestorDirectories(Path path) throws IOException, KeyFileFailure {
        Path directory = path.getRoot();
        if (directory == null) {
            throw new KeyFileFailure("invalid_path");
        }
        requireDirectory(directory);
        for (int index = 0; index < path.getNameCount() - 1; index++) {
            directory = directory.resolve(path.getName(index));
            requireDirectory(directory);
        }
    }

    private static void requireDirectory(Path path) throws IOException, KeyFileFailure {
        BasicFileAttributes attributes =
                Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isDirectory()) {
            throw new KeyFileFailure("unsafe_path");
        }
    }

    private static SecretKey decode(byte[] content) throws KeyFileFailure {
        int length = content.length;
        while (length > 0 && (content[length - 1] == '\n' || content[length - 1] == '\r')) {
            length--;
        }
        byte[] encoded = new byte[length];
        int output = 0;
        for (int i = 0; i < length; i++) {
            if (content[i] != '\n' && content[i] != '\r') {
                encoded[output++] = content[i];
            }
        }
        byte[] trimmed = Arrays.copyOf(encoded, output);
        try {
            byte[] raw = Base64.getDecoder().decode(trimmed);
            try {
                if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
                    throw new KeyFileFailure("invalid_format");
                }
                return new SecretKeySpec(raw, "AES");
            } finally {
                Arrays.fill(raw, (byte) 0);
            }
        } catch (IllegalArgumentException e) {
            throw new KeyFileFailure("invalid_format");
        } finally {
            Arrays.fill(trimmed, (byte) 0);
            Arrays.fill(encoded, (byte) 0);
        }
    }

    private static void checkPermissions(Path path) throws IOException, KeyFileFailure {
        PosixFileAttributeView posix =
                Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (posix != null) {
            UserPrincipal current = currentUser(path);
            var file = posix.readAttributes();
            if (!file.owner().equals(current)
                    || !disjoint(
                            file.permissions(),
                            EnumSet.of(
                                    PosixFilePermission.GROUP_READ,
                                    PosixFilePermission.GROUP_WRITE,
                                    PosixFilePermission.GROUP_EXECUTE,
                                    PosixFilePermission.OTHERS_READ,
                                    PosixFilePermission.OTHERS_WRITE,
                                    PosixFilePermission.OTHERS_EXECUTE))) {
                throw new KeyFileFailure("unsafe_permissions");
            }
            PosixFileAttributes directory =
                    Files.readAttributes(path.getParent(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!directory.isDirectory()
                    || (!directory.owner().equals(current)
                            && !directory.owner().getName().equals("root"))
                    || !disjoint(
                            directory.permissions(),
                            EnumSet.of(PosixFilePermission.GROUP_WRITE, PosixFilePermission.OTHERS_WRITE))) {
                throw new KeyFileFailure("unsafe_permissions");
            }
            return;
        }

        AclFileAttributeView acl =
                Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        AclFileAttributeView parentAcl =
                Files.getFileAttributeView(path.getParent(), AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (acl == null || parentAcl == null) {
            throw new KeyFileFailure("permissions_unavailable");
        }
        UserPrincipal current = currentUser(path);
        if (!Files.readAttributes(path.getParent(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)
                        .isDirectory()
                || unsafeWindowsPermissions(acl, parentAcl, current)) {
            throw new KeyFileFailure("unsafe_permissions");
        }
    }

    static boolean unsafeWindowsPermissions(
            AclFileAttributeView acl, AclFileAttributeView parentAcl, UserPrincipal current) throws IOException {
        return !acl.getOwner().equals(current)
                || !trustedPrincipal(parentAcl.getOwner(), current)
                || unsafeAcl(acl, current, FILE_SENSITIVE_PERMISSIONS)
                || unsafeAcl(parentAcl, current, DIRECTORY_SENSITIVE_PERMISSIONS);
    }

    private static UserPrincipal currentUser(Path path) throws IOException {
        String processUser = ProcessHandle.current()
                .info()
                .user()
                .orElseThrow(() -> new IOException("Process identity unavailable"));
        return path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(processUser);
    }

    private static boolean unsafeAcl(
            AclFileAttributeView view, UserPrincipal current, Set<AclEntryPermission> sensitive) throws IOException {
        for (AclEntry entry : view.getAcl()) {
            if (entry.type() == AclEntryType.ALLOW
                    && !trustedPrincipal(entry.principal(), current)
                    && !disjoint(entry.permissions(), sensitive)) {
                return true;
            }
        }
        return false;
    }

    private static boolean trustedPrincipal(UserPrincipal principal, UserPrincipal current) {
        if (principal.equals(current)) {
            return true;
        }
        String name = principal.getName();
        return name.equalsIgnoreCase("NT AUTHORITY\\SYSTEM") || name.equalsIgnoreCase("BUILTIN\\Administrators");
    }

    private static <T> boolean disjoint(Set<T> left, Set<T> right) {
        for (T value : left) {
            if (right.contains(value)) {
                return false;
            }
        }
        return true;
    }

    private static SecretKey fail(String category) {
        logger.error("Master key configuration rejected: {}", category);
        throw new IllegalStateException("Invalid CANIS_SECRET_KEY configuration (" + category + ")");
    }

    private static final class KeyFileFailure extends Exception {
        private final String category;

        private KeyFileFailure(String category) {
            this.category = category;
        }
    }
}
