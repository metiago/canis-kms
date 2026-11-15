# TLS security test suite

Issue #4 verifies that TLS failures stop CANISP communication. The official client uses JSSE
TLS 1.3 with PKIX server validation and HTTPS endpoint identification. The server uses JSSE TLS 1.3
with required client certificates. Tests exercise these production paths; no alternate transport or
trust policy is installed in production.

| Scenario | Client coverage | Server coverage |
| --- | --- | --- |
| Expired certificate | `SocketClientTest` rejects an expired server leaf before login | `ServerTlsCertificateTest` rejects an expired client leaf without a CANISP response |
| Invalid certificate | `SocketClientTest` rejects a server leaf with client-only extended key usage and a malformed trust store | `ServerTlsCertificateTest` rejects a client leaf with server-only extended key usage; `ServerTlsSecurityTest` rejects a malformed trust store |
| Untrusted certificate | `SocketClientTest` rejects an untrusted server and an untrusted client | `ServerTlsSecurityTest` rejects an untrusted client |
| Wrong hostname | `SocketClientTest` rejects a trusted server whose SAN does not match the connection address | The server has no outbound hostname to validate |
| Downgrade to TLS 1.2 | `SocketClientTest` rejects a TLS 1.2-only peer | `ServerTlsSecurityTest` rejects a TLS 1.2-only client |
| Plaintext | `SocketClientTest` sends a TLS ClientHello to a plaintext endpoint and fails without login fallback | `ServerTlsSecurityTest` rejects a plaintext login before CANISP processing |

The certificate helpers use JDK `keytool` to generate temporary PKCS#12 identities, a currently
valid CA, and CA-signed test leaves. Expired and wrong-purpose leaves are otherwise trusted, so the
failure is attributable to the tested certificate property. The helpers verify each leaf's
validity period, extended key usage, and CA signature before using it. A valid CA-signed server
authenticates in the client suite, and a valid CA-signed client authenticates in the server suite,
providing positive controls. Test-only JSSE peers can present expired
leaves; production context factories reject expired local identities before connection setup.

No certificate, private key, store password, or trust override is checked into the repository.
The fixtures use temporary directories and generated passwords. Tests use bounded socket waits and
assert rejection without relying on provider-specific exception messages or TLS alert codes.
They also retain the existing invalid-credential, unauthorized-command, and database-tampering
tests in the server security suite.

Run the full suite with `mvn test`. For focused runs, use:

```text
mvn -pl client -Dtest=SocketClientTest test
mvn -pl server -am -Dtest=ServerTlsCertificateTest -Dsurefire.failIfNoSpecifiedTests=false test
```

The server command builds its reactor dependency too.

This issue changes test fixtures and documentation only. Runtime configuration, CANISP framing,
stored data, and deployment requirements are unchanged. The fixtures depend on the JDK `keytool`
and a JSSE provider that supports the approved TLS 1.3 suites. A future improvement is a shared
test fixture module to remove duplicate CA issuance helpers from the client and server tests.
