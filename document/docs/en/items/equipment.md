# Equipment and Armor

Armor must be worn in its dedicated slot to take effect; held items function according to individual controls. Click entries below to inspect verified durability, default components, and recipes.

| Equipment | Verified Properties & Active Mechanics |
|---|---|
| **Earphone** | Displays directional indicators on the HUD pointing toward nearby sound sources |
| **Koishi Hat** | Grants complete invisibility while moving at sneak-walk speed |
| **Oni Mask** | Durability 198. Radiates Oni Intimidation: hostile monsters within an **8-block radius** receive **Weakness I for 4 seconds** every 2 seconds. Oni regard the wearer as kin and **never initiate unprovoked hostility**. Repairable with **Oni Horns** at an anvil |
| **Low Gravity Boots** | Grants Jump Boost and Slow Falling while worn; ideal for vertical traversal and mountain leaps |
| **Crown of the Underworld** | Undead creatures ignore the wearer; inverts potion effects (Harming heals, Healing damages). Altar recipe requires Ghost Veil and Wither Skeleton Skulls |
| **Speed Feather** | Held in either main or off hand to grant a movement speed boost; also serves as a projectile speed modifier on the Strengthening Table |

Tengu Shield controls are detailed under [Weapon Controls](./weapons). Do not confuse Lunar dimension low gravity with the active boot effect.

## Maximum Health

Consume **Life Fragment (Remnant Life)** to permanently gain one extra heart of maximum health, up to the server's `maxUpgradedHealthValue`. Shards and complete fragments are separate items; inventory shards do not passively heal or increase health.

<ItemCatalog category="equipment" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/item/RDItems.java","common/src/main/java/cc/thonly/reverie_dreams/item/armor/OniMaskItem.java","common/src/generated/resources/assets/reverie_dreams/lang/zh_cn.json"]' />