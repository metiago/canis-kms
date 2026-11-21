# Repository cleanup phase 2 — 2026-11-20

This pass reviewed every file under `docs/research/`. The table below records the
715-file pre-move inventory and this new report, including the two archived CSVs. Relative archive paths are
preserved below `C:\Users\TSIG\workspace\projects\ieee\Conference-Templates-Word\canis-backlog`.
The published tag and release, software, tests, `paper.md`, and `paper.bib` were
not changed. The JOSS-02 CSVs describe an earlier working-tree run; the fixed
candidate and published release have separate validation records.

## File classification

| File | Classification | Reason |
| --- | --- | --- |
| `docs/research/analyze-issue31-baseline.py` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/analyze-issue32.py` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/build-joss05-windows.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/check-joss05-paper.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code02-evidence-incomplete-corrected.consumer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-incomplete-corrected.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-incomplete-corrected.producer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-certificate-corrected.consumer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-certificate-corrected.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-certificate-corrected.producer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-credentials-corrected.consumer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-credentials-corrected.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-invalid-credentials-corrected.producer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-normal-corrected.consumer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-normal-corrected.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-normal-corrected.producer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-tamper-corrected.consumer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-tamper-corrected.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/code02-evidence-tamper-corrected.producer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/Code02Harness.java` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code02-method.md` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code02-results.md` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code03-files.csv` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code03-method.md` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code03-results.md` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/code03-runs.csv` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/export-code03.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/export-issue31-baseline.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/export-joss02-summary.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/issue31-baseline-files.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue31-baseline-method.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue31-baseline-results.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue31-baseline-runs.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue31-protocol.md` | Historical Evidence Only | Historical method or scope record cited by retained evidence. |
| `docs/research/issue32-artifacts.sha256` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue32-artifacts/analysis-files.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/analysis-pairs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/analysis-runs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/analysis-summary.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/environment.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r1-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r2-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r3-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r4-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-r5-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d0-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c1-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4.inputs.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4.pids.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-A.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.consumer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.consumer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.consumer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.memory.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.producer.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.producer-connections.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/issue32-w-d60000-c4-B.producer-heap.txt` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue32-artifacts/schedule.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/Issue32Harness.java` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/issue32-method.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue32-results.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-artifacts.sha256` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.duplicate.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.events.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.producer-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.recipient-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-A.replay.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.events.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.producer-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.producer-b.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.recipient-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-B.recipient-b.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-C.events.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-C.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-C.producer-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-C.recipient-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.bad-password.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.events.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.observer.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.producer-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.recipient-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-D.untrusted.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-E.events.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-E.json` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-E.producer-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-E.recipient-a.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-final-E.tampered.csv` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/Issue33Harness.java` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/issue33-method.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue33-results.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue34-artifacts.sha256` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r1.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r2.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-A-standard-r3.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r1.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r2.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-B-standard-r3.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r1.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r2.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.live-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.live-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-live-r3.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r1.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r2.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-C-stopped-r3.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r1.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r2.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-deleted-r3.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r1.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r2.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.postfault-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-D-modified-r3.postfault-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r1.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r2.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.baseline-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.baseline-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.events.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.json` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.rejected-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.rejected-encrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/issue34-artifacts/issue34-run-E-standard-r3.restored-decrypt.csv` | Required for Reproducibility | Final raw study artifact retained with its SHA-256 manifest. |
| `docs/research/Issue34Harness.java` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/issue34-method.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue34-results.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue35-method.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/issue35-results.md` | Required for Reproducibility | Final study method, result, dataset, or evidence manifest. |
| `docs/research/joss01-method.md` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss01-results.md` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss02-final-claim-audit.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss02-method.md` | Historical Evidence Only | Historical method or scope record cited by retained evidence. |
| `docs/research/joss02-release-boundary-final.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss02-release-notes.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss02-validation.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss03-method.md` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss03-posix-report-fixture.xml` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss03-results.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss03-suite-report.csv` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss03-test-cases.csv` | Required for Reproducibility | Final study method, bounded result, dataset, or evidence manifest. |
| `docs/research/joss06-audit.md` | Historical Evidence Only | Author-approved provenance cited by retained validation records; its broader tool inventory differs from the current paper. |
| `docs/research/joss07-snapshot.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss-final-manuscript-release.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/joss-final-paper-validation.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/jreleaser-setup.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/README.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/release-candidate.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/repository-cleanup-phase2.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/repository-cleanup-report.md` | Required for JOSS Review | Current review, release, disclosure, validation, or archive record. |
| `docs/research/run-code02.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-code02.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-code03.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue31-baseline.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue32.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue33.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue33.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue34.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue34.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-issue34-campaign.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss01.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss01.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss02.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss02.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss03.ps1` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/run-joss03.sh` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/verify-issue32.py` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/verify-issue33.py` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/verify-issue34.py` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/VerifyPosixCoverage.java` | Required for Reproducibility | Runner, analyzer, verifier, fixture, or paper-check tool. |
| `docs/research/joss02-suite-report.csv` | Archive Candidate | Earlier working-tree JOSS-02 summary; final fixed-candidate validation remains. |
| `docs/research/joss02-test-cases.csv` | Archive Candidate | Earlier working-tree JOSS-02 summary; final fixed-candidate validation remains. |

## Retained Files

All 714 files now under `docs/research/` were retained. This includes the
JOSS-01, CODE-02, CODE-03, and Issues #31–34 final evidence; Issue #32's 438
artifacts, Issue #33's 28 final artifacts, and Issue #34's 159 artifacts; their
methods, manifests, analyzers, verifiers, and reproduction runners. It also
includes `release-candidate.md`, `joss07-snapshot.md`,
`joss02-final-claim-audit.md`, `joss02-release-boundary-final.md`,
`joss03-results.md`, `joss-final-paper-validation.md`, and
`repository-cleanup-report.md`.

`issue31-protocol.md` and `joss02-method.md` are historical method records, but
current retained evidence links to them to define study deviations and scope.
`joss06-audit.md` is also historical provenance, retained because current
validation records cite it; JOSS does not require that separate audit file.
Release notes and JReleaser setup remain because the release configuration and
validation records still use them. The research index and this report remain
the entry points for evidence and archive mapping.

## Archived Files

| Source file | Archive destination | SHA-256 | Reason |
| --- | --- | --- | --- |
| `docs/research/joss02-suite-report.csv` | `C:\Users\TSIG\workspace\projects\ieee\Conference-Templates-Word\canis-backlog\docs\research\joss02-suite-report.csv` | `954D97DF3CC5908EB614567EFCDF38D9CF657404F188C69611957664C47355D2` | Superseded working-tree suite summary; fixed candidate validation and JOSS-03 final evidence remain. |
| `docs/research/joss02-test-cases.csv` | `C:\Users\TSIG\workspace\projects\ieee\Conference-Templates-Word\canis-backlog\docs\research\joss02-test-cases.csv` | `BB46F9C9FD32DD9E7688923F0B001A365736A7110560F2D5EBA616F229F23822` | Superseded working-tree case summary; fixed candidate validation and JOSS-03 final evidence remain. |

## Updated References

- `README.md` in this directory now distinguishes retained JOSS-03 CSVs from
  the archived earlier JOSS-02 summaries and links to this mapping.
- `repository-cleanup-report.md` now points from its first-pass duplicate-copy
  table to this phase 2 archive mapping. Its original first-pass findings remain
  historical.
- `joss02-final-claim-audit.md` now links to the existing
  `canisp-protocol.md#file-envelopes` heading; its earlier anchor did not exist.

