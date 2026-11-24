# Materials and Currency

Materials and their uses can be cross-referenced bidirectionally in the catalog: click "How to craft" for creation recipes, "Used in" for recipes requiring the item, and "Obtaining" for direct drops and villager trades.

## Key Materials

* **Point and Power**: Tangible items in the player inventory. Consumed according to specific altar and danmaku recipes; Trumpet Guns additionally expend one Power per shot. They do not automatically merge into a global mana bar.
* **Mob Drop Catalysts**:
  * **Fairy Dust (`fairy_dust`)**: Dropped by fairies, required in mid-to-high tier altar recipes such as Power of Nature, and traded with Priest villagers.
  * **Ghost Veil (`ghost_veil`)**: Dropped by ghosts, serving as a primary ingredient in the Crown of the Underworld.
  * **Oni Horn (`oni_horn`)**: Dropped by Oni, used for anvil repair of the Iron Rod, Oni Iron Bar, and Oni Mask, or sold to Level-4 Priests for silver coins.
  * **Danmaku Core, Gemstones, and Dream Crystal Shards**: Used across various projectiles, relics, and equipment recipes.
* **Silver Metallurgy**: **Silver Ore → Raw Silver → Silver Ingot** feeds into silver weaponry and tools. Silver weapons feature innate damage affinity against undead monsters.

## Exorcism Paper

**Exorcism Paper (`exorcism_paper`)** is a versatile anti-hostile consumable (stack size **64**, crafting yield increased to **16**):

* **Throw and Purification Radius**: Right-click to throw as a projectile or attach to surfaces/targets. Within a **2.5-block** purification radius, deals **5 magic damage** to Undead, Youkai, and Fairies.
* **Debuff Package**: Afflicted hostiles receive **Weakness II**, **Slowness II**, and **Glowing** outlines for 100 ticks.
* **Safeguards**: Completely harmless to players, tamed pets, and allied companions. Boss entities retain control immunity. Item cooldown is 10 ticks.

## Priest Villager Trades

Priest villagers offer tiered religious materials and silver equipment:

* **Level 2**: Offers 16 Exorcism Papers.
* **Level 3**: Trades 16 Fairy Dust and 8 copper coins for 16 Exorcism Papers.
* **Level 4**: Sells a silver sword enchanted with **Smite V** and **Unbreaking III**; purchases 2 Oni Horns from players for 8 silver coins.
* **Level 5**: Trades 32 Fairy Dust and 1 gold coin for 32 Exorcism Papers.

## Currency and Banking

Copper, silver, and gold coins represent distinct denominations, traded according to specific exchange recipes. Cheques store and redeem coin balances; they do not substitute for tangible Point, Power, or emerald requirements in recipes.

## Templates and Component-Bound Items

Role cards, spellcard templates, and danmaku recipe creators have internal data components defining their exact payload. Do not assume items with identical icons are interchangeable; select specific variants in the catalog to verify required recipes.

<ItemCatalog category="material" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/item/RDItems.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/Cheque.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/ExorcismPaperItem.java"]' />
