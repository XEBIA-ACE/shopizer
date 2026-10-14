#!/usr/bin/env python3
"""Static audit of javax.* namespace usage across the Shopizer Maven modules.

Scans every *.java file of the five Maven modules, records each javax.* reference
(imports, static/wildcard imports, inline fully-qualified names) plus the annotation
sites bound to imported javax.* types, and writes:

  javax-inventory.csv       one row per reference / annotation site
  javax-audit-summary.json  aggregates by family, module, layer, file
  JAVAX_AUDIT_REPORT.md     human readable report

Usage:
  python3 tools/javax-audit/javax_audit.py            # regenerate reports
  python3 tools/javax-audit/javax_audit.py --check    # fail if reports are stale
"""

import argparse
import csv
import io
import json
import os
import re
import subprocess
import sys
from collections import Counter, OrderedDict, defaultdict

MODULES = ["sm-core", "sm-core-model", "sm-core-modules", "sm-shop", "sm-shop-model"]
FAMILIES = ["servlet", "persistence", "validation", "ws.rs", "inject", "annotation", "xml", "other"]
# Files outside the five modules (e.g. .mvn/wrapper/MavenWrapperDownloader.java).
ROOT_MODULE = "(root)"
SKIP_DIRS = {".git", "target", "build", "bin", "node_modules"}
LAYERS = ["controller", "service", "repository", "model/DTO", "configuration", "other"]

DEFAULT_OUTPUT_DIR = os.path.join("docs", "migration", "javax-audit")
CSV_NAME = "javax-inventory.csv"
JSON_NAME = "javax-audit-summary.json"
MD_NAME = "JAVAX_AUDIT_REPORT.md"

CSV_FIELDS = [
    "module", "source_set", "layer", "file", "line", "column", "kind", "family",
    "package", "reference", "migration_required", "jakarta_target",
]

# Packages that moved to the jakarta.* namespace with Jakarta EE 9+ (Spring Boot 3).
# Longest prefix wins. Anything under javax.* not listed here is part of the JDK
# (Java SE) and keeps its javax.* name.
JAKARTA_PREFIXES = OrderedDict([
    ("javax.servlet", "jakarta.servlet"),
    ("javax.persistence", "jakarta.persistence"),
    ("javax.validation", "jakarta.validation"),
    ("javax.ws.rs", "jakarta.ws.rs"),
    ("javax.inject", "jakarta.inject"),
    ("javax.annotation.PostConstruct", "jakarta.annotation.PostConstruct"),
    ("javax.annotation.PreDestroy", "jakarta.annotation.PreDestroy"),
    ("javax.annotation.Resource", "jakarta.annotation.Resource"),
    ("javax.annotation.Resources", "jakarta.annotation.Resources"),
    ("javax.annotation.Generated", "jakarta.annotation.Generated"),
    ("javax.annotation.Priority", "jakarta.annotation.Priority"),
    ("javax.annotation.ManagedBean", "jakarta.annotation.ManagedBean"),
    ("javax.annotation.security", "jakarta.annotation.security"),
    ("javax.annotation.sql", "jakarta.annotation.sql"),
    ("javax.xml.bind", "jakarta.xml.bind"),
    ("javax.xml.ws", "jakarta.xml.ws"),
    ("javax.xml.soap", "jakarta.xml.soap"),
    ("javax.jws", "jakarta.jws"),
    ("javax.mail", "jakarta.mail"),
    ("javax.activation", "jakarta.activation"),
    ("javax.el", "jakarta.el"),
    ("javax.transaction", "jakarta.transaction"),
    ("javax.ejb", "jakarta.ejb"),
    ("javax.enterprise", "jakarta.enterprise"),
    ("javax.faces", "jakarta.faces"),
    ("javax.jms", "jakarta.jms"),
    ("javax.json", "jakarta.json"),
    ("javax.websocket", "jakarta.websocket"),
    ("javax.interceptor", "jakarta.interceptor"),
    ("javax.batch", "jakarta.batch"),
    ("javax.decorator", "jakarta.decorator"),
    ("javax.resource", "jakarta.resource"),
    ("javax.security.enterprise", "jakarta.security.enterprise"),
    ("javax.security.auth.message", "jakarta.security.auth.message"),
    ("javax.security.jacc", "jakarta.security.jacc"),
])
# javax.transaction.xa stays in the JDK.
JDK_EXCEPTIONS = ("javax.transaction.xa",)

