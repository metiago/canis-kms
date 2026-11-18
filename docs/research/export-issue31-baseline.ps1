$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$inputDir = Join-Path $repositoryRoot 'server/target/issue31-baseline'
$researchDir = Join-Path $repositoryRoot 'docs/research'
$runs = [System.Collections.Generic.List[object]]::new()
$files = [System.Collections.Generic.List[object]]::new()
foreach ($delay in @(0, 60000)) {
    foreach ($workers in @(1, 4)) {
        foreach ($repetition in 0..5) {
            $warmup = $repetition -eq 0
            $label = if ($warmup) { 'warmup' } else { "r$repetition" }
            $name = "d$delay-c$workers-$label"
            $base = Join-Path $inputDir $name
            foreach ($suffix in @('.json', '.producer.csv', '.consumer.csv', '.os-memory.json')) {
                if (-not (Test-Path -LiteralPath "$base$suffix")) { throw "Missing raw record: $name$suffix" }
            }
            $data = Get-Content -LiteralPath "$base.json" -Raw | ConvertFrom-Json
            $memory = Get-Content -LiteralPath "$base.os-memory.json" -Raw | ConvertFrom-Json
            $producer = @(Import-Csv -LiteralPath "$base.producer.csv")
            $consumer = @(Import-Csv -LiteralPath "$base.consumer.csv")
            $expected = if ($warmup) { 20 } else { 100 }
            if ($producer.Count -ne $expected -or $consumer.Count -ne $expected -or
                $data.file_count -ne $expected -or $data.repetition -ne $(if ($warmup) { 1 } else { $repetition }) -or
                $data.concurrency -ne $workers -or $data.delay_ms -ne $delay) {
                throw "Unexpected workload shape: $name"
            }
            $firstStart = [DateTimeOffset]::Parse($producer[0].planned_start_utc)
            $lastFinish = $null
            $verifiedBytes = [long]0
            for ($i = 0; $i -lt $expected; $i++) {
                $p = $producer[$i]
                $c = $consumer[$i]
                if ([int]$p.ordinal -ne $i -or [int]$c.ordinal -ne $i -or
                    [int]$p.size -ne [int]$c.size) { throw "File row mismatch: $name ordinal $i" }
                $planned = [DateTimeOffset]::Parse($p.planned_start_utc)
                $actual = [DateTimeOffset]::Parse($p.actual_start_utc)
                $published = [DateTimeOffset]::Parse($p.published_utc)
                $claimed = [DateTimeOffset]::Parse($c.claimed_utc)
                $finished = [DateTimeOffset]::Parse($c.finished_utc)
                if ($null -eq $lastFinish -or $finished -gt $lastFinish) { $lastFinish = $finished }
                if ($c.outcome -eq 'verified') { $verifiedBytes += [long]$p.size }
                $files.Add([pscustomobject][ordered]@{
                    run = $name; warmup = $warmup; repetition = $repetition
                    delay_ms = $delay; concurrency = $workers; ordinal = $i; size_bytes = [int]$p.size
                    planned_start_utc = $p.planned_start_utc; actual_start_utc = $p.actual_start_utc
                    published_utc = $p.published_utc; claimed_utc = $c.claimed_utc
                    finished_utc = $c.finished_utc
                    schedule_lag_ms = [Math]::Round(($actual - $planned).TotalMilliseconds, 3)
                    end_to_end_ms = [Math]::Round(($finished - $planned).TotalMilliseconds, 3)
                    queue_wait_ms = [Math]::Round(($claimed - $published).TotalMilliseconds, 3)
                    processing_ms = [Math]::Round(($finished - $claimed).TotalMilliseconds, 3)
                    encrypt_ns = $p.encrypt_ns; decrypt_ns = $c.decrypt_ns
                    producer_outcome = $p.outcome; consumer_outcome = $c.outcome
                    failure_category = $c.failure_category
                })
            }
            $interval = ($lastFinish - $firstStart).TotalSeconds
            if ($interval -le 0) { throw "Invalid measured interval: $name" }
            $runs.Add([pscustomobject][ordered]@{
                run = $name; warmup = $warmup; repetition = $repetition
                delay_ms = $delay; concurrency = $workers; commit = $data.commit
                dirty_tree = $data.dirty_tree; jdk_version = $data.jdk_version
                planned_files = $expected; verified_files = $data.verified_files
                outcome = $data.outcome; failure_category = $data.failure_category
                first_planned_start_utc = $producer[0].planned_start_utc
                last_finish_utc = $lastFinish.ToString('o')
                measured_interval_s = [Math]::Round($interval, 6)
                verified_files_per_s = [Math]::Round($data.verified_files / $interval, 6)
                verified_mib_per_s = [Math]::Round(($verifiedBytes / 1MB) / $interval, 6)
                producer_elapsed_ms = $data.producer_elapsed_ms
                consumer_elapsed_ms = $data.consumer_elapsed_ms
                server_peak_working_set_bytes = $memory.server_peak_working_set_bytes
                producer_peak_working_set_bytes = $memory.producer_peak_working_set_bytes
                consumer_peak_working_set_bytes = $memory.consumer_peak_working_set_bytes
                producer_heap_used_bytes_at_exit = $data.producer_heap_used_bytes_at_exit
                consumer_heap_used_bytes_at_exit = $data.consumer_heap_used_bytes_at_exit
                os_sample_count = $memory.sample_count; max_sample_gap_ms = $memory.max_sample_gap_ms
                result_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath "$base.json").Hash
                producer_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath "$base.producer.csv").Hash
                consumer_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath "$base.consumer.csv").Hash
                os_memory_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath "$base.os-memory.json").Hash
            })
        }
    }
}
$runs | Export-Csv -LiteralPath (Join-Path $researchDir 'issue31-baseline-runs.csv') -NoTypeInformation -Encoding UTF8
$files | Export-Csv -LiteralPath (Join-Path $researchDir 'issue31-baseline-files.csv') -NoTypeInformation -Encoding UTF8
Write-Output "ISSUE31_EXPORT=runs:$($runs.Count) files:$($files.Count)"
