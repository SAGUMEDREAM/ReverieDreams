package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.damage.DanmakuDamageType;
import cc.thonly.reverie_dreams.damage.DanmakuDamageTypes;
import cc.thonly.reverie_dreams.danmaku.*;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.engine.JavaScriptManager;
import cc.thonly.reverie_dreams.entity.npc.*;
import cc.thonly.reverie_dreams.entity.skin.*;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.RoleCards;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
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
public class RegistryManager {
    public static final Map<ResourceKey<? extends Registry<?>>, IntrinsicalRegister<?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final IntrinsicalRegister<BaseRecipeType<?>> RECIPE_TYPE = RegistryManager.<BaseRecipeType<?>>ofEntry(ReverieDreams.id("recipe_type"));

    public static final IntrinsicalRegister<DanmakuType> DANMAKU_TYPE = RegistryManager.<DanmakuType>ofEntry(ReverieDreams.id("danmaku_type"))
            .codec(DanmakuType.CODEC)
            .builder(DanmakuTypes::bootstrap);

    public static final IntrinsicalRegister<DanmakuShape> DANMAKU_SHAPE = RegistryManager.<DanmakuShape>ofEntry(ReverieDreams.id("danmaku_shape"))
            .codec(DanmakuShape.CODEC)
            .builder(DanmakuShapes::bootstrap);

    public static final IntrinsicalRegister<DanmakuTrajectory> DANMAKU_TRAJECTORY = RegistryManager.<DanmakuTrajectory>ofEntry(ReverieDreams.id("danmaku_trajectory"))
            .codec(DanmakuTrajectory.CODEC)
            .builder(DanmakuTrajectories::bootstrap);

    public static final IntrinsicalRegister<DanmakuDamageType> DANMAKU_DAMAGE_TYPE = RegistryManager.<DanmakuDamageType>ofEntry(ReverieDreams.id("danmaku_damage_type"))
            .codec(DanmakuDamageType.CODEC)
            .defaultId(DanmakuDamageType.DEFAULT_ID)
            .builder(DanmakuDamageTypes::bootstrap);

    public static final IntrinsicalRegister<JavaScriptElement> JAVASCRIPT_ELEMENT = RegistryManager.<JavaScriptElement>ofEntry(ReverieDreams.id("javascript_element"))
            .codec(JavaScriptElement.CODEC)
            .reloadBuilder(JavaScriptManager::reload)
            .builder(JavaScriptManager::bootstrap);

    public static final IntrinsicalRegister<SkinType> SKIN_TYPE = RegistryManager.<SkinType>ofEntry(ReverieDreams.id("skin_type"))
            .codec(SkinType.UNIT_CODEC)
            .reloadBuilder(SkinType::onReload)
            .builder(GensokyoSkinTypes::bootstrap, MobSkinTypes::bootstrap);

    public static final IntrinsicalRegister<SkinConfig> SKIN_CONFIG = RegistryManager.<SkinConfig>ofEntry(ReverieDreams.id("skin_config"))
            .codec(SkinConfig.CODEC)
            .reloadBuilder(SkinConfigs::reload)
            .builder(SkinConfigs::bootstrap);

    public static final IntrinsicalRegister<NPCRole> NPC_ROLE = RegistryManager.<NPCRole>ofEntry(ReverieDreams.id("npc_role"))
            .codec(NPCRole.CODEC)
            .builder(NPCRoles::bootstrap);

    public static final IntrinsicalRegister<RoleCard> ROLE_CARD = RegistryManager.<RoleCard>ofEntry(ReverieDreams.id("role_card"))
            .codec(RoleCard.CODEC)
            .builder(RoleCards::bootstrap);

    public static final IntrinsicalRegister<NPCRoleInteractionEvent> ROLE_INTERACTION_EVENT = RegistryManager.<NPCRoleInteractionEvent>ofEntry(ReverieDreams.id("interaction_event"))
            .codec(NPCRoleInteractionEvent.CODEC)
            .builder(NPCRoleInteractionEvents::bootstrap);

    public static final IntrinsicalRegister<NPCState> NPC_STATE = RegistryManager.<NPCState>ofEntry(ReverieDreams.id("npc_state"))
            .codec(NPCState.CODEC)
            .defaultId(NPCState.DEFAULT_ID)
            .builder(NPCStates::bootstrap);

    public static final IntrinsicalRegister<NPCWorkMode> NPC_WORK_MODE = RegistryManager.<NPCWorkMode>ofEntry(ReverieDreams.id("npc_work_mode"))
            .codec(NPCWorkMode.CODEC)
            .defaultId(NPCWorkMode.DEFAULT_ID)
            .builder(NPCWorkModes::bootstrap);

    public static final IntrinsicalRegister<Fumo> FUMO = RegistryManager.<Fumo>ofEntry(ReverieDreams.id("fumo"))
            .codec(Fumo.CODEC)
            .builder(Fumos::bootstrap);

    public static final IntrinsicalRegister<YouseiVariant> YOUSEI_VARIANT = RegistryManager.<YouseiVariant>ofEntry(ReverieDreams.id("yousei_variant"))
            .codec(YouseiVariant.CODEC)
            .defaultId(ReverieDreams.id("blue"))
            .builder(YouseiVariants::bootstrap);

    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            IntrinsicalRegister<?> registry = entry.getValue();
            registry.build();
        }
    }

    public static <T> T registerForBuiltin(IntrinsicalRegister<T> registry, ResourceLocation key, T value) {
        register(registry, key, value);
        registry.setBuiltin(key, value);
        return value;
    }

    public static <T> T register(IntrinsicalRegister<T> registry, ResourceLocation key, T value) {
        registry.register(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> T set(IntrinsicalRegister<T> registry, ResourceLocation key, T value) {
        registry.set(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> IntrinsicalRegister<T> ofEntry(ResourceLocation identifier) {
        return ofEntry(ResourceKey.createRegistryKey(identifier));
    }

    public static <T> IntrinsicalRegister<T> ofEntry(ResourceKey<? extends Registry<T>> key) {
        if (ROOT.containsKey(key)) {
            return (IntrinsicalRegister<T>) ROOT.get(key);
        }
        IntrinsicalRegister<T> intrinsicalRegister = new IntrinsicalRegister<>(key);
        ROOT.put(key, intrinsicalRegister);
        return intrinsicalRegister;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> getSuggestProvider(
            Command<CommandSourceStack> command
    ) {
        return Commands
                .argument("registry_key", ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    for (ResourceKey<? extends Registry<?>> registryKey : RegistryManager.ROOT.keySet()) {
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

                            if (!RegistryManager.ROOT.containsKey(registryKey)) {
                                return builder.buildFuture();
                            }

                            IntrinsicalRegister<?> registry = RegistryManager.ROOT.get(registryKey);
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
                    IntrinsicalRegister<?> registry = ROOT.get(registryKey);
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
