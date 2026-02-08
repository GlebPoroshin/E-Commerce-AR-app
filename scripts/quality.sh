#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

./gradlew --console=plain check

if ./gradlew --console=plain tasks --all | grep -q '^detekt'; then
  ./gradlew --console=plain detekt
fi

if ./gradlew --console=plain tasks --all | grep -q '^ktlintCheck'; then
  ./gradlew --console=plain ktlintCheck
fi

if command -v swiftlint >/dev/null 2>&1; then
  (cd iosApp && swiftlint)
else
  echo "swiftlint is not installed; skipping"
fi
