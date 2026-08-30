# Fix log — "You Are The Monster"

This document tracks every issue found in the code review, the fix applied, and
why that fix was chosen over the alternatives. Grouped by area, roughly in the
order the problems bite a new player.

The game was verified end‑to‑end by running it headless (Xvfb + a scripted
input driver, `Robot`‑based) and inspecting screenshots at each step: title →
opening cutscene → level 1 → movement/jump/collision → combat (hit, damage
numbers, death, score) → exit portal → level‑complete screen → next level.

---

## 1. Build & tooling

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 1.1 | `build.sh` calls `javac` with no `-encoding`, so the non‑ASCII comment in `BossVampireLord` broke every build on a US‑ASCII default charset. | Added `-encoding UTF-8` to `build.sh`; also removed the offending comment (see 4.1). | `build.gradle` already sets UTF‑8; the hand‑rolled script just needed to match. |
| 1.2 | `gradlew` had no execute bit and there was no `gradlew.bat`, so `./gradlew shadowJar` (the README's headline command) failed on Linux/macOS and Windows had no wrapper at all. | `chmod +x gradlew` (tracked via `git update-index --chmod=+x`); added a standard `gradlew.bat`. | Restores the documented build path on every platform without adding a Gradle install requirement. |
| 1.3 | CI (`.github/workflows/build.yml`) ran `./gradlew shadowJar` on `windows-latest` (PowerShell), which cannot execute a POSIX shell script. | Changed all three jobs to `gradle shadowJar`. | `gradle/actions/setup-gradle@v3` already puts a real `gradle` on `PATH`; using it side‑steps the wrapper‑vs‑shell mismatch entirely. |
| 1.4 | `Game extends Application` was the jar's `Main-Class`, so `java -jar YouAreTheMonster.jar` throws *"JavaFX runtime components are missing"* — the classic shaded‑jar/JavaFX trap. | Added `Launcher` (a plain class whose `main` calls `Game.main`); pointed `application.mainClass`, `jar` and `shadowJar` manifests at it. | The only reliable way to launch a JavaFX app from a classpath jar. One tiny class, zero behaviour change. |
| 1.5 | `run.sh` hard‑coded `-cp "build/classes:lib/json.jar"`, discarding the `$CP` it had just assembled and breaking if `json.jar` was absent. | Use the assembled `$CP` (which conditionally includes `json.jar`). | Removes a latent "class not found" for anyone without the JSON jar. |
| 1.6 | 18 stale 2016 `.class` files were committed under `src/`. | `git rm` the class files (and the now‑dead sources — see 6). `.gitignore` already excludes `src/*.class`. | Compiled artefacts don't belong in VCS; they were also months out of date. |

## 2. Runtime crashes & the game loop

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 2.1 | The update loop ran on a **background thread** (`Task` + `Thread.sleep(50)`) that called `Platform.runLater` every 50 ms. Enemy death did `game.removeUnit(this)` **while `updateAll` iterated the `characters` HashSet** → guaranteed `ConcurrentModificationException` the first time any enemy finished dying. | Replaced with a single `AnimationTimer` on the FX thread using a fixed‑timestep accumulator (30 logic ticks/s, render every frame). `updateAll` iterates a **snapshot** (`new ArrayList<>(enemies)`); `Attack` iterates `getCollisionSet().toArray(...)`. | `AnimationTimer` is the idiomatic JavaFX loop and removes the cross‑thread hazard entirely. Fixed timestep keeps physics deterministic; snapshotting makes add/remove‑during‑iteration safe without a deferred‑removal queue. |
| 2.2 | Logic ran at 20 FPS; camera + sprites stepped at 20 FPS too. | Logic at 30 Hz, **camera follow and parallax run every rendered frame** (~60 FPS) for smooth scrolling. Movement constants retuned for 30 Hz. | 30 Hz is a good balance for integer‑velocity physics (coarser and speeds get chunky; finer and the retune risk grows). Rendering the camera per‑frame is cheap and removes the visible judder. |
| 2.3 | `LevelData.loadFromJSON` used `new FileReader("levels/1_1.json")` — a path relative to the process CWD, which nothing sets up. Every load threw `FileNotFoundException`, was swallowed, and fell back to a hard‑coded arena. **All 18 level files were dead.** | `LevelData.load()` reads via `getResourceAsStream("/levels/…")` (UTF‑8). Kept a default arena as a genuine last resort. | Levels are bundled on the classpath (jar or `build/classes`); the classpath is the only location that works in every run mode. |
| 2.4 | X11 delivers `release`+`press` pairs for a held key; `setOnKeyReleased` removed the key immediately, so holding a direction stuttered at ~50 % speed. | Track `releaseAt[key] = now`; a subsequent press cancels it; `tick()` only really drops the key once a 60 ms grace window elapses. Edge actions (jump/attack) fire only on a *fresh* press (`keysDown.add` returned true). | Deterministic relative to game ticks (a `Platform.runLater`‑based version raced the pulse). Harmless on Windows/macOS where repeats behave differently. |

## 3. Missing game loop / progression (the "18 levels" were unreachable)

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 3.1 | Nothing advanced levels. `loadLevel(0)` was called once; `levelComplete()`, `loadNextLevel()`, `VictoryScreen`, `CutsceneSystem` were never invoked from anywhere. You could not finish level 1. | Added an **exit portal** (`LevelData.exit`, a generous doorway hitbox) → touching it calls `levelComplete()` → `VictoryScreen` → `afterLevelComplete()` → next level or, at area boundaries, an area‑intro cutscene, or (after the boss) the ending. | A visible goal object is the clearest possible "you finished the level" signal and needs no extra UI. The doorway hitbox extends to the ground so it is always reachable by walking. |
| 3.2 | `TitleScreen`, `PauseMenu`, `GameOverScreen`, `VictoryScreen`, `CutsceneSystem` all existed but were dead code — `Game` re‑implemented menus inline as raw `Text` nodes (white text on a white background: invisible). | Deleted the inline versions; wired the real screen classes as overlays in a dedicated UI layer. State machine: `MENU → CUTSCENE → PLAYING ↔ PAUSED → LEVEL_COMPLETE / GAME_OVER`. | The screen classes were already written and styled (dark panels); using them removed ~120 lines of duplicated inline UI and fixed the invisible‑menu bug for free. |
| 3.3 | `DifficultyManager` computed HP/damage multipliers that were **never read**. | Applied on level build: player max HP = `100 × healthMultiplier`; every enemy gets `scaleStats(enemyHealthMultiplier, damageMultiplier)`. | The multipliers were already sensible; they just needed to reach the entities. |
| 3.4 | `LevelData` parsed an `items` array that `LevelManager` never instantiated (`Meat`, `Pickup` were dead). | `Game.spawnPickup()` creates a hovering `Pickup`; `tick()` checks overlap with the player → heals + score + sound + despawn. | Small, self‑contained; gives the meat pickups (which the level files already place) an actual effect. |
| 3.5 | Cutscenes never triggered. | Opening cutscene on New Game; area‑intro cutscene (indices 1–5) when entering areas 2–6; ending cutscene (index 6) after the Vampire Lord dies. | Uses the `CutsceneSystem`'s existing script verbatim; the trigger points map 1:1 to the "6 areas" structure. |

## 4. Enemies & combat

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 4.1 | `BossVampireLord.die()` contained a machine‑generated Chinese comment (`// Boss死亡特殊处理`) that also broke the ASCII build. | Removed it; `die()` now calls `game.onBossDefeated()` to drive the ending. | Dead comment, real bug. |
| 4.2 | "8 enemy types" was really 3 with art: `Guard`/`Zombie`/`Knight`/`Boss` loaded the **player** sprites, `Spider` loaded the **bat** sprites, and the `skeleton` set was a desaturated copy of the player. The 6 humanoid enemies were ~90 lines of near‑identical copy‑paste each. | Extracted `WalkerEnemy` (shared patrol/chase/wind‑up‑and‑hit state machine). Each enemy is now ~10 lines: stats + sprite folder + a distinct `ColorAdjust` **tint** (bone‑white skeleton, steel guard, green zombie, dark‑iron knight, crimson boss). Documented that bespoke sprites are still wanted. | Tinting is a genuine stop‑gap that makes enemies *visually distinguishable right now* without art skills; the dedup makes the eventual real fix (unique behaviour/art per type) a small change instead of a 6‑file one. |
| 4.3 | Patrolling enemies walked straight off platform edges and fell to the ground. | `Character.groundAhead()` / `wallAhead()` probes; `WalkerEnemy` turns at ledges/walls and won't chase off a cliff. | Cheap AABB probes (walls only, ~10 per level); makes patrols look deliberate and keeps platform enemies on their platforms. |
| 4.4 | The attack was cosmetic: `Player.attack()` dealt all damage in a single one‑frame loop, then spawned an `Attack` entity that just displayed `FACERIGHT1.png` for 200 ms. | `Attack` is now the hitbox: a 6‑tick swipe that damages each enemy **once** (tracked set), draws a visible arc, and follows the player. Added knockback (`applyKnockback` + `hitStun` so it isn't instantly overwritten by input) and floating damage numbers via `Game.onPlayerHitEnemy`. | Makes the swing readable and the hit feel physical; the "damage once per swing" set prevents a single swipe from deleting an enemy in one frame. |
| 4.5 | Bats had gravity and would sink; enemy `update(Player)` signatures didn't match the loop; `moveHorizontal(dir, click)` had inconsistent semantics across enemies (bat's "move right" comment called it with `left=true`). | `Character` gained an explicit `walk(dir)` / `inputDir` model and a `flying` flag (bats skip gravity, steer in both axes). All enemies rewritten to `walk(±1)`. | One unambiguous movement API; removes the per‑file guesswork about what `(true,false)` meant. |
| 4.6 | Gravity was `velocity += 2 * velocityTime` with `velocityTime++` each frame — quadratic acceleration, so jumps yanked you down unnaturally. | Linear `velocity += GRAVITY`, capped at `fallCap`. | Standard platformer gravity; predictable arcs. |
| 4.7 | `CollisionUnit.checkOverlap` was a dense, stringly‑typed point‑sampler that only sampled points **left of** a wall's origin — it never detected the player standing on the full‑width ground strip, so the player fell through the world. | Replaced with straightforward swept‑AABB resolution in `Character` (`moveAxis`): move one axis, push out of any overlapping wall, repeat for the other axis. `isOnGround()` is a 2px downward probe. | The old routine was subtly broken for any wall wider than the mover; a plain AABB resolver is correct, tiny, and easy to reason about. `checkOverlap` is deleted. |

## 5. Rendering & visuals

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 5.1 | No level background was drawn at all — `Game` built a bare `Pane`, `GameBackground.fxml` and `LevelData.background` were unused, and the `Scene` had no fill, so the game ran on a **white** background. | Layered scene graph: solid dark sky → tiled parallax backdrop (`ImagePattern`, 1.5× native scale, scrolls at 0.4×) → world → UI. | The one background image (`background layers.png`) is genuinely good; tiling + parallax makes it fill arbitrarily wide levels without the stretched‑pixel look. |
| 5.2 | `Wall` stretched the 640 px `foreground.png` (a mostly‑transparent grass strip) to its natural size at every wall's top‑left — dozens of overlapping grass images that never lined up with the (invisible) collision boxes. | `Wall` now draws a **sized stone block** (gradient fill + lit top lip) exactly matching its rectangle. | Always correct regardless of wall size; reads unambiguously as a platform. A tileset would look nicer but needs art. |
| 5.3 | Only one background exists for all six "areas". | Per‑area `ColorAdjust` on the backdrop (village brighter, graveyard desaturated, caves dark, castle cold, throne‑room crimson). Documented that real per‑area art is still wanted. | Cheap way to give each area a distinct mood now. |
| 5.4 | A permanent dev overlay (`State: PLAYING | Health: …`, `Level: 1/18`) sat next to the HUD in normal play. | Removed. A richer debug overlay now exists but only when `-Dgame.debug=true`. | Players shouldn't see engine internals; developers still can, opt‑in. |
| 5.5 | `SpriteAnimation` forced a fixed 500 ms cycle for every state (run and idle animated at the same rate); its `addSequence(…, cycleDuration)` overload ignored the argument. | Per‑sequence durations are honoured; `loadAnim()` sets run ≈ 480 ms, idle ≈ 800 ms, fall = single frame. | The API already implied this; it just wasn't implemented. |
| 5.6 | Player/enemy sprites never flashed on hit; deaths just vanished. | Invincibility flash (opacity strobe), death spin‑and‑fade for enemies. | Standard "juice"; makes hits and kills legible. |

## 6. Audio & dead code

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 6.1 | `AudioManager` was a no‑op that printed "Loading sounds…" and loaded nothing; it used the removed `java.applet.AudioClip`. | Rewrote it to synthesise short tones with `javax.sound.sampled` (jump, attack, hit, hurt, pickup, enemy‑down, victory, game‑over). Probes for a usable mixer at startup and **disables itself silently** if there isn't one (headless CI). | No audio assets ship with the game; procedural blips are self‑contained, tiny, and give the game a pulse. Graceful degradation keeps it crash‑free on machines with no sound device. |
| 6.2 | `BatAttack`, `Boulder`, `BonePile`, `Breakable` were referenced nowhere; `BonePile` loaded a path that doesn't exist (`resources/pickup/BonePile.png`). `GameBackground.fxml` was unused. | Deleted. | Dead weight; `BonePile` would have crashed if ever instantiated. |
| 6.3 | `CollisionUnit.getImageView()` returned `ImageView`, forcing every unit's visual to be an `ImageView` (why `Wall` abused one). | Generalised to `getNode()` returning `javafx.scene.Node`. | Lets `Wall` be a `Pane`, `Attack` an `Arc`, sprites an `ImageView` — each unit draws itself with whatever node fits. |

## 7. Levels

| # | Issue | Fix | Why |
|---|-------|-----|-----|
| 7.1 | The 18 level files were near‑identical: scattered 32 px "platforms" placed 30–70 px above the ground, which act as **waist‑high walls** the player can neither pass nor (easily) mount — the player got wedged a few seconds into level 1. Enemy `y` values put some enemies inside geometry. | Regenerated all 18 from `scripts/genlevels.py` (deterministic seed per level). Rules that guarantee completability: one solid ground strip the whole width; floating platforms confined to a band 130–300 px above the ground (walk under *and* jump onto); the exit sits on the ground at the right edge; enemies spaced ≥ 260 px and never in a wall; difficulty (length, enemy count/mix) scales with area. `6_3` is a dedicated boss arena. | Hand‑authoring 18 good levels is out of scope for a fix pass; a generator with invariants gives coherent, always‑finishable levels and is easy to re‑run or tune. The ground path is always clear, so even a hard platform section can't soft‑lock the player. |

---

## Known limitations / follow‑ups (not fixed here)

* Enemy art is still tints of the player/bat sprites — bespoke pixel art wanted for skeleton, guard, zombie, knight, spider, Vampire Lord.
* One background image, colour‑shifted per area — real per‑area backdrops wanted.
* The Vampire Lord is melee‑only with timing‑based "phases"; no projectiles or unique attacks.
* Unlockable abilities from `docs/PLAN.md` (charge, ground slam, wall‑jump, howl, rage) are not implemented.
* No save system; progress is per‑session.
* Levels are procedurally generated with invariants, not hand‑tuned for pacing.
