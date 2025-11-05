package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RecipeInjectCallback;
import cc.thonly.reverie_dreams.recipe.entry.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class RecipeManager {
    public static final Map<ResourceLocation, BaseRecipeType<?>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    public static final BaseRecipeType<DanmakuRecipe> DANMAKU_TYPE = registerRecipeType(ReverieDreams.id("danmaku"), new DanmakuRecipeType());
    public static final BaseRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW_TYPE = registerRecipeType(ReverieDreams.id("danmaku_shape_draw"), new DanmakuShapeDrawRecipeType());
    public static final BaseRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = registerRecipeType(ReverieDreams.id("gensokyo_altar"), new GensokyoAltarRecipeType());
    public static final BaseRecipeType<StrengthTableRecipe> STRENGTH_TABLE = registerRecipeType(ReverieDreams.id("strength_table"), new StrengthTableRecipeType());
    public static final BaseRecipeType<KitchenRecipe> KITCHEN_TYPE = registerRecipeType(ReverieDreams.id("kitchen"), new KitchenRecipeType());

    public static void bootstrap() {

    }

    public static BaseRecipe getFromOutput(Item item) {
        for (Map.Entry<ResourceLocation, BaseRecipeType<?>> recipeTypeEntry : RECIPE_TYPES.entrySet()) {
            Map<ResourceLocation, ?> registryView = recipeTypeEntry.getValue().getRegistryView();
            for (Map.Entry<ResourceLocation, ?> recipeEntry : registryView.entrySet()) {
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

    public static <R extends BaseRecipe, BR extends BaseRecipeType<R>> BR registerRecipeType(ResourceLocation id, BR recipeType) {
        RegistryHandlers.register(RegistryHandlers.RECIPE_TYPE, id, recipeType);
        RECIPE_TYPES.put(id, recipeType);
        recipeType.bootstrap();
        assert id == recipeType.getId();
        return recipeType;
    }
}
