# CANIS threat model

CANIS is a key-recovery service for locally encrypted files. The official Java client fetches
public keys, encrypts files outside CANIS, and asks the server to unwrap file keys. CANIS stores
RSA private keys and key metadata; shared file storage does not need access to plaintext.

## Security objectives and deployment scope

CANIS is intended for services inside a corporate environment operated by the organization.
It is not a public cloud KMS, a multi-tenant key service, an HSM, or Vault as a Service. Its
controls aim to prevent accidental disclosure of cryptographic material, catch unsafe deployment
and configuration mistakes, deny non-privileged users and applications unauthorized access, and
fail closed when required key material cannot be trusted. These goals apply within the trusted
host and organizational infrastructure described below; they do not imply isolation between
admitted CANIS clients.

## Trust assumptions and Trusted Computing Base

The Trusted Computing Base (TCB) comprises the operating system, infrastructure administrators,
deployment automation, the CANIS service account, and corporate identity and access management
systems. CANIS assumes these components are administered by the deploying organization and act
according to its security policy. The host filesystem, process isolation, file ownership, ACLs,
certificate issuance, and credential provisioning depend on that TCB. An administrator who can
read process memory, replace files, change ACLs, or alter the service account is trusted by this
model.

Deploy CANIS on a managed host and private corporate network. Use a dedicated service account,
trusted PKI and identity management, protected deployment automation, access-controlled key and
database storage, restricted backups, and host network controls. Keep every ancestor of the
master-key and database paths under trusted administrative control. Admit only applications whose
operators may use the shared key namespace; CANIS does not enforce per-service key isolation.

## Out-of-scope threats

CANIS does not defend against a privileged host administrator, an attacker with root or
Administrator access, or a fully compromised operating system or deployment pipeline. It does
not provide HSM-level protection against extraction from process memory or a zero-trust boundary
around the deployment environment. Public cloud KMS and multi-tenant threat models are outside
this scope. A stolen valid client identity or credential can exercise the access available to
that admitted client; see the shared-access boundary below.

## Trust boundaries

- **Server and storage:** The CANIS process and host are inside the TCB. An attacker who obtains
  both the encrypted `CANISDB1` database and the master-key file can recover stored private keys.
  Protect the files and their backups with separate access controls.
- **Network:** The server requires TLS 1.3 and a trusted client certificate before processing
  CANISP. The client validates the server certificate and hostname. The deployment must use
  corporate network controls to restrict who can reach the service.
- **Applications:** Clients authenticate with CANIS credentials after the TLS handshake. Every
  authenticated client can request decryption under every stored service key. A compromised
  client can therefore recover file keys across services. Decrypt audit events record the
  credential identity and key name, without key material.
- **Shared filesystems:** Encrypted files may be stored on untrusted shared storage; plaintext
  confidentiality depends on keeping their file envelopes encrypted at rest.

Applications should use the official `client` Maven artifact. CANISP is the internal
client/server protocol, documented for maintenance, debugging, research, and PoCs rather than as
an application API.

## What clients can do

`|set serviceName` creates a 2048-bit RSA key pair only if the name is unused. A duplicate
returns `ERROR: Service already exists` and preserves the existing pair. The
`|get-public serviceName` command exposes the public key for encrypting file keys.
`|decrypt serviceName payload` uses the private key on the server and returns the decrypted
payload, such as a file key; it never returns the RSA private key. `|del serviceName` removes
the active key.

Service names are identifiers validated by both the client and server. To rotate a key, create a
new service name and re-encrypt envelopes that must remain readable before deleting the old key.
Deletion cannot erase key material from JVM memory, backups, snapshots, or storage history. See
[RSA key lifecycle](rsa-key-lifecycle.md).

## Master key and recovery

`CANIS_SECRET_KEY` names the AES key file used to encrypt the database. CANIS validates and
loads it before opening the TLS listener; a missing, malformed, or insecurely accessible file
stops startup, even if the database is absent. The server does not generate a replacement key.
Changing the file after startup does not rotate the loaded key.

These checks catch operational mistakes and unsafe file configuration and prevent access by
non-privileged identities under the host's access controls. They do not prevent a TCB member from
reading or replacing the key. The loader checks the key file and every ancestor for symbolic
links, while ownership and access checks cover the file and immediate parent. Higher ancestors
must be controlled by the deployment. See the exact checks and limits
in the [master-key guide](master-key.md).

Normal startup rejects a missing database. A new database requires explicit initialization; a
valid key cannot be matched to a database that is absent. Database authentication and parsing
finish before the listener binds. If the database disappears or changes during operation, CANIS
enters a recovery-required state instead of creating an empty store. Restore the original
database with its matching master key from protected backups. See the
[master-key requirements](master-key.md).

Backups should contain the encrypted database and matching key file under separate access
controls, never plaintext private keys or decrypted file keys. Test restores by loading the
database and decrypting a known test envelope.

## Logs

`io.canis.audit` records authentication, key creation, access and deletion, decrypt success or
failure, and rejected commands. Events may include credential identities, key names, remote
addresses, and failure reasons. They must not contain raw credentials, private keys, encrypted
payloads, or decrypted payloads.
