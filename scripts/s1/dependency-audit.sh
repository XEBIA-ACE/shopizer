#!/usr/bin/env bash
# S1 dependency audit (story b2ec327d-0260-5051-b2b9-e8e046c072a4; FR-S1-005, FR-S1-006; S1-AC3).
# Writes deterministic, reviewable reports to docs/dependency-audit/S1/reports (or $1).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT_DIR="$(mkdir -p "${1:-$ROOT/docs/dependency-audit/S1/reports}" && cd "${1:-$ROOT/docs/dependency-audit/S1/reports}" && pwd)"
DEP_PLUGIN="org.apache.maven.plugins:maven-dependency-plugin:3.6.1"
MVN="${MVN:-mvn}"

cd "$ROOT"
"$MVN" -B -q "$DEP_PLUGIN:tree" -DoutputType=text \
  "-DoutputFile=$OUT_DIR/dependency-tree-\${project.artifactId}.txt"
"$MVN" -B -q "$DEP_PLUGIN:tree" -DoutputType=text -Dverbose \
  "-DoutputFile=$OUT_DIR/dependency-tree-verbose-\${project.artifactId}.txt"
python3 "$ROOT/scripts/s1/summarize_dependency_trees.py" "$OUT_DIR"
echo "S1 dependency reports written to $OUT_DIR"
