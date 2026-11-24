# Opening a restaurant

Use the mod's **night-sparrow bell** and **menu book**. A vanilla bell does not substitute for this facility.

## Set up your first shop

1. Place both blocks. They record an owner; the owner or a creative player can configure them.
2. Right-click the menu book. Select at least one valid dish and one valid drink from their separate categories. These are ghost configuration entries, not inventory or free food.
3. Use search, paging and the selected-only filter. Each category accepts up to 128 entries.
4. **Sneak-right-click the bell** to bind a nearby menu book. While closed, adjust its horizontal radius from 2–16, default 8; its vertical range is 8. A book must be in range and cannot serve two shops simultaneously.
5. Prepare reachable seats, real dish/drink stock and customer work points inside the shop area.
6. **Right-click the bell normally** to open. A valid reciprocal binding and both menu categories are required.

## Orders and closing

Customers order from this shop's menu. Close with a normal bell right-click to stop new orders; accepted orders retain their original menu snapshot. Changing a menu does not silently replace an accepted order.

Removing/replacing facilities or rebinding invalidates the previous facility identity. A replacement block at the same coordinates is not the same shop. Services wait while required facility chunks are unavailable.

## Generic versus dedicated customers

A role uses a dedicated customer profile only when that role has an actual registered profile. Uncovered roles use the generic customer rules; they do not borrow another character's budget, preferences or evaluations. Both kinds require an open shop and obey its menu.

Once seated and ordering, normal empty-hand right-click asks for the order. Normal right-click while holding a dish or drink submits one of that category; both categories complete the serving. Sneaking does not submit. Properties matter, especially for dedicated preferences; matching a name alone may not give the same evaluation. The non-eating order-refresh countdown and post-serving eating duration are separate [settings](../settings).

If customers do not order, check the bell type, opening state, binding, both menu categories, work-point range, seat access and previous eating period, in that order.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/block/entity/RestaurantBlockEntity.java","common/src/main/java/cc/thonly/reverie_dreams/gui/block/RestaurantGui.java","common/src/main/java/cc/thonly/reverie_dreams/restaurant/RestaurantService.java","common/src/main/java/cc/thonly/reverie_dreams/registry/content/Customers.java","common/src/main/java/cc/thonly/reverie_dreams/entity/ai/goal/work/NPCCustomerGoal.java"]' />
