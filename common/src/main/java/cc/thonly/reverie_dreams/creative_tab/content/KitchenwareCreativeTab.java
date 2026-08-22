package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("06_kitchenware_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(KitchenwareCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDKitchenBlocks.COOKING_POT))
                .title(Component.translatable("item_group.kitchenware_item_group"))
                .displayItems((parameters, output) -> {
                    output.accept(RDKitchenBlocks.COOKING_POT);
                    output.accept(RDKitchenBlocks.CUTTING_BOARD);
                    output.accept(RDKitchenBlocks.FRYING_PAN);
                    output.accept(RDKitchenBlocks.GRILL);
                    output.accept(RDKitchenBlocks.STEAMER);

                    output.accept(RDKitchenBlocks.MYSTIA_COOKING_POT);
                    output.accept(RDKitchenBlocks.MYSTIA_CUTTING_BOARD);
                    output.accept(RDKitchenBlocks.MYSTIA_FRYING_PAN);
                    output.accept(RDKitchenBlocks.MYSTIA_GRILL);
                    output.accept(RDKitchenBlocks.MYSTIA_STEAMER);

                    output.accept(RDKitchenBlocks.SUPER_COOKING_POT);
                    output.accept(RDKitchenBlocks.SUPER_CUTTING_BOARD);
                    output.accept(RDKitchenBlocks.SUPER_FRYING_PAN);
                    output.accept(RDKitchenBlocks.SUPER_GRILL);
                    output.accept(RDKitchenBlocks.SUPER_STEAMER);

                    output.accept(RDKitchenBlocks.EXTREME_COOKING_POT);
                    output.accept(RDKitchenBlocks.EXTREME_CUTTING_BOARD);
                    output.accept(RDKitchenBlocks.EXTREME_FRYING_PAN);
                    output.accept(RDKitchenBlocks.EXTREME_GRILL);
                    output.accept(RDKitchenBlocks.EXTREME_STEAMER);

                    output.accept(RDKitchenBlocks.NUKE_COOKING_POT);
                    output.accept(RDKitchenBlocks.NUKE_CUTTING_BOARD);
                    output.accept(RDKitchenBlocks.NUKE_FRYING_PAN);
                    output.accept(RDKitchenBlocks.NUKE_GRILL);
                    output.accept(RDKitchenBlocks.NUKE_STEAMER);

                    output.accept(RDBlocks.PLATE);
                    output.accept(RDBlocks.CHAIR);
                    output.accept(RDBlocks.TABLE);
                    output.accept(RDBlocks.BREWING_BARREL);
                    output.accept(RDBlocks.CUPBOARD);
                    output.accept(RDItems.FAST_RECIPE_BOOK);
                })
        );

    }
}
