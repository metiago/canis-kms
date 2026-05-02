# CANIS Java Client Package

The CANIS Java client is the supported application-facing API for Java applications that need to
use CANIS KMS.

Package page:

```text
https://github.com/metiago/canis-client/packages/2344030
```

## Integration Contract

Application developers should use `JpawClient` instead of constructing raw CANISP socket messages.
CANISP is an internal client/server protocol, similar to the wire protocol beneath a database
driver. It is documented for maintainers, debugging, research, and PoCs, but it is not the supported
application integration API.

The client owns command construction, response parsing, socket limits, service-name validation, and
file envelope helpers.

## Maven Dependency

Use the currently published client artifact coordinates:

```xml
<dependency>
  <groupId>io.canis</groupId>
  <artifactId>client</artifactId>
  <version>1.0.8</version>
</dependency>
```

## Configuration

The default `JpawClient` constructor reads connection settings from environment variables:

```bash
export CANIS_SERVER_HOST=localhost
export CANIS_SERVER_PORT=3307
export CANIS_USERNAME=admin
export CANIS_PASSWORD=your-password
```

`CANIS_SERVER_HOST` is optional and defaults to `localhost`.

## Service Names

Service names are identifiers, not arbitrary free-form text. Client methods that receive a service
name validate it before sending any command to CANIS.

Accepted format:

```text
[A-Za-z0-9._-]{1,128}
```

Valid examples:

```text
serviceA
service-a
service_a
service.a
svc123
```

Invalid examples:

```text
service A
service\tA
service\nA
service|A
service:A
```

The client rejects invalid service names with `IllegalArgumentException`. Invalid names are rejected
before a socket command is sent.

## Basic Usage

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

Private keys stay on the CANIS server. The client fetches public keys for encryption and asks CANIS
to perform private-key operations server-side.
