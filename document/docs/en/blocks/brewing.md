# Brewing barrels and drinks

Barrel recipes include alcoholic and non-alcoholic drinks. Actual beverage properties determine their category and customer suitability.

## Brew and bottle

1. Place a barrel and right-click to open its ingredient inventory. Input slots accept one item each.
2. Add only the ingredients for the desired recipe, close the inventory and **sneak-right-click with an empty hand** to start.
3. Wait while the barrel's chunk remains active.
4. Use **glass bottles** on the finished barrel to collect drinks. A batch allows **9 bottlings**, consuming one bottle per collection.
5. Empty the completed batch before starting another.

Starting also consumes items in extra occupied slots, so avoid unrelated materials. A menu-book entry does not supply drink inventory.

Recipe cost_time is in ticks; the Wiki divides it by 20. Plum Wine's **1120 ticks means 56 seconds**. Low server tick speed and unloaded chunks increase real waiting time. Check the actual bottled stack for explicit properties when serving customers.

<RecipeCatalog type="reverie_dreams:barrel" />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/BrewingBarrelBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/entity/BrewingBarrelBlockEntity.java"]' />
