package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.damage.DanmakuDamageType;
import cc.thonly.reverie_dreams.damage.DanmakuDamageTypes;
import cc.thonly.reverie_dreams.data.danmaku.*;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfigs;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.data.npc.NPCRoleInteractionEvent;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.engine.JavaScriptManager;
import cc.thonly.reverie_dreams.entity.npc.*;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuShapes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.SkinConfigs;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RegistryHandlers {
    public static final Map<ResourceKey<? extends Registry<?>>, RegistryHandler<?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final RegistryHandler<BaseRecipeType<?>> RECIPE_TYPE = RegistryHandlers.<BaseRecipeType<?>>ofEntry(ReverieDreams.id("recipe_type"));

    public static final RegistryHandler<DanmakuType> DANMAKU_TYPE = RegistryHandlers.<DanmakuType>ofEntry(ReverieDreams.id("danmaku_type"))
            .codec(DanmakuType.CODEC)
            .builder(DanmakuTypes::bootstrap);

    public static final RegistryHandler<DanmakuShape> DANMAKU_SHAPE = RegistryHandlers.<DanmakuShape>ofEntry(ReverieDreams.id("danmaku_shape"))
            .codec(DanmakuShape.CODEC)
            .builder(DanmakuShapes::bootstrap);

    public static final RegistryHandler<DanmakuTrajectory> DANMAKU_TRAJECTORY = RegistryHandlers.<DanmakuTrajectory>ofEntry(ReverieDreams.id("danmaku_trajectory"))
            .codec(DanmakuTrajectory.CODEC)
            .builder(DanmakuTrajectories::bootstrap);

    public static final RegistryHandler<SpellCardFrameConfig> DANMAKU_CONFIG = RegistryHandlers.<SpellCardFrameConfig>ofEntry(ReverieDreams.id("danmaku_config"))
            .codec(SpellCardFrameConfig.CODEC)
            .reloadBuilder(SpellCardFrameConfigs::reload)
            .builder(SpellCardFrameConfigs::bootstrap);

    public static final RegistryHandler<DanmakuDamageType> DANMAKU_DAMAGE_TYPE = RegistryHandlers.<DanmakuDamageType>ofEntry(ReverieDreams.id("danmaku_damage_type"))
            .codec(DanmakuDamageType.CODEC)
            .defaultId(DanmakuDamageType.DEFAULT_ID)
            .builder(DanmakuDamageTypes::bootstrap);

    public static final RegistryHandler<JavaScriptElement> JAVASCRIPT_ELEMENT = RegistryHandlers.<JavaScriptElement>ofEntry(ReverieDreams.id("javascript_element"))
            .codec(JavaScriptElement.CODEC)
            .reloadBuilder(JavaScriptManager::reload)
            .builder(JavaScriptManager::bootstrap);

    public static final RegistryHandler<SkinType> SKIN_TYPE = RegistryHandlers.<SkinType>ofEntry(ReverieDreams.id("skin_type"))
            .codec(SkinType.UNIT_CODEC)
            .reloadBuilder(SkinType::onReload)
            .builder(GensokyoSkinTypes::bootstrap, MobSkinTypes::bootstrap);

    public static final RegistryHandler<SkinConfig> SKIN_CONFIG = RegistryHandlers.<SkinConfig>ofEntry(ReverieDreams.id("skin_config"))
            .codec(SkinConfig.CODEC)
            .reloadBuilder(SkinConfigs::reload)
            .builder(SkinConfigs::bootstrap);

    public static final RegistryHandler<NPCRole> NPC_ROLE = RegistryHandlers.<NPCRole>ofEntry(ReverieDreams.id("npc_role"))
            .codec(NPCRole.CODEC)
            .builder(NPCRoles::bootstrap);

    public static final RegistryHandler<RoleCard> ROLE_CARD = RegistryHandlers.<RoleCard>ofEntry(ReverieDreams.id("role_card"))
            .codec(RoleCard.CODEC)
            .builder(RoleCards::bootstrap);

    public static final RegistryHandler<NPCRoleInteractionEvent> ROLE_INTERACTION_EVENT = RegistryHandlers.<NPCRoleInteractionEvent>ofEntry(ReverieDreams.id("interaction_event"))
            .codec(NPCRoleInteractionEvent.CODEC)
            .builder(NPCRoleInteractionEvents::bootstrap);

    public static final RegistryHandler<NPCState> NPC_STATE = RegistryHandlers.<NPCState>ofEntry(ReverieDreams.id("npc_state"))
            .codec(NPCState.CODEC)
            .defaultId(NPCState.DEFAULT_ID)
            .builder(NPCStates::bootstrap);

    public static final RegistryHandler<NPCWorkMode> NPC_WORK_MODE = RegistryHandlers.<NPCWorkMode>ofEntry(ReverieDreams.id("npc_work_mode"))
            .codec(NPCWorkMode.CODEC)
            .defaultId(NPCWorkMode.DEFAULT_ID)
            .builder(NPCWorkModes::bootstrap);

    public static final RegistryHandler<FumoType> FUMO = RegistryHandlers.<FumoType>ofEntry(ReverieDreams.id("fumo"))
            .codec(FumoType.CODEC)
            .builder(FumoTypes::bootstrap);

    public static final RegistryHandler<YouseiVariant> YOUSEI_VARIANT = RegistryHandlers.<YouseiVariant>ofEntry(ReverieDreams.id("yousei_variant"))
            .codec(YouseiVariant.CODEC)
            .defaultId(ReverieDreams.id("blue"))
            .builder(YouseiVariants::bootstrap);
    public static final RegistryHandler<FoodProperty> FOOD_PROPERTY = RegistryHandlers.<FoodProperty>ofEntry(ReverieDreams.id("food_property"))
            .codec(FoodProperty.CODEC)
            .reloadBuilder(FoodProperties::reload)
            .builder(FoodProperties::bootstrap);
    public static final RegistryHandler<DrinkProperty> DRINK_PROPERTY = RegistryHandlers.<DrinkProperty>ofEntry(ReverieDreams.id("drink_property"))
            .codec(DrinkProperty.CODEC)
            .reloadBuilder(DrinkProperties::reload)
            .builder(DrinkProperties::bootstrap);
    public static final RegistryHandler<CraftingConflict> CRAFTING_CONFLICT = RegistryHandlers.<CraftingConflict>ofEntry(ReverieDreams.id("crafting_conflict"))
            .codec(CraftingConflict.CODEC)
            .reloadBuilder(CraftingConflict::reload)
            .builder(CraftingConflict::bootstrap);

    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            RegistryHandler<?> registry = entry.getValue();
            registry.build();
        }
    }

    public static <T> T registerForBuiltin(RegistryHandler<T> registry, ResourceLocation key, T value) {
        register(registry, key, value);
        registry.setBuiltin(key, value);
        return value;
    }

    public static <T> T register(RegistryHandler<T> registry, ResourceLocation key, T value) {
        registry.register(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> T set(RegistryHandler<T> registry, ResourceLocation key, T value) {
        registry.set(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> RegistryHandler<T> ofEntry(ResourceLocation identifier) {
        return ofEntry(ResourceKey.createRegistryKey(identifier));
    }

    public static <T> RegistryHandler<T> ofEntry(ResourceKey<? extends Registry<T>> key) {
        if (ROOT.containsKey(key)) {
            return (RegistryHandler<T>) ROOT.get(key);
        }
        RegistryHandler<T> intrinsicalRegister = new RegistryHandler<>(key);
        ROOT.put(key, intrinsicalRegister);
        return intrinsicalRegister;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> getSuggestProvider(
            Command<CommandSourceStack> command
    ) {
        return Commands
                .argument("registry_key", ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    for (ResourceKey<? extends Registry<?>> registryKey : RegistryHandlers.ROOT.keySet()) {
                        builder.suggest(registryKey.location().toString());
                    }
                    return builder.buildFuture();
                })
                .then(Commands.argument("id", ResourceLocationArgument.id())
                        .suggests((context, builder) -> {
                            ResourceLocation identifier = ResourceLocationArgument.getId(context, "registry_key");
                            if (identifier == null) {
                                return builder.buildFuture();
                            }

                            ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(identifier);

                            if (!RegistryHandlers.ROOT.containsKey(registryKey)) {
                                return builder.buildFuture();
                            }

                            RegistryHandler<?> registry = RegistryHandlers.ROOT.get(registryKey);
                            for (ResourceLocation key : registry.keys()) {
                                builder.suggest(key.toString());
                            }

                            return builder.buildFuture();
                        })
                        .executes(command)
                );
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> getSuggestProvider(
            Command<CommandSourceStack> command,
            ResourceKey<Registry<T>> registryKey
    ) {
        return Commands
                .argument("id", ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    RegistryHandler<?> registry = ROOT.get(registryKey);
                    if (registry == null) {
                        return builder.buildFuture();
                    }
                    for (ResourceLocation key : registry.keys()) {
                        builder.suggest(key.toString());
                    }
                    return builder.buildFuture();
                })
                .executes(command);
    }
}
