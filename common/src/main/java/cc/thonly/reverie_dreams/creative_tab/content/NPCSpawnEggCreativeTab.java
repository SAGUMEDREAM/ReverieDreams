package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;

public class NPCSpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("12_item_group_role_spawn_egg"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(NPCSpawnEggCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.SPAWN_EGG.asItem()))
                .displayItems((parameters, output) -> {
                    output.accept(RDItems.OWNER_STICK);
                    BuiltInRegistryProviders.NPC_ROLE_TYPE.values().stream()
                            .sorted(Comparator.comparingInt(role -> TouhouCreativeOrder.character(role.getId())))
                            .forEach(role -> output.accept(role.getEgg().createStack()));
                    output.accept(Items.CAKE);
                })
                .title(Component.translatable("item_group.touhou.role.spawn_egg")));
    }
}
