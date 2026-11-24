# Weapon Controls and Combat Mechanics

Weapon attributes, durability and components can be inspected in the catalog below. Normal use, sneak-use, and holding stances trigger distinct and verified mechanics.

## Numbered Lunar Weapons

| Type | Capacity | Fire mode | Shot interval | Reload wait |
|---|---:|---|---:|---:|
| I rifle | 30 | Automatic | 2 ticks | 40 ticks |
| II pistol | 12 | Semi-automatic | 7 ticks | 26 ticks |
| III shotgun | 8 | Semi-automatic, 8 pellets per ammo | 16 ticks | 56 ticks |
| IV SMG | 40 | Automatic | 1 tick | 36 ticks |
| V sniper | 5 | Semi-automatic | 24 ticks | 60 ticks |

At normal speed, 20 ticks = 1 second. Carry a **matching numbered magazine**. Right-click to fire; while sneaking, release a short press below 6 ticks to reload, or hold to aim and **left-click to fire**. Hold left-click for automatic fire with Types I and IV. Release use to lower the gun. Reload waiting prevents firing. Remaining old-magazine ammo is returned; check the ground if your inventory was full.

Crafted magazines are empty. To fill one, combine a matching magazine, 1 Power Block and 5 gunpowder in any order on a crafting table.

Aiming applies Slowness and reduces spread. Experience lowers recoil: level 0 uses the original multiplier, level 16 uses 51%, and **level 32+ reaches the 2% minimum**. Aiming also reduces recoil subject to that floor. Nausea multiplies the final recoil by 1.3. Each weapon retains its own base recoil; firing does not consume experience.

## Trumpet Gun

Carry **Power items**. Right-click for a burst of up to three shots, consuming **one Power per successful shot**. One or two Power are sufficient for a shorter burst; a full three is not required. Removing ammo or switching weapons affects remaining shots. Creative mode does not consume Power.

## Clubs and Heavy Weapons

| Weapon | Damage | Durability | Knockback | Properties & Repair |
|---|---:|---:|---:|---|
| **Iron Rod** | 8 | 400 | 0.8 | Plays mace heavy smash audio on hit with solid horizontal knockback; repairable with **Oni Horns** at an anvil |
| **Oni Iron Bar** | 10 | 800 | 1.4 | High-impact heavy club crafted on the altar using an Iron Rod core; repairable with **Oni Horns** |

Both clubs deliver blunt knockback without forced vertical impulses, preventing infinite juggling loops.

## Relic and Unique Weapon Actions

| Weapon | Control and Effect |
|---|---|
| **Throwing Knife** | Silver affinity (bonus damage against undead). Right-click fires a **5-knife fan volley** (center blade 5.5 damage, side blades 4.5 damage, projectile speed 3.0), 2-second cooldown (40 ticks), costs 5 durability, subject to a 64-bullet per-use volley budget |
| **Flower Parasol** | Right-click deploys a **2-second** (40 ticks) floral guard granting Speed II and Resistance I, reflecting frontal projectiles and eliminating danmaku; 5-second cooldown (100 ticks), 800 durability, fire resistant; altar recipe uses Point and Power instead of flint |
| **Gungnir** | Custom `ThrownTouhouTrident` projectile. Hold right-click to ready a charge, then left-click while holding to throw; left-click alone jabs. Retains full trident enchantments (Loyalty, Riptide, Channeling) and water inertia |
| **Nue's Trident** | Custom thrown entity; grants a **+30% water-sprint speed bonus** when held in either the main or off hand |
| **Bagua Furnace** | Handheld right-click shoots fireballs (sneak-hold to charge). As a **deployed turret**, operates on a charge-burst state machine: 2s tracking rest → 0.7s locked charge (furnace swells, end-rod particles gather with audio cues) → 0.3s burst firing two fan volleys of three lasers each (4 damage per laser, ±2.5° then ±6° spread). Cycle lasts ~3s for ~5 bursts over its 15s lifespan |
| **Vaisravana's Pagoda** | Two-mode relic: right-click fires auto-aimed fan golden lasers that ignite enemies and can dislodge coins; sneak-right-click opens a **12-second** domain bombarding nearby enemies and granting allied players Luck II, Strength I, and Fire Resistance |
| **Jeweled Branch of Hourai** | Right-click launches large high-velocity fireballs dealing 3 damage and igniting targets for 3 seconds; 5-second cooldown |
| **Magic Broom** | Right-click on a block deploys a rideable vehicle; normal hits emit rune rings and spirals; falling jump attacks add bonus impact damage (2 base + 0.5 per block fallen, up to 6 blocks) with descending gold/magenta slash arcs (does not trigger on ground, in water, on ladders, or while flying with elytra) |
| **Wind-Blessing Gohei** | 66% chance of wind-charge in rain; sneak-hold right-click for 10 seconds to execute the Wind's Blessing ritual (costs 25% hunger, creates pentagram particles, grants nearby non-hostiles Luck II for 3 minutes and Regeneration for 1.5 minutes, 360s cooldown) |
| **Treasure Hunting Rod** | Sneak-right-click runs a 6-neighbour BFS vein scan (range: 8 horizontal, 8 down, 4 up); renders true visible ESP block outlines through walls (colored per ore type), marking up to 12 blocks of the nearest vein and 1 of others (max 20 markers, expires after 100 ticks, clears when mined); outputs distance and distribution lines to chat/action-bar |
| **Hakurei Cane** | Right-click fires three columns of ofuda danmaku |
| **Roukanken** | Extended reach; right-click fires wind projectiles and marks nearby undead |
| **Maple Leaf Fan** | Right-click unleashes wind projectile sweeps |

## Graze Mechanic

When hostile danmaku or high-speed projectiles pass in extreme proximity to the player without a direct hit, a **Graze** event triggers with dedicated audio feedback (`RDSoundEvents.GRAZE`). Each projectile can graze once, throttled by a brief audio cooldown.

## Tengu Shield

Hold right-click to block. Activation delay is **0.25 seconds**, durability **600**. Face incoming attacks directly; flank/rear hits and unblockable damage bypass the shield.

<ItemCatalog category="weapon" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/item/weapon/LunarWeaponType.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/WeaponOfTheMoon.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/TrumpetGun.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/TenguShieldItem.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/IronBarItem.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/Knife.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/YukaFlowerUmbrella.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/BaguaFurnace.java","common/src/main/java/cc/thonly/reverie_dreams/item/weapon/TreasureHuntingRod.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/VaisravanasPagodaItem.java","common/src/generated/resources/assets/reverie_dreams/lang/zh_cn.json"]' />
