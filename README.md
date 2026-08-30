# You Are The Monster

A Ludum Dare 33 game where you play as a cursed werewolf fighting through dark fantasy areas to defeat the Vampire Lord who cursed you.

**Theme:** "You Are The Monster"

## Download

Download the latest release from the [Releases](https://github.com/snatarajan8/You-are-the-Monster/releases) page.

- **Windows**: Download and run `YouAreTheMonster.exe`
- **macOS**: Download and run `YouAreTheMonster.app`
- **Linux**: Download and run `YouAreTheMonster`

## Quick Start (from source)

Requires Java 17+ ([Download](https://adoptium.net/))

```bash
./gradlew shadowJar
java -jar build/libs/YouAreTheMonster-1.0.0.jar
```

## Controls

| Key | Action |
|-----|--------|
| Arrow Keys | Move |
| UP | Jump (press again for double jump) |
| Mouse Click | Attack |
| ESC | Pause |

## Difficulty Levels

- **Easy**: 2x HP, 0.5x enemy damage, more pickups
- **Normal**: Balanced gameplay
- **Hard**: 0.5x HP, 2x enemy damage, fewer pickups
- **Nightmare**: One-hit kills, no healing

## Game Structure

- **6 Areas** with **3 Levels** each = **18 Levels** (~10+ hours)
- **8 Enemy Types**: Bats, Werewolves, Skeletons, Guards, Zombies, Spiders, Knights, Vampire Lord
- **Boss Fights** at the end of each area
- **Story Cutscenes** between areas
- **Unlockable Abilities** as you progress

### Areas

1. **Dark Forest** - Tutorial area, learn the basics
2. **Abandoned Village** - Human enemies, introduction to combat
3. **Haunted Graveyard** - Undead enemies, vertical platforming
4. **Cursed Caves** - Environmental hazards, maze-like levels
5. **Castle Walls** - Heavy combat, armored knights
6. **Throne Room** - Final area, boss gauntlet, Vampire Lord

## Building from Source

See [README_BUILD.md](README_BUILD.md) for detailed build instructions.

## License

Created for [Ludum Dare 33](https://ldj.am/)
