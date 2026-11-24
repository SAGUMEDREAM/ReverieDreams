# Danmaku, Templates and Strengthening

Danmaku projectiles are defined by **shape, color and concrete attributes**. Creation templates, spellcard templates, and fireable danmaku serve different roles, and items with the same ID can carry distinct components.

## Crafting Route

1. Obtain a **Danmaku Shape Blueprint** (formerly **Danmaku Recipe Creator**, `danmaku_recipe_creator` / `danmaku_shape_blueprint`) and right-click to open the canvas. Draw a supported silhouette matching the pattern shown in the [Recipe Catalog](../recipes); arbitrary sketches do not synthesize new danmaku types.
2. Open the **Danmaku Crafting Table**, inserting dyes, a Danmaku Core, Power, Point, and the configured **Danmaku Recipe Creator** into their respective slots. Slot predicates validate true item types without rejecting valid inputs.
3. Take the finished danmaku item once generated. Ingredients consume the recipe's exact quantities. Blank creator templates cannot substitute for a pattern-inscribed creator.
4. Right-click with the finished danmaku to fire. Unless possessing the infinite property, durability is consumed per shot until exhausted. Shots obey per-type cooldowns.

In recipe diagrams, "Material" indicates a creator template inscribed with shape components; check detailed item components to distinguish otherwise identical templates.

## Strengthening Table Adjustments

Place the upgradeable danmaku into the primary slot and a modifier into the secondary slot, then click the output preview to claim it. Each operation consumes one item from the input slots.

| Secondary Material | Rule |
|---|---|
| **Spellcard Template** | Replaces the trajectory template and copies its trajectory configuration |
| **Speed Feather** | +0.25 projectile speed, capped at ≤2.5 |
| **Slime Block** | +1 volley count, capped at <3 (yielding a maximum of 2) |
| **Iron Sword** | +0.25 projectile damage, capped at <5.5 |

Reaching the upper limit produces no further valid output. Recipe registries display runtime operations to show valid inputs; final stats inherit from your placed item, not from generic catalog icons.

Strengthening Table network synchronization utilizes chunked channels, ensuring large numbers of projectile recipes and trajectories never exceed vanilla's 2 MiB single-packet NBT limit or cause player disconnects.

## Trajectories and Volley Budget

* **16 Trajectory Patterns**: Supports 16 parameterized trajectories including fan, ring_view, ring_horizontal, round, pattern, and bullet, configurable through the Strengthening Table.
* **Unified Volley Budget**: All danmaku firing from items and NPCs is subject to a strict **64-bullet per-use limit** (`DanmakuVolleyBudget`), preventing server tick lag or client rendering crashes from excessive bullet floods.

## Hitbox Scaling and Star Rendering

* **Per-Type Hitbox Sizing**:
  * **Bubble (Large Ball)**: Slower travel speed with an enlarged collision box and sweep volume for area denial.
  * **Glowing Ball, Note, Rice**: High projectile speed with lower base damage for sustained skirmishing.
  * **Bullet**: High linear speed and focused single-target damage.
* **Smooth 2D Star Spin**: Star danmaku incorporates a synced 2D ROLL angle that spins smoothly along the screen plane, billboarded to always face the camera like authentic Touhou projectile stars.

## Spellcards and Bombing

Right-click a spellcard to release its inscribed danmaku pattern. Using a **Bomb** erases nearby enemy danmaku and converts it into Point items, consuming the bomb. Hatate Himekaidou's camera phone also provides photographic danmaku clearing; these operate independently of lunar firearm magazines.

<ItemCatalog category="danmaku" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/gui/block/DanmakuCraftingTableGui.java","common/src/main/java/cc/thonly/reverie_dreams/recipe/type/StrengthTableRecipeType.java","common/src/main/java/cc/thonly/reverie_dreams/item/danmaku/AbstractDanmakuItem.java","common/src/main/java/cc/thonly/reverie_dreams/entity/misc/DanmakuEntity.java"]' />
