#!/usr/bin/env bash
# S1 build warning gate (story b2ec327d-0260-5051-b2b9-e8e046c072a4; FR-S1-011, FR-S1-012; S1-AC6).
#
# Runs `mvn -B clean compile` with the current JAVA_HOME and fails when the Maven output contains
# [WARNING] lines about Java language level, compiler configuration or deprecated Maven plugins
# (LEVEL_RE), either globally or from the monitoring packages (MONITORING_RE).
# Log: build/s1-logs/compile-jdk<major>.log (build/ is git-ignored).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MVN="${MVN:-mvn}"
MONITORING_RE='com[./]salesmanager[./]core[./]business[./]monitoring|com[./]salesmanager[./]shop[./]monitoring'
LEVEL_RE='source value|target value|source release|release version|bootstrap class ?path|obsolete|not supported|-source|-target|--release|compiler|Plugin validation|deprecated.*(plugin|mojo|parameter|goal)|plugin.*deprecated'

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
MAJOR="$("$JAVA_BIN" -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java.specification.version = //p')"
LOG_DIR="$ROOT/build/s1-logs"
LOG="$LOG_DIR/compile-jdk${MAJOR}.log"
mkdir -p "$LOG_DIR"

cd "$ROOT"
if ! "$MVN" -B clean compile >"$LOG" 2>&1; then
  echo "FAIL: mvn clean compile failed on JDK $MAJOR (see $LOG)"
  exit 1
fi

for dir in sm-core/src/main/java/com/salesmanager/core/business/monitoring sm-shop/src/main/java/com/salesmanager/shop/monitoring; do
  [ -d "$dir" ] || echo "NOTE: monitoring package $dir does not exist (nothing to compile there yet)"
done

WARNINGS="$(grep -E '^\[WARNING\]|^WARNING:' "$LOG" || true)"
LEVEL_WARNINGS="$(printf '%s\n' "$WARNINGS" | grep -Ei "$LEVEL_RE" || true)"
MONITORING_WARNINGS="$(printf '%s\n' "$WARNINGS" | grep -E "$MONITORING_RE" || true)"

echo "JDK $MAJOR: total warnings=$(printf '%s' "$WARNINGS" | grep -c . || true)," \
  "language-level/compiler/plugin warnings=$(printf '%s' "$LEVEL_WARNINGS" | grep -c . || true)," \
  "monitoring-package warnings=$(printf '%s' "$MONITORING_WARNINGS" | grep -c . || true)"

if [ -n "$LEVEL_WARNINGS$MONITORING_WARNINGS" ]; then
  printf '%s\n%s\n' "$LEVEL_WARNINGS" "$MONITORING_WARNINGS" | sed '/^$/d' | sort -u
  echo "FAIL: S1-AC6 warning gate (see $LOG)"
  exit 1
fi
echo "PASS: S1-AC6 warning gate on JDK $MAJOR"
