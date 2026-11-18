#!/usr/bin/env python3
"""Recompute Issue #32 descriptive observations from sanitized raw records."""

import argparse
import csv
import json
import math
import statistics
from collections import defaultdict
from datetime import datetime
from pathlib import Path


def read_json(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def read_csv(path):
    with path.open(newline="", encoding="utf-8-sig") as stream:
        return list(csv.DictReader(stream))


def instant(value):
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def median(values):
    return statistics.median(values) if values else None


def spread(values):
    if not values:
        return {"median": None, "minimum": None, "maximum": None, "sample_sd": None}
    return {
        "median": median(values),
        "minimum": min(values),
        "maximum": max(values),
        "sample_sd": statistics.stdev(values) if len(values) > 1 else None,
    }


def nearest_rank(values, fraction):
    if not values:
        return None
    ordered = sorted(values)
    rank = max(1, math.ceil(fraction * len(ordered)))
    return ordered[rank - 1]


def write_csv(path, rows, fields):
    with path.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def collect(directory):
    schedule = read_csv(directory / "schedule.csv")
    runs = []
    files = []
    pairs = []
    for setting in schedule:
        pair_id = setting["pair_id"]
        per_pair = {}
        inputs = {int(row["ordinal"]): row for row in read_csv(directory / f"{pair_id}.inputs.csv")}
        for mode in ("A", "B"):
            run_id = f"{pair_id}-{mode}"
            raw = read_json(directory / f"{run_id}.json")
            memory = read_json(directory / f"{run_id}.memory.json")
            producer = {int(row["ordinal"]): row for row in read_csv(directory / f"{run_id}.producer.csv")}
            consumer = {int(row["ordinal"]): row for row in read_csv(directory / f"{run_id}.consumer.csv")}
            producer_connections = read_csv(directory / f"{run_id}.producer-connections.csv")
            consumer_connections = read_csv(directory / f"{run_id}.consumer-connections.csv")
            latencies = []
            processing = []
            verified_bytes = 0
            last_finish = None
            first_planned = None
            for ordinal in sorted(inputs):
                source = inputs[ordinal]
                p = producer.get(ordinal)
                c = consumer.get(ordinal)
                if p is None or c is None:
                    continue
                if first_planned is None:
                    first_planned = instant(p["planned_start_utc"])
                matched = c["outcome"] == "verified" and p["outcome"] == "published"
                e2e = process_ms = queue = None
                if matched:
                    finish = instant(c["finished_utc"])
                    claim = instant(c["claimed_utc"])
                    publication = instant(p["published_utc"])
                    e2e = (finish - instant(p["planned_start_utc"])).total_seconds() * 1000
                    process_ms = (finish - claim).total_seconds() * 1000
                    queue = (claim - publication).total_seconds() * 1000
                    latencies.append(e2e)
                    processing.append(process_ms)
                    verified_bytes += int(source["size"])
                    if last_finish is None or finish > last_finish:
                        last_finish = finish
                files.append({
                    "run_id": run_id, "mode": mode, "warmup": raw["warmup"],
                    "repetition": raw["repetition"], "delay_ms": raw["delay_ms"],
                    "workers": raw["workers"], "ordinal": ordinal, "size_bytes": source["size"],
                    "producer_outcome": p["outcome"], "consumer_outcome": c["outcome"],
                    "failure_category": c["failure_category"],
                    "end_to_end_ms": e2e, "processing_ms": process_ms, "queue_wait_ms": queue,
                    "fetch_ms": int(p["fetch_ns"]) / 1_000_000 if int(p["fetch_ns"]) >= 0 else None,
                    "local_envelope_ms": int(p["local_envelope_ns"]) / 1_000_000
                    if int(p["local_envelope_ns"]) >= 0 else None,
                    "unwrap_ms": int(c["unwrap_ns"]) / 1_000_000
                    if int(c["unwrap_ns"]) >= 0 else None,
                    "local_decrypt_verify_ms": int(c["local_decrypt_verify_ns"]) / 1_000_000
                    if int(c["local_decrypt_verify_ns"]) >= 0 else None,
                })
            complete = raw["status"] == "passed" and raw["successful_files"] == raw["planned_files"]
            interval = (last_finish - first_planned).total_seconds() if complete and first_planned and last_finish else None
            run_files = [row for row in files if row["run_id"] == run_id]
            def file_median(field):
                return median([row[field] for row in run_files if row[field] is not None])
            def connection_median(connection_rows):
                return median([int(row["connect_login_ns"]) / 1_000_000 for row in connection_rows
                               if row["outcome"] == "connected"])
            run = {
                "run_id": run_id, "pair_id": pair_id, "mode": mode,
                "warmup": raw["warmup"], "repetition": raw["repetition"],
                "delay_ms": raw["delay_ms"], "workers": raw["workers"],
                "status": raw["status"], "failure_category": raw["failure_category"],
                "planned_files": raw["planned_files"], "successful_files": raw["successful_files"],
                "failed_files": raw["failed_files"], "run_duration_ms": raw["run_duration_ms"],
                "median_e2e_ms": median(latencies), "median_processing_ms": median(processing),
                "median_fetch_ms": file_median("fetch_ms"),
                "median_local_envelope_ms": file_median("local_envelope_ms"),
                "median_unwrap_ms": file_median("unwrap_ms"),
                "median_local_decrypt_verify_ms": file_median("local_decrypt_verify_ms"),
                "producer_connect_login_ms": connection_median(producer_connections),
                "consumer_connect_login_median_ms": connection_median(consumer_connections),
                "throughput_files_s": raw["successful_files"] / interval if interval and interval > 0 else None,
                "throughput_mib_s": verified_bytes / 1048576 / interval if interval and interval > 0 else None,
                "server_peak_working_set_bytes": memory.get("server_peak_working_set_bytes"),
                "producer_peak_working_set_bytes": memory.get("producer_peak_working_set_bytes"),
                "consumer_peak_working_set_bytes": memory.get("consumer_peak_working_set_bytes"),
                "producer_heap_peak_bytes": int((directory / f"{run_id}.producer-heap.txt").read_text().strip())
                if (directory / f"{run_id}.producer-heap.txt").exists() else None,
                "consumer_heap_peak_bytes": int((directory / f"{run_id}.consumer-heap.txt").read_text().strip())
                if (directory / f"{run_id}.consumer-heap.txt").exists() else None,
                "server_memory_samples": memory.get("server_sample_count"),
                "producer_memory_samples": memory.get("producer_sample_count"),
                "consumer_memory_samples": memory.get("consumer_sample_count"),
                "max_sample_gap_ms": memory.get("pair_max_sample_gap_ms"),
            }
            runs.append(run)
            per_pair[mode] = run
        a, b = per_pair["A"], per_pair["B"]
        paired = a["status"] == "passed" and b["status"] == "passed" and not a["warmup"]
        pairs.append({
            "pair_id": pair_id, "repetition": a["repetition"], "delay_ms": a["delay_ms"],
            "workers": a["workers"], "warmup": a["warmup"], "paired": paired,
            "latency_difference_ms": a["median_e2e_ms"] - b["median_e2e_ms"] if paired else None,
            "fetch_difference_ms": a["median_fetch_ms"] - b["median_fetch_ms"] if paired else None,
            "unwrap_difference_ms": a["median_unwrap_ms"] - b["median_unwrap_ms"] if paired else None,
            "producer_connect_difference_ms": a["producer_connect_login_ms"] - b["producer_connect_login_ms"]
            if paired else None,
            "consumer_connect_difference_ms": a["consumer_connect_login_median_ms"]
            - b["consumer_connect_login_median_ms"] if paired else None,
            "throughput_difference_files_s": a["throughput_files_s"] - b["throughput_files_s"] if paired else None,
            "throughput_difference_mib_s": a["throughput_mib_s"] - b["throughput_mib_s"] if paired else None,
            "server_memory_difference_bytes": a["server_peak_working_set_bytes"] - b["server_peak_working_set_bytes"]
            if paired and a["server_peak_working_set_bytes"] is not None and b["server_peak_working_set_bytes"] is not None else None,
            "producer_memory_difference_bytes": a["producer_peak_working_set_bytes"] - b["producer_peak_working_set_bytes"]
            if paired and a["producer_peak_working_set_bytes"] is not None and b["producer_peak_working_set_bytes"] is not None else None,
            "consumer_memory_difference_bytes": a["consumer_peak_working_set_bytes"] - b["consumer_peak_working_set_bytes"]
            if paired and a["consumer_peak_working_set_bytes"] is not None and b["consumer_peak_working_set_bytes"] is not None else None,
        })
    return schedule, runs, files, pairs


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    directory = args.directory
    schedule, runs, files, pairs = collect(directory)
    write_csv(directory / "analysis-runs.csv", runs, list(runs[0]))
    write_csv(directory / "analysis-files.csv", files, list(files[0]))
    write_csv(directory / "analysis-pairs.csv", pairs, list(pairs[0]))
    grouped = defaultdict(list)
    for run in runs:
        if not run["warmup"]:
            grouped[(run["delay_ms"], run["workers"], run["mode"])].append(run)
    summary = {
        "schema": "issue32-analysis-v1",
        "measured_runs": sum(not run["warmup"] for run in runs),
        "warmup_runs": sum(run["warmup"] for run in runs),
        "measured_pairs": sum(pair["paired"] for pair in pairs),
        "groups": [],
        "size_groups": [],
        "paired_groups": [],
    }
    for (delay, workers, mode), group in sorted(grouped.items()):
        valid = [run for run in group if run["status"] == "passed"]
        summary["groups"].append({
            "delay_ms": delay, "workers": workers, "mode": mode,
            "runs": len(group), "successful_files": sum(run["successful_files"] for run in group),
            "failed_files": sum(run["failed_files"] for run in group),
            "latency_ms": spread([run["median_e2e_ms"] for run in valid if run["median_e2e_ms"] is not None]),
            "fetch_ms": spread([run["median_fetch_ms"] for run in valid if run["median_fetch_ms"] is not None]),
            "unwrap_ms": spread([run["median_unwrap_ms"] for run in valid if run["median_unwrap_ms"] is not None]),
            "local_envelope_ms": spread([run["median_local_envelope_ms"] for run in valid
                                         if run["median_local_envelope_ms"] is not None]),
            "local_decrypt_verify_ms": spread([run["median_local_decrypt_verify_ms"] for run in valid
                                               if run["median_local_decrypt_verify_ms"] is not None]),
            "producer_connect_login_ms": spread([run["producer_connect_login_ms"] for run in valid
                                                 if run["producer_connect_login_ms"] is not None]),
            "consumer_connect_login_ms": spread([run["consumer_connect_login_median_ms"] for run in valid
                                                 if run["consumer_connect_login_median_ms"] is not None]),
            "throughput_files_s": spread([run["throughput_files_s"] for run in valid if run["throughput_files_s"] is not None]),
            "throughput_mib_s": spread([run["throughput_mib_s"] for run in valid if run["throughput_mib_s"] is not None]),
            "server_peak_bytes": spread([run["server_peak_working_set_bytes"] for run in valid
                                         if run["server_peak_working_set_bytes"] is not None]),
            "producer_peak_bytes": spread([run["producer_peak_working_set_bytes"] for run in valid
                                           if run["producer_peak_working_set_bytes"] is not None]),
            "consumer_peak_bytes": spread([run["consumer_peak_working_set_bytes"] for run in valid
                                           if run["consumer_peak_working_set_bytes"] is not None]),
        })
    file_groups = defaultdict(list)
    for file in files:
        if not file["warmup"] and file["consumer_outcome"] == "verified":
            file_groups[(file["delay_ms"], file["workers"], file["mode"], int(file["size_bytes"]))].append(file)
    for (delay, workers, mode, size), group in sorted(file_groups.items()):
        e2e = [file["end_to_end_ms"] for file in group]
        processing = [file["processing_ms"] for file in group]
        summary["size_groups"].append({
            "delay_ms": delay, "workers": workers, "mode": mode, "size_bytes": size,
            "verified_samples": len(group),
            "end_to_end_p50_ms": nearest_rank(e2e, 0.50),
            "end_to_end_p95_ms": nearest_rank(e2e, 0.95),
            "processing_p50_ms": nearest_rank(processing, 0.50),
            "processing_p95_ms": nearest_rank(processing, 0.95),
        })
    paired_groups = defaultdict(list)
    for pair in pairs:
        if pair["paired"]:
            paired_groups[(pair["delay_ms"], pair["workers"])].append(pair)
    for (delay, workers), group in sorted(paired_groups.items()):
        summary["paired_groups"].append({
            "delay_ms": delay, "workers": workers, "pairs": len(group),
            "latency_difference_ms": spread([pair["latency_difference_ms"] for pair in group]),
            "fetch_difference_ms": spread([pair["fetch_difference_ms"] for pair in group]),
            "unwrap_difference_ms": spread([pair["unwrap_difference_ms"] for pair in group]),
            "producer_connect_difference_ms": spread([pair["producer_connect_difference_ms"] for pair in group]),
            "consumer_connect_difference_ms": spread([pair["consumer_connect_difference_ms"] for pair in group]),
            "throughput_difference_files_s": spread([pair["throughput_difference_files_s"] for pair in group]),
            "throughput_difference_mib_s": spread([pair["throughput_difference_mib_s"] for pair in group]),
            "server_memory_difference_bytes": spread([pair["server_memory_difference_bytes"] for pair in group
                                                      if pair["server_memory_difference_bytes"] is not None]),
            "producer_memory_difference_bytes": spread([pair["producer_memory_difference_bytes"] for pair in group
                                                        if pair["producer_memory_difference_bytes"] is not None]),
            "consumer_memory_difference_bytes": spread([pair["consumer_memory_difference_bytes"] for pair in group
                                                        if pair["consumer_memory_difference_bytes"] is not None]),
        })
    (directory / "analysis-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    print(f"ISSUE32_ANALYSIS=recorded measured_runs={summary['measured_runs']} "
          f"paired={summary['measured_pairs']} warmups={summary['warmup_runs']}")


if __name__ == "__main__":
    main()
