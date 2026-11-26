# Contributing to CANIS-KMS

CANIS is a security-sensitive project. Before proposing a change,
read `docs/threat-model.md` and `docs/reviewer-guide.md`.
Use the public GitHub issue tracker for ordinary bugs, questions, and feature
requests. Include the software revision, Java/Maven versions, OS, steps to
reproduce, expected behavior, and sanitized actual behavior. Never post keys,
passwords, plaintext files, TLS private material, or raw logs containing them.

For a code change, open a pull request with a focused description of
architecture, security, operational, and backward-compatibility effects,
risks, and future improvements. Run `mvn spotless:apply` and
`mvn --batch-mode --no-transfer-progress clean verify`. Include unit,
integration, and negative security cases for a security change, and update
documentation when behavior changes. Keep
storage compatibility and TLS 1.3 validation requirements intact.

Use the public issue tracker to seek support for non-sensitive problems.
For a suspected vulnerability, follow [SECURITY.md](SECURITY.md) and do not disclose
exploit details in a public issue.
