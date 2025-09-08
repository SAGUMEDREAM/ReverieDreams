package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class RoleCardCreativeTab implements ItemGroupContent {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_role_card"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(() -> new ItemStack(ModItems.ROLE_CARD))
            .displayName(Text.translatable("item_group.touhou.role_card"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(RoleCardCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(ModItems.ROLE_CARD);
            RegistryManager.ROLE_CARD.values().forEach(instance -> itemGroup.add(instance.itemStack()));
        });
        ItemGroupContent.registerGroup(RoleCardCreativeTab.ITEM_GROUP_KEY, RoleCardCreativeTab.ITEM_GROUP);
    }
}
