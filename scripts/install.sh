#!/usr/bin/env bash
# Builds the debug APK and installs it on the attached device or emulator.
#
# Use this rather than `./gradlew installDebug`. The Gradle daemon runs with the
# real $HOME, so the adb it starts for an install writes its RSA keys to
# ~/.android — a leak the sealed toolchain otherwise never has. The wrapper in
# .toolchain/bin pins HOME, so going through it keeps the host clean (and avoids
# the second adb server that leaves the emulator "unauthorized").
set -euo pipefail
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
source "$PROJECT_ROOT/env.sh"

APK="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"

echo "==> building"
"$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" assembleDebug

echo "==> installing $APK"
adb install -r "$APK"

if [ "${1:-}" = "--run" ]; then
    echo "==> launching"
    adb shell am start -n com.yash.thecroncher/.MainActivity
fi

"$PROJECT_ROOT/scripts/leak-check.sh"
