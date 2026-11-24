# Workstations and decorations

| Block | Purpose |
|---|---|
| Gensokyo Altar | Special recipes with eight pillars; [construction](./altar) |
| Danmaku Crafting Table | Dye, core, Power, Point and shape → danmaku |
| Strengthening Table | Adjust trajectory, speed, damage or count; [guide](../items/danmaku) |
| Five kitchenware types | Select dishes from loaded ingredients; [cooking](./cooking) |
| Brewing Barrel | Load, start, wait and bottle; [brewing](./brewing) |
| Night-sparrow bell and menu book | Bind, choose menu entries and open/close a [restaurant](./restaurant) |
| Fumo, furniture and building blocks | Decorations or their specific interactions; inspect individual entries |

The block catalog also includes placed dishes and crops with no standalone item. A model filename is never treated as an item ID. Similar-looking wood species, colors and shapes can have distinct recipes and structure requirements.


## Dishes, seats and storage

Empty-hand right-click a **chair** to sit; sneak-right-click can eject its passenger. Occupied seats cannot take another passenger. Leave paths to restaurant seats.

Use a held item on an empty **plate** to place one. Normal interaction with a filled plate ejects its contents; sneak-use can eat valid food when the player meets its consumption conditions. Food remainders follow the real consumption rules.

A **cupboard** opens paged storage for items with food/beverage properties or supported food, beverage and ingredient tags. It is not unrestricted general storage. Silver/wooden boxes use their own storage interfaces; NPC logistics compatibility depends on supported container behavior.

Tea sets, screens, scrolls, curtains, tatami and lanterns are decorative content. A tea-set model is not a brewing station or an unlimited drink source.

## Ice-making machine

Use a **water bucket** on an idle machine with no remaining output. It returns an empty bucket. At standard efficiency it takes **8 seconds** and produces **8 randomly selected entries** from the ice-making output tag. Collect the batch before starting another; normal use collects one output at a time.

A dispenser facing the machine can use water buckets to start it or a pickaxe to extract one output while using durability. Outputs are dropped at the machine's designated side. The output tag determines candidates; it is not a promise of eight identical ice blocks.

## Cash Box and Music

Use a **Cash Box** (donation box, `cash_box`) while holding copper/silver/gold coins to offer them for faith, at most one successful offering per player per game day. Success randomly consumes 1–3 held coins. Without held coins, the interaction opens coin storage. This is different from depositing cheque value.

The **Music Box** reads NBS files in `config/reverie_dreams/nota`. Sneak-right-click to select a track, normal right-click to switch, and power it with redstone to play. Ask the server owner to provide valid .nbs files; MP3 files and ordinary discs are not NBS inputs. NPC performance uses the same local music source.

## Redstone and minecarts

| Block | Actual operation |
|---|---|
| Rail controller | Stop carts near its center without input; redstone releases them. A comparator reads cart presence |
| Signal rail | Configure its identifier with a renamed name tag and configure the cart identifier; matching carts trigger it |
| Signal delayer | Right-click for a positive tick delay. Sustained input activates output after the delay; removing input resets it |
| Remote server/client | Right-click both and enter matching nonempty signal names and tokens. The server reads redstone and matching clients output it; keep facilities loaded |
| Speaker | Normal right-click edits text; sneak-right-click toggles bell sound. A rising redstone input broadcasts to nearby players |

Signal identifiers can match exactly or by one nonempty identifier containing the other, so avoid unintended overlapping station names. Remote pairing requires the token as well as the signal name.

<BlockCatalog />

<SourceNote :files='["common/src/main/java/cc/thonly/reverie_dreams/registry/content/block/RDBlocks.java","common/src/main/java/cc/thonly/reverie_dreams/creative_tab/content/DecorationCreativeTab.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/ChairBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/PlateBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/kitchen/IceMakingMachine.java","common/src/main/java/cc/thonly/reverie_dreams/block/entity/IceMakingMachineBlockEntity.java","common/src/main/java/cc/thonly/reverie_dreams/gui/block/CupboardGui.java","common/src/main/java/cc/thonly/reverie_dreams/block/props/CashBoxBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/props/MusicBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RailControllerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SignalRailBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SignalDelayerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RemoteServerBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/RemoteClientBlock.java","common/src/main/java/cc/thonly/reverie_dreams/block/redstone/SpeakerBlock.java"]' />
