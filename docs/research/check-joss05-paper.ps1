$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$paperPath = Join-Path $root 'joss-papers/paper.md'
$bibliographyPath = Join-Path $root 'joss-papers/paper.bib'
$paper = Get-Content -Raw -Encoding UTF8 -LiteralPath $paperPath
$bibliography = Get-Content -Raw -Encoding UTF8 -LiteralPath $bibliographyPath

$requiredSections = @(
    'Summary',
    'Statement of need',
    'State of the field',
    'Software design',
    'Research impact statement',
    'AI usage disclosure',
    'Acknowledgements',
    'References'
)
$headings = @([regex]::Matches($paper, '(?m)^# ([^\r\n]+)\r?$') |
    ForEach-Object { $_.Groups[1].Value })
$missingSections = @($requiredSections | Where-Object { $_ -notin $headings })
$body = [regex]::Replace($paper, '(?s)^---\s*.*?\s*---\s*', '')
$body = [regex]::Replace($body, '(?m)^# .+$', '')
$wordCount = [regex]::Matches($body, '\b[\p{L}\p{N}]+\b').Count
$citations = @([regex]::Matches($paper, '@[A-Za-z][A-Za-z0-9_-]*') |
    ForEach-Object { $_.Value.Substring(1) } | Sort-Object -Unique)
$entries = @([regex]::Matches($bibliography, '(?m)^@[A-Za-z]+\{([^,]+),') |
    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
$missingEntries = @($citations | Where-Object { $_ -notin $entries })
$uncitedEntries = @($entries | Where-Object { $_ -notin $citations })
$placeholders = $paper.Contains('REPLACE WITH AUTHOR NAME') -or
    $paper.Contains('REPLACE WITH INSTITUTION AND COUNTRY')

Write-Output "Body word count (simple Unicode token count; excludes YAML/headings): $wordCount"
Write-Output "Required headings found: $($requiredSections.Count - $missingSections.Count)/$($requiredSections.Count)"
Write-Output "Citation keys: $($citations.Count); bibliography entries: $($entries.Count)"
Write-Output "Missing keys: $($missingEntries.Count); uncited entries: $($uncitedEntries.Count)"
Write-Output "Author/affiliation placeholders present: $placeholders"
Write-Output 'This static check does not compile the paper or establish research use or metadata approval.'

if ($wordCount -lt 750 -or $wordCount -gt 1750 -or $placeholders -or
    $missingSections.Count -gt 0 -or $missingEntries.Count -gt 0 -or $uncitedEntries.Count -gt 0) {
    exit 1
}
