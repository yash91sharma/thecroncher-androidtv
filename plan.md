# Pac-Man for Google TV Streamer — Project Plan

> **This is the living project plan.** Every task has a checkbox. Tick them as they
> complete so work can be resumed later from a cold start. See
> [How to resume](#how-to-resume) at the bottom.

**Status legend:** `[ ]` not started · `[~]` in progress · `[x]` done · `[!]` blocked

---

## Progress at a glance

| Phase | What | Status |
|---|---|---|
| 0 | Repo sync, project rules, isolation ground rules | `[x]` |
| 1 | Sealed toolchain bootstrap (JDK + Android SDK) | `[x]` |
| 2 | Two-module Gradle skeleton | `[x]` |
| 3 | Ports, theme system, renderer, game loop | `[x]` |
| 4 | Gameplay — maze, Pac-Man, ghosts, scoring, audio | `[~]` |
| 5 | Screens, menus, settings, input | `[ ]` |
| 6 | Debug APK | `[ ]` |
| 7 | Verification — emulator, then the TV | `[ ]` |

---

## Context

Build a 16-bit style Pac-Man as a native Android TV app, sideloaded onto a
**Google TV Streamer** (Android 14 / API 34), played on an **LG C3 77" 4K OLED**
with a **Google Stadia Controller in Bluetooth mode**.

Verified starting state (2026-09-08):

| Thing | State |
|---|---|
| `/Users/yashsharma/Dev/pacman-androidtv` | Cloned from GitHub, only `.gitignore` |
| `github.com/yash91sharma/pacman-androidtv` | `main`, 1 commit |
| Java / JDK | **Not installed** |
| Android SDK / `adb` / `sdkmanager` | **Not installed** |
| Android Studio | Not installed (VS Code only) |
| Homebrew | Installed — **deliberately not used** |
| Docker Desktop | Installed — not needed for this path |
| Host | Apple Silicon (arm64), macOS 15.6, 275 GB free |

**Hard constraint: the host computer stays 100% clean.** Everything installs into
one deletable folder in the repo. No Homebrew, nothing written to `~/`, no global
`PATH` changes.

---

## Decisions (settled — do not relitigate)

| Question | Decision |
|---|---|
| Engine | Kotlin + `SurfaceView`, custom fixed-timestep loop on a `Canvas`. No game frameworks. |
| Architecture | Two Gradle modules (`:core` pure JVM, `:app` Android) + ports/adapters. See [Phase 0d](#0d-modularity--the-extension-points). |
| Toolchain | Sealed project-local `./.toolchain`, activated by `source env.sh`. |
| Testing | TDD throughout, then Android TV emulator, then the real Streamer. |
| Scope | Faithful arcade + polish — 4 distinct ghost AIs, scatter/chase waves, fright, fruit, lives, levels, chiptune SFX. |
| Display | 4K-native, integer-scaled pixel art, 60 fps, OLED-safe palette. |
| Input | D-pad keycodes + HAT axes + analog stick, plus a Controller Test screen. |
| APK | Debug-signed (a truly *unsigned* APK cannot be installed by Android). |
| Git | **Claude never runs `git add` / `commit` / `push`.** User commits manually. |

---

## Phase 0 — Repo, rules, isolation

### 0a. Repo sync

- [x] Clone `https://github.com/yash91sharma/pacman-androidtv.git` into the working dir
- [x] Confirm existing `.gitignore` already covers `build/`, `.gradle/`, `local.properties`, `*.apk`, `*.keystore`
- [x] Append `.toolchain/` to `.gitignore` (extend, never replace)
- [x] Write this `plan.md`
- [x] Write `CLAUDE.md` with the project rules below
- [x] Write `env.sh`
- [x] Write `README.md` (can be stubbed now, completed in Phase 7)

### Git rules (enforced by `CLAUDE.md`)

- Claude **never** runs `git add`, `git commit`, `git push`, `git tag`, or opens PRs.
- Read-only git (`status`, `diff`, `log`, `ls-files`) is fine.
- At the end of every phase Claude summarises what changed and suggests a commit
  message — then stops. The user commits.

### 0b. Isolation ground rules

A project-local SDK alone still leaks. The Android tools write outside the SDK dir
by default, so `env.sh` redirects all of it:

| Tool | Default write location | Redirected to |
|---|---|---|
| `avdmanager` / `emulator` | `~/.android/avd` | `.toolchain/.android/avd` |
| `adb` (RSA keys) | `~/.android/adbkey` | `.toolchain/.android/` |
| AGP debug keystore | `~/.android/debug.keystore` | `.toolchain/.android/` |
| Gradle (caches, daemon, dists) | `~/.gradle` | `.toolchain/.gradle` |

On Apple Silicon the emulator uses macOS's built-in **Hypervisor.framework** — no
kernel extension, no privileged installer (unlike the old Intel HAXM). So the
complete footprint really is one folder.

> **Finding (Phase 1, the hard-won one).** `ANDROID_USER_HOME` covers the adb keys
> and the debug keystore, but **not** the Android Gradle Plugin's
> `analytics.settings`, which kept reappearing in `~/.android`. Neither `HOME=`
> nor `-Duser.home=` fixes it — on macOS the JVM reads `user.home` from the OS
> user record, so the plugin finds the real home regardless. The variable that
> actually works is the legacy **`ANDROID_SDK_HOME`** (semantically "the parent
> directory of `.android`"), set to `$TOOLCHAIN`. It is exported by `env.sh`,
> `scripts/setup.sh` and the tool wrappers. Note the leak happens at Gradle
> *configuration* time, so even `./gradlew :core:test` triggers it — don't assume
> a non-Android task is safe.
>
> Tool wrappers in `.toolchain/bin/` (generated by `scripts/make-wrappers.sh`,
> first on `PATH`) pin `HOME` and the Android variables for `sdkmanager`,
> `avdmanager`, `adb` and `emulator`, so the sandbox holds even if someone runs
> them without sourcing `env.sh`.

Verify at any time with `./scripts/leak-check.sh`. It treats an *empty* `~/.android` as noise rather than a leak — some Android tools `mkdir` it defensively before consulting `ANDROID_USER_HOME` and then write nothing into it — so the script removes empty directories (via `rmdir`, which cannot touch a non-empty one) and reports a leak only when something was actually written outside the toolchain.

**`env.sh`:**

```bash
#!/usr/bin/env bash
# source env.sh   — activates the sealed toolchain for THIS shell only
export PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]:-$0}" )" && pwd )"
export TOOLCHAIN="$PROJECT_ROOT/.toolchain"

export JAVA_HOME="$TOOLCHAIN/jdk-17"
export ANDROID_HOME="$TOOLCHAIN/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_USER_HOME="$TOOLCHAIN/.android"      # adb keys, debug.keystore
export ANDROID_AVD_HOME="$TOOLCHAIN/.android/avd"   # emulator images
export ANDROID_EMULATOR_HOME="$TOOLCHAIN/.android"
export GRADLE_USER_HOME="$TOOLCHAIN/.gradle"        # gradle caches + daemon

export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
export PS1="(pacman-tv) $PS1"
```

**Leak check — run after every phase:**

```bash
[ -e ~/.android ] || [ -e ~/.gradle ] || [ -e ~/Library/Android ] && echo "LEAK" || echo "clean"
```

**Full uninstall, any time:** `rm -rf .toolchain` (~6 GB reclaimed).

### 0c. TDD approach

**The enabling decision:** all game simulation lives in `:core`, a pure-Kotlin JVM
module that *cannot* see the Android SDK. `./gradlew test` runs the entire Pac-Man
simulation on the JVM in seconds — no emulator, no device, no Robolectric. Making
it a separate Gradle *module* rather than just a package means the compiler
enforces the boundary; it can't rot.

Seams designed in from the start:

| Concern | Seam | In tests |
|---|---|---|
| Randomness (frightened turns, fruit) | `Rng` interface | Seeded `Random(42)` → deterministic |
| Persistence (difficulty, theme, high score) | `SettingsStore` interface | `FakeStore` in-memory map |
| Time | Logic advances by **tick count**, never wall clock | Call `tick()` N times |
| Input | `InputMapper` takes plain `Int` keycodes + `Float` axes | Feed raw numbers |
| **Drawing** | **`Gfx` interface** | **`RecordingGfx` — assert what was drawn** |

That last one is the payoff of the modular design: because screens draw through a
`Gfx` port instead of a raw `Canvas`, **the UI itself is unit-testable** — a test
asserts "the menu drew 3 items and highlighted index 1" with no emulator.

Test deps kept minimal: `junit:junit:4.13.2` + `kotlin-test`; plus
`androidx.test.ext:junit` for one instrumented smoke test. No Robolectric, no
mocking framework — the seams make them unnecessary.

**The cycle:** failing test → *see it fail* → minimum implementation → green → refactor.

**Honest scope of coverage:** ~90% of `:core` (all logic and screen behaviour). The
`:app` adapters — real Canvas pixel output, audio synthesis, Android lifecycle —
are verified visually in Phase 7, not by unit tests.

### 0d. Modularity — the extension points

Everything flagged as "might change later" is data or an interface, never a
hardcoded call site.

```
:core   pure Kotlin/JVM — simulation, theme model, screens, menu models, ports
:app    Android — Canvas/AudioTrack/SurfaceView adapters, resources, Activity
```

`:app` depends on `:core`; `:core` depends on nothing.

**1. `Theme` — change colours/look without touching drawing code**

```kotlin
data class Theme(
  val id: String, val displayName: String,
  val maze: MazeColors,       // wall, wallInner, door, tunnel
  val entities: EntityColors, // pacman, blinky, pinky, inky, clyde,
                              // frightened, frightenedFlash, eyes
  val hud: HudColors,         // text, score, highScore, lifeIcon
  val menu: MenuColors,       // bg, item, itemSelected, cursor, footer
  val pellet: PelletColors,
  val sprites: SpriteSource   // art is part of the theme
)
```

No colour literal exists outside a theme file. Ships **Classic Arcade** (default),
**Neon**, **Monochrome** (Game Boy green). Adding a fourth = one file + one
registry line. Selectable in Settings; persisted.

**2. `SpriteSource` — change Pac-Man's icon, ghost art, pellets**

```kotlin
interface SpriteSource { fun sprite(id: SpriteId, frame: Int): Sprite }

enum class SpriteId {
  PACMAN_RIGHT, PACMAN_LEFT, PACMAN_UP, PACMAN_DOWN, PACMAN_DEATH,
  GHOST_BODY, GHOST_EYES, GHOST_FRIGHTENED,
  PELLET, ENERGIZER, FRUIT_CHERRY, FRUIT_STRAWBERRY, /* … */ LIFE_ICON
}
```

Two implementations, chosen by the theme:
- `ProceduralSpriteSource` — drawn from code (default; nothing to download or license)
- `BitmapSpriteSource` — loads PNGs from `app/src/main/assets/sprites/<theme>/`

Replacing Pac-Man's icon later = drop a PNG in a folder, **zero code changes**.

**3. `Gfx` port — swap rendering backend, and make UI testable**

```kotlin
interface Gfx {
  val width: Int; val height: Int
  fun clear(color: Int)
  fun fillRect(x: Int, y: Int, w: Int, h: Int, color: Int)
  fun drawSprite(sprite: Sprite, x: Int, y: Int, tint: Int? = null)
  fun drawSpriteCentred(sprite: Sprite, cx: Int, cy: Int, tint: Int? = null)
  fun drawText(text: String, x: Int, y: Int, color: Int, align: Align = LEFT)
}
```

> **Deviation from the original sketch (Phase 3).** `drawSprite` takes a resolved
> `Sprite` rather than a `SpriteId` + frame. Callers do
> `theme.sprites.sprite(SpriteId.PACMAN_RIGHT, frame)` and pass the result. This
> keeps `Gfx` stateless — it needs no reference to a `SpriteSource` — and the
> extensibility rule is unaffected: call sites still only ever name a `SpriteId`,
> never a colour or a file.

`:app` implements `CanvasGfx`; tests use `RecordingGfx`. Moving to OpenGL later
would be one new implementation, nothing above it changes.

**4. `Screen` + `ScreenStack` — add, replace or reorder UI screens**

```kotlin
interface Screen {
  fun onEnter() {}
  fun update(input: InputState, tick: Long): Transition?   // None / Push / Pop / Replace
  fun render(g: Gfx, theme: Theme)
  fun onExit() {}
}
```

Adding a "High Scores" screen later = one new class + one registry entry.
Replacing the whole menu UI = swap implementations; the game never notices.

**5. `MenuModel` — menus are data, rendered generically**

```kotlin
data class MenuItem(val labelKey: String, val kind: Kind)
sealed interface Kind {
  data class Action(val onSelect: () -> Transition) : Kind
  data class Choice<T>(val options: List<T>, val get: () -> T, val set: (T) -> Unit) : Kind
  data class Submenu(val screen: () -> Screen) : Kind
}
```

One `MenuRenderer` draws any `MenuModel`. **Adding a settings option is a one-line
data change** — no new drawing or navigation code.

Supporting pieces: **`Layout`** holds every screen-position constant in one file;
**`Strings`** centralises all user-facing text.

**Content is data too:** the maze is a text resource
(`core/src/main/resources/maze/classic.txt`), not a Kotlin array — a new maze is a
new text file. Level speed tables and fruit tables live in `LevelTable.kt`.

**Honest cost:** ~15–20% more up-front code than the shortest path, mostly
interfaces plus one extra module. It pays for itself immediately in TDD, because
the `Gfx` and `SettingsStore` ports make screens testable on the JVM.

**Phase 0 gate:** `CLAUDE.md`, `env.sh`, `plan.md`, `.gitignore` written → user commits.

---

## Phase 1 — Bootstrap the sealed toolchain

Download ≈ **2.5 GB**, on-disk ≈ **6 GB**.

- [x] `mkdir -p .toolchain`
- [x] **JDK 17** — Eclipse Temurin 17 macOS **aarch64** tarball from `api.adoptium.net`
      → `.toolchain/jdk-17` (`JAVA_HOME` points at the `Contents/Home` subdir).
      AGP 8.x requires JDK 17.
- [x] **Android command-line tools** — `commandlinetools-mac-*_latest.zip` from
      `dl.google.com/android/repository`
      ⚠️ **Gotcha:** must be unpacked to `$ANDROID_HOME/cmdline-tools/latest/`. The
      zip's own top folder is `cmdline-tools/`, so it needs renaming — otherwise
      `sdkmanager` fails with "Could not determine SDK root".
- [x] `xattr -dr com.apple.quarantine .toolchain` — clears the macOS Gatekeeper flag
      on downloaded binaries. File-local; no system setting changed.
- [x] Resolve **actual current versions** via `sdkmanager --list` (do not hardcode), then install:
  - [x] `platform-tools`
  - [x] `platforms;android-34`
  - [x] `build-tools;34.0.0`
  - [x] `emulator`
  - [x] `system-images;android-34;android-tv;arm64-v8a`
  - [x] `yes | sdkmanager --licenses`
- [x] Write `scripts/setup.sh` making all of the above re-runnable and idempotent

**Phase 1 gate:**
- [x] `java -version` → 17.x
- [x] `adb version` works
- [x] `sdkmanager --list_installed` shows all packages
- [x] Leak check prints `clean`

**Risk:** if the API 34 arm64 **TV** image isn't published, fall back to
`android-33;android-tv;arm64-v8a`, or a generic API 34 AVD forced landscape.
Decide from `sdkmanager --list` at execution time and record which was used here:

> Image actually used: `system-images;android-34;android-tv;arm64-v8a` (API 34 arm64 TV image was available — no fallback needed)

---

## Phase 2 — Two-module Gradle skeleton

```
pacman-androidtv/
├── CLAUDE.md  plan.md  README.md  env.sh  .gitignore
├── settings.gradle.kts            # include(":core", ":app")
├── build.gradle.kts  gradle.properties  gradlew  gradle/wrapper/
├── scripts/{setup,emulator,build-install,tv-connect}.sh
│
├── core/                          ← PURE KOTLIN JVM — no Android, ever
│   ├── build.gradle.kts           # kotlin("jvm") — cannot see the Android SDK
│   └── src/
│       ├── main/kotlin/com/yash/pacmantv/core/
│       │   ├── ports/       Gfx.kt  Rng.kt  SettingsStore.kt  AudioOut.kt
│       │   ├── input/       InputMapper.kt  InputState.kt  Intent.kt
│       │   ├── theme/       Theme.kt  SpriteSource.kt  SpriteId.kt
│       │   │                ProceduralSpriteSource.kt  ThemeRegistry.kt
│       │   │                themes/{ClassicArcade,Neon,Monochrome}.kt
│       │   ├── ui/          Screen.kt  ScreenStack.kt
│       │   │                MenuModel.kt  MenuRenderer.kt  Layout.kt  Strings.kt
│       │   │                screens/{Menu,Settings,ControllerTest,
│       │   │                          Game,Pause,GameOver}Screen.kt
│       │   └── game/        Maze.kt  Pacman.kt  Ghost.kt  GhostAi.kt
│       │                    GameState.kt  GameLoop.kt  Difficulty.kt
│       │                    LevelTable.kt  Fruit.kt  ScoreBoard.kt  Scaling.kt
│       ├── main/resources/maze/classic.txt
│       └── test/kotlin/...        ← the TDD workhorse, runs in seconds
│
└── app/                           ← THIN Android adapter
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/sprites/<theme>/…       # optional PNG art packs
        │   ├── res/drawable/banner.xml        # 320x180 TV banner
        │   ├── res/mipmap-*/ic_launcher.png
        │   └── kotlin/com/yash/pacmantv/
        │       ├── MainActivity.kt            # hosts the SurfaceView
        │       ├── GameSurfaceView.kt         # loop thread, 60 Hz timestep
        │       ├── CanvasGfx.kt               # Gfx -> android.graphics.Canvas
        │       ├── AndroidSettingsStore.kt    # SettingsStore -> SharedPreferences
        │       ├── AndroidAudioOut.kt         # AudioOut -> AudioTrack
        │       ├── BitmapSpriteSource.kt      # SpriteSource -> assets PNGs
        │       └── AndroidInputAdapter.kt     # KeyEvent/MotionEvent -> InputState
        └── androidTest/…                      # one instrumented smoke test
```

- [x] `settings.gradle.kts` with `include(":core", ":app")`
- [x] Bootstrap the Gradle wrapper (one-shot `gradle wrapper` from a temporary
      distribution downloaded into `.toolchain`; afterwards always `./gradlew`)
- [x] `core/build.gradle.kts` — `kotlin("jvm")`, JUnit 4 + kotlin-test
- [x] `app/build.gradle.kts` — AGP, `compileSdk`/`targetSdk` **34**, `minSdk` **24**,
      `versionCode 1`, depends on `:core`
- [x] Production deps: **`androidx.core:core-ktx` only** (for `WindowInsetsControllerCompat`).
      No leanback library — every screen is custom-drawn, which is what gives the pixel-art look.
- [ ] Pin `signingConfigs.debug.storeFile` to `.toolchain/.android/debug.keystore`
      so builds are deterministic and the keystore never lands in `~/` or in git
- [x] `AndroidManifest.xml` (see below)
- [x] `res/drawable/banner.xml` — 320×180 TV home-screen banner
- [x] **TDD:** `ToolchainSmokeTest` in `:core`

### `AndroidManifest.xml` — the Android TV essentials

These decide whether the app even *appears* on the TV home screen:

```xml
<uses-feature android:name="android.software.leanback"     android:required="true"  />
<uses-feature android:name="android.hardware.touchscreen"  android:required="false" />  <!-- MANDATORY: without this the app is hidden on TV -->
<uses-feature android:name="android.hardware.gamepad"      android:required="false" />

<application android:banner="@drawable/banner" android:isGame="true" ...>
  <activity android:name=".MainActivity"
            android:screenOrientation="landscape"
            android:launchMode="singleTask"
            android:configChanges="orientation|screenSize|keyboard|keyboardHidden|navigation|uiMode"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar.Fullscreen"
            android:exported="true">
    <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
    </intent-filter>
  </activity>
</application>
```

No `INTERNET` permission — the app requests nothing.

**Phase 2 gate:**
- [x] `./gradlew test` green
- [x] `./gradlew assembleDebug` produces an APK from the empty skeleton
      (proves the whole toolchain end-to-end before a line of game code exists)
- [x] Leak check `clean`

---

## Phase 3 — Ports, theme system, renderer, game loop

**Approach: render once at arcade resolution, integer-scale up.** Keeps pixels
razor-sharp on a 77" panel instead of a soft bilinear smear.

- Virtual framebuffer **224 × 288 px** (arcade native — 28×31 tiles of 8 px, plus HUD rows)
- Draw each frame into an offscreen `Bitmap(224, 288, ARGB_8888)` via `CanvasGfx`
- Blit with `paint.isFilterBitmap = false`, `isAntiAlias = false` → **nearest-neighbour**
- Scale `floor(min(w / 224, h / 288))`, centred, black letterbox

**Getting true 4K:** Android TV apps get a 1080p surface by default even on a 4K
panel. Request the real resolution with `surfaceHolder.setFixedSize(3840, 2160)`
after querying `Display.getMode()`. If declined, the scale math handles 1080p
transparently — nothing breaks.

| Surface | Scale | Playfield | Margins |
|---|---|---|---|
| 1920 × 1080 | ×3 | 672 × 864 | 108 px |
| 3840 × 2160 | ×7 | 1568 × 2016 | 72 px |

**Game loop:** dedicated render thread on the `SurfaceView`, **fixed timestep at
60 Hz** with an accumulator. Arcade Pac-Man logic is natively 60 Hz, so one tick =
one arcade frame — this is what makes faithful timing possible *and* the simulation
deterministic enough to unit-test.

**LG C3 / OLED specifics** (all theme values, not literals):
- HUD text `#E0E0E0`, never pure white (burn-in)
- Deep `#000000` background (free on OLED; letterbox bars vanish into the bezel)
- Immersive sticky fullscreen via `WindowInsetsControllerCompat`
- `FLAG_KEEP_SCREEN_ON`
- Slow drift on menu screens so nothing is perfectly static

**Tests first (all `:core`, no device):**
- [x] `ScalingTest` — `computeScale(1920,1080)==3`, `(3840,2160)==7`, `(1280,720)==2`; dst rect centred; never 0
- [x] `GameLoopTest` — elapsed time yields the exact tick count; zero drift over 10 000 frames; long stalls clamped, not spiralling
- [x] `ProceduralSpriteSourceTest` — every `SpriteId` resolves for every frame index
- [x] `ThemeRegistryTest` — every registered theme fills every colour slot and resolves every `SpriteId`

**Then implement:**
- [x] `ports/` — `Gfx`, `Rng`, `SettingsStore`, `AudioOut`
- [x] `theme/` — `Theme`, `SpriteId`, `SpriteSource`, `ProceduralSpriteSource`, `ThemeRegistry`
- [x] `themes/` — ClassicArcade, Neon, Monochrome
- [x] `game/Scaling.kt`, `game/GameLoop.kt`
- [x] `:app` — `CanvasGfx`, `GameSurfaceView`, `MainActivity`, `AndroidSettingsStore`

**Phase 3 gate:**
- [x] Tests green (42 in `:core`, all passing)
- [x] A scaled test pattern draws on the emulator at a measured 60 fps
- [ ] ✋ **Checkpoint with user** — first thing visible on screen

> **Verified on the emulator (Phase 3).** Both display paths confirmed, straight
> from the app's own startup log:
>
> ```
> tv1080p: display reports 1920x1080
>          surface 1920x1080 -> playfield scale x3 (672x864 at 624,108)
> tv4k   : display reports 3840x2160; requesting it for the surface
>          surface 1920x1080 -> playfield scale x3        <- default surface first
>          surface 3840x2160 -> playfield scale x7 (1568x2016 at 1136,72)
> ```
>
> `setFixedSize(3840, 2160)` **does** work: the surface starts at the default
> 1080p and upgrades to true 2160p, matching the sizes quoted in the table above
> exactly. Measured 60-61 fps at both scales. Switching theme restyles the whole
> screen live, proving that extension point end to end.
>
> Two gotchas worth remembering. `adb exec-out screencap` returns 1080p even on
> the 4K AVD — it captures the WindowManager display, not our SurfaceView's
> buffer, so screenshots understate the real resolution; trust the log instead.
> And `dumpsys gfxinfo` reports nothing meaningful for this app, because it
> measures the UI thread's Choreographer while we render on our own thread via
> `lockCanvas` — the on-screen FPS counter is the real measurement.

---

## Phase 4 — Gameplay (faithful arcade), test-first

**Tests written before each unit:**

- [ ] `MazeTest` — loads `classic.txt` to 28×31; exactly **240 dots + 4 energizers**; known wall coords; tunnel wraps on row 14
- [ ] `PacmanMovementTest` — speed per tick; blocked by walls; **buffered turn** applies at the next legal tile; early input retained; tunnel wrap
- [ ] `CorneringTest` — diagonal pre-turn offsets match the arcade shortcut
- [ ] `GhostTargetTest` — Blinky → Pac-Man's tile; Pinky → 4 ahead **including the original up-direction overflow quirk**; Inky → doubled vector from Blinky through the 2-ahead point; Clyde → flips at exactly the 8-tile boundary (both sides tested)
- [ ] `ScatterChaseTest` — wave table 7/20/7/20/5/20/5/∞; forced reversal on mode change; **no** reversal when leaving frightened
- [ ] `FrightModeTest` — duration per difficulty; flash warning at the right tick; eaten → EYES → returns to house → revives
- [ ] `GhostHouseTest` — dot counters release Pinky, then Inky, then Clyde, in order
- [ ] `RestrictedTurnTest` — ghosts cannot turn upward in the four restricted tiles
- [ ] `ScoringTest` — dot 10, energizer 50; ghost chain 200→400→800→1600 **resetting per energizer**; extra life at 10 000 fires exactly once; fruit value per level
- [ ] `LevelProgressionTest` — 244 pellets → next level; speed and fright tables scale
- [ ] `DifficultyTest` — Easy/Normal/Hard yield the documented speed, lives, fright values
- [ ] `CollisionTest` — same tile → death in chase, eaten in fright, pass-through as eyes
- [ ] **`DeterministicGameTest`** — **golden regression:** seeded RNG + scripted input, 10 000 ticks headless, assert exact final score and state. Any behavioural drift anywhere fails this one test.

**Then implement:** `Maze`, `Pacman`, `Ghost`, `GhostAi`, `GameState`, `Difficulty`,
`LevelTable`, `Fruit`, `ScoreBoard`, and `maze/classic.txt`.

### Difficulty — the 3 settings tiers

| | Ghost speed | Fright duration | Lives | Waves |
|---|---|---|---|---|
| **Easy** | 85% | 9 s | 5 | Longer scatter |
| **Normal** | Arcade-accurate | Arcade table | 3 | Arcade table |
| **Hard** | 110% | 3 s | 2 | Shorter scatter, faster escalation |

All three are entries in a `Difficulty` data class — retuning them, or adding a
fourth tier, is a data change.

> **Honest scoping note:** this targets the *documented* arcade behaviour (the
> well-known Pittman/Birnbaum dossiers), not cycle-exact ROM emulation. Sub-frame
> speed tables are approximated per difficulty. It will play like Pac-Man; it will
> not be bit-identical to the 1980 ROM.

### Audio

- [ ] `AudioOut` port in `:core` emits sound *events* (unit-tested)
- [ ] `AndroidAudioOut` synthesises them with a square/triangle-wave `AudioTrack` —
      no audio files, backend swappable like everything else
- [ ] Intro jingle, alternating waka chomp, siren rising as dots deplete,
      power-pellet warble, ghost-eaten blip, death spiral, extra-life chime
      *(waveform verified by ear, not by unit test)*

**Phase 4 gate:**
- [ ] Full `game/` suite green, including the golden regression test

---

## Phase 5 — Screens, menus, input, test-first

**Tests first — these cover the UI, which the `Gfx` port makes possible:**

- [ ] `InputMapperTest` — `DPAD_LEFT` → LEFT; `AXIS_HAT_X = -1.0` → LEFT; `AXIS_X = -0.9` → LEFT; `AXIS_X = 0.3` → NONE (deadzone); two sources at once don't double-fire; `BUTTON_A`/`DPAD_CENTER`/`ENTER` → CONFIRM; `BUTTON_B`/`BACK` → BACK; `BUTTON_START`/`MENU` → PAUSE
- [ ] `MenuModelTest` — selection wraps top↔bottom; `Choice` items cycle and clamp; each entry dispatches the right `Transition`
- [ ] `MenuRendererTest` — against `RecordingGfx`: one row per item, highlights the selected index, uses `theme.menu.itemSelected` for it
- [ ] `ScreenStackTest` — Push/Pop/Replace; `onEnter`/`onExit` fire exactly once each
- [ ] `SettingsScreenTest` — difficulty and theme persist to `FakeStore`; reload restores them

**Screens** (all `Screen` implementations, drawn through `Gfx` with `Theme` colours):

- [ ] **Main menu** — PLAY / SETTINGS / EXIT, Pac-Man cursor. EXIT calls `finishAndRemoveTask()`
- [ ] **Settings** — Difficulty (Easy/Normal/Hard), **Theme** (Classic/Neon/Monochrome), Sound (On/Off), **Controller Test**, Back. All five are `MenuItem` data entries
- [ ] **Controller Test** — live device name, last keycode + symbolic name, all axis values
- [ ] **Game**, **Pause** (Resume / Restart / Quit to Menu), **Game Over**

Menu navigation uses an explicit selection index, **not** Android's focus system —
more predictable for custom-drawn UI, identical across the remote and the gamepad,
and testable on the JVM.

### Input mapping

| Intent | Sources accepted |
|---|---|
| Direction | `KEYCODE_DPAD_*` **and** `AXIS_HAT_X/Y` **and** `AXIS_X/Y` (0.5 deadzone) |
| Confirm | `BUTTON_A`, `DPAD_CENTER`, `ENTER` |
| Back | `BUTTON_B`, `KEYCODE_BACK` |
| Pause | `BUTTON_START`, `KEYCODE_MENU` |

**Why all three direction sources:** the Stadia controller's D-pad in Bluetooth HID
mode reports as either discrete `DPAD_*` keycodes *or* as a hat switch on
`AXIS_HAT_X/Y`, depending on firmware and how Android resolves the device's key
layout. Handling both (plus the left stick) means it works whichever path the unit
takes. `AndroidInputAdapter` overrides **both** `onKeyDown/onKeyUp` and
`onGenericMotionEvent` (for `SOURCE_JOYSTICK`) and filters synthetic key events
from the joystick to avoid double-processing. The mapping table itself is data in
`:core` — remapping buttons later is a data edit.

- [ ] `InputManager.InputDeviceListener` auto-pauses with "Controller disconnected"
      if the pad drops mid-game
- [ ] Google TV remote D-pad works everywhere (game fully playable without the gamepad)

> ⚠️ **Prerequisite for the user to confirm:** the Stadia Controller only speaks
> Bluetooth if Google's **Bluetooth-mode firmware update** has been applied, via
> `stadia.google.com/controller` in a Chromium browser. If the unit was never
> updated it is USB-only and won't pair. Doesn't block development either way.

**Phase 5 gate:**
- [ ] UI + input suites green against `RecordingGfx`

---

## Phase 6 — Build the APK

```bash
source env.sh
./gradlew test            # must be green first
./gradlew assembleDebug   # -> app/build/outputs/apk/debug/app-debug.apk
```

- [ ] `app-debug.apk` produced (~2–3 MB)

**On "unsigned or debug":** Android's package manager **rejects genuinely unsigned
APKs** — they cannot be installed at all. The right artifact is the
**debug-signed** APK that `assembleDebug` produces automatically. It sideloads
freely; no Play Store, no developer account, no key management. The keystore is
generated into `.toolchain/.android/debug.keystore` (kept in the sealed folder by
`ANDROID_USER_HOME`).

---

## Phase 7 — Verification

### 7a. Local emulator

```bash
source env.sh
avdmanager create avd -n tv1080p -d tv_1080p \
  -k "system-images;android-34;android-tv;arm64-v8a"
emulator @tv1080p -gpu host -no-snapshot-load &
adb wait-for-device
./gradlew installDebug
./gradlew connectedDebugAndroidTest          # instrumented smoke test
adb shell am start -n com.yash.pacmantv/.MainActivity

adb shell input keyevent KEYCODE_DPAD_DOWN   # menu navigation
adb shell input keyevent KEYCODE_DPAD_CENTER # confirm
adb exec-out screencap -p > /tmp/shot.png    # visual check
adb shell dumpsys gfxinfo com.yash.pacmantv  # frame timing
adb logcat -s PacmanTV
```

- [ ] All `:core` unit tests green; instrumented smoke test passes
- [ ] App appears with its banner on the TV launcher (leanback intent works)
- [ ] Menu renders fullscreen, no system bars, correct 16-bit look
- [ ] D-pad navigates menu; difficulty **and theme** persist across an app restart
- [ ] **Switching theme visibly restyles the whole game** — the modularity, proven end to end
- [ ] Maze, all 244 pellets, 4 ghosts render at the right scale
- [ ] Pac-Man moves, eats, dies, respawns; ghosts chase and scatter distinctly
- [ ] Energizer → fright → ghost eaten → eyes return home
- [ ] Level advances on clearing the maze
- [ ] Each difficulty visibly changes speed, lives, fright duration
- [ ] Sustained 60 fps
- [ ] EXIT actually closes the app
- [ ] ✋ **Checkpoint with user** before sideloading to the TV

*Emulator limits:* no Bluetooth gamepad emulation, and it renders at 1080p. Keycode
injection covers the logic; the real controller and true 4K are proven in 7b.

### 7b. Real hardware — Google TV Streamer

**On the Streamer, one time (user does this):**
1. Settings → System → About → tap **Android TV OS build** 7× → "You are now a developer"
2. Settings → System → Developer options → enable **USB debugging** *and* **Network debugging** (note the IP:port)
3. Settings → Apps → Security & restrictions → **Install unknown apps** (fallback path only)

**From this machine:**
```bash
source env.sh
adb connect <streamer-ip>:5555      # accept the RSA prompt on the TV
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.yash.pacmantv/.MainActivity
```

**Pair the Stadia controller:** hold **Y + Stadia** ~2 s until the light pulses
orange → Streamer Settings → Remotes & Accessories → Pair.

On the 77" panel:
- [ ] **Settings → Controller Test**: press every button and both sticks, confirm the mapping, fix anything unexpected on the spot
- [ ] Play a full life — responsiveness, buffered turns, cornering
- [ ] Confirm 2160p was actually granted (logged at startup) and pixels look crisp
- [ ] Cycle all three themes on the big screen
- [ ] Pause via Start, resume, quit to menu, EXIT

**Fallback if network `adb` is blocked** on the Wi-Fi: transfer the APK via Google
Drive or the "Send files to TV" app and install with a file manager — which is why
"Install unknown apps" is enabled above.

### 7c. Cleanliness verification (final gate)

```bash
ls ~/.android ~/.gradle ~/Library/Android 2>&1   # expect: No such file or directory
brew list 2>/dev/null | wc -l                     # unchanged
du -sh .toolchain                                 # the entire footprint
```

- [ ] Host confirmed clean

---

## Deliverables

- [ ] `CLAUDE.md` — git, TDD, module-boundary and extensibility rules
- [ ] `plan.md` — this document
- [ ] `env.sh` + `scripts/setup.sh` — reproducible, idempotent, sealed bootstrap
- [ ] `:core` — pure-Kotlin game, theme system and screens, with a fast JVM test suite
- [ ] `:app` — thin Android adapters
- [ ] `app-debug.apk` — installable
- [ ] `README.md` — activate, build, test, run on emulator, sideload to TV, delete
      everything — **plus a "How to customise" section**: add a theme, swap sprite
      art, add a settings option, add a screen, add a maze

---

## Risks and mitigations

| Risk | Mitigation |
|---|---|
| arm64 Android TV system image unavailable for API 34 | Fall back to API 33 TV image, or generic API 34 AVD forced landscape. Record the choice in Phase 1. |
| `setFixedSize(3840,2160)` ignored by the Streamer | Scale math is resolution-agnostic; silently lands on 1080p ×3. Actual resolution logged at startup. |
| Stadia controller lacks the Bluetooth firmware update | Flagged as a prerequisite; game stays playable on the TV remote meanwhile. |
| Stadia D-pad arrives as HAT axes not keycodes | Both paths handled; Controller Test screen makes any surprise immediately visible. |
| Network `adb` blocked on the Wi-Fi | Manual APK transfer + file-manager install documented. |
| Gatekeeper blocks downloaded JDK/emulator binaries | `xattr -dr com.apple.quarantine .toolchain` — file-local, no system change. |
| Abstraction overhead slows delivery | Limited to the five named extension points. Phases 2–3 still gate on a *working APK* early. |
| `:core`/`:app` split adds build complexity | One-time cost in Phase 2; it's what makes "no Android in game logic" compiler-enforced rather than a convention that erodes. |

---

## How to resume

Cold start on a new machine or after a break:

```bash
cd ~/Dev/pacman-androidtv
git pull
source env.sh                    # if .toolchain exists
./scripts/setup.sh               # if it doesn't — rebuilds the sealed toolchain
./gradlew test                   # confirm where things stand
```

Then open this file, find the first unticked `[ ]` box, and continue from there.
The **Progress at a glance** table at the top is the fast way in.

**Reminder for whoever (or whatever) is working on this:** read `CLAUDE.md` first.
Claude does not run `git add`/`commit`/`push` on this project — the user commits
manually.
