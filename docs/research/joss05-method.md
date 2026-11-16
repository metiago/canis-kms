# JOSS-05 manuscript and evidence verification method

## Scope and prerequisites

This procedure checks the current working tree that supports the JOSS-05
manuscript edit. It repeats the existing categorical shared-trust experiment
and the full software test suite. It does not measure performance or establish
research adoption. Run from the repository root with JDK 21 plus `keytool`,
Maven 3.9 or compatible, Git, PowerShell for the Windows commands, an available
loopback port, and a Java TLS provider with an approved TLS 1.3 cipher suite.
No externally supplied credentials, certificates, or production database are
used. A standard local Inara build additionally requires GNU make, Pandoc,
TeX Live with Inara's required LaTeX packages, and the Hack font. The Windows
draft fallback uses Pandoc 3.14 and portable MiKTeX 26.5 with LuaLaTeX instead.
The official Inara v1.3.1 source is cloned outside this repository. No
container is needed.

## Exact commands

On Windows, from the repository root:

```powershell
git rev-parse HEAD
git status --short
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss02.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-joss02-summary.ps1
Copy-Item -LiteralPath docs/research/joss02-suite-report.csv -Destination docs/research/joss05-suite-report.csv
Copy-Item -LiteralPath docs/research/joss02-test-cases.csv -Destination docs/research/joss05-test-cases.csv
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/check-joss05-paper.ps1
```

On POSIX, run `sh docs/research/run-joss02.sh` and
`sh docs/research/run-joss01.sh` for software verification. The report-export
and static paper-check scripts require PowerShell; run them with `pwsh` where
available, or inspect the Surefire XML and manuscript manually. Record the
exact commit, dirty status, OS, Maven/JDK, commands, exit codes, suite counts,
and any skips. Rerun the full suite on the final fixed revision because this
method's working-tree outcome does not validate a later tag.

For a **local paper build**, clone a fixed Inara revision into a separate
temporary tool directory, run make from the Inara root, and point `ARTICLE` to
this repository's `joss-papers/paper.md`. On POSIX:

```sh
git clone --depth 1 --branch v1.3.1 https://github.com/openjournals/inara.git /tmp/canis-joss05-inara
cd /tmp/canis-joss05-inara
git rev-parse HEAD
make --version
pandoc --version
make pdf JOURNAL=joss ARTICLE=/absolute/path/to/canis-kms/joss-papers/paper.md
```

On Windows PowerShell, use a temporary path such as
`$env:TEMP\canis-joss05-inara`. If GNU make is available, invoke the same
`make pdf` target from the clone root with the absolute manuscript path in
`ARTICLE`. In Inara v1.3.1, `ARTICLE` is the paper path;
`INARA_DATA_PATH` is the bundled Pandoc data/filter directory and should stay
at its Makefile default `data`. The expected output is the clone's
`publishing-artifacts/paper.pdf`. Record the Inara commit, tool versions,
exit code, PDF existence/size, build warnings, and whether each citation is
rendered. Inspect the PDF visually and compare its references with the
primary-source table in [JOSS-04 results](joss04-results.md). If a dependency
is missing or the build fails, retain the error and leave PDF verification
pending. Do not infer success from static citation-key matching.

When `make` is unavailable on Windows, use the checked-in direct Pandoc
fallback from the repository root after cloning the pinned release and
installing Pandoc and MiKTeX:

```powershell
$env:Path = "$env:LOCALAPPDATA\Programs\pandoc;$env:Path"
pandoc --version
git clone --depth 1 --branch v1.3.1 https://github.com/openjournals/inara.git (Join-Path $env:TEMP 'canis-joss05-inara')
powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/build-joss05-windows.ps1
```

If the clone already exists, omit the clone command after confirming its
revision. The script requires Inara commit
`88ee5d656f147d801448ce77ad8375619d383df6`; its optional `-InaraPath`,
`-PandocDir`, and `-TexBin` arguments locate the tools. It copies the exact
`paper.bib` into the temporary clone, checks its hash, and creates temporary
Windows defaults for Inara's absolute footer CSL, direct `lualatex`, and the
available Latin Modern Mono font. It then executes Pandoc with Inara's JOSS
defaults, filters, and template and writes
[`joss05-draft.pdf`](joss05-draft.pdf). The expected outcome is exit code 0,
a nonempty PDF, and four resolved references; verify those independently by
PDF text extraction and visual inspection. This is a draft layout check with
a font substitution and a different TeX distribution from the standard
Inara build; repeat the canonical build on the fixed candidate revision.

## Generated fixtures and expected output

`SharedTrustDomainResearchTest` creates a temporary directory; a fresh master
key; an explicitly initialized encrypted database; temporary PKCS#12 TLS
identity/trust stores; three random service passwords; and a synthetic UTF-8
input consisting of eight copies of `Synthetic CANIS research fixture\n`.
It creates one encrypted envelope and two decrypted outputs. The expected
categorical outcome is byte-for-byte equality of each output with the input
for the valid `recipient` and `observer` credentials, plus rejection of an
altered `observer` password. The expected focused Surefire result is one test,
zero failures/errors/skips. It does not test separate client certificates.

The full suite should exit 0 with no failures or errors; counts and skips are
environment-dependent and must be recorded, not assumed. The export script
reads local Surefire XML and writes sanitized suite/test-case CSVs, excluding
raw logs, stack traces, paths, system properties, and generated key material.
The JOSS-05 copies preserve this run's summary separately from JOSS-02. The
paper-check script reports an approximate body word count, required headings,
citation-key correspondence, and whether author placeholders remain. It does
not check rendered citations or author approval.

## Cleanup and platform limits

JUnit `@TempDir` cleans the synthetic files, database, keys, passwords, and
TLS stores after normal test completion; the test stops its loopback server in
a `finally` block. An interrupted process may leave temporary files and must
be reviewed before removal. Maven's `client/target/` and `server/target/`
contain local build and raw Surefire artifacts; retain the needed evidence,
then use `mvn clean` if local cleanup is desired. Do not commit raw Surefire
XML, logs, credentials, certificates, or private keys. The sanitized CSV
summaries and this method are the repository artifacts.

On Windows, the test account must be allowed to set restrictive ACLs on its
temporary directory; a restricted sandbox may deny this. POSIX-specific
master-key permission and symlink checks are skipped on Windows, so this run
cannot validate them. TLS cipher availability and loopback behavior depend on
the local JDK/provider. The direct Windows build writes temporary overrides
and a bibliography copy in the Inara clone, a PDF in its
`publishing-artifacts/` directory, and a retained draft PDF in this repository.
MiKTeX may install missing packages in the user's local package tree. After
retaining review evidence, the clone and generated PDF may be removed by the
operator; inspect the resolved target before deleting it. The retained draft
can be removed with `Remove-Item -LiteralPath docs/research/joss05-draft.pdf`
after its evidence is no longer needed. The standard build still needs GNU
make, Pandoc, TeX Live packages, and fonts on its host. A missing dependency
is a build blocker, not a manuscript rendering result.
