package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("06_kitchenware_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(KitchenwareCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDKitchenBlocks.COOKING_POT))
                .title(Component.translatable("item_group.kitchenware_item_group"))
                .displayItems((parameters, output) -> getItems().forEach(output::accept))
        );
    }

    /** Keep display order and the general tab's exclusions in one place. */
    public static Set<Item> getItems() {
        List<ItemLike> entries = List.of(
                RDItems.FAST_RECIPE_BOOK,
                RDKitchenBlocks.COOKING_POT, RDKitchenBlocks.MYSTIA_COOKING_POT, RDKitchenBlocks.SUPER_COOKING_POT,
                RDKitchenBlocks.EXTREME_COOKING_POT, RDKitchenBlocks.NUKE_COOKING_POT,
                RDKitchenBlocks.CUTTING_BOARD, RDKitchenBlocks.MYSTIA_CUTTING_BOARD, RDKitchenBlocks.SUPER_CUTTING_BOARD,
                RDKitchenBlocks.EXTREME_CUTTING_BOARD, RDKitchenBlocks.NUKE_CUTTING_BOARD,
                RDKitchenBlocks.FRYING_PAN, RDKitchenBlocks.MYSTIA_FRYING_PAN, RDKitchenBlocks.SUPER_FRYING_PAN,
                RDKitchenBlocks.EXTREME_FRYING_PAN, RDKitchenBlocks.NUKE_FRYING_PAN,
                RDKitchenBlocks.GRILL, RDKitchenBlocks.MYSTIA_GRILL, RDKitchenBlocks.SUPER_GRILL,
                RDKitchenBlocks.EXTREME_GRILL, RDKitchenBlocks.NUKE_GRILL,
                RDKitchenBlocks.STEAMER, RDKitchenBlocks.MYSTIA_STEAMER, RDKitchenBlocks.SUPER_STEAMER,
                RDKitchenBlocks.EXTREME_STEAMER, RDKitchenBlocks.NUKE_STEAMER,
                RDBlocks.MYSTIA_BELL, RDBlocks.MENU_BOOK, RDBlocks.PLATE,
                RDBlocks.CHAIR, RDBlocks.TABLE, RDBlocks.CHABUDAI, RDBlocks.TEA_SET,
                RDBlocks.CUPBOARD, RDBlocks.BREWING_BARREL, RDBlocks.ICE_MAKING_MACHINE);
        Set<Item> items = new LinkedHashSet<>();
        entries.forEach(entry -> items.add(entry.asItem()));
        return Collections.unmodifiableSet(items);
    }
}
