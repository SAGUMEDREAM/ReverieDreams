package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SpawnEggCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("11_item_group_spawn_egg"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(SpawnEggCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.SPAWN_EGG.asItem()))
                .title(Component.translatable("item_group.touhou.spawn_egg"))
                .displayItems((parameters, output) -> {
                    var remaining = RDEntityTypes.getSpawnEggItemView();
                    for (var type : List.of(RDEntityTypes.YOUSEI, RDEntityTypes.MAID_YOUSEI,
                            RDEntityTypes.SUNFLOWER_YOUSEI, RDEntityTypes.ICE_FAIRY,
                            RDEntityTypes.GHOST, RDEntityTypes.YOUKAI_RABBIT, RDEntityTypes.RABBIT_UNIT,
                            RDEntityTypes.ONI, RDEntityTypes.WHITE_WOLF_TENGU, RDEntityTypes.CROW_TENGU,
                            RDEntityTypes.UFO, RDEntityTypes.WATER_ELEMENTAL, RDEntityTypes.FIRE_ELEMENTAL,
                            RDEntityTypes.ICE_ELEMENTAL, RDEntityTypes.HAIRBALL, RDEntityTypes.GOBLIN,
                            RDEntityTypes.MUSHROOM_MONSTER, RDEntityTypes.KILLER_BEE, RDEntityTypes.WILD_PIG,
                            RDEntityTypes.FUMO_SELLER_VILLAGER, RDEntityTypes.TAVERN_VILLAGER,
                            RDEntityTypes.MAGIC_BROOM, RDEntityTypes.WHEEL_CHAIR, RDEntityTypes.SCARECROW)) {
                        var egg = RDEntityTypes.SPAWN_EGG_BIND.get(type);
                        if (remaining.remove(egg)) output.accept(egg);
                    }
                    remaining.forEach(output::accept);
                })
        );

    }
}
