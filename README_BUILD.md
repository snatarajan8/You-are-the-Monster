# You Are The Monster

A Ludum Dare 33 game where you play as a cursed werewolf.

## Quick Start (Pre-built)

Download the latest release from the [Releases](https://github.com/snatarajan8/You-are-the-Monster/releases) page.

- **Windows**: Download and run `YouAreTheMonster.exe`
- **macOS**: Download and run `YouAreTheMonster.app`
- **Linux**: Download and run `YouAreTheMonster`

## Building from Source

### Prerequisites

- Java 17 or higher ([Download](https://adoptium.net/))
- Gradle (included via wrapper)

### Build Steps

**Option 1: Using build script (recommended)**
```bash
./build.sh
```

**Option 2: Using Gradle directly**
```bash
./gradlew shadowJar
```

The fat JAR will be at: `build/libs/YouAreTheMonster-1.0.0.jar`

### Run the JAR

```bash
java -jar build/libs/YouAreTheMonster-1.0.0.jar
```

### Create Native Executable (Optional)

If you have `jpackage` (included with Java 14+):

```bash
# Linux
jpackage --input build/libs --main-jar YouAreTheMonster-1.0.0.jar --name YouAreTheMonster --type app-image --dest build/executables

# Windows (in CMD)
jpackage --input build\libs --main-jar YouAreTheMonster-1.0.0.jar --name YouAreTheMonster --type app-image --dest build\executables

# macOS
jpackage --input build/libs --main-jar YouAreTheMonster-1.0.0.jar --name YouAreTheMonster --type app-image --dest build/executables
```

## GitHub Actions

The repository includes GitHub Actions workflows that automatically build for:
- Windows
- macOS
- Linux

When you push to `master`, the workflow will:
1. Build fat JARs
2. Create native executables using jpackage
3. Create a release with all executables

## Controls

- **Arrow Keys**: Move
- **UP**: Jump
- **Mouse Click**: Attack
- **ESC**: Pause

## Difficulty Levels

- **Easy**: 2x HP, 0.5x enemy damage
- **Normal**: Balanced
- **Hard**: 0.5x HP, 2x enemy damage
- **Nightmare**: One-hit kills, no healing

## Game Structure

- **6 Areas** with **3 Levels** each = **18 Levels**
- Dark Forest → Abandoned Village → Haunted Graveyard → Cursed Caves → Castle Walls → Throne Room
- Boss fights including the Vampire Lord

## License

Created for Ludum Dare 33
