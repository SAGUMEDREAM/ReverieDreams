package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FumoCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Touhou.id("item_group_fumo"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(ModItems.FUMO_ICON))
            .title(Component.translatable("item_group.touhou.fumo"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(FumoCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(ModItems.FUMO_LICENSE);
            for (Fumo fumo : Fumos.getView()) {
                itemGroup.accept(fumo.item());
            }
        });
        ItemGroupContentHelper.registerGroup(FumoCreativeTab.ITEM_GROUP_KEY, FumoCreativeTab.ITEM_GROUP);
    }
}
