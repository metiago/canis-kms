$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    Write-Output 'JOSS-02 source revision:'
    git rev-parse HEAD
    Write-Output 'JOSS-02 working-tree changes:'
    git status --short
    Write-Output ('Operating system: ' + [System.Runtime.InteropServices.RuntimeInformation]::OSDescription)
    Write-Output 'Maven and Maven Java runtime:'
    mvn --version
    Write-Output 'Full reactor test suite:'
    mvn -q test
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Output 'Test reports: client/target/surefire-reports and server/target/surefire-reports'
} finally {
    Pop-Location
}
