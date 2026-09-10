#!/usr/bin/env bash
# Sealed toolchain activation.  Usage:  source env.sh   (works in bash and zsh)
#
# Everything this project needs — JDK, Android SDK, emulator, AVDs, adb keys,
# debug keystore, Gradle cache — lives inside ./.toolchain and nowhere else.
# The host machine stays 100% clean.  Full uninstall: rm -rf .toolchain

# Resolve this file's directory in either shell. Kept `set -u`-safe so that
# scripts running under `set -euo pipefail` can source it.
if [ -n "${BASH_SOURCE:-}" ]; then
    _croncher_env_src="${BASH_SOURCE[0]}"
elif [ -n "${ZSH_VERSION:-}" ]; then
    _croncher_env_src="${(%):-%N}"
else
    _croncher_env_src="$0"
fi

PROJECT_ROOT="$( cd "$( dirname "$_croncher_env_src" )" && pwd )"
unset _croncher_env_src
export PROJECT_ROOT
export TOOLCHAIN="$PROJECT_ROOT/.toolchain"

export JAVA_HOME="$TOOLCHAIN/jdk-17"
export ANDROID_HOME="$TOOLCHAIN/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

# These four keep the Android tools from writing into ~/ :
export ANDROID_USER_HOME="$TOOLCHAIN/.android"      # adb keys, debug.keystore
# ANDROID_SDK_HOME is the legacy "parent of .android" variable. It is the only
# thing that keeps the Android Gradle Plugin's analytics file out of the real
# $HOME — ANDROID_USER_HOME alone does not cover it, and neither HOME nor
# -Duser.home works (the JVM reads user.home from the OS user record).
export ANDROID_SDK_HOME="$TOOLCHAIN"
export ANDROID_AVD_HOME="$TOOLCHAIN/.android/avd"   # emulator images
export ANDROID_EMULATOR_HOME="$TOOLCHAIN/.android"
export GRADLE_USER_HOME="$TOOLCHAIN/.gradle"        # gradle caches + daemon

export PATH="$TOOLCHAIN/bin:$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Only decorate an interactive prompt, and never stack the marker twice.
case "${PS1:-}" in
    "")             ;;
    "(croncher) "*) ;;
    *) export PS1="(croncher) ${PS1}" ;;
esac

if [ ! -d "$JAVA_HOME" ]; then
    echo "toolchain not installed yet — run ./scripts/setup.sh" >&2
fi
