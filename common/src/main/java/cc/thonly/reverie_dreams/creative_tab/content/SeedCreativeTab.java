package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooForestBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;

public class SeedCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("10_seeds_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(SeedCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(SeedCreativeTab::getSeedItemIcon)
                .title(Component.translatable("item_group.seed_item_group"))
                .displayItems((parameters, output) -> getItems().forEach(output::accept))
        );

    }

    /** The planting page and general-page exclusions use the same live registry view. */
    public static Set<Item> getItems() {
        Set<Item> items = new LinkedHashSet<>();
        CropBlockBundle.getViews().forEach(view -> items.add(view.getValue().getSeed().asItem()));
        WoodBundle.INSTANCES.forEach(wood -> items.add(wood.sapling().asItem()));
        cc.thonly.reverie_dreams.registry.content.block.RDGensokyoSaplings.SAPLINGS.values()
                .forEach(sapling -> items.add(sapling.asItem()));
        items.add(RDLandmarkBlocks.PURPLE_CHERRY_SAPLING.asItem());
        items.add(RDBlocks.PADDY_FIELD.asItem());
        items.add(RDWoodBlocks.UDUMBARA_FLOWER.asItem());
        items.add(RDWoodBlocks.TREMELLA.asItem());
        items.add(RDBlocks.BLUE_MUSHROOM.asItem());
        items.add(RDBlocks.PURPLE_MUSHROOM.asItem());
        RDLandmarkBlocks.ALL.forEach(block -> items.add(block.asItem()));
        RDBambooBlocks.ALL.forEach(block -> items.add(block.asItem()));
        RDBambooForestBlocks.ALL.forEach(block -> items.add(block.asItem()));
        return Collections.unmodifiableSet(items);
    }

    public static ItemStack getSeedItemIcon() {
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            return new ItemStack(entry.getSeed().asItem());
        }
        return new ItemStack(Items.WHEAT_SEEDS);
    }
}
