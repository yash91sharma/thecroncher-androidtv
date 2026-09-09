#!/usr/bin/env bash
# One-shot: download a Gradle distribution into .toolchain and use it to generate
# the project's Gradle wrapper. After this, always use ./gradlew.
set -euo pipefail

PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$PROJECT_ROOT"
source "$PROJECT_ROOT/env.sh"

GRADLE_VERSION="8.7"
BOOT="$TOOLCHAIN/gradle-boot"

if [ -x "./gradlew" ]; then
    echo "gradle wrapper already present — skipping"
    exit 0
fi

if [ ! -x "$BOOT/gradle-$GRADLE_VERSION/bin/gradle" ]; then
    echo "==> Downloading Gradle $GRADLE_VERSION"
    mkdir -p "$BOOT"
    curl -fL --retry 3 --progress-bar \
        "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" \
        -o "$TOOLCHAIN/downloads/gradle.zip"
    unzip -q "$TOOLCHAIN/downloads/gradle.zip" -d "$BOOT"
fi

echo "==> Generating the wrapper"
"$BOOT/gradle-$GRADLE_VERSION/bin/gradle" wrapper \
    --gradle-version "$GRADLE_VERSION" \
    --distribution-type bin \
    --no-daemon

echo "==> Done: ./gradlew is ready"
