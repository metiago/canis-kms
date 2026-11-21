# CANIS

## What CANIS Is

CANIS is a Java 21 key-recovery service and client for asynchronous encrypted-file exchange between Java services. A producer encrypts a file locally and publishes the encrypted file to shared storage.

A recipient can retrieve that file later and ask CANIS to recover its file key. File contents stay outside the key service, while private keys remain on the server. Admitted clients share access to stored key names; the trust boundary is described in the threat model.

## Quick Start

```sh
git clone https://github.com/metiago/canis-kms.git
cd canis-kms
mvn clean verify
```

Expect `BUILD SUCCESS` with no test failures. Follow the [Reviewer Guide](docs/reviewer-guide.md) for the runnable file-exchange example and platform-specific checks.

## Documentation

- [Reviewer Guide](docs/reviewer-guide.md)
- [Reviewer Quickstart](docs/reviewer-quickstart.md)
- [Threat Model](docs/threat-model.md)
- [CANISP Protocol](docs/canisp-protocol.md)
- [Master Key Guide](docs/master-key.md)
- [RSA Key Lifecycle](docs/rsa-key-lifecycle.md)
- [Java Client Package](docs/client-package.md)
- [Research Evidence Index](docs/research/README.md)

## Release

- Latest public release: [v0.1.0-rc.1](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1) (research prerelease).
- Paper: [joss-papers/paper.md](joss-papers/paper.md).
- Citation: Souza, T. (2026), *CANIS: Key Management for Asynchronous Encrypted File Exchange Between Java Services*, version 0.1.0-rc.1. The `canis-rc1` entry in [paper.bib](joss-papers/paper.bib) cites the released source archive.

## Security

Rotate certificates before expiry. Report suspected vulnerabilities privately as described in [SECURITY.md](SECURITY.md).

## License

MIT. See [LICENSE.txt](LICENSE.txt).
