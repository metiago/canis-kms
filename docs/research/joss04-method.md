# JOSS-04 bibliography and paper-build method

## Scope and prerequisites

This is publication-artifact validation, not a cryptographic or performance
experiment. It uses no sample data, credentials, keys, certificates, databases,
network listener, or research-subject fixture. Keep the current `paper.md` and
`paper.bib` together in `joss-papers/` and run from the repository root.

Prerequisites: Git, a running Docker-compatible container engine able to pull
`docker.io/openjournals/inara:1.3.1`, and permission to mount the paper directory
read-write into a container. The image is the Open Journals Inara paper tool.
Record the Git commit and dirty status, host/container environment, image ID or
digest, exit code, and rendered-reference inspection result. This run does not
substitute for rebuilding on the exact eventual candidate release.

## Exact commands

On this Windows host the usable Docker-compatible engine is in WSL's
`podman-machine-default` distribution. From the repository root in PowerShell:

```powershell
git rev-parse HEAD
git status --short
wsl -d podman-machine-default -- sh -lc 'docker image inspect docker.io/openjournals/inara:1.3.1 --format "{{.Id}}"'
wsl -d podman-machine-default -- sh -lc 'docker run --rm --volume "$PWD/joss-papers:/data" --env JOURNAL=joss docker.io/openjournals/inara:1.3.1 -o pdf paper.md'
```

The first image-inspection command is expected to fail if the image has not yet
been pulled; in that case run `wsl -d podman-machine-default -- docker pull
docker.io/openjournals/inara:1.3.1` and repeat the inspection. On POSIX with a
working Docker-compatible engine, run:

```sh
git rev-parse HEAD
git status --short
docker image inspect docker.io/openjournals/inara:1.3.1 --format '{{.Id}}'
docker run --rm --volume "$PWD/joss-papers:/data" --env JOURNAL=joss \
  docker.io/openjournals/inara:1.3.1 -o pdf paper.md
```

Inspect `joss-papers/paper.pdf` for four resolved references corresponding to
the four citation keys and for missing-reference warnings in the build output.
Compare the displayed authors/organizations, titles, dates, and DOI/URLs with
the primary-source table in [JOSS-04 results](joss04-results.md). Check that
all `@...` keys in `paper.md` have exactly one entry in `paper.bib` and that
no bibliography entry is uncited.

## Expected output, generated artifact, and cleanup

Expected success is container exit code 0, a nonempty
`joss-papers/paper.pdf`, four resolved and cited references, and no missing-
reference warning. The PDF is the only generated artifact. There are no
generated experimental fixtures. The draft PDF has provisional author and
affiliation metadata until JOSS-06 is completed; a successful JOSS-04 build
does not validate that metadata.

After retaining the build evidence needed for review, the draft PDF can be
removed manually with `Remove-Item -LiteralPath joss-papers/paper.pdf` in
PowerShell or `rm -- joss-papers/paper.pdf` on POSIX. Never remove an existing
paper PDF without checking its provenance first. The container is removed by
`--rm`; its downloaded image remains cached for later reproduction.

## Platform and interpretation limits

Windows Docker Desktop is unavailable in this environment; use the WSL engine
above. The WSL path and mount behavior may differ on other machines. Pulling
the image requires network access. A draft build checks citation resolution
and rendering on this working tree only; it does not prove author-approved
metadata, a public archive, independent reproduction, or JOSS eligibility.
No latency, throughput, scalability, reliability, cryptographic strength, or
research adoption is measured here.
