# Security reporting and supported scope

Report suspected vulnerabilities privately to **metiago.work@gmail.com** or,
if available, through GitHub's **Report a vulnerability** option. Do not publish
vulnerabilities, credentials, private keys, plaintext, or exploit steps in a
public issue. Include the affected revision, impact, and a minimal synthetic
reproduction when safe to share. Coordinate the timing of public disclosure
with the maintainer so there is an opportunity to investigate and address the
report.

These email and GitHub vulnerability-reporting routes are private channels.
The public GitHub issue tracker is for ordinary, non-sensitive bugs, questions,
and support only; it is not a vulnerability-reporting channel.

The maintainer aims to acknowledge and initially respond within **7 calendar
days**. If no substantive follow-up arrives within **30 calendar days**, please
follow up through the same private channel. These are communication targets,
not a remediation deadline.

The current development tree is a proof of concept; no production support or
remediation-time commitment has been established. CANIS targets organization-managed
corporate environments. Its operating system, infrastructure administrators,
deployment automation, service account, and corporate identity systems are trusted.
Privileged-host compromise, HSM-level guarantees, multi-tenant isolation, and public
cloud KMS threat models are outside the intended security boundary. The supported
behavior and known limits are in the [threat model](docs/threat-model.md),
[master-key guide](docs/master-key.md), and [operational recovery guide](docs/operational-recovery.md).
