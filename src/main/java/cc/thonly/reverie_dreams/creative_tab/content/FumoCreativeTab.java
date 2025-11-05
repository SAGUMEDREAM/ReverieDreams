package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FumoCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_fumo"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.FUMO_ICON))
            .title(Component.translatable("item_group.touhou.fumo"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(FumoCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(RDItems.FUMO_LICENSE);
            for (FumoType fumo : FumoTypes.getView()) {
                itemGroup.accept(fumo.item());
            }
        });
        ItemGroupContentHelper.registerGroup(FumoCreativeTab.ITEM_GROUP_KEY, FumoCreativeTab.ITEM_GROUP);
    }
}
