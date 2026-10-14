"""Tests for javax_audit.py.

Run from the repository root:  python3 -m unittest discover -s tools/javax-audit -v
"""

import csv
import io
import json
import os
import re
import subprocess
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
OUT_DIR = os.path.join(ROOT, "docs", "migration", "javax-audit")
sys.path.insert(0, HERE)

import javax_audit as audit  # noqa: E402

AUDITED_COMMIT = "6a4a0a65a3408ee8f62597b51d1b3aac24b77dee"


class ScanSourceTest(unittest.TestCase):

    def test_detects_import_kinds_and_inline_references(self):
        src = "\n".join([
            "package a;",
            "import javax.persistence.Entity;",
            "import static javax.persistence.FetchType.LAZY;",
            "import javax.validation.constraints.*;",
            "import java.util.List;",
            "@Entity",
            "class A {",
            "  @javax.persistence.Transient int x;",
            "  void f() { try {} catch (javax.persistence.NoResultException e) {} }",
            "}",
        ])
        found = [(f["line"], f["kind"], f["reference"]) for f in audit.scan_source(src)]
        self.assertEqual(found, [
            (2, "import", "javax.persistence.Entity"),
            (3, "static_import", "javax.persistence.FetchType.LAZY"),
            (4, "wildcard_import", "javax.validation.constraints.*"),
            (6, "annotation_usage", "javax.persistence.Entity"),
            (8, "fqn_reference", "javax.persistence.Transient"),
            (9, "fqn_reference", "javax.persistence.NoResultException"),
        ])

    def test_ignores_comments_strings_and_lookalikes(self):
        src = "\n".join([
            "// import javax.servlet.Filter;",
            "/* javax.persistence.Entity",
            "   javax.inject.Inject */",
            "class B {",
            '  String s = "javax.ws.rs.Path";',
            "  char c = 'j';",
            "  String t = \"\"\"",
            "     javax.xml.bind.JAXB",
            "     \"\"\";",
            "  Object o = myjavax.Thing; Object p = foo.javax.Bar;",
            "  @Override public String toString() { return null; }",
            "}",
        ])
        self.assertEqual(audit.scan_source(src), [])

    def test_reports_column_and_preserves_line_numbers_after_block_comment(self):
        src = "/*\n\n*/\nimport javax.inject.Inject;\nclass C { @Inject Object a; @Inject Object b; }\n"
        found = audit.scan_source(src)
        self.assertEqual([(f["line"], f["column"], f["kind"]) for f in found],
                         [(4, 8, "import"), (5, 11, "annotation_usage"), (5, 29, "annotation_usage")])


