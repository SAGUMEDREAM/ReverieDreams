package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FumoCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("04_item_group_fumo"));

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {
        ItemGroupContentHelper.registerGroup(registrar, FumoCreativeTab.ITEM_GROUP_KEY, (builder) -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.FUMO_ICON.asItem()))
                .title(Component.translatable("item_group.touhou.fumo"))
                .displayItems((parameters, output) -> {
                    output.accept(RDItems.FUMO_LICENSE);
                    for (FumoType fumo : FumoTypes.getView()) {
                        output.accept(fumo.item());
                    }
                }));
    }
}
