package io.canis.models;

import java.util.Map;

public record Environment(String password, String username, int port,
    Map<String, String> serviceCredentials) {
}
