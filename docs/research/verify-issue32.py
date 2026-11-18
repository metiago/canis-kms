#!/usr/bin/env python3
"""Check Issue #32 raw artifact consistency and optional SHA-256 manifest."""

import argparse
import csv
import hashlib
import json
from collections import Counter
from pathlib import Path


def record(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def rows(path):
    with path.open(newline="", encoding="utf-8-sig") as stream:
        return list(csv.DictReader(stream))


def require(condition, message):
    if not condition:
        raise ValueError(message)


def verify(directory, scope):
    schedule = rows(directory / "schedule.csv")
    expected_pairs = 1 if scope == "smoke" else 24
    require(len(schedule) == expected_pairs, "unexpected pair count")
    environment = record(directory / "environment.json")
    require(environment["schema"] == "issue32-environment-v1", "environment schema")
    source_revisions = set()
    jar_hashes = set()
    measured = Counter()
    orders = Counter()
    total_files = 0
    for entry in schedule:
        pair_id = entry["pair_id"]
        count = int(entry["planned_files"])
        repetition = int(entry["repetition"])
        delay = int(entry["delay_ms"])
        workers = int(entry["workers"])
        warmup = entry["warmup"] == "true"
        require(count in (1, 20, 100) and workers in (1, 4) and delay in (0, 60000), "workload bound")
        require(entry["order"] in ("AB", "BA") and entry["pair_status"] == "passed"
                and entry["coordinator_exit_code"] in ("", "0"), "pair outcome")
        if scope == "campaign":
            require(count == (20 if warmup else 100), "campaign file count")
            if not warmup:
                measured[(delay, workers)] += 1
                orders[(repetition, entry["order"])] += 1
        input_rows = rows(directory / f"{pair_id}.inputs.csv")
        require(len(input_rows) == count, "input count")
        size_counts = Counter(int(row["size"]) for row in input_rows)
        expected_sizes = {4096: 10, 262144: 7, 4194304: 3} if count == 20 else (
            {4096: 50, 262144: 35, 4194304: 15} if count == 100 else {4096: 1})
        require(size_counts == expected_sizes, "input mix")
        inputs = {int(row["ordinal"]): row for row in input_rows}
        require(set(inputs) == set(range(count)), "input ordinals")
        pids = rows(directory / f"{pair_id}.pids.csv")
        require(len(pids) == 6, "PID count")
        snapshots = set()
        for mode in ("A", "B"):
            run_id = f"{pair_id}-{mode}"
            run = record(directory / f"{run_id}.json")
            memory = record(directory / f"{run_id}.memory.json")
            producer = rows(directory / f"{run_id}.producer.csv")
            consumer = rows(directory / f"{run_id}.consumer.csv")
            producer_connections = rows(directory / f"{run_id}.producer-connections.csv")
            consumer_connections = rows(directory / f"{run_id}.consumer-connections.csv")
            require(run["schema"] == "issue32-run-v1" and run["run_id"] == run_id, "run identity")
            require(run["mode"] == mode and run["repetition"] == repetition
                    and run["delay_ms"] == delay and run["workers"] == workers
                    and run["warmup"] == warmup and run["planned_files"] == count, "setting mismatch")
            require(run["status"] == "passed" and run["failure_category"] == "none", "run failure")
            require(run["successful_files"] == count and run["failed_files"] == 0, "verified count")
            require(run["producer_exit_code"] == 0 and run["consumer_exit_code"] == 0, "worker exit")
            require(run["run_duration_ms"] > 0, "duration")
            require(len(producer) == count and len(consumer) == count, "file row count")
            require(len(producer_connections) == 1 and len(consumer_connections) == workers, "connection count")
            require(all(row["outcome"] == "connected" for row in producer_connections + consumer_connections),
                    "connection failure")
            require(memory["schema"] == "issue32-os-memory-v1" and memory["run_id"] == run_id,
                    "memory identity")
            for role in ("server", "producer", "consumer"):
                matching = [row for row in pids if row["run_id"] == run_id and row["role"] == role]
                require(len(matching) == 1 and int(matching[0]["pid"]) == run[f"{role}_pid"], "PID mismatch")
                require(memory[f"{role}_pid"] == run[f"{role}_pid"], "memory PID mismatch")
                require(memory[f"{role}_sample_count"] > 0
                        and memory[f"{role}_peak_working_set_bytes"] > 0, "missing memory sample")
            for name in ("producer", "consumer"):
                require(int((directory / f"{run_id}.{name}-heap.txt").read_text().strip()) > 0,
                        "missing worker heap")
            for index in range(count):
                source, p, c = inputs[index], producer[index], consumer[index]
                require(int(p["ordinal"]) == index and int(c["ordinal"]) == index, "file ordinal")
                require(p["size"] == source["size"] and c["size"] == source["size"], "file size")
                require(p["source_sha256"] == source["source_sha256"]
                        and c["verified_sha256"] == source["source_sha256"], "byte hash")
                require(p["outcome"] == "published" and c["outcome"] == "verified"
                        and p["failure_category"] == "none" and c["failure_category"] == "none",
                        "file outcome")
            source_revisions.add(run["source_revision"])
            jar_hashes.add(run["server_jar_sha256"])
            snapshots.add(run["database_snapshot_sha256"])
            total_files += count
        require(len(snapshots) == 1, "paired database differs")
    if scope == "campaign":
        require(measured == Counter({(0, 1): 5, (0, 4): 5, (60000, 1): 5, (60000, 4): 5}),
                "measured matrix")
        require(all(orders[(repetition, order)] == 2 for repetition in range(1, 6)
                    for order in ("AB", "BA")), "unbalanced arm order")
    require(len(source_revisions) == 1 and len(jar_hashes) == 1, "build identity changed")
    require(environment["server_jar_sha256"] in jar_hashes, "environment jar hash")
    analysis = record(directory / "analysis-summary.json")
    require(analysis["schema"] == "issue32-analysis-v1", "analysis schema")
    require(analysis["measured_runs"] == (2 if scope == "smoke" else 40), "analysis measured count")
    require(analysis["warmup_runs"] == (0 if scope == "smoke" else 8), "analysis warmup count")
    require(len(rows(directory / "analysis-runs.csv")) == len(schedule) * 2, "analysis run rows")
    require(len(rows(directory / "analysis-files.csv")) == total_files, "analysis file rows")
    require(len(rows(directory / "analysis-pairs.csv")) == len(schedule), "analysis pair rows")
    banned = ("-----BEGIN PRIVATE KEY", "CANIS_SECRET_KEY=", "CANIS_PASSWORD=",
              "master.key", ".p12", "canis-issue32-")
    for path in directory.iterdir():
        if path.is_file():
            data = path.read_text(encoding="utf-8-sig")
            require(not any(marker in data for marker in banned), f"secret/path marker in {path.name}")
    return len(schedule), total_files


def manifest(directory, path, write):
    lines = [f"{hashlib.sha256(file.read_bytes()).hexdigest()}  {file.name}"
             for file in sorted(directory.iterdir()) if file.is_file()]
    if write:
        require(not path.exists(), "manifest exists")
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    elif path.exists():
        require(path.read_text(encoding="utf-8").splitlines() == lines, "manifest mismatch")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--scope", choices=("smoke", "campaign"), default="campaign")
    parser.add_argument("--manifest", type=Path)
    parser.add_argument("--write-manifest", action="store_true")
    args = parser.parse_args()
    pairs, files = verify(args.directory, args.scope)
    if args.manifest:
        manifest(args.directory, args.manifest, args.write_manifest)
    print(f"ISSUE32_EVIDENCE=passed pairs={pairs} file_rows={files}")


if __name__ == "__main__":
    main()
