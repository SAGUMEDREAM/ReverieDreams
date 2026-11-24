package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoolBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import cc.thonly.reverie_dreams.registry.content.block.RDDeadwoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseHomeBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseLivingBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseShrineBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;

public class DecorationCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("01_item_group_decoration"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDBlocks.CHOCHIN.asItem()))
                .title(Component.translatable("item_group.decoration_item_group"))
                .displayItems((parameters, output) -> getItems().forEach(output::accept)));
    }

    /** Leaves are shared with their existing tabs; other entries also define general-tab exclusions. */
    public static Set<Item> getItems() {
        Set<Item> items = new LinkedHashSet<>();

        // Building materials, grouped by wood/stone family.
        for (WoodBundle wood : WoodBundle.INSTANCES) {
            add(items, wood.log());
            if (wood == RDWoodBlocks.SPIRITUAL_BUNDLE) add(items, RDWoodBlocks.BLESSED_SPIRITUAL_LOG);
            add(items, wood.wood(), wood.strippedLog(), wood.strippedWood(),
                    wood.planks(), wood.stairs(), wood.slab(), wood.fence(), wood.fenceGate(),
                    wood.door(), wood.trapdoor(), wood.button(), wood.pressurePlate());
        }
        items.addAll(getLeaves());
        for (DecorativeBlockBundle stone : DecorativeBlockBundle.INSTANCES) {
            stone.stream().forEach(block -> items.add(block.asItem()));
        }
        for (RDWoolBlocks.Family wool : RDWoolBlocks.FAMILIES) {
            add(items, wool.slab(), wool.stairs());
        }
        add(items, RDBlocks.SHIKKUI_BLOCK, RDBlocks.SHIKKUI_STAIRS, RDBlocks.SHIKKUI_SLAB, RDBlocks.SHIKKUI_WALL,
                RDBlocks.KAWARA_TILES, RDBlocks.KAWARA_TILES_STAIRS, RDBlocks.KAWARA_TILES_SLAB,
                RDBlocks.TATAMI_BLOCK, RDBlocks.TATAMI, RDBlocks.TATAMI_CORNER, RDBlocks.ENGAWA);

        add(items, RDJapaneseLivingBlocks.TIMBER_WHITE_WALL,
                RDJapaneseLivingBlocks.TIMBER_WHITE_WALL_HORIZONTAL,
                RDJapaneseLivingBlocks.TIMBER_WHITE_WALL_CROSS);

        // Doors, windows and room dividers; keep the noren colors together.
        add(items, RDBlocks.SHOJI_PANE, RDBlocks.FUSUMA_DOOR, RDJapaneseHomeBlocks.SHOJI_DOOR,
                RDBlocks.RANMA, RDBlocks.MUSHIKO_MADO,
                RDBlocks.SUDARE, RDBlocks.NOREN_INDIGO, RDBlocks.NOREN_WHITE, RDBlocks.NOREN_RED,
                RDBlocks.NOREN_BLACK, RDBlocks.NOREN_BROWN, RDBlocks.NOREN_GREEN, RDBlocks.BYOBU);

        // Furniture and furnishings. The original cushion remains available beside all 16 dyed ones.
        add(items, RDBlocks.CHAIR, RDBlocks.TABLE, RDBlocks.CHABUDAI, RDBlocks.TEA_SET, RDBlocks.ZABUTON);
        RDBlocks.ZABUTONS.values().forEach(block -> items.add(block.asItem()));
        add(items, RDBlocks.XIANGQI_BOARD, RDBlocks.CHESS_BOARD, RDBlocks.GO_BOARD);
        add(items, RDBlocks.CUPBOARD, RDBlocks.WOODEN_BOX.chestBlock(), RDBlocks.SILVER_CHEST_BLOCK.chestBlock(), RDBlocks.WHEEL_CHAIR,
                RDBlocks.KAKEJIKU, RDBlocks.CASH_BOX_BLOCK, RDBlocks.MUSIC_BLOCK, RDBlocks.RECORD_PLAYER, RDBlocks.ANTI_COLLISION_BARREL);

        add(items, RDJapaneseLivingBlocks.OSHIIRE,
                RDJapaneseLivingBlocks.TANSU,
                RDJapaneseLivingBlocks.FUTON,
                RDJapaneseLivingBlocks.FOLDED_FUTON,
                RDJapaneseLivingBlocks.IRORI,
                RDJapaneseLivingBlocks.JIZAIKAGI,
                RDJapaneseLivingBlocks.SHRINE_BELL,
                RDJapaneseLivingBlocks.BELL_ROPE,
                RDJapaneseLivingBlocks.WIND_CHIME);
        add(items, RDJapaneseShrineBlocks.SHIMENAWA, RDJapaneseShrineBlocks.GOHEI,
                RDJapaneseShrineBlocks.SAISEN_BAKO, RDJapaneseShrineBlocks.OMIKUJI_RACK,
                RDJapaneseShrineBlocks.INCENSE_BURNER, RDJapaneseShrineBlocks.JIZO_STATUE,
                RDJapaneseShrineBlocks.YUKIMI_LANTERN, RDJapaneseShrineBlocks.HOKORA);
        add(items, RDJapaneseHomeBlocks.KOTATSU, RDJapaneseHomeBlocks.HIBACHI,
                RDJapaneseHomeBlocks.KAMIDANA, RDJapaneseHomeBlocks.BUTSUDAN);

        RDJapaneseSet2Blocks.ENTRIES.forEach((kind, block) -> {
            add(items, block);
            switch (kind) {
                case EMA_RACK -> add(items, RDWorkshopItems.EMA);
                case OFUDA_RACK -> add(items, RDWorkshopItems.OFUDA);
                case OMAMORI_CABINET -> add(items, RDWorkshopItems.OMAMORI);
                default -> { }
            }
        });

        // Lighting and garden decorations.
        add(items, RDBlocks.CHOCHIN, RDBlocks.ANDON, RDBlocks.STONE_LANTERN,
                RDBlocks.RAKED_GRAVEL, RDBlocks.STEPPING_STONE, RDBlocks.TSUKUBAI,
                RDBlocks.YOTSUME_GAKI, RDBlocks.BAMBOO_FENCE);
        RDDeadwoodBlocks.SPLINTERED_LOGS.values().forEach(block -> items.add(block.asItem()));
        return Collections.unmodifiableSet(items);
    }

    static Set<Item> getLeaves() {
        Set<Item> leaves = new LinkedHashSet<>();
        for (WoodBundle wood : WoodBundle.INSTANCES) {
            add(leaves, wood.leaves());
            if (wood.fruitLeaves() != null) add(leaves, wood.fruitLeaves());
        }
        add(leaves, RDLandmarkBlocks.PURPLE_CHERRY_LEAVES);
        return leaves;
    }

    private static void add(Set<Item> items, ItemLike... entries) {
        for (ItemLike entry : entries) items.add(entry.asItem());
    }
}
