package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("kitchenware_item_group"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(MIBlocks.COOKING_POT))
            .displayName(Text.translatable("item_group.kitchenware_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(KitchenwareCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(MIBlocks.COOKING_POT);
            itemGroup.add(MIBlocks.CUTTING_BOARD);
            itemGroup.add(MIBlocks.FRYING_PAN);
            itemGroup.add(MIBlocks.GRILL);
            itemGroup.add(MIBlocks.STEAMER);

            itemGroup.add(MIBlocks.MYSTIA_COOKING_POT);
            itemGroup.add(MIBlocks.MYSTIA_CUTTING_BOARD);
            itemGroup.add(MIBlocks.MYSTIA_FRYING_PAN);
            itemGroup.add(MIBlocks.MYSTIA_GRILL);
            itemGroup.add(MIBlocks.MYSTIA_STEAMER);

            itemGroup.add(MIBlocks.SUPER_COOKING_POT);
            itemGroup.add(MIBlocks.SUPER_CUTTING_BOARD);
            itemGroup.add(MIBlocks.SUPER_FRYING_PAN);
            itemGroup.add(MIBlocks.SUPER_GRILL);
            itemGroup.add(MIBlocks.SUPER_STEAMER);

            itemGroup.add(MIBlocks.EXTREME_COOKING_POT);
            itemGroup.add(MIBlocks.EXTREME_CUTTING_BOARD);
            itemGroup.add(MIBlocks.EXTREME_FRYING_PAN);
            itemGroup.add(MIBlocks.EXTREME_GRILL);
            itemGroup.add(MIBlocks.EXTREME_STEAMER);

            itemGroup.add(MIBlocks.ITEM_DISPLAY);
        });
        ItemGroupContentHelper.registerGroup(KitchenwareCreativeTab.ITEM_GROUP_KEY, KitchenwareCreativeTab.ITEM_GROUP);

    }
}
