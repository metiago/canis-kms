package io.canis.jpaw;

import io.canis.jpaw.client.JpawClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** A small console for trying the client against a separately running CANIS server. */
public final class JpawConsole {

    private JpawConsole() {}

    public static void main(String[] args) throws IOException {
        try (JpawClient client = new JpawClient()) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("Connected to CANIS. Type help for commands.");

            while (true) {
                System.out.print("canis> ");
                String line = input.readLine();
                if (line == null) {
                    break;
                }

                String[] command = line.trim().split("\\s+", 2);
                String action = command[0].toLowerCase(Locale.ROOT);
                if (action.equals("quit") || action.equals("exit")) {
                    break;
                }

                try {
                    switch (action) {
                        case "help" ->
                            System.out.println(
                                    "health, version, list, set NAME, get NAME, public NAME, delete NAME, quit");
                        case "health" -> System.out.println(client.health());
                        case "version" -> System.out.println(client.protocolVersion());
                        case "list" -> client.list().forEach(entry -> System.out.println(entry.get("name")));
                        case "set" -> System.out.println(client.set(name(command)));
                        case "get" -> System.out.println(client.get(name(command)));
                        case "public" -> System.out.println(client.getPublicKey(name(command)));
                        case "delete" -> System.out.println(client.delete(name(command)));
                        default -> System.out.println("Unknown command. Type help for commands.");
                    }
                } catch (IOException | IllegalArgumentException e) {
                    System.out.println("Command failed: " + e.getMessage());
                }
            }
        }
    }

    private static String name(String[] command) {
        if (command.length < 2 || command[1].isBlank()) {
            throw new IllegalArgumentException("Enter a service name, for example: set demo");
        }
        return command[1];
    }
}
