# NPC Companions and Tasks

## Summoning, States, and Inventory

Use a valid **Role Card** on a block, select the character in the dialog, and confirm the summon text. Grouped cards provide only their designated character; servers with free selection allow picking from all registered groups. Blank cards and group-bound cards represent distinct data.

Sneak-right-click empty-handed on an NPC you own to access the inventory menu for equipment, states, and work modes. Unclaimed NPCs can be befriended with #reverie_dreams:role_tame_food items (40% success chance per attempt). Set the primary state to **Work** before choosing a work mode; merely switching work modes does not activate working behavior. Follow, roam, hold position, sneak, and sit handle daily movement control.

Work centers on a recorded coordinate origin (default radius: 8 horizontal, 8 vertical blocks, configurable by server). Freight uses an independent **18-slot** storage partition; hand and armor slots are not dumping bins. Default automated logistics offload freight when occupancy reaches 14 slots, provided an accessible container is within range.

## Work Mode Requirements

| Mode | Player Preparations | Operating Essentials |
|---|---|---|
| Combat | Weapons, armor, firearm magazines | Can patrol; target priority obeys server config |
| Farming | Mature crops, matching seeds, freight space | Replants only the harvested crop type |
| Breeding | Species-appropriate breeding food | Retains 2 food reserve; 32 animal regional cap |
| Shearing | Functional shears, reachable sheep | Checks wool readiness and freight space |
| Smelting | Raw materials, fuel, furnace, output chest | Verifies recipes, fuel, and unblocked outputs |
| Chest sorting | Chests/barrels, sample items or item frames | See sorting rules below; reserves tools and food |
| Music | Local NBS tracks and playlists | Configurable volume, range, loop mode |
| Honey harvesting | Full hives, shears or bottles, smoke | Selects honey, honeycomb, or auto |
| Fishing | Fishing rod, water body, standable ledge | Leaves open freight slots for catches |
| Customer | Active tavern, menu book, seats | See [Restaurant Guide](../blocks/restaurant) |

Hand-crank integrations appear only when compatible mods are present. When an NPC pauses work, inspect status notes and supplies rather than toggling modes repeatedly.

## Sorting Chests with Item Frames

Mount item frames holding sample items onto storage containers. **Every two rotations** steps to the next mode; eight rotational positions map into four modes:

| Rotations | Mode | Rule |
|---|---|---|
| 0, 1 | Same Item | Accepts items sharing the exact same item ID |
| 2, 3 | Same Category | Categorizes by allowed tags (e.g., logs, planks, ingots) |
| 4, 5 | Input Chest | NPCs extract unsorted items from here |
| 6, 7 | Overflow Chest | Receives items when other target containers are full |

Mounting a sample defaults to "Same Item". Multiple frames on one container must use the same mode to prevent conflicting logic. Sorting does not force-merge items with differing components. Containers without frames obey existing contents and the "claim empty chests" configuration.

Default sorting reserves 16 food and 1 tool. If containers are full or paths blocked, NPCs pause safely without hauling equipped gear away.

## AI Natural Language Interaction & Persistent Tasks

When the server configures an AI provider with Tool Calls, owners can issue direct world interaction commands via natural language in the NPC chat dialog. All actions are validated on the server thread for ownership, alive status, and 32-block proximity.

### World Interaction Directives

* **Observe (`observe`)**: Reports current coordinates, visible entities within 8 blocks, weather, inventory, and the state of up to 32 nearby farmlands, furnaces, and containers.
* **Follow & Stay (`follow` / `stay`)**: Commands the companion to follow the owner or wait in place.
* **Pathfinding (`move_to`)**: Navigates on foot to coordinates within 16 blocks (does not teleport or load distant chunks, 20-second timeout).
* **Work (`work`)**: Sets current position as the work origin and initiates a specified work mode.

### Persistent Tasks and Count Quotas

Through the skill system (`start_skill`, `task_status`, `cancel_task`), companions execute persistent tasks with verifiable targets:

* **Production Tracking**: Specify `target_item` and `target_count` (1–1024) for `farm`, `fish`, `shear_sheep`, and `harvest_honey`. Only **genuine work production events** where items enter companion inventory increment progress; preexisting items or player handouts do not count.
* **Smelting & Breeding**: `smelt` supports target production limits and tracks individual fuel/smelt batches; `breed_animals` increments only when a baby animal successfully spawns.
* **Owner Presence Safeguards**: If the owner goes offline, switches dimensions, or strays beyond 32 blocks, tasks pause automatically and resume upon return.

### Container Authorization and Logistics

* **Container Authorization (`authorize_container`)**: Owners explicitly grant access to chests or barrels within 6 blocks while the companion is within 16 blocks (up to 8 locations).
* **Item Transfers (`deposit_items` / `withdraw_items`)**: Companions walk to within 3 blocks of authorized containers to execute safe item transfers.

### Character Personas and Injury Safeguards

* **110 Role Personas**: Characters possess individualized conversational responses and natural work preferences (e.g., Youmu prefers gardening, Sakuya prefers sorting, Nitori prefers smelting).
* **Hysteresis Injury Protection**: Companions pause work when health drops below 25% (or 50% for cautious roles) and resume once healed to 40% (or 65% for cautious roles), avoiding rapid toggle loops.

## Archiving and Redeployment

Use a blank **Soul Ofuda** on your companion to archive it into a role archive item; right-click an archive item onto a block to redeploy. Archive items store ownership data; only the owning player can redeploy them.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/entity/npc/NPCLikeInteractionEvents.java","common/src/main/java/cc/thonly/reverie_dreams/registry/content/NPCStates.java","common/src/main/java/cc/thonly/reverie_dreams/registry/content/NPCWorkModes.java","common/src/main/java/cc/thonly/reverie_dreams/inventory/storage/StorageRule.java","common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java","AI_WORLD_INTERACTION.md"]' />
