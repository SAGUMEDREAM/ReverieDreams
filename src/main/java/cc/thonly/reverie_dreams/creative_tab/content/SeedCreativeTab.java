package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.block.PolymerCropCreator;
import cc.thonly.reverie_dreams.block.WoodCreator;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SeedCreativeTab implements ItemGroupContent {

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("seeds_item_group"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(SeedCreativeTab::getSeedItemIcon)
            .displayName(Text.translatable("item_group.seed_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(SeedCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : PolymerCropCreator.getViews()) {
                PolymerCropCreator.Instance instance = view.getValue();
                Item seed = instance.getSeed();
                itemGroup.add(seed);
            }
            itemGroup.add(MIBlocks.UDUMBARA_FLOWER);
            itemGroup.add(MIBlocks.TREMELLA);
            WoodCreator.INSTANCES.forEach((instance) -> itemGroup.add(instance.sapling()));

        });
        ItemGroupContent.registerGroup(SeedCreativeTab.ITEM_GROUP_KEY, SeedCreativeTab.ITEM_GROUP);

    }

    public static ItemStack getSeedItemIcon() {
        for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : PolymerCropCreator.getViews()) {
            PolymerCropCreator.Instance instance = view.getValue();
            return new ItemStack(instance.getSeed());
        }
        return new ItemStack(Items.WHEAT_SEEDS);
    }
}
