package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.Collection;

public class EndLoaderInit implements ModInitializer {
    public static final RegistryKey<ItemGroup> ROLE_SPAWN_EGG_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_role_spawn_egg"));
    public static final RegistryKey<ItemGroup> SPAWN_EGG_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_spawn_egg"));
    
    public static final ItemGroup ITEM_GROUP_SPAWN_EGG = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.spawn_egg"))
            .build();
    public static final ItemGroup ITEM_GROUP_NPC_SPAWN_EGG = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.role.spawn_egg"))
            .build();

    @Override
    public void onInitialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(SPAWN_EGG_ITEM_GROUP_KEY, ITEM_GROUP_SPAWN_EGG);
        PolymerItemGroupUtils.registerPolymerItemGroup(ROLE_SPAWN_EGG_ITEM_GROUP_KEY, ITEM_GROUP_NPC_SPAWN_EGG);
        ItemGroupEvents.modifyEntriesEvent(SPAWN_EGG_ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : ModEntities.getSpawnEggItemView()) {
                itemGroup.add(item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ROLE_SPAWN_EGG_ITEM_GROUP_KEY).register(itemGroup -> {
            Collection<NPCRole> roles = RegistryManager.NPC_ROLE.values();
            for (NPCRole role : roles) {
                Item egg = role.getEgg();
                itemGroup.add(egg.getDefaultStack());
            }
        });
    }
}
