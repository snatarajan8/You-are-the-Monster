# Game Completion Plan - Ludum Dare 33

## Ludum Dare 33 Theme: "You Are The Monster"

**Objective:** Complete the game to deliver 10+ hours of playtime with high quality.

---

## User Decisions

- **Quality:** High quality, take as long as needed. Overdeliver on quantity too.
- **Art:** High quality pixel art, matching existing dark/gothic style.
- **Level Style:** Use judgment, with stylistic variance (mix of linear, room-based, vertical).
- **Difficulty:** Multiple difficulty tiers (Easy, Normal, Hard).
- **Story:** Include cutscenes and narrative elements (time permits).

---

## Current State Assessment

### What Works
- JavaFX rendering engine with 1280x704 window
- Player movement (left/right with acceleration)
- Jumping + double jumping
- Collision detection system (rectangle-based)
- Sprite animation system (state-based transitions)
- Background and foreground tile rendering
- Game loop (50ms tick, ~20 FPS)

### What's Broken
1. **Attack system** - adds and immediately removes Attack on same frame (Player.java:67-69)
2. **Enemy update signatures** - `EnemyWerewolf.update(Player)` and `EnemyBat.update(Player)` take Player param but game loop calls `Character.update()` with no params
3. **Werewolf sprites** - FACELEFT/FACERIGHT files missing (code references them at EnemyWerewolf.java:24-25)
4. **BatAttack** - assigns `this.directionx = directionx` (field to itself) instead of using constructor param
5. **Key release** - commented out in Game.java:47-55; velocity resets every frame instead of on key release
6. **Health system** - health defaults to 0, no initialization, no death handling, no UI
7. **`.project`** - has unresolved git merge conflict

### What's Missing (for 10 hours of playtime)
- No enemies spawned anywhere
- No level progression (1 hardcoded screen)
- No UI/HUD
- No menus (title, pause, game over)
- No audio
- No win/lose conditions
- No game progression or narrative
- No player abilities beyond basic attack

---

## Game Design: "Lunar Curse"

### Concept
You ARE the monster. Play as a cursed werewolf fighting through a dark fantasy world. Grow stronger, unlock abilities, defeat the Vampire Lord who cursed you.

### Progression (targeting 10+ hours)
- **6 Areas**, each with **3 Levels** = **18 levels total**
- Each level: 20-40 minutes (exploration + combat + puzzles)
- Total: 18 levels x ~35 min = ~10.5 hours

### Areas
1. **Dark Forest** (Tutorial) - Learn basics, simple enemies
2. **Abandoned Village** - Human enemies, introductions
3. **Haunted Graveyard** - Undead enemies, verticality
4. **Cursed Caves** - Environmental hazards, darkness mechanics
5. **Castle Walls** - Heavy combat, ranged enemies
6. **Throne Room** - Final area, boss gauntlet, Vampire Lord

### Player Character
- Werewolf form (existing sprites)
- 100 HP (5 hearts)
- Melee attack (claw swipe)
- Unlockable abilities per area:
  - Area 1: Double Jump (existing)
  - Area 2: Charge Attack (dash forward with damage)
  - Area 3: Ground Slam (AoE damage below)
  - Area 4: Wall Jump/Climb
  - Area 5: Howl (stun nearby enemies)
  - Area 6: Rage Mode (temporary damage boost)

### Enemies (per area)
1. **Dark Forest:** Bats, Rats, Skeletons
2. **Abandoned Village:** Villagers, Guards, Archers
3. **Haunted Graveyard:** Zombies, Ghosts, Skeleton Warriors
4. **Cursed Caves:** Cave Spiders, Boulders (rolling), Dark Wraiths
5. **Castle Walls:** Knights, Crossbowmen, Mages
6. **Throne Room:** Vampire Knights, Blood Mages, Vampire Lord (Boss)

### Items and Collectibles
- **Meat** - Heals 20 HP (existing sprite)
- **Bone** - Currency for ability upgrades
- **Soul Fragments** - Story items (18 total, one per level)
- **Health Upgrade** - Increases max HP (hidden in levels)

### Difficulty Tiers
- **Easy:** 2x HP, 0.5x enemy damage, more pickups
- **Normal:** Standard balance (1x HP, 1x damage)
- **Hard:** 0.5x HP, 2x enemy damage, fewer pickups, aggressive AI
- **Nightmare:** 1-hit kills, no healing items, permadeath option

