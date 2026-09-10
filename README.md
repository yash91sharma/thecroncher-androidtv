# The Croncher

A 16-bit maze game for Android TV: a grey tabby cat eats his way through a house
full of treats while a dog, a vacuum cleaner, a spray bottle and a cucumber do
their best to ruin his evening. Built for a Google TV Streamer on a 4K OLED,
played with a Bluetooth gamepad.

The working rules for this repo — git, TDD, the `:core`/`:app` boundary and the
sealed toolchain — live in an untracked rules file in the project root.

## Quick start

```bash
source env.sh           # activate the sealed toolchain (every new shell)
./gradlew test          # fast JVM tests — the whole game simulation, no device
./gradlew assembleDebug # -> app/build/outputs/apk/debug/app-debug.apk
./scripts/emulator.sh   # boot the Android TV emulator
./scripts/install.sh --run   # build, install and launch on it
```

Install with `scripts/install.sh`, not `./gradlew installDebug`: the Gradle daemon
runs with the real `$HOME`, so the adb *it* starts writes RSA keys into
`~/.android`. The script goes through the sealed wrapper instead and finishes by
running the leak check.

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

## Deploy to TV

The Google TV Streamer's USB-C port is power only, so the APK goes over the
network. The pairing and authorisation below are one-time; after that, shipping a
new build is three commands.

The Mac and the TV must be on the same network, and `source env.sh` is required in
every new shell.

### 1. Pair the Stadia controller

Pair it to the **TV**, not to the Mac.

If the controller has never been switched out of Wi-Fi mode: open
`stadia.google.com/controller` in Chrome (the tool uses WebHID, so Safari will not
do), connect the pad with a USB-C **data** cable — a charge-only cable is not
detected, and that is nearly always the reason the page sees nothing — and follow
the steps. The flash is one-way: afterwards it is a plain Bluetooth gamepad.

Then:

1. Hold **Stadia + Y** for two seconds. The status light pulses orange.
2. On the TV, **Settings → Remotes & Accessories → Pair remote or accessory**.
3. Select `Stadia Controller` when it appears. The light goes solid white.

If it never shows up, hold the **Stadia** button for ten seconds to power the pad
right down and start again.

### 2. Enable ADB on the TV

1. **Settings → System → About**, then press **Android TV OS build** seven times.
2. **Settings → System → Developer options → USB debugging** (labelled **ADB
   debugging** on some builds — either one also enables ADB over the network).
3. **Settings → Network & Internet → [your network]** and note the **IP address**.
   It survives until the TV reboots unless the router reserves it.

### 3. Connect

```bash
source env.sh
adb connect 192.168.1.42:5555     # the TV's IP
```

The TV shows *"Allow USB debugging?"*. Tick **Always allow from this computer** and
accept — with the TV remote; the Stadia pad does not always drive system dialogs.
`adb devices` should then report the address as `device`. `unauthorized` means the
dialog is still waiting; `offline` is in the troubleshooting table below.

### 4. Install

```bash
./scripts/install.sh --run
```

Builds, installs, launches, and runs the leak check. By hand it is
`adb install -r app/build/outputs/apk/debug/app-debug.apk` — `-r` replaces in
place, which is how updates keep the saved high score.

### 5. Play

The game declares `LEANBACK_LAUNCHER`, so it lands on the Google TV home screen
under **Your apps** (**Apps → See all apps** if the row has not refreshed).

| Input | Does |
|---|---|
| D-pad or left stick | Move |
| **A**, Select, Enter | Confirm |
| **B**, Back | Back |
| **Start**, Menu | Pause |

**Settings → Controller Test** shows live D-pad, hat, both sticks, triggers,
button names and the detected device name — the first place to look if a pad
reports something unexpected.

### Unplugging

Nothing depends on the Mac once the install finishes. Disconnect adb, turn USB
debugging back off — network ADB is not worth leaving open on a LAN — and take
the TV off the internet if you like: the manifest declares no permissions, so the
game never reaches for it. Settings and the high score live in SharedPreferences
(`the-croncher`) and survive reboots and reinstalls, but not an uninstall.

