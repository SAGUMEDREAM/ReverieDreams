# Gensokyo Altar

Look up the altar block's own crafting recipe, then build its eight pillars before attempting altar recipes.

## Interactive construction preview

Drag to rotate, use the top view to check positions, or select an individual layer. Select a block to read its coordinates relative to the altar. Prepare **1 Gensokyo Altar, 16 Stripped Jomon Sugi Logs and 8 Blessed Spiritual Logs**.

<AltarStructure />

## Pillar positions

Use the altar block as the origin, east as +X and south as +Z. Each pillar has **Stripped Jomon Sugi Logs at Y and Y+1**, and a **Blessed Spiritual Log at Y+2**. Use paper on a Stripped Jomon Sugi Log to bless it.

| X | Z |
|---:|---:|
| 0 | -4 |
| -3 | -3 |
| 3 | -3 |
| -4 | 0 |
| 4 | 0 |
| -3 | 3 |
| 3 | 3 |
| 0 | 4 |

These Y positions are relative to the altar's Y, not an assumed floor below it. Other wood and visually similar decorations cannot replace the required blocks.

## Load and activate

Right-click to open the altar inventory. The layout is row-major around the center:

| Outer 0 | Outer 1 | Outer 2 |
|---|---|---|
| Outer 3 | **Core 8** | Outer 4 |
| Outer 5 | Outer 6 | Outer 7 |

Follow the recipe diagram exactly, including empty slots, counts and component variants. Close the inventory and **sneak-right-click the altar** to validate the structure and craft. The result replaces the core slot; surplus core items are returned or dropped. Point and Power are actual ingredient stacks.

If nothing crafts, check pillar coordinates/materials, the center, outer slot order, quantities and the selected role/template variant.

<RecipeCatalog type="reverie_dreams:gensokyo_altar" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/block/props/GensokyoAltarBlock.java","common/src/main/java/cc/thonly/reverie_dreams/gui/block/GensokyoAltarGui.java","common/src/main/java/cc/thonly/reverie_dreams/mixin/item/ItemMixin.java"]' />
