import io.canis.jpaw.client.JpawClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.crypto.KeyGenerator;

/** Research-only process harness. No production CANISP, storage, or envelope code is changed. */
public final class Code02Harness {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String MAGIC = "CANISHYB1";
    private static final long PROCESS_DEADLINE_SECONDS = 90;

    private Code02Harness() {}

    public static void main(String[] args) {
        try {
            if (args.length > 0 && (args[0].equals("producer") || args[0].equals("consumer"))) {
                worker(args);
            } else if (args.length > 0 && args[0].equals("self-test")) {
                selfTest();
            } else {
                run(args);
            }
        } catch (Exception e) {
            System.err.println("CODE02_FAILURE=" + category(e));
            System.exit(1);
        }
    }

    private record Options(String scenario, int files, int size, int concurrency, long delayMs, long intervalMs,
            int repetition, Path output) {
        static Options parse(String[] args) {
            String scenario = "normal";
            int files = 1, size = 4096, concurrency = 1, repetition = 1;
            long delayMs = 1000, intervalMs = 500;
            Path output = null;
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) throw new IllegalArgumentException("Option requires a value");
                switch (args[i]) {
                    case "--scenario" -> scenario = args[i + 1];
                    case "--files" -> files = Integer.parseInt(args[i + 1]);
                    case "--size" -> size = Integer.parseInt(args[i + 1]);
                    case "--concurrency" -> concurrency = Integer.parseInt(args[i + 1]);
                    case "--delay-ms" -> delayMs = Long.parseLong(args[i + 1]);
                    case "--interval-ms" -> intervalMs = Long.parseLong(args[i + 1]);
                    case "--repetition" -> repetition = Integer.parseInt(args[i + 1]);
                    case "--output" -> output = Path.of(args[i + 1]);
                    default -> throw new IllegalArgumentException("Unknown option");
                }
            }
            if (!List.of("normal", "tamper", "incomplete", "invalid-credentials", "invalid-certificate")
                    .contains(scenario) || files < 1 || files > 100 || size < 0 || size > 4 * 1024 * 1024
                    || concurrency < 1 || concurrency > 4 || delayMs < 0 || delayMs > 60000
                    || intervalMs < 0 || intervalMs > 60000 || repetition < 1 || repetition > 5) {
                throw new IllegalArgumentException("Invalid research scenario or workload");
            }
            if (size == 0 && files != 100 && files != 20) {
                throw new IllegalArgumentException("Issue 31 size schedule requires 20 warm-up or 100 measured files");
            }
            if (!scenario.equals("normal") && files != 1) {
                throw new IllegalArgumentException("Fault scenarios use one file");
            }
            return new Options(scenario, files, size, concurrency, delayMs, intervalMs, repetition, output);
        }
    }

    private static void run(String[] args) throws Exception {
        Options options = Options.parse(args);
        Path repository = Path.of("").toAbsolutePath().normalize();
        Path jar = repository.resolve("server/target/canis-jar-with-dependencies.jar");
        Path clientClasses = repository.resolve("client/target/classes");
        Path harnessClasses = repository.resolve("server/target/code02-classes");
        if (!Files.isRegularFile(jar) || !Files.isDirectory(clientClasses) || !Files.isDirectory(harnessClasses)) {
            throw new IllegalStateException("Build the server and research harness before running");
        }
        String revision = commandOutput(repository, "git", "rev-parse", "HEAD").trim();
        boolean dirty = !commandOutput(repository, "git", "status", "--porcelain").isBlank();
        String logicalId = "code02-" + revision.substring(0, 12) + "-" + options.scenario + "-r"
                + options.repetition + "-n" + options.files + "-s" + options.size + "-c"
                + options.concurrency + "-d" + options.delayMs;
        String attemptId = Instant.now().toString().replace(':', '-') + "-" + randomHex(4);
        Path output = repository.resolve(options.output == null
                ? Path.of("server", "target", "code02-" + attemptId + ".json") : options.output).normalize();
        if (Files.exists(output)) throw new IllegalArgumentException("Result path already exists");
        Path root = Files.createTempDirectory("canis-code02-").toRealPath();
        Path privateDir = secureDirectory(root.resolve("private"));
        Path serverDir = secureDirectory(root.resolve("server"));
        Path shared = secureDirectory(root.resolve("shared"));
        Path workerDir = secureDirectory(root.resolve("worker"));
        Files.writeString(root.resolve(".code02-run"), logicalId, StandardCharsets.US_ASCII);
        Process server = null, producer = null, consumer = null;
        String outcome = "failed", failure = "none";
        long producerMs = -1, consumerMs = -1, producerPeak = -1, consumerPeak = -1, serverPeak = -1;
        long serverPid = -1, producerPid = -1, consumerPid = -1;
        Instant producerStart = null, consumerStart = null;
        int verified = 0;
        try {
            String storePassword = randomHex(24), producerPassword = randomHex(24), recipientPassword = randomHex(24);
            provisionMasterKey(privateDir.resolve("master.key"));
            provisionTls(privateDir, storePassword);
            int port;
            try (ServerSocket reservation = new ServerSocket(0)) { port = reservation.getLocalPort(); }
            Map<String, String> serverEnv = Map.ofEntries(
                    Map.entry("CANIS_PORT", Integer.toString(port)),
                    Map.entry("CANIS_USERNAME", "recipient"), Map.entry("CANIS_PASSWORD", recipientPassword),
                    Map.entry("CANIS_SERVICE_CREDENTIALS", "producer:" + producerPassword),
                    Map.entry("CANIS_SECRET_KEY", privateDir.resolve("master.key").toString()),
                    Map.entry("CANIS_TLS_KEYSTORE", privateDir.resolve("server.p12").toString()),
                    Map.entry("CANIS_TLS_KEYSTORE_PASSWORD", storePassword),
                    Map.entry("CANIS_TLS_TRUSTSTORE", privateDir.resolve("server-trust.p12").toString()),
                    Map.entry("CANIS_TLS_TRUSTSTORE_PASSWORD", storePassword));
            List<String> serverCommand = List.of(javaExecutable("java"), "-Xms128m", "-Xmx512m", "-jar", jar.toString());
            Process init = start(serverCommandWithInit(serverCommand), serverDir, serverEnv,
                    privateDir.resolve("init.log"));
            if (!init.waitFor(PROCESS_DEADLINE_SECONDS, TimeUnit.SECONDS) || init.exitValue() != 0
                    || !Files.isRegularFile(serverDir.resolve("db.dat"))) {
                throw new IllegalStateException("Isolated database initialization failed");
            }
            server = start(serverCommand, serverDir, serverEnv, privateDir.resolve("server.log"));
            serverPid = server.pid();
            Map<String, String> recipientEnv = clientEnvironment(privateDir, port, storePassword,
                    "recipient", recipientPassword);
            Map<String, String> producerEnv = clientEnvironment(privateDir, port, storePassword,
                    "producer", producerPassword);
            awaitServer(recipientEnv);
            try (JpawClient client = new JpawClient(clientConfiguration(recipientEnv))) {
                if (!"OK".equals(client.set("recipient"))) throw new IOException("Key creation failed");
            }
            String classpath = harnessClasses + System.getProperty("path.separator") + clientClasses;
            List<String> base = List.of(javaExecutable("java"), "-Xms128m", "-Xmx512m", "-cp", classpath,
                    "Code02Harness");
            List<String> common = List.of(root.toString(), logicalId, Integer.toString(options.files),
                    Integer.toString(options.size), Integer.toString(options.repetition),
                    Long.toString(options.intervalMs), Integer.toString(options.concurrency));
            producerStart = Instant.now();
            long producerNanos = System.nanoTime();
            producer = start(workerCommand(base, "producer", common), workerDir, producerEnv,
                    privateDir.resolve("producer.log"));
            producerPid = producer.pid();
            Path firstEnvelope = safeChild(shared, logicalId + "-0.canis");
            long firstDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!Files.isRegularFile(firstEnvelope) && System.nanoTime() < firstDeadline && producer.isAlive()) {
                Thread.sleep(10);
            }
            if (!Files.isRegularFile(firstEnvelope)) throw new IllegalStateException("First publication missing");
            if (!options.scenario.equals("normal")) {
                if (!producer.waitFor(PROCESS_DEADLINE_SECONDS, TimeUnit.SECONDS) || producer.exitValue() != 0) {
                    throw new IllegalStateException("Producer failed");
                }
                if (options.scenario.equals("tamper")) {
                    byte[] bytes = Files.readAllBytes(firstEnvelope);
                    bytes[bytes.length - 1] ^= 1;
                    Files.write(firstEnvelope, bytes);
                }
                if (options.scenario.equals("incomplete")) {
                    Files.move(firstEnvelope, safeChild(shared, logicalId + "-0.part"), StandardCopyOption.ATOMIC_MOVE);
                }
            }
            Thread.sleep(options.delayMs);
            consumerStart = Instant.now();
            Map<String, String> workerEnv = new java.util.HashMap<>(recipientEnv);
            if (options.scenario.equals("invalid-credentials")) workerEnv.put("CANIS_PASSWORD", randomHex(24));
            if (options.scenario.equals("invalid-certificate")) {
                workerEnv.put("CANIS_CLIENT_TLS_KEYSTORE", privateDir.resolve("untrusted.p12").toString());
            }
            int tlsFailuresBefore = countTlsRejections(privateDir.resolve("server.log"));
            long consumerNanos = System.nanoTime();
            consumer = start(workerCommand(base, "consumer", common), workerDir, workerEnv,
                    privateDir.resolve("consumer.log"));
            consumerPid = consumer.pid();
            if (!producer.waitFor(Math.max(PROCESS_DEADLINE_SECONDS, options.files), TimeUnit.SECONDS)) {
                throw new IllegalStateException("Producer deadline exceeded");
            }
            producerMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - producerNanos);
            producerPeak = peakHeap(privateDir.resolve("producer.heap"));
            if (producer.exitValue() != 0) throw new IllegalStateException("Producer failed");
            if (!consumer.waitFor(PROCESS_DEADLINE_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Consumer deadline exceeded");
            }
            consumerMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - consumerNanos);
            consumerPeak = peakHeap(privateDir.resolve("consumer.heap"));
            verified = countVerified(privateDir.resolve("consumer.csv"));
            if (options.scenario.equals("normal")) {
                if (consumer.exitValue() != 0 || verified != options.files) {
                    throw new IllegalStateException("Byte verification failed");
                }
            } else if (consumer.exitValue() == 0 || verified != 0 || hasRecoveredFiles(privateDir)) {
                throw new IllegalStateException("Fault did not fail closed");
            }
            if (!options.scenario.equals("normal")) {
                String observed = readFailureCategory(privateDir.resolve("consumer.csv"));
                String expected = switch (options.scenario) {
                    case "tamper" -> "authentication_tag_rejected";
                    case "incomplete" -> "incomplete_or_missing";
                    case "invalid-credentials" -> "credentials_rejected";
                    case "invalid-certificate" -> "server_tls_rejection_required";
                    default -> throw new IllegalStateException("Unexpected fault scenario");
                };
                if (options.scenario.equals("invalid-certificate")) {
                    if (!List.of("io_or_authentication_failure", "tls_connection_rejected", "tls_certificate_rejected")
                                    .contains(observed)
                            || countTlsRejections(privateDir.resolve("server.log")) <= tlsFailuresBefore) {
                        throw new IllegalStateException("Server TLS rejection was not established");
                    }
                    failure = "server_tls_rejection_observed";
                } else {
                    if (!expected.equals(observed)) throw new IllegalStateException("Wrong fault outcome");
                    failure = observed;
                }
            }
            assertSharedStorage(shared, logicalId, options.scenario, options.files);
            outcome = "passed";
        } catch (Exception e) {
            failure = category(e);
        } finally {
            if (producer != null && producer.isAlive()) {
                producer.destroyForcibly();
                producer.waitFor(5, TimeUnit.SECONDS);
            }
            if (consumer != null && consumer.isAlive()) {
                consumer.destroyForcibly();
                consumer.waitFor(5, TimeUnit.SECONDS);
            }
            if (server != null) {
                serverPeak = Math.max(serverPeak, server.info().totalCpuDuration().map(d -> d.toNanos()).orElse(-1L));
                server.destroy();
                if (!server.waitFor(5, TimeUnit.SECONDS)) {
                    server.destroyForcibly();
                    server.waitFor(5, TimeUnit.SECONDS);
                }
            }
            if ((producer != null && producer.isAlive()) || (consumer != null && consumer.isAlive())
                    || (server != null && server.isAlive())) {
                outcome = "failed";
                failure = "process_teardown_failed";
            }
            Files.createDirectories(output.getParent());
            try {
                for (String role : List.of("producer", "consumer")) {
                    Path csv = privateDir.resolve(role + ".csv");
                    if (Files.isRegularFile(csv)) {
                        Files.copy(csv, output.resolveSibling(output.getFileName().toString().replaceFirst("\\.json$", "")
                                + "." + role + ".csv"));
                    }
                }
            } catch (IOException e) {
                outcome = "failed";
                failure = "result_export_failed";
            }
            Instant firstPublished = firstPublication(privateDir.resolve("producer.csv"));
            if (outcome.equals("passed")) {
                try { deleteRunRoot(root, logicalId); }
                catch (IOException e) { outcome = "failed"; failure = "fixture_cleanup_failed"; }
            }
            String result = resultJson(logicalId, attemptId, revision, dirty, options, outcome, failure,
                    producerStart, firstPublished, consumerStart,
                    serverPid, producerPid, consumerPid, producerMs, consumerMs,
                    producerPeak, consumerPeak, serverPeak, verified);
            Files.writeString(output, result, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            System.out.println("CODE02_RESULT=" + outcome + " scenario=" + options.scenario + " verified=" + verified
                    + "/" + options.files + " metadata=" + output);
            if (!outcome.equals("passed")) {
                System.err.println("CODE02_PRIVATE_EVIDENCE=" + root);
            }
        }
        if (!outcome.equals("passed")) throw new IllegalStateException(failure);
    }

    private static void worker(String[] args) throws Exception {
        if (args.length != 8) throw new IllegalArgumentException("Invalid worker arguments");
        String role = args[0];
        Path root = Path.of(args[1]).toRealPath();
        String runId = args[2];
        if (!Files.readString(root.resolve(".code02-run")).equals(runId)) {
            throw new IllegalArgumentException("Run identity mismatch");
        }
        int files = Integer.parseInt(args[3]), size = Integer.parseInt(args[4]);
        int repetition = Integer.parseInt(args[5]);
        long intervalMs = Long.parseLong(args[6]);
        int concurrency = Integer.parseInt(args[7]);
        Path privateDir = root.resolve("private"), shared = root.resolve("shared");
        try {
            if (role.equals("producer")) {
                try (JpawClient client = new JpawClient()) {
                    long scheduleNanos = System.nanoTime();
                    Instant scheduleStart = Instant.now();
                    StringBuilder csv = new StringBuilder(
                            "ordinal,size,planned_start_utc,actual_start_utc,published_utc,encrypt_ns,outcome\n");
                    for (int i = 0; i < files; i++) {
                        int fileSize = sizeFor(repetition, i, size, files);
                        long due = scheduleNanos + TimeUnit.MILLISECONDS.toNanos(intervalMs * i);
                        while (System.nanoTime() < due) Thread.sleep(1);
                        Instant actualStart = Instant.now();
                        Path input = safeChild(privateDir, "source-" + i + ".bin");
                        Files.write(input, payload(repetition, i, fileSize));
                        Path partial = safeChild(shared, runId + "-" + i + ".part");
                        Path finalFile = safeChild(shared, runId + "-" + i + ".canis");
                        if (Files.exists(partial) || Files.exists(finalFile)) {
                            throw new IOException("Duplicate publication");
                        }
                        long began = System.nanoTime();
                        client.encryptFile("recipient", input.toFile(), partial.toFile());
                        byte[] prefix = new byte[MAGIC.length()];
                        try (var stream = Files.newInputStream(partial)) {
                            if (stream.read(prefix) != prefix.length || !MAGIC.equals(new String(prefix, StandardCharsets.US_ASCII))) {
                                throw new IOException("Invalid generated envelope");
                            }
                        }
                        Files.move(partial, finalFile, StandardCopyOption.ATOMIC_MOVE);
                        csv.append(i).append(',').append(fileSize).append(',')
                                .append(scheduleStart.plusMillis(intervalMs * i)).append(',')
                                .append(actualStart).append(',').append(Instant.now()).append(',')
                                .append(System.nanoTime() - began)
                                .append(",published\n");
                    }
                    Files.writeString(privateDir.resolve("producer.csv"), csv, StandardCharsets.US_ASCII);
                }
            } else if (role.equals("consumer")) {
                String[] rows = new String[files];
                try (var pool = Executors.newFixedThreadPool(concurrency)) {
                    List<Future<Void>> futures = new ArrayList<>();
                    for (int worker = 0; worker < concurrency; worker++) {
                        final int assignedWorker = worker;
                        futures.add(pool.submit((Callable<Void>) () -> {
                            JpawClient client = null;
                            String connectionFailure = null;
                            try { client = new JpawClient(); }
                            catch (Exception e) { connectionFailure = category(e); }
                            try {
                                for (int i = assignedWorker; i < files; i += concurrency) {
                                    rows[i] = consumeFile(client, connectionFailure, shared, privateDir,
                                            runId, repetition, i, sizeFor(repetition, i, size, files));
                                }
                            } finally { if (client != null) client.close(); }
                            return null;
                        }));
                    }
                    for (Future<Void> future : futures) future.get();
                }
                StringBuilder csv = new StringBuilder(
                        "ordinal,size,claimed_utc,finished_utc,decrypt_ns,outcome,failure_category\n");
                int verified = 0;
                for (String row : rows) {
                    csv.append(row);
                    if (row.endsWith(",verified,none\n")) verified++;
                }
                Files.writeString(privateDir.resolve("consumer.csv"), csv, StandardCharsets.US_ASCII);
                if (verified != files) throw new IllegalStateException("Consumer verification failed");
            } else {
                throw new IllegalArgumentException("Invalid worker role");
            }
        } finally {
            long peak = java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
            Files.writeString(privateDir.resolve(role + ".heap"), Long.toString(peak), StandardCharsets.US_ASCII);
        }
    }

    private static int sizeFor(int repetition, int ordinal, int configuredSize, int files) {
        if (configuredSize != 0) return configuredSize;
        int count = files;
        int[] sizes = new int[count];
        int small = count == 20 ? 10 : 50;
        int medium = count == 20 ? 17 : 85;
        Arrays.fill(sizes, 0, small, 4096);
        Arrays.fill(sizes, small, medium, 256 * 1024);
        Arrays.fill(sizes, medium, count, 4 * 1024 * 1024);
        SplittableRandom random = new SplittableRandom(31000L + repetition);
        for (int i = sizes.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int previous = sizes[i]; sizes[i] = sizes[j]; sizes[j] = previous;
        }
        return sizes[ordinal];
    }


    private static String consumeFile(JpawClient client, String connectionFailure, Path shared, Path privateDir,
            String runId, int repetition, int ordinal, int size) throws Exception {
        Path envelope = safeChild(shared, runId + "-" + ordinal + ".canis");
        Path output = safeChild(privateDir, "recovered-" + ordinal + ".bin");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!Files.isRegularFile(envelope) && System.nanoTime() < deadline) Thread.sleep(10);
        if (!Files.isRegularFile(envelope)) {
            return ordinal + "," + size + ",not_applicable," + Instant.now()
                    + ",0,failed,incomplete_or_missing\n";
        }
        Instant claimed = Instant.now();
        if (connectionFailure != null) {
            return ordinal + "," + size + "," + claimed + "," + Instant.now()
                    + ",0,failed," + connectionFailure + "\n";
        }
        long began = System.nanoTime();
        try {
            client.decryptFile("recipient", envelope.toFile(), output.toFile());
            if (!Arrays.equals(payload(repetition, ordinal, size), Files.readAllBytes(output))) {
                throw new IOException("Byte mismatch");
            }
            return ordinal + "," + size + "," + claimed + "," + Instant.now() + ","
                    + (System.nanoTime() - began) + ",verified,none\n";
        } catch (Exception e) {
            Files.deleteIfExists(output);
            return ordinal + "," + size + "," + claimed + "," + Instant.now() + ","
                    + (System.nanoTime() - began) + ",failed," + category(e) + "\n";
        }
    }

    private static byte[] payload(int repetition, int ordinal, int length) throws Exception {
        byte[] result = new byte[length];
        byte[] prefix = "CANIS-I31-v1".getBytes(StandardCharsets.US_ASCII);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int offset = 0, block = 0; offset < length; block++) {
            java.nio.ByteBuffer input = java.nio.ByteBuffer.allocate(prefix.length + 16);
            input.put(prefix).putInt(repetition).putInt(ordinal).putLong(block);
            byte[] hash = digest.digest(input.array());
            int count = Math.min(hash.length, length - offset);
            System.arraycopy(hash, 0, result, offset, count);
            offset += count;
        }
        return result;
    }

    private static void provisionMasterKey(Path path) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        byte[] raw = generator.generateKey().getEncoded();
        try {
            Files.writeString(path, Base64.getEncoder().encodeToString(raw), StandardCharsets.US_ASCII);
            restrict(path, false);
        } finally { Arrays.fill(raw, (byte) 0); }
    }

    private static void provisionTls(Path directory, String password) throws Exception {
        for (String name : List.of("server", "producer", "recipient", "untrusted")) {
            String commonName = name.equals("server") ? "localhost" : name;
            keytool(directory, password, "-genkeypair", "-alias", name, "-keyalg", "RSA", "-keysize", "2048",
                    "-validity", "2", "-dname", "CN=" + commonName, "-ext", "SAN=dns:localhost",
                    "-keystore", directory.resolve(name + ".p12").toString(), "-storetype", "PKCS12",
                    "-storepass:env", "CODE02_STORE_PASSWORD", "-keypass:env", "CODE02_STORE_PASSWORD", "-noprompt");
            restrict(directory.resolve(name + ".p12"), false);
            keytool(directory, password, "-exportcert", "-alias", name,
                    "-keystore", directory.resolve(name + ".p12").toString(), "-storetype", "PKCS12",
                    "-storepass:env", "CODE02_STORE_PASSWORD", "-file", directory.resolve(name + ".cer").toString());
        }
        for (String name : List.of("producer", "recipient")) {
            keytool(directory, password, "-importcert", "-alias", name, "-file", directory.resolve(name + ".cer").toString(),
                    "-keystore", directory.resolve("server-trust.p12").toString(), "-storetype", "PKCS12",
                    "-storepass:env", "CODE02_STORE_PASSWORD", "-noprompt");
        }
        for (String name : List.of("producer", "recipient", "untrusted")) {
            keytool(directory, password, "-importcert", "-alias", "server", "-file", directory.resolve("server.cer").toString(),
                    "-keystore", directory.resolve(name + "-trust.p12").toString(), "-storetype", "PKCS12",
                    "-storepass:env", "CODE02_STORE_PASSWORD", "-noprompt");
        }
        for (String name : List.of("server-trust", "producer-trust", "recipient-trust", "untrusted-trust")) {
            restrict(directory.resolve(name + ".p12"), false);
        }
    }

    private static void keytool(Path directory, String password, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable("keytool"));
        command.addAll(List.of(arguments));
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
        builder.environment().put("CODE02_STORE_PASSWORD", password);
        Process process = builder.start();
        process.getInputStream().readAllBytes();
        if (!process.waitFor(PROCESS_DEADLINE_SECONDS, TimeUnit.SECONDS) || process.exitValue() != 0) {
            process.destroyForcibly();
            throw new IOException("Temporary TLS fixture generation failed");
        }
    }

    private static String javaExecutable(String name) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }

    private static Map<String, String> clientEnvironment(Path privateDir, int port, String storePassword,
            String identity, String password) {
        return Map.of("CANIS_SERVER_HOST", "localhost", "CANIS_SERVER_PORT", Integer.toString(port),
                "CANIS_USERNAME", identity, "CANIS_PASSWORD", password,
                "CANIS_CLIENT_TLS_KEYSTORE", privateDir.resolve(identity + ".p12").toString(),
                "CANIS_CLIENT_TLS_KEYSTORE_PASSWORD", storePassword,
                "CANIS_CLIENT_TLS_TRUSTSTORE", privateDir.resolve(identity + "-trust.p12").toString(),
                "CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD", storePassword);
    }

    private static io.canis.jpaw.pojo.Environment clientConfiguration(Map<String, String> env) {
        return new io.canis.jpaw.pojo.Environment("localhost", Integer.parseInt(env.get("CANIS_SERVER_PORT")),
                env.get("CANIS_USERNAME"), env.get("CANIS_PASSWORD"), new io.canis.jpaw.config.TlsConfiguration(
                        Path.of(env.get("CANIS_CLIENT_TLS_KEYSTORE")), env.get("CANIS_CLIENT_TLS_KEYSTORE_PASSWORD"),
                        Path.of(env.get("CANIS_CLIENT_TLS_TRUSTSTORE")), env.get("CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD")));
    }

    private static void awaitServer(Map<String, String> env) throws Exception {
        for (int i = 0; i < 100; i++) {
            try (JpawClient client = new JpawClient(clientConfiguration(env))) {
                if ("OK".equals(client.health())) return;
            } catch (Exception ignored) { Thread.sleep(50); }
        }
        throw new IOException("TLS server did not become ready");
    }

    private static Process start(List<String> command, Path directory, Map<String, String> env, Path log) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile());
        builder.environment().keySet().removeIf(key -> key.startsWith("CANIS_") || key.startsWith("CODE02_"));
        builder.environment().putAll(env);
        return builder.start();
    }

    private static List<String> serverCommandWithInit(List<String> command) {
        List<String> result = new ArrayList<>(command);
        result.add("--init-db");
        return result;
    }

    private static List<String> workerCommand(List<String> base, String role, List<String> common) {
        List<String> result = new ArrayList<>(base);
        result.add(role);
        result.addAll(common);
        return result;
    }

    private static Path secureDirectory(Path path) throws IOException {
        Files.createDirectory(path);
        restrict(path, true);
        return path.toRealPath();
    }

    private static void restrict(Path path, boolean directory) throws IOException {
        PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (posix != null) {
            Files.setPosixFilePermissions(path, directory
                    ? EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE)
                    : EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            return;
        }
        AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (acl == null) throw new IOException("Filesystem access controls unavailable");
        AclEntry owner = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(acl.getOwner())
                .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
        acl.setAcl(List.of(owner));
    }

    private static Path safeChild(Path parent, String name) throws IOException {
        Path child = parent.resolve(name).normalize();
        if (!child.getParent().equals(parent) || Files.isSymbolicLink(child)) {
            throw new IOException("Unsafe research fixture path");
        }
        return child;
    }

    private static void assertSharedStorage(Path shared, String runId, String scenario, int expectedFiles) throws IOException {
        int complete = 0, partial = 0;
        try (var files = Files.list(shared)) {
            for (Path file : files.toList()) {
                if (Files.isSymbolicLink(file) || !file.getFileName().toString().startsWith(runId + "-")) {
                    throw new IOException("Unexpected shared-storage entry");
                }
                String name = file.getFileName().toString();
                boolean expectedName = false;
                for (int i = 0; i < expectedFiles; i++) {
                    if (name.equals(runId + "-" + i + ".canis")
                            || (scenario.equals("incomplete") && name.equals(runId + "-" + i + ".part"))) {
                        expectedName = true; break;
                    }
                }
                if (!expectedName) throw new IOException("Duplicate or unexpected shared-storage file");
                if (!name.endsWith(".canis") && !(scenario.equals("incomplete") && name.endsWith(".part"))) {
                    throw new IOException("Unexpected shared-storage file type");
                }
                if (name.endsWith(".canis")) {
                    complete++;
                    byte[] prefix = new byte[MAGIC.length()];
                    try (var stream = Files.newInputStream(file)) {
                        if (stream.read(prefix) != prefix.length
                                || !MAGIC.equals(new String(prefix, StandardCharsets.US_ASCII))) {
                            throw new IOException("Shared file is not a CANIS envelope");
                        }
                    }
                } else {
                    partial++;
                }
            }
        }
        if (scenario.equals("incomplete") ? complete != 0 || partial != 1 : complete != expectedFiles || partial != 0) {
            throw new IOException("Shared-storage publication count mismatch");
        }
    }

    private static int countVerified(Path csv) throws IOException {
        if (!Files.isRegularFile(csv)) return 0;
        int count = 0;
        for (String line : Files.readAllLines(csv)) if (line.endsWith(",verified,none")) count++;
        return count;
    }

    private static String readFailureCategory(Path csv) throws IOException {
        List<String> rows = Files.readAllLines(csv, StandardCharsets.US_ASCII);
        if (rows.size() != 2) throw new IOException("Fault result count mismatch");
        String row = rows.get(1);
        int separator = row.lastIndexOf(',');
        if (separator < 0) throw new IOException("Fault result malformed");
        return row.substring(separator + 1);
    }

    private static Instant firstPublication(Path csv) {
        try {
            List<String> rows = Files.readAllLines(csv, StandardCharsets.US_ASCII);
            if (rows.size() < 2) return null;
            return Instant.parse(rows.get(1).split(",", -1)[4]);
        } catch (Exception e) {
            return null;
        }
    }

    private static int countTlsRejections(Path log) throws IOException {
        if (!Files.isRegularFile(log)) return 0;
        String text = Files.readString(log, StandardCharsets.UTF_8);
        String marker = "TLS handshake or connection failed";
        int count = 0, cursor = 0;
        while ((cursor = text.indexOf(marker, cursor)) >= 0) {
            count++;
            cursor += marker.length();
        }
        return count;
    }

    private static boolean hasRecoveredFiles(Path privateDir) throws IOException {
        try (var files = Files.list(privateDir)) {
            return files.anyMatch(p -> p.getFileName().toString().startsWith("recovered-"));
        }
    }

    private static long peakHeap(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return -1;
        return Long.parseLong(Files.readString(path).trim());
    }

    private static void deleteRunRoot(Path root, String runId) throws IOException {
        if (!Files.isDirectory(root) || Files.isSymbolicLink(root)
                || !Files.readString(root.resolve(".code02-run")).equals(runId)
                || !root.toRealPath().startsWith(Path.of(System.getProperty("java.io.tmpdir")).toRealPath())) {
            throw new IOException("Run cleanup guard rejected path");
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path directory, IOException error) throws IOException {
                if (error != null) throw error;
                Files.delete(directory); return FileVisitResult.CONTINUE;
            }
        });
    }

    private static String resultJson(String id, String attempt, String revision, boolean dirty, Options options,
            String outcome, String failure, Instant producerStart, Instant firstPublication, Instant consumerStart, long serverPid,
            long producerPid, long consumerPid, long producerMs, long consumerMs, long producerHeap,
            long consumerHeap, long serverCpuNs, int verified) {
        return "{\n" +
                "  \"schema\": \"code02-v1\",\n" +
                "  \"run_id\": \"" + id + "\",\n" +
                "  \"attempt_id\": \"" + attempt + "\",\n" +
                "  \"commit\": \"" + revision + "\",\n" +
                "  \"dirty_tree\": " + dirty + ",\n" +
                "  \"timestamp_utc\": \"" + Instant.now() + "\",\n" +
                "  \"os\": \"" + json(System.getProperty("os.name") + " " + System.getProperty("os.version")) + "\",\n" +
                "  \"jdk_version\": \"" + json(System.getProperty("java.runtime.version")) + "\",\n" +
                "  \"scenario\": \"" + options.scenario + "\",\n" +
                "  \"file_size_bytes\": " + (options.size == 0 ? "null" : options.size) + ",\n" +
                "  \"size_schedule\": \"" + (options.size == 0 ? "issue31-v1" : "uniform") + "\",\n" +
                "  \"file_count\": " + options.files + ",\n" +
                "  \"repetition\": " + options.repetition + ",\n" +
                "  \"concurrency\": " + options.concurrency + ",\n" +
                "  \"delay_ms\": " + options.delayMs + ",\n" +
                "  \"interval_ms\": " + options.intervalMs + ",\n" +
                "  \"producer_start_utc\": " + nullable(producerStart) + ",\n" +
                "  \"first_publication_utc\": " + nullable(firstPublication) + ",\n" +
                "  \"consumer_start_utc\": " + nullable(consumerStart) + ",\n" +
                "  \"server_pid\": " + serverPid + ",\n" +
                "  \"producer_pid\": " + producerPid + ",\n" +
                "  \"consumer_pid\": " + consumerPid + ",\n" +
                "  \"producer_elapsed_ms\": " + producerMs + ",\n" +
                "  \"consumer_elapsed_ms\": " + consumerMs + ",\n" +
                "  \"producer_heap_used_bytes_at_exit\": " + producerHeap + ",\n" +
                "  \"consumer_heap_used_bytes_at_exit\": " + consumerHeap + ",\n" +
                "  \"server_cpu_ns_before_shutdown\": " + serverCpuNs + ",\n" +
                "  \"verified_files\": " + verified + ",\n" +
                "  \"outcome\": \"" + outcome + "\",\n" +
                "  \"failure_category\": \"" + failure + "\"\n}" + System.lineSeparator();
    }

    private static String nullable(Instant time) { return time == null ? "null" : "\"" + time + "\""; }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String randomHex(int bytes) {
        byte[] random = new byte[bytes]; RANDOM.nextBytes(random); return HexFormat.of().formatHex(random);
    }
    private static String category(Exception error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof javax.crypto.AEADBadTagException) return "authentication_tag_rejected";
            if (cause instanceof javax.net.ssl.SSLHandshakeException) return "tls_certificate_rejected";
        }
        if (error instanceof IOException && error.getMessage() != null
                && error.getMessage().startsWith("Authentication failed:")) {
            return "credentials_rejected";
        }
        if (error instanceof IOException && "TLS connection could not be established.".equals(error.getMessage())) {
            return "tls_connection_rejected";
        }
        if (error instanceof AtomicMoveNotSupportedException) return "atomic_move_unsupported";
        if (error instanceof IllegalArgumentException) return "invalid_input";
        if (error instanceof java.net.SocketTimeoutException) return "timeout";
        if (error instanceof IOException) return "io_or_authentication_failure";
        return "research_run_failure";
    }
    private static String commandOutput(Path cwd, String... command) throws Exception {
        Process p = new ProcessBuilder(command).directory(cwd.toFile()).start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (p.waitFor() != 0) throw new IOException("Source revision unavailable");
        return output;
    }
    private static void selfTest() throws Exception {
        if (!Arrays.equals(payload(1, 0, 4096), payload(1, 0, 4096))
                || Arrays.equals(payload(1, 0, 4096), payload(1, 1, 4096))) {
            throw new IllegalStateException("Fixture generation inconsistent");
        }
        int small = 0, medium = 0, large = 0;
        for (int i = 0; i < 100; i++) {
            int size = sizeFor(1, i, 0, 100);
            if (size == 4096) small++;
            else if (size == 256 * 1024) medium++;
            else if (size == 4 * 1024 * 1024) large++;
        }
        if (small != 50 || medium != 35 || large != 15) {
            throw new IllegalStateException("Issue 31 size schedule inconsistent");
        }
        try {
            Options.parse(new String[] {"--files", "1", "--size", "0"});
            throw new IllegalStateException("Invalid workload was accepted");
        } catch (IllegalArgumentException expected) { /* fail closed */ }
        Path root = Files.createTempDirectory("canis-code02-selftest-");
        try {
            Path shared = secureDirectory(root.resolve("shared"));
            if (!safeChild(shared, "test-0.part").getParent().equals(shared)) throw new IOException("Path guard failed");
            try { safeChild(shared, "../escape"); throw new IOException("Path guard accepted traversal"); }
            catch (IOException expected) { if (expected.getMessage().equals("Path guard accepted traversal")) throw expected; }
            Path partial = shared.resolve("test-0.part");
            byte[] random = new byte[32]; RANDOM.nextBytes(random);
            Files.write(partial, random);
            assertSharedStorage(shared, "test", "incomplete", 1);
            Files.write(shared.resolve("test-duplicate.canis"), random);
            try {
                assertSharedStorage(shared, "test", "incomplete", 1);
                throw new IllegalStateException("Duplicate was accepted");
            } catch (IOException expected) { /* fail closed */ }
        } finally {
            Files.deleteIfExists(root.resolve("shared/test-duplicate.canis"));
            Files.deleteIfExists(root.resolve("shared/test-0.part"));
            Files.deleteIfExists(root.resolve("shared"));
            Files.delete(root);
        }
        System.out.println("CODE02_SELF_TEST=PASS");
    }
}
