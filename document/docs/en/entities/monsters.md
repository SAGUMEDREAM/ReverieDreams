# Creatures and Youkai

This page lists registered entities, including wilderness youkai, fairies, ghosts, lunar units and neutral creatures. Attributes are base values before equipment, difficulty, effects or datapacks.

## Oni and Combat Balance

**Oni** are high-threat melee hostiles wielding an Iron Rod or Oni Iron Bar.

* **Melee Reach and Attack Cadence**: Oni attack range is tuned to **2.2–2.5 blocks** based on `ENTITY_INTERACTION_RANGE`, eliminating inflated hitboxes. Attacks feature a **12-tick windup animation** and a **40-tick attack cooldown**, allowing players to dodge or counter-attack when the swing begins.
* **Knockback and Impact**: The Iron Rod and Oni Iron Bar apply horizontal knockback (strength 0.8 and 1.4 respectively) accompanied by mace impact audio. Forced vertical lift impulses are removed to prevent inescapable juggle loops.
* **Kin Disguise**: Wearing an **Oni Mask** (`oni_mask`) projects intimidation that causes Oni to regard the wearer as kin, **preventing unprovoked hostile targeting** (retaliation when damaged remains active).
* **Loot**: Slain Oni drop **Oni Horns** (`oni_horn`), used at an anvil to repair the Iron Rod, Oni Iron Bar, and Oni Mask, or traded to a Level-4 Priest for silver coins.

## Forest Youkai and Faction Reputation

**Forest Youkai** (Fox, Tanuki, and Cat species) spawn at night across standard Overworld forests (excluding maple forests). They cannot be tamed as companions; interactions are governed by faction reputation.

* **Combat Roles**: Naturally spawned Forest Youkai appear as either **melee defenders** or **ranged danmaku skirmishers**.
* **Meat Feeding and Standing**: Right-clicking raw or cooked meat onto a Forest Youkai grants **+5 standing** in `FactionReputationComponent.yokai`. Players can feed successfully up to **4 times per in-game day**. Damaging a Forest Youkai incurs a **-10 standing** penalty.
* **Dynamic Safe Zone**: At **50+ standing**, Forest Youkai become friendly and provide a dynamic safe zone within a **128-block radius** around the player, deterring hostile monster aggression.

## Fairies

The fairy family includes common Fairies, Maid Fairies, Sunflower Fairies, and Ice Fairies.

* **True 3D Flight**: All wild fairies employ `FlyingPathNavigation` and `FlyingMoveControl` for full three-dimensional movement and hovering. They are **immune to fall damage** while flying, returning to the ground only when leashed or sitting.
* **Aimed Danmaku Accuracy**: Maid Fairies re-aim each shot at target velocity lead, firing high-speed kunai and knives with reduced spread.
* **Loot**: Fairies drop **Fairy Dust** (`fairy_dust`), a key catalyst in mid-to-late-game altar crafting.

## Ghosts, Goblins, and Lunar Units

* **Ghost**: Natural spawning is controlled by `enableGhostSpawn` (disabled by default). Slain ghosts drop **Ghost Veil** (`ghost_veil`), required for the Crown of the Underworld altar recipe.
* **Goblin**: Tagged under `reverie_dreams:youkai`, taking full damage and debuffs from Exorcism Paper and youkai-effective equipment.
* **Moon Rabbit and Lunar Rabbit Soldier**: Inhabit the Moon dimension. Soldiers wield standard-issue firearms with high fire rate and mobile flanking behavior.

<EntityCatalog />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/entity/Oni.java","common/src/main/java/cc/thonly/reverie_dreams/entity/ForestYoukai.java","common/src/main/java/cc/thonly/reverie_dreams/item/armor/OniMaskItem.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/IronBarItem.java","common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java"]' />
