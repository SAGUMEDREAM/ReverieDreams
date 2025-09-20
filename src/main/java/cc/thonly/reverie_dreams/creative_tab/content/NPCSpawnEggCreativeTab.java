package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.Collection;

public class NPCSpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_role_spawn_egg"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(ModItems.SPAWN_EGG))
            .displayName(Text.translatable("item_group.touhou.role.spawn_egg"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(NPCSpawnEggCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            Collection<NPCRole> roles = RegistryManager.NPC_ROLE.values();
            for (NPCRole role : roles) {
                Item egg = role.getEgg();
                itemGroup.add(egg.getDefaultStack());
            }
        });
        ItemGroupContentHelper.registerGroup(NPCSpawnEggCreativeTab.ITEM_GROUP_KEY, NPCSpawnEggCreativeTab.ITEM_GROUP);
    }
}
