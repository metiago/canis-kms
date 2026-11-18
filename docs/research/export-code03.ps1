param(
    [string]$InputDirectory,
    [string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
if (-not $InputDirectory) { $InputDirectory = Join-Path $repositoryRoot 'server/target/code03-preflight' }
if (-not $OutputDirectory) { $OutputDirectory = $PSScriptRoot }
$expected = @('warmup-4m')
foreach ($size in @(4096, 262144, 1048576, 4194304)) {
    foreach ($repetition in @(1, 2)) { $expected += "size-$size-r$repetition" }
}
$expected += 'campaign-shape-r1'
$runs = @()
$files = @()
foreach ($name in $expected) {
    $base = Join-Path $InputDirectory $name
    $jsonPath = "$base.json"
    $producerPath = "$base.producer.csv"
    $consumerPath = "$base.consumer.csv"
    $memoryPath = "$base.os-memory.json"
    foreach ($path in @($jsonPath, $producerPath, $consumerPath, $memoryPath)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing CODE-03 artifact: $path" }
    }
    $json = Get-Content -LiteralPath $jsonPath -Raw | ConvertFrom-Json
    $memory = Get-Content -LiteralPath $memoryPath -Raw | ConvertFrom-Json
    $producer = @(Import-Csv -LiteralPath $producerPath)
    $consumer = @(Import-Csv -LiteralPath $consumerPath)
    if ($json.outcome -ne 'passed' -or $json.verified_files -ne $json.file_count -or
        $producer.Count -ne $json.file_count -or $consumer.Count -ne $json.file_count) {
        throw "Incomplete CODE-03 artifact: $name"
    }
    $runs += [pscustomobject][ordered]@{
        run = $name; warmup = $memory.warmup; commit = $json.commit; dirty_tree = $json.dirty_tree
        timestamp_utc = $json.timestamp_utc; file_size_bytes = $json.file_size_bytes
        size_schedule = $json.size_schedule; file_count = $json.file_count
        repetition = $json.repetition; concurrency = $json.concurrency
        delay_ms = $json.delay_ms; interval_ms = $json.interval_ms
        outcome = $json.outcome; failure_category = $json.failure_category
        verified_files = $json.verified_files; producer_elapsed_ms = $json.producer_elapsed_ms
        consumer_elapsed_ms = $json.consumer_elapsed_ms
        producer_heap_used_bytes_at_exit = $json.producer_heap_used_bytes_at_exit
        consumer_heap_used_bytes_at_exit = $json.consumer_heap_used_bytes_at_exit
        server_peak_working_set_bytes = $memory.server_peak_working_set_bytes
        producer_peak_working_set_bytes = $memory.producer_peak_working_set_bytes
        consumer_peak_working_set_bytes = $memory.consumer_peak_working_set_bytes
        sample_count = $memory.sample_count; max_sample_gap_ms = $memory.max_sample_gap_ms
        coordinator_exit_code = $memory.coordinator_exit_code
        wall_elapsed_ms = $memory.elapsed_ms
        json_sha256 = (Get-FileHash -LiteralPath $jsonPath -Algorithm SHA256).Hash
        producer_csv_sha256 = (Get-FileHash -LiteralPath $producerPath -Algorithm SHA256).Hash
        consumer_csv_sha256 = (Get-FileHash -LiteralPath $consumerPath -Algorithm SHA256).Hash
        os_memory_sha256 = (Get-FileHash -LiteralPath $memoryPath -Algorithm SHA256).Hash
    }
    for ($i = 0; $i -lt $json.file_count; $i++) {
        if ([int]$producer[$i].ordinal -ne $i -or [int]$consumer[$i].ordinal -ne $i) {
            throw "Ordinal mismatch in $name"
        }
        $files += [pscustomobject][ordered]@{
            run = $name; ordinal = $i; size_bytes = $producer[$i].size
            planned_start_utc = $producer[$i].planned_start_utc
            actual_start_utc = $producer[$i].actual_start_utc
            published_utc = $producer[$i].published_utc
            encrypt_ns = $producer[$i].encrypt_ns
            producer_outcome = $producer[$i].outcome
            claimed_utc = $consumer[$i].claimed_utc
            finished_utc = $consumer[$i].finished_utc
            decrypt_ns = $consumer[$i].decrypt_ns
            consumer_outcome = $consumer[$i].outcome
            failure_category = $consumer[$i].failure_category
        }
    }
}
$runOutput = Join-Path $OutputDirectory 'code03-runs.csv'
$fileOutput = Join-Path $OutputDirectory 'code03-files.csv'
if ((Test-Path -LiteralPath $runOutput) -or (Test-Path -LiteralPath $fileOutput)) {
    throw 'CODE-03 export destination already has results'
}
$runs | Export-Csv -LiteralPath $runOutput -NoTypeInformation -Encoding UTF8
$files | Export-Csv -LiteralPath $fileOutput -NoTypeInformation -Encoding UTF8
Write-Output "CODE03_EXPORT=runs:$($runs.Count) files:$($files.Count)"
