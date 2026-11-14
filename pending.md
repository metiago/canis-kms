# Milestone 8 - AI-Assisted Security Audit Research

## Issue #36 - Define AI Security Audit Research Protocol

### Objective

Define a reproducible experimental methodology to evaluate whether AI-assisted audit analysis improves security incident detection and investigation in CANIS-KMS.

### Tasks

- [ ] Define research questions and hypotheses.
- [ ] Establish deterministic rule-based baselines.
- [ ] Define incident categories and evaluation metrics.
- [ ] Define ground-truth labeling procedures.
- [ ] Document experimental limitations and threats to validity.

### Acceptance Criteria

- [ ] Research protocol is documented before experiments.
- [ ] Baselines and evaluation metrics are reproducible.

---

## Issue #37 - Implement Asynchronous Audit Event Consumer

### Objective

Develop a read-only component that consumes structured CANIS-KMS audit events without affecting cryptographic operations.

### Tasks

- [ ] Implement asynchronous event ingestion.
- [ ] Support replay of historical audit events.
- [ ] Validate event integrity before analysis.
- [ ] Implement checkpointing and recovery.
- [ ] Prevent access to private keys and cryptographic operations.
- [ ] Enforce audit-data sanitization.

### Acceptance Criteria

- [ ] Audit analysis does not block KMS operations.
- [ ] The consumer cannot modify original audit records.
- [ ] Invalid or unverified events are flagged.

---

## Issue #38 - Implement AI Security Analysis Agent

### Objective

Evaluate the feasibility of using an AI agent to identify suspicious patterns, correlate security events, and assist incident investigations.

### Tasks

- [ ] Implement a configurable AI analysis backend.
- [ ] Define structured inputs and outputs.
- [ ] Implement event correlation and contextual analysis.
- [ ] Generate incident summaries with event references.
- [ ] Record model version and analysis configuration.
- [ ] Prevent sensitive data exposure.
- [ ] Treat audit event contents as untrusted input.
- [ ] Prevent autonomous security enforcement actions.

### Acceptance Criteria

- [ ] Reports reference verifiable audit events.
- [ ] The agent operates independently from the KMS execution path.
- [ ] Model outputs cannot modify KMS state.

---

## Issue #39 - Implement Security Incident Simulation Dataset

### Objective

Create a reproducible dataset of legitimate and suspicious CANIS-KMS activities.

### Scenarios

- [ ] Repeated authentication failures.
- [ ] Unusual authentication patterns.
- [ ] Abnormal decryption request volumes.
- [ ] Unexpected key lifecycle operations.
- [ ] Correlated activity across multiple service identities.
- [ ] Audit integrity failures.
- [ ] Legitimate workload spikes to evaluate false positives.

### Acceptance Criteria

- [ ] Every scenario has documented ground truth.
- [ ] Datasets contain no real credentials or sensitive payloads.
- [ ] Experiments can be replayed deterministically.

---

## Issue #40 - Benchmark AI-Assisted Incident Analysis

### Objective

Compare AI-assisted security analysis against deterministic rule-based detection.

### Metrics

- [ ] Precision, recall, and F1-score.
- [ ] False-positive and false-negative rates.
- [ ] Mean time to detect.
- [ ] Incident explanation accuracy.
- [ ] Resource consumption and inference cost.
- [ ] Performance under increasing event volumes.

### Acceptance Criteria

- [ ] Results include repeated experiments.
- [ ] Baselines and AI configurations are documented.
- [ ] Experimental artifacts support reproducibility.
- [ ] Negative and inconclusive results are reported.

---

## Issue #41 - Publish AI Security Research Artifacts

### Objective

Publish reproducible research materials under `docs/research/ai-audit`.

### Deliverables

- [ ] Research protocol.
- [ ] Synthetic audit datasets.
- [ ] Experimental configurations.
- [ ] Benchmark scripts.
- [ ] Results and statistical analysis.
- [ ] Threats to validity.
- [ ] Reproduction guide.