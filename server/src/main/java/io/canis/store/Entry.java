package io.canis.store;

/** Public metadata for a stored key. Private material never leaves the store through this type. */
public record Entry(String name, String publicKey) {}
