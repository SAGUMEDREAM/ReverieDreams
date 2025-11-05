package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_spawn_egg"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.SPAWN_EGG))
            .title(Component.translatable("item_group.touhou.spawn_egg"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(SpawnEggCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : RDEntityTypes.getSpawnEggItemView()) {
                itemGroup.accept(item);
            }
        });
        ItemGroupContentHelper.registerGroup(SpawnEggCreativeTab.ITEM_GROUP_KEY, SpawnEggCreativeTab.ITEM_GROUP);

    }
}
