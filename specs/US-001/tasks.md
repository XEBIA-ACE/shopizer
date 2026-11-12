# US-001 — Tasks

Each task is independently reviewable and traceable to its ID.

## Requirements artifacts (T001–T005)

| Task | Requirement | Artifact | Status |
|------|-------------|----------|--------|
| T001 | `FR-US-001-AC1` | `docs/hrms/v1.0/requirements-baseline.md` §2.3 *Out of scope* (`HRMS-V1-SCOPE-OUT-01`) and §3 *Feature summary* | Done |
| T002 | `FR-US-001-AC2` | `docs/hrms/v1.0/requirements-baseline.md` §4.1–§4.4 (employee profiles, leave, performance, payroll) with no cart / purchase selection / monetary checkout journeys | Done (content placeholders — see plan R3) |
| T003 | `FR-US-001-AC3` | `docs/hrms/v1.0/requirements-baseline.md` §5 *Non-functional requirements* with no checkout or multi-currency payment references | Done |
| T004 | `FR-US-001-AC4` | `docs/hrms/v1.0/requirements-baseline.md` §6 *Future initiatives (outside HRMS v1.0)* (`HRMS-FUT-PAY-01`) | Done |
| T005 | `FR-US-001-AC5` | `docs/hrms/v1.0/signoff-record.md` | Record prepared; sign-off **Pending** (see plan R4) |

## Verification steps (T006–T010)

| Task | Acceptance criterion | Verification | Automated test |
|------|---------------------|--------------|----------------|
| T006 | `US-001-ac-1` | `verification.md` §T006 | `test_us001_ac_1_scope_and_features_state_multicurrency_checkout_out_of_scope` |
| T007 | `US-001-ac-2` | `verification.md` §T007 | `test_us001_ac_2_hr_sections_have_no_cart_selection_or_checkout_journeys` |
| T008 | `US-001-ac-3` | `verification.md` §T008 | `test_us001_ac_3_nfrs_do_not_reference_checkout_or_multicurrency_payment` |
| T009 | `US-001-ac-4` | `verification.md` §T009 | `test_us001_ac_4_future_payments_are_separate_initiatives_outside_v1` |
| T010 | `US-001-ac-5` | `verification.md` §T010 | `test_us001_ac_5_signoff_record_covers_compliance_security_audit`, `test_us001_ac_5_release_gate_all_signoffs_approved` |

## IMPACT evidence (T011–T015)

| Task | Tag | Confirms | Evidence |
|------|-----|----------|----------|
| T011 | `IMPACT` | `US-001-ac-1` | `verification.md` §T011 + `test_impact_us001_ac_1_*` |
| T012 | `IMPACT` | `US-001-ac-2` | `verification.md` §T012 + `test_impact_us001_ac_2_*` |
| T013 | `IMPACT` | `US-001-ac-3` | `verification.md` §T013 + `test_impact_us001_ac_3_*` |
| T014 | `IMPACT` | `US-001-ac-4` | `verification.md` §T014 + `test_impact_us001_ac_4_*` |
| T015 | `IMPACT` | `US-001-ac-5` | `verification.md` §T015 + `test_impact_us001_ac_5_*` |
