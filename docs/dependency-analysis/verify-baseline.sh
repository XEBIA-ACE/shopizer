#!/usr/bin/env bash
# Characterization check for the dependency baseline documented in README.md.
# Regenerates the Maven dependency tree and asserts the documented key versions.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$(mktemp)"
trap 'rm -f "$OUT"' EXIT

cd "$ROOT"
MVN="${MVN:-mvn}"
rm -f "$OUT"
"$MVN" -B -q ${MVN_ARGS:-} dependency:tree -DoutputFile="$OUT" -DappendOutput=true

fail=0
check() {
  local label="$1" pattern="$2"
  if grep -qE "$pattern" "$OUT"; then
    echo "PASS  $label"
  else
    echo "FAIL  $label (pattern: $pattern)"
    fail=1
  fi
}

check_pom() {
  local label="$1" file="$2" pattern="$3"
  if grep -qE "$pattern" "$ROOT/$file"; then
    echo "PASS  $label"
  else
    echo "FAIL  $label ($file, pattern: $pattern)"
    fail=1
  fi
}

# S1-AC1: tree generated for every reactor module
for m in shopizer:pom sm-core-model:jar sm-core-modules:jar sm-core:jar sm-shop-model:jar sm-shop:jar; do
  check "S1-AC1 tree contains com.shopizer:$m:3.2.5" "^com\.shopizer:$m:3\.2\.5$"
done

# S1-AC2: Spring Boot
check_pom "S1-AC2 root parent spring-boot-starter-parent 2.5.12" pom.xml "<version>2\.5\.12</version>"
check "S1-AC2 spring-boot resolved 2.5.12" "org\.springframework\.boot:spring-boot:jar:2\.5\.12"

# S1-AC3: Spring Framework
check "S1-AC3 spring-core resolved 5.3.18" "org\.springframework:spring-core:jar:5\.3\.18"
check "S1-AC3 spring-webmvc resolved 5.3.18" "org\.springframework:spring-webmvc:jar:5\.3\.18"

# S1-AC4: Hibernate / JPA
check "S1-AC4 hibernate-core 5.4.33" "org\.hibernate:hibernate-core:jar:5\.4\.33"
check "S1-AC4 spring-data-jpa 2.5.10" "org\.springframework\.data:spring-data-jpa:jar:2\.5\.10"
check "S1-AC4 jakarta.persistence-api 2.2.3" "jakarta\.persistence:jakarta\.persistence-api:jar:2\.2\.3"

# S1-AC5: javax.* / Jakarta EE 8 (pre-namespace-change)
check "S1-AC5 javax.validation 2.0.1.Final" "javax\.validation:validation-api:jar:2\.0\.1\.Final"
check "S1-AC5 jakarta.transaction-api 1.3.3" "jakarta\.transaction:jakarta\.transaction-api:jar:1\.3\.3"
check "S1-AC5 tomcat-embed-core 9.0.60 (Servlet 4.0)" "org\.apache\.tomcat\.embed:tomcat-embed-core:jar:9\.0\.60"

# S1-AC6: Java compiler target
check_pom "S1-AC6 root java.version 11" pom.xml "<java\.version>11</java\.version>"
check_pom "S1-AC6 sm-core-model java.version 11" sm-core-model/pom.xml "<java\.version>11</java\.version>"

# S1-AC7: dependencyManagement overrides
check "S1-AC7 jackson-databind pinned 2.13.4.1" "com\.fasterxml\.jackson\.core:jackson-databind:jar:2\.13\.4\.1"
check "S1-AC7 mysql-connector-java pinned 8.0.21" "mysql:mysql-connector-java:jar:8\.0\.21"

exit "$fail"
