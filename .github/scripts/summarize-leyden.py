#!/usr/bin/env python3
"""Summarize paired CANIS startup and client latency samples from the CI run."""

import csv
import math
import os
import statistics
import sys
from pathlib import Path

METRICS = ("ready_ms", "first_req_ms", "warmup_50_ms")
MODES = ("baseline", "aot")
ITERATIONS = set(range(1, 11))
FIELDS = (
    "metric",
    "mode",
    "n",
    "median_ms",
    "mean_ms",
    "min_ms",
    "max_ms",
    "stdev_ms",
    "median_improvement_pct",
    "mean_improvement_pct",
)


def read_samples(path: Path) -> dict[str, dict[int, dict[str, float]]]:
    samples: dict[str, dict[int, dict[str, float]]] = {mode: {} for mode in MODES}
    with path.open(newline="", encoding="utf-8") as source:
        reader = csv.DictReader(source)
        required = {"mode", "iteration", *METRICS}
        if not required.issubset(reader.fieldnames or []):
            raise ValueError(f"Missing raw result columns: {required - set(reader.fieldnames or [])}")
        for row in reader:
            mode = row["mode"]
            iteration = int(row["iteration"])
            if mode not in samples or iteration not in ITERATIONS or iteration in samples[mode]:
                raise ValueError(f"Unexpected or duplicate run: {mode} {iteration}")
            values = {metric: float(row[metric]) for metric in METRICS}
            if any(not math.isfinite(value) or value <= 0 for value in values.values()):
                raise ValueError(f"Invalid latency in {mode} run {iteration}")
            samples[mode][iteration] = values
    for mode in MODES:
        if set(samples[mode]) != ITERATIONS:
            raise ValueError(f"Expected 10 {mode} runs; found {sorted(samples[mode])}")
    return samples


def summarize(samples: dict[str, dict[int, dict[str, float]]]) -> list[dict[str, str]]:
    rows = []
    for metric in METRICS:
        values = {
            mode: [samples[mode][iteration][metric] for iteration in sorted(ITERATIONS)]
            for mode in MODES
        }
        baseline_median = statistics.median(values["baseline"])
        baseline_mean = statistics.mean(values["baseline"])
        for mode in MODES:
            current = values[mode]
            median = statistics.median(current)
            mean = statistics.mean(current)
            rows.append(
                {
                    "metric": metric,
                    "mode": mode,
                    "n": str(len(current)),
                    "median_ms": f"{median:.3f}",
                    "mean_ms": f"{mean:.3f}",
                    "min_ms": f"{min(current):.3f}",
                    "max_ms": f"{max(current):.3f}",
                    "stdev_ms": f"{statistics.stdev(current):.3f}",
                    "median_improvement_pct": f"{100 * (baseline_median - median) / baseline_median:.2f}",
                    "mean_improvement_pct": f"{100 * (baseline_mean - mean) / baseline_mean:.2f}",
                }
            )
    return rows


def main() -> None:
    raw_path, summary_path = map(Path, sys.argv[1:3])
    rows = summarize(read_samples(raw_path))
    with summary_path.open("w", newline="", encoding="utf-8") as target:
        writer = csv.DictWriter(target, fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows(rows)

    headers = (
        "Metric",
        "Mode",
        "n",
        "Median ms",
        "Mean ms",
        "Min ms",
        "Max ms",
        "SD ms",
        "Median gain %",
        "Mean gain %",
    )
    lines = ["| " + " | ".join(headers) + " |", "| " + " | ".join("---" for _ in headers) + " |"]
    for row in rows:
        lines.append("| " + " | ".join(row[field] for field in FIELDS) + " |")
    table = "\n".join(lines)
    print(table)
    print("Positive gain means lower AOT latency; SD is sample standard deviation.")

    github_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if github_summary:
        with open(github_summary, "a", encoding="utf-8") as target:
            target.write("## CANIS Leyden benchmark: 10 runs per mode\n\n")
            target.write(table + "\n\n")
            target.write(
                "Positive gain means lower AOT latency. SD is sample standard deviation. "
                "First request includes TLS connection and authentication; the next 50 reads use that connection.\n"
            )


if __name__ == "__main__":
    main()
