# HRMS v1.0 Requirements Baseline

| Field | Value |
|-------|-------|
| Document | HRMS v1.0 requirements baseline |
| Release | HRMS v1.0 |
| Baseline status | Draft — pending compliance, security, and audit sign-off (see §7) |
| Story | `US-001` |
| Accepted impact analysis | `sha256:33d44186c6f105d5bd643fcf384c8dd32ac06a1676db37b7a81c8a2b46db203e` |
| Grounding reference | `5e6174fd1d9ee0f1` |

> This document is a requirements artifact only. It does not change any Shopizer runtime
> behavior (Java, API, frontend, database, or configuration). Shopizer's own checkout
> capabilities (for example `checkout` in `OrderApi`) and the `Currency` class are unrelated to
> and unaffected by this baseline.

## 1. Purpose

Define the approved scope baseline for the HRMS v1.0 release so that project teams and
stakeholders cannot assume, design, or implement unsupported payment features that create
compliance and audit risk.

## 2. Scope

### 2.1 Release

This baseline applies to the HRMS v1.0 release only.

### 2.2 In scope for HRMS v1.0

The HRMS v1.0 release scope is limited to the following functional areas, detailed in §4:

1. Employee profiles (§4.1)
2. Leave (§4.2)
3. Performance (§4.3)
4. Payroll (§4.4)

No other capability is part of the HRMS v1.0 release scope.

### 2.3 Out of scope for HRMS v1.0

**HRMS-V1-SCOPE-OUT-01 — Multi-currency employee self-service checkout** (`FR-US-001-AC1`)

Multi-currency employee self-service checkout is **out of scope** for the HRMS v1.0 release and
**will not be delivered in v1.0**. This exclusion also covers all related checkout and payment
flows, including:

- any cart or basket;
- any product or service selection for purchase;
- any monetary checkout or payment capture initiated by an employee;
- any currency selection or currency conversion for such payments.

Teams SHALL NOT design, build, or test these capabilities as part of HRMS v1.0. Any future need
is tracked only as a separate future initiative (§6, `HRMS-FUT-PAY-01`).

## 3. Feature summary

| Feature | HRMS v1.0 status |
|---------|------------------|
| Employee profiles | In scope (§4.1) |
| Leave | In scope (§4.2) |
| Performance | In scope (§4.3) |
| Payroll | In scope (§4.4) |
| Multi-currency employee self-service checkout | **Out of scope for HRMS v1.0 — not delivered in v1.0** (`HRMS-V1-SCOPE-OUT-01`) |
| Transactional payments (any form) | **Out of scope for HRMS v1.0** — separate future initiative `HRMS-FUT-PAY-01` (§6) |

## 4. Functional requirements

Constraint (`FR-US-001-AC2`): the functional requirement sections §4.1–§4.4 SHALL NOT describe
any user journey or flow that involves a cart, any product or service selection for purchase, or
any monetary checkout. Detailed functional content for these sections is to be supplied by the
requirement owner (open item R3 in `specs/US-001/plan.md`); this baseline does not invent it.

### 4.1 Employee profiles

- Detailed requirements: to be supplied by the requirement owner (open item R3).
- User journeys described in this baseline: none.

### 4.2 Leave

- Detailed requirements: to be supplied by the requirement owner (open item R3).
- User journeys described in this baseline: none.

### 4.3 Performance

- Detailed requirements: to be supplied by the requirement owner (open item R3).
- User journeys described in this baseline: none.

### 4.4 Payroll

- Detailed requirements: to be supplied by the requirement owner (open item R3).
- User journeys described in this baseline: none.

## 5. Non-functional requirements

The non-functional requirements below apply only to the in-scope areas listed in §2.2. Concrete
targets are to be supplied by the requirement owner (open item R3); this baseline does not invent
them.

### 5.1 Performance

- Targets: to be supplied by the requirement owner (open item R3).
- Applies to: employee profiles, leave, performance, and payroll.

### 5.2 Availability

- Targets: to be supplied by the requirement owner (open item R3).
- Applies to: employee profiles, leave, performance, and payroll.

### 5.3 Security

- Targets: to be supplied by the requirement owner (open item R3).
- Applies to: employee profiles, leave, performance, and payroll.
- Example data used in any HRMS v1.0 artifact SHALL be fictitious and non-identifying, and no
  credentials SHALL be recorded in requirements artifacts.

## 6. Future initiatives (outside HRMS v1.0)

Items in this section are **not part of the HRMS v1.0 release scope** and SHALL NOT be listed in
§2.2 or delivered in HRMS v1.0.

| ID | Initiative | Relationship to HRMS v1.0 |
|----|------------|---------------------------|
| `HRMS-FUT-PAY-01` | Transactional payments, including any multi-currency employee self-service checkout | Separate future initiative/epic, **outside HRMS v1.0 scope**. Requires its own requirements baseline and its own compliance, security, and audit review before any work starts. (`FR-US-001-AC4`) |

Clarification (`FR-US-001-AC4`): transactional payments of any kind are out of scope for HRMS
v1.0. Mentioning them in this section does not add them to the v1.0 release scope.

## 7. Sign-off

Compliance, security, and audit sign-off confirming that the exclusion of multi-currency checkout
for HRMS v1.0 (`HRMS-V1-SCOPE-OUT-01`) is clearly documented is recorded in
[`signoff-record.md`](signoff-record.md) (`FR-US-001-AC5`). This baseline becomes the approved
baseline only when all three sign-offs are recorded as `Approved`.

## 8. Change log

| Change | Story | Description |
|--------|-------|-------------|
| 1 | `US-001` | Baseline created; multi-currency employee self-service checkout explicitly excluded from HRMS v1.0; transactional payments deferred to `HRMS-FUT-PAY-01`. |
