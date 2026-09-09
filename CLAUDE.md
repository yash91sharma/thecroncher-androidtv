# Project rules — Pac-Man for Google TV

Read this before doing anything. `plan.md` is the living project plan with
checkboxes; find the first unticked box and continue from there.

## Git — hard rule

**Never run `git add`, `git commit`, `git push`, `git tag`, or open PRs.**
The user commits manually. Read-only git (`status`, `diff`, `log`, `ls-files`)
is fine.

At the end of a unit of work: summarise what changed, suggest a commit message,
then **stop**. Do not stage anything.

## Development is TDD — hard rule

Write the failing test first, watch it fail, then write the minimum code to pass
it, then refactor. No production code without a test that demanded it.

`./gradlew test` must be green before any task is called done.

## Module boundary — hard rule

`:core` is a pure-Kotlin JVM module. It **cannot** depend on Android — the build
enforces this, it is not a convention. All game simulation, theming, menu models,
screens and port interfaces live there and are unit-tested without a device.

`:app` is the thin Android adapter: Canvas, AudioTrack, SurfaceView, Activity,
SharedPreferences, resources.

If you need an Android API inside `:core`, you are in the wrong module — add a
port interface to `core/ports/` and implement it in `:app` instead.

## Extensibility — hard rule

Never hardcode a colour, sprite, string, or screen-layout number at a call site:

| Thing | Comes from |
|---|---|
| Colours | `Theme` (`core/theme/themes/*.kt`) |
| Art | `SpriteSource`, addressed by `SpriteId` |
| User-facing text | `Strings` |
| Screen positions | `Layout` |
| Menu contents | `MenuModel` data |

Adding a settings option must be a **one-line data change**, not new drawing or
navigation code. Adding a theme must be one new file plus one registry line.

## Toolchain — hard rule

Always `source env.sh` before any build command. The toolchain is sealed inside
`./.toolchain` — JDK, Android SDK, emulator, AVDs, adb keys, debug keystore and
the Gradle cache all live there.

Never install anything globally. Never use Homebrew. Never write outside
`.toolchain/`. The host machine must stay 100% clean.

Verify after every phase:

```bash
[ -e ~/.android ] || [ -e ~/.gradle ] || [ -e ~/Library/Android ] && echo "LEAK" || echo "clean"
```

Full uninstall is `rm -rf .toolchain`.

## Commands

```bash
source env.sh                        # activate the sealed toolchain (every shell)
./gradlew test                       # fast JVM logic tests — run constantly
./gradlew assembleDebug              # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest  # instrumented, needs emulator/device
./scripts/setup.sh                   # rebuild the sealed toolchain from scratch
./scripts/emulator.sh                # create + boot the Android TV AVD
```

## Target hardware

Google TV Streamer (Android 14 / API 34) on an LG C3 77" 4K OLED, with a Google
Stadia Controller in Bluetooth mode. Render at 224x288 and integer-scale to the
panel; never use bilinear filtering. Keep static HUD elements off pure white
(OLED burn-in).
