# TLS security tests

The client and server tests verify that TLS failures stop CANISP before login or command
processing. The official client uses JSSE TLS 1.3, PKIX certificate validation, and HTTPS
hostname verification. The server uses JSSE TLS 1.3 and requires client certificates. Production
uses these paths without an alternate transport or trust policy.

## Coverage

- `SocketClientTest` rejects expired, untrusted, or client-only server certificates; a trusted
  server with a hostname mismatch; malformed trust stores; TLS 1.2-only peers; and plaintext
  endpoints. It sends a TLS ClientHello to plaintext endpoints and does not fall back to login.
  It also rejects an untrusted client identity.
- `ServerTlsCertificateTest` rejects expired and server-only client certificates before a CANISP
  response.
- `ServerTlsSecurityTest` rejects untrusted clients, malformed trust stores, TLS 1.2-only
  clients, and plaintext login before CANISP processing. The server has no outbound hostname to
  validate.

The suites include successful CA-signed server and client connections as positive controls. JDK
`keytool` helpers generate a valid CA and temporary PKCS#12 identities. Expired and
wrong-purpose leaves are otherwise trusted; helpers verify their validity periods, extended key
usage, and CA signatures before the tests. Test-only JSSE peers can present expired leaves,
while production context factories reject expired local identities before connection setup.

Fixtures use temporary directories and generated passwords. No certificate, private key, store
password, or trust override is checked in. Bounded socket waits and rejection assertions avoid
provider-specific exception messages and TLS alert codes. The server security suite also retains
invalid-credential, unauthorized-command, and database-tampering tests.

## Run

Run all tests with `mvn test`, or focus on the TLS suites:

```sh
mvn -pl client -Dtest=SocketClientTest test
mvn -pl server -am -Dtest=ServerTlsCertificateTest -Dsurefire.failIfNoSpecifiedTests=false test
```

The server command also builds its reactor dependency. The fixtures require JDK `keytool` and a
JSSE provider supporting the approved TLS 1.3 suites. These tests and fixtures do not change
runtime configuration, CANISP framing, stored data, or deployment requirements. A shared fixture
module could remove duplicate CA issuance helpers from the client and server tests.
