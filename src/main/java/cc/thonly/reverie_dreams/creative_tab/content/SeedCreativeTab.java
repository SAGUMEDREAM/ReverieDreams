package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class SeedCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("seeds_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(SeedCreativeTab::getSeedItemIcon)
            .title(Component.translatable("item_group.seed_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(SeedCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
                CropBlockCreator.Instance instance = view.getValue();
                Item seed = instance.getSeed();
                itemGroup.accept(seed);
            }
            itemGroup.accept(RDWoodBlocks.UDUMBARA_FLOWER);
            itemGroup.accept(RDWoodBlocks.TREMELLA);
            WoodCreator.INSTANCES.forEach((instance) -> itemGroup.accept(instance.sapling()));

        });
        ItemGroupContentHelper.registerGroup(SeedCreativeTab.ITEM_GROUP_KEY, SeedCreativeTab.ITEM_GROUP);

    }

    public static ItemStack getSeedItemIcon() {
        for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            CropBlockCreator.Instance instance = view.getValue();
            return new ItemStack(instance.getSeed());
        }
        return new ItemStack(Items.WHEAT_SEEDS);
    }
}
