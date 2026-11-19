#!/bin/sh
set -e
GRADLE_VERSION="8.9"
GRADLE_DIR="$HOME/.gradle_cli/gradle-${GRADLE_VERSION}"
if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  mkdir -p "$HOME/.gradle_cli"
  curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip
  unzip -qo /tmp/gradle.zip -d "$HOME/.gradle_cli"
  chmod +x "$GRADLE_DIR/bin/gradle"
fi
exec "$GRADLE_DIR/bin/gradle" "$@"
