# Farming, Forestry, and Ingredients

Mod crops and specialty trees supply materials for cooking, brewing, and construction. Plant seeds on farmland or paddy fields, ensure light level ≥9, and wait for maturity; bone meal acceleration is supported.

## Planting and Harvesting

| Crop | Max Growth Stage |
|---|---:|
| Chili, Cucumber, Red Bean, Broccoli, Soybean | 4 |
| Grape, Onion | 5 |
| Toon, White Radish, Sweet Potato | 3 |
| Tomato | 3 |

**Empty-hand right-click** on mature mod crops to harvest yields while leaving the plant at seedling stage; immature crops ignore right-click harvesting. Breaking crops drops standard loot table yields.

* **Crop Reverse-Decomposition**: Harvested mod produce (chili, cucumber, grape, onion, red beans, tomato, toon, white radish, sweet potato, broccoli) can be crafted into **2 of their matching seeds** at a crafting table (excluding self-seeding soybeans), providing rapid multiplication.
* **Vine Foraging**: Breaking vanilla vines (`minecraft:vine`) with any hoe has a **10% chance to drop a grape**, offering an early fruit source before locating grape seeds.

## Paddy Fields

**Paddy Fields** represent spiritually hydrated farmland that remains perpetually moist and **never reverts to dirt from dehydration or entity trampling**. Breaking a paddy field returns dirt.

## Maple Trees and Forestry

Three distinct maple species generate naturally in temperate forest biomes:

* **Maple Species**: **Iroha Maple**, **Itaya Maple**, and **Hauchiwa Maple**.
* **Complete Wood Sets**: Each wood type includes full logs, wood, stripped variants, leaves, saplings, planks, stairs, slabs, doors, trapdoors, fences, fence gates, buttons, and pressure plates.
* **Natural Foliage Decay**: Maple leaf canopies adhere to vanilla distance-based decay rules, shedding naturally and dropping saplings after supporting logs are chopped.

## Animal Products, Fish, and Trades

Meats, seafood, and seasonings originate beyond farmed crops. Review individual ingredient records in the catalog for butcher trades and creature loot tables. Companion farming replants only matching seeds; see [NPC Companions](../entities/npc-companion).

<ItemCatalog category="ingredient" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/block/RDCropBlocks.java","common/src/main/java/cc/thonly/reverie_dreams/block/base/AbstractCropBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/PaddyFieldBlock.java"]' />