### Game Flow
1. Title Screen -> New Game -> Difficulty Selection
2. Opening cinematic (story introduction)
3. Level select (unlocked sequentially)
4. Gameplay loop: Explore -> Fight -> Collect -> Boss
5. Area complete -> Ability unlock -> Cutscene -> Next area
6. Final boss -> Ending cinematic -> Credits

### Cutscenes and Story
- **Opening Cinematic:** Text-based intro with artwork explaining the curse
- **Area Transitions:** Short story segments between areas
- **Ability Unlocks:** Brief cutscene showing power acquisition
- **Boss Introductions:** Each boss has intro dialogue
- **Ending:** Multiple endings based on collectibles found
- **Style:** Visual novel style with character portraits and text

---

## Technical Architecture Changes

### 1. Fix Existing Bugs
- Fix attack system (add duration timer before removal)
- Fix enemy update() signatures (remove Player parameter)
- Create missing werewolf FACELEFT/FACERIGHT sprites
- Fix BatAttack constructor parameter bug
- Fix key release handling
- Initialize player health to 100
- Resolve .project merge conflict

### 2. New Core Systems
- **GameState** - Manage game states (MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE, CUTSCENE)
- **LevelManager** - Load/save levels, manage transitions
- **Camera** - Scrollable viewport following player
- **HUD** - Health bar, ability icons, score display, difficulty indicator
- **MenuSystem** - Title screen, pause menu, game over screen, difficulty select
- **AudioManager** - Sound effects and music playback
- **ParticleSystem** - Visual effects (blood, dust, particles)
- **SaveSystem** - Persist progress between sessions
- **CutsceneSystem** - Story delivery with portraits and text
- **DifficultyManager** - Scale enemy stats and pickups based on difficulty

### 3. Level System
- Levels defined as JSON/text files (not hardcoded)
- Each level file specifies:
  - Background image
  - Wall/platform positions
  - Enemy spawn points and types
  - Item/pickup locations
  - Player start position
  - Level exit location
  - Environmental hazards
  - Puzzle elements
  - Cutscene triggers

### 4. Enemy AI Improvements
- Fix patrol behavior (correct direction detection)
- Add proper state machine (IDLE, PATROL, CHASE, ATTACK, HURT, DEAD)
- Add enemy health bars
- Add death animations
- Add loot drops
- Difficulty scaling (more aggressive on Hard/Nightmare)

### 5. Combat System Overhaul
- Player attack with proper timing and hitbox
- Enemy attack patterns (melee, ranged, charge)
- Knockback on hit
- Invincibility frames after taking damage
- Screen shake on heavy hits
- Damage numbers popup
- Combo system (light attack, heavy attack)

### 6. Art Assets to Create

#### Sprites Needed (pixel art, matching existing dark/gothic style)
- **Player:** Attack animation (6 frames), Hurt animation (3 frames), Death animation (4 frames)
- **Werewolf:** FACELEFT (3 frames), FACERIGHT (3 frames) - MISSING
- **Enemies (20+ enemy types):**
  - Villager (idle, walk, attack, hurt, death) - 15 sprites
  - Guard (idle, walk, attack, hurt, death) - 15 sprites
  - Archer (idle, walk, shoot, hurt, death) - 15 sprites
  - Skeleton (idle, walk, attack, hurt, death) - 15 sprites
  - Zombie (idle, walk, attack, hurt, death) - 15 sprites
  - Ghost (idle, float, attack, hurt, death) - 15 sprites
  - Knight (idle, walk, attack, hurt, death) - 15 sprites
  - Mage (idle, walk, cast, hurt, death) - 15 sprites
  - Spider (idle, crawl, attack, hurt, death) - 15 sprites
  - Wraith (idle, float, attack, hurt, death) - 15 sprites
  - Vampire Knight (idle, walk, attack, hurt, death) - 15 sprites
  - Vampire Lord Boss (idle, phase1, phase2, phase3, hurt, death) - 18 sprites
- **Projectiles:** Arrow, Magic bolt, Blood orb, Spider web
- **Effects:** Blood splatter, Dust cloud, Sparkle (for pickups), Moon glow
- **UI:** Health heart (full, empty, half), Ability icons, Button sprites, Panel backgrounds
- **Environment:** Grass, Stone, Dirt, Wood, Metal tiles for each area

#### Backgrounds (6 unique backgrounds)
- Dark Forest (modify existing)
- Abandoned Village
- Haunted Graveyard
- Cursed Caves
- Castle Walls
- Throne Room

#### Foreground Tiles (6 tilesets)
- Forest floor/walls
- Village buildings
- Graveyard stones
- Cave rocks
- Castle stone
- Throne room ornate

