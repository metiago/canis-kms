package io.canis.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.MasterKeyTestSupport;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class MasterKeyLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsLegacyAndLongerAesKeysAndLineBreaks() throws Exception {
        Path key = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        for (int length : new int[] {16, 24, 32}) {
            byte[] raw = new byte[length];
            new SecureRandom().nextBytes(raw);
            String encoded = Base64.getEncoder().encodeToString(raw);
            Files.writeString(
                    key, encoded.substring(0, 8) + "\r\n" + encoded.substring(8) + "\n", StandardCharsets.US_ASCII);
            assertArrayEquals(
                    raw,
                    new SymmetricGenerator(key.toString())
                            .decrypt(new SymmetricGenerator(key.toString()).encrypt(raw)));
        }
    }

    @Test
    void rejectsMissingPathFileAndDirectoryWithoutCreatingAKey() {
        Path missing = tempDir.resolve("missing.key");
        assertFailure(null, "missing_path");
        assertFailure(" ", "missing_path");
        assertFailure("secret" + (char) 0 + "path", "invalid_path");
        assertFailure(missing.toString(), "missing_file");
        assertFailure(tempDir.toString(), "not_regular_file");
        assertFalse(Files.exists(missing));
    }

    @Test
    void rejectsInvalidEncodingLengthAndOversizedFileWithoutLeakingContent() throws Exception {
        Path key = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        Files.writeString(key, "SECRET_SENTINEL_NOT_BASE64!", StandardCharsets.US_ASCII);
        Logger logger = (Logger) LoggerFactory.getLogger(MasterKeyLoader.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        IllegalStateException malformed;
        try {
            malformed = assertFailure(key.toString(), "invalid_format");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        assertFalse(malformed.toString().contains("SECRET_SENTINEL"));
        assertFalse(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("SECRET_SENTINEL") || message.contains(key.toString())));

        Files.writeString(key, Base64.getEncoder().encodeToString(new byte[15]), StandardCharsets.US_ASCII);
        assertFailure(key.toString(), "invalid_format");

        Files.writeString(key, " ".repeat(257), StandardCharsets.US_ASCII);
        assertFailure(key.toString(), "invalid_format");

        Files.writeString(key, "", StandardCharsets.US_ASCII);
        assertFailure(key.toString(), "invalid_format");
    }

    @Test
    void rejectsSymbolicLink() throws Exception {
        Path target = tempDir.resolve("target.key");
        MasterKeyTestSupport.provision(target);
        Path link = tempDir.resolve("link.key");
        try {
            Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            assumeTrue(false, "Symbolic links cannot be created on this platform");
        }
        assertFailure(link.toString(), "not_regular_file");
    }

    @Test
    void rejectsUnsafePosixFileAndDirectoryPermissions() throws Exception {
        Path key = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        assumeTrue(Files.getFileAttributeView(key, PosixFileAttributeView.class) != null);

        Files.setPosixFilePermissions(
                key,
                EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.GROUP_READ));
        assertFailure(key.toString(), "unsafe_permissions");

        MasterKeyTestSupport.restrict(key, false);
        Files.setPosixFilePermissions(
                tempDir,
                EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.GROUP_WRITE));
        assertFailure(key.toString(), "unsafe_permissions");
    }

    @Test
    void rejectsUnsafeWindowsFileAndDirectoryAcls() throws Exception {
        Path key = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        AclFileAttributeView fileAcl = Files.getFileAttributeView(key, AclFileAttributeView.class);
        assumeTrue(fileAcl != null && Files.getFileAttributeView(key, PosixFileAttributeView.class) == null);
        var users = key.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("BUILTIN\\Users");

        AclEntry broadRead = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(users)
                .setPermissions(AclEntryPermission.READ_DATA)
                .build();
        var entries = new ArrayList<>(fileAcl.getAcl());
        entries.add(broadRead);
        fileAcl.setAcl(entries);
        assertFailure(key.toString(), "unsafe_permissions");

        MasterKeyTestSupport.restrict(key, false);
        AclFileAttributeView parentAcl = Files.getFileAttributeView(tempDir, AclFileAttributeView.class);
        AclEntry broadWrite = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(users)
                .setPermissions(AclEntryPermission.WRITE_DATA)
                .build();
        entries = new ArrayList<>(parentAcl.getAcl());
        entries.add(broadWrite);
        parentAcl.setAcl(entries);
        assertFailure(key.toString(), "unsafe_permissions");
    }

    private IllegalStateException assertFailure(String location, String category) {
        IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> new SymmetricGenerator(location));
        assertTrue(failure.getMessage().contains(category));
        assertFalse(failure.getMessage().contains(tempDir.toString()));
        assertTrue(failure.getCause() == null);
        return failure;
    }
}
