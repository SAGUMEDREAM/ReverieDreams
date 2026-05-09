package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.component.UseCooldown;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class DanmakuRecipeType extends BaseRecipeType<DanmakuRecipe> {
    private static DanmakuRecipeType INSTANCE;

    public DanmakuRecipeType() {
        INSTANCE = this;
    }

    public static synchronized DanmakuRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^danmaku_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DanmakuRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load danmaku recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            recipe.getOutput().set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(Identifier.parse(UUID.randomUUID().toString()))));
                            this.add(registryKey, recipe);
                        });
            } catch (IOException e) {
                log.error("Failed to load danmaku recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
    }

    @Override
    public void bootstrap() {

    }

    @Override
    public List<DanmakuRecipe> getMatches(List<IngredientStack> wrappers) {
        if (wrappers.size() < 5) {
            return List.of();
        }

        IngredientStack dyeSlot = wrappers.get(0);
        IngredientStack coreSlot = wrappers.get(1);
        IngredientStack powerSlot = wrappers.get(2);
        IngredientStack pointSlot = wrappers.get(3);
        IngredientStack materialSlot = wrappers.get(4);

        List<DanmakuRecipe> matches = new ArrayList<>();
        for (DanmakuRecipe recipe : this.stream().toList()) {
//            System.out.println("Matching recipe key: " + this.getRecipeKey(recipe));
//            System.out.println("Matching recipe: " + recipe);
//            System.out.println("input dyeSlot:      " + dyeSlot.build());
//            System.out.println("recipe.getDye():    " + ItemComparatorView.of(recipe.getDye().build()));
//            System.out.println("dye compare:        " + ItemComparatorView.of(recipe.getDye()).greaterThan(dyeSlot));
//
//            System.out.println("core compare:       " + ItemComparatorView.of(recipe.getCore()).greaterThan(coreSlot));
//            System.out.println("point compare:      " + ItemComparatorView.of(recipe.getPoint()).greaterThan(pointSlot));
//            System.out.println("power compare:      " + ItemComparatorView.of(recipe.getPower()).greaterThan(powerSlot));
//            System.out.println("material compare:   " + ItemComparatorView.of(recipe.getMaterial()).greaterThan(materialSlot));
//            System.out.println("-----------------------------");
            if (
                    ItemComparatorView.of(recipe.getDye()).greaterThan(dyeSlot) &&
                            ItemComparatorView.of(recipe.getCore()).greaterThan(coreSlot) &&
                            ItemComparatorView.of(recipe.getPoint()).greaterThan(pointSlot) &&
                            ItemComparatorView.of(recipe.getPower()).greaterThan(powerSlot) &&
                            ItemComparatorView.of(recipe.getMaterial()).greaterThan(materialSlot)
            ) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public Codec<DanmakuRecipe> getCodec() {
        return DanmakuRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "danmaku";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
