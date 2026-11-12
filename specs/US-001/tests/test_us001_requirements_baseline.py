"""Characterization tests for US-001 (HRMS v1.0 multi-currency checkout exclusion).

Run from the repository root:
    python3 -m unittest discover -s specs/US-001/tests -v
Release gate for US-001-ac-5:
    US001_REQUIRE_SIGNOFF=1 python3 -m unittest discover -s specs/US-001/tests -v
"""

import os
import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
BASELINE = REPO_ROOT / "docs" / "hrms" / "v1.0" / "requirements-baseline.md"
SIGNOFF = REPO_ROOT / "docs" / "hrms" / "v1.0" / "signoff-record.md"
VERIFICATION = REPO_ROOT / "specs" / "US-001" / "verification.md"
ACCEPTED_IMPACT = "sha256:33d44186c6f105d5bd643fcf384c8dd32ac06a1676db37b7a81c8a2b46db203e"

PURCHASE_JOURNEY_TERMS = [
    r"\bcart\b", r"\bbasket\b", r"check-?out", r"\bpurchas\w*", r"\bbuy\w*",
    r"\bshop\w*", r"\border\w*", r"product (or service )?selection",
    r"service selection", r"payment gateway", r"multi-currency",
]
NFR_FORBIDDEN_TERMS = [
    r"check-?out", r"\bcart\b", r"\bbasket\b", r"multi-currency", r"\bcurrenc\w*",
    r"\bpayments?\b", r"\bpurchas\w*",
]


def read(path):
    return path.read_text(encoding="utf-8")


def section(text, heading):
    """Return the body of the markdown section whose heading starts with `heading`."""
    lines = text.splitlines()
    for i, line in enumerate(lines):
        m = re.match(r"^(#+)\s+(.*)$", line)
        if m and m.group(2).strip().startswith(heading):
            level = len(m.group(1))
            body = []
            for nxt in lines[i + 1:]:
                n = re.match(r"^(#+)\s", nxt)
                if n and len(n.group(1)) <= level:
                    break
                body.append(nxt)
            return "\n".join(body)
    raise AssertionError("Section not found: %s" % heading)


def find_terms(text, patterns):
    return [p for p in patterns if re.search(p, text, re.IGNORECASE)]


def signoff_rows():
    rows = {}
    for line in read(SIGNOFF).splitlines():
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) == 6 and cells[0] in ("Compliance", "Security", "Audit"):
            rows[cells[0]] = {"clause": cells[2], "decision": cells[3], "date": cells[4]}
    return rows


class US001AcceptanceCriteriaTest(unittest.TestCase):

    def setUp(self):
        self.baseline = read(BASELINE)

    # [US-001-ac-1] / T006
    def test_us001_ac_1_scope_and_features_state_multicurrency_checkout_out_of_scope(self):
        out_of_scope = section(self.baseline, "2.3 Out of scope")
        self.assertRegex(out_of_scope, r"HRMS-V1-SCOPE-OUT-01")
        self.assertRegex(
            out_of_scope,
            r"Multi-currency employee self-service checkout is \*\*out of scope\*\* for the HRMS v1\.0 release")
        self.assertIn("will not be delivered in v1.0", out_of_scope)
        features = section(self.baseline, "3. Feature summary")
        row = [l for l in features.splitlines() if "Multi-currency employee self-service checkout" in l]
        self.assertEqual(len(row), 1)
        self.assertIn("Out of scope for HRMS v1.0", row[0])

    # [US-001-ac-2] / T007
    def test_us001_ac_2_hr_sections_have_no_cart_selection_or_checkout_journeys(self):
        for heading in ("4.1 Employee profiles", "4.2 Leave", "4.3 Performance", "4.4 Payroll"):
            with self.subTest(section=heading):
                body = section(self.baseline, heading)
                self.assertEqual(find_terms(body, PURCHASE_JOURNEY_TERMS), [],
                                 "%s describes a purchase/checkout journey" % heading)

    # [US-001-ac-3] / T008
    def test_us001_ac_3_nfrs_do_not_reference_checkout_or_multicurrency_payment(self):
        nfr = section(self.baseline, "5. Non-functional requirements")
        for heading in ("5.1 Performance", "5.2 Availability", "5.3 Security"):
            self.assertIn("### " + heading, "\n" + nfr)
        self.assertEqual(find_terms(nfr, NFR_FORBIDDEN_TERMS), [])

    # [US-001-ac-4] / T009
    def test_us001_ac_4_future_payments_are_separate_initiatives_outside_v1(self):
        future = section(self.baseline, "6. Future initiatives (outside HRMS v1.0)")
        row = [l for l in future.splitlines() if l.startswith("| `HRMS-FUT-PAY-01`")]
        self.assertEqual(len(row), 1)
        self.assertIn("Transactional payments", row[0])
        self.assertIn("outside HRMS v1.0 scope", row[0])
        self.assertIn("transactional payments of any kind are out of scope for HRMS\nv1.0", future)
        in_scope = section(self.baseline, "2.2 In scope for HRMS v1.0")
        self.assertEqual(find_terms(in_scope, [r"payment", r"check-?out", r"HRMS-FUT-"]), [])

    # [US-001-ac-5] / T010
    def test_us001_ac_5_signoff_record_covers_compliance_security_audit(self):
        self.assertIn("signoff-record.md", section(self.baseline, "7. Sign-off"))
        signoff = read(SIGNOFF)
        self.assertIn("the exclusion of\n> multi-currency checkout for HRMS v1.0", signoff)
        rows = signoff_rows()
        self.assertEqual(sorted(rows), ["Audit", "Compliance", "Security"])
        for role, row in rows.items():
            with self.subTest(role=role):
                self.assertEqual(row["clause"], "`HRMS-V1-SCOPE-OUT-01`")
                self.assertIn(row["decision"], ("Pending", "Approved", "Rejected"))

    # [US-001-ac-5] / T010 release gate (required_before: release)
    @unittest.skipUnless(os.environ.get("US001_REQUIRE_SIGNOFF") == "1",
                         "release gate: set US001_REQUIRE_SIGNOFF=1")
    def test_us001_ac_5_release_gate_all_signoffs_approved(self):
        for role, row in signoff_rows().items():
            with self.subTest(role=role):
                self.assertEqual(row["decision"], "Approved")
                self.assertRegex(row["date"], r"^\d{4}-\d{2}-\d{2}$")