FAMILY_PREFIXES = [
    ("javax.servlet", "servlet"),
    ("javax.persistence", "persistence"),
    ("javax.validation", "validation"),
    ("javax.ws.rs", "ws.rs"),
    ("javax.inject", "inject"),
    ("javax.annotation", "annotation"),
    ("javax.xml", "xml"),
]

JAVAX_RE = re.compile(r"(?<![\w.$])javax(?:\s*\.\s*[A-Za-z_$][\w$]*)+(?:\s*\.\s*\*)?")
IMPORT_RE = re.compile(r"^\s*import\s+(static\s+)?(javax(?:\s*\.\s*[\w$]+)+(?:\s*\.\s*\*)?)\s*;")
ANNOTATION_RE = re.compile(r"(?<![\w.$])@\s*([A-Z][\w$]*)\b")


def strip_comments_and_strings(text):
    """Blank out comments, string and char literals while keeping offsets/newlines."""
    out = []
    i, n = 0, len(text)
    state = None
    while i < n:
        c = text[i]
        nxt = text[i + 1] if i + 1 < n else ""
        if state is None:
            if c == "/" and nxt == "/":
                state = "line"
                out.append("  ")
                i += 2
                continue
            if c == "/" and nxt == "*":
                state = "block"
                out.append("  ")
                i += 2
                continue
            if text.startswith('"""', i):
                state = "text_block"
                out.append("   ")
                i += 3
                continue
            if c == '"':
                state = "string"
                out.append(" ")
                i += 1
                continue
            if c == "'":
                state = "char"
                out.append(" ")
                i += 1
                continue
            out.append(c)
            i += 1
            continue
        if state == "line":
            if c == "\n":
                state = None
                out.append("\n")
            else:
                out.append(" ")
            i += 1
            continue
        if state == "block":
            if c == "*" and nxt == "/":
                state = None
                out.append("  ")
                i += 2
                continue
            out.append("\n" if c == "\n" else " ")
            i += 1
            continue
        if state == "text_block":
            if c == "\\":
                out.append("  " if nxt != "\n" else " \n")
                i += 2
                continue
            if text.startswith('"""', i):
                state = None
                out.append("   ")
                i += 3
                continue
            out.append("\n" if c == "\n" else " ")
            i += 1
            continue
        # string / char literal
        if c == "\\":
            out.append("  ")
            i += 2
            continue
        if (state == "string" and c == '"') or (state == "char" and c == "'") or c == "\n":
            state = None
        out.append("\n" if c == "\n" else " ")
        i += 1
    return "".join(out)


def normalize(name):
    return re.sub(r"\s+", "", name)


def family_of(reference):
    for prefix, family in FAMILY_PREFIXES:
        if reference == prefix or reference.startswith(prefix + "."):
            return family
    return "other"


def jakarta_target(reference):
    """Return the jakarta.* replacement, or '' when the type stays in the JDK."""
    if any(reference == p or reference.startswith(p + ".") for p in JDK_EXCEPTIONS):
        return ""
    best = None
    for prefix in JAKARTA_PREFIXES:
        if reference == prefix or reference.startswith(prefix + "."):
            if best is None or len(prefix) > len(best):
                best = prefix
    if best is None:
        return ""
    return JAKARTA_PREFIXES[best] + reference[len(best):]


def package_of(reference):
    """Package part of a reference (segments before the first capitalised one)."""
    parts = reference.split(".")
    pkg = []
    for part in parts:
        if part == "*" or part[:1].isupper():
            break
        pkg.append(part)
    return ".".join(pkg)


def module_of(rel_path):
    first = rel_path.split("/", 1)[0]
    return first if first in MODULES else ROOT_MODULE


def source_set_of(rel_path):
    m = re.search(r"/src/([^/]+)/java/", "/" + rel_path)
    return m.group(1) if m else "unknown"