---

## Implementation Phases

### Phase 1: Foundation (Fix and Core Systems) ~20%
1. Fix all existing bugs
2. Implement GameState system
3. Implement Camera system
4. Implement basic HUD (health bar)
5. Implement Level file loader
6. Convert hardcoded level to file format
7. Implement difficulty selection

### Phase 2: Combat and Enemies ~25%
1. Fix and improve attack system
2. Create enemy state machine
3. Fix enemy AI and spawning
4. Create 6 enemy types for first 2 areas
5. Implement combat mechanics (knockback, iframes, damage numbers)
6. Create death/loot system
7. Implement difficulty scaling

### Phase 3: Content - Areas 1-3 ~25%
1. Design and create Dark Forest levels (3)
2. Design and create Abandoned Village levels (3)
3. Design and create Haunted Graveyard levels (3)
4. Create all enemy sprites for these areas
5. Create backgrounds and tiles for these areas
6. Implement first 3 unlockable abilities
7. Create cutscenes for area transitions

### Phase 4: Content - Areas 4-6 ~20%
1. Design and create Cursed Caves levels (3)
2. Design and create Castle Walls levels (3)
3. Design and create Throne Room levels (3)
4. Create all enemy sprites for these areas
5. Create backgrounds and tiles for these areas
6. Implement remaining abilities
7. Create Vampire Lord boss fight
8. Create cutscenes for area transitions and ending

### Phase 5: Polish and Menus ~10%
1. Create Title Screen
2. Create Pause Menu
3. Create Game Over / Victory screens
4. Add audio (sound effects + music)
5. Add particle effects
6. Add screen shake and juice
7. Implement save system
8. Final testing and bug fixes
9. Create opening and ending cinematics

---

## Art Style Guidelines

Based on existing sprites:
- **Resolution:** Pixel art, characters ~32x32 to 64x64 pixels
- **Palette:** Dark purples, blues, blacks, grays. Occasional yellow/orange for eyes/fire.
- **Style:** Dark gothic fantasy, slightly cartoonish but moody
- **Lighting:** Night scenes, moonlight, shadows
- **Theme:** Horror-fantasy, you ARE the monster

### Color Palette (extracted from existing sprites)
- Darkest: #0a0a1a (background shadows)
- Dark: #1a1a3a (clothing, dark areas)
- Mid: #3a3a5a (skin, highlights)
- Light: #5a5a7a (bright highlights)
- Accent: #ffaa00 (eyes, fire)
- Accent: #ff0000 (blood, damage)

---

## Level Design Philosophy

### Stylistic Variance
- **Area 1 (Dark Forest):** Linear progression, tutorial-focused
- **Area 2 (Abandoned Village):** Room-based exploration, vertical buildings
- **Area 3 (Haunted Graveyard):** Vertical climbing, platforming challenges
- **Area 4 (Cursed Caves):** Maze-like with environmental hazards
- **Area 5 (Castle Walls):** Combat-heavy horizontal gauntlets
- **Area 6 (Throne Room):** Mixed style, boss rush

### Level Elements
- **Secret Areas:** Hidden rooms with collectibles
- **Environmental Puzzles:** Switches, moving platforms, darkness/light
- **Enemy Placement:** Strategic, not spam
- **Difficulty Scaling:** More enemies on higher difficulties
- **Time Trials:** Optional speed run challenges
- **Collectible Hunts:** 100% completion rewards

---

## Implementation Notes

### Compilation
- Project uses Eclipse IDE (`.classpath`, `.project`)
- Java 8 with JavaFX
- No build system (Maven/Gradle) - relies on Eclipse JDT
- Compiled `.class` files are in `src/` (should be gitignored)

### Git Setup
- Resolve `.project` merge conflict first
- Add `.gitignore` for `.class` files
- Consider adding Maven/Gradle for better build management

### Testing Strategy
- Manual testing (no test framework exists)
- Test each system independently before integration
- Playtest each level for balance
- Performance profiling for particle effects and large enemy counts

---

## Success Criteria

1. **Playability:** Game runs without crashes, all mechanics work
2. **Content:** 18 playable levels across 6 areas
3. **Art:** High quality pixel art matching existing style
4. **Audio:** Sound effects and background music
5. **Polish:** Smooth animations, screen shake, particles
6. **Story:** Intro, area transitions, ending cutscenes
7. **Replayability:** Multiple difficulty levels, collectibles
8. **Duration:** 10+ hours of gameplay
