package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class FumoCreativeTab implements ItemGroupContentHelper {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_fumo"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(ModItems.FUMO_ICON))
            .displayName(Text.translatable("item_group.touhou.fumo"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(FumoCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(ModItems.FUMO_LICENSE);
            for (Fumo fumo : Fumos.getView()) {
                itemGroup.add(fumo.item());
            }
        });
        ItemGroupContentHelper.registerGroup(FumoCreativeTab.ITEM_GROUP_KEY, FumoCreativeTab.ITEM_GROUP);
    }
}
