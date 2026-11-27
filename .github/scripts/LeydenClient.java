import io.canis.jpaw.client.JpawClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Client-side driver kept alive across benchmark runs to avoid client JVM startup in the timings. */
public final class LeydenClient {
    private static final String MEASURED_KEY = "train-01";

    private LeydenClient() {}

    public static void main(String[] args) throws Exception {
        try (var commands = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                var replies = new PrintWriter(System.out, true, StandardCharsets.UTF_8)) {
            String command;
            while ((command = commands.readLine()) != null) {
                try {
                    switch (command) {
                        case "train" -> {
                            train();
                            replies.println("trained,40");
                        }
                        case "measure" -> measure(replies);
                        default -> throw new IllegalArgumentException("Unknown benchmark command");
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    replies.println("error");
                }
            }
        }
    }

    private static void train() throws Exception {
        for (int i = 1; i <= 20; i++) {
            String key = String.format(Locale.ROOT, "train-%02d", i);
            try (var client = new JpawClient()) {
                client.set(key);
            }
            try (var client = new JpawClient()) {
                requireKey(client, key);
            }
        }
    }

    private static void measure(PrintWriter replies) throws Exception {
        long start = System.nanoTime();
        try (var client = new JpawClient()) {
            requireKey(client, MEASURED_KEY);
            long firstDone = System.nanoTime();
            for (int i = 0; i < 50; i++) {
                requireKey(client, MEASURED_KEY);
            }
            long batchDone = System.nanoTime();
            replies.printf(
                    Locale.ROOT,
                    "measure,%.3f,%.3f%n",
                    (firstDone - start) / 1_000_000.0,
                    (batchDone - firstDone) / 1_000_000.0);
        }
    }

    private static void requireKey(JpawClient client, String key) throws Exception {
        if (client.get(key) == null) {
            throw new IllegalStateException("Benchmark key missing: " + key);
        }
    }
}
