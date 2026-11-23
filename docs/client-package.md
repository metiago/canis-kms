# CANIS Java client package

## Overview

The CANIS Java client is the supported application-facing API for Java applications that use the
CANIS key-recovery service. Use `JpawClient` for CANISP commands, response parsing, socket
limits, service-name validation, and file envelopes.

Package: <https://github.com/metiago/canis-client/packages/2344030>

## Dependency

Use the published Maven artifact:

```xml
<dependency>
  <groupId>io.canis</groupId>
  <artifactId>client</artifactId>
  <version>1.0.8</version>
</dependency>
```

## Configuration

The default `JpawClient` constructor reads these environment variables:

```sh
export CANIS_SERVER_HOST=localhost
export CANIS_SERVER_PORT=3307
export CANIS_USERNAME=admin
export CANIS_PASSWORD=your-password
export CANIS_CLIENT_TLS_KEYSTORE=/secure/path/client-identity.p12
export CANIS_CLIENT_TLS_KEYSTORE_PASSWORD=provided-by-secret-manager
export CANIS_CLIENT_TLS_TRUSTSTORE=/secure/path/canis-servers.p12
export CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD=provided-by-secret-manager
```

`CANIS_SERVER_HOST` defaults to `localhost`. The PKCS#12 identity store must contain one valid
client private key and certificate chain. The truststore must contain the server certificate or
its issuing CA. The client checks the configured host against the server certificate, requires
TLS 1.3, and has no plaintext fallback.

Service names must match `[A-Za-z0-9._-]{1,128}`. For example, `serviceA`, `service-a`,
`service_a`, `service.a`, and `svc123` are valid; spaces, tabs, newlines, `|`, and `:` are not.
Client methods throw `IllegalArgumentException` for an invalid name before sending a socket
command.

## Example

```java
import io.canis.jpaw.client.JpawClient;
import java.io.File;

public class Example {
  public static void main(String[] args) throws Exception {
    File inputFile = new File("payload.txt");
    File encryptedFile = new File("payload.canis");
    File decryptedFile = new File("payload.dec.txt");

    try (JpawClient client = new JpawClient()) {
      client.set("serviceA");
      client.encryptFile("serviceA", inputFile, encryptedFile);
      client.decryptFile("serviceA", encryptedFile, decryptedFile);
    }
  }
}
```

## Security notes

### Client Admission and Shared Key Access

CANIS operates within a shared trust domain. Any authenticated client may request operations
involving any stored service key. Service names and key names identify keys; they do not create
authorization boundaries. Different TLS certificates do not isolate clients, and different
usernames do not imply authorization scopes. CANIS provides no per-key authorization or client
isolation. If applications require independent authorization domains, separate CANIS deployments
should be used. See the [threat model](threat-model.md).

The client fetches public keys to encrypt files locally and asks CANIS to perform private-key
operations on the server. RSA private keys are not returned. Protect identity and trust stores
with operating-system permissions, supply passwords through a protected environment or secret
manager, and do not commit these secrets.
Use the client only with a CANIS deployment administered within the organization's trusted
environment. A valid admitted client can request recovery under any stored key name; the
[threat model](threat-model.md) defines this shared-access boundary and the trusted host and
identity systems.

The client offers only `TLS_AES_128_GCM_SHA256`, `TLS_AES_256_GCM_SHA384`, and
`TLS_CHACHA20_POLY1305_SHA256`, limited to suites enabled by its Java TLS provider.
Provider-disabled suites stay disabled. If no approved suite is enabled, configuration fails
with a sanitized error; if the server shares none, the handshake fails. PKIX chain validation
and hostname verification are mandatory. Environment variables cannot bypass validation or
override the cipher-suite policy.
