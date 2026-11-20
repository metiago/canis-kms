# Official JOSS research release candidate

## Release details

| Field | Value |
| --- | --- |
| Version and tag | `0.1.0-rc.1`, `v0.1.0-rc.1` |
| Tagged commit | `e1d6998b5c5658750870b39e2a4d511de5bed7f3` |
| Release URL | [GitHub Release `v0.1.0-rc.1`](https://github.com/metiago/canis-kms/releases/tag/v0.1.0-rc.1) |
| GitHub Actions run | [CANIS KMS GitHub Release](https://github.com/metiago/canis-kms/actions/runs/44433044373) |
| Release type | GitHub prerelease; not marked latest |
| Publication date | 2026-11-19 01:05:38 UTC |
| Publication method | GitHub Releases API, after the tagged Actions build and POSIX gate passed; the JReleaser publication step failed on branch detection |

The `0.1.0-rc.1` version follows the root, client, and server Maven POMs and the existing JReleaser `v<projectVersion>` tag rule. A `v1.0.0-joss-rc1` tag would not match the release workflow's version gate and would imply a maturity level outside the documented research-candidate boundary. The annotated tag identifies the tested source and manuscript revision. This is a software release, not a JOSS submission.

## Generated artifacts and SHA-256

The tagged workflow built the tracked source archive and JReleaser checksum manifest. After its publication step failed, those retained CI outputs and the generated release notes were used to create the GitHub prerelease through the Releases API. The JARs and generated release notes below were built locally as validation outputs; they are not GitHub Release attachments. GitHub-generated source archives are separate downloads and are not the checksummed attached ZIP.

| Artifact | SHA256 |
|-----------|---------|
| [canis-kms-0.1.0-rc.1-src.zip](https://github.com/metiago/canis-kms/releases/download/v0.1.0-rc.1/canis-kms-0.1.0-rc.1-src.zip) (CI release attachment, 1,863,202 bytes) | `81f132bf6b69b482649e6d4e300c7b40c5394954ac858c0ff2b2bf4a49621c8a` |
| [checksums_sha256.txt](https://github.com/metiago/canis-kms/releases/download/v0.1.0-rc.1/checksums_sha256.txt) (CI release attachment) | `1000b1ebd86791c94e9d437689be40cd8827a2666d95c70e7ee7d5d28c40d86c` |
| `CHANGELOG.md` (generated JReleaser release notes; local) | `c9b8b56c50b33e3dbd107387f781a07887341f22b68fcb50a9da16cb06e2d95f` |
| `client-0.1.0-rc.1.jar` (local build) | `3c37830f731da7151c08d631fd61fdd0a57579f33dd3ba652d5243f830ede676` |
| `canis.jar` (local server build) | `dd03c90450d6ed4510dbd45594bdbfbd59eb27f0c8509823d5ed4b37ac21b668` |
| `canis-jar-with-dependencies.jar` (local server assembly) | `187ad0be89f467760bba6d0b0e6fcb620e48a4972239463f419830569c98fed1` |

The CI JReleaser manifest contains `81f132bf6b69b482649e6d4e300c7b40c5394954ac858c0ff2b2bf4a49621c8a  canis-kms-0.1.0-rc.1-src.zip`. The Windows and Ubuntu `git archive` ZIPs from the same tagged commit have identical 977 entry names but different ZIP bytes. The table uses the published Ubuntu workflow output, not the Windows dry-run archive.

## Validation

| Check | Result |
| --- | --- |
| `mvn clean verify` on Windows 11 with Maven 3.9.12 and Java 21.0.6 | Passed on the tagged commit: 159 tests, no failures or errors, eight skips. Five skips require a separately started integration server; three require POSIX. |
| Maven Spotless validation and packaging | Passed as part of `mvn clean verify`; client JAR, server JAR, and server assembly built. |
| Source archive inspection | CI and Windows ZIP entry lists match (977 entries); tracked-source scan found no private-key PEM marker or GitHub PAT marker. |
| `mvn -N jreleaser:config` | Passed with a nonfunctional placeholder token. |
| JReleaser `full-release` dry run | Passed with deploy, upload, and package publication disabled; generated release notes and `checksums_sha256.txt` and selected the expected two attachments. |
| Tagged Ubuntu workflow and POSIX security gate | Maven passed: 159 tests, no failures or errors, seven skips. The gate reported `JOSS03_POSIX_COVERAGE=PASS required permission and symlink tests executed`. The final JReleaser publication step failed because its tag checkout inferred branch `main` instead of `master`; release assets were retained by Actions for verification. |
| Published release metadata | GitHub API reports tag `v0.1.0-rc.1`, target commit `e1d6998b5c5658750870b39e2a4d511de5bed7f3`, `prerelease: true`, and `draft: false`. Release-note SHA-256 matched the CI-generated `CHANGELOG.md`. |
| Published asset checksum verification | Both assets were downloaded from the public release and their SHA-256 values matched GitHub's asset digests and the table above. The published manifest's source-ZIP digest matched the downloaded ZIP; its listing contained 977 entries. |

## Scope and limitations

The released software is the Java 21 client and server, official TLS 1.3/mTLS CANISP path, client-side `CANISHYB1` file envelope, encrypted `CANISDB1` persistence, and documented credential-level shared-trust model. The release adds no architecture or wire-format change beyond the tagged implementation. Operators must supply TLS identities, trust material, and the database master key externally. Existing `CANISDB1` and `CANISHYB1` formats remain the documented compatibility boundary; historical predecessor wire compatibility is not claimed.

The research-candidate risks remain the shared-access blast radius, missing built-in audit-integrity verifier, unmeasured interrupted-write durability and service-availability windows, and lack of independent reproduction of this revision. The tagged release workflow also has a JReleaser branch-detection defect: its publication step attempted `main` although this revision is on `master`. Future automation should set the release branch explicitly and test the tagged publication path. The original local-control study, broader workloads, and canonical JOSS paper build remain future work. See the [fixed candidate record (archived)](repository-cleanup-report.md#archived-files), [validation](joss02-validation.md), [final release boundary](joss02-release-boundary-final.md), and [release notes](joss02-release-notes.md) for the precise evidence and limits. No JOSS submission was made.
