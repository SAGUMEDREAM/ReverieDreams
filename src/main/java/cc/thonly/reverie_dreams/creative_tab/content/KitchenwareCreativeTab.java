package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("kitchenware_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(KitchenBlocks.COOKING_POT))
            .title(Component.translatable("item_group.kitchenware_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(KitchenwareCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(KitchenBlocks.COOKING_POT);
            itemGroup.accept(KitchenBlocks.CUTTING_BOARD);
            itemGroup.accept(KitchenBlocks.FRYING_PAN);
            itemGroup.accept(KitchenBlocks.GRILL);
            itemGroup.accept(KitchenBlocks.STEAMER);

            itemGroup.accept(KitchenBlocks.MYSTIA_COOKING_POT);
            itemGroup.accept(KitchenBlocks.MYSTIA_CUTTING_BOARD);
            itemGroup.accept(KitchenBlocks.MYSTIA_FRYING_PAN);
            itemGroup.accept(KitchenBlocks.MYSTIA_GRILL);
            itemGroup.accept(KitchenBlocks.MYSTIA_STEAMER);

            itemGroup.accept(KitchenBlocks.SUPER_COOKING_POT);
            itemGroup.accept(KitchenBlocks.SUPER_CUTTING_BOARD);
            itemGroup.accept(KitchenBlocks.SUPER_FRYING_PAN);
            itemGroup.accept(KitchenBlocks.SUPER_GRILL);
            itemGroup.accept(KitchenBlocks.SUPER_STEAMER);

            itemGroup.accept(KitchenBlocks.EXTREME_COOKING_POT);
            itemGroup.accept(KitchenBlocks.EXTREME_CUTTING_BOARD);
            itemGroup.accept(KitchenBlocks.EXTREME_FRYING_PAN);
            itemGroup.accept(KitchenBlocks.EXTREME_GRILL);
            itemGroup.accept(KitchenBlocks.EXTREME_STEAMER);

            itemGroup.accept(KitchenBlocks.NUKE_COOKING_POT);
            itemGroup.accept(KitchenBlocks.NUKE_CUTTING_BOARD);
            itemGroup.accept(KitchenBlocks.NUKE_FRYING_PAN);
            itemGroup.accept(KitchenBlocks.NUKE_GRILL);
            itemGroup.accept(KitchenBlocks.NUKE_STEAMER);

            itemGroup.accept(RDBlocks.ITEM_DISPLAY);
        });
        ItemGroupContentHelper.registerGroup(KitchenwareCreativeTab.ITEM_GROUP_KEY, KitchenwareCreativeTab.ITEM_GROUP);

    }
}
