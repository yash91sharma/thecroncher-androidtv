# Pac-Man for Google TV

A 16-bit style Pac-Man for Android TV, built for a Google TV Streamer on a 4K OLED,
played with a Bluetooth gamepad.

See **[plan.md](plan.md)** for the full project plan and current progress.
See **[CLAUDE.md](CLAUDE.md)** for the project rules.

## Quick start

```bash
source env.sh          # activate the sealed toolchain (every new shell)
./gradlew test         # fast JVM tests — the whole game simulation, no device
./gradlew assembleDebug # -> app/build/outputs/apk/debug/app-debug.apk
```

If `.toolchain/` does not exist yet:

```bash
./scripts/setup.sh              # JDK 17 + Android SDK + emulator, ~2.5 GB
./scripts/bootstrap-gradle.sh   # generates ./gradlew
```

## The sealed toolchain

Everything this project needs — JDK, Android SDK, emulator, AVDs, adb keys, the
debug keystore and the Gradle cache — lives inside `./.toolchain` and nowhere
else. Nothing is installed via Homebrew, nothing is written to your home
directory, and your global `PATH` is untouched.

```bash
./scripts/leak-check.sh   # verifies nothing escaped .toolchain
rm -rf .toolchain         # complete uninstall
```

## Layout

| Module | What |
|---|---|
| `:core` | Pure Kotlin/JVM — game simulation, theming, screens, port interfaces. **No Android dependency**, so it runs and tests on the JVM in seconds. |
| `:app` | Thin Android adapter — Canvas, AudioTrack, SurfaceView, Activity, SharedPreferences. |

## How to customise

*(Filled in as the pieces land — see plan.md Phase 0d for the design.)*

| To change… | Edit… |
|---|---|
| Colours / look | A `Theme` in `core/.../theme/themes/` |
| Pac-Man, ghost or pellet art | Drop PNGs into `app/src/main/assets/sprites/<theme>/` |
| A settings option | One `MenuItem` entry — no drawing code |
| Add a screen | One `Screen` implementation + a registry entry |
| The maze | `core/src/main/resources/maze/classic.txt` |
