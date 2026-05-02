package io.canis.protocol;

public enum ProtocolError {

  REQUEST_TOO_LARGE("REQUEST_TOO_LARGE", "Request too large"),
  INVALID_SERVICE_NAME("INVALID_SERVICE_NAME", "Invalid service name"),
  INVALID_INPUT_FORMAT("INVALID_INPUT_FORMAT", "Invalid input format"),
  KEY_NOT_FOUND("KEY_NOT_FOUND", "Key not found"),
  DECRYPTION_FAILED("DECRYPTION_FAILED", "Decryption failed");

  private final String code;
  private final String message;

  ProtocolError(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }

  public String wireMessage() {
    return "ERROR: " + message;
  }
}
