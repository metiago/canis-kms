import io.canis.handlers.ClientHandler;
import io.canis.jpaw.client.JpawClient;
import io.canis.jpaw.utils.EnvelopeCryptographer;
import io.canis.jpaw.utils.Parser;
import io.canis.net.BoundedLineReader;
import io.canis.store.KeyValueStore;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Issue #32 only. The plaintext listener is a loopback-only temporary research fixture. */
public final class Issue32Harness {
    private static final String MAGIC = "CANISHYB1";
    private static final int MAX_FILE = 4 * 1024 * 1024;
    private static final int DEADLINE_SECONDS = 1800;
    private static final String HEAP = "-Xms128m";
    private static final String MAX_HEAP = "-Xmx512m";

    private Issue32Harness() {}

    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("self-test")) selfTest();
            else if (args.length == 1 && args[0].equals("plain-server")) plainServer();
            else if (args.length == 9 && args[0].equals("worker")) worker(args);
            else if (args.length == 9 && args[0].equals("pair")) pair(args);
            else throw new IllegalArgumentException("Invalid Issue32 research arguments");
        } catch (Exception e) {
            System.err.println("ISSUE32_FAILURE=" + Code02Harness.category(e));
            System.exit(1);
        }
    }

    private static void selfTest() throws Exception {
        for (int n : new int[] {20, 100}) {
            int small = 0, middle = 0, large = 0;
            for (int i = 0; i < n; i++) {
                int size = sizeFor(1, i, n);
                if (size == 4096) small++;
                else if (size == 256 * 1024) middle++;
                else if (size == MAX_FILE) large++;
                else throw new IllegalStateException("Unverified file size");
            }
            if (small != (n == 20 ? 10 : 50) || middle != (n == 20 ? 7 : 35)
                    || large != (n == 20 ? 3 : 15)) throw new IllegalStateException("Invalid size schedule");
        }
        if (!Arrays.equals(Code02Harness.payload(2, 4, 4096), Code02Harness.payload(2, 4, 4096))) {
            throw new IllegalStateException("Synthetic payload mismatch");
        }
        System.out.println("ISSUE32_SELF_TEST=passed");
    }

    private static int sizeFor(int repetition, int ordinal, int count) {
        if (count == 1) return 4096;
        if (count != 20 && count != 100) throw new IllegalArgumentException("Unverified workload count");
        int[] sizes = new int[count];
        int small = count == 20 ? 10 : 50;
        int middle = count == 20 ? 17 : 85;
        Arrays.fill(sizes, 0, small, 4096);
        Arrays.fill(sizes, small, middle, 256 * 1024);
        Arrays.fill(sizes, middle, count, MAX_FILE);
        SplittableRandom random = new SplittableRandom(31000L + repetition);
        for (int i = count - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int previous = sizes[i]; sizes[i] = sizes[j]; sizes[j] = previous;
        }
        return sizes[ordinal];
    }

    private static void pair(String[] args) throws Exception {
        String prefix = args[1];
        int files = Integer.parseInt(args[2]);
        int repetition = Integer.parseInt(args[3]);
        long delayMs = Long.parseLong(args[4]);
        int workers = Integer.parseInt(args[5]);
        String order = args[6];
        boolean warmup = Boolean.parseBoolean(args[7]);
        Path output = Path.of(args[8]).toAbsolutePath().normalize();
        if (!prefix.matches("issue32-[a-zA-Z0-9-]+") || (files != 1 && files != 20 && files != 100)
                || repetition < 1 || repetition > 5 || (delayMs != 0 && delayMs != 60000)
                || (workers != 1 && workers != 4) || (!order.equals("AB") && !order.equals("BA"))
                || (warmup && files != 20) || (!warmup && files == 20)) {
            throw new IllegalArgumentException("Invalid or unverified Issue32 workload");
        }
        Path repository = Path.of("").toAbsolutePath().normalize();
        if (!output.startsWith(repository.resolve("server/target").normalize())) {
            throw new IOException("Research output must remain under server/target");
        }
        Files.createDirectories(output);
        for (String mode : List.of("A", "B")) {
            if (Files.exists(output.resolve(prefix + "-" + mode + ".json"))) {
                throw new IOException("Research result already exists");
            }
        }
        Path jar = repository.resolve("server/target/canis-jar-with-dependencies.jar");
        Path classes = repository.resolve("server/target/issue32-classes");
        Path client = repository.resolve("client/target/classes");
        if (!Files.isRegularFile(jar) || !Files.isDirectory(classes) || !Files.isDirectory(client)) {
            throw new IOException("Build and compile research sources first");
        }
        String revision = Code02Harness.commandOutput(repository, "git", "rev-parse", "HEAD").trim();
        boolean dirty = !Code02Harness.commandOutput(repository, "git", "status", "--porcelain").isBlank();
        String jarHash = sha256(jar);
        Path root = Files.createTempDirectory("canis-issue32-").toRealPath();
        Code02Harness.restrict(root, true);
        Files.writeString(root.resolve(".code02-run"), prefix, StandardCharsets.US_ASCII);
        Path privateDir = Code02Harness.secureDirectory(root.resolve("private"));
        Path provisionDir = Code02Harness.secureDirectory(root.resolve("provision"));
        for (String mode : List.of("A", "B")) {
            Path base = Code02Harness.secureDirectory(root.resolve(mode));
            for (String child : List.of("server", "shared", "private", "worker")) {
                Code02Harness.secureDirectory(base.resolve(child));
            }
        }
        Path pids = output.resolve(prefix + ".pids.csv");
        Files.writeString(pids, "run_id,role,pid\n", StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
        boolean allPassed = false;
        String setupFailure = "none";
        String storePassword = Code02Harness.randomHex(24);
        String producerPassword = Code02Harness.randomHex(24);
        String recipientPassword = Code02Harness.randomHex(24);
        try {
            Code02Harness.provisionMasterKey(privateDir.resolve("master.key"));
            Code02Harness.provisionTls(privateDir, storePassword, List.of("producer", "recipient"));
            int port = freePort();
            Map<String, String> serverEnv = serverEnvironment(privateDir, port, storePassword,
                    producerPassword, recipientPassword, root, prefix);
            List<String> tlsCommand = List.of(Code02Harness.javaExecutable("java"), HEAP, MAX_HEAP,
                    "-jar", jar.toString());
            Process init = Code02Harness.start(Code02Harness.serverCommandWithInit(tlsCommand), provisionDir,
                    serverEnv, privateDir.resolve("init.log"));
            if (!init.waitFor(90, TimeUnit.SECONDS) || init.exitValue() != 0
                    || !Files.isRegularFile(provisionDir.resolve("db.dat"))) {
                init.destroyForcibly();
                throw new IOException("Synthetic database initialization failed");
            }
            Process provisioningServer = null;
            try {
                provisioningServer = Code02Harness.start(tlsCommand, provisionDir, serverEnv,
                        privateDir.resolve("provision-server.log"));
                Map<String, String> recipientEnv = Code02Harness.clientEnvironment(privateDir, port,
                        storePassword, "recipient", recipientPassword);
                Code02Harness.awaitServer(recipientEnv);
                try (JpawClient recipient = new JpawClient(Code02Harness.clientConfiguration(recipientEnv))) {
                    recipient.set("recipient");
                    Path source = privateDir.resolve("parity-source.bin");
                    Path envelope = privateDir.resolve("parity-envelope.canis");
                    Path recovered = privateDir.resolve("parity-recovered.bin");
                    Files.write(source, Code02Harness.payload(repetition, 0, 4096));
                    recipient.encryptFile("recipient", source.toFile(), envelope.toFile());
                    recipient.decryptFile("recipient", envelope.toFile(), recovered.toFile());
                    if (!Arrays.equals(Files.readAllBytes(source), Files.readAllBytes(recovered))) {
                        throw new IOException("Official client parity smoke failed");
                    }
                    Files.delete(source); Files.delete(envelope); Files.delete(recovered);
                }
            } finally {
                stop(provisioningServer);
            }
            String databaseHash = sha256(provisionDir.resolve("db.dat"));
            for (String mode : List.of("A", "B")) {
                Files.copy(provisionDir.resolve("db.dat"), root.resolve(mode).resolve("server/db.dat"));
            }
            writeInputs(output.resolve(prefix + ".inputs.csv"), repetition, files);
            for (char mode : order.toCharArray()) {
                RunResult result = runMode(repository, root, output, pids, prefix, String.valueOf(mode), files,
                        repetition, delayMs, workers, warmup, storePassword, producerPassword,
                        recipientPassword, revision, dirty, jarHash, databaseHash);
                if (!result.passed()) setupFailure = result.failure();
            }
            allPassed = Files.isRegularFile(output.resolve(prefix + "-A.json"))
                    && Files.isRegularFile(output.resolve(prefix + "-B.json"))
                    && Files.readString(output.resolve(prefix + "-A.json")).contains("\"status\": \"passed\"")
                    && Files.readString(output.resolve(prefix + "-B.json")).contains("\"status\": \"passed\"");
        } catch (Exception e) {
            setupFailure = Code02Harness.category(e);
        } finally {
            if (allPassed) {
                try { Code02Harness.deleteRunRoot(root, prefix); }
                catch (IOException e) { allPassed = false; setupFailure = "fixture_cleanup_failed"; }
            }
            if (!allPassed) {
                String record = "{\n  \"schema\": \"issue32-pair-failure-v1\",\n"
                        + "  \"pair_id\": \"" + prefix + "\",\n"
                        + "  \"failure_category\": \"" + setupFailure + "\",\n"
                        + "  \"private_fixture_retained\": true\n}\n";
                Files.writeString(output.resolve(prefix + ".pair-failure.json"), record, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW);
                System.err.println("ISSUE32_PRIVATE_EVIDENCE=" + root);
            }
        }
        System.out.println("ISSUE32_PAIR=" + (allPassed ? "passed" : "failed") + " id=" + prefix
                + " files=" + files + " order=" + order + " category=" + setupFailure);
        if (!allPassed) throw new IOException("Issue32 pair failed");
    }

    private record RunResult(boolean passed, String failure) {}

    private static RunResult runMode(Path repository, Path root, Path output, Path pids, String prefix,
            String mode, int files, int repetition, long delayMs, int workers, boolean warmup,
            String storePassword, String producerPassword, String recipientPassword, String revision,
            boolean dirty, String jarHash, String databaseHash) throws Exception {
        String runId = prefix + "-" + mode;
        Path base = root.resolve(mode);
        Path privateDir = base.resolve("private");
        Path shared = base.resolve("shared");
        Path serverDir = base.resolve("server");
        Path workerDir = base.resolve("worker");
        Path globalPrivate = root.resolve("private");
        int port = freePort();
        Map<String, String> serverEnv = serverEnvironment(globalPrivate, port, storePassword,
                producerPassword, recipientPassword, root, prefix);
        Map<String, String> producerEnv = Code02Harness.clientEnvironment(globalPrivate, port,
                storePassword, "producer", producerPassword);
        Map<String, String> recipientEnv = Code02Harness.clientEnvironment(globalPrivate, port,
                storePassword, "recipient", recipientPassword);
        Path jar = repository.resolve("server/target/canis-jar-with-dependencies.jar");
        String classpath = repository.resolve("server/target/issue32-classes") +
                System.getProperty("path.separator") + repository.resolve("client/target/classes") +
                System.getProperty("path.separator") + jar;
        List<String> serverCommand = mode.equals("A")
                ? List.of(Code02Harness.javaExecutable("java"), HEAP, MAX_HEAP, "-jar", jar.toString())
                : List.of(Code02Harness.javaExecutable("java"), HEAP, MAX_HEAP,
                        "-cp", classpath, "Issue32Harness", "plain-server");
        long serverPid = -1, producerPid = -1, consumerPid = -1;
        Process server = null, producer = null, consumer = null;
        String status = "failed", failure = "none";
        int successful = 0, failed = files;
        Instant producerStart = null, runFinish = null;
        int producerExit = -1, consumerExit = -1;
        String tlsProbeProtocol = "not_applicable", tlsProbeSuite = "not_applicable";
        try {
            if (!sha256(serverDir.resolve("db.dat")).equals(databaseHash)) {
                throw new IOException("Paired database mismatch");
            }
            server = Code02Harness.start(serverCommand, serverDir, serverEnv,
                    globalPrivate.resolve(runId + ".server.log"));
            serverPid = server.pid();
            appendPid(pids, runId, "server", serverPid);
            if (mode.equals("A")) {
                Code02Harness.awaitServer(recipientEnv);
                String[] probe = probeTls(recipientEnv);
                tlsProbeProtocol = probe[0]; tlsProbeSuite = probe[1];
                if (!"TLSv1.3".equals(tlsProbeProtocol)) throw new IOException("TLS probe version mismatch");
            } else awaitPlainServer(port, recipientPassword);
            List<String> workerBase = List.of(Code02Harness.javaExecutable("java"), HEAP, MAX_HEAP,
                    "-cp", classpath, "Issue32Harness", "worker");
            List<String> common = List.of(root.toString(), prefix, mode, Integer.toString(files),
                    Integer.toString(repetition), Integer.toString(workers), "500");
            List<String> producerCommand = new ArrayList<>(workerBase);
            producerCommand.add("producer"); producerCommand.addAll(common);
            producer = Code02Harness.start(producerCommand, workerDir, producerEnv,
                    globalPrivate.resolve(runId + ".producer.log"));
            producerPid = producer.pid(); appendPid(pids, runId, "producer", producerPid);
            long runDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(DEADLINE_SECONDS);
            Instant firstPublication = awaitFirstPublication(shared, runId, producer);
            Instant consumerDue = firstPublication.plusMillis(delayMs);
            while (Instant.now().isBefore(consumerDue)) Thread.sleep(5);
            List<String> consumerCommand = new ArrayList<>(workerBase);
            consumerCommand.add("consumer"); consumerCommand.addAll(common);
            consumer = Code02Harness.start(consumerCommand, workerDir, recipientEnv,
                    globalPrivate.resolve(runId + ".consumer.log"));
            consumerPid = consumer.pid(); appendPid(pids, runId, "consumer", consumerPid);
            if (!producer.waitFor(remainingMillis(runDeadline), TimeUnit.MILLISECONDS)) {
                throw new IOException("Producer deadline exceeded");
            }
            producerExit = producer.exitValue();
            if (!consumer.waitFor(remainingMillis(runDeadline), TimeUnit.MILLISECONDS)) {
                throw new IOException("Consumer deadline exceeded");
            }
            consumerExit = consumer.exitValue();
            Path producerCsv = privateDir.resolve("producer.csv");
            Path consumerCsv = privateDir.resolve("consumer.csv");
            successful = countOutcome(consumerCsv, "verified");
            failed = files - successful;
            if (producerExit != 0 || consumerExit != 0 || successful != files
                    || countOutcome(producerCsv, "published") != files) {
                throw new IOException("File workflow incomplete");
            }
            assertShared(shared, runId, files);
            status = "passed";
        } catch (Exception e) {
            failure = Code02Harness.category(e);
        } finally {
            stop(producer); stop(consumer); stop(server);
            for (String name : List.of("producer.csv", "consumer.csv", "producer-connections.csv",
                    "consumer-connections.csv", "producer-heap.txt", "consumer-heap.txt")) {
                Path source = privateDir.resolve(name);
                if (Files.isRegularFile(source)) {
                    Files.copy(source, output.resolve(runId + "." + name), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            Path producerCsv = privateDir.resolve("producer.csv");
            Path consumerCsv = privateDir.resolve("consumer.csv");
            producerStart = firstTimestamp(producerCsv, 2);
            runFinish = lastTimestamp(consumerCsv, 3);
            if (runFinish == null) runFinish = Instant.now();
            long duration = producerStart == null ? -1 : Duration.between(producerStart, runFinish).toMillis();
            String json = "{\n" +
                    "  \"schema\": \"issue32-run-v1\",\n" +
                    "  \"run_id\": \"" + runId + "\",\n" +
                    "  \"mode\": \"" + mode + "\",\n" +
                    "  \"warmup\": " + warmup + ",\n" +
                    "  \"repetition\": " + repetition + ",\n" +
                    "  \"delay_ms\": " + delayMs + ",\n" +
                    "  \"workers\": " + workers + ",\n" +
                    "  \"planned_files\": " + files + ",\n" +
                    "  \"successful_files\": " + successful + ",\n" +
                    "  \"failed_files\": " + failed + ",\n" +
                    "  \"run_duration_ms\": " + duration + ",\n" +
                    "  \"producer_exit_code\": " + producerExit + ",\n" +
                    "  \"consumer_exit_code\": " + consumerExit + ",\n" +
                    "  \"server_pid\": " + serverPid + ",\n" +
                    "  \"producer_pid\": " + producerPid + ",\n" +
                    "  \"consumer_pid\": " + consumerPid + ",\n" +
                    "  \"first_planned_utc\": " + quotedInstant(producerStart) + ",\n" +
                    "  \"last_finish_utc\": " + quotedInstant(runFinish) + ",\n" +
                    "  \"source_revision\": \"" + revision + "\",\n" +
                    "  \"dirty_tree\": " + dirty + ",\n" +
                    "  \"server_jar_sha256\": \"" + jarHash + "\",\n" +
                    "  \"database_snapshot_sha256\": \"" + databaseHash + "\",\n" +
                    "  \"tls_probe_protocol\": \"" + tlsProbeProtocol + "\",\n" +
                    "  \"tls_probe_suite\": \"" + tlsProbeSuite + "\",\n" +
                    "  \"os\": \"" + Code02Harness.json(System.getProperty("os.name") + " "
                            + System.getProperty("os.version")) + "\",\n" +
                    "  \"jdk\": \"" + Code02Harness.json(System.getProperty("java.runtime.version")) + "\",\n" +
                    "  \"status\": \"" + status + "\",\n" +
                    "  \"failure_category\": \"" + failure + "\"\n" +
                    "}\n";
            Files.writeString(output.resolve(runId + ".json"), json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
        }
        return new RunResult(status.equals("passed"), failure);
    }

    private static void worker(String[] args) throws Exception {
        String role = args[1];
        Path root = Path.of(args[2]).toRealPath();
        String prefix = args[3], mode = args[4];
        int files = Integer.parseInt(args[5]), repetition = Integer.parseInt(args[6]);
        int workers = Integer.parseInt(args[7]);
        long intervalMs = Long.parseLong(args[8]);
        if (!Files.readString(root.resolve(".code02-run")).equals(prefix)
                || !List.of("A", "B").contains(mode) || !List.of("producer", "consumer").contains(role)
                || (files != 1 && files != 20 && files != 100) || workers < 1 || workers > 4
                || intervalMs != 500 || repetition < 1 || repetition > 5) {
            throw new IOException("Research worker fixture rejected");
        }
        Path base = root.resolve(mode);
        Path privateDir = base.resolve("private");
        Path shared = base.resolve("shared");
        if (!privateDir.toRealPath().startsWith(root) || !shared.toRealPath().startsWith(root)) {
            throw new IOException("Research worker path rejected");
        }
        String runId = prefix + "-" + mode;
        AtomicBoolean sampling = new AtomicBoolean(true);
        AtomicLong peakHeap = new AtomicLong();
        Thread heapThread = Thread.ofVirtual().start(() -> {
            while (sampling.get()) {
                long used = java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
                peakHeap.accumulateAndGet(used, Math::max);
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
            }
        });
        try {
            if (role.equals("producer")) produce(mode, privateDir, shared, runId, files, repetition, intervalMs);
            else consume(mode, privateDir, shared, runId, files, repetition, workers);
        } finally {
            sampling.set(false); heapThread.join(500);
            Files.writeString(privateDir.resolve(role + "-heap.txt"), Long.toString(peakHeap.get()),
                    StandardCharsets.US_ASCII);
        }
    }

    private static void produce(String mode, Path privateDir, Path shared, String runId,
            int files, int repetition, long intervalMs) throws Exception {
        StringBuilder rows = new StringBuilder("ordinal,size,planned_start_utc,actual_start_utc,published_utc,fetch_ns,local_envelope_ns,source_sha256,outcome,failure_category\n");
        StringBuilder connections = new StringBuilder("worker,connect_login_ns,outcome\n");
        long started = System.nanoTime();
        KeyClient client = null;
        String connectFailure = "none";
        try {
            client = connect(mode);
            connections.append("0,").append(System.nanoTime() - started).append(",connected\n");
        } catch (Exception e) {
            connectFailure = Code02Harness.category(e);
            connections.append("0,").append(System.nanoTime() - started).append(",failed\n");
        }
        Instant scheduleUtc = Instant.now();
        long scheduleNanos = System.nanoTime();
        int published = 0;
        try {
            for (int i = 0; i < files; i++) {
                long due = scheduleNanos + TimeUnit.MILLISECONDS.toNanos(intervalMs * i);
                while (System.nanoTime() < due) Thread.sleep(1);
                Instant actual = Instant.now();
                int size = sizeFor(repetition, i, files);
                byte[] source = Code02Harness.payload(repetition, i, size);
                String sourceHash = sha256(source);
                Path input = Code02Harness.safeChild(privateDir, "source-" + i + ".bin");
                Files.write(input, source, StandardOpenOption.CREATE_NEW);
                Path part = Code02Harness.safeChild(shared, runId + "-" + i + ".part");
                Path publishedPath = Code02Harness.safeChild(shared, runId + "-" + i + ".canis");
                long fetchNs = -1, localNs = -1;
                String outcome = "failed", failure = connectFailure;
                Instant publishedUtc = null;
                try {
                    if (client == null) throw new IOException("Client connection unavailable");
                    if (Files.exists(part) || Files.exists(publishedPath)) throw new IOException("Duplicate publication");
                    long begin = System.nanoTime();
                    String encoded = client.getPublicKey("recipient");
                    fetchNs = System.nanoTime() - begin;
                    PublicKey publicKey = EnvelopeCryptographer.publicKeyFromString(encoded);
                    begin = System.nanoTime();
                    EnvelopeCryptographer.encryptFile(input.toFile(), part.toFile(), publicKey);
                    localNs = System.nanoTime() - begin;
                    try (var stream = Files.newInputStream(part)) {
                        byte[] header = stream.readNBytes(MAGIC.length());
                        if (!MAGIC.equals(new String(header, StandardCharsets.US_ASCII))) {
                            throw new IOException("Invalid encrypted envelope");
                        }
                    }
                    Files.move(part, publishedPath, StandardCopyOption.ATOMIC_MOVE);
                    publishedUtc = Instant.now();
                    outcome = "published"; failure = "none"; published++;
                } catch (Exception e) {
                    failure = Code02Harness.category(e);
                    Files.deleteIfExists(part);
                }
                rows.append(i).append(',').append(size).append(',')
                        .append(scheduleUtc.plusMillis(intervalMs * i)).append(',').append(actual).append(',')
                        .append(publishedUtc == null ? "not_applicable" : publishedUtc).append(',')
                        .append(fetchNs).append(',').append(localNs).append(',').append(sourceHash).append(',')
                        .append(outcome).append(',').append(failure).append('\n');
            }
        } finally {
            if (client != null) client.close();
            Files.writeString(privateDir.resolve("producer.csv"), rows, StandardCharsets.US_ASCII);
            Files.writeString(privateDir.resolve("producer-connections.csv"), connections, StandardCharsets.US_ASCII);
        }
        if (published != files) throw new IOException("Producer incomplete");
    }

    private static void consume(String mode, Path privateDir, Path shared, String runId,
            int files, int repetition, int workers) throws Exception {
        String[] rows = new String[files];
        String[] connections = new String[workers];
        try (var pool = Executors.newFixedThreadPool(workers)) {
            List<Future<Void>> futures = new ArrayList<>();
            for (int worker = 0; worker < workers; worker++) {
                int assigned = worker;
                futures.add(pool.submit((Callable<Void>) () -> {
                    KeyClient client = null;
                    String connectFailure = "none";
                    long begin = System.nanoTime();
                    try {
                        client = connect(mode);
                        connections[assigned] = assigned + "," + (System.nanoTime() - begin) + ",connected\n";
                    } catch (Exception e) {
                        connectFailure = Code02Harness.category(e);
                        connections[assigned] = assigned + "," + (System.nanoTime() - begin) + ",failed\n";
                    }
                    try {
                        for (int i = assigned; i < files; i += workers) {
                            rows[i] = consumeOne(client, connectFailure, privateDir, shared,
                                    runId, repetition, i, sizeFor(repetition, i, files));
                        }
                    } finally {
                        if (client != null) client.close();
                    }
                    return null;
                }));
            }
            for (Future<Void> future : futures) future.get();
        }
        StringBuilder csv = new StringBuilder("ordinal,size,claimed_utc,finished_utc,unwrap_ns,local_decrypt_verify_ns,verified_sha256,outcome,failure_category\n");
        StringBuilder conn = new StringBuilder("worker,connect_login_ns,outcome\n");
        int verified = 0;
        for (String connection : connections) conn.append(connection);
        for (String row : rows) {
            csv.append(row);
            if (row != null && row.endsWith(",verified,none\n")) verified++;
        }
        Files.writeString(privateDir.resolve("consumer.csv"), csv, StandardCharsets.US_ASCII);
        Files.writeString(privateDir.resolve("consumer-connections.csv"), conn, StandardCharsets.US_ASCII);
        if (verified != files) throw new IOException("Consumer incomplete");
    }

    private static String consumeOne(KeyClient client, String connectFailure, Path privateDir, Path shared,
            String runId, int repetition, int ordinal, int size) throws Exception {
        Path envelope = Code02Harness.safeChild(shared, runId + "-" + ordinal + ".canis");
        Path output = Code02Harness.safeChild(privateDir, "recovered-" + ordinal + ".bin");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (!Files.isRegularFile(envelope) && System.nanoTime() < deadline) Thread.sleep(10);
        if (!Files.isRegularFile(envelope)) {
            return ordinal + "," + size + ",not_applicable," + Instant.now()
                    + ",-1,-1,not_applicable,failed,incomplete_or_missing\n";
        }
        Instant claim = Instant.now();
        if (client == null) {
            return ordinal + "," + size + "," + claim + "," + Instant.now()
                    + ",-1,-1,not_applicable,failed," + connectFailure + "\n";
        }
        AtomicLong unwrap = new AtomicLong();
        long begin = System.nanoTime();
        try {
            EnvelopeCryptographer.decryptFile(envelope.toFile(), output.toFile(), encryptedKey -> {
                long request = System.nanoTime();
                try { return client.decrypt("recipient", encryptedKey); }
                finally { unwrap.addAndGet(System.nanoTime() - request); }
            });
            byte[] recovered = Files.readAllBytes(output);
            if (!Arrays.equals(Code02Harness.payload(repetition, ordinal, size), recovered)) {
                throw new IOException("Recovered bytes mismatch");
            }
            long local = System.nanoTime() - begin - unwrap.get();
            return ordinal + "," + size + "," + claim + "," + Instant.now() + ","
                    + unwrap.get() + "," + local + "," + sha256(recovered) + ",verified,none\n";
        } catch (Exception e) {
            Files.deleteIfExists(output);
            long local = System.nanoTime() - begin - unwrap.get();
            return ordinal + "," + size + "," + claim + "," + Instant.now() + ","
                    + unwrap.get() + "," + local + ",not_applicable,failed,"
                    + Code02Harness.category(e) + "\n";
        }
    }

    private interface KeyClient extends AutoCloseable {
        String getPublicKey(String key) throws IOException;
        byte[] decrypt(String key, byte[] wrapped) throws IOException;
        @Override void close() throws IOException;
    }

    private static KeyClient connect(String mode) throws IOException {
        if (mode.equals("A")) {
            JpawClient tls = new JpawClient();
            return new KeyClient() {
                public String getPublicKey(String key) throws IOException { return tls.getPublicKey(key); }
                public byte[] decrypt(String key, byte[] wrapped) throws IOException { return tls.decrypt(key, wrapped); }
                public void close() throws IOException { tls.close(); }
            };
        }
        return new PlainClient("127.0.0.1", Integer.parseInt(System.getenv("CANIS_SERVER_PORT")),
                System.getenv("CANIS_USERNAME"), System.getenv("CANIS_PASSWORD"));
    }

    private static final class PlainClient implements KeyClient {
        private final Socket socket;
        private final PrintWriter out;
        private final DataInputStream in;

        PlainClient(String host, int port, String username, String password) throws IOException {
            if (!host.equals("127.0.0.1") || username == null || password == null) {
                throw new IOException("Research loopback client configuration rejected");
            }
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 30_000);
                socket.setSoTimeout(30_000);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new DataInputStream(socket.getInputStream());
                send("|login " + username + ":" + password);
                if (!"Authentication successful".equals(read())) {
                    throw new IOException("Authentication failed");
                }
            } catch (IOException e) {
                socket.close();
                throw e;
            }
        }

        private void send(String line) throws IOException {
            if (line.length() > 1_048_576) throw new IOException("Research command too long");
            out.println(line);
            if (out.checkError()) throw new IOException("Research send failed");
        }

        private String read() throws IOException {
            int length = in.readInt();
            if (length <= 0 || length > 1_048_576) throw new IOException("Research response invalid");
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }

        String health() throws IOException {
            send("|health");
            return Parser.parseString(read());
        }

        public String getPublicKey(String key) throws IOException {
            send("|get-public " + key);
            String response = Parser.parseString(read());
            if (response.startsWith("ERROR:")) throw new IOException("Research key request failed");
            return response;
        }

        public byte[] decrypt(String key, byte[] wrapped) throws IOException {
            send("|decrypt " + key + " " + Base64.getEncoder().encodeToString(wrapped));
            String response = Parser.parseString(read());
            if (response.startsWith("ERROR:")) throw new IOException("Research unwrap request failed");
            try { return Base64.getDecoder().decode(response); }
            catch (IllegalArgumentException e) { throw new IOException("Research unwrap response invalid"); }
        }

        public void close() throws IOException { socket.close(); }
    }

    private static void plainServer() throws Exception {
        String rootValue = System.getenv("CANIS_ISSUE32_RUN_ROOT");
        String runId = System.getenv("CANIS_ISSUE32_RUN_ID");
        String keyValue = System.getenv("CANIS_SECRET_KEY");
        String portValue = System.getenv("CANIS_PORT");
        if (rootValue == null || runId == null || keyValue == null || portValue == null) {
            throw new IOException("Research server configuration absent");
        }
        Path root = Path.of(rootValue).toRealPath();
        Path temp = Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
        Path cwd = Path.of("").toRealPath();
        Path master = Path.of(keyValue).toRealPath();
        if (!root.startsWith(temp) || !root.getFileName().toString().startsWith("canis-issue32-")
                || !Files.readString(root.resolve(".code02-run")).equals(runId)
                || !cwd.equals(root.resolve("B/server").toRealPath())
                || !master.equals(root.resolve("private/master.key").toRealPath())
                || !Files.isRegularFile(cwd.resolve("db.dat"))) {
            throw new IOException("Research server fixture guard rejected path");
        }
        int port = Integer.parseInt(portValue);
        if (port < 1 || port > 65535) throw new IOException("Research port invalid");
        String recipientPassword = System.getenv("CANIS_PASSWORD");
        String producerEntry = System.getenv("CANIS_SERVICE_CREDENTIALS");
        if (recipientPassword == null || producerEntry == null || !producerEntry.startsWith("producer:")) {
            throw new IOException("Research credentials absent");
        }
        Map<String, String> credentials = Map.of("recipient", recipientPassword,
                "producer", producerEntry.substring("producer:".length()));
        KeyValueStore store = new KeyValueStore("db.dat", keyValue);
        store.ensureHealthy();
        try (ServerSocket listener = new ServerSocket()) {
            listener.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
            System.out.println("ISSUE32_RESEARCH_LOOPBACK_LISTENER=ready");
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (store.isHealthy()) {
                    Socket socket = listener.accept();
                    executor.submit(() -> handlePlain(socket, store, credentials));
                }
            }
        }
    }

    private static void handlePlain(Socket socket, KeyValueStore store, Map<String, String> credentials) {
        try (socket) {
            if (!socket.getInetAddress().isLoopbackAddress()) return;
            socket.setSoTimeout(30_000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String login = BoundedLineReader.readLine(in);
            String identity = null;
            if (login != null && login.startsWith("|login ")) {
                String argument = login.substring(7).trim();
                int separator = argument.indexOf(':');
                if (separator > 0) {
                    String username = argument.substring(0, separator).trim();
                    String password = argument.substring(separator + 1).trim();
                    if (!password.isEmpty() && password.equals(credentials.get(username))) identity = username;
                }
            }
            byte[] response = (identity == null ? "Authentication failed" : "Authentication successful")
                    .getBytes(StandardCharsets.UTF_8);
            out.writeInt(response.length); out.write(response);
            if (identity != null) new ClientHandler(socket, in, out, store, identity).run();
        } catch (Exception ignored) {
            // Private fixture log contains no request or key material.
        }
    }

    private static Map<String, String> serverEnvironment(Path privateDir, int port, String storePassword,
            String producerPassword, String recipientPassword, Path root, String runId) {
        return Map.ofEntries(
                Map.entry("CANIS_PORT", Integer.toString(port)),
                Map.entry("CANIS_USERNAME", "recipient"),
                Map.entry("CANIS_PASSWORD", recipientPassword),
                Map.entry("CANIS_SERVICE_CREDENTIALS", "producer:" + producerPassword),
                Map.entry("CANIS_SECRET_KEY", privateDir.resolve("master.key").toString()),
                Map.entry("CANIS_TLS_KEYSTORE", privateDir.resolve("server.p12").toString()),
                Map.entry("CANIS_TLS_KEYSTORE_PASSWORD", storePassword),
                Map.entry("CANIS_TLS_TRUSTSTORE", privateDir.resolve("server-trust.p12").toString()),
                Map.entry("CANIS_TLS_TRUSTSTORE_PASSWORD", storePassword),
                Map.entry("CANIS_ISSUE32_RUN_ROOT", root.toString()),
                Map.entry("CANIS_ISSUE32_RUN_ID", runId));
    }

    private static void awaitPlainServer(int port, String password) throws Exception {
        for (int i = 0; i < 100; i++) {
            try (PlainClient client = new PlainClient("127.0.0.1", port, "recipient", password)) {
                if ("OK".equals(client.health())) return;
            } catch (IOException ignored) { Thread.sleep(50); }
        }
        throw new IOException("Research loopback server did not become ready");
    }

    private static String[] probeTls(Map<String, String> env) throws Exception {
        int port = Integer.parseInt(env.get("CANIS_SERVER_PORT"));
        var context = io.canis.jpaw.config.TlsContextFactory.create(Code02Harness.clientConfiguration(env).tls());
        Socket transport = new Socket();
        try {
            transport.connect(new InetSocketAddress("localhost", port), 30_000);
            try (var socket = (javax.net.ssl.SSLSocket)
                    context.getSocketFactory().createSocket(transport, "localhost", port, true)) {
                socket.setSoTimeout(30_000);
                io.canis.jpaw.config.TlsCipherPolicy.configure(socket);
                var parameters = socket.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                socket.setSSLParameters(parameters);
                socket.startHandshake();
                return new String[] {socket.getSession().getProtocol(), socket.getSession().getCipherSuite()};
            }
        } finally {
            transport.close();
        }
    }

    private static Instant awaitFirstPublication(Path shared, String runId, Process producer) throws Exception {
        Path first = shared.resolve(runId + "-0.canis");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90);
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(first)) return Instant.now();
            if (!producer.isAlive()) break;
            Thread.sleep(5);
        }
        throw new IOException("First encrypted publication unavailable");
    }

    private static void assertShared(Path shared, String runId, int files) throws IOException {
        try (var stream = Files.list(shared)) {
            List<Path> paths = stream.toList();
            if (paths.size() != files) throw new IOException("Shared envelope count mismatch");
            for (Path path : paths) {
                if (Files.isSymbolicLink(path) || !path.getFileName().toString().startsWith(runId + "-")
                        || !path.getFileName().toString().endsWith(".canis")) {
                    throw new IOException("Unexpected shared entry");
                }
                try (var input = Files.newInputStream(path)) {
                    if (!MAGIC.equals(new String(input.readNBytes(MAGIC.length()), StandardCharsets.US_ASCII))) {
                        throw new IOException("Shared plaintext or invalid envelope");
                    }
                }
            }
        }
    }

    private static void writeInputs(Path output, int repetition, int count) throws Exception {
        StringBuilder csv = new StringBuilder("ordinal,size,source_sha256\n");
        for (int i = 0; i < count; i++) {
            int size = sizeFor(repetition, i, count);
            csv.append(i).append(',').append(size).append(',')
                    .append(sha256(Code02Harness.payload(repetition, i, size))).append('\n');
        }
        Files.writeString(output, csv, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
    }

    private static int countOutcome(Path csv, String outcome) throws IOException {
        if (!Files.isRegularFile(csv)) return 0;
        int count = 0;
        for (String line : Files.readAllLines(csv)) {
            if (line.contains("," + outcome + ",none")) count++;
        }
        return count;
    }

    private static Instant firstTimestamp(Path csv, int column) {
        try {
            List<String> rows = Files.readAllLines(csv);
            return rows.size() < 2 ? null : Instant.parse(rows.get(1).split(",", -1)[column]);
        } catch (Exception e) { return null; }
    }

    private static Instant lastTimestamp(Path csv, int column) {
        try {
            List<String> rows = Files.readAllLines(csv);
            Instant latest = null;
            for (String row : rows.subList(1, rows.size())) {
                Instant value = Instant.parse(row.split(",", -1)[column]);
                if (latest == null || value.isAfter(latest)) latest = value;
            }
            return latest;
        } catch (Exception e) { return null; }
    }

    private static void appendPid(Path path, String runId, String role, long pid) throws IOException {
        Files.writeString(path, runId + "," + role + "," + pid + "\n", StandardCharsets.US_ASCII,
                StandardOpenOption.APPEND);
    }

    private static int freePort() throws IOException {
        try (ServerSocket reservation = new ServerSocket(0)) { return reservation.getLocalPort(); }
    }

    private static long remainingMillis(long deadline) {
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
    }

    private static void stop(Process process) {
        if (process == null) return;
        if (process.isAlive()) process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly(); process.waitFor(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); process.destroyForcibly(); }
    }

    private static String sha256(Path path) throws Exception { return sha256(Files.readAllBytes(path)); }
    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    private static String quotedInstant(Instant value) { return value == null ? "null" : "\"" + value + "\""; }
}
