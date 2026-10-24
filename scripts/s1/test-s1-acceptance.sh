#!/usr/bin/env bash
# S1 acceptance tests (story b2ec327d-0260-5051-b2b9-e8e046c072a4).
# One test function per acceptance criterion: test_S1_AC1 .. test_S1_AC6.
# Usage: scripts/s1/test-s1-acceptance.sh [S1-AC1 ...]   (default: all)
# JDKs for S1-AC2/S1-AC6: JAVA17_HOME and JAVA21_HOME (auto-detected under /usr/lib/jvm and $JAVA_HOME).
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DOC_DIR="$ROOT/docs/dependency-audit/S1"
REPORT_DIR="$DOC_DIR/reports"
WORK_DIR="$ROOT/build/s1-logs"
MVN="${MVN:-mvn}"
MODULES=(shopizer sm-core-model sm-core-modules sm-core sm-shop-model sm-shop)
mkdir -p "$WORK_DIR"
cd "$ROOT"

java_major() { "$1/bin/java" -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java.specification.version = //p'; }

find_jdk() {
  local want="$1" candidate
  for candidate in "${2:-}" "${JAVA_HOME:-}" /usr/lib/jvm/* "$HOME"/.local/tools/jdk*; do
    if [ -n "$candidate" ] && [ -x "$candidate/bin/javac" ] && [ "$(java_major "$candidate")" = "$want" ]; then
      echo "$candidate"; return 0
    fi
  done
  return 1
}

# [S1-AC1] FR-S1-001, FR-S1-002: effective maven-compiler-plugin <source>/<target> is 17 or 21 and identical in every module.
test_S1_AC1() {
  local effective="$WORK_DIR/effective-pom.xml"
  "$MVN" -B -q org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom "-Doutput=$effective" >/dev/null || return 1
  python3 - "$effective" "${MODULES[@]}" <<'PY'
import sys, xml.etree.ElementTree as ET
ns = {"m": "http://maven.apache.org/POM/4.0.0"}
root = ET.parse(sys.argv[1]).getroot()
projects = root.findall("m:project", ns) if root.tag.endswith("projects") else [root]
seen = {}
for p in projects:
    aid = p.find("m:artifactId", ns).text
    for plugin in p.findall("m:build/m:plugins/m:plugin", ns):
        if plugin.find("m:artifactId", ns).text == "maven-compiler-plugin":
            cfg = plugin.find("m:configuration", ns)
            seen[aid] = (cfg.findtext("m:source", None, ns), cfg.findtext("m:target", None, ns))
missing = set(sys.argv[2:]) - set(seen)
levels = set(seen.values())
for aid in sys.argv[2:]:
    print(f"  {aid}: source/target={seen.get(aid)}")
ok = not missing and len(levels) == 1 and next(iter(levels)) in {("17", "17"), ("21", "21")}
sys.exit(0 if ok else 1)
PY
}

# [S1-AC2] FR-S1-003, FR-S1-004: mvn clean compile exits 0 on JDK 17 and on JDK 21.
test_S1_AC2() {
  local rc=0 v home
  for v in 17 21; do
    local override_var="JAVA${v}_HOME"
    if ! home="$(find_jdk "$v" "${!override_var:-}")"; then
      echo "  JDK $v not found (set $override_var)"; rc=1; continue
    fi
    if JAVA_HOME="$home" "$MVN" -B clean compile >"$WORK_DIR/ac2-compile-jdk$v.log" 2>&1; then
      echo "  JDK $v ($home): BUILD SUCCESS"
    else
      echo "  JDK $v ($home): BUILD FAILURE (see $WORK_DIR/ac2-compile-jdk$v.log)"; rc=1
    fi
  done
  return $rc
}

# [S1-AC3] FR-S1-005, FR-S1-006: committed report is reproducible and covers Spring Boot, AspectJ, SLF4J, Logback, JUnit 5.
test_S1_AC3() {
  local fresh="$WORK_DIR/dependency-report" rc=0
  rm -rf "$fresh"
  "$ROOT/scripts/s1/dependency-audit.sh" "$fresh" >/dev/null || return 1
  if ! diff -r "$REPORT_DIR" "$fresh" >"$WORK_DIR/ac3-report.diff"; then
    echo "  committed report under docs/dependency-audit/S1/reports is stale (see $WORK_DIR/ac3-report.diff)"; rc=1
  fi
  local m pattern
  for m in "${MODULES[@]}"; do
    [ -s "$REPORT_DIR/dependency-tree-$m.txt" ] || { echo "  missing tree for $m"; rc=1; }
  done
  for pattern in "org.springframework.boot:spring-boot:" "org.aspectj:aspectjweaver:" "org.slf4j:slf4j-api:" \
                 "ch.qos.logback:logback-classic:" "org.junit.jupiter:junit-jupiter-api:"; do
    if grep -q "$pattern" "$REPORT_DIR"/dependency-tree-sm-shop.txt "$REPORT_DIR"/dependency-tree-sm-core.txt; then
      echo "  found $pattern"
    else
      echo "  missing $pattern"; rc=1
    fi
  done
  for section in "## Spring Boot" "## AspectJ" "## SLF4J" "## Logback" "## JUnit 5"; do
    grep -qx "$section" "$REPORT_DIR/dependency-summary.md" || { echo "  summary lacks '$section'"; rc=1; }
  done
  return $rc
}

# [S1-AC4] FR-S1-007, FR-S1-008: every javax.* / Jakarta EE 8 artifact and every version conflict in the
# resolved graph is documented in spring-boot-3-migration-conflicts.md.
test_S1_AC4() {
  local doc="$DOC_DIR/spring-boot-3-migration-conflicts.md" rc=0 ga
  [ -s "$doc" ] || { echo "  missing $doc"; return 1; }
  while read -r ga; do
    grep -qF "$ga" "$doc" || { echo "  undocumented javax/jakarta artifact: $ga"; rc=1; }
  done < <(awk '/^## javax/,/^## Logging/' "$REPORT_DIR/dependency-summary.md" | awk -F' \\| ' 'NF>2 && $2 ~ /:/ {print $2}' | sort -u)
  while read -r ga; do
    grep -qF "$ga" "$doc" || { echo "  undocumented version conflict: $ga"; rc=1; }
  done < <(awk '/^## Version conflicts/,/^## Versions overridden/' "$REPORT_DIR/dependency-summary.md" | awk -F' \\| ' 'NF>2 && $2 ~ /:/ {print $2}' | sort -u)
  grep -q "org.springframework.boot:spring-boot-starter-parent" "$doc" || { echo "  Spring Boot parent not documented"; rc=1; }
  return $rc
}

# [S1-AC5] FR-S1-009, FR-S1-010: every logging implementation/bridge on the classpath is documented in
# logging-bindings.md with an exclusion recommendation, and exactly one SLF4J binding (logback-classic) is resolved.
test_S1_AC5() {
  local doc="$DOC_DIR/logging-bindings.md" rc=0 ga bindings
  [ -s "$doc" ] || { echo "  missing $doc"; return 1; }
  while read -r ga; do
    grep -qF "$ga" "$doc" || { echo "  undocumented logging artifact: $ga"; rc=1; }
  done < <(awk '/^## Logging implementations/,/^## Version conflicts/' "$REPORT_DIR/dependency-summary.md" | awk -F' \\| ' 'NF>2 && $2 ~ /:/ {print $2}' | sort -u)
  bindings="$(grep -hoE '(ch\.qos\.logback:logback-classic|org\.slf4j:slf4j-(simple|log4j12|reload4j|jdk14|nop|jcl)|org\.apache\.logging\.log4j:log4j-slf4j2?-impl):jar' \
    "$REPORT_DIR"/dependency-tree-*.txt | grep -v '^$' | sort -u)"
  echo "  resolved SLF4J bindings: ${bindings//$'\n'/, }"
  [ "$bindings" = "ch.qos.logback:logback-classic:jar" ] || { echo "  expected only logback-classic as SLF4J binding"; rc=1; }
  grep -qiE "parent POM|module POM" "$doc" || { echo "  exclusion level (parent/module POM) not documented"; rc=1; }
  return $rc
}

# [S1-AC6] FR-S1-011, FR-S1-012: no language-level / compiler / deprecated-plugin warnings (globally and from the
# monitoring packages) on JDK 17 and JDK 21.
test_S1_AC6() {
  local rc=0 v home
  for v in 17 21; do
    local override_var="JAVA${v}_HOME"
    if ! home="$(find_jdk "$v" "${!override_var:-}")"; then
      echo "  JDK $v not found (set $override_var)"; rc=1; continue
    fi
    JAVA_HOME="$home" "$ROOT/scripts/s1/check-build-warnings.sh" | sed 's/^/  /' || rc=1
    [ "${PIPESTATUS[0]}" -eq 0 ] || rc=1
  done
  return $rc
}

selected=("$@")
[ ${#selected[@]} -gt 0 ] || selected=(S1-AC1 S1-AC2 S1-AC3 S1-AC4 S1-AC5 S1-AC6)
failed=0
for ac in "${selected[@]}"; do
  fn="test_${ac//-/_}"
  echo "[$ac] running $fn"
  if "$fn"; then echo "[$ac] PASS"; else echo "[$ac] FAIL"; failed=1; fi
done
exit $failed
