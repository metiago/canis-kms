package io.canis.jpaw.client;

import java.util.regex.Pattern;

final class ServiceNameValidator {

  static final String SERVICE_NAME_PATTERN = "[A-Za-z0-9._-]{1,128}";

  private static final Pattern VALID_SERVICE_NAME = Pattern.compile(SERVICE_NAME_PATTERN);

  private ServiceNameValidator() {
  }

  static String requireValid(String serviceName) {
    if (serviceName == null || !VALID_SERVICE_NAME.matcher(serviceName).matches()) {
      throw new IllegalArgumentException(
          "Invalid service name. Service name must match " + SERVICE_NAME_PATTERN + ".");
    }
    return serviceName;
  }
}
