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

Everything below is a data change. None of it needs new drawing or navigation code.

### Add a theme

1. Copy `core/src/main/kotlin/com/yash/pacmantv/core/theme/themes/Neon.kt`, rename
   it, and give it a unique `id`.
2. Add it to the list in `ThemeRegistry.all`.

That is the whole procedure. `ThemeRegistryTest` fails if you leave a colour slot
unfilled or a sprite unresolved, so a half-finished theme breaks at test time
rather than on the television. It appears in Settings automatically.

### Change Pac-Man's icon, the ghosts, the pellets

Art is addressed by `SpriteId`, never by file path, so no call site knows where the
pixels come from. Today `ProceduralSpriteSource` draws them in code. To use your
own artwork, point a theme's `sprites` at a `SpriteSource` that loads PNGs from
`app/src/main/assets/sprites/<theme>/`. Nothing else changes.

To change only the *launcher* icon or the TV banner, edit
`app/src/main/res/drawable/ic_launcher.xml` and `banner.xml`.

### Add a settings option

One entry in the `items` list in `SettingsScreen`:

```kotlin
MenuItem(
    "MY OPTION",
    ItemKind.Choice(
        options = listOf("OFF", "ON"),
        getIndex = { if (settings.myOption) 1 else 0 },
        setIndex = { settings.myOption = it == 1 },
    ),
)
```

`MenuRenderer` draws any menu, and `MenuModel` handles all the navigation.

### Add a screen

Implement `Screen` (`handle`, `render`, optionally `onEnter`/`onExit`/`update`) and
push it from wherever it belongs with `Transition.Push(MyScreen())`.

### Change the maze

`core/src/main/resources/maze/classic.txt`, where `#` is wall, `.` a dot, `o` an
energizer, `-` the ghost-house door, `_` the house interior, and a space is empty
corridor. `MazeTest` flood-fills from Pac-Man's start and fails if any pellet
becomes unreachable, so you cannot accidentally ship an unplayable maze.

### Retune the difficulty tiers

`Difficulties.EASY` / `NORMAL` / `HARD` in
`core/src/main/kotlin/com/yash/pacmantv/core/game/Difficulty.kt`. Adding a fourth
tier is one more entry in `Difficulties.all`.
