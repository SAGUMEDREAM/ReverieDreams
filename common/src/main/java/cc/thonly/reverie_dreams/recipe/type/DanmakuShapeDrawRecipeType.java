package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cc.thonly.reverie_dreams.recipe.RecipeResourceLoader;

@Slf4j
public class DanmakuShapeDrawRecipeType extends BaseRecipeType<DanmakuShapeDrawRecipe> {
    private static DanmakuShapeDrawRecipeType INSTANCE;

    public DanmakuShapeDrawRecipeType() {
        INSTANCE = this;
    }

    public static synchronized DanmakuShapeDrawRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        RecipeResourceLoader.load(manager, this.getTypeId() + "_recipe",
                id -> id.getNamespace().equals(ReverieDreams.MOD_ID), this.getCodec(), this::add);
        this.loadPreset();
    }

    private void loadPreset() {

    }

    @Override
    public void bootstrap() {

    }

    public List<DanmakuShapeDrawRecipe> getMatches(List<List<Boolean>> input, Unit unit) {
        List<DanmakuShapeDrawRecipe> matches = new ArrayList<>();

        for (DanmakuShapeDrawRecipe recipe : stream().toList()) {
            List<List<Boolean>> shape = recipe.getShape();

            if (input.size() != shape.size()) {
                continue;
            }

            boolean matched = true;
            for (int y = 0; y < shape.size(); y++) {
                List<Boolean> rowShape = shape.get(y);
                List<Boolean> rowInput = input.get(y);

                if (rowInput.size() != rowShape.size()) {
                    matched = false;
                    break;
                }

                for (int x = 0; x < rowShape.size(); x++) {
                    if (!rowShape.get(x).equals(rowInput.get(x))) {
                        matched = false;
                        break;
                    }
                }

                if (!matched) break;
            }

            if (matched) {
                matches.add(recipe);
                return matches;
            }
        }

        return matches;
    }

    public List<List<List<Boolean>>> getShapesByOutput(IngredientStack output) {
        List<List<List<Boolean>>> results = new ArrayList<>();

        for (DanmakuShapeDrawRecipe recipe : stream().toList()) {
            IngredientStack outputStack = recipe.getOutput();
            ItemStack itemStack = outputStack.build();
            IngredientStack ingredientStack = itemStack.get(RDDataComponentTypes.DANMAKU_SHAPE.value());
            if (ingredientStack == null) {
                continue;
            }
            if (ItemComparatorView.of(ingredientStack).matches(output)) {
                results.add(recipe.getShape());
            }
        }

        return results;
    }


    @Override
    public List<DanmakuShapeDrawRecipe> getMatches(List<IngredientStack> wrappers) {
        return List.of();
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public Codec<DanmakuShapeDrawRecipe> getCodec() {
        return DanmakuShapeDrawRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "danmaku_shape_draw";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
