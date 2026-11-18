import io.canis.jpaw.client.JpawClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Issue #33 research-only functional study. Production code is not used as a test double. */
public final class Issue33Harness {
    private static final long DEADLINE_SECONDS = 90;
    private static final String CSV_HEADER = "event,identity,key_name,file_ordinal,time_utc,detail\n";

    private Issue33Harness() {}

    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals("worker")) worker(args);
            else run(args);
        } catch (Exception e) {
            System.err.println("ISSUE33_FAILURE=" + Code02Harness.category(e));
            System.exit(1);
        }
    }

    private record Options(String scenario, Path output) {
        static Options parse(String[] args) {
            String scenario = null;
            Path output = null;
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) throw new IllegalArgumentException("Missing option value");
                switch (args[i]) {
                    case "--scenario" -> scenario = args[i + 1];
                    case "--output" -> output = Path.of(args[i + 1]);
                    default -> throw new IllegalArgumentException("Unknown option");
                }
            }
            if (!List.of("A", "B", "C", "D", "E").contains(scenario)) {
                throw new IllegalArgumentException("Scenario must be A, B, C, D, or E");
            }
            return new Options(scenario, output);
        }
    }

    private static void run(String[] args) throws Exception {
        Options options = Options.parse(args);
        Path repository = Path.of("").toAbsolutePath().normalize();
        Path jar = repository.resolve("server/target/canis-jar-with-dependencies.jar");
        Path clientClasses = repository.resolve("client/target/classes");
        Path harnessClasses = repository.resolve("server/target/issue33-classes");
        if (!Files.isRegularFile(jar) || !Files.isDirectory(clientClasses) || !Files.isDirectory(harnessClasses)) {
            throw new IllegalStateException("Build server, client, and research harness first");
        }
        String revision = Code02Harness.commandOutput(repository, "git", "rev-parse", "HEAD").trim();
        boolean dirty = !Code02Harness.commandOutput(repository, "git", "status", "--porcelain").isBlank();
        String runId = "issue33-" + revision.substring(0, 12) + "-" + options.scenario;
        String attempt = Instant.now().toString().replace(':', '-') + "-" + Code02Harness.randomHex(4);
        Path output = repository.resolve(options.output == null
                ? Path.of("server", "target", "issue33", options.scenario + "-" + attempt + ".json")
                : options.output).normalize();
        if (!output.startsWith(repository.resolve("server/target").normalize())
                && !output.startsWith(repository.resolve("docs/research").normalize())) {
            throw new IllegalArgumentException("Result output must remain in research or build directory");
        }
        if (Files.exists(output)) throw new IllegalArgumentException("Result path already exists");
        Path root = Files.createTempDirectory("canis-issue33-").toRealPath();
        Path privateDir = Code02Harness.secureDirectory(root.resolve("private"));
        Path serverDir = Code02Harness.secureDirectory(root.resolve("server"));
        Path shared = Code02Harness.secureDirectory(root.resolve("shared"));
        Path workerDir = Code02Harness.secureDirectory(root.resolve("worker"));
        Files.writeString(root.resolve(".code02-run"), runId, StandardCharsets.US_ASCII);
        Instant started = Instant.now();
        Instant firstPublication = null, firstConsumerStart = null, ended;
        Instant serverStopped = null, serverRestarted = null;
        long firstServerPid = -1, secondServerPid = -1;
        int published = 0, recovered = 0, rejected = 0;
        String status = "failed", failure = "none";
        List<String> events = new ArrayList<>();
        List<String> workerCsvNames = new ArrayList<>();
        List<Process> workers = new ArrayList<>();
        Process server = null;
        try {
            List<String> trusted = switch (options.scenario) {
                case "B" -> List.of("producer-a", "producer-b", "recipient-a", "recipient-b");
                case "D" -> List.of("producer-a", "recipient-a", "observer");
                default -> List.of("producer-a", "recipient-a");
            };
            String storePassword = Code02Harness.randomHex(24);
            Map<String, String> passwords = new HashMap<>();
            for (String identity : trusted) passwords.put(identity, Code02Harness.randomHex(24));
            Code02Harness.provisionMasterKey(privateDir.resolve("master.key"));
            Code02Harness.provisionTls(privateDir, storePassword, trusted);
            int port;
            try (ServerSocket reservation = new ServerSocket(0)) { port = reservation.getLocalPort(); }
            Map<String, String> serverEnv = new HashMap<>();
            serverEnv.put("CANIS_PORT", Integer.toString(port));
            serverEnv.put("CANIS_USERNAME", "recipient-a");
            serverEnv.put("CANIS_PASSWORD", passwords.get("recipient-a"));
            StringBuilder additional = new StringBuilder();
            for (String identity : trusted) {
                if (identity.equals("recipient-a")) continue;
                if (!additional.isEmpty()) additional.append(',');
                additional.append(identity).append(':').append(passwords.get(identity));
            }
            serverEnv.put("CANIS_SERVICE_CREDENTIALS", additional.toString());
            serverEnv.put("CANIS_SECRET_KEY", privateDir.resolve("master.key").toString());
            serverEnv.put("CANIS_TLS_KEYSTORE", privateDir.resolve("server.p12").toString());
            serverEnv.put("CANIS_TLS_KEYSTORE_PASSWORD", storePassword);
            serverEnv.put("CANIS_TLS_TRUSTSTORE", privateDir.resolve("server-trust.p12").toString());
            serverEnv.put("CANIS_TLS_TRUSTSTORE_PASSWORD", storePassword);
            List<String> serverCommand = List.of(Code02Harness.javaExecutable("java"), "-Xms128m", "-Xmx512m",
                    "-jar", jar.toString());
            Process init = Code02Harness.start(Code02Harness.serverCommandWithInit(serverCommand), serverDir,
                    serverEnv, privateDir.resolve("init.log"));
            if (!init.waitFor(DEADLINE_SECONDS, java.util.concurrent.TimeUnit.SECONDS) || init.exitValue() != 0
                    || !Files.isRegularFile(serverDir.resolve("db.dat"))) {
                throw new IOException("Isolated database initialization failed");
            }
            addEvent(events, "database_initialized", "server", "", -1, "new_fixture");
            server = Code02Harness.start(serverCommand, serverDir, serverEnv, privateDir.resolve("server-1.log"));
            firstServerPid = server.pid();
            Map<String, Map<String, String>> clientEnvs = new HashMap<>();
            for (String identity : trusted) {
                clientEnvs.put(identity, Code02Harness.clientEnvironment(privateDir, port, storePassword,
                        identity, passwords.get(identity)));
            }
            Code02Harness.awaitServer(clientEnvs.get("recipient-a"));
            addEvent(events, "server_ready", "server", "", -1, "first_process");
            try (JpawClient client = new JpawClient(Code02Harness.clientConfiguration(clientEnvs.get("recipient-a")))) {
                if (!"OK".equals(client.set("recipient-a"))) throw new IOException("Recipient A key creation failed");
            }
            if (options.scenario.equals("B")) {
                try (JpawClient client = new JpawClient(Code02Harness.clientConfiguration(clientEnvs.get("recipient-b")))) {
                    if (!"OK".equals(client.set("recipient-b"))) throw new IOException("Recipient B key creation failed");
                }
            }
            addEvent(events, "keys_created", "server", options.scenario.equals("B") ? "recipient-a+recipient-b" : "recipient-a", -1, "");
            List<String> base = List.of(Code02Harness.javaExecutable("java"), "-Xms128m", "-Xmx512m", "-cp",
                    harnessClasses + System.getProperty("path.separator") + clientClasses, "Issue33Harness", "worker");
            int total = options.scenario.equals("B") ? 4 : options.scenario.equals("D") || options.scenario.equals("E") ? 1 : 2;
            Process p1 = launch(base, "producer", root, runId, "producer-a", "recipient-a", 0,
                    options.scenario.equals("B") ? 2 : total, "normal", workerDir,
                    clientEnvs.get("producer-a"), privateDir, workers, workerCsvNames);
            Process p2 = null;
            if (options.scenario.equals("B")) {
                p2 = launch(base, "producer", root, runId, "producer-b", "recipient-b", 2, 2,
                        "normal", workerDir, clientEnvs.get("producer-b"), privateDir, workers, workerCsvNames);
            }
            awaitExit(p1, 0);
            if (p2 != null) awaitExit(p2, 0);
            published = countOutcome(privateDir, workerCsvNames, "published");
            if (published != total) throw new IOException("Publication count mismatch");
            firstPublication = firstEventTime(privateDir, workerCsvNames, "published");
            addEvent(events, "all_published", "producer-a", "", -1, Integer.toString(published));
            assertShared(shared, runId, total, 0);
            if (options.scenario.equals("A")) {
                Path original = shared.resolve(runId + "-0.canis");
                String before = sha256(original);
                Process duplicate = launch(base, "producer", root, runId, "duplicate", "recipient-a", 0, 1,
                        "normal", workerDir, clientEnvs.get("producer-a"), privateDir, workers, workerCsvNames);
                awaitExit(duplicate, 1);
                if (!before.equals(sha256(original))) throw new IOException("Duplicate changed published envelope");
                addEvent(events, "duplicate_publication_rejected", "producer-a", "recipient-a", 0, "unchanged_hash");
                Files.copy(original, shared.resolve(runId + "-decoy.part"));
                addEvent(events, "incomplete_present", "producer-a", "recipient-a", -1, "encrypted_part_only");
                assertShared(shared, runId, total, 1);
            }
            if (options.scenario.equals("C")) {
                String databaseHash = sha256(serverDir.resolve("db.dat"));
                server.destroy();
                if (!server.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) || server.isAlive()) {
                    throw new IOException("Clean server stop failed");
                }
                serverStopped = Instant.now();
                addEvent(events, "server_stopped", "server", "", -1, "clean_exit");
                server = Code02Harness.start(serverCommand, serverDir, serverEnv, privateDir.resolve("server-2.log"));
                secondServerPid = server.pid();
                Code02Harness.awaitServer(clientEnvs.get("recipient-a"));
                serverRestarted = Instant.now();
                if (firstServerPid == secondServerPid || !databaseHash.equals(sha256(serverDir.resolve("db.dat")))) {
                    throw new IOException("Restart did not preserve database");
                }
                addEvent(events, "server_restarted", "server", "", -1, "same_database");
            }
            long delayMs = options.scenario.equals("A") || options.scenario.equals("C") ? 60_000 : 1_000;
            long remaining = delayMs - Duration.between(firstPublication, Instant.now()).toMillis();
            if (remaining > 0) Thread.sleep(remaining);
            firstConsumerStart = Instant.now();
            addEvent(events, "consumer_started", "recipient-a", "recipient-a", -1, "");
            Process r1 = launch(base, "consumer", root, runId, "recipient-a", "recipient-a", 0,
                    options.scenario.equals("B") ? 2 : total, "normal", workerDir,
                    clientEnvs.get("recipient-a"), privateDir, workers, workerCsvNames);
            Process r2 = null;
            if (options.scenario.equals("B")) {
                r2 = launch(base, "consumer", root, runId, "recipient-b", "recipient-b", 2, 2,
                        "normal", workerDir, clientEnvs.get("recipient-b"), privateDir, workers, workerCsvNames);
            }
            awaitExit(r1, 0);
            if (r2 != null) awaitExit(r2, 0);
            recovered = countOutcome(privateDir, workerCsvNames, "verified");
            if (recovered != total) throw new IOException("Byte verification count mismatch");
            if (options.scenario.equals("A")) {
                Process replay = launch(base, "consumer", root, runId, "replay", "recipient-a", 0, 1,
                        "normal", workerDir, clientEnvs.get("recipient-a"), privateDir, workers, workerCsvNames);
                awaitExit(replay, 0);
                if (countOutcome(privateDir, workerCsvNames, "verified") != total + 1) {
                    throw new IOException("Valid envelope replay failed");
                }
                addEvent(events, "duplicate_delivery_verified", "recipient-a", "recipient-a", 0, "not_unique_file");
                Files.delete(shared.resolve(runId + "-decoy.part"));
            }
            if (options.scenario.equals("D")) {
                Process observer = launch(base, "consumer", root, runId, "observer", "recipient-a", 0, 1,
                        "normal", workerDir, clientEnvs.get("observer"), privateDir, workers, workerCsvNames);
                awaitExit(observer, 0);
                recovered++;
                addEvent(events, "shared_trust_recovery", "observer", "recipient-a", 0, "verified");
                Map<String, String> badPassword = new HashMap<>(clientEnvs.get("observer"));
                badPassword.put("CANIS_PASSWORD", Code02Harness.randomHex(24));
                Process bad = launch(base, "consumer", root, runId, "bad-password", "recipient-a", 0, 1,
                        "normal", workerDir, badPassword, privateDir, workers, workerCsvNames);
                awaitExit(bad, 1);
                requireFailure(privateDir.resolve("bad-password.csv"), "credentials_rejected");
                requireNoOutput(privateDir, "bad-password", 0);
                rejected++;
                int tlsBefore = Code02Harness.countTlsRejections(privateDir.resolve("server-1.log"));
                Map<String, String> badCert = new HashMap<>(clientEnvs.get("observer"));
                badCert.put("CANIS_CLIENT_TLS_KEYSTORE", privateDir.resolve("untrusted.p12").toString());
                Process untrusted = launch(base, "consumer", root, runId, "untrusted", "recipient-a", 0, 1,
                        "normal", workerDir, badCert, privateDir, workers, workerCsvNames);
                awaitExit(untrusted, 1);
                requireAnyFailure(privateDir.resolve("untrusted.csv"));
                requireNoOutput(privateDir, "untrusted", 0);
                if (Code02Harness.countTlsRejections(privateDir.resolve("server-1.log")) <= tlsBefore) {
                    throw new IOException("Server-side TLS rejection not observed");
                }
                rejected++;
                addEvent(events, "invalid_credentials_rejected", "observer", "recipient-a", 0, "no_output");
                addEvent(events, "untrusted_certificate_rejected", "observer", "recipient-a", 0, "no_output");
            }
            if (options.scenario.equals("E")) {
                byte[] altered = Files.readAllBytes(shared.resolve(runId + "-0.canis"));
                altered[altered.length - 1] ^= 1;
                Path tampered = shared.resolve(runId + "-1.canis");
                Files.write(tampered, altered, StandardOpenOption.CREATE_NEW);
                Arrays.fill(altered, (byte) 0);
                addEvent(events, "tag_copy_tampered", "fixture", "recipient-a", 1, "original_preserved");
                Process bad = launch(base, "consumer", root, runId, "tampered", "recipient-a", 1, 1,
                        "normal", workerDir, clientEnvs.get("recipient-a"), privateDir, workers, workerCsvNames);
                awaitExit(bad, 1);
                requireFailure(privateDir.resolve("tampered.csv"), "authentication_tag_rejected");
                requireNoOutput(privateDir, "tampered", 1);
                rejected++;
                assertShared(shared, runId, 2, 0);
            } else {
                assertShared(shared, runId, total, 0);
            }
            try (var files = Files.list(shared)) {
                if (files.anyMatch(p -> !p.getFileName().toString().endsWith(".canis"))) {
                    throw new IOException("Unexpected shared file after scenario");
                }
            }
            status = "passed";
        } catch (Exception e) {
            failure = Code02Harness.category(e);
        } finally {
            for (Process worker : workers) {
                if (worker.isAlive()) {
                    worker.destroyForcibly();
                    worker.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                }
            }
            if (server != null && server.isAlive()) {
                server.destroy();
                if (!server.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) server.destroyForcibly();
            }
            ended = Instant.now();
            if (status.equals("passed") && workers.stream().anyMatch(Process::isAlive)) {
                status = "failed";
                failure = "process_teardown_failed";
            }
            Files.createDirectories(output.getParent());
            String stem = output.getFileName().toString().replaceFirst("\\.json$", "");
            for (String name : workerCsvNames) {
                Path source = privateDir.resolve(name);
                if (Files.isRegularFile(source)) Files.copy(source, output.resolveSibling(stem + "." + name));
            }
            Files.writeString(output.resolveSibling(stem + ".events.csv"), CSV_HEADER + String.join("", events),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            String result = resultJson(options.scenario, runId, attempt, revision, dirty, repository, jar,
                    privateDir, started, ended, firstPublication, firstConsumerStart, serverStopped, serverRestarted,
                    firstServerPid, secondServerPid, workers, workerCsvNames, published, recovered, rejected, status, failure);
            if (status.equals("passed")) {
                try { Code02Harness.deleteRunRoot(root, runId); }
                catch (IOException e) {
                    status = "failed";
                    failure = "fixture_cleanup_failed";
                    result = result.replace("\"byte_verification_success\": true", "\"byte_verification_success\": false")
                            .replace("\"scenario_completion_status\": \"passed\"", "\"scenario_completion_status\": \"failed\"")
                            .replace("\"failure_category\": \"none\"", "\"failure_category\": \"fixture_cleanup_failed\"");
                    System.err.println("ISSUE33_PRIVATE_EVIDENCE=" + root);
                }
            } else {
                System.err.println("ISSUE33_PRIVATE_EVIDENCE=" + root);
            }
            Files.writeString(output, result, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            System.out.println("ISSUE33_RESULT=" + status + " scenario=" + options.scenario
                    + " published=" + published + " recovered=" + recovered + " rejected=" + rejected
                    + " metadata=" + output);
        }
        if (!status.equals("passed")) throw new IllegalStateException(failure);
    }

    private static Process launch(List<String> base, String kind, Path root, String runId, String label,
            String key, int first, int count, String mode, Path workerDir, Map<String, String> env,
            Path privateDir, List<Process> workers, List<String> names) throws IOException {
        List<String> command = new ArrayList<>(base);
        command.addAll(List.of(kind, root.toString(), runId, label, key, Integer.toString(first),
                Integer.toString(count), mode));
        String csv = label + ".csv";
        if (Files.exists(privateDir.resolve(csv))) throw new IOException("Duplicate worker output");
        Process p = Code02Harness.start(command, workerDir, env, privateDir.resolve(label + ".log"));
        workers.add(p);
        names.add(csv);
        return p;
    }

    private static void worker(String[] args) throws Exception {
        if (args.length != 9) throw new IllegalArgumentException("Invalid worker arguments");
        String kind = args[1], runId = args[3], label = args[4], key = args[5];
        Path root = Path.of(args[2]).toRealPath();
        if (!Files.readString(root.resolve(".code02-run")).equals(runId)) {
            throw new IOException("Run identity mismatch");
        }
        int first = Integer.parseInt(args[6]), count = Integer.parseInt(args[7]);
        Path privateDir = root.resolve("private"), shared = root.resolve("shared");
        StringBuilder csv = new StringBuilder("identity,key_name,file_ordinal,size_bytes,started_utc,finished_utc,outcome,failure_category,input_sha256,envelope_sha256\n");
        boolean failed = false;
        JpawClient client = null;
        String connectionFailure = null;
        try { client = new JpawClient(); }
        catch (Exception e) { connectionFailure = Code02Harness.category(e); }
        try {
            for (int ordinal = first; ordinal < first + count; ordinal++) {
                int size = ordinal % 2 == 0 ? 4096 : 262144;
                Instant began = Instant.now();
                String outcome = "failed", category = "none", inputHash = "", envelopeHash = "";
                try {
                    if (connectionFailure != null) throw new IOException("Connection unavailable");
                    Path envelope = Code02Harness.safeChild(shared, runId + "-" + ordinal + ".canis");
                    byte[] expected = Code02Harness.payload(1, ordinal, size);
                    inputHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(expected));
                    if (kind.equals("producer")) {
                        Path source = Code02Harness.safeChild(privateDir, "source-" + ordinal + ".bin");
                        Path partial = Code02Harness.safeChild(shared, runId + "-" + ordinal + ".part");
                        if (Files.exists(envelope) || Files.exists(partial)) {
                            category = "duplicate_publication";
                            throw new IOException("Duplicate publication");
                        }
                        Files.write(source, expected, StandardOpenOption.CREATE_NEW);
                        client.encryptFile(key, source.toFile(), partial.toFile());
                        assertMagic(partial);
                        Files.move(partial, envelope, StandardCopyOption.ATOMIC_MOVE);
                        envelopeHash = sha256(envelope);
                        outcome = "published";
                    } else if (kind.equals("consumer")) {
                        if (!Files.isRegularFile(envelope)) {
                            category = "incomplete_or_missing";
                            throw new IOException("Complete envelope missing");
                        }
                        envelopeHash = sha256(envelope);
                        Path recovered = Code02Harness.safeChild(privateDir, "recovered-" + label + "-" + ordinal + ".bin");
                        try {
                            client.decryptFile(key, envelope.toFile(), recovered.toFile());
                            if (!Arrays.equals(expected, Files.readAllBytes(recovered))) {
                                category = "byte_mismatch";
                                throw new IOException("Byte mismatch");
                            }
                        } catch (Exception e) {
                            Files.deleteIfExists(recovered);
                            throw e;
                        }
                        outcome = "verified";
                    } else throw new IllegalArgumentException("Invalid worker kind");
                } catch (Exception e) {
                    failed = true;
                    if (category.equals("none")) category = connectionFailure == null
                            ? Code02Harness.category(e) : connectionFailure;
                }
                csv.append(label).append(',').append(key).append(',').append(ordinal).append(',').append(size)
                        .append(',').append(began).append(',').append(Instant.now()).append(',').append(outcome)
                        .append(',').append(category).append(',').append(inputHash).append(',').append(envelopeHash)
                        .append('\n');
            }
        } finally {
            if (client != null) client.close();
            Files.writeString(privateDir.resolve(label + ".csv"), csv, StandardCharsets.US_ASCII,
                    StandardOpenOption.CREATE_NEW);
        }
        if (failed) throw new IOException("Worker operation rejected");
    }

    private static void awaitExit(Process p, int expected) throws Exception {
        if (!p.waitFor(DEADLINE_SECONDS, java.util.concurrent.TimeUnit.SECONDS)) throw new IOException("Worker timeout");
        if (expected == 0 ? p.exitValue() != 0 : p.exitValue() == 0) throw new IOException("Unexpected worker exit");
    }

    private static int countOutcome(Path dir, List<String> names, String outcome) throws IOException {
        int count = 0;
        for (String name : names) {
            Path csv = dir.resolve(name);
            if (!Files.isRegularFile(csv)) continue;
            for (String row : Files.readAllLines(csv, StandardCharsets.US_ASCII).stream().skip(1).toList()) {
                if (row.split(",", -1)[6].equals(outcome)) count++;
            }
        }
        return count;
    }

    private static Instant firstEventTime(Path dir, List<String> names, String outcome) throws IOException {
        Instant first = null;
        for (String name : names) {
            Path csv = dir.resolve(name);
            if (!Files.isRegularFile(csv)) continue;
            for (String row : Files.readAllLines(csv, StandardCharsets.US_ASCII).stream().skip(1).toList()) {
                String[] fields = row.split(",", -1);
                if (fields[6].equals(outcome)) {
                    Instant time = Instant.parse(fields[5]);
                    if (first == null || time.isBefore(first)) first = time;
                }
            }
        }
        if (first == null) throw new IOException("Publication event missing");
        return first;
    }

    private static void requireFailure(Path csv, String expected) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.US_ASCII);
        if (lines.size() != 2 || !lines.get(1).split(",", -1)[7].equals(expected)) {
            throw new IOException("Unexpected rejection category");
        }
    }

    private static void requireAnyFailure(Path csv) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.US_ASCII);
        if (lines.size() != 2 || !lines.get(1).split(",", -1)[6].equals("failed")) {
            throw new IOException("Expected rejection not recorded");
        }
    }

    private static void requireNoOutput(Path privateDir, String label, int ordinal) throws IOException {
        if (Files.exists(privateDir.resolve("recovered-" + label + "-" + ordinal + ".bin"))) {
            throw new IOException("Rejected attempt left recovered output");
        }
    }

    private static void assertMagic(Path envelope) throws IOException {
        byte[] prefix = new byte[9];
        try (var in = Files.newInputStream(envelope)) {
            if (in.read(prefix) != prefix.length || !Arrays.equals(prefix, "CANISHYB1".getBytes(StandardCharsets.US_ASCII))) {
                throw new IOException("Invalid envelope header");
            }
        }
    }

    private static void assertShared(Path shared, String runId, int expectedComplete, int expectedPartial) throws IOException {
        int complete = 0, partial = 0;
        try (var files = Files.list(shared)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString();
                if (Files.isSymbolicLink(file) || !Files.isRegularFile(file) || !name.startsWith(runId + "-")) {
                    throw new IOException("Unexpected shared entry");
                }
                if (name.endsWith(".canis")) {
                    boolean expectedName = false;
                    for (int ordinal = 0; ordinal < expectedComplete; ordinal++) {
                        if (name.equals(runId + "-" + ordinal + ".canis")) expectedName = true;
                    }
                    if (!expectedName) throw new IOException("Unexpected envelope name");
                    complete++;
                    assertMagic(file);
                }
                else if (name.equals(runId + "-decoy.part")) { partial++; assertMagic(file); }
                else throw new IOException("Unexpected shared file type");
            }
        }
        if (complete != expectedComplete || partial != expectedPartial) {
            throw new IOException("Shared publication count mismatch");
        }
    }

    private static void addEvent(List<String> events, String event, String identity, String key, int ordinal, String detail) {
        events.add(event + "," + identity + "," + key + "," + (ordinal < 0 ? "" : ordinal)
                + "," + Instant.now() + "," + detail + "\n");
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static String resultJson(String scenario, String runId, String attempt, String revision, boolean dirty,
            Path repository, Path jar, Path privateDir, Instant started, Instant ended, Instant firstPublication,
            Instant firstConsumerStart, Instant stopped, Instant restarted, long firstPid, long secondPid,
            List<Process> workers, List<String> workerCsvNames, int published, int recovered, int rejected,
            String status, String failure) throws Exception {
        StringBuilder certs = new StringBuilder("{");
        try (var files = Files.list(privateDir)) {
            for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".cer")).sorted().toList()) {
                if (certs.length() > 1) certs.append(',');
                certs.append('"').append(file.getFileName().toString().replace(".cer", ""))
                        .append("\":\"").append(sha256(file)).append('"');
            }
        }
        certs.append('}');
        StringBuilder workerPids = new StringBuilder("{");
        for (int i = 0; i < workers.size(); i++) {
            if (i > 0) workerPids.append(',');
            workerPids.append('"').append(workerCsvNames.get(i).replace(".csv", ""))
                    .append("\":").append(workers.get(i).pid());
        }
        workerPids.append('}');
        String harnessHash = sha256(repository.resolve("docs/research/Issue33Harness.java"));
        long delay = firstPublication == null || firstConsumerStart == null ? -1
                : Duration.between(firstPublication, firstConsumerStart).toMillis();
        return "{\n" +
                "  \"schema\": \"issue33-v1\",\n" +
                "  \"scenario\": \"" + scenario + "\",\n" +
                "  \"run_id\": \"" + runId + "\",\n" +
                "  \"attempt_id\": \"" + attempt + "\",\n" +
                "  \"revision\": \"" + revision + "\",\n" +
                "  \"dirty_tree\": " + dirty + ",\n" +
                "  \"harness_sha256\": \"" + harnessHash + "\",\n" +
                "  \"server_jar_sha256\": \"" + sha256(jar) + "\",\n" +
                "  \"os\": \"" + Code02Harness.json(System.getProperty("os.name") + " " + System.getProperty("os.version")) + "\",\n" +
                "  \"jdk\": \"" + Code02Harness.json(System.getProperty("java.runtime.version")) + "\",\n" +
                "  \"started_utc\": \"" + started + "\",\n" +
                "  \"ended_utc\": \"" + ended + "\",\n" +
                "  \"run_duration_ms\": " + Duration.between(started, ended).toMillis() + ",\n" +
                "  \"first_publication_utc\": " + nullable(firstPublication) + ",\n" +
                "  \"first_consumer_start_utc\": " + nullable(firstConsumerStart) + ",\n" +
                "  \"observed_delay_ms\": " + delay + ",\n" +
                "  \"server_stopped_utc\": " + nullable(stopped) + ",\n" +
                "  \"server_restarted_utc\": " + nullable(restarted) + ",\n" +
                "  \"server_pids\": [" + firstPid + "," + secondPid + "],\n" +
                "  \"worker_pids\": " + workerPids + ",\n" +
                "  \"certificate_sha256\": " + certs + ",\n" +
                "  \"files_published\": " + published + ",\n" +
                "  \"files_recovered\": " + recovered + ",\n" +
                "  \"files_rejected\": " + rejected + ",\n" +
                "  \"byte_verification_success\": " + status.equals("passed") + ",\n" +
                "  \"scenario_completion_status\": \"" + status + "\",\n" +
                "  \"failure_category\": \"" + failure + "\"\n" +
                "}\n";
    }

    private static String nullable(Instant value) { return value == null ? "null" : "\"" + value + "\""; }
}
