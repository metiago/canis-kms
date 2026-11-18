#!/usr/bin/env python3
"""Independently check the sanitized Issue #33 scenario artifacts."""

import argparse
import csv
import json
from datetime import datetime
from pathlib import Path


def rows(path):
    with path.open(newline="", encoding="utf-8") as stream:
        return list(csv.DictReader(stream))


def instant(text):
    return datetime.fromisoformat(text.replace("Z", "+00:00"))


def check(directory, prefix):
    expected = {
        "A": (2, 2, 0, 60000),
        "B": (4, 4, 0, 1000),
        "C": (2, 2, 0, 60000),
        "D": (1, 2, 2, 1000),
        "E": (1, 1, 1, 1000),
    }
    for scenario, (published, recovered, rejected, delay) in expected.items():
        stem = prefix + scenario
        summary = json.loads((directory / f"{stem}.json").read_text(encoding="utf-8"))
        assert summary["scenario"] == scenario
        assert summary["scenario_completion_status"] == "passed"
        assert summary["failure_category"] == "none"
        assert summary["byte_verification_success"] is True
        assert (summary["files_published"], summary["files_recovered"], summary["files_rejected"]) == (
            published,
            recovered,
            rejected,
        )
        assert summary["observed_delay_ms"] >= delay
        assert summary["run_duration_ms"] >= summary["observed_delay_ms"]
        events = rows(directory / f"{stem}.events.csv")
        assert any(row["event"] == "all_published" for row in events)
        assert any(row["event"] == "consumer_started" for row in events)
        assert instant(summary["first_publication_utc"]) < instant(summary["first_consumer_start_utc"])
        assert len(set(summary["worker_pids"].values())) == len(summary["worker_pids"])
        certs = summary["certificate_sha256"]
        assert len(set(certs.values())) == len(certs)
        raw = {}
        for path in directory.glob(f"{stem}.*.csv"):
            if path.name.endswith(".events.csv"):
                continue
            raw[path.name.removeprefix(stem + ".").removesuffix(".csv")] = rows(path)
        assert set(raw) == set(summary["worker_pids"])
        for worker_rows in raw.values():
            for row in worker_rows:
                assert int(row["size_bytes"]) in (4096, 262144)
        published_rows = [row for name, worker_rows in raw.items() if name.startswith("producer-")
                          for row in worker_rows if row["outcome"] == "published"]
        assert len(published_rows) == published
        valid_rows = [row for worker_rows in raw.values() for row in worker_rows
                      if row["outcome"] == "verified"]
        assert len(valid_rows) == recovered + (1 if scenario == "A" else 0)
        for producer in published_rows:
            match = [row for row in valid_rows if row["file_ordinal"] == producer["file_ordinal"]
                     and row["key_name"] == producer["key_name"]]
            assert match
            assert all(row["input_sha256"] == producer["input_sha256"] and
                       row["envelope_sha256"] == producer["envelope_sha256"] for row in match)
        if scenario == "A":
            assert raw["duplicate"][0]["failure_category"] == "duplicate_publication"
            assert any(row["event"] == "incomplete_present" for row in events)
            assert any(row["event"] == "duplicate_delivery_verified" for row in events)
        if scenario == "B":
            assert set(raw) == {"producer-a", "producer-b", "recipient-a", "recipient-b"}
            assert {row["key_name"] for row in published_rows} == {"recipient-a", "recipient-b"}
        if scenario == "C":
            assert summary["server_pids"][0] != summary["server_pids"][1]
            assert summary["server_pids"][1] > 0
            assert instant(summary["server_stopped_utc"]) < instant(summary["server_restarted_utc"])
            assert instant(summary["server_restarted_utc"]) < instant(summary["first_consumer_start_utc"])
        if scenario == "D":
            assert raw["observer"][0]["outcome"] == "verified"
            assert raw["bad-password"][0]["failure_category"] == "credentials_rejected"
            assert raw["untrusted"][0]["outcome"] == "failed"
            assert len({certs[name] for name in ("recipient-a", "producer-a", "observer")}) == 3
        if scenario == "E":
            assert raw["tampered"][0]["failure_category"] == "authentication_tag_rejected"
        for path in directory.glob(f"{stem}.*"):
            if path.suffix not in (".csv", ".json"):
                continue
            data = path.read_text(encoding="utf-8")
            assert all(secret not in data for secret in (
                "CANIS_SECRET_KEY", "CANIS_PASSWORD", "PRIVATE KEY", "master.key", ".p12",
                "AppData", "C:\\Users",
            ))
        print(f"ISSUE33_EVIDENCE=passed scenario={scenario} published={published} recovered={recovered} rejected={rejected}")
    print("ISSUE33_EVIDENCE=passed scenarios=5")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--directory", type=Path, default=Path("docs/research"))
    parser.add_argument("--prefix", default="issue33-final-")
    arguments = parser.parse_args()
    check(arguments.directory, arguments.prefix)
