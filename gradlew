#!/usr/bin/env sh
# Cloud-friendly Gradle bootstrapper for AutoFolder.
# If the standard wrapper JAR is supplied, use it. Otherwise prefer the
# Gradle binary preinstalled on CI, and finally bootstrap the pinned release.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  exec java ${JAVA_OPTS:-} -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
DIST_VERSION=8.11.1
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/autofolder-gradle"
DIST_ZIP="$DIST_DIR/gradle-${DIST_VERSION}-bin.zip"
DIST_HOME="$DIST_DIR/gradle-${DIST_VERSION}"
mkdir -p "$DIST_DIR"
if [ ! -x "$DIST_HOME/bin/gradle" ]; then
  command -v curl >/dev/null 2>&1 || { echo 'curl is required to bootstrap Gradle.' >&2; exit 1; }
  echo "Downloading Gradle ${DIST_VERSION}..."
  curl -fL --retry 3 -o "$DIST_ZIP" "https://services.gradle.org/distributions/gradle-${DIST_VERSION}-bin.zip"
  rm -rf "$DIST_HOME.tmp"
  mkdir -p "$DIST_HOME.tmp"
  unzip -q "$DIST_ZIP" -d "$DIST_HOME.tmp"
  mv "$DIST_HOME.tmp/gradle-${DIST_VERSION}" "$DIST_HOME"
  rm -rf "$DIST_HOME.tmp"
fi
exec "$DIST_HOME/bin/gradle" "$@"
