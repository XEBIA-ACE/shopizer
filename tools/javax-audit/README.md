# javax.* namespace audit

Static scan of all `*.java` sources for `javax.*` usage, used to size the Jakarta EE
(`jakarta.*`) namespace migration required by Spring Boot 3. The scan only reads
sources; it does not modify code.

Outputs (committed under `docs/migration/javax-audit/`):

- `JAVAX_AUDIT_REPORT.md` - migration scope statement and summaries by family, module, layer, top namespaces
- `javax-inventory.csv` - one row per finding (file, line, column, kind, family, package, jakarta target)
- `javax-audit-summary.json` - all aggregates, per-file breakdown and javax.* Maven coordinates

```bash
# regenerate the reports (Python 3.8+, no dependencies)
python3 tools/javax-audit/javax_audit.py

# fail if the committed reports no longer match the sources
python3 tools/javax-audit/javax_audit.py --check

# tests
python3 -m unittest discover -s tools/javax-audit -v
```