def layer_of(rel_path):
    """Heuristic architectural layer from the package path and file name."""
    module = module_of(rel_path)
    m = re.search(r"/java/(.+)$", rel_path)
    inner = m.group(1) if m else rel_path
    segments = [s.lower() for s in inner.split("/")[:-1]]
    name = os.path.splitext(os.path.basename(rel_path))[0]

    if (set(segments) & {"repository", "repositories", "dao"}) or re.search(
            r"(Repository|RepositoryImpl|RepositoryCustom|Dao|DaoImpl)$", name):
        return "repository"
    if (set(segments) & {"service", "services", "facade", "modules"}) or re.search(
            r"(Service|ServiceImpl|Facade|FacadeImpl)$", name):
        return "service"
    if (set(segments) & {"controller", "controllers", "api", "filter", "rest"}) or re.search(
            r"(Controller|Api|Filter|Resource)$", name):
        return "controller"
    if (set(segments) & {"config", "configuration", "application"}) or re.search(
            r"(Config|Configuration|Configurer|Application)$", name):
        return "configuration"
    if module in ("sm-core-model", "sm-shop-model") or (
            set(segments) & {"model", "entity", "entities", "dto"}):
        return "model/DTO"
    return "other"


def scan_source(text):
    """Return a list of javax.* findings for a Java source text.

    Each finding is a dict with line, column, kind and reference.
    kind is one of: import, static_import, wildcard_import, fqn_reference, annotation_usage.
    """
    code = strip_comments_and_strings(text)
    lines = code.split("\n")
    line_starts = [0]
    for ln in lines[:-1]:
        line_starts.append(line_starts[-1] + len(ln) + 1)

    findings = []
    import_spans = set()
    imported_types = {}  # simple name -> fqn (single-type imports only)

    for idx, ln in enumerate(lines):
        m = IMPORT_RE.match(ln)
        if not m:
            continue
        ref = normalize(m.group(2))
        is_static = bool(m.group(1))
        if ref.endswith(".*"):
            kind = "wildcard_import"
        elif is_static:
            kind = "static_import"
        else:
            kind = "import"
        col = m.start(2)
        findings.append({"line": idx + 1, "column": col + 1, "kind": kind, "reference": ref})
        import_spans.add(line_starts[idx] + col)
        if kind == "import":
            imported_types[ref.rsplit(".", 1)[1]] = ref

    for m in JAVAX_RE.finditer(code):
        if m.start() in import_spans:
            continue
        idx = code.count("\n", 0, m.start())
        col = m.start() - line_starts[idx]
        findings.append({"line": idx + 1, "column": col + 1, "kind": "fqn_reference",
                         "reference": normalize(m.group(0))})

    if imported_types:
        for m in ANNOTATION_RE.finditer(code):
            simple = m.group(1)
            if simple in imported_types:
                idx = code.count("\n", 0, m.start())
                col = m.start() - line_starts[idx]
                findings.append({"line": idx + 1, "column": col + 1, "kind": "annotation_usage",
                                 "reference": imported_types[simple]})

    findings.sort(key=lambda f: (f["line"], f["column"], f["kind"]))
    return findings


def list_java_files(root):
    """All *.java files of the repository, excluding build output and VCS metadata."""
    files = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = sorted(d for d in dirnames if d not in SKIP_DIRS)
        for fn in sorted(filenames):
            if fn.endswith(".java"):
                files.append(os.path.relpath(os.path.join(dirpath, fn), root).replace(os.sep, "/"))
    return sorted(files)


def build_inventory(root):
    files = list_java_files(root)
    rows = []
    for rel in files:
        with open(os.path.join(root, rel), encoding="utf-8", errors="replace") as fh:
            text = fh.read()
        for f in scan_source(text):
            target = jakarta_target(f["reference"])
            rows.append({
                "module": module_of(rel),
                "source_set": source_set_of(rel),
                "layer": layer_of(rel),
                "file": rel,
                "line": f["line"],
                "column": f["column"],
                "kind": f["kind"],
                "family": family_of(f["reference"]),
                "package": package_of(f["reference"]),
                "reference": f["reference"],
                "migration_required": "yes" if target else "no",
                "jakarta_target": target,
            })
    return files, rows


def scan_maven_coordinates(root):
    """List javax.* groupIds/artifactIds declared in the module POMs (input for S8)."""
    coords = []
    for pom in ["pom.xml"] + [m + "/pom.xml" for m in MODULES]:
        path = os.path.join(root, pom)
        if not os.path.exists(path):
            continue
        with open(path, encoding="utf-8") as fh:
            text = re.sub(r"<!--.*?-->", lambda m: "\n" * m.group(0).count("\n"), fh.read(), flags=re.S)
        for m in re.finditer(r"<(dependency|exclusion)>(.*?)</\1>", text, flags=re.S):
            body = m.group(2)
            g = re.search(r"<groupId>\s*([^<\s]+)\s*</groupId>", body)
            a = re.search(r"<artifactId>\s*([^<\s]+)\s*</artifactId>", body)
            if not g or not a:
                continue
            if g.group(1).startswith("javax") or a.group(1).startswith("javax"):
                v = re.search(r"<version>\s*([^<\s]+)\s*</version>", body)
                coords.append({
                    "pom": pom,
                    "line": text.count("\n", 0, m.start()) + 1,
                    "element": m.group(1),
                    "groupId": g.group(1),
                    "artifactId": a.group(1),
                    "version": v.group(1) if v else "",
                })
    return coords


