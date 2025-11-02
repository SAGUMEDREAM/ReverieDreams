package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class RoleCardCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_role_card"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(ModItems.ROLE_CARD))
            .title(Component.translatable("item_group.touhou.role_card"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(RoleCardCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(ModItems.ROLE_CARD);
            RegistryManager.ROLE_CARD.values().forEach(instance -> itemGroup.accept(instance.itemStack()));
        });
        ItemGroupContentHelper.registerGroup(RoleCardCreativeTab.ITEM_GROUP_KEY, RoleCardCreativeTab.ITEM_GROUP);
    }
}
