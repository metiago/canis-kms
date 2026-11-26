# CANIS

CANIS is a key-recovery service and client 
for asynchronous encrypted-file exchange between Java services. A producer encrypts a file locally and publishes 
the encrypted file to shared storage.

## Quick Start

```sh
git clone https://github.com/metiago/canis-kms.git
cd canis-kms
mvn clean verify
```

## Documentation

- [Threat Model](docs/threat-model.md)
- [CANISP Protocol](docs/canisp-protocol.md)
- [Master Key Guide](docs/master-key.md)
- [Operational Recovery Runbook](docs/operational-recovery.md)
- [Java Client Package](docs/client-package.md)

Repository layout: `docs/` contains product documentation and `tools/ci/` contains CI checks and fixture setup.

## Release

- [GitHub releases](https://github.com/metiago/canis-kms/releases)

## License

MIT. See [LICENSE.txt](LICENSE.txt).
