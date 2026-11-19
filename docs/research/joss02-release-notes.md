# CANIS-KMS JOSS research release candidate

This release candidate fixes the Java 21 client and server source used for the JOSS-02 research-release review. It provides the official TLS 1.3 and mutual-TLS CANISP path, client-side `CANISHYB1` encrypted file envelopes, encrypted `CANISDB1` persistence, and the documented shared-trust model. An admitted client may request recovery under any stored key name.

The candidate validation in `docs/research/joss02-validation.md` records 159 Windows tests with no failures or errors and eight explained skips, plus four selected functional smoke checks on the earlier fixed source and manuscript revision. The final claim audit and release boundary in `docs/research/` distinguish those checks from historical research measurements. The release workflow verifies the tagged revision again before creating a GitHub Release.

The attached `canis-kms-*-src.zip` contains tracked source from the release tag. The attached SHA-256 checksum file identifies that archive. Build from the archive with Java 21 and `mvn clean verify`; see `docs/reviewer-guide.md` for setup and a synthetic example.

This is a research candidate, not a production-readiness or general scalability claim. The original local-control and service-availability studies, independent candidate reproduction, and canonical JOSS paper build remain separate submission work. The release workflow runs the POSIX security coverage gate on the tagged revision. The shared-access blast radius, missing built-in audit-integrity verifier, and unmeasured interrupted-write durability are documented limits.
