package cc.thonly.mystias_izakaya.block;

import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;

import java.util.Map;

public class KitchenBlockType {
    public static final Map<Block, KitchenRecipeType.KitchenType> BLOCK_2_KITCHEN_TYPE = new Object2ObjectOpenHashMap<>();
    public static final Map<KitchenRecipeType.KitchenType, Block> KITCHEN_TYPE_2_BLOCK = new Object2ObjectOpenHashMap<>();

    public static void init() {
        registerRecipeType(MIBlocks.COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(MIBlocks.CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(MIBlocks.FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(MIBlocks.GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(MIBlocks.STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(MIBlocks.MYSTIA_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(MIBlocks.MYSTIA_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(MIBlocks.MYSTIA_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(MIBlocks.MYSTIA_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(MIBlocks.MYSTIA_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(MIBlocks.SUPER_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(MIBlocks.SUPER_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(MIBlocks.SUPER_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(MIBlocks.SUPER_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(MIBlocks.SUPER_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(MIBlocks.EXTREME_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(MIBlocks.EXTREME_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(MIBlocks.EXTREME_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(MIBlocks.EXTREME_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(MIBlocks.EXTREME_STEAMER, KitchenRecipeType.KitchenType.STREAMER);

        registerRecipeType(MIBlocks.NUKE_COOKING_POT, KitchenRecipeType.KitchenType.COOKING_POT);
        registerRecipeType(MIBlocks.NUKE_CUTTING_BOARD, KitchenRecipeType.KitchenType.CUTTING_BOARD);
        registerRecipeType(MIBlocks.NUKE_FRYING_PAN, KitchenRecipeType.KitchenType.FRYING_PAN);
        registerRecipeType(MIBlocks.NUKE_GRILL, KitchenRecipeType.KitchenType.GRILL);
        registerRecipeType(MIBlocks.NUKE_STEAMER, KitchenRecipeType.KitchenType.STREAMER);


    }

    public static void registerRecipeType(Block block, KitchenRecipeType.KitchenType recipeType) {
        BLOCK_2_KITCHEN_TYPE.put(block, recipeType);
        if (!KITCHEN_TYPE_2_BLOCK.containsKey(recipeType)) {
            KITCHEN_TYPE_2_BLOCK.put(recipeType, block);
        }
    }
}
