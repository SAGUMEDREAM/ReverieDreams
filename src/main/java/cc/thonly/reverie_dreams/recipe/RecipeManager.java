package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.RecipeInjectCallback;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.type.DanmakuShapeDrawRecipeType;
import cc.thonly.reverie_dreams.recipe.type.DanmakuRecipeType;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class RecipeManager {
    public static final Map<Identifier, BaseRecipeType<?>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    public static final BaseRecipeType<DanmakuRecipe> DANMAKU_TYPE = registerRecipeType(Touhou.id("danmaku"), new DanmakuRecipeType());
    public static final BaseRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW_TYPE = registerRecipeType(Touhou.id("danmaku_shape_draw"), new DanmakuShapeDrawRecipeType());
    public static final BaseRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = registerRecipeType(Touhou.id("gensokyo_altar"), new GensokyoAltarRecipeType());
    public static final BaseRecipeType<StrengthTableRecipe> STRENGTH_TABLE = registerRecipeType(Touhou.id("strength_table"), new StrengthTableRecipeType());

    public static void bootstrap() {

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

    public static<R extends BaseRecipe> BaseRecipeType<R> registerRecipeType(Identifier id, BaseRecipeType<R> recipeType) {
        RegistryManager.register(RegistryManager.RECIPE_TYPE, id, recipeType);
        RECIPE_TYPES.put(id, recipeType);
        recipeType.bootstrap();
        assert id == recipeType.getId();
        return recipeType;
    }
}
