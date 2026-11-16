param(
    [string]$InaraPath = (Join-Path $env:TEMP 'canis-joss05-inara'),
    [string]$PandocDir = (Join-Path $env:LOCALAPPDATA 'Programs\pandoc'),
    [string]$TexBin = (Join-Path $env:USERPROFILE 'miktex\texmfs\install\miktex\bin\x64')
)

$ErrorActionPreference = 'Stop'
$expectedInaraRevision = '88ee5d656f147d801448ce77ad8375619d383df6'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$paperPath = Join-Path $repositoryRoot 'joss-papers\paper.md'
$bibliographyPath = Join-Path $repositoryRoot 'joss-papers\paper.bib'
$draftPdfPath = Join-Path $PSScriptRoot 'joss05-draft.pdf'

if (-not (Test-Path -LiteralPath (Join-Path $InaraPath 'Makefile'))) {
    throw 'Inara v1.3.1 is not cloned at InaraPath; see joss05-method.md.'
}
if (-not (Test-Path -LiteralPath (Join-Path $PandocDir 'pandoc.exe'))) {
    throw 'Pandoc executable not found at PandocDir.'
}
if (-not (Test-Path -LiteralPath (Join-Path $TexBin 'lualatex.exe'))) {
    throw 'MiKTeX LuaLaTeX executable not found at TexBin.'
}

$inaraRevision = (& git -C $InaraPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $inaraRevision -ne $expectedInaraRevision) {
    throw "Expected Inara v1.3.1 at $expectedInaraRevision; found $inaraRevision"
}

$oldPath = $env:Path
$oldArtifactsPath = $env:INARA_ARTIFACTS_PATH
Push-Location $InaraPath
try {
    $env:Path = "$PandocDir;$TexBin;$oldPath"
    $env:INARA_ARTIFACTS_PATH = 'publishing-artifacts/'

    # Inara's self-citation filter joins resource paths using a POSIX separator.
    # An absolute footer style and one Windows resource directory avoid that
    # lookup failure without changing the cloned release files.
    $shared = Get-Content -Raw -Encoding UTF8 -LiteralPath 'data\defaults\shared.yaml'
    $footer = (Resolve-Path -LiteralPath 'resources\footer.csl').Path.Replace('\', '/')
    $sharedWindows = $shared.Replace("footer-csl: 'footer.csl'", "footer-csl: '$footer'")
    if ($sharedWindows -eq $shared) { throw 'Could not locate footer-csl in Inara defaults.' }
    Set-Content -Encoding UTF8 -LiteralPath 'data\defaults\shared-windows.yaml' -Value $sharedWindows

    # Portable MiKTeX has no Perl for latexmk. Call LuaLaTeX directly and
    # allow its required LaTeX packages to be installed without a GUI prompt.
    $pdfDefaults = Get-Content -Raw -Encoding UTF8 -LiteralPath 'data\defaults\pdf.yaml'
    $pdfWindows = $pdfDefaults.Replace('pdf-engine: latexmk', "pdf-engine: lualatex`r`npdf-engine-opt: --enable-installer")
    $pdfWindows = [regex]::Replace($pdfWindows, '(?m)^pdf-engine-opt: -lualatex\r?\n', '')
    if ($pdfWindows -eq $pdfDefaults) { throw 'Could not adjust Inara PDF engine.' }
    Set-Content -Encoding UTF8 -LiteralPath 'data\defaults\pdf-windows.yaml' -Value $pdfWindows

    # Hack is not installed on this host. This draft uses a local substitution
    # so its layout and references can be inspected; it is not the final style.
    $template = Get-Content -Raw -Encoding UTF8 -LiteralPath 'data\templates\default.latex'
    $templateWindows = $template.Replace('\setmonofont{Hack}', '\setmonofont{Latin Modern Mono}')
    if ($templateWindows -eq $template) { throw 'Could not locate Hack font in Inara template.' }
    Set-Content -Encoding UTF8 -LiteralPath 'data\templates\default-windows.latex' -Value $templateWindows

    Copy-Item -LiteralPath $bibliographyPath -Destination 'resources\paper.bib' -Force
    $sourceBibHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $bibliographyPath).Hash
    $copiedBibHash = (Get-FileHash -Algorithm SHA256 -LiteralPath 'resources\paper.bib').Hash
    if ($sourceBibHash -ne $copiedBibHash) { throw 'Temporary bibliography copy differs from source.' }

    New-Item -ItemType Directory -Path 'publishing-artifacts' -Force | Out-Null
    Write-Output "Inara revision: $inaraRevision"
    & pandoc --version | Select-Object -First 1
    & pandoc --data-dir=data --defaults=shared-windows --defaults=pdf-windows.yaml `
        --defaults=resources/joss/defaults.yaml --resource-path=resources `
        --metadata=article-info-file=resources/default-article-info.yaml `
        --variable=joss --template=data/templates/default-windows.latex `
        --quiet --output=publishing-artifacts/paper.pdf $paperPath.Replace('\', '/')
    if ($LASTEXITCODE -ne 0) { throw "Pandoc/Inara PDF build failed with exit code $LASTEXITCODE" }

    $builtPdf = Join-Path $InaraPath 'publishing-artifacts\paper.pdf'
    if (-not (Test-Path -LiteralPath $builtPdf)) { throw 'Pandoc exited 0 without a PDF.' }
    Copy-Item -LiteralPath $builtPdf -Destination $draftPdfPath -Force
    $pdf = Get-Item -LiteralPath $draftPdfPath
    if ($pdf.Length -le 0) { throw 'Generated PDF is empty.' }
    Write-Output "Draft PDF: $draftPdfPath"
    Write-Output "PDF bytes: $($pdf.Length)"
    Write-Output "PDF SHA-256: $((Get-FileHash -Algorithm SHA256 -LiteralPath $draftPdfPath).Hash)"
} finally {
    Pop-Location
    $env:Path = $oldPath
    $env:INARA_ARTIFACTS_PATH = $oldArtifactsPath
}
