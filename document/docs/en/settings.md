# Settings and FAQ

The following represent source-code defaults. Server administrators may customize values; multiplayer sessions adhere to server settings. Configuration files reside in the instance `config/` directory prefixed with `reverie_dreams`. Restart after edits to guarantee all changes take effect.

| Setting Key | Default | Impact |
|---|---|---|
| debugMode | false | Developer diagnostics; leave disabled during standard play |
| maxCustomerTickTime | 1200 ticks (60s) | Order cycle countdown while customers are waiting |
| maxCustomerCooldownTickTime | 400 ticks (20s) | Dining duration after serving, preventing new orders |
| npcCustomerResetTimerPerOrder | false | Whether each served dish resets the order timer |
| npcWorkRange / npcWorkVerticalRange | 8 / 8 | Horizontal/vertical companion working radius |
| npcWorkAutoLogistics | true | Automated restocking from input chests and unloading |
| npcWorkUnloadThreshold | 14 | Freight slot occupancy triggering unload trips |
| npcBreedMaxAnimals / npcBreedFoodReserve | 32 / 2 | Regional breeding cap and reserved animal feed |
| npcHoneyProduct / npcHoneyRequireSmoke | auto / true | Hive harvesting type (honey/comb/auto) and smoke requirements |
| chestSortClaimEmptyChests | true | Allows sorting to claim unassigned empty containers |
| enableYouseiSpawn / enableGhostSpawn | true / false | Natural spawning toggles for Fairies and Ghosts |
| enableGensokyoChunkZstd | false | Optional [Gensokyo Zstd Compression](./world/chunk-compression) |
| aiProxyUrl | "" (empty) | Dedicated proxy URL for AI completions (SOCKS5/HTTP/HTTPS) |

## Order Timers Explained

`maxCustomerTickTime` governs customer impatience while waiting for a dish; when it hits zero, the customer resets and issues a new order. `maxCustomerCooldownTickTime` is the eating period after receiving food, during which no further orders are accepted. Neither represents tavern revisit intervals. Normal server speed is 20 ticks = 1 second.

## Companion Work Troubleshooting

Confirm the primary state is set to "Work" and the work origin is established. Verify appropriate tools, fuel/seeds/food supplies, 18-slot freight clearance, container frame rules, and accessible pathfinding. Tavern work additionally requires an active Mystia's Clock, Menu Book, and registered seating.

## Recipe or Name Divergence

Confirm the Minecraft and mod release versions, then check active datapacks and resource packs. Component-bound templates, role cards, or upgraded items carry distinct attributes; pick matching variants in item details. The Wiki documents built-in assets and does not overwrite custom server scripts.

## AI Configuration and Proxy Support

AI endpoint URLs, models, and API keys are managed in the mod configuration screen. The `aiProxyUrl` setting configures a dedicated proxy for AI requests (e.g., `socks5://127.0.0.1:1080` or `http://127.0.0.1:7890`), defaulting to direct connection when blank. This proxy routes solely AI chat and tool calls, without affecting skin downloads or mod update checks.

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/ReverieDreamsConfiguration.java"]' />
