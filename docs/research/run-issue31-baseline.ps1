$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Baseline build failed' }
    New-Item -ItemType Directory -Force -Path server/target/code02-classes | Out-Null
    javac -cp client/target/classes -d server/target/code02-classes docs/research/Code02Harness.java
    if ($LASTEXITCODE -ne 0) { throw 'Research harness compilation failed' }

    $outputDir = Join-Path $repositoryRoot 'server/target/issue31-baseline'
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    $matrix = @(
        @{ Delay = 0; Workers = 1 }, @{ Delay = 60000; Workers = 4 },
        @{ Delay = 0; Workers = 4 }, @{ Delay = 60000; Workers = 1 }
    )
    foreach ($setting in $matrix) {
        foreach ($repetition in 0..5) {
            $warmup = $repetition -eq 0
            $files = if ($warmup) { 20 } else { 100 }
            $seed = if ($warmup) { 1 } else { $repetition }
            $label = if ($warmup) { 'warmup' } else { "r$repetition" }
            $name = "d$($setting.Delay)-c$($setting.Workers)-$label"
            $base = Join-Path $outputDir $name
            $result = "$base.json"
            if (Test-Path -LiteralPath $result) {
                $prior = Get-Content -LiteralPath $result -Raw | ConvertFrom-Json
                if ($prior.outcome -eq 'passed' -and $prior.verified_files -eq $files -and
                    (Test-Path -LiteralPath "$base.os-memory.json")) {
                    Write-Output "ISSUE31_EXISTING_PASS=$name"
                    continue
                }
                throw "Existing incomplete or failed result: $name"
            }
            $stdout = "$base.stdout.txt"
            $stderr = "$base.stderr.txt"
            $arguments = '-cp "server/target/code02-classes;client/target/classes" Code02Harness' +
                " --scenario normal --files $files --size 0 --concurrency $($setting.Workers)" +
                " --interval-ms 500 --delay-ms $($setting.Delay) --repetition $seed" +
                " --output server/target/issue31-baseline/$name.json"
            $started = [DateTime]::UtcNow
            $timer = [System.Diagnostics.Stopwatch]::StartNew()
            $process = Start-Process -FilePath (Get-Command java).Source -ArgumentList $arguments `
                -WorkingDirectory $repositoryRoot -RedirectStandardOutput $stdout `
                -RedirectStandardError $stderr -PassThru -WindowStyle Hidden
            $peaks = @{}
            $samples = 0
            $lastSample = 0
            $maxGap = 0
            while (-not $process.HasExited) {
                $now = $timer.ElapsedMilliseconds
                if ($samples -gt 0) { $maxGap = [Math]::Max($maxGap, $now - $lastSample) }
                $lastSample = $now
                $samples++
                foreach ($javaProcess in @(Get-Process -Name java -ErrorAction SilentlyContinue)) {
                    $pidKey = [string]$javaProcess.Id
                    $value = [long]$javaProcess.PeakWorkingSet64
                    if (-not $peaks.ContainsKey($pidKey) -or $value -gt $peaks[$pidKey]) {
                        $peaks[$pidKey] = $value
                    }
                }
                if ($timer.Elapsed.TotalSeconds -gt 240) {
                    Stop-Process -Id $process.Id -Force
                    throw "Coordinator exceeded 240 seconds: $name"
                }
                Start-Sleep -Milliseconds 100
                $process.Refresh()
            }
            $process.WaitForExit()
            $timer.Stop()
            $data = $null
            if (Test-Path -LiteralPath $result) { $data = Get-Content -LiteralPath $result -Raw | ConvertFrom-Json }
            $memory = [ordered]@{
                schema = 'issue31-os-memory-v1'; run = $name; warmup = $warmup
                started_utc = $started.ToString('o'); elapsed_ms = $timer.ElapsedMilliseconds
                coordinator_exit_code = $process.ExitCode; sample_count = $samples
                max_sample_gap_ms = $maxGap
                server_peak_working_set_bytes = $null
                producer_peak_working_set_bytes = $null
                consumer_peak_working_set_bytes = $null
            }
            if ($null -ne $data) {
                foreach ($role in @('server', 'producer', 'consumer')) {
                    $pidKey = [string]$data.("${role}_pid")
                    if ($peaks.ContainsKey($pidKey)) {
                        $memory.("${role}_peak_working_set_bytes") = $peaks[$pidKey]
                    }
                }
            }
            $memory | ConvertTo-Json | Set-Content -LiteralPath "$base.os-memory.json" -Encoding UTF8
            Get-Content -LiteralPath $stdout
            if (($null -ne $process.ExitCode -and $process.ExitCode -ne 0) -or
                $null -eq $data -or $data.outcome -ne 'passed' -or $data.verified_files -ne $files) {
                if (Test-Path -LiteralPath $stderr) { Get-Content -LiteralPath $stderr }
                throw "Baseline run failed: $name"
            }
        }
    }
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/export-issue31-baseline.ps1
    if ($LASTEXITCODE -ne 0) { throw 'Baseline export failed' }
    Write-Output 'ISSUE31_BASELINE=passed measured=20 warmup=4'
} finally {
    Pop-Location
}