def git_head(root):
    try:
        return subprocess.check_output(["git", "-C", root, "rev-parse", "HEAD"],
                                       stderr=subprocess.DEVNULL).decode().strip()
    except Exception:
        return "unknown"


def _counter_dict(counter, keys=None):
    if keys is None:
        keys = sorted(counter, key=lambda k: (-counter[k], k))
    return OrderedDict((k, counter.get(k, 0)) for k in keys)


def summarize(files, rows, maven_coords, commit):
    refs = [r for r in rows if r["kind"] != "annotation_usage"]
    ann = [r for r in rows if r["kind"] == "annotation_usage"]
    mig = [r for r in refs if r["migration_required"] == "yes"]
    mig_ann = [r for r in ann if r["migration_required"] == "yes"]

    files_per_module = Counter(module_of(f) for f in files)
    module_keys = MODULES + ([ROOT_MODULE] if files_per_module.get(ROOT_MODULE) else [])
    files_per_layer = Counter(layer_of(f) for f in files)

    def group(rows_, key, keys):
        out = OrderedDict()
        for k in keys:
            sub = [r for r in rows_ if r[key] == k]
            out[k] = sub
        return out

    def section(key, keys, java_files_counter):
        result = OrderedDict()
        total_refs = len(refs) or 1
        by = group(refs, key, keys)
        by_ann = group(ann, key, keys)
        for k in keys:
            sub = by[k]
            msub = [r for r in sub if r["migration_required"] == "yes"]
            entry = OrderedDict()
            if java_files_counter is not None:
                entry["java_files"] = java_files_counter.get(k, 0)
            entry["files_with_javax"] = len({r["file"] for r in sub})
            entry["files_requiring_migration"] = len({r["file"] for r in msub})
            entry["references"] = len(sub)
            entry["references_requiring_migration"] = len(msub)
            entry["share_of_references_pct"] = round(100.0 * len(sub) / total_refs, 2)
            entry["annotation_usages"] = len(by_ann[k])
            entry["by_family"] = _counter_dict(Counter(r["family"] for r in sub), FAMILIES)
            if key != "layer":
                entry["by_layer"] = _counter_dict(Counter(r["layer"] for r in sub), LAYERS)
            if key != "module":
                entry["by_module"] = _counter_dict(Counter(r["module"] for r in sub), module_keys)
            result[k] = entry
        return result

    by_family = OrderedDict()
    for fam in FAMILIES:
        sub = [r for r in refs if r["family"] == fam]
        msub = [r for r in sub if r["migration_required"] == "yes"]
        pkgs = Counter(r["package"] for r in sub)
        types = Counter(r["reference"] for r in sub)
        by_family[fam] = OrderedDict([
            ("references", len(sub)),
            ("references_requiring_migration", len(msub)),
            ("files", len({r["file"] for r in sub})),
            ("annotation_usages", len([r for r in ann if r["family"] == fam])),
            ("packages", _counter_dict(pkgs)),
            ("top_types", _counter_dict(Counter(dict(types.most_common(10))))),
            ("jakarta_targets", sorted({r["package"] + " -> " + package_of(r["jakarta_target"])
                                        if r["jakarta_target"] else r["package"] + " -> (JDK, unchanged)"
                                        for r in sub})),
        ])

    # Top namespace families/packages ranked by references, with representative locations.
    pkg_counter = Counter(r["package"] for r in refs)
    top_namespaces = []
    for pkg, count in sorted(pkg_counter.items(), key=lambda kv: (-kv[1], kv[0]))[:10]:
        sub = [r for r in refs if r["package"] == pkg]
        # One location per module first, then further distinct files, then further lines.
        ordered = sorted(sub, key=lambda r: (r["module"], r["file"], r["line"]))
        reps, seen_modules, seen_files = [], set(), set()
        for r in ordered:
            if r["module"] not in seen_modules:
                seen_modules.add(r["module"])
                seen_files.add(r["file"])
                reps.append("%s:%d" % (r["file"], r["line"]))
        for r in ordered:
            if r["file"] not in seen_files:
                seen_files.add(r["file"])
                reps.append("%s:%d" % (r["file"], r["line"]))
        for r in ordered:
            loc = "%s:%d" % (r["file"], r["line"])
            if loc not in reps:
                reps.append(loc)
        reps = reps[:3]
        migrating = [r for r in sub if r["migration_required"] == "yes"]
        if not migrating:
            ns_migration, ns_target = "no", ""
        else:
            ns_migration = "yes" if len(migrating) == len(sub) else "partial"
            ns_target = ", ".join(sorted({package_of(r["jakarta_target"]) for r in migrating}))
        top_namespaces.append(OrderedDict([
            ("rank", len(top_namespaces) + 1),
            ("namespace", pkg),
            ("family", family_of(pkg)),
            ("references", count),
            ("files", len({r["file"] for r in sub})),
            ("annotation_usages", len([r for r in ann if r["package"] == pkg])),
            ("migration_required", ns_migration),
            ("jakarta_target", ns_target),
            ("representative_locations", reps),
        ]))

    per_file = []
    for f in sorted({r["file"] for r in refs}):
        sub = [r for r in refs if r["file"] == f]
        per_file.append(OrderedDict([
            ("file", f),
            ("module", module_of(f)),
            ("source_set", source_set_of(f)),
            ("layer", layer_of(f)),
            ("references", len(sub)),
            ("references_requiring_migration", len([r for r in sub if r["migration_required"] == "yes"])),
            ("annotation_usages", len([r for r in ann if r["file"] == f])),
            ("families", sorted({r["family"] for r in sub}, key=FAMILIES.index)),
        ]))

    kinds = _counter_dict(Counter(r["kind"] for r in rows),
                          ["import", "static_import", "wildcard_import", "fqn_reference", "annotation_usage"])

    return OrderedDict([
        ("schema_version", 1),
        ("generator", "tools/javax-audit/javax_audit.py"),
        ("commit", commit),
        ("scope", OrderedDict([
            ("modules", MODULES),
            ("java_files_scanned", len(files)),
            ("java_files_per_module", _counter_dict(files_per_module, module_keys)),
            ("source_sets", _counter_dict(Counter(source_set_of(f) for f in files))),
        ])),
        ("definitions", OrderedDict([
            ("reference", "One textual javax.* occurrence in Java code (import, static/wildcard import, or "
                          "inline fully-qualified name). Comments and string literals are ignored. "
                          "Each reference is one edit in a namespace migration."),
            ("annotation_usage", "An @Annotation site whose simple name is bound to a single-type javax.* "
                                 "import in the same file. Not an edit by itself (it follows the import), "
                                 "reported to size the annotation surface."),
            ("migration_required", "yes when the type moved to jakarta.* in Jakarta EE 9+ (Spring Boot 3); "
                                   "no when it is part of the JDK (e.g. javax.crypto, javax.imageio)."),
            ("layer", "Heuristic from package path / file name, first match wins: repository "
                      "(repository|repositories|dao, *Repository*), service (service|services|facade|modules, "
                      "*Service*, *Facade*), controller (controller|api|filter|rest, *Controller, *Api, *Filter), "
                      "configuration (config|configuration|application, *Config*, *Application), model/DTO "
                      "(sm-core-model, sm-shop-model, model|entity|dto packages), else other."),
        ])),
        ("totals", OrderedDict([
            ("files_with_javax", len({r["file"] for r in refs})),
            ("files_requiring_migration", len({r["file"] for r in mig})),
            ("references", len(refs)),
            ("references_requiring_migration", len(mig)),
            ("references_jdk_unchanged", len(refs) - len(mig)),
            ("annotation_usages", len(ann)),
            ("annotation_usages_requiring_migration", len(mig_ann)),
            ("by_kind", kinds),
        ])),
        ("by_family", by_family),
        ("by_module", section("module", module_keys, files_per_module)),
        ("by_layer", section("layer", LAYERS, files_per_layer)),
        ("by_source_set", section("source_set", sorted({r["source_set"] for r in refs}), None)),
        ("top_namespaces", top_namespaces),
        ("maven_coordinates", maven_coords),
        ("files", per_file),
    ])


