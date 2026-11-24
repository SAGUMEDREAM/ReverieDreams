# Cooking and kitchenware

Recipes use one of five station types: **cooking pot, cutting board, frying pan, grill or steamer**. Check the station on the recipe card first; another station type does not provide the same recipes.

## Cook a dish

1. Place kitchenware on a supported surface and right-click it.
2. Put the required ingredients into its five input slots.
3. Click the desired result among the matching dishes. Loading ingredients alone does not select a dish.
4. Wait for completion, then collect the output. Keep the area ticking.

One item is consumed from **every occupied input slot**, including extra ingredients. Container remainders may be returned. The current kitchenware does not require an additional coal input to start.

## Extra ingredients and properties

The recipe determines the base dish. Extra ingredients can add food properties; explicit properties on a finished stack override that item's defaults. Two dishes with the same name can therefore receive different customer evaluations.

Conflicting properties can produce Dark Cuisine. Check the actual finished stack and customer preferences before serving. The catalog lists default properties, not every possible custom combination.

## Completion time

Cards show base recipe seconds. Manual GUI cooking currently adds **1.25 seconds**. The Quick Recipe Book path instead adds **0.25 seconds per base recipe ingredient**. Divide that total by the station's progress multiplier:

| Series | Progress multiplier | Random failure chance |
|---|---:|---:|
| Normal | 1.00 | 0 |
| Mystia | 0.75 | 0 |
| Super | 1.05 | 0 |
| Extreme | 1.10 | 0 |
| Nuclear | 1.50 | 40% |

Mystia's current progress rate is 0.75, so it takes longer in this version. Property conflicts are a separate failure check.

Right-click a **Quick Recipe Book** to save a recipe, then use it on matching, already stocked kitchenware. It does not supply free ingredients. Use the manual interface to verify custom extra-ingredient results. Supported dishes can also be placed; select “Placed dish model” in their details to inspect that form.

<ItemCatalog category="food" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/gui/block/KitchenBlockGui.java","common/src/main/java/cc/thonly/reverie_dreams/block/entity/KitchenwareBlockEntity.java","common/src/main/java/cc/thonly/reverie_dreams/registry/content/block/RDKitchenBlocks.java","common/src/main/java/cc/thonly/reverie_dreams/util/item/ItemUtils.java","common/src/main/java/cc/thonly/reverie_dreams/item/prop/FastRecipeBook.java"]' />
