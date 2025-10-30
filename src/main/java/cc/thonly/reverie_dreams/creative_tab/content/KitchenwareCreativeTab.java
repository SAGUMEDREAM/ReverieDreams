package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, MystiasIzakaya.id("kitchenware_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(MIBlocks.COOKING_POT))
            .title(Component.translatable("item_group.kitchenware_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(KitchenwareCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(MIBlocks.COOKING_POT);
            itemGroup.accept(MIBlocks.CUTTING_BOARD);
            itemGroup.accept(MIBlocks.FRYING_PAN);
            itemGroup.accept(MIBlocks.GRILL);
            itemGroup.accept(MIBlocks.STEAMER);

            itemGroup.accept(MIBlocks.MYSTIA_COOKING_POT);
            itemGroup.accept(MIBlocks.MYSTIA_CUTTING_BOARD);
            itemGroup.accept(MIBlocks.MYSTIA_FRYING_PAN);
            itemGroup.accept(MIBlocks.MYSTIA_GRILL);
            itemGroup.accept(MIBlocks.MYSTIA_STEAMER);

            itemGroup.accept(MIBlocks.SUPER_COOKING_POT);
            itemGroup.accept(MIBlocks.SUPER_CUTTING_BOARD);
            itemGroup.accept(MIBlocks.SUPER_FRYING_PAN);
            itemGroup.accept(MIBlocks.SUPER_GRILL);
            itemGroup.accept(MIBlocks.SUPER_STEAMER);

            itemGroup.accept(MIBlocks.EXTREME_COOKING_POT);
            itemGroup.accept(MIBlocks.EXTREME_CUTTING_BOARD);
            itemGroup.accept(MIBlocks.EXTREME_FRYING_PAN);
            itemGroup.accept(MIBlocks.EXTREME_GRILL);
            itemGroup.accept(MIBlocks.EXTREME_STEAMER);

            itemGroup.accept(MIBlocks.NUKE_COOKING_POT);
            itemGroup.accept(MIBlocks.NUKE_CUTTING_BOARD);
            itemGroup.accept(MIBlocks.NUKE_FRYING_PAN);
            itemGroup.accept(MIBlocks.NUKE_GRILL);
            itemGroup.accept(MIBlocks.NUKE_STEAMER);

            itemGroup.accept(MIBlocks.ITEM_DISPLAY);
        });
        ItemGroupContentHelper.registerGroup(KitchenwareCreativeTab.ITEM_GROUP_KEY, KitchenwareCreativeTab.ITEM_GROUP);

    }
}
