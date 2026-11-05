# Gauntlet of Hell (NeoForge 1.21.1)

Build: JDK 21, then `./gradlew build` (jar ends up in build/libs/), or let the GitHub Action build it.

Craft: obsidian, netherite ingots and a nether star (see data/hellgauntlet/recipe), or `/give @s hellgauntlet:gauntlet_of_hell`.

Stats: 10000 durability, 5 attack damage, 1.8 attack speed, mending/sharpness/unbreaking allowed,
fire resistant, dropped copies survive explosions.

Controls
- Right click: shoot the orb (or the single laser in laser mode)
- Hold right click: continuous laser (laser mode, "Continuous laser")
- Shift + right click: next mode
- X: laser mode on/off (rebind in Controls under "Gauntlet of Hell")

Durability per use: orb Small 3, Entity only 5, Medium 5, Large 8; laser 10; continuous laser 10 every 2.5 seconds.

Continuous laser on something it can't delete (bedrock, portal frames and so on): the beam stays on and its tip
vibrates in and out with sparks, instead of making a new ball that fades away every few ticks. It still makes one
ball the first time to clear whatever it can around that spot. Tune it in LaserBeamEntity.java (VIBRATE_AMPLITUDE,
STUCK_TICKS).
Tweak numbers in BlastMode.java and LaserMode.java.

Hell Skull structure (all data files, no code), find it with `/locate structure hellgauntlet:hellskull`
- Where: Nether, in basalt deltas and soul sand valley. Edit data/hellgauntlet/tags/worldgen/biome/has_structure/hellskull.json to change the biomes.
- How often: worldgen/structure_set/hellskull.json. Spacing 57 means about 2x as many placement attempts as the woodland mansion (spacing 80). Density goes with 1/spacing^2, so a bigger spacing makes it rarer.
- Height: worldgen/structure/hellskull.json, `start_height`. The skull is placed at a random Y in that range and the terrain is blended around it (`beard_thin`).
- Look: data/hellgauntlet/structure/hellskull.nbt. Replace that file to change the building.
- Free space: the template is padded with air (5 blocks around, 7 above, nothing below), so when it lands inside a basalt pillar it carves a bubble around itself instead of being buried. If you rebuild the template, keep a margin of air blocks around it for the same effect.
