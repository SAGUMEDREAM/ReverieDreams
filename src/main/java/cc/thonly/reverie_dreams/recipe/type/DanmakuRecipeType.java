package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
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
        Map<ResourceLocation, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^danmaku_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DanmakuRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load danmaku recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                            ItemStack itemStack = recipe.getOutput().getItemStack();
                            itemStack.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.5f, Optional.of(ResourceLocation.parse(UUID.randomUUID().toString()))));
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
    public List<DanmakuRecipe> getMatches(List<ItemStackWrapper> wrappers) {
        if (wrappers.size() < 5) {
            return List.of();
        }

        ItemStackWrapper dyeSlot = wrappers.get(0);
        ItemStackWrapper coreSlot = wrappers.get(1);
        ItemStackWrapper powerSlot = wrappers.get(2);
        ItemStackWrapper pointSlot = wrappers.get(3);
        ItemStackWrapper materialSlot = wrappers.get(4);

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
    public Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe) {
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
    public ResourceLocation getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
