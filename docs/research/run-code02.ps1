$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    New-Item -ItemType Directory -Force -Path server/target/code02-classes | Out-Null
    javac -cp client/target/classes -d server/target/code02-classes docs/research/Code02Harness.java
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    java -cp 'server/target/code02-classes;client/target/classes' Code02Harness @args
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
