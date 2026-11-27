package io.canis.handlers;

public sealed interface Command {

    String CANISP_VERSION = "CANISP/1";
    String LOGIN = "|login";
    String VERSION = "|version";
    String HEALTH = "|health";
    String ADD = "|set ";
    String LIST = "|list";
    String GET_PUBLIC = "|get-public ";
    String GET = "|get";
    String DELETE = "|del ";
    String DECRYPT = "|decrypt ";
    String INVALID_COMMAND = "Unknown command";
    String OK_COMMAND = "OK";

    record Version() implements Command {}

    record Health() implements Command {}

    record ListKeys() implements Command {}

    record Add(String serviceName) implements Command {}

    record GetPublic(String serviceName) implements Command {}

    record Get(String serviceName) implements Command {}

    record Decrypt(String payload) implements Command {}

    record Delete(String serviceName) implements Command {}

    record Unknown() implements Command {}

    static Command parse(String input) {
        return switch (input) {
            case VERSION -> new Version();
            case HEALTH -> new Health();
            case LIST -> new ListKeys();
            case String s when s.startsWith(ADD) -> new Add(extractArg(s, ADD));
            case String s when s.startsWith(GET_PUBLIC) -> new GetPublic(extractArg(s, GET_PUBLIC));
            case String s when s.startsWith(GET) -> new Get(extractArg(s, GET));
            case String s when s.startsWith(DECRYPT) -> new Decrypt(extractArg(s, DECRYPT));
            case String s when s.startsWith(DELETE) -> new Delete(extractArg(s, DELETE));
            case null, default -> new Unknown();
        };
    }

    private static String extractArg(String input, String prefix) {
        return input.substring(prefix.length()).trim();
    }
}
