#!/bin/sh
set -eu
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
DIST="$BASE_DIR/.gradle-dist/gradle-8.14.3/bin/gradle"
if [ ! -x "$DIST" ]; then
  mkdir -p "$BASE_DIR/.gradle-dist"
  ZIP="$BASE_DIR/.gradle-dist/gradle-8.14.3-bin.zip"
  if command -v curl >/dev/null 2>&1; then curl -L --fail --retry 3 -o "$ZIP" https://services.gradle.org/distributions/gradle-8.14.3-bin.zip
  elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" https://services.gradle.org/distributions/gradle-8.14.3-bin.zip
  else echo 'curl or wget is required to bootstrap Gradle' >&2; exit 1; fi
  rm -rf "$BASE_DIR/.gradle-dist/gradle-8.14.3"
  unzip -q "$ZIP" -d "$BASE_DIR/.gradle-dist"
fi
exec "$DIST" "$@"
