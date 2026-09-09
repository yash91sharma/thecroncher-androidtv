#!/usr/bin/env bash
# Bootstrap the sealed toolchain into ./.toolchain
#
# Idempotent: safe to re-run. Nothing is installed globally, nothing is written
# outside .toolchain/. Full uninstall is: rm -rf .toolchain
set -euo pipefail

PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$PROJECT_ROOT"
TOOLCHAIN="$PROJECT_ROOT/.toolchain"
DL="$TOOLCHAIN/downloads"
mkdir -p "$TOOLCHAIN" "$DL"

# Activate the (partially built) toolchain so every tool below writes inside it.
# shellcheck disable=SC1091
source "$PROJECT_ROOT/env.sh" 2>/dev/null || true

say() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }

# ---------------------------------------------------------------- JDK 17 ----
if [ -x "$TOOLCHAIN/jdk-17/bin/java" ]; then
  say "JDK 17 already present — skipping"
else
  say "Downloading Eclipse Temurin JDK 17 (macOS aarch64)"
  JDK_URL="https://api.adoptium.net/v3/binary/latest/17/ga/mac/aarch64/jdk/hotspot/normal/eclipse"
  curl -fL --retry 3 --progress-bar "$JDK_URL" -o "$DL/jdk17.tar.gz"

  say "Extracting JDK"
  rm -rf "$TOOLCHAIN/jdk-extract" "$TOOLCHAIN/jdk-17"
  mkdir -p "$TOOLCHAIN/jdk-extract"
  tar -xzf "$DL/jdk17.tar.gz" -C "$TOOLCHAIN/jdk-extract"
  # The tarball contains <jdk-17.x.y>/Contents/Home — make that our JAVA_HOME.
  JDK_HOME="$(find "$TOOLCHAIN/jdk-extract" -maxdepth 3 -type d -name Home -path '*/Contents/Home' | head -1)"
  [ -n "$JDK_HOME" ] || { echo "could not locate Contents/Home in JDK tarball" >&2; exit 1; }
  mv "$JDK_HOME" "$TOOLCHAIN/jdk-17"
  rm -rf "$TOOLCHAIN/jdk-extract"
fi

export JAVA_HOME="$TOOLCHAIN/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"

# ------------------------------------------------ Android cmdline-tools ----
SDK="$TOOLCHAIN/android-sdk"
if [ -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  say "Android command-line tools already present — skipping"
else
  say "Resolving the current Android command-line tools version"
  curl -fsSL --retry 3 https://dl.google.com/android/repository/repository2-3.xml -o "$DL/repo23.xml"
  # Pick the highest build number rather than pinning a version that goes stale.
  CLT_ZIP="$(grep -o 'commandlinetools-mac-[0-9]*_latest\.zip' "$DL/repo23.xml" \
            | sort -u | sed 's/[^0-9]*\([0-9]*\).*/\1 &/' | sort -rn | head -1 | cut -d' ' -f2)"
  [ -n "$CLT_ZIP" ] || { echo "could not resolve cmdline-tools version" >&2; exit 1; }
  say "Downloading $CLT_ZIP"
  curl -fL --retry 3 --progress-bar \
    "https://dl.google.com/android/repository/$CLT_ZIP" -o "$DL/cmdline-tools.zip"

  say "Installing command-line tools"
  # NOTE: the zip's top-level folder is 'cmdline-tools/', but sdkmanager requires
  # the layout $ANDROID_HOME/cmdline-tools/latest/ — hence the rename below.
  rm -rf "$TOOLCHAIN/clt-extract" "$SDK/cmdline-tools/latest"
  mkdir -p "$TOOLCHAIN/clt-extract" "$SDK/cmdline-tools"
  unzip -q "$DL/cmdline-tools.zip" -d "$TOOLCHAIN/clt-extract"
  mv "$TOOLCHAIN/clt-extract/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$TOOLCHAIN/clt-extract"
fi

# macOS Gatekeeper: clear the quarantine flag on everything we downloaded.
# File-local — changes no system setting.
say "Clearing macOS quarantine flags"
xattr -dr com.apple.quarantine "$TOOLCHAIN" 2>/dev/null || true

export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export ANDROID_USER_HOME="$TOOLCHAIN/.android"
export ANDROID_SDK_HOME="$TOOLCHAIN"
export ANDROID_AVD_HOME="$TOOLCHAIN/.android/avd"
export ANDROID_EMULATOR_HOME="$TOOLCHAIN/.android"
export GRADLE_USER_HOME="$TOOLCHAIN/.gradle"
mkdir -p "$ANDROID_AVD_HOME"
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"

# ------------------------------------------------------- SDK packages ----
say "Generating sandboxed tool wrappers"
"$PROJECT_ROOT/scripts/make-wrappers.sh"

say "Accepting SDK licenses"
yes 2>/dev/null | "$SDKMANAGER" --sdk_root="$SDK" --licenses >/dev/null || true

# Pick the Android TV system image: prefer API 34, fall back to 33.
say "Selecting an Android TV system image"
AVAILABLE="$("$SDKMANAGER" --sdk_root="$SDK" --list 2>/dev/null || true)"
TV_IMAGE=""
for candidate in \
  "system-images;android-34;android-tv;arm64-v8a" \
  "system-images;android-33;android-tv;arm64-v8a"
do
  if printf '%s' "$AVAILABLE" | grep -q "$candidate"; then TV_IMAGE="$candidate"; break; fi
done
[ -n "$TV_IMAGE" ] || { echo "no arm64 Android TV system image available" >&2; exit 1; }
echo "    using: $TV_IMAGE"
echo "$TV_IMAGE" > "$TOOLCHAIN/tv-image.txt"

say "Installing SDK packages (this is the big download)"
"$SDKMANAGER" --sdk_root="$SDK" \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "emulator" \
  "$TV_IMAGE"

# ------------------------------------------------------------ verify ----
say "Verifying"
"$JAVA_HOME/bin/java" -version
"$SDK/platform-tools/adb" version | head -1
echo
echo "TV system image: $TV_IMAGE"
du -sh "$TOOLCHAIN"

say "Leak check (host must be clean)"
if [ -e ~/.android ] || [ -e ~/.gradle ] || [ -e ~/Library/Android ]; then
  echo "LEAK — something wrote outside .toolchain"; ls -d ~/.android ~/.gradle ~/Library/Android 2>/dev/null
else
  echo "clean — nothing written outside .toolchain"
fi

say "Done. Run:  source env.sh"
