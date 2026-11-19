# JReleaser GitHub Release setup

## Scope and configuration

The root [`pom.xml`](../../pom.xml) pins JReleaser Maven Plugin 1.26.0 with inheritance disabled, so it runs once for the reactor. [`jreleaser.yml`](../../jreleaser.yml) uses the root Maven version, GitHub repository `metiago/canis-kms`, a pre-existing `v<version>` tag, and [`joss02-release-notes.md`](joss02-release-notes.md) as the reviewed release body. The release is marked as a prerelease and is not made the latest release. JReleaser does not create or move the tag and does not overwrite an existing release.

The manual [release workflow](../../.github/workflows/release.yml) verifies the selected tag matches the root POM version, runs `mvn clean verify` and the POSIX security coverage gate, and creates `target/release/canis-kms-<version>-src.zip` from tracked files at that tag with `git archive`. JReleaser calculates a SHA-256 checksum file for the archive and attaches both to a GitHub Release. The workflow also retains generated local outputs as a short-lived Actions artifact.

The workflow invokes JReleaser with deploy, uploader, and package publication steps disabled. It never runs `mvn deploy`. The client POM's existing `distributionManagement` describes a separate GitHub Packages path and is not used by this workflow. No Maven Central or package registry publication is configured here.

## GitHub token and permissions

No custom repository secret is required. GitHub supplies `secrets.GITHUB_TOKEN` to the workflow as `JRELEASER_GITHUB_TOKEN`; the workflow grants it only `contents: write`. Repository or organization Actions settings must allow that permission for the release job. The checkout step does not persist the token in Git configuration. Do not put a token in `jreleaser.yml`, a POM, release notes, a source archive, or a command-line argument.

Local `jreleaser:config` and dry-run checks also require `JRELEASER_GITHUB_TOKEN`; a nonfunctional placeholder is sufficient for local validation. A real token is needed only when creating the GitHub Release.

## Release process

1. Confirm the desired release revision is committed, the working tree is clean, and the three Maven modules use the intended version. Review the JOSS-02 evidence and the release notes for that exact revision. Complete any separate JOSS submission gates before claiming submission readiness.
2. Run `mvn clean verify`. Review the test failures and skips. On a POSIX host, verify the POSIX security cases execute.
3. Review the source archive contents for secrets and generated files. `git archive` includes tracked files from the selected commit and excludes the local `.git` directory and ignored `target` output.
4. After release approval, create and push an annotated `v<version>` tag pointing to that revision. Do not move or reuse a published tag.
5. Manually dispatch **CANIS KMS GitHub Release** at that tag (for example, `gh workflow run release.yml --ref v0.1.0-rc.1`). Selecting a branch fails the workflow's tag gate. A tag push alone does not start this workflow.
6. Inspect the workflow result and the GitHub prerelease: title, notes, source ZIP, SHA-256 checksum file, and tag target. The GitHub-generated source downloads are separate from the attached, checksummed ZIP.

For local configuration validation, run `mvn -N jreleaser:config` with `JRELEASER_GITHUB_TOKEN` set to a nonfunctional placeholder. To exercise the pipeline without remote writes, prepare the source archive under `target/release/` and run `mvn -N jreleaser:full-release -Djreleaser.dry.run=true -Djreleaser.deploy.skip=true -Djreleaser.upload.skip=true -Djreleaser.publish.skip=true`. In PowerShell, quote each `-D...` argument. JReleaser's [dry-run option](https://jreleaser.org/guide/latest/tools/jreleaser-maven.html) skips remote operations. The local archive and dry-run outputs are validation artifacts, not a release.

## Rollback and correction

If a workflow fails before the GitHub Release step, keep the tag and diagnose the failed build, archive, or configuration. Do not rerun against a different commit under the same tag.

If a release was created with incorrect notes or assets, first stop further distribution, then remove the GitHub Release through the repository UI after reviewing who may have downloaded it. Deleting a GitHub Release does not by itself undo a tag or copies already downloaded. Preserve the immutable tag for traceability; make the correction on a new commit and issue a new candidate version and tag, such as `v0.1.0-rc.2`. If a release token was exposed, revoke or rotate it and investigate the workflow logs and artifacts. No Maven registry rollback is needed for this workflow because it does not publish packages.

## References

- [JReleaser Maven plugin](https://jreleaser.org/guide/latest/tools/jreleaser-maven.html)
- [GitHub release configuration](https://jreleaser.org/guide/latest/reference/release/github.html)
- [Checksums](https://jreleaser.org/guide/latest/reference/checksum.html)
