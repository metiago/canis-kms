# CANIS

## What CANIS Is

CANIS is a Java 21 key-recovery service and client for asynchronous encrypted-file exchange between Java services. A producer encrypts a file locally and publishes the encrypted file to shared storage.

CANIS is intended for organization-managed corporate environments with trusted host
administrators, deployment automation, service accounts, and identity systems. It is not a
public cloud KMS, multi-tenant service, HSM, or Vault as a Service. See the
[threat model](docs/threat-model.md) before deployment.

A recipient can retrieve that file later and ask CANIS to recover its file key. File contents stay outside the key service, while private keys remain on the server. Admitted clients share access to stored key names; the trust boundary is described in the threat model.

## Quick Start

```sh
git clone https://github.com/metiago/canis-kms.git
cd canis-kms
mvn clean verify
```

Expect `BUILD SUCCESS` with no test failures. The [setup guide](docs/reviewer-guide.md) describes the file-exchange tests and platform checks.

## Documentation

- [Setup and Test Guide](docs/reviewer-guide.md)
- [Threat Model](docs/threat-model.md)
- [CANISP Protocol](docs/canisp-protocol.md)
- [Master Key Guide](docs/master-key.md)
- [Operational Recovery Runbook](docs/operational-recovery.md)
- [RSA Key Lifecycle](docs/rsa-key-lifecycle.md)
- [Java Client Package](docs/client-package.md)

Repository layout: `docs/` contains product documentation and `tools/ci/` contains CI checks and fixture setup.

## Release

- [GitHub releases](https://github.com/metiago/canis-kms/releases)

## Security

CANIS is an internal service on organization-managed infrastructure. The operating system,
infrastructure administrators, deployment automation, and service account are trusted. Its
checks address accidental disclosure, unsafe configuration, deployment mistakes, and
unauthorized non-privileged access. CANIS does not provide Vault, cloud KMS, HSM, or
multi-tenant security guarantees.

### Client Admission and Shared Key Access

CANIS operates within a shared trust domain. Any authenticated CANIS client may request
operations involving any stored service key. Service names and key names are identifiers, not
authorization boundaries. Different TLS certificates do not isolate clients, and different
usernames do not grant different authorization scopes. CANIS does not provide per-key
authorization or client isolation. If applications require independent authorization domains,
separate CANIS deployments should be used. See the [threat model](docs/threat-model.md).

Use a dedicated service account, protected master-key and database paths, corporate PKI, and
restricted network access. Master-key file checks detect unsafe configuration and fail closed;
they do not defend against a privileged host administrator or compromised operating system.
Rotate certificates before expiry. See the [master-key guide](docs/master-key.md) for exact
loader checks and deployment responsibilities. Report suspected vulnerabilities privately as
described in [SECURITY.md](SECURITY.md).
For database or master-key recovery, follow the [operational recovery runbook](docs/operational-recovery.md).

## License

MIT. See [LICENSE.txt](LICENSE.txt).
