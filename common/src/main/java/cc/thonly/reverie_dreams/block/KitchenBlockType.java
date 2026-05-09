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
    public static final Map<TagKey<Block>, KitchenRecipeType.MappingType> VALUES = new Object2ObjectOpenHashMap<>();

    public static void initialize() {
        registerRecipeType(RDBlockTags.COOKING_TOP, KitchenRecipeType.MappingType.COOKING_POT);
        registerRecipeType(RDBlockTags.CUTTING_BOARD, KitchenRecipeType.MappingType.CUTTING_BOARD);
        registerRecipeType(RDBlockTags.FRYING_PAN, KitchenRecipeType.MappingType.FRYING_PAN);
        registerRecipeType(RDBlockTags.GRILL, KitchenRecipeType.MappingType.GRILL);
        registerRecipeType(RDBlockTags.STEAMER, KitchenRecipeType.MappingType.STEAMER);
    }

    public static List<Block> getMatchBlocks(RegistryAccess registryAccess,
                                                   KitchenRecipeType.MappingType recipeType) {
        Registry<Block> registry = registryAccess.lookupOrThrow(Registries.BLOCK);
        List<Block> result = new ArrayList<>();

        for (Map.Entry<TagKey<Block>, KitchenRecipeType.MappingType> entry : VALUES.entrySet()) {
            if (entry.getValue() == recipeType) {
                for (Holder<Block> holder : registry.getTagOrEmpty(entry.getKey())) {
                    result.add(holder.value());
                }
            }
        }
        return result;
    }

    public static KitchenRecipeType.MappingType getMatchType(RegistryAccess registryAccess, Block block) {
        Registry<Block> registry = registryAccess.lookupOrThrow(Registries.BLOCK);
        Holder<Block> holder = registry.wrapAsHolder(block);
        for (Map.Entry<TagKey<Block>, KitchenRecipeType.MappingType> entry : VALUES.entrySet()) {
            if (holder.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void registerRecipeType(TagKey<Block> tagKey, KitchenRecipeType.MappingType recipeType) {
        VALUES.put(tagKey, recipeType);
    }

}
