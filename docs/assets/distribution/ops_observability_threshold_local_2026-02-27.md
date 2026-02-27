# Ops Observability Threshold Report

- Generated at (UTC): 2026-02-27T10:27:58Z
- Source: log_file:/tmp/ops_threshold_sample_2026-02-27.log
- Verdict: **PASS**

## Metrics

| Metric | Value | Threshold | Result |
|---|---:|---:|---|
| API samples (official) | 2 | >= 1 | PASS |
| API official failure rate (%) | 50.00 | <= 60 | PASS |
| API terminal failure rate (%) | 0.00 | <= 5 | PASS |
| API p95 latency (ms) | 510 | <= 2000 | PASS |
| Storage samples (success+failure) | 2 | >= 1 | PASS |
| Storage failure rate (%) | 0.00 | <= 5 | PASS |
| Storage p95 latency (ms) | 32 | <= 250 | PASS |
| Storage skipped count | 1 | n/a | INFO |
