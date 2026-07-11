package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class SpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("11_item_group_spawn_egg"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(SpawnEggCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.SPAWN_EGG.asItem()))
                .title(Component.translatable("item_group.touhou.spawn_egg"))
                .displayItems((parameters, output) -> {
                    for (var item : RDEntityTypes.getSpawnEggItemView()) {
                        output.accept(item);
                    }
                })
        );

    }
}
