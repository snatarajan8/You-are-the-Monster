#!/usr/bin/env python3
"""Regenerate the 18 level JSON files with coherent, playable geometry.

Rules that keep every level completable:
  * one solid ground strip spanning the whole level (the exit is always
    reachable by just walking right, so a hard platform section can't soft-lock)
  * bounding walls left/right
  * floating platforms sit 130-300px above the ground: high enough to walk
    under when standing, low enough to always jump onto
  * platform-to-platform steps <= 90px, gaps <= 120px
  * the exit sits on the ground at the right edge
  * enemies are spaced >= 260px apart and never inside a wall
  * length, enemy count and enemy mix scale with the area number

Run from the repo root:  python3 scripts/genlevels.py
"""
import json, os, random

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "levels")
H = 704
GROUND_Y = H - 56
FOOT = GROUND_Y            # y of the ground surface

AREA = {
    1: ("Dark Forest",        ["bat", "werewolf"]),
    2: ("Abandoned Village",   ["guard", "bat"]),
    3: ("Haunted Graveyard",   ["skeleton", "zombie"]),
    4: ("Cursed Caves",        ["spider", "bat"]),
    5: ("Castle Walls",        ["knight", "guard"]),
    6: ("Throne Room",         ["knight", "skeleton"]),
}
SUBTITLES = ["Entrance", "Depths", "Heart"]


def pick(rng, pool):
    # pool[0] is the common/weak enemy, pool[1] the tougher one
    return pool[0] if rng.random() < 0.7 else pool[1]


def build(area, level, rng):
    name, pool = AREA[area]
    width = 2000 + area * 260 + level * 340
    max_enemies = 2 + area + level
    walls = [
        {"x": 0, "y": GROUND_Y, "width": width, "height": 60},
        {"x": -20, "y": 0, "width": 20, "height": H},
        {"x": width, "y": 0, "width": 20, "height": H},
    ]
    enemies, items = [], []

    # Floating platforms live in this band: high enough to walk under when
    # standing (player is ~44 tall) yet always reachable by a jump.
    PLAT_LOW = GROUND_Y - 130
    PLAT_HIGH = GROUND_Y - 300

    # walk a cursor left -> right laying down platforms and things to fight
    x = 360
    foot_y = PLAT_LOW
    last_enemy_x = 0
    while x < width - 420:
        gap = rng.randint(70, 120)
        pw = rng.randint(120, 240)
        # next platform height: step up or down but stay within the band + reachable
        dy = rng.choice([-90, -70, 0, 0, 70, 90])
        py = max(PLAT_HIGH, min(PLAT_LOW, foot_y + dy))
        if rng.random() < 0.72:
            walls.append({"x": x, "y": py, "width": pw, "height": 26})
            foot_y = py
            if rng.random() < 0.5 and x - last_enemy_x > 260 and len(enemies) < max_enemies and pw >= 150:
                enemies.append({"type": pick(rng, pool), "x": x + pw // 2,
                                "y": py - 44, "patrol": max(50, pw - 60)})
                last_enemy_x = x
            if rng.random() < 0.35:
                items.append({"type": "meat", "x": x + pw // 2, "y": py - 40})
            x += pw + gap
        else:
            # a stretch of open ground with a grounded enemy
            if x - last_enemy_x > 260 and len(enemies) < max_enemies:
                enemies.append({"type": pick(rng, pool), "x": x,
                                "y": FOOT - 46, "patrol": rng.randint(140, 240)})
                last_enemy_x = x
            foot_y = FOOT
            x += rng.randint(220, 320)

    # guarantee a couple of pickups
    while len(items) < 2:
        items.append({"type": "meat", "x": rng.randint(500, width - 500), "y": FOOT - 46})

    data = {
        "name": f"{name} - {SUBTITLES[level - 1]}",
        "area": area,
        "levelNumber": level,
        "background": "resources/background layers.png",
        "width": width,
        "height": H,
        "playerStart": {"x": 90, "y": FOOT - 60},
        "exit": {"x": width - 150, "y": FOOT - 72},
        "walls": walls,
        "enemies": enemies,
        "items": items,
    }
    return data


def boss_level():
    width = 1600
    walls = [
        {"x": 0, "y": GROUND_Y, "width": width, "height": 60},
        {"x": -20, "y": 0, "width": 20, "height": H},
        {"x": width, "y": 0, "width": 20, "height": H},
        {"x": 240, "y": GROUND_Y - 150, "width": 180, "height": 24},
        {"x": 1180, "y": GROUND_Y - 150, "width": 180, "height": 24},
        {"x": 690, "y": GROUND_Y - 230, "width": 220, "height": 24},
    ]
    return {
        "name": "Throne Room - The Vampire Lord",
        "area": 6,
        "levelNumber": 3,
        "background": "resources/background layers.png",
        "width": width,
        "height": H,
        "playerStart": {"x": 90, "y": FOOT - 60},
        "exit": {"x": width - 150, "y": FOOT - 72},
        "walls": walls,
        "enemies": [{"type": "vampirelord", "x": width // 2, "y": FOOT - 120, "patrol": 0}],
        "items": [
            {"type": "meat", "x": 300, "y": GROUND_Y - 190},
            {"type": "meat", "x": 1270, "y": GROUND_Y - 190},
            {"type": "meat", "x": 800, "y": GROUND_Y - 270},
        ],
    }


def main():
    os.makedirs(OUT, exist_ok=True)
    for area in range(1, 7):
        for level in range(1, 4):
            if area == 6 and level == 3:
                data = boss_level()
            else:
                rng = random.Random(area * 100 + level)  # deterministic
                data = build(area, level, rng)
            path = os.path.join(OUT, f"{area}_{level}.json")
            with open(path, "w") as f:
                json.dump(data, f, indent=2)
            print(f"{area}_{level}: w={data['width']} enemies={len(data['enemies'])} items={len(data['items'])}")


if __name__ == "__main__":
    main()
