package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackRecipeWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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
        Map<Identifier, Resource> resources = manager.findResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(Touhou.MOD_ID) && id.getPath().endsWith(".json");
        });

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.of(id.getNamespace(), id.getPath().replaceFirst("^danmaku_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DanmakuRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load danmaku recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                            ItemStack itemStack = recipe.getOutput().getItemStack();
                            itemStack.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.5f, Optional.of(Identifier.of(UUID.randomUUID().toString()))));
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
    public List<DanmakuRecipe> getMatches(List<ItemStackRecipeWrapper> wrappers) {
        if (wrappers.size() < 5) {
            return List.of();
        }

        ItemStackRecipeWrapper dyeSlot = wrappers.get(0);
        ItemStackRecipeWrapper coreSlot = wrappers.get(1);
        ItemStackRecipeWrapper powerSlot = wrappers.get(2);
        ItemStackRecipeWrapper pointSlot = wrappers.get(3);
        ItemStackRecipeWrapper materialSlot = wrappers.get(4);

        List<DanmakuRecipe> matches = new ArrayList<>();
        for (DanmakuRecipe recipe : stream().toList()) {
//            System.out.println("Matching recipe: " + recipe);
//            System.out.println("input dyeSlot:      " + dyeSlot.getItemStack());
//            System.out.println("recipe.getDye():    " + recipe.getDye().getItemStack());
//            System.out.println("dye compare:        " + recipe.getDye().greaterThan(dyeSlot.getItemStack()));
//
//            System.out.println("core compare:       " + recipe.getCore().greaterThan(coreSlot.getItemStack()));
//            System.out.println("point compare:      " + recipe.getPoint().greaterThan(pointSlot.getItemStack()));
//            System.out.println("power compare:      " + recipe.getPower().greaterThan(powerSlot.getItemStack()));
//            System.out.println("material compare:   " + recipe.getMaterial().greaterThan(materialSlot.getItemStack()));
//            System.out.println("-----------------------------");
            if (
                    recipe.getDye().greaterThan(dyeSlot.getItemStack()) &&
                            recipe.getCore().greaterThan(coreSlot.getItemStack()) &&
                            recipe.getPoint().greaterThan(pointSlot.getItemStack()) &&
                            recipe.getPower().greaterThan(powerSlot.getItemStack()) &&
                            recipe.getMaterial().greaterThan(materialSlot.getItemStack())
            ) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    @Override
    public Boolean isMatch(ItemStackRecipeWrapper input, ItemStackRecipeWrapper recipe) {
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
        return Touhou.id(this.getTypeId());
    }
}
