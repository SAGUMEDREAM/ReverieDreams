package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.api.recipe.RecipeInjectCallback;
import cc.thonly.reverie_dreams.networking.payload.RecipeManagerSyncPacket;
import cc.thonly.reverie_dreams.recipe.crafting.DanmakuDyeRecipe;
import cc.thonly.reverie_dreams.recipe.entry.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class RecipeManager {
    public static final Map<Identifier, BaseRecipeType<?>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    public static final BaseRecipeType<DanmakuRecipe> DANMAKU = registerRecipeType(ReverieDreams.id("danmaku"), new DanmakuRecipeType());
    public static final BaseRecipeType<DanmakuShapeDrawRecipe> DANMAKU_SHAPE_DRAW = registerRecipeType(ReverieDreams.id("danmaku_shape_draw"), new DanmakuShapeDrawRecipeType());
    public static final BaseRecipeType<GensokyoAltarRecipe> GENSOKYO_ALTAR = registerRecipeType(ReverieDreams.id("gensokyo_altar"), new GensokyoAltarRecipeType());
    public static final BaseRecipeType<StrengthTableRecipe> STRENGTH_TABLE = registerRecipeType(ReverieDreams.id("strength_table"), new StrengthTableRecipeType());
    public static final BaseRecipeType<KitchenRecipe> KITCHEN_TYPE = registerRecipeType(ReverieDreams.id("kitchen"), new KitchenRecipeType());
    public static Holder<RecipeSerializer<DanmakuDyeRecipe>> DANMAKU_DYE_RECIPE;

    public static void bootstrap(BalmRegistrars registrars) {
        BalmRegistrar.Scoped<RecipeSerializer<?>> recipeSerializerScoped = registrars.registrar(Registries.RECIPE_SERIALIZER);
        DANMAKU_DYE_RECIPE = registerRecipeSerializer(recipeSerializerScoped, "crafting_special_danmakudye", key -> new CustomRecipe.Serializer<>(DanmakuDyeRecipe::new));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe<?>> Holder<RecipeSerializer<T>> registerRecipeSerializer(
            BalmRegistrar.Scoped<? extends RecipeSerializer<?>> scoped,
            String name,
            Function<Identifier, RecipeSerializer<T>> resourceFunction
    ) {
        return (Holder<RecipeSerializer<T>>) (Holder<?>) scoped.register(name, (Function) resourceFunction);
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
                Balm.networking().sendTo(player, payload);
            }
        }
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
        RegistryImpls.register(RegistryImpls.RECIPE_TYPE, id, recipeType);
        RECIPE_TYPES.put(id, recipeType);
        recipeType.bootstrap();
        assert id == recipeType.getId();
        return recipeType;
    }
}