### Updating later

```bash
source env.sh
adb connect 192.168.1.42:5555
./scripts/install.sh --run
```

The Mac's key stays authorised, so the TV does not prompt again.

### Troubleshooting

| Symptom | Fix |
|---|---|
| `connection refused` | The TV is not listening. Toggle USB debugging off and on. If the build only offers **Wireless debugging** with a pairing code, `adb pair <ip>:<pairing-port>` first, then `adb connect <ip>:<debug-port>` — the two ports differ. |
| `offline` | `adb disconnect && adb kill-server && adb connect <ip>:5555` |
| `unauthorized` | Reconnect and watch the TV for the dialog. If it never appears, **Developer options → Revoke USB debugging authorizations**, then reconnect. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | An older copy signed with a different key. `adb uninstall com.yash.thecroncher`, then install again — this loses the high score. |
| Not on the home screen | `adb shell am start -n com.yash.thecroncher/.MainActivity` launches it directly. |
| Logs while playing | `adb logcat --pid=$(adb shell pidof -s com.yash.thecroncher)` |

### Disconnect

`adb disconnect`

## Layout

| Module | What |
|---|---|
| `:core` | Pure Kotlin/JVM — game simulation, theming, screens, port interfaces. **No Android dependency**, so it runs and tests on the JVM in seconds. |
| `:app` | Thin Android adapter — Canvas, AudioTrack, SurfaceView, Activity, SharedPreferences. |

## How to customise

Everything below is a data change. None of it needs new drawing or navigation code.

### Restyle the cast

Every colour in the game lives in one file:
`core/src/main/kotlin/com/yash/thecroncher/core/theme/themes/TheCroncher.kt`. The
art names *inks* — "fur", "tongue", "water" — and the theme's `SpritePalette` says
what each ink looks like, so a ginger cat is a one-line change and nothing is
redrawn.

### Redraw a character

`core/src/main/kotlin/com/yash/thecroncher/core/theme/CronchArt.kt` holds every
sprite as an ASCII grid you can read and edit in place. `PixelArt` turns a grid
into pixels and supplies the tricks that keep the drawing down: facing left is the
right-facing art mirrored, walking is the same art shifted a pixel, frightened is
the same silhouette in blue with a face painted inside it.

The grids were laid out with the throwaway scripts kept out of the repo; editing
them by hand is expected and `SpriteSourceTest` will catch a ragged row, an
unknown ink, or a cast that has stopped being tellable apart.

### Add a theme

1. Copy `theme/themes/TheCroncher.kt`, rename it, and give it a unique `id`.
2. Add it to the list in `ThemeRegistry.all`.

That is the whole procedure. `ThemeRegistryTest` fails if you leave a colour slot
unfilled or a sprite unresolved, so a half-finished theme breaks at test time
rather than on the television. (With more than one theme registered, add a `THEME`
row back to `SettingsScreen` — one `MenuItem`, as below.)

### Recast the foes

`FoeCast.all` in `theme/FoeCast.kt` says which art each of the four ghosts wears.
Swapping the cucumber for a hair dryer is an entry there plus its grids in
`CronchArt.kt`; the chase logic underneath never knows.

### Change the launcher icon or the TV banner

Both are generated from the game's own art, so they cannot drift from it:

```bash
python3 scripts/make-icons.py     # -> app/src/main/res/drawable/{ic_launcher,banner}.xml
```

Edit the cat grid or the palette and run it again.

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
corridor. `MazeTest` flood-fills from the croncher's start and fails if any treat
becomes unreachable, so you cannot accidentally ship an unplayable maze.

### Retune the difficulty tiers

`Difficulties.EASY` / `NORMAL` / `HARD` in
`core/src/main/kotlin/com/yash/thecroncher/core/game/Difficulty.kt`. Adding a fourth
tier is one more entry in `Difficulties.all`.
