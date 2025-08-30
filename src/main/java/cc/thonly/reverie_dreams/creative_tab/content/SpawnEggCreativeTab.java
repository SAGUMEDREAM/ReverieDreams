package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class SpawnEggCreativeTab implements ItemGroupContent {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_spawn_egg"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.spawn_egg"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(SpawnEggCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : ModEntities.getSpawnEggItemView()) {
                itemGroup.add(item);
            }
        });
        ItemGroupContent.registerGroup(SpawnEggCreativeTab.ITEM_GROUP_KEY, SpawnEggCreativeTab.ITEM_GROUP);

    }
}
