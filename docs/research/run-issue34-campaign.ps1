$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    New-Item -ItemType Directory -Force -Path server/target/issue34-classes | Out-Null
    javac -cp client/target/classes -d server/target/issue34-classes docs/research/Code02Harness.java docs/research/Issue33Harness.java docs/research/Issue34Harness.java
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $cases = @(
        @{ Scenario = 'A'; Variant = 'standard' },
        @{ Scenario = 'B'; Variant = 'standard' },
        @{ Scenario = 'C'; Variant = 'live' },
        @{ Scenario = 'C'; Variant = 'stopped' },
        @{ Scenario = 'D'; Variant = 'modified' },
        @{ Scenario = 'D'; Variant = 'deleted' },
        @{ Scenario = 'E'; Variant = 'standard' }
    )
    foreach ($repetition in 1..3) {
        foreach ($case in $cases) {
            $scenario = $case.Scenario
            $variant = $case.Variant
            $output = "server/target/issue34/issue34-run-$scenario-$variant-r$repetition.json"
            if (Test-Path -LiteralPath $output) { throw "Output path already exists: $output" }
            java -cp 'server/target/issue34-classes;client/target/classes' Issue34Harness --scenario $scenario --variant $variant --repetition $repetition --output $output
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        }
    }
    Write-Output 'ISSUE34_CAMPAIGN=passed cases=7 repetitions=3'
} finally {
    Pop-Location
}