def render_csv(rows):
    buf = io.StringIO()
    w = csv.DictWriter(buf, fieldnames=CSV_FIELDS, lineterminator="\n")
    w.writeheader()
    for r in rows:
        w.writerow(r)
    return buf.getvalue()


def render_json(summary):
    return json.dumps(summary, indent=2) + "\n"


def _table(headers, data):
    lines = ["| " + " | ".join(headers) + " |",
             "|" + "|".join("---" for _ in headers) + "|"]
    for row in data:
        lines.append("| " + " | ".join(str(c) for c in row) + " |")
    return "\n".join(lines)


def render_markdown(summary):
    t = summary["totals"]
    s = summary["scope"]
    out = []
    out.append("# javax.* Namespace Usage Audit")
    out.append("")
    out.append("Generated by `%s` at commit `%s`. Do not edit by hand; rerun the generator." % (
        summary["generator"], summary["commit"]))
    out.append("")
    out.append("Machine-readable outputs: [`%s`](%s) (one row per finding) and [`%s`](%s) (all aggregates)." % (
        CSV_NAME, CSV_NAME, JSON_NAME, JSON_NAME))
    out.append("")
    out.append("## Migration scope")
    out.append("")
    out.append("Scanned **%d** Java files (%d Maven modules plus repository-root build tooling). **%d files** contain javax.* references; "
               "**%d files** and **%d individual references** require conversion to jakarta.* "
               "(%d further references are JDK packages that keep the javax.* name). "
               "These references bind **%d** annotation sites (%d on types that move to jakarta.*), "
               "which follow their import and need no separate edit." % (
                   s["java_files_scanned"], len(s["modules"]), t["files_with_javax"],
                   t["files_requiring_migration"], t["references_requiring_migration"],
                   t["references_jdk_unchanged"], t["annotation_usages"],
                   t["annotation_usages_requiring_migration"]))
    out.append("")
    out.append(_table(["Metric", "Value"], [
        ["Java files scanned", s["java_files_scanned"]],
        ["Files with javax.* references", t["files_with_javax"]],
        ["Files requiring jakarta.* migration", t["files_requiring_migration"]],
        ["javax.* references (total)", t["references"]],
        ["References requiring migration", t["references_requiring_migration"]],
        ["References staying javax.* (JDK)", t["references_jdk_unchanged"]],
        ["Annotation usages bound to javax.* imports", t["annotation_usages"]],
    ] + [["Findings of kind `%s`" % k, v] for k, v in t["by_kind"].items()]))
    out.append("")
    out.append("## Inventory by namespace family")
    out.append("")
    rows = []
    for fam, e in summary["by_family"].items():
        rows.append([fam, e["references"], e["references_requiring_migration"], e["files"],
                     e["annotation_usages"],
                     "<br>".join(e["jakarta_targets"]) if e["jakarta_targets"] else "-"])
    out.append(_table(["Family", "References", "Requiring migration", "Files", "Annotation usages",
                       "Package mapping"], rows))
    out.append("")
    out.append("## Summary by module")
    out.append("")
    rows = []
    for mod, e in summary["by_module"].items():
        rows.append([mod, e["java_files"], e["files_with_javax"], e["files_requiring_migration"],
                     e["references"], e["references_requiring_migration"], "%.2f%%" % e["share_of_references_pct"],
                     e["annotation_usages"]])
    out.append(_table(["Module", "Java files", "Files w/ javax", "Files to migrate", "References",
                       "Refs to migrate", "Share", "Annotation usages"], rows))
    out.append("")
    out.append("Distribution by family (references):")
    out.append("")
    out.append(_table(["Module"] + FAMILIES,
                      [[m] + [e["by_family"][f] for f in FAMILIES] for m, e in summary["by_module"].items()]))
    out.append("")
    out.append("## Summary by architectural layer")
    out.append("")
    rows = []
    for layer, e in summary["by_layer"].items():
        rows.append([layer, e["java_files"], e["files_with_javax"], e["files_requiring_migration"],
                     e["references"], e["references_requiring_migration"], "%.2f%%" % e["share_of_references_pct"],
                     e["annotation_usages"]])
    out.append(_table(["Layer", "Java files", "Files w/ javax", "Files to migrate", "References",
                       "Refs to migrate", "Share", "Annotation usages"], rows))
    out.append("")
    out.append("Distribution by family (references):")
    out.append("")
    out.append(_table(["Layer"] + FAMILIES,
                      [[l] + [e["by_family"][f] for f in FAMILIES] for l, e in summary["by_layer"].items()]))
    out.append("")
    out.append("Layer classification rule: %s" % summary["definitions"]["layer"])
    out.append("")
    out.append("## Main vs test sources")
    out.append("")
    out.append(_table(["Source set", "Files w/ javax", "References", "Refs to migrate"],
                      [[k, e["files_with_javax"], e["references"], e["references_requiring_migration"]]
                       for k, e in summary["by_source_set"].items()]))
    out.append("")
    out.append("## Top javax.* namespaces")
    out.append("")
    rows = []
    for e in summary["top_namespaces"]:
        rows.append([e["rank"], "`%s`" % e["namespace"], e["family"], e["references"], e["files"],
                     e["annotation_usages"], e["jakarta_target"] or "(JDK, unchanged)",
                     "<br>".join("`%s`" % l for l in e["representative_locations"])])
    out.append(_table(["#", "Namespace", "Family", "References", "Files", "Annotation usages",
                       "Target", "Representative locations (file:line)"], rows))
    out.append("")
    out.append("## javax.* Maven coordinates (input for S8)")
    out.append("")
    if summary["maven_coordinates"]:
        out.append(_table(["POM", "Line", "Element", "groupId", "artifactId", "version"],
                          [[c["pom"], c["line"], c["element"], c["groupId"], c["artifactId"], c["version"] or "-"]
                           for c in summary["maven_coordinates"]]))
    else:
        out.append("None.")
    out.append("")
    out.append("## Definitions and limitations")
    out.append("")
    for k, v in summary["definitions"].items():
        out.append("- **%s**: %s" % (k, v))
    out.append("- Scope is every `*.java` file in the repository (main and test sources of the five modules, plus "
               "build tooling under `.mvn/`), excluding `target/` build output. Non-Java resources "
               "(XML, properties, templates) and transitive dependencies that use javax.* internally are "
               "not covered and must be assessed in S8.")
    out.append("- Annotation usages are only resolved for single-type imports; wildcard imports and same-named "
               "types from other packages are not resolved (none occur in this codebase at this commit).")
    out.append("- Layer assignment is a path/name heuristic; per-file layers are listed in `%s` for review." % JSON_NAME)
    out.append("")
    return "\n".join(out)


