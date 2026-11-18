#!/usr/bin/env python3
"""Check Issue #34 sanitized artifacts; never open private fixture roots."""

import argparse
import csv
import hashlib
import json
from pathlib import Path

CASES = [("A", "standard"), ("B", "standard"), ("C", "live"),
         ("C", "stopped"), ("D", "modified"), ("D", "deleted"),
         ("E", "standard")]
FORBIDDEN = (b"CANIS_SECRET_KEY", b"BEGIN PRIVATE KEY", b"PASSWORD=",
             b"master.key", b".p12", b"AppData\\Local\\Temp", b"/tmp/canis-issue34-")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def rows(path):
    with path.open(newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def require(condition, message):
    if not condition:
        raise ValueError(message)


def event(events, phase, operation):
    matches = [e for e in events if e["phase"] == phase and e["operation"] == operation]
    require(len(matches) == 1, f"missing/duplicate {phase}/{operation}")
    return matches[0]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--write-manifest", type=Path)
    args = parser.parse_args()
    directory = args.directory.resolve()
    require(directory.is_dir(), "artifact directory absent")
    expected_json = {f"issue34-run-{scenario}-{variant}-r{rep}.json"
                     for rep in range(1, 4) for scenario, variant in CASES}
    actual_json = {p.name for p in directory.glob("issue34-run-*.json")}
    require(actual_json == expected_json, f"JSON matrix mismatch: {actual_json ^ expected_json}")
    source_hashes, jar_hashes, revisions = set(), set(), set()
    checked = set()
    for rep in range(1, 4):
        for scenario, variant in CASES:
            stem = f"issue34-run-{scenario}-{variant}-r{rep}"
            path = directory / f"{stem}.json"
            data = json.loads(path.read_text(encoding="utf-8"))
            require((data["scenario"], data["variant"], data["repetition"]) ==
                    (scenario, variant, rep), f"identity mismatch: {stem}")
            require(data["schema"] == "issue34-v1" and data["status"] == "passed" and
                    data["failure_category"] == "none", f"failed record: {stem}")
            require(data["baseline_verified"] and data["restored_and_verified"],
                    f"baseline/recovery incomplete: {stem}")
            require(data["database_sha256_before"] == data["database_sha256_after_restore"],
                    f"database changed: {stem}")
            events_path = directory / f"{stem}.events.csv"
            require(events_path.is_file(), f"missing events: {stem}")
            events = rows(events_path)
            require(event(events, "baseline", "startup")["outcome"] == "ready", stem)
            require(event(events, "baseline", "encrypt")["outcome"] == "published", stem)
            require(event(events, "baseline", "decrypt")["outcome"] == "verified", stem)
            require(event(events, "baseline", "decrypt")["output_present"] == "true", stem)
            times = [e["time_utc"] for e in events]
            require(times == sorted(times), f"event order: {stem}")
            require(data["first_server_pid"] > 0 and data["second_server_pid"] > 0, stem)
            if scenario == "D":
                require(data["external_audit_change_detected"] and data["faulted_restart_ready"], stem)
                require(data["audit_sha256_before"] != data["audit_sha256_after_fault"], stem)
                require(data["post_fault_encrypted"] and data["post_fault_decrypted"], stem)
                require(event(events, "postfault", "encrypt")["outcome"] == "published", stem)
                require(event(events, "postfault", "decrypt")["outcome"] == "verified", stem)
            else:
                require(not data["faulted_restart_ready"] and data["faulted_startup_exit"] != 0, stem)
                require(not data["rejected_output_present"], stem)
                require(event(events, "faulted_restart", "encrypt")["outcome"] == "failed", stem)
                require(event(events, "faulted_restart", "decrypt")["outcome"] == "failed", stem)
                require(event(events, "faulted_restart", "decrypt")["output_present"] == "false", stem)
                require(event(events, "recovery", "decrypt")["outcome"] == "verified", stem)
                if scenario in ("A", "B"):
                    require(data["post_fault_encrypted"] and data["post_fault_decrypted"], stem)
                else:
                    require(not data["post_fault_encrypted"] and not data["post_fault_decrypted"], stem)
            worker_csvs = list(directory.glob(stem + ".*.csv"))
            require(len(worker_csvs) >= 5, f"worker CSVs missing: {stem}")
            label_phases = {"baseline-encrypt": ("baseline", "encrypt"),
                            "baseline-decrypt": ("baseline", "decrypt")}
            if scenario in ("A", "B") or (scenario == "C" and variant == "live"):
                label_phases.update({"live-encrypt": ("live", "encrypt"),
                                     "live-decrypt": ("live", "decrypt")})
            if scenario == "D":
                label_phases.update({"postfault-encrypt": ("postfault", "encrypt"),
                                     "postfault-decrypt": ("postfault", "decrypt")})
            else:
                label_phases.update({"rejected-encrypt": ("faulted_restart", "encrypt"),
                                     "rejected-decrypt": ("faulted_restart", "decrypt"),
                                     "restored-decrypt": ("recovery", "decrypt")})
            require(len(worker_csvs) == len(label_phases) + 1, f"unexpected CSV count: {stem}")
            for label, (phase, operation) in label_phases.items():
                worker_path = directory / f"{stem}.{label}.csv"
                require(worker_path.is_file(), f"worker CSV absent: {worker_path.name}")
                worker_rows = rows(worker_path)
                require(len(worker_rows) == 1, f"worker row count: {worker_path.name}")
                worker_row = worker_rows[0]
                event_row = event(events, phase, operation)
                require(worker_row["outcome"] == event_row["outcome"] and
                        worker_row["failure_category"] == event_row["failure_category"],
                        f"worker/event disagreement: {worker_path.name}")
            for worker_csv in worker_csvs:
                require(len(rows(worker_csv)) > 0, f"empty CSV: {worker_csv.name}")
                checked.add(worker_csv.name)
            checked.add(path.name)
            source_hashes.add(data["harness_sha256"])
            jar_hashes.add(data["server_jar_sha256"])
            revisions.add(data["revision"])
    require(len(source_hashes) == len(jar_hashes) == len(revisions) == 1,
            "campaign used multiple source/jar revisions")
    files = sorted(directory.glob("issue34-run-*"))
    require({p.name for p in files} == checked, "unaccounted campaign artifact")
    for path in files:
        content = path.read_bytes()
        require(not any(marker in content for marker in FORBIDDEN),
                f"selected secret/path marker in {path.name}")
    if args.write_manifest:
        manifest = args.write_manifest.resolve()
        require(not manifest.exists(), "manifest already exists")
        require(manifest.parent == directory.parent, "manifest must be next to artifact directory")
        manifest.write_bytes("".join(f"{digest(path)}  {directory.name}/{path.name}\n"
                                     for path in files).encode("ascii"))
    print(f"ISSUE34_EVIDENCE=passed cases={len(CASES)} repetitions=3 artifacts={len(files)}")


if __name__ == "__main__":
    main()
