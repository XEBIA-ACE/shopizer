# HRMS v1.0 Requirements Baseline — Sign-off Record

- Story: `US-001` — requirement `FR-US-001-AC5`, acceptance criterion `US-001-ac-5`
- Baseline under review: [`requirements-baseline.md`](requirements-baseline.md)
- Clause under review: `HRMS-V1-SCOPE-OUT-01` (multi-currency employee self-service checkout out of scope for HRMS v1.0)

## Confirmation requested

Each stakeholder is asked to confirm:

> I have reviewed the HRMS v1.0 requirements baseline and confirm that the exclusion of
> multi-currency checkout for HRMS v1.0 (`HRMS-V1-SCOPE-OUT-01`) is clearly documented.

## Sign-offs

Allowed decisions: `Pending`, `Approved`, `Rejected`. Stakeholder names are recorded by the
requirement owner; do not record credentials or personal data beyond name and role.

| Role | Stakeholder | Clause | Decision | Date | Notes |
|------|-------------|--------|----------|------|-------|
| Compliance | To be named by requirement owner | `HRMS-V1-SCOPE-OUT-01` | Pending | — | — |
| Security | To be named by requirement owner | `HRMS-V1-SCOPE-OUT-01` | Pending | — | — |
| Audit | To be named by requirement owner | `HRMS-V1-SCOPE-OUT-01` | Pending | — | — |

## Status

Sign-off has been **requested but not yet obtained**. `US-001-ac-5` is not satisfied until all
three rows are `Approved`. The release gate is:

```bash
US001_REQUIRE_SIGNOFF=1 python3 -m unittest discover -s specs/US-001/tests -v
```
