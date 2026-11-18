$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    mvn --batch-mode --no-transfer-progress -pl server -am -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'CODE-03 package build failed' }
    New-Item -ItemType Directory -Force -Path server/target/code02-classes | Out-Null
    javac -cp client/target/classes -d server/target/code02-classes docs/research/Code02Harness.java
    if ($LASTEXITCODE -ne 0) { throw 'CODE-03 harness compilation failed' }

    $outputDir = Join-Path $repositoryRoot 'server/target/code03-preflight'
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    $runs = @(@{ Name = 'warmup-4m'; Size = 4194304; Repetition = 1; Warmup = $true })
    foreach ($size in @(4096, 262144, 1048576, 4194304)) {
        foreach ($repetition in @(1, 2)) {
            $runs += @{ Name = "size-$size-r$repetition"; Size = $size; Repetition = $repetition; Warmup = $false }
        }
    }
    $runs += @{ Name = 'campaign-shape-r1'; Size = 0; Repetition = 1; Warmup = $true;
        Files = 100; DelayMs = 60000; IntervalMs = 500 }

    foreach ($run in $runs) {
        $fileCount = if ($run.ContainsKey('Files')) { $run.Files } else { 4 }
        $delayMs = if ($run.ContainsKey('DelayMs')) { $run.DelayMs } else { 1000 }
        $intervalMs = if ($run.ContainsKey('IntervalMs')) { $run.IntervalMs } else { 0 }
        $base = Join-Path $outputDir $run.Name
        $result = "$base.json"
        if (Test-Path -LiteralPath $result) {
            $previous = Get-Content -LiteralPath $result -Raw | ConvertFrom-Json
            if ($previous.outcome -eq 'passed' -and $previous.verified_files -eq $fileCount -and
                (Test-Path -LiteralPath "$base.os-memory.json")) {
                Write-Output "CODE03_EXISTING_PASS=$($run.Name)"
                continue
            }
            throw "Existing unsuccessful or incomplete result: $result"
        }
        $stdout = "$base.stdout.txt"
        $stderr = "$base.stderr.txt"
        $classpath = 'server/target/code02-classes;client/target/classes'
        $arguments = '-cp "' + $classpath + '" Code02Harness --scenario normal --files ' +
            $fileCount + ' --size ' + $run.Size + ' --concurrency 4 --interval-ms ' +
            $intervalMs + ' --delay-ms ' + $delayMs + ' --repetition ' +
            $run.Repetition + ' --output server/target/code03-preflight/' + $run.Name + '.json'
        $started = [DateTime]::UtcNow
        $timer = [System.Diagnostics.Stopwatch]::StartNew()
        $process = Start-Process -FilePath (Get-Command java).Source -ArgumentList $arguments `
            -WorkingDirectory $repositoryRoot -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr -PassThru -WindowStyle Hidden
        $peaks = @{}
        $samples = 0
        $lastSampleMs = 0
        $maxGapMs = 0
        while (-not $process.HasExited) {
            $nowMs = $timer.ElapsedMilliseconds
            if ($samples -gt 0) { $maxGapMs = [Math]::Max($maxGapMs, $nowMs - $lastSampleMs) }
            $lastSampleMs = $nowMs
            $samples++
            foreach ($javaProcess in @(Get-Process -Name java -ErrorAction SilentlyContinue)) {
                $id = [string]$javaProcess.Id
                $value = [long]$javaProcess.PeakWorkingSet64
                if (-not $peaks.ContainsKey($id) -or $value -gt $peaks[$id]) { $peaks[$id] = $value }
            }
            if ($timer.Elapsed.TotalSeconds -gt 240) {
                Stop-Process -Id $process.Id -Force
                throw "CODE-03 coordinator exceeded 240 seconds: $($run.Name)"
            }
            Start-Sleep -Milliseconds 100
            $process.Refresh()
        }
        $process.WaitForExit()
        $timer.Stop()
        $data = $null
        if (Test-Path -LiteralPath $result) { $data = Get-Content -LiteralPath $result -Raw | ConvertFrom-Json }
        $memory = [ordered]@{
            schema = 'code03-os-memory-v1'
            run = $run.Name
            warmup = $run.Warmup
            started_utc = $started.ToString('o')
            elapsed_ms = $timer.ElapsedMilliseconds
            coordinator_exit_code = $process.ExitCode
            sampler = 'PowerShell Get-Process PeakWorkingSet64; approximately 100 ms polling'
            sample_count = $samples
            max_sample_gap_ms = $maxGapMs
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
        $memory | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath "$base.os-memory.json" -Encoding UTF8
        Get-Content -LiteralPath $stdout
        if (($null -ne $process.ExitCode -and $process.ExitCode -ne 0) -or
            $null -eq $data -or $data.outcome -ne 'passed' -or
            $data.verified_files -ne $fileCount) {
            if (Test-Path -LiteralPath $stderr) { Get-Content -LiteralPath $stderr }
            throw "CODE-03 run failed: $($run.Name); inspect $base files"
        }
    }
    Write-Output 'CODE03_PREFLIGHT=passed measured_attempts=8 warmup=1 campaign_shape_checks=1'
} finally {
    Pop-Location
}
