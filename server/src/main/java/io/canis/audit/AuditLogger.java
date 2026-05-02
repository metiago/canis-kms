package io.canis.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuditLogger {

  private static final Logger audit = LoggerFactory.getLogger("io.canis.audit");

  private AuditLogger() {
  }

  public static void authenticationSucceeded(String identity, Object remoteAddress) {
    audit.info("event=auth_success identity={} remote={}", identity, remoteAddress);
  }

  public static void authenticationFailed(String identity, Object remoteAddress, String reason) {
    audit.warn("event=auth_failure identity={} remote={} reason={}", identity, remoteAddress, reason);
  }

  public static void keyCreated(String identity, String key, Object remoteAddress) {
    audit.info("event=key_created identity={} key={} remote={}", identity, key, remoteAddress);
  }

  public static void keyAccessed(String identity, String key, Object remoteAddress) {
    audit.info("event=key_accessed identity={} key={} remote={}", identity, key, remoteAddress);
  }

  public static void keyListed(String identity, Object remoteAddress) {
    audit.info("event=key_listed identity={} remote={}", identity, remoteAddress);
  }

  public static void keyDeleted(String identity, String key, Object remoteAddress) {
    audit.info("event=key_deleted identity={} key={} remote={}", identity, key, remoteAddress);
  }

  public static void decryptSucceeded(String identity, String key, Object remoteAddress) {
    audit.info("event=decrypt_success identity={} key={} remote={}", identity, key, remoteAddress);
  }

  public static void decryptFailed(String identity, String key, Object remoteAddress, String reason) {
    audit.warn("event=decrypt_failure identity={} key={} remote={} reason={}",
        identity, key, remoteAddress, reason);
  }

  public static void commandRejected(String identity, Object remoteAddress, String reason) {
    audit.warn("event=command_rejected identity={} remote={} reason={}",
        identity, remoteAddress, reason);
  }
}
