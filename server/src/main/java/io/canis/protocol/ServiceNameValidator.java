package io.canis.protocol;

import java.util.regex.Pattern;

public final class ServiceNameValidator {

  public static final String SERVICE_NAME_PATTERN = "[A-Za-z0-9._-]{1,128}";

  private static final Pattern VALID_SERVICE_NAME = Pattern.compile(SERVICE_NAME_PATTERN);

  private ServiceNameValidator() {
  }

  public static boolean isValid(String serviceName) {
    return serviceName != null && VALID_SERVICE_NAME.matcher(serviceName).matches();
  }
}
