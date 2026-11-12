# US-001 — Implementation plan

Documentation-only implementation plan for `US-001`, built from accepted impact analysis
`sha256:33d44186c6f105d5bd643fcf384c8dd32ac06a1676db37b7a81c8a2b46db203e` and grounding
reference `5e6174fd1d9ee0f1`.

## Approach

1. Establish the HRMS v1.0 requirements baseline at
   [`docs/hrms/v1.0/requirements-baseline.md`](../../docs/hrms/v1.0/requirements-baseline.md)
   that:
   - explicitly records multi-currency employee self-service checkout as out of scope (`FR-US-001-AC1`);
   - keeps the employee profile, leave, performance, and payroll sections free of cart, purchase
     selection, and monetary checkout journeys (`FR-US-001-AC2`);
   - keeps non-functional requirements free of any checkout / multi-currency payment
     assumption (`FR-US-001-AC3`);
   - defers transactional payments to clearly labelled future initiatives outside v1.0 (`FR-US-001-AC4`).
2. Record the compliance / security / audit sign-off in
   [`docs/hrms/v1.0/signoff-record.md`](../../docs/hrms/v1.0/signoff-record.md) (`FR-US-001-AC5`).
3. Provide verification steps (T006–T010) and IMPACT evidence (T011–T015) in
   [`verification.md`](verification.md), automated by the characterization tests in
   [`tests/test_us001_requirements_baseline.py`](tests/test_us001_requirements_baseline.py).

## Explicitly untouched

No Java, API, frontend, database, or configuration behavior in Shopizer is changed. In particular
`checkout` in `OrderApi` and the `Currency` class are **not** modified for this story.

## Verification plan

Run the characterization tests from the repository root:

```bash
python3 -m unittest discover -s specs/US-001/tests -v
```

Release gate for `US-001-ac-5` (fails until all three stakeholder sign-offs are `Approved`):

```bash
US001_REQUIRE_SIGNOFF=1 python3 -m unittest discover -s specs/US-001/tests -v
```

## Unresolved readiness items (disclosed, not worked around)

| # | Item | Affected IDs | Handling |
|---|------|--------------|----------|
| R1 | Candidate verification flagged a possibly better-matching focus object `checkout (9201)` (`OrderApi.checkout`) that was not selected; the selected focus object `Currency` was judged implausible. | All | Accepted as-is: neither object is modified because US-001 is documentation-only. Reviewer should confirm the focus-object choice in the impact analysis. |
| R2 | No HRMS requirements, scope documents, or stakeholder sign-offs exist in the CAST analysis or in this repository. | `FR-US-001-AC1`–`FR-US-001-AC5`, T001–T005 | No pre-existing HRMS v1.0 requirements document was found. A new baseline was created at `docs/hrms/v1.0/`. **Clarification required:** confirm this is the authoritative location/format, or point to the system of record so the content can be moved. |
| R3 | No pre-existing implementation; scope derived solely from the requirement text. | All | The baseline contains only the content needed to satisfy and verify the FRs; employee profile, leave, performance, and payroll sections are placeholders that state scope boundaries only and do not invent HRMS features. **Clarification required:** the requirement owner must supply the real feature content for those sections. |
| R4 | Stakeholder sign-off (`FR-US-001-AC5`) cannot be obtained by the implementing agent. | `FR-US-001-AC5`, `US-001-ac-5`, T005, T010, T015 | A sign-off record with status `Pending` for Compliance, Security, and Audit is provided. **Action required:** the named roles must review and record their decision. `US-001-ac-5` is not complete until then. |
| R5 | API impact readiness check `accepted_impact` is `blocked`, `grounded_components` is `warning`. | All | Disclosed; no runtime components are built, so no component grounding is needed for this documentation change. |
