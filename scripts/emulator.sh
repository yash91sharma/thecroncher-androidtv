#!/usr/bin/env bash
# Boot an Android TV emulator from the sealed toolchain.
#   ./scripts/emulator.sh          -> 1080p TV
#   ./scripts/emulator.sh tv4k     -> 2160p TV (exercises the x7 pixel scale)
set -euo pipefail
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
source "$PROJECT_ROOT/env.sh"

AVD="${1:-tv1080p}"

if ! avdmanager list avd 2>/dev/null | grep -q "Name: $AVD"; then
    echo "AVD '$AVD' does not exist. Run ./scripts/setup.sh first." >&2
    exit 1
fi

echo "==> booting $AVD"
exec emulator "@$AVD" -gpu host -no-snapshot-load -no-boot-anim "${@:2}"
