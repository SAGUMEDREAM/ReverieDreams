package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class SeedCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("10_seeds_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(SeedCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(SeedCreativeTab::getSeedItemIcon)
                .title(Component.translatable("item_group.seed_item_group"))
                .displayItems((parameters, output) -> {
                    for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
                        CropBlockBundle.Entry entry = view.getValue();
                        Item seed = entry.getSeed().asItem();
                        output.accept(seed);
                    }
                    output.accept(RDWoodBlocks.UDUMBARA_FLOWER);
                    output.accept(RDWoodBlocks.TREMELLA);
                    WoodBundle.INSTANCES.forEach((instance) -> output.accept(instance.sapling()));
                })
        );

    }

    public static ItemStack getSeedItemIcon() {
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            return new ItemStack(entry.getSeed().asItem());
        }
        return new ItemStack(Items.WHEAT_SEEDS);
    }
}
