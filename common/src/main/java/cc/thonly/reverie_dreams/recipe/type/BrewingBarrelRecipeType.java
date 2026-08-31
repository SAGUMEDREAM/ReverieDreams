package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BrewingBarrelRecipeType extends BaseRecipeType<BrewingBarrelRecipe> {
    private static BrewingBarrelRecipeType INSTANCE;

    public BrewingBarrelRecipeType() {
        INSTANCE = this;
    }

    public static synchronized BrewingBarrelRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void bootstrap() {

    }

    @Override
    public List<BrewingBarrelRecipe> getMatches(List<IngredientStack> stackList) {
        List<BrewingBarrelRecipe> recipeList = new ArrayList<>();

        for (BrewingBarrelRecipe recipe : this.registries.values()) {
            List<IngredientStack> materials = recipe.getMaterials();

            if (materials.size() > stackList.size()) {
                continue;
            }

            boolean matched = true;

            for (int i = 0; i < materials.size(); i++) {
                ItemComparatorView material = ItemComparatorView.of(materials.get(i)).map(ItemUtils::updateItemStackTag);
                ItemComparatorView input = ItemComparatorView.of(stackList.get(i)).map(ItemUtils::updateItemStackTag);

                if (!material.matches(input)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                recipeList.add(recipe);
            }
        }

        recipeList.sort((a, b) -> Integer.compare(
                b.getMaterials().size(),
                a.getMaterials().size()
        ));

        return recipeList;
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^barrel_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<BrewingBarrelRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load gensokyo altar recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                        });
            } catch (IOException e) {
                log.error("Failed to load gensokyo altar recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
    }

    @Override
    public Codec<BrewingBarrelRecipe> getCodec() {
        return BrewingBarrelRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "barrel";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