class ClassificationTest(unittest.TestCase):

    def test_family(self):
        cases = {
            "javax.servlet.http.HttpServletRequest": "servlet",
            "javax.persistence.Entity": "persistence",
            "javax.validation.constraints.NotNull": "validation",
            "javax.ws.rs.core.MediaType": "ws.rs",
            "javax.inject.Inject": "inject",
            "javax.annotation.PostConstruct": "annotation",
            "javax.xml.bind.JAXBContext": "xml",
            "javax.mail.Message": "other",
            "javax.crypto.Cipher": "other",
            "javax.servletx.Foo": "other",
        }
        for ref, family in cases.items():
            self.assertEqual(audit.family_of(ref), family, ref)

    def test_jakarta_target(self):
        self.assertEqual(audit.jakarta_target("javax.persistence.Entity"), "jakarta.persistence.Entity")
        self.assertEqual(audit.jakarta_target("javax.servlet.http.HttpServletRequest"),
                         "jakarta.servlet.http.HttpServletRequest")
        self.assertEqual(audit.jakarta_target("javax.annotation.PostConstruct"), "jakarta.annotation.PostConstruct")
        self.assertEqual(audit.jakarta_target("javax.mail.internet.MimeMessage"), "jakarta.mail.internet.MimeMessage")
        self.assertEqual(audit.jakarta_target("javax.validation.constraints.*"), "jakarta.validation.constraints.*")
        for jdk in ("javax.crypto.Cipher", "javax.imageio.ImageIO", "javax.annotation.Nullable",
                    "javax.xml.parsers.DocumentBuilder", "javax.transaction.xa.XAResource", "javax.net.ssl.SSLContext"):
            self.assertEqual(audit.jakarta_target(jdk), "", jdk)

    def test_package(self):
        self.assertEqual(audit.package_of("javax.servlet.http.HttpServletRequest"), "javax.servlet.http")
        self.assertEqual(audit.package_of("javax.persistence.FetchType.LAZY"), "javax.persistence")
        self.assertEqual(audit.package_of("javax.validation.constraints.*"), "javax.validation.constraints")

    def test_module_source_set_and_layer(self):
        base = "sm-shop/src/main/java/com/salesmanager/shop/"
        self.assertEqual(audit.module_of(base + "X.java"), "sm-shop")
        self.assertEqual(audit.module_of(".mvn/wrapper/MavenWrapperDownloader.java"), audit.ROOT_MODULE)
        self.assertEqual(audit.source_set_of("sm-core/src/test/java/a/B.java"), "test")
        cases = {
            "sm-core/src/main/java/com/salesmanager/core/business/repositories/user/UserRepositoryImpl.java":
                "repository",
            "sm-core/src/main/java/com/salesmanager/core/business/services/user/UserServiceImpl.java": "service",
            base + "store/controller/order/facade/OrderFacadeImpl.java": "service",
            base + "store/api/v1/order/OrderApi.java": "controller",
            base + "filter/CorsFilter.java": "controller",
            base + "application/config/ShopApplicationConfiguration.java": "configuration",
            "sm-core-model/src/main/java/com/salesmanager/core/model/catalog/catalog/Catalog.java": "model/DTO",
            "sm-shop-model/src/main/java/com/salesmanager/shop/model/customer/CustomerEntity.java": "model/DTO",
            base + "utils/ImageFilePath.java": "other",
        }
        for path, layer in cases.items():
            self.assertEqual(audit.layer_of(path), layer, path)


