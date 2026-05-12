package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KitchenBlockType {
    public static final Map<TagKey<Block>, KitchenRecipeType.TypeInstance> VALUES = new Object2ObjectOpenHashMap<>();

    public static void initialize() {
        registerRecipeType(RDBlockTags.COOKING_TOP, KitchenRecipeType.TypeInstance.COOKING_POT);
        registerRecipeType(RDBlockTags.CUTTING_BOARD, KitchenRecipeType.TypeInstance.CUTTING_BOARD);
        registerRecipeType(RDBlockTags.FRYING_PAN, KitchenRecipeType.TypeInstance.FRYING_PAN);
        registerRecipeType(RDBlockTags.GRILL, KitchenRecipeType.TypeInstance.GRILL);
        registerRecipeType(RDBlockTags.STEAMER, KitchenRecipeType.TypeInstance.STEAMER);
    }

    public static List<Block> getMatchBlocks(RegistryAccess registryAccess,
                                                   KitchenRecipeType.TypeInstance recipeType) {
        Registry<Block> registry = registryAccess.lookupOrThrow(Registries.BLOCK);
        List<Block> result = new ArrayList<>();

        for (Map.Entry<TagKey<Block>, KitchenRecipeType.TypeInstance> entry : VALUES.entrySet()) {
            if (entry.getValue() == recipeType) {
                for (Holder<Block> holder : registry.getTagOrEmpty(entry.getKey())) {
                    result.add(holder.value());
                }
            }
        }
        return result;
    }

    public static KitchenRecipeType.TypeInstance getMatchType(RegistryAccess registryAccess, Block block) {
        Registry<Block> registry = registryAccess.lookupOrThrow(Registries.BLOCK);
        Holder<Block> holder = registry.wrapAsHolder(block);
        for (Map.Entry<TagKey<Block>, KitchenRecipeType.TypeInstance> entry : VALUES.entrySet()) {
            if (holder.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void registerRecipeType(TagKey<Block> tagKey, KitchenRecipeType.TypeInstance recipeType) {
        VALUES.put(tagKey, recipeType);
    }

}
