"""Reproduce descriptive Issue #31 CANIS baseline summaries from sanitized CSVs."""

import csv
import json
import statistics
from collections import Counter, defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def read(name):
    with (ROOT / name).open(newline="", encoding="utf-8-sig") as stream:
        return list(csv.DictReader(stream))


def nearest_rank(values, percentile):
    ordered = sorted(values)
    return ordered[max(0, (len(ordered) * percentile + 99) // 100 - 1)]


def span(values):
    return {"median": statistics.median(values), "min": min(values), "max": max(values)}


def main():
    runs = read("issue31-baseline-runs.csv")
    files = read("issue31-baseline-files.csv")
    assert len(runs) == 24 and len(files) == 2080
    by_run = defaultdict(list)
    for row in files:
        by_run[row["run"]].append(row)
    summary = {}
    for delay in (0, 60000):
        for workers in (1, 4):
            group = [
                row for row in runs
                if row["warmup"] == "False"
                and int(row["delay_ms"]) == delay
                and int(row["concurrency"]) == workers
            ]
            assert len(group) == 5
            all_files = [file for run in group for file in by_run[run["run"]]]
            assert len(all_files) == 500
            assert Counter(int(file["size_bytes"]) for file in all_files) == {
                4096: 250, 262144: 175, 4194304: 75
            }
            assert all(run["outcome"] == "passed" and int(run["verified_files"]) == 100 for run in group)
            assert all(file["producer_outcome"] == "published" and
                       file["consumer_outcome"] == "verified" and
                       file["failure_category"] == "none" for file in all_files)
            run_medians = [statistics.median(float(file["processing_ms"]) for file in by_run[run["run"]])
                           for run in group]
            end_to_end = [float(file["end_to_end_ms"]) for file in all_files]
            processing = [float(file["processing_ms"]) for file in all_files]
            queue = [float(file["queue_wait_ms"]) for file in all_files]
            key = f"delay_{delay}_workers_{workers}"
            summary[key] = {
                "run_count": 5,
                "verified_files": 500,
                "interval_s": span([float(run["measured_interval_s"]) for run in group]),
                "files_per_s": span([float(run["verified_files_per_s"]) for run in group]),
                "mib_per_s": span([float(run["verified_mib_per_s"]) for run in group]),
                "processing_run_median_ms": span(run_medians),
                "processing_ms_p50": statistics.median(processing),
                "processing_ms_p95": nearest_rank(processing, 95),
                "end_to_end_ms_p50": statistics.median(end_to_end),
                "end_to_end_ms_p95": nearest_rank(end_to_end, 95),
                "queue_wait_ms_p50": statistics.median(queue),
                "queue_wait_ms_p95": nearest_rank(queue, 95),
                "server_peak_working_set_bytes": span([int(run["server_peak_working_set_bytes"]) for run in group]),
                "producer_peak_working_set_bytes": span([int(run["producer_peak_working_set_bytes"]) for run in group]),
                "consumer_peak_working_set_bytes": span([int(run["consumer_peak_working_set_bytes"]) for run in group]),
                "max_sampler_gap_ms": max(int(run["max_sample_gap_ms"]) for run in group),
            }
    warmups = [row for row in runs if row["warmup"] == "True"]
    assert len(warmups) == 4
    for run in warmups:
        assert run["outcome"] == "passed" and int(run["verified_files"]) == 20
        assert Counter(int(file["size_bytes"]) for file in by_run[run["run"]]) == {
            4096: 10, 262144: 7, 4194304: 3
        }
    print(json.dumps(summary, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
