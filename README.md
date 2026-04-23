# Animal Farm
CMSC 137 Final Project — A.Y. 2025-2026, Second Semester

A 2D Auto-Battler game. Two teams of two players defend a shared Barn across two lanes. A test of resource management and unit matchups.

---

## Tech Stack

| Tool | Version |
|---|---|
| Java | 21+ (tested on JDK 23) |
| JavaFX | OpenJFX 21.0.5 |
| Build | Maven 3.9+ |

---

## Prerequisites

- **JDK 21+** — [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** — `brew install maven` (macOS) or [Download](https://maven.apache.org/download.cgi)

Verify your setup:
```bash
java -version   # should be 21+
mvn -version    # should be 3.9+
```

---

## Run

```bash
cd animal-farm/
mvn javafx:run
```

Maven will download OpenJFX on first run. Subsequent runs are instant.

---

## Project Structure

```
CMSC137AnoTara/
├── animal-farm/                     Main Maven project
│   ├── pom.xml                      Maven config & OpenJFX dependencies
│   └── src/main/java/com/animalfarm/
│       ├── App.java                 JavaFX entry point — window & scene setup
│       │
│       ├── model/                   Pure game state (zero JavaFX imports)
│       │   ├── GameConfig.java      Constants: unit stats, map dimensions, economy
│       │   ├── GameState.java       Top-level game logic & update loop
│       │   ├── Lane.java            Lane state: units, barns, positions
│       │   ├── Player.java          Feed balance and spawn cooldown per player
│       │   ├── Unit.java            Unit POJO: position, HP, direction, state
│       │   ├── UnitType.java        Enum of unit types with stats
│       │   └── Barn.java            Barn POJO: HP pool and position
│       │
│       ├── view/                    Rendering layer (reads model, never mutates)
│       │   ├── GameRenderer.java    Draws lanes, barns, and units to Canvas
│       │   └── HUDRenderer.java     Updates Feed and HP labels
│       │
│       └── controller/              Input & loop orchestration
│           ├── GameLoop.java        AnimationTimer: ticks model, calls renderers
│           └── InputHandler.java    Maps key presses to spawn commands
│
docs/                            Design documents and assets
```
