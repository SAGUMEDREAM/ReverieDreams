# Gensokyo, the dream world and the Moon

The registered dimensions are **reverie_dreams:gensokyo**, **reverie_dreams:dream_world** and **reverie_dreams:the_moon**. A ruined shrine or other structure is not another dimension.

## Gensokyo

Use a **Boundary Ofuda on a block** to open a two-way gap for 3 minutes, consuming one ofuda. In Gensokyo it attempts to return to the recorded origin. Bring supplies, a spare ofuda and remember the exit.

Gensokyo uses supplied map data and a dedicated generator. Allow initial installation/loading to complete. Forests, underground content, ores and buildings follow the current map; this guide does not invent fixed coordinates or guaranteed rewards.

## Dream world

**Sneak-use a Dream Pillow on an Overworld bed**. After sleeping there, waking triggers entry at a usable **Huai'an Station** arrival platform when one is available. Waking after sleep inside the dream world returns to the Overworld spawn.

The station contains a waiting pavilion, a bed, low tea tables and a reading corner. Guarded, three-block-wide paths lead west to **Bamboo Memory**, south to **The Unfinished Tea Garden**, and east to **Mirror Pond**. The islands contain a broken bamboo gate, half a tea room, and a pond bridge with empty dream gates. These are explorable scenes with usable existing furniture; dedicated quests, shops and dynamic puzzles are separate future features.

Island clusters repeat every 512 blocks, with core walking floors at Y=128. The central station arrival is `(0,129,8)`. The red/blue grid has solid, walkable lines: red at Y=32 and blue at Y=64 below the islands, then red at Y=192 and blue at Y=224 above them. Lines remain one block wide and 16 blocks apart horizontally. Y=80..184 is reserved for island and building activity; the main grid is no longer cut by the scenes' surrounding viewing reservations. Dream-stone/crystal rocks still avoid scenes and paths. Zombie/skeleton trial rooms no longer generate. Existing rooms and player builds are preserved; an old or damaged arrival platform makes the entrance search for another usable station, so entry is not guaranteed to use the origin cluster.

The wilderness now includes **Crystal Reef, Cherry Isle and Archive Ruin**, bringing the total to six residual-dream themes. Each cluster has four extra exploration islands with world-seeded coastlines, position, rotation and shuffled floor heights of Y104/116/140/152. These outer islands have no core-level bridges, so bring building or mobility tools. Asymmetric coasts and tapering undersides replace the uniform oval bases.

Dream water and its underwater fog are pink. Dream-stone/crystal rocks are less frequent, smaller, flattened and bevelled, avoiding dense large spheres and single-block debris. The generation update affects new chunks; no existing save or chunk was cleared or regenerated.

The pillow is installed on the bed, rather than a freely reusable instant-teleport item. Special lethal-damage handling should not be treated as a promise that exploration has no hazards.

## Moon

In the End, ascend to the world's height threshold using flight or building to reach the Moon. The Moon grants continuous Slow Falling, while entities in the Dream World fall according to their usual gravity. Rising above either world's ceiling returns you to the End with 40 seconds of Slow Falling for landing.

Current lunar content includes highlands, basalt mare, shadowed ice craters and sea boundaries, with caves and ores. Prepare supplies and a return route. Read [save migration](./moon-upgrade) before upgrading an existing Moon. Optional [Gensokyo compression](./chunk-compression) is separate and defaults off.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/world/dimension/RDBuiltInDimensions.java","common/src/main/java/cc/thonly/reverie_dreams/mixin/entity/LivingEntityMixin.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/DreamPillowItem.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/BoundaryOfuda.java"]' />
