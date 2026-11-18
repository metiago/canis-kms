param(
    [ValidateSet('smoke', 'campaign')][string]$Scope = 'campaign',
    [string]$OutputDirectory = 'server/target/issue32-campaign',
    [string]$ExportDirectory = ''
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Push-Location $repositoryRoot
try {
    $jar = Join-Path $repositoryRoot 'server/target/canis-jar-with-dependencies.jar'
    $clientClasses = Join-Path $repositoryRoot 'client/target/classes'
    if (-not (Test-Path -LiteralPath $jar) -or -not (Test-Path -LiteralPath $clientClasses)) {
        throw 'Run mvn clean verify before Issue32'
    }
    $classes = Join-Path $repositoryRoot 'server/target/issue32-classes'
    New-Item -ItemType Directory -Force -Path $classes | Out-Null
    $compileClasspath = "$jar;$clientClasses"
    javac -cp $compileClasspath -d $classes docs/research/Code02Harness.java docs/research/Issue32Harness.java
    if ($LASTEXITCODE -ne 0) { throw 'Issue32 research compilation failed' }
    $runtimeClasspath = "$classes;$jar;$clientClasses"
    java -cp $runtimeClasspath Issue32Harness self-test
    if ($LASTEXITCODE -ne 0) { throw 'Issue32 self-test failed' }

    $output = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $OutputDirectory))
    $targetRoot = [IO.Path]::GetFullPath((Join-Path $repositoryRoot 'server/target'))
    if (-not $output.StartsWith($targetRoot + [IO.Path]::DirectorySeparatorChar,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Issue32 output must be a new directory under server/target'
    }
    if (Test-Path -LiteralPath $output) { throw 'Issue32 output directory already exists' }
    New-Item -ItemType Directory -Path $output | Out-Null

    $javaVersionText = (java --version | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Could not record Java version' }

    $environment = [ordered]@{
        schema = 'issue32-environment-v1'
        recorded_utc = [DateTime]::UtcNow.ToString('o')
        source_revision = (git rev-parse HEAD).Trim()
        dirty_tree = -not [string]::IsNullOrWhiteSpace((git status --porcelain | Out-String))
        os = [Environment]::OSVersion.VersionString
        java_version = $javaVersionText
        maven_version = (mvn --version | Out-String).Trim()
        processor = $null
        logical_processors = [Environment]::ProcessorCount
        total_ram_bytes = $null
        filesystem = $null
        java_flags = '-Xms128m -Xmx512m'
        server_jar_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $jar).Hash.ToLowerInvariant()
        issue32_source_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath 'docs/research/Issue32Harness.java').Hash.ToLowerInvariant()
        code02_source_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath 'docs/research/Code02Harness.java').Hash.ToLowerInvariant()
        sampler = 'Windows Get-Process WorkingSet64 and PeakWorkingSet64 approximately every 100 ms'
        clock = 'System.nanoTime in workers; UTC wall clock across processes'
        tls = 'TLS 1.3 mTLS, existing JSSE cipher policy; measured negotiated suite not observed'
        inbound_firewall_profiles = $null
        java_firewall_program_filter_count = $null
    }
    try {
        $processor = Get-CimInstance Win32_Processor | Select-Object -First 1
        $environment.processor = $processor.Name
        $environment.logical_processors = [int]$processor.NumberOfLogicalProcessors
        $computer = Get-CimInstance Win32_ComputerSystem
        $environment.total_ram_bytes = [long]$computer.TotalPhysicalMemory
        $drive = Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='C:'" | Select-Object -First 1
        if ($null -ne $drive) { $environment.filesystem = $drive.FileSystem }
    } catch {
        # Missing host metadata remains null rather than an inferred value.
    }
    try {
        $environment.inbound_firewall_profiles = @(Get-NetFirewallProfile -PolicyStore ActiveStore |
            Select-Object Name,Enabled,DefaultInboundAction)
        $environment.java_firewall_program_filter_count = @(
            Get-NetFirewallApplicationFilter | Where-Object { $_.Program -match 'java(w)?\.exe' }
        ).Count
    } catch {
        # Firewall state is evidence only when the host permits a read-only query.
    }
    $environment | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $output 'environment.json') -Encoding UTF8

    $schedule = New-Object System.Collections.Generic.List[object]
    if ($Scope -eq 'smoke') {
        $schedule.Add([pscustomobject]@{ Id = 'issue32-smoke'; Files = 1; Repetition = 1;
                Delay = 0; Workers = 1; Order = 'AB'; Warmup = $false })
    } else {
        foreach ($setting in @(@(0,1), @(0,4), @(60000,1), @(60000,4))) {
            $schedule.Add([pscustomobject]@{ Id = "issue32-w-d$($setting[0])-c$($setting[1])";
                    Files = 20; Repetition = 1; Delay = $setting[0]; Workers = $setting[1];
                    Order = 'AB'; Warmup = $true })
        }
        foreach ($repetition in 1..5) {
            $settings = New-Object System.Collections.Generic.List[object]
            foreach ($setting in @(@(0,1), @(0,4), @(60000,1), @(60000,4))) {
                $settings.Add([pscustomobject]@{ Delay = $setting[0]; Workers = $setting[1] })
            }
            $random = [Random]::new(32000 + $repetition)
            for ($i = $settings.Count - 1; $i -gt 0; $i--) {
                $j = $random.Next($i + 1)
                $previous = $settings[$i]
                $settings[$i] = $settings[$j]
                $settings[$j] = $previous
            }
            for ($index = 0; $index -lt $settings.Count; $index++) {
                $setting = $settings[$index]
                $order = if (($index + $repetition) % 2 -eq 0) { 'AB' } else { 'BA' }
                $schedule.Add([pscustomobject]@{
                    Id = "issue32-r$repetition-d$($setting.Delay)-c$($setting.Workers)"
                    Files = 100; Repetition = $repetition; Delay = $setting.Delay
                    Workers = $setting.Workers; Order = $order; Warmup = $false
                })
            }
        }
    }
    $schedulePath = Join-Path $output 'schedule.csv'
    'pair_id,warmup,repetition,delay_ms,workers,order,planned_files,pre_cpu_load_percent,pre_free_ram_bytes,started_utc,finished_utc,coordinator_exit_code,pair_status,samples,max_sample_gap_ms' |
        Set-Content -LiteralPath $schedulePath -Encoding ASCII

    foreach ($entry in $schedule) {
        $name = $entry.Id
        $cpuLoad = ''
        $freeRam = ''
        try {
            $cpuLoad = [string]((Get-CimInstance Win32_Processor | Measure-Object -Property LoadPercentage -Average).Average)
            $freeRam = [string]([long](Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory * 1024)
        } catch { }
        $stdout = Join-Path $output "$name.stdout.txt"
        $stderr = Join-Path $output "$name.stderr.txt"
        $pidsPath = Join-Path $output "$name.pids.csv"
        $javaArgs = @('-cp', "`"$runtimeClasspath`"", 'Issue32Harness', 'pair', $name,
            [string]$entry.Files, [string]$entry.Repetition, [string]$entry.Delay,
            [string]$entry.Workers, $entry.Order, [string]$entry.Warmup.ToString().ToLowerInvariant(),
            "`"$output`"")
        $started = [DateTime]::UtcNow
        $timer = [Diagnostics.Stopwatch]::StartNew()
        $process = Start-Process -FilePath (Get-Command java).Source -ArgumentList $javaArgs `
            -WorkingDirectory $repositoryRoot -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr -PassThru -WindowStyle Hidden
        $peaks = @{}
        $currentPeaks = @{}
        $hits = @{}
        $samples = 0
        $lastSample = 0
        $maxGap = 0
        while (-not $process.HasExited) {
            $now = $timer.ElapsedMilliseconds
            if ($samples -gt 0) { $maxGap = [Math]::Max($maxGap, $now - $lastSample) }
            $lastSample = $now
            $samples++
            if (Test-Path -LiteralPath $pidsPath) {
                try { $pidRows = @(Import-Csv -LiteralPath $pidsPath) } catch { $pidRows = @() }
                foreach ($pidRow in $pidRows) {
                    $key = "$($pidRow.run_id)|$($pidRow.role)|$($pidRow.pid)"
                    $observed = Get-Process -Id ([int]$pidRow.pid) -ErrorAction SilentlyContinue
                    if ($null -ne $observed) {
                        try {
                            $peak = [long]$observed.PeakWorkingSet64
                            $current = [long]$observed.WorkingSet64
                            if (-not $peaks.ContainsKey($key) -or $peak -gt $peaks[$key]) { $peaks[$key] = $peak }
                            if (-not $currentPeaks.ContainsKey($key) -or $current -gt $currentPeaks[$key]) {
                                $currentPeaks[$key] = $current
                            }
                            if (-not $hits.ContainsKey($key)) { $hits[$key] = 0 }
                            $hits[$key]++
                        } catch { }
                    }
                }
            }
            if ($timer.Elapsed.TotalSeconds -gt 3900) {
                Stop-Process -Id $process.Id -Force
                throw "Issue32 pair deadline exceeded: $name"
            }
            Start-Sleep -Milliseconds 100
            $process.Refresh()
        }
        $process.WaitForExit()
        $timer.Stop()
        $finished = [DateTime]::UtcNow
        $stdoutText = if (Test-Path -LiteralPath $stdout) { Get-Content -LiteralPath $stdout -Raw } else { '' }
        $aResult = Join-Path $output "$name-A.json"
        $bResult = Join-Path $output "$name-B.json"
        $pairPassed = $stdoutText.Contains("ISSUE32_PAIR=passed id=$name ") -and
            (Test-Path -LiteralPath $aResult) -and (Test-Path -LiteralPath $bResult)
        if ($pairPassed) {
            $pairPassed = ((Get-Content -LiteralPath $aResult -Raw | ConvertFrom-Json).status -eq 'passed') -and
                ((Get-Content -LiteralPath $bResult -Raw | ConvertFrom-Json).status -eq 'passed')
        }
        if ($null -ne $process.ExitCode -and $process.ExitCode -ne 0) { $pairPassed = $false }
        $pairStatus = if ($pairPassed) { 'passed' } else { 'failed' }
        $exitText = if ($null -eq $process.ExitCode) { '' } else { [string]$process.ExitCode }
        $pidRows = if (Test-Path -LiteralPath $pidsPath) { @(Import-Csv -LiteralPath $pidsPath) } else { @() }
        foreach ($mode in @('A', 'B')) {
            $runId = "$name-$mode"
            $memory = [ordered]@{
                schema = 'issue32-os-memory-v1'; run_id = $runId
                pair_sample_count = $samples; pair_max_sample_gap_ms = $maxGap
            }
            foreach ($role in @('server', 'producer', 'consumer')) {
                $matching = @($pidRows | Where-Object { $_.run_id -eq $runId -and $_.role -eq $role })
                $memory["${role}_pid"] = $null
                $memory["${role}_peak_working_set_bytes"] = $null
                $memory["${role}_sampled_working_set_peak_bytes"] = $null
                $memory["${role}_sample_count"] = 0
                if ($matching.Count -eq 1) {
                    $pidText = $matching[0].pid
                    $key = "$runId|$role|$pidText"
                    $memory["${role}_pid"] = [long]$pidText
                    if ($peaks.ContainsKey($key)) {
                        $memory["${role}_peak_working_set_bytes"] = $peaks[$key]
                        $memory["${role}_sampled_working_set_peak_bytes"] = $currentPeaks[$key]
                        $memory["${role}_sample_count"] = $hits[$key]
                    }
                }
            }
            $memory | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $output "$runId.memory.json") -Encoding UTF8
        }
        $scheduleLine = "$name,$($entry.Warmup.ToString().ToLowerInvariant()),$($entry.Repetition)," +
            "$($entry.Delay),$($entry.Workers),$($entry.Order),$($entry.Files),$cpuLoad,$freeRam," +
            "$($started.ToString('o')),$($finished.ToString('o')),$exitText,$pairStatus,$samples,$maxGap"
        Add-Content -LiteralPath $schedulePath -Value $scheduleLine -Encoding ASCII
        Get-Content -LiteralPath $stdout
        if (-not $pairPassed) {
            if (Test-Path -LiteralPath $stderr) { Get-Content -LiteralPath $stderr }
            throw "Issue32 pair failed: $name. Raw records remain in $output"
        }
    }

    if ($ExportDirectory -ne '') {
        $export = [IO.Path]::GetFullPath((Join-Path $repositoryRoot $ExportDirectory))
        $researchRoot = [IO.Path]::GetFullPath((Join-Path $repositoryRoot 'docs/research'))
        if (-not $export.StartsWith($researchRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase) -or (Test-Path -LiteralPath $export)) {
            throw 'Export must be a new directory under docs/research'
        }
        New-Item -ItemType Directory -Path $export | Out-Null
        foreach ($file in Get-ChildItem -LiteralPath $output -File) {
            if ($file.Extension -in @('.csv', '.json') -or $file.Name -match '-heap\.txt$') {
                Copy-Item -LiteralPath $file.FullName -Destination (Join-Path $export $file.Name)
            }
        }
    }
    Write-Output "ISSUE32_CAMPAIGN=recorded pairs=$($schedule.Count) runs=$($schedule.Count * 2) scope=$Scope"
} finally {
    Pop-Location
}
