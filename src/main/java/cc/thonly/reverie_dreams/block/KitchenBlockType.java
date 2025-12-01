package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class KitchenBlockType {
    public static final Map<Block, KitchenRecipeType.KitchenType> BLOCK_2_KITCHEN_TYPE = new Object2ObjectOpenHashMap<>();
    public static final Map<KitchenRecipeType.KitchenType, Block> KITCHEN_TYPE_2_BLOCK = new Object2ObjectOpenHashMap<>();

    public static void init() {
        registerRecipeType(KitchenBlocks.COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(KitchenBlocks.CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(KitchenBlocks.FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(KitchenBlocks.GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(KitchenBlocks.STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(KitchenBlocks.MYSTIA_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(KitchenBlocks.MYSTIA_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(KitchenBlocks.MYSTIA_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(KitchenBlocks.MYSTIA_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(KitchenBlocks.MYSTIA_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(KitchenBlocks.SUPER_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(KitchenBlocks.SUPER_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(KitchenBlocks.SUPER_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(KitchenBlocks.SUPER_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(KitchenBlocks.SUPER_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(KitchenBlocks.EXTREME_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(KitchenBlocks.EXTREME_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(KitchenBlocks.EXTREME_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(KitchenBlocks.EXTREME_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(KitchenBlocks.EXTREME_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(KitchenBlocks.NUKE_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(KitchenBlocks.NUKE_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(KitchenBlocks.NUKE_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(KitchenBlocks.NUKE_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(KitchenBlocks.NUKE_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

    }

    public static void registerRecipeType(Block block, KitchenRecipeType.KitchenType recipeType) {
        BLOCK_2_KITCHEN_TYPE.put(block, recipeType);
        if (!KITCHEN_TYPE_2_BLOCK.containsKey(recipeType)) {
            KITCHEN_TYPE_2_BLOCK.put(recipeType, block);
        }
    }
}
