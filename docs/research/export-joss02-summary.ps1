param(
    [ValidatePattern('^joss[0-9]{2}$')]
    [string]$OutputPrefix = 'joss02'
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$suiteRows = @()
$caseRows = @()

foreach ($module in 'client', 'server') {
    $reportDirectory = Join-Path $repositoryRoot "$module/target/surefire-reports"
    $reports = @(Get-ChildItem -Path (Join-Path $reportDirectory 'TEST-*.xml') -ErrorAction Stop | Sort-Object Name)
    if ($reports.Count -eq 0) {
        throw "No Surefire XML reports found for $module"
    }
    foreach ($reportFile in $reports) {
        [xml]$report = Get-Content -Raw -LiteralPath $reportFile.FullName
        $suite = $report.testsuite
        $suiteRows += [pscustomobject]@{
            module = $module
            suite = [string]$suite.name
            tests = [int]$suite.tests
            failures = [int]$suite.failures
            errors = [int]$suite.errors
            skipped = [int]$suite.skipped
        }
        foreach ($testCase in @($suite.testcase)) {
            $outcome = if ($null -ne $testCase.failure) {
                'failure'
            } elseif ($null -ne $testCase.error) {
                'error'
            } elseif ($null -ne $testCase.skipped) {
                'skipped'
            } else {
                'passed'
            }
            $caseRows += [pscustomobject]@{
                module = $module
                suite = [string]$suite.name
                test = [string]$testCase.name
                outcome = $outcome
            }
        }
    }
}

$reportedCount = ($suiteRows | Measure-Object -Property tests -Sum).Sum
if ($reportedCount -ne $caseRows.Count) {
    throw "Surefire suite and test-case counts disagree"
}

$suiteRows | Export-Csv -Path (Join-Path $PSScriptRoot "$OutputPrefix-suite-report.csv") -NoTypeInformation -Encoding UTF8
$caseRows | Export-Csv -Path (Join-Path $PSScriptRoot "$OutputPrefix-test-cases.csv") -NoTypeInformation -Encoding UTF8
Write-Output "Exported $($suiteRows.Count) suites and $($caseRows.Count) test cases without raw logs or system properties."
