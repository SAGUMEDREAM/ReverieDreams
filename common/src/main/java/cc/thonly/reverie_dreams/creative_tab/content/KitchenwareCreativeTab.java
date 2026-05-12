package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class KitchenwareCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("06_kitchenware_item_group"));

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {
        ItemGroupContentHelper.registerGroup(registrar, KitchenwareCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(KitchenBlocks.COOKING_POT))
                .title(Component.translatable("item_group.kitchenware_item_group"))
                .displayItems((parameters, output) -> {
                    output.accept(KitchenBlocks.COOKING_POT);
                    output.accept(KitchenBlocks.CUTTING_BOARD);
                    output.accept(KitchenBlocks.FRYING_PAN);
                    output.accept(KitchenBlocks.GRILL);
                    output.accept(KitchenBlocks.STEAMER);

                    output.accept(KitchenBlocks.MYSTIA_COOKING_POT);
                    output.accept(KitchenBlocks.MYSTIA_CUTTING_BOARD);
                    output.accept(KitchenBlocks.MYSTIA_FRYING_PAN);
                    output.accept(KitchenBlocks.MYSTIA_GRILL);
                    output.accept(KitchenBlocks.MYSTIA_STEAMER);

                    output.accept(KitchenBlocks.SUPER_COOKING_POT);
                    output.accept(KitchenBlocks.SUPER_CUTTING_BOARD);
                    output.accept(KitchenBlocks.SUPER_FRYING_PAN);
                    output.accept(KitchenBlocks.SUPER_GRILL);
                    output.accept(KitchenBlocks.SUPER_STEAMER);

                    output.accept(KitchenBlocks.EXTREME_COOKING_POT);
                    output.accept(KitchenBlocks.EXTREME_CUTTING_BOARD);
                    output.accept(KitchenBlocks.EXTREME_FRYING_PAN);
                    output.accept(KitchenBlocks.EXTREME_GRILL);
                    output.accept(KitchenBlocks.EXTREME_STEAMER);

                    output.accept(KitchenBlocks.NUKE_COOKING_POT);
                    output.accept(KitchenBlocks.NUKE_CUTTING_BOARD);
                    output.accept(KitchenBlocks.NUKE_FRYING_PAN);
                    output.accept(KitchenBlocks.NUKE_GRILL);
                    output.accept(KitchenBlocks.NUKE_STEAMER);

                    output.accept(RDBlocks.FOOD_DISPLAY);
                    output.accept(RDItems.FAST_RECIPE_BOOK);
                })
        );

    }
}
