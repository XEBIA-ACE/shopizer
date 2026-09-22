#!/usr/bin/env bash
# Verifies CONTRIBUTING.md acceptance criteria. Run with: bash scripts/tests/test-contributing.sh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FILE="$REPO_ROOT/CONTRIBUTING.md"

# [SHOPIZER-governed-irt-1790097772-AC1] CONTRIBUTING.md exists at the repository root
if [ ! -f "$FILE" ]; then
  echo "FAIL AC1: $FILE does not exist"
  exit 1
fi
echo "PASS AC1: CONTRIBUTING.md exists"

# [SHOPIZER-governed-irt-1790097772-AC2] Documents the Maven build command
if ! grep -q "mvn clean install" "$FILE"; then
  echo "FAIL AC2: 'mvn clean install' not documented"
  exit 1
fi
echo "PASS AC2: Maven build command documented"

# [SHOPIZER-governed-irt-1790097772-AC3] Documents the test command
if ! grep -q "mvn test" "$FILE"; then
  echo "FAIL AC3: 'mvn test' not documented"
  exit 1
fi
echo "PASS AC3: Maven test command documented"

echo "All CONTRIBUTING.md acceptance criteria passed"
