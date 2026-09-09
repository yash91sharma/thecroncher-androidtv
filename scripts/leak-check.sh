#!/usr/bin/env bash
# Verifies the host machine is clean: nothing this project uses may live outside
# ./.toolchain. Run after every phase.
#
# Empty directories are treated as noise, not leakage: some Android tools mkdir
# ~/.android defensively before they consult ANDROID_USER_HOME, then write
# nothing into it. We remove those (rmdir only succeeds on an empty directory, so
# this can never destroy real data) and report a leak only when something was
# actually written outside the toolchain.
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
leaked=0
tidied=()

for d in "$HOME/.android" "$HOME/.gradle" "$HOME/Library/Android" "$HOME/.konan"; do
    [ -e "$d" ] || continue
    if [ -d "$d" ] && [ -z "$(ls -A "$d" 2>/dev/null)" ]; then
        rmdir "$d" 2>/dev/null && tidied+=("$d")
        continue
    fi
    echo "LEAK: $d"
    find "$d" -maxdepth 2 | head -10 | sed 's/^/      /'
    leaked=1
done

if [ "$leaked" -eq 0 ]; then
    echo "clean — nothing written outside .toolchain"
    [ ${#tidied[@]} -gt 0 ] && echo "        (removed empty dir: ${tidied[*]})"
    echo "footprint: $(du -sh "$PROJECT_ROOT/.toolchain" | cut -f1)"
fi
exit "$leaked"
