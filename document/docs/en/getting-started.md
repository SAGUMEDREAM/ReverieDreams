# Getting started

This guide targets **Minecraft 26.1.2, ReverieDreams 0.6.9+26.1.2 and Java 25**. Names come from the game language files; catalog entries and recipes come from the registered content of this version. Server datapacks and settings can change these rules.

## Install the matching version

Use the Fabric or NeoForge build that matches your instance. Install the dependencies declared by that build: Fabric needs Fabric API and Architectury; follow the NeoForge dependency requirements for Architectury, GeckoLib and any other required libraries. Do not install both loader builds into one instance.

Ordinary modded play needs matching client/server content. The optional Fabric Polymer addon is a separate deployment option using a server resource pack for vanilla clients; it is not a general promise that a normal mod installation needs no client mods.

## Choose a first goal

1. Establish vanilla tools, food and shelter. Keep Point, Power, fairy materials, silver and colored orbs you obtain.
2. Search the [item catalog](./catalog), then open an item for recipes, uses and obtaining records. Counts, ingredient tags and component variants matter.
3. Build a [Gensokyo Altar](./blocks/altar) for special items and role cards, or follow [shape drawing → danmaku crafting → strengthening](./items/danmaku).
4. For a restaurant, start with [farming](./blocks/crops), [cooking](./blocks/cooking) and [brewing](./blocks/brewing), then configure the [bell and menu book](./blocks/restaurant).
5. Summon [NPC companions](./entities/npc-companion), equip them and prepare a reachable work area before assigning jobs.
6. Prepare supplies and a return route before [dimension exploration](./world/gensokyo).

## Read the catalog correctly

Select a variant to distinguish component-based role cards, templates and colors. Drag a detail model to rotate it; supported dishes have a separate placed-model view. Loot entries retain conditions and random rules. An empty crafting list does not mean creative-only. Internal display/debug entries are labeled and are not survival progression goals.

<SourceNote :files='["gradle.properties","fabric/src/main/resources/fabric.mod.json","neoforge/src/main/resources/META-INF/neoforge.mods.toml"]' />