class US001ImpactEvidenceTest(unittest.TestCase):
    """IMPACT-tagged evidence (T011-T015). Confirms the spec only; adds no requirements."""

    def setUp(self):
        self.baseline = read(BASELINE)
        self.verification = read(VERIFICATION)

    def _assert_traced(self, task_id, ac_id):
        self.assertRegex(self.verification,
                         r"(?m)^## %s — IMPACT `%s`$" % (re.escape(task_id), re.escape(ac_id)))
        self.assertIn("Automated:", section(self.verification, task_id))

    # IMPACT [US-001-ac-1] / T011
    def test_impact_us001_ac_1_exclusion_tied_to_accepted_impact(self):
        self._assert_traced("T011", "US-001-ac-1")
        self.assertIn(ACCEPTED_IMPACT, self.baseline)
        self.assertIn("FR-US-001-AC1", section(self.baseline, "2.3 Out of scope"))

    # IMPACT [US-001-ac-2] / T012
    def test_impact_us001_ac_2_in_scope_limited_to_hr_areas(self):
        self._assert_traced("T012", "US-001-ac-2")
        in_scope = section(self.baseline, "2.2 In scope for HRMS v1.0")
        items = re.findall(r"^\d+\.\s+(.+?)\s+\(", in_scope, re.MULTILINE)
        self.assertEqual(items, ["Employee profiles", "Leave", "Performance", "Payroll"])

    # IMPACT [US-001-ac-3] / T013
    def test_impact_us001_ac_3_nfrs_scoped_to_in_scope_areas_only(self):
        self._assert_traced("T013", "US-001-ac-3")
        nfr = section(self.baseline, "5. Non-functional requirements")
        self.assertIn("apply only to the in-scope areas listed in §2.2", nfr)

    # IMPACT [US-001-ac-4] / T014
    def test_impact_us001_ac_4_future_ids_absent_from_release_scope(self):
        self._assert_traced("T014", "US-001-ac-4")
        features = section(self.baseline, "3. Feature summary")
        row = [l for l in features.splitlines() if l.startswith("| Transactional payments")]
        self.assertEqual(len(row), 1)
        self.assertIn("Out of scope for HRMS v1.0", row[0])
        self.assertIn("HRMS-FUT-PAY-01", row[0])

    # IMPACT [US-001-ac-5] / T015
    def test_impact_us001_ac_5_signoff_references_story_and_clause(self):
        self._assert_traced("T015", "US-001-ac-5")
        signoff = read(SIGNOFF)
        for token in ("US-001", "FR-US-001-AC5", "US-001-ac-5", "HRMS-V1-SCOPE-OUT-01"):
            self.assertIn(token, signoff)


if __name__ == "__main__":
    unittest.main()
