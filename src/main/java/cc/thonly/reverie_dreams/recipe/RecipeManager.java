package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RecipeInjectCallback;
import cc.thonly.reverie_dreams.recipe.crafting.DanmakuDyeRecipe;
import cc.thonly.reverie_dreams.recipe.entry.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class RecipeManager {
    public static final Map<Identifier, BaseRecipeType<?>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    public static final BaseRecipeType<DanmakuRecipe> DANMAKU_TYPE = registerRecipeType(ReverieDreams.id("danmaku"), new DanmakuRecipeType());
    public static final BaseRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW_TYPE = registerRecipeType(ReverieDreams.id("danmaku_shape_draw"), new DanmakuShapeDrawRecipeType());
    public static final BaseRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = registerRecipeType(ReverieDreams.id("gensokyo_altar"), new GensokyoAltarRecipeType());
    public static final BaseRecipeType<StrengthTableRecipe> STRENGTH_TABLE = registerRecipeType(ReverieDreams.id("strength_table"), new StrengthTableRecipeType());
    public static final BaseRecipeType<KitchenRecipe> KITCHEN_TYPE = registerRecipeType(ReverieDreams.id("kitchen"), new KitchenRecipeType());
    public static final RecipeSerializer<DanmakuDyeRecipe> DANMAKU_DYE_RECIPE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ReverieDreams.id("crafting_special_danmakudye"), new CustomRecipe.Serializer<>(DanmakuDyeRecipe::new));

    public static void bootstrap() {

    }

    public static <R extends BaseRecipe> SuggestionProvider<CommandSourceStack> getSuggestions(BaseRecipeType<R> type) {
        return (context, builder) -> {
            for (Identifier key : type.keys()) {
                builder.suggest(String.valueOf(key));
            }
            return builder.buildFuture();
        };
    }

    public static BaseRecipe getFromOutput(Item item) {
        for (Map.Entry<Identifier, BaseRecipeType<?>> recipeTypeEntry : RECIPE_TYPES.entrySet()) {
            Map<Identifier, ?> registryView = recipeTypeEntry.getValue().getRegistryView();
            for (Map.Entry<Identifier, ?> recipeEntry : registryView.entrySet()) {
                Object recipeObj = recipeEntry.getValue();
                ItemStackWrapper wrapper = getOutputReflective(recipeObj);
                if (wrapper != null && wrapper.getItem() == item) {
                    return (BaseRecipe) recipeObj;
                }
            }
        }
        return null;
    }

    public static ItemStackWrapper getOutputReflective(Object recipeObj) {
        try {
            Method method = recipeObj.getClass().getMethod("getOutput");
            Object result = method.invoke(recipeObj);

            if (result instanceof ItemStackWrapper wrapper) {
                return wrapper;
            }
        } catch (Exception e) {
            log.error("Can't invoke getOutput: {}", recipeObj.getClass());
        }
        return null;
    }

    public static void onReload(ResourceManager manager) {
        RecipeCompatPatchesImpl.Builder.INSTANCE.clear();
        RECIPE_TYPES.forEach((key, recipeType) -> {
            try {
                recipeType.removeAll();
                recipeType.reload(manager);
                recipeType.sort();
                recipeType.assignRawId();
                RecipeCompatPatchesCallback.EVENT.invoker().onLoad();
                RecipeInjectCallback.EVENT.invoker().onLoad(recipeType);
                log.info("Reloaded Recipe Type {}", key.toString());
                RecipeCompatPatchesImpl.apply(recipeType);
            } catch (Exception e) {
                log.error("Can't reload recipes {}, {}", key, e);
            }
        });
    }

    public static <R extends BaseRecipe, BR extends BaseRecipeType<R>> BR registerRecipeType(Identifier id, BR recipeType) {
        RegistryHandlers.register(RegistryHandlers.RECIPE_TYPE, id, recipeType);
        RECIPE_TYPES.put(id, recipeType);
        recipeType.bootstrap();
        assert id == recipeType.getId();
        return recipeType;
    }
}