## Validation Results

| Check | Result |
| --- | --- |
| Archive integrity | Both source CSV paths are absent; both archive destinations exist at the same relative paths and retain their recorded SHA-256 hashes. |
| Markdown links and anchors | 644 local links across 48 current Markdown files checked; zero missing targets or anchors. The scan found and prompted correction of one pre-existing CANISP heading link. |
| Moved-file references | No live Markdown link to either archived CSV remains. Their names occur only in this archive mapping and the first cleanup report's historical duplicate-copy table. |
| Paper source check | `check-joss05-paper.ps1` passed: 935 body words by its token rule, eight of eight headings, five citations matched to five bibliography entries, no author or affiliation placeholders. |
| Paper render | `build-joss05-windows.ps1` passed using the pinned Inara revision and Pandoc 3.14; generated `target/joss05-draft.pdf` (three A4 pages, 215,129 bytes, SHA-256 `062829C42CDB77D5B1E67C3DF46D4D2A2843F3DA57678E1B215D59E3EC967079`). The tracked paper and bibliography were not edited. |
| Reviewer workflow | `run-joss03.ps1` exited 0: `JOSS03_REVIEWER_DEMO=PASS` and `JOSS03_OUTAGE_CHECK=PASS`. The reviewer guide, quickstart, and their referenced methods and runners remain. |
| Final evidence | All 21 checked critical research records exist. Issue #32 verifier passed (`pairs=24 file_rows=4160`); Issue #33 verifier passed (`scenarios=5`); Issue #34 verifier passed (`cases=7 repetitions=3 artifacts=159`). The retained artifact counts are 438, 28, and 159 respectively. |
| Disclosure consistency | The current paper names Codex in its AI disclosure; `joss06-audit.md` also records ChatGPT and Copilot. Older validation records say the paper matches that audit. The intended author-approved disclosure needs reconciliation before submission; this pass did not edit the paper or those historical records. |
| Git whitespace check | `git diff --check` passed. No software, tests, paper source, bibliography, release, or tag was changed by this pass. |
| Existing tag-record discrepancy | The local and origin annotated `v0.1.0-rc.1` tag both peel to `5ea7e87d7f6c0aba0ed5ca32d809d615a88a9ec0`; older retained release and first-cleanup records name `e1d6998b5c5658750870b39e2a4d511de5bed7f3`. This pass did not alter either tag. The older commit claim needs separate reconciliation before citing it as the current tag target. |

## Remaining JOSS-Critical Artifacts

The manuscript, bibliography, existing paper PDF, root README, reviewer guide,
reviewer quickstart, release-candidate record, final claim and release-boundary
audits, JOSS-07 snapshot, final paper validation, and
the indexed study evidence remain. Current research reproductions use retained
methods, runners, final result records, data, manifests, and verifiers. The
published release and tag are unchanged. This documentation move does not
resolve the JOSS public-development-history question recorded in the retained
final paper validation.

## Reduction Summary

Two superseded CSV summaries moved out of the repository. The research tree
decreased from 715 to 713 existing files, then this report brought it to 714:
a net reduction of one file. All final evidence and reproduction material
remains.

