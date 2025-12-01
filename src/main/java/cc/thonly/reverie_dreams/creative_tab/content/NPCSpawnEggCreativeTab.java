package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class NPCSpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_role_spawn_egg"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.SPAWN_EGG))
            .displayItems((parameters, output) -> {
                output.accept(Items.CAKE);
                output.accept(RDItems.OWNER_STICK);
                Collection<NPCRole> roles = RegistryHandlers.NPC_ROLE.values();
                for (NPCRole role : roles) {
                    Item egg = role.getEgg();
                    output.accept(egg.getDefaultInstance());
                }
            })
            .title(Component.translatable("item_group.touhou.role.spawn_egg"))
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(NPCSpawnEggCreativeTab.ITEM_GROUP_KEY, NPCSpawnEggCreativeTab.ITEM_GROUP);
    }
}