def generate(root):
    files, rows = build_inventory(root)
    summary = summarize(files, rows, scan_maven_coordinates(root), git_head(root))
    return OrderedDict([
        (CSV_NAME, render_csv(rows)),
        (JSON_NAME, render_json(summary)),
        (MD_NAME, render_markdown(summary)),
    ])


def main(argv=None):
    here = os.path.dirname(os.path.abspath(__file__))
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--root", default=os.path.abspath(os.path.join(here, "..", "..")),
                        help="repository root (default: two levels above this script)")
    parser.add_argument("--output-dir", default=None, help="output directory (default: %s)" % DEFAULT_OUTPUT_DIR)
    parser.add_argument("--check", action="store_true",
                        help="do not write; exit 1 if committed outputs differ from a fresh scan "
                             "(the commit field is ignored)")
    args = parser.parse_args(argv)
    out_dir = args.output_dir or os.path.join(args.root, DEFAULT_OUTPUT_DIR)
    outputs = generate(args.root)

    if args.check:
        stale = []
        for name, content in outputs.items():
            path = os.path.join(out_dir, name)
            existing = open(path, encoding="utf-8").read() if os.path.exists(path) else None
            if existing is None or _strip_commit(existing) != _strip_commit(content):
                stale.append(name)
        if stale:
            print("javax audit outputs are stale: %s" % ", ".join(stale), file=sys.stderr)
            return 1
        print("javax audit outputs are up to date")
        return 0

    os.makedirs(out_dir, exist_ok=True)
    for name, content in outputs.items():
        with open(os.path.join(out_dir, name), "w", encoding="utf-8", newline="\n") as fh:
            fh.write(content)
    print("wrote %s" % ", ".join(os.path.join(out_dir, n) for n in outputs))
    return 0


def _strip_commit(text):
    text = re.sub(r'"commit": "[^"]*"', '"commit": ""', text)
    return re.sub(r"at commit `[^`]*`", "at commit ``", text)


if __name__ == "__main__":
    sys.exit(main())