class RepositoryCharacterizationTest(unittest.TestCase):
    """Characterizes the committed audit against the repository sources (S3-AC1..S3-AC6)."""

    @classmethod
    def setUpClass(cls):
        cls.files, cls.rows = audit.build_inventory(ROOT)
        with open(os.path.join(OUT_DIR, audit.JSON_NAME), encoding="utf-8") as fh:
            cls.summary = json.load(fh)
        with open(os.path.join(OUT_DIR, audit.CSV_NAME), encoding="utf-8") as fh:
            cls.csv_rows = list(csv.DictReader(fh))
        with open(os.path.join(OUT_DIR, audit.MD_NAME), encoding="utf-8") as fh:
            cls.report = fh.read()
        cls.refs = [r for r in cls.rows if r["kind"] != "annotation_usage"]

    def _git_java_files(self):
        try:
            out = subprocess.check_output(["git", "-C", ROOT, "ls-files", "*.java"], stderr=subprocess.DEVNULL)
        except Exception:
            self.skipTest("git not available")
        return sorted(out.decode().split())

    # S3-AC1
    def test_ac1_scan_covers_every_java_file(self):
        self.assertEqual(self.files, self._git_java_files())
        self.assertEqual(self.summary["scope"]["java_files_scanned"], len(self.files))
        if self.summary["commit"] == AUDITED_COMMIT:
            self.assertEqual(len(self.files), 1210)

    def test_ac1_every_javax_token_in_code_is_inventoried(self):
        expected = 0
        for rel in self.files:
            with open(os.path.join(ROOT, rel), encoding="utf-8", errors="replace") as fh:
                code = audit.strip_comments_and_strings(fh.read())
            expected += len(re.findall(r"(?<![\w.$])javax\s*\.", code))
        self.assertEqual(len(self.refs), expected)
        self.assertEqual(self.summary["totals"]["references"], expected)

    def test_ac1_categorized_by_required_families(self):
        self.assertEqual(list(self.summary["by_family"]), audit.FAMILIES)
        self.assertTrue(all(r["family"] in audit.FAMILIES for r in self.csv_rows))
        self.assertEqual(sum(e["references"] for e in self.summary["by_family"].values()),
                         self.summary["totals"]["references"])

    def test_ac1_inventory_rows_point_at_real_source_lines(self):
        cache = {}
        for r in self.csv_rows:
            if r["file"] not in cache:
                with open(os.path.join(ROOT, r["file"]), encoding="utf-8", errors="replace") as fh:
                    cache[r["file"]] = fh.read().split("\n")
            line = cache[r["file"]][int(r["line"]) - 1]
            if r["kind"] == "annotation_usage":
                self.assertIn("@" + r["reference"].rsplit(".", 1)[1], line)
            else:
                self.assertEqual(re.sub(r"\s+", "", line[int(r["column"]) - 1:]).find(r["reference"]), 0,
                                 "%s:%s" % (r["file"], r["line"]))

    # S3-AC2
    def test_ac2_module_summary(self):
        by_module = self.summary["by_module"]
        for module in audit.MODULES:
            self.assertIn(module, by_module)
            self.assertEqual(sum(by_module[module]["by_family"].values()), by_module[module]["references"])
        self.assertEqual(sum(e["references"] for e in by_module.values()), self.summary["totals"]["references"])
        self.assertEqual(sum(e["java_files"] for e in by_module.values()), len(self.files))
        self.assertAlmostEqual(sum(e["share_of_references_pct"] for e in by_module.values()), 100.0, delta=0.1)

    # S3-AC3
    def test_ac3_layer_summary(self):
        by_layer = self.summary["by_layer"]
        self.assertEqual(list(by_layer), audit.LAYERS)
        self.assertEqual(sum(e["references"] for e in by_layer.values()), self.summary["totals"]["references"])
        self.assertEqual(sum(e["files_with_javax"] for e in by_layer.values()),
                         self.summary["totals"]["files_with_javax"])
        for entry in self.summary["files"]:
            self.assertEqual(entry["layer"], audit.layer_of(entry["file"]))

    # S3-AC4
    def test_ac4_top_namespaces_with_locations(self):
        top = self.summary["top_namespaces"]
        self.assertTrue(5 <= len(top) <= 10)
        counts = [e["references"] for e in top]
        self.assertEqual(counts, sorted(counts, reverse=True))
        for e in top:
            self.assertTrue(e["representative_locations"])
            for loc in e["representative_locations"]:
                path, line = loc.rsplit(":", 1)
                with open(os.path.join(ROOT, path), encoding="utf-8") as fh:
                    self.assertIn(e["namespace"] + ".", fh.read().split("\n")[int(line) - 1])

    # S3-AC5
    def test_ac5_migration_scope_statement(self):
        t = self.summary["totals"]
        mig = [r for r in self.refs if r["migration_required"] == "yes"]
        self.assertEqual(t["references_requiring_migration"], len(mig))
        self.assertEqual(t["files_requiring_migration"], len({r["file"] for r in mig}))
        self.assertEqual(t["references"], t["references_requiring_migration"] + t["references_jdk_unchanged"])
        self.assertIn("**%d files** and **%d individual references** require conversion" % (
            t["files_requiring_migration"], t["references_requiring_migration"]), self.report)

    # S3-AC6
    def test_ac6_structured_outputs_are_complete_and_current(self):
        self.assertEqual(list(self.csv_rows[0].keys()), audit.CSV_FIELDS)
        self.assertEqual(len(self.csv_rows), len(self.rows))
        for header in ("## Migration scope", "## Inventory by namespace family", "## Summary by module",
                       "## Summary by architectural layer", "## Top javax.* namespaces"):
            self.assertIn(header, self.report)
        stderr = io.StringIO()
        old, sys.stderr = sys.stderr, stderr
        try:
            self.assertEqual(audit.main(["--root", ROOT, "--check"]), 0, stderr.getvalue())
        finally:
            sys.stderr = old


if __name__ == "__main__":
    unittest.main()
