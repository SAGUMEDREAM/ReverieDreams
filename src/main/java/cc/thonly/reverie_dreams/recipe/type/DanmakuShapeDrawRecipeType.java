package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Map<Identifier, Resource> resources = manager.findResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(Touhou.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.of(id.getNamespace(), id.getPath().replaceFirst("^danmaku_shape_draw_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DanmakuShapeDrawRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load danmaku shape draw recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                        });
            } catch (IOException e) {
                log.error("Failed to load danmaku shape draw recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
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

    public List<List<List<Boolean>>> getShapesByOutput(ItemStackWrapper output) {
        List<List<List<Boolean>>> results = new ArrayList<>();

        for (DanmakuShapeDrawRecipe recipe : stream().toList()) {
            ItemStackWrapper outputWrapper = recipe.getOutput();
            ItemStack itemStack = outputWrapper.getItemStack();
            ItemStackWrapper itemStackWrapper = itemStack.get(ModDataComponentTypes.Danmaku.SHAPE);
            if (itemStackWrapper == null) {
                continue;
            }
            if (itemStackWrapper.equals(output)) {
                results.add(recipe.getShape());
            }
        }

        return results;
    }


    @Override
    public List<DanmakuShapeDrawRecipe> getMatches(List<ItemStackWrapper> wrappers) {
        return List.of();
    }

    @Override
    public Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe) {
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
        return Touhou.id(this.getTypeId());
    }
}
