package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeInjectCallback;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.networking.payload.RecipeManagerSyncPacket;
import cc.thonly.reverie_dreams.recipe.crafting.DanmakuDyeRecipe;
import cc.thonly.reverie_dreams.recipe.entry.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
public class RecipeManager {
    public static final Map<Identifier, BaseRecipeType<?>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    public static final BaseRecipeType<DanmakuRecipe> DANMAKU = registerRecipeType(ReverieDreams.id("danmaku"), new DanmakuRecipeType());
    public static final BaseRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW = registerRecipeType(ReverieDreams.id("danmaku_shape_draw"), new DanmakuShapeDrawRecipeType());
    public static final BaseRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = registerRecipeType(ReverieDreams.id("gensokyo_altar"), new GensokyoAltarRecipeType());
    public static final BaseRecipeType<StrengthTableRecipe> STRENGTH_TABLE = registerRecipeType(ReverieDreams.id("strength_table"), new StrengthTableRecipeType());
    public static final BaseRecipeType<KitchenRecipe> KITCHEN_TYPE = registerRecipeType(ReverieDreams.id("kitchen"), new KitchenRecipeType());
    public static final BaseRecipeType<BarrelRecipe> BARREL_RECIPE = registerRecipeType(ReverieDreams.id("barrel"), new BarrelRecipeType());
    public static final RegistrySupplier<RecipeSerializer<DanmakuDyeRecipe>> DANMAKU_DYE_RECIPE = registerRecipeSerializer("crafting_special_danmakudye", () -> new RecipeSerializer<>(DanmakuDyeRecipe.MAP_CODEC, DanmakuDyeRecipe.STREAM_CODEC));

    public static void bootstrap() {
    }

    public static <T extends Recipe<?>> RegistrySupplier<RecipeSerializer<T>> registerRecipeSerializer(
            String name,
            Supplier<RecipeSerializer<T>> resourceFunction
    ) {
        return MCBuiltInRegistries.RECIPE_SERIALIZER.register(name, resourceFunction);
    }

    public static <R extends BaseRecipe> SuggestionProvider<CommandSourceStack> getSuggestions(BaseRecipeType<R> type) {
        return (context, builder) -> {
            for (Identifier key : type.keys()) {
                builder.suggest(String.valueOf(key));
            }
            return builder.buildFuture();
        };
    }

    public static void startSyncRecipe(List<ServerPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        List<RecipeManagerSyncPacket> payloads = new ArrayList<>(RECIPE_TYPES.size());

        for (Map.Entry<Identifier, BaseRecipeType<?>> entry : RECIPE_TYPES.entrySet()) {
            Identifier typeId = entry.getKey();
            BaseRecipeType<?> recipeType = entry.getValue();

            CompoundTag tag = BaseRecipeType.writeForTag(recipeType);

            payloads.add(new RecipeManagerSyncPacket(typeId, tag));
        }

        for (ServerPlayer player : players) {
            if (player.isLocalPlayer()) {
                continue;
            }
            for (RecipeManagerSyncPacket payload : payloads) {
                Identifier key = payload.typeId();
                NetworkManager.sendToPlayer(player, payload);
                log.info("Send recipe type registry {} to {}", key, player.getPlainTextName());
            }
        }
    }

    public static BaseRecipe getFromOutput(Item item) {
        for (Map.Entry<Identifier, BaseRecipeType<?>> recipeTypeEntry : RECIPE_TYPES.entrySet()) {
            Map<Identifier, ?> registryView = recipeTypeEntry.getValue().getRegistryView();
            for (Map.Entry<Identifier, ?> recipeEntry : registryView.entrySet()) {
                Object recipeObj = recipeEntry.getValue();
                IngredientStack wrapper = getOutputReflective(recipeObj);
                if (wrapper != null && wrapper.getItem() == item) {
                    return (BaseRecipe) recipeObj;
                }
            }
        }
        return null;
    }

    public static IngredientStack getOutputReflective(Object recipeObj) {
        try {
            Method method = recipeObj.getClass().getMethod("getOutput");
            Object result = method.invoke(recipeObj);

            if (result instanceof IngredientStack wrapper) {
                return wrapper;
            }
        } catch (Exception e) {
            log.error("Can't invoke getOutput: {}", recipeObj.getClass());
        }
        return null;
    }

    public static void onReload(ResourceManager manager) {
        PatchBuilder.INSTANCE.clear();
        RECIPE_TYPES.forEach((key, recipeType) -> {
            long startTime = System.currentTimeMillis();
            try {
                recipeType.removeAll();
                recipeType.reload(manager);
                RecipeInjectCallback.EVENT.invoker().onLoad(recipeType);
                RecipeCompatPatches.removeAll(recipeType);
                RecipeCompatPatchesCallback.EVENT.invoker().onLoad();
                recipeType.sort();
                recipeType.assignRawId();
                long patchesStartTime = System.currentTimeMillis();
                log.info("Start load compatibility recipes");
                RecipeCompatPatches.apply(recipeType);
                long patchesEndTime = System.currentTimeMillis();
                log.info("Finished load compatibility recipes, is took {}ms", patchesEndTime - patchesStartTime);
            } catch (Exception e) {
                log.error("Can't reload recipes {}", key, e);
            } finally {
                long endTime = System.currentTimeMillis();
                log.info("Reloaded Recipe Type {}, it took {}ms", key.toString(), endTime - startTime);
                recipeType.setAcceptNetworking(true);
            }
        });
    }

    public static <R extends BaseRecipe, BR extends BaseRecipeType<R>> BR registerRecipeType(Identifier id, BR recipeType) {
        BuiltInRegistryProviders.register(BuiltInRegistryProviders.RECIPE_TYPE, id, recipeType);
        RECIPE_TYPES.put(id, recipeType);
        recipeType.bootstrap();
        if (!Objects.equals(id, recipeType.getId())) {
            throw new IllegalArgumentException("RecipeType id must be equal registry id, %s != %s".formatted(recipeType.getId(), id));
        }
        return recipeType;
    }

}
