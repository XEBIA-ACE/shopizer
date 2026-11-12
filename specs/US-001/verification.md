# US-001 — Verification record

Artifacts under review:
[`docs/hrms/v1.0/requirements-baseline.md`](../../docs/hrms/v1.0/requirements-baseline.md) and
[`docs/hrms/v1.0/signoff-record.md`](../../docs/hrms/v1.0/signoff-record.md).

Automated characterization tests: [`tests/test_us001_requirements_baseline.py`](tests/test_us001_requirements_baseline.py)

```bash
python3 -m unittest discover -s specs/US-001/tests -v
```

## Acceptance-criterion verification (T006–T010)

## T006 — `US-001-ac-1`

- Review step: open §2.3 *Out of scope* and §3 *Feature summary*; confirm the explicit statement
  that multi-currency employee self-service checkout is out of scope and will not be delivered in v1.0.
- Automated: `test_us001_ac_1_scope_and_features_state_multicurrency_checkout_out_of_scope`.
- Result: pass.

## T007 — `US-001-ac-2`

- Review step: scan §4.1 Employee profiles, §4.2 Leave, §4.3 Performance, §4.4 Payroll for any
  cart, product/service selection for purchase, or monetary checkout journey.
- Automated: `test_us001_ac_2_hr_sections_have_no_cart_selection_or_checkout_journeys`.
- Result: pass (no such journeys). Note: section content is placeholder pending requirement owner input (plan R3); the check must be re-run when content is added.

## T008 — `US-001-ac-3`

- Review step: review §5.1 Performance, §5.2 Availability, §5.3 Security; confirm no reference to
  or assumption of checkout or multi-currency payment capability.
- Automated: `test_us001_ac_3_nfrs_do_not_reference_checkout_or_multicurrency_payment`.
- Result: pass. Targets are pending requirement owner input (plan R3); re-run when added.

## T009 — `US-001-ac-4`

- Review step: confirm §6 lists transactional payments only as separate future initiative
  `HRMS-FUT-PAY-01` outside HRMS v1.0, with explicit clarifying wording, and that §2.2 does not list it.
- Automated: `test_us001_ac_4_future_payments_are_separate_initiatives_outside_v1`.
- Result: pass.

## T010 — `US-001-ac-5`

- Review step: compliance, security, and audit stakeholders review the baseline and record their
  decision in the sign-off record.
- Automated: `test_us001_ac_5_signoff_record_covers_compliance_security_audit` (record structure)
  and `test_us001_ac_5_release_gate_all_signoffs_approved` (release gate, `US001_REQUIRE_SIGNOFF=1`).
- Result: record structure pass; release gate **not yet satisfied** — all three sign-offs are
  `Pending` (plan R4).

## IMPACT evidence (T011–T015)

These confirm what the spec already states; they add no requirements.

## T011 — IMPACT `US-001-ac-1`

- Evidence: the exclusion clause `HRMS-V1-SCOPE-OUT-01` is traced to `FR-US-001-AC1` and the
  baseline references accepted impact `sha256:33d44186c6f105d5bd643fcf384c8dd32ac06a1676db37b7a81c8a2b46db203e`.
- Automated: `test_impact_us001_ac_1_exclusion_tied_to_accepted_impact`.

## T012 — IMPACT `US-001-ac-2`

- Evidence: §2.2 release scope is limited to employee profiles, leave, performance, and payroll.
- Automated: `test_impact_us001_ac_2_in_scope_limited_to_hr_areas`.

## T013 — IMPACT `US-001-ac-3`

- Evidence: §5 states NFRs apply only to the in-scope areas in §2.2.
- Automated: `test_impact_us001_ac_3_nfrs_scoped_to_in_scope_areas_only`.

## T014 — IMPACT `US-001-ac-4`

- Evidence: §3 feature summary marks transactional payments out of scope for HRMS v1.0 and
  points to `HRMS-FUT-PAY-01`.
- Automated: `test_impact_us001_ac_4_future_ids_absent_from_release_scope`.

## T015 — IMPACT `US-001-ac-5`

- Evidence: the sign-off record references `US-001`, `FR-US-001-AC5`, `US-001-ac-5`, and clause
  `HRMS-V1-SCOPE-OUT-01`.
- Automated: `test_impact_us001_ac_5_signoff_references_story_and_clause`.

## Runtime impact

No Shopizer runtime code changed: `git diff --stat` for this change touches only `specs/US-001/`
and `docs/hrms/v1.0/`. `OrderApi.checkout` and `Currency` are unchanged.
