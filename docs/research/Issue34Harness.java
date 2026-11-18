import io.canis.jpaw.client.JpawClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Research-only fault injection. All mutable fixtures live below a fresh temporary root. */
public final class Issue34Harness {
    private static final int DEADLINE_SECONDS = 90;
    private static final String CSV_HEADER = "phase,time_utc,operation,process_exit,outcome,failure_category,output_present,log_marker\n";

    private Issue34Harness() {}

    private record Options(String scenario, String variant, int repetition, Path output) {
        static Options parse(String[] args) {
            String scenario = null, variant = "standard";
            int repetition = 1;
            Path output = null;
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) throw new IllegalArgumentException("Missing option value");
                switch (args[i]) {
                    case "--scenario" -> scenario = args[i + 1];
                    case "--variant" -> variant = args[i + 1];
                    case "--repetition" -> repetition = Integer.parseInt(args[i + 1]);
                    case "--output" -> output = Path.of(args[i + 1]);
                    default -> throw new IllegalArgumentException("Unknown option");
                }
            }
            if (!List.of("A", "B", "C", "D", "E").contains(scenario) || repetition < 1 || repetition > 3
                    || (scenario.equals("C") && !List.of("live", "stopped").contains(variant))
                    || (scenario.equals("D") && !List.of("modified", "deleted").contains(variant))
                    || (!List.of("C", "D").contains(scenario) && !variant.equals("standard"))) {
                throw new IllegalArgumentException("Invalid Issue #34 scenario, variant, or repetition");
            }
            return new Options(scenario, variant, repetition, output);
        }
    }

    public static void main(String[] args) {
        try { run(Options.parse(args)); }
        catch (Exception e) {
            System.err.println("ISSUE34_FAILURE=" + Code02Harness.category(e));
            System.exit(1);
        }
    }

    private static void run(Options options) throws Exception {
        Path repo = Path.of("").toAbsolutePath().normalize();
        Path jar = repo.resolve("server/target/canis-jar-with-dependencies.jar");
        Path classes = repo.resolve("server/target/issue34-classes");
        Path clientClasses = repo.resolve("client/target/classes");
        if (!Files.isRegularFile(jar) || !Files.isDirectory(classes) || !Files.isDirectory(clientClasses)) {
            throw new IOException("Build the server and research harness first");
        }
        String revision = Code02Harness.commandOutput(repo, "git", "rev-parse", "HEAD").trim();
        boolean dirty = !Code02Harness.commandOutput(repo, "git", "status", "--porcelain").isBlank();
        String runId = "issue34-" + revision.substring(0, 12) + "-" + options.scenario + "-" + options.variant
                + "-r" + options.repetition + "-" + Code02Harness.randomHex(4);
        Path output = (options.output == null
                ? repo.resolve("server/target/issue34/" + options.scenario + "-" + options.variant + "-r"
                        + options.repetition + ".json") : repo.resolve(options.output)).normalize();
        if (!output.startsWith(repo.resolve("server/target/issue34")) || Files.exists(output)
                || !output.getFileName().toString().endsWith(".json")) {
            throw new IOException("Output must be a new JSON path under server/target/issue34");
        }
        Path root = Files.createTempDirectory("canis-issue34-").toRealPath();
        Path privateDir = Code02Harness.secureDirectory(root.resolve("private"));
        Path serverDir = Code02Harness.secureDirectory(root.resolve("server"));
        Path sharedDir = Code02Harness.secureDirectory(root.resolve("shared"));
        Path workerDir = Code02Harness.secureDirectory(root.resolve("worker"));
        Files.writeString(root.resolve(".code02-run"), runId, StandardCharsets.US_ASCII);
        List<String> rows = new ArrayList<>();
        List<String> workerLabels = new ArrayList<>();
        List<Process> workers = new ArrayList<>();
        Instant started = Instant.now();
        String status = "failed", failure = "none", beforeDbHash = "", afterRestoreHash = "";
        String beforeAuditHash = "", afterAuditHash = "";
        boolean baselineVerified = false, postFaultEncrypted = false, postFaultDecrypted = false;
        boolean rejectedOutputPresent = false, restartReady = false, restored = false, logChangeDetected = false;
        int startupExit = -1;
        long firstPid = -1, secondPid = -1;
        Process server = null;
        try {
            String storePassword = Code02Harness.randomHex(24);
            String recipientPassword = Code02Harness.randomHex(24);
            String producerPassword = Code02Harness.randomHex(24);
            Path master = privateDir.resolve("master.key");
            Path db = serverDir.resolve("db.dat");
            Code02Harness.provisionMasterKey(master);
            Code02Harness.provisionTls(privateDir, storePassword, List.of("producer-a", "recipient-a"));
            int port;
            try (ServerSocket reservation = new ServerSocket(0)) { port = reservation.getLocalPort(); }
            Map<String, String> serverEnv = new HashMap<>();
            serverEnv.put("CANIS_PORT", Integer.toString(port));
            serverEnv.put("CANIS_USERNAME", "recipient-a");
            serverEnv.put("CANIS_PASSWORD", recipientPassword);
            serverEnv.put("CANIS_SERVICE_CREDENTIALS", "producer-a:" + producerPassword);
            serverEnv.put("CANIS_SECRET_KEY", master.toString());
            serverEnv.put("CANIS_TLS_KEYSTORE", privateDir.resolve("server.p12").toString());
            serverEnv.put("CANIS_TLS_KEYSTORE_PASSWORD", storePassword);
            serverEnv.put("CANIS_TLS_TRUSTSTORE", privateDir.resolve("server-trust.p12").toString());
            serverEnv.put("CANIS_TLS_TRUSTSTORE_PASSWORD", storePassword);
            Map<String, String> producerEnv = Code02Harness.clientEnvironment(privateDir, port, storePassword,
                    "producer-a", producerPassword);
            Map<String, String> recipientEnv = Code02Harness.clientEnvironment(privateDir, port, storePassword,
                    "recipient-a", recipientPassword);
            List<String> serverCommand = List.of(Code02Harness.javaExecutable("java"), "-Xms128m", "-Xmx512m",
                    "-jar", jar.toString());
            Process init = Code02Harness.start(Code02Harness.serverCommandWithInit(serverCommand), serverDir,
                    serverEnv, privateDir.resolve("init.log"));
            await(init);
            if (init.exitValue() != 0 || !Files.isRegularFile(db)) throw new IOException("Fixture initialization failed");
            row(rows, "setup", "init-db", init.exitValue(), "initialized", "none", false,
                    marker(privateDir.resolve("init.log")));
            server = Code02Harness.start(serverCommand, serverDir, serverEnv, privateDir.resolve("server-1.log"));
            firstPid = server.pid();
            Code02Harness.awaitServer(recipientEnv);
            row(rows, "baseline", "startup", -1, "ready", "none", false,
                    marker(privateDir.resolve("server-1.log")));
            try (JpawClient client = new JpawClient(Code02Harness.clientConfiguration(recipientEnv))) {
                if (!"OK".equals(client.set("recipient-a"))) throw new IOException("Key creation failed");
            }
            List<String> workerBase = List.of(Code02Harness.javaExecutable("java"), "-Xms128m", "-Xmx512m", "-cp",
                    classes + System.getProperty("path.separator") + clientClasses, "Issue33Harness", "worker");
            Attempt baselineEncrypt = worker(workerBase, "producer", root, runId, "baseline-encrypt", 0,
                    producerEnv, workerDir, privateDir, workers, workerLabels);
            recordAttempt(rows, "baseline", "encrypt", baselineEncrypt, privateDir, 0);
            Attempt baselineDecrypt = worker(workerBase, "consumer", root, runId, "baseline-decrypt", 0,
                    recipientEnv, workerDir, privateDir, workers, workerLabels);
            recordAttempt(rows, "baseline", "decrypt", baselineDecrypt, privateDir, 0);
            baselineVerified = baselineEncrypt.outcome.equals("published") && baselineDecrypt.outcome.equals("verified");
            if (!baselineVerified || baselineEncrypt.exit != 0 || baselineDecrypt.exit != 0) {
                throw new IOException("Baseline exchange failed");
            }
            beforeDbHash = hash(db);
            Files.copy(db, privateDir.resolve("db.backup"));
            Files.copy(master, privateDir.resolve("master.backup"));
            if (!Files.isRegularFile(sharedDir.resolve(runId + "-0.canis"))) {
                throw new IOException("Baseline envelope missing");
            }

            switch (options.scenario) {
                case "A", "B" -> {
                    if (options.scenario.equals("A")) Files.delete(master);
                    else {
                        Code02Harness.provisionMasterKey(privateDir.resolve("replacement.key"));
                        Files.move(privateDir.resolve("replacement.key"), master, StandardCopyOption.REPLACE_EXISTING);
                    }
                    row(rows, "fault", options.scenario.equals("A") ? "remove-master" : "replace-master", -1,
                            "injected", "none", false, "none");
                    Attempt liveEncrypt = worker(workerBase, "producer", root, runId, "live-encrypt", 1,
                            producerEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "live", "encrypt", liveEncrypt, privateDir, 1);
                    Attempt liveDecrypt = worker(workerBase, "consumer", root, runId, "live-decrypt", 0,
                            recipientEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "live", "decrypt", liveDecrypt, privateDir, 0);
                    postFaultEncrypted = liveEncrypt.outcome.equals("published");
                    postFaultDecrypted = liveDecrypt.outcome.equals("verified");
                    row(rows, "live", "server-log", -1, "observed", "none", false,
                            marker(privateDir.resolve("server-1.log")));
                    stop(server);
                    server = Code02Harness.start(serverCommand, serverDir, serverEnv, privateDir.resolve("server-2.log"));
                    secondPid = server.pid();
                    startupExit = await(server);
                    restartReady = canConnect(recipientEnv);
                    row(rows, "faulted_restart", "startup", startupExit, restartReady ? "ready" : "rejected",
                            "none", false, marker(privateDir.resolve("server-2.log")));
                    Attempt deniedEncrypt = worker(workerBase, "producer", root, runId, "rejected-encrypt", 2,
                            producerEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "faulted_restart", "encrypt", deniedEncrypt, privateDir, 2);
                    Attempt deniedDecrypt = worker(workerBase, "consumer", root, runId, "rejected-decrypt", 0,
                            recipientEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "faulted_restart", "decrypt", deniedDecrypt, privateDir, 0);
                    rejectedOutputPresent = Files.exists(privateDir.resolve("recovered-rejected-decrypt-0.bin"));
                    if (restartReady || deniedDecrypt.exit == 0 || rejectedOutputPresent) {
                        throw new IOException("Faulted restart unexpectedly served recovery");
                    }
                    Files.copy(privateDir.resolve("master.backup"), master, StandardCopyOption.REPLACE_EXISTING);
                    Code02Harness.restrict(master, false);
                    server = startReady(serverCommand, serverDir, serverEnv, privateDir.resolve("server-3.log"),
                            recipientEnv);
                    row(rows, "recovery", "startup", -1, "ready", "none", false,
                            marker(privateDir.resolve("server-3.log")));
                    restored = verifyRestored(workerBase, root, runId, recipientEnv, workerDir, privateDir,
                            workers, workerLabels, rows);
                    afterRestoreHash = hash(db);
                    if (!restored) throw new IOException("Restored key did not recover envelope");
                }
                case "C", "E" -> {
                    if (options.scenario.equals("E") || options.variant.equals("stopped")) stop(server);
                    byte[] damaged = Files.readAllBytes(db);
                    damaged[damaged.length - 1] ^= 1;
                    Files.write(db, damaged, StandardOpenOption.TRUNCATE_EXISTING);
                    java.util.Arrays.fill(damaged, (byte) 0);
                    row(rows, "fault", "flip-database-final-byte", -1, "injected", "none", false, "none");
                    if (options.scenario.equals("C") && options.variant.equals("live")) {
                        Attempt liveEncrypt = worker(workerBase, "producer", root, runId, "live-encrypt", 1,
                                producerEnv, workerDir, privateDir, workers, workerLabels);
                        recordAttempt(rows, "live", "encrypt", liveEncrypt, privateDir, 1);
                        Attempt liveDecrypt = worker(workerBase, "consumer", root, runId, "live-decrypt", 0,
                                recipientEnv, workerDir, privateDir, workers, workerLabels);
                        recordAttempt(rows, "live", "decrypt", liveDecrypt, privateDir, 0);
                        postFaultEncrypted = liveEncrypt.outcome.equals("published");
                        postFaultDecrypted = liveDecrypt.outcome.equals("verified");
                        row(rows, "live", "server-log", -1, "observed", "none", false,
                                marker(privateDir.resolve("server-1.log")));
                        stop(server);
                    }
                    server = Code02Harness.start(serverCommand, serverDir, serverEnv, privateDir.resolve("server-2.log"));
                    secondPid = server.pid();
                    startupExit = await(server);
                    restartReady = canConnect(recipientEnv);
                    row(rows, "faulted_restart", "startup", startupExit, restartReady ? "ready" : "rejected",
                            "none", false, marker(privateDir.resolve("server-2.log")));
                    Attempt deniedEncrypt = worker(workerBase, "producer", root, runId, "rejected-encrypt", 2,
                            producerEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "faulted_restart", "encrypt", deniedEncrypt, privateDir, 2);
                    Attempt deniedDecrypt = worker(workerBase, "consumer", root, runId, "rejected-decrypt", 0,
                            recipientEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "faulted_restart", "decrypt", deniedDecrypt, privateDir, 0);
                    rejectedOutputPresent = Files.exists(privateDir.resolve("recovered-rejected-decrypt-0.bin"));
                    if (restartReady || deniedDecrypt.exit == 0 || rejectedOutputPresent) {
                        throw new IOException("Corrupt database unexpectedly served recovery");
                    }
                    Files.copy(privateDir.resolve("db.backup"), db, StandardCopyOption.REPLACE_EXISTING);
                    server = startReady(serverCommand, serverDir, serverEnv, privateDir.resolve("server-3.log"),
                            recipientEnv);
                    row(rows, "recovery", "startup", -1, "ready", "none", false,
                            marker(privateDir.resolve("server-3.log")));
                    restored = verifyRestored(workerBase, root, runId, recipientEnv, workerDir, privateDir,
                            workers, workerLabels, rows);
                    afterRestoreHash = hash(db);
                    if (!restored) throw new IOException("Restored database did not recover envelope");
                }
                case "D" -> {
                    stop(server);
                    Path log = serverDir.resolve("logs/application.log");
                    String logText = Files.readString(log, StandardCharsets.UTF_8);
                    List<String> logLines = new ArrayList<>(logText.lines().toList());
                    int index = -1;
                    for (int i = 0; i < logLines.size(); i++) {
                        if (logLines.get(i).contains("io.canis.audit")
                                && logLines.get(i).contains("event=decrypt_success")) index = i;
                    }
                    if (index < 0) throw new IOException("Baseline audit record absent");
                    beforeAuditHash = hash(log);
                    if (options.variant.equals("modified")) {
                        logLines.set(index, logLines.get(index).replace("event=decrypt_success", "event=decrypt_failure"));
                    } else logLines.remove(index);
                    Files.writeString(log, String.join("\n", logLines) + "\n", StandardCharsets.UTF_8,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    afterAuditHash = hash(log);
                    logChangeDetected = !beforeAuditHash.equals(afterAuditHash);
                    row(rows, "fault", "audit-" + options.variant, -1, logChangeDetected ? "changed" : "unchanged",
                            "none", false, "external_hash_mismatch");
                    server = startReady(serverCommand, serverDir, serverEnv, privateDir.resolve("server-2.log"),
                            recipientEnv);
                    secondPid = server.pid();
                    restartReady = true;
                    row(rows, "faulted_restart", "startup", -1, "ready", "none", false,
                            marker(privateDir.resolve("server-2.log")));
                    Attempt encrypt = worker(workerBase, "producer", root, runId, "postfault-encrypt", 1,
                            producerEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "postfault", "encrypt", encrypt, privateDir, 1);
                    Attempt decrypt = worker(workerBase, "consumer", root, runId, "postfault-decrypt", 0,
                            recipientEnv, workerDir, privateDir, workers, workerLabels);
                    recordAttempt(rows, "postfault", "decrypt", decrypt, privateDir, 0);
                    postFaultEncrypted = encrypt.outcome.equals("published");
                    postFaultDecrypted = decrypt.outcome.equals("verified");
                    restored = postFaultEncrypted && postFaultDecrypted;
                    afterRestoreHash = hash(db);
                    if (!logChangeDetected || !restored) throw new IOException("Audit fault control failed");
                }
                default -> throw new IOException("Unknown scenario");
            }
            if (!beforeDbHash.equals(afterRestoreHash)) throw new IOException("Database restore hash differs");
            status = "passed";
        } catch (Exception e) {
            failure = Code02Harness.category(e);
            row(rows, "harness", "failure", -1, "failed", failure, false, "none");
        } finally {
            for (Process worker : workers) if (worker.isAlive()) worker.destroyForcibly();
            if (server != null && server.isAlive()) stop(server);
            Files.createDirectories(output.getParent());
            String stem = output.getFileName().toString().replaceFirst("\\.json$", "");
            Path csv = output.resolveSibling(stem + ".events.csv");
            Files.writeString(csv, CSV_HEADER + String.join("", rows), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
            for (String label : workerLabels) {
                Path source = privateDir.resolve(label + ".csv");
                if (Files.isRegularFile(source)) Files.copy(source, output.resolveSibling(stem + "." + label + ".csv"));
            }
            if (status.equals("passed")) {
                try { Code02Harness.deleteRunRoot(root, runId); }
                catch (IOException e) { status = "failed"; failure = "fixture_cleanup_failed"; }
            }
            String json = "{\n" +
                    "  \"schema\": \"issue34-v1\",\n" +
                    "  \"scenario\": \"" + options.scenario + "\",\n" +
                    "  \"variant\": \"" + options.variant + "\",\n" +
                    "  \"repetition\": " + options.repetition + ",\n" +
                    "  \"run_id\": \"" + runId + "\",\n" +
                    "  \"revision\": \"" + revision + "\",\n" +
                    "  \"dirty_tree\": " + dirty + ",\n" +
                    "  \"harness_sha256\": \"" + hash(repo.resolve("docs/research/Issue34Harness.java")) + "\",\n" +
                    "  \"server_jar_sha256\": \"" + hash(jar) + "\",\n" +
                    "  \"os\": \"" + Code02Harness.json(System.getProperty("os.name") + " " + System.getProperty("os.version")) + "\",\n" +
                    "  \"jdk\": \"" + Code02Harness.json(System.getProperty("java.runtime.version")) + "\",\n" +
                    "  \"started_utc\": \"" + started + "\",\n" +
                    "  \"ended_utc\": \"" + Instant.now() + "\",\n" +
                    "  \"first_server_pid\": " + firstPid + ",\n" +
                    "  \"second_server_pid\": " + secondPid + ",\n" +
                    "  \"faulted_startup_exit\": " + startupExit + ",\n" +
                    "  \"faulted_restart_ready\": " + restartReady + ",\n" +
                    "  \"baseline_verified\": " + baselineVerified + ",\n" +
                    "  \"post_fault_encrypted\": " + postFaultEncrypted + ",\n" +
                    "  \"post_fault_decrypted\": " + postFaultDecrypted + ",\n" +
                    "  \"rejected_output_present\": " + rejectedOutputPresent + ",\n" +
                    "  \"restored_and_verified\": " + restored + ",\n" +
                    "  \"database_sha256_before\": \"" + beforeDbHash + "\",\n" +
                    "  \"database_sha256_after_restore\": \"" + afterRestoreHash + "\",\n" +
                    "  \"audit_sha256_before\": \"" + beforeAuditHash + "\",\n" +
                    "  \"audit_sha256_after_fault\": \"" + afterAuditHash + "\",\n" +
                    "  \"external_audit_change_detected\": " + logChangeDetected + ",\n" +
                    "  \"status\": \"" + status + "\",\n" +
                    "  \"failure_category\": \"" + failure + "\"\n" +
                    "}\n";
            Files.writeString(output, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            System.out.println("ISSUE34_RESULT=" + status + " scenario=" + options.scenario + " variant="
                    + options.variant + " repetition=" + options.repetition);
            if (!status.equals("passed")) {
                System.err.println("ISSUE34_PRIVATE_EVIDENCE=" + root);
                throw new IllegalStateException(failure);
            }
        }
    }

    private record Attempt(int exit, String outcome, String category) {}

    private static Attempt worker(List<String> base, String kind, Path root, String runId, String label, int ordinal,
            Map<String, String> env, Path workerDir, Path privateDir, List<Process> workers,
            List<String> labels) throws Exception {
        List<String> command = new ArrayList<>(base);
        command.addAll(List.of(kind, root.toString(), runId, label, "recipient-a", Integer.toString(ordinal), "1", "normal"));
        Process process = Code02Harness.start(command, workerDir, env, privateDir.resolve(label + ".log"));
        workers.add(process);
        labels.add(label);
        int exit = await(process);
        List<String> lines = Files.readAllLines(privateDir.resolve(label + ".csv"), StandardCharsets.US_ASCII);
        if (lines.size() != 2) throw new IOException("Worker CSV malformed");
        String[] fields = lines.get(1).split(",", -1);
        if (fields.length != 10) throw new IOException("Worker CSV columns malformed");
        return new Attempt(exit, fields[6], fields[7]);
    }

    private static void recordAttempt(List<String> rows, String phase, String operation, Attempt a,
            Path privateDir, int ordinal) {
        String label = switch (phase + ":" + operation) {
            case "baseline:encrypt" -> "baseline-encrypt";
            case "baseline:decrypt" -> "baseline-decrypt";
            case "live:encrypt" -> "live-encrypt";
            case "live:decrypt" -> "live-decrypt";
            case "faulted_restart:encrypt" -> "rejected-encrypt";
            case "faulted_restart:decrypt" -> "rejected-decrypt";
            case "postfault:encrypt" -> "postfault-encrypt";
            case "postfault:decrypt" -> "postfault-decrypt";
            case "recovery:decrypt" -> "restored-decrypt";
            default -> "unknown";
        };
        boolean output = operation.equals("decrypt") && Files.exists(privateDir.resolve(
                "recovered-" + label + "-" + ordinal + ".bin"));
        row(rows, phase, operation, a.exit, a.outcome, a.category, output, "none");
    }

    private static boolean verifyRestored(List<String> base, Path root, String runId, Map<String, String> recipientEnv,
            Path workerDir, Path privateDir, List<Process> workers, List<String> labels, List<String> rows) throws Exception {
        Attempt a = worker(base, "consumer", root, runId, "restored-decrypt", 0, recipientEnv,
                workerDir, privateDir, workers, labels);
        recordAttempt(rows, "recovery", "decrypt", a, privateDir, 0);
        return a.exit == 0 && a.outcome.equals("verified");
    }

    private static Process startReady(List<String> command, Path dir, Map<String, String> env, Path log,
            Map<String, String> recipientEnv) throws Exception {
        Process p = Code02Harness.start(command, dir, env, log);
        Code02Harness.awaitServer(recipientEnv);
        return p;
    }

    private static boolean canConnect(Map<String, String> env) {
        try (JpawClient client = new JpawClient(Code02Harness.clientConfiguration(env))) {
            return "OK".equals(client.health());
        } catch (Exception e) { return false; }
    }

    private static int await(Process process) throws Exception {
        if (!process.waitFor(DEADLINE_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("Process timeout");
        }
        return process.exitValue();
    }

    private static void stop(Process process) throws Exception {
        if (process == null || !process.isAlive()) return;
        process.destroy();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
        if (process.isAlive()) throw new IOException("Server did not stop");
    }

    private static void row(List<String> rows, String phase, String operation, int exit, String outcome,
            String category, boolean output, String marker) {
        rows.add(phase + "," + Instant.now() + "," + operation + "," + exit + "," + outcome + ","
                + category + "," + output + "," + marker + "\n");
    }

    private static String marker(Path log) throws IOException {
        if (!Files.isRegularFile(log)) return "none";
        String content = Files.readString(log, StandardCharsets.UTF_8);
        if (content.contains("Invalid CANIS_SECRET_KEY configuration (missing_file)")) return "missing_file";
        if (content.contains("CANIS database startup rejected")) return "database_startup_rejected";
        if (content.contains("CANIS database entered recovery-required state: changed_database")) return "changed_database";
        if (content.contains("CANIS database entered recovery-required state")) return "database_recovery_required";
        if (content.contains("TLS server is listening")) return "tls_listener_started";
        return "none";
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
}
