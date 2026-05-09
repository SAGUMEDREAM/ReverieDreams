package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
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
import cc.thonly.reverie_dreams.entity.npc.NPCRoleInteractionEvents;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitVariant;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitVariants;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariant;
import cc.thonly.reverie_dreams.entity.variant.YouseiVariants;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.networking.payload.RegistryImpSyncPacket;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuShapes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.SkinConfigs;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RegistryImpls {
    public static final Map<ResourceKey<? extends Registry<?>>, RegistryImpl<?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final RegistryImpl<BaseRecipeType<?>> RECIPE_TYPE = RegistryImpls.<BaseRecipeType<?>>ofEntry(ReverieDreams.id("recipe_type"));

    public static final RegistryImpl<DanmakuType> DANMAKU_TYPE = RegistryImpls.<DanmakuType>ofEntry(ReverieDreams.id("danmaku_type"))
            .codec(DanmakuType.COMPONENT_CODEC)
            .builder(DanmakuTypes::bootstrap);

    public static final RegistryImpl<DanmakuShape> DANMAKU_SHAPE = RegistryImpls.<DanmakuShape>ofEntry(ReverieDreams.id("danmaku_shape"))
            .codec(DanmakuShape.CODEC)
            .builder(DanmakuShapes::bootstrap);

    public static final RegistryImpl<DanmakuTrajectory> DANMAKU_TRAJECTORY = RegistryImpls.<DanmakuTrajectory>ofEntry(ReverieDreams.id("danmaku_trajectory"))
            .codec(DanmakuTrajectory.CODEC)
            .builder(DanmakuTrajectories::bootstrap);

    public static final RegistryImpl<SpellCardFrameConfig> DANMAKU_CONFIG = RegistryImpls.<SpellCardFrameConfig>ofEntry(ReverieDreams.id("danmaku_config"))
            .codec(SpellCardFrameConfig.COMPONENT_CODEC)
            .reloadBuilder(SpellCardFrameConfigs::reload)
            .builder(SpellCardFrameConfigs::bootstrap);

    public static final RegistryImpl<JavaScriptElement> JAVASCRIPT_ELEMENT = RegistryImpls.<JavaScriptElement>ofEntry(ReverieDreams.id("javascript_element"))
            .codec(JavaScriptElement.CODEC)
            .reloadBuilder(JavaScriptManager::reload)
            .builder(JavaScriptManager::bootstrap);

    public static final RegistryImpl<SkinType> SKIN_TYPE = RegistryImpls.<SkinType>ofEntry(ReverieDreams.id("skin_type"))
            .codec(SkinType.UNIT_CODEC)
            .reloadBuilder(SkinType::onReload)
            .builder(GensokyoSkinTypes::bootstrap, MobSkinTypes::bootstrap);

    public static final RegistryImpl<SkinConfig> SKIN_CONFIG = RegistryImpls.<SkinConfig>ofEntry(ReverieDreams.id("skin_config"))
            .codec(SkinConfig.CODEC)
            .reloadBuilder(SkinConfigs::reload)
            .builder(SkinConfigs::bootstrap);

    public static final RegistryImpl<NPCRole> NPC_ROLE = RegistryImpls.<NPCRole>ofEntry(ReverieDreams.id("npc_role"))
            .codec(NPCRole.CODEC)
            .builder(NPCRoles::bootstrap);

    public static final RegistryImpl<RoleCard> ROLE_CARD = RegistryImpls.<RoleCard>ofEntry(ReverieDreams.id("role_card"))
            .codec(RoleCard.CODEC)
            .builder(RoleCards::bootstrap);

    public static final RegistryImpl<NPCRoleInteractionEvent> ROLE_INTERACTION_EVENT = RegistryImpls.<NPCRoleInteractionEvent>ofEntry(ReverieDreams.id("interaction_event"))
            .codec(NPCRoleInteractionEvent.CODEC)
            .builder(NPCRoleInteractionEvents::bootstrap);

    public static final RegistryImpl<NPCState> NPC_STATE = RegistryImpls.<NPCState>ofEntry(ReverieDreams.id("npc_state"))
            .codec(NPCState.CODEC)
            .defaultId(NPCState.DEFAULT_ID)
            .builder(NPCStates::bootstrap);

    public static final RegistryImpl<NPCWorkMode> NPC_WORK_MODE = RegistryImpls.<NPCWorkMode>ofEntry(ReverieDreams.id("npc_work_mode"))
            .codec(NPCWorkMode.CODEC)
            .defaultId(NPCWorkMode.DEFAULT_ID)
            .builder(NPCWorkModes::bootstrap);

    public static final RegistryImpl<FumoType> FUMO = RegistryImpls.<FumoType>ofEntry(ReverieDreams.id("fumo"))
            .codec(FumoType.CODEC)
            .builder(FumoTypes::bootstrap);

    public static final RegistryImpl<YouseiVariant> YOUSEI_VARIANT = RegistryImpls.<YouseiVariant>ofEntry(ReverieDreams.id("yousei_variant"))
            .codec(YouseiVariant.CODEC)
            .defaultId(ReverieDreams.id("blue"))
            .builder(YouseiVariants::bootstrap);

    public static final RegistryImpl<RabbitUnitVariant> RABBIT_UNIT_VARIANT = RegistryImpls.<RabbitUnitVariant>ofEntry(ReverieDreams.id("rabbit_unit_variant"))
            .codec(RabbitUnitVariant.CODEC)
            .defaultId(ReverieDreams.id("blue"))
            .builder(RabbitUnitVariants::bootstrap);

    public static final RegistryImpl<FoodProperty> FOOD_PROPERTY = RegistryImpls.<FoodProperty>ofEntry(ReverieDreams.id("food_property"))
            .codec(FoodProperty.COMPONENT_CODEC)
            .reloadBuilder(FoodProperties::reload)
            .builder(FoodProperties::bootstrap)
            .syncToClient(RegistrySyncers.FOOD_PROPERTY);

    public static final RegistryImpl<DrinkProperty> DRINK_PROPERTY = RegistryImpls.<DrinkProperty>ofEntry(ReverieDreams.id("drink_property"))
            .codec(DrinkProperty.COMPONENT_CODEC)
            .reloadBuilder(DrinkProperties::reload)
            .builder(DrinkProperties::bootstrap)
            .syncToClient(RegistrySyncers.DRINK_PROPERTY);

    public static final RegistryImpl<CraftingConflict> CRAFTING_CONFLICT = RegistryImpls.<CraftingConflict>ofEntry(ReverieDreams.id("crafting_conflict"))
            .codec(CraftingConflict.CODEC)
            .reloadBuilder(CraftingConflict::reload)
            .builder(CraftingConflict::bootstrap)
            .syncToClient(RegistrySyncers.CRAFTING_CONFLICT);

    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            RegistryImpl<?> registry = entry.getValue();
            registry.build();
        }
    }

    public static void startSyncRegistry(List<ServerPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        Map<Identifier, RegistryImpl<?>> map = new Object2ObjectLinkedOpenHashMap<>();

        ROOT.forEach((key, object) -> {
            if (!(object instanceof RegistryImpl<?> registry)) {
                return;
            }
            if (!registry.isSyncToClient()) {
                return;
            }

            map.put(key.identifier(), registry);
        });

        List<RegistryImpSyncPacket> payloads = buildPayload(map);

        for (ServerPlayer player : players) {
            for (RegistryImpSyncPacket payload : payloads) {
                Balm.networking().sendTo(player, payload);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<RegistryImpSyncPacket> buildPayload(
            Map<Identifier, RegistryImpl<?>> registryMap
    ) {
        List<RegistryImpSyncPacket> payloads = new ArrayList<>(registryMap.size());

        for (Map.Entry<Identifier, RegistryImpl<?>> mapEntry : registryMap.entrySet()) {
            Identifier key = mapEntry.getKey();
            RegistryImpl<?> registry = mapEntry.getValue();

            if (key == null || registry == null) continue;

            RegistrySyncer syncer = registry.getSyncer();

            List<RegistrySyncer.Entry> entryList = new ArrayList<>();

            for (Map.Entry<? extends ResourceKey<?>, ?> registryEntry : registry.entrySet()) {
                ResourceKey itemKey = registryEntry.getKey();
                Object itemValue = registryEntry.getValue();

                entryList.add(
                        RegistrySyncer.Entry.of(
                                itemKey.identifier(),
                                itemValue
                        )
                );
            }

            CompoundTag tag = syncer.writeToTag(entryList);

            payloads.add(new RegistryImpSyncPacket(key, tag));
        }

        return payloads;
    }

    public static <T> T registerForBuiltin(RegistryImpl<T> registry, Identifier key, T value) {
        register(registry, key, value);
        registry.setBuiltin(key, value);
        return value;
    }

    public static <T> T register(RegistryImpl<T> registry, Identifier key, T value) {
        registry.register(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> T set(RegistryImpl<T> registry, Identifier key, T value) {
        registry.set(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> RegistryImpl<T> ofEntry(Identifier identifier) {
        return ofEntry(ResourceKey.createRegistryKey(identifier));
    }

    public static <T> RegistryImpl<T> ofEntry(ResourceKey<? extends Registry<T>> key) {
        if (ROOT.containsKey(key)) {
            return (RegistryImpl<T>) ROOT.get(key);
        }
        RegistryImpl<T> intrinsicalRegister = (RegistryImpl<T>) ReverieDreams.REGISTRY_GETTER.apply(key);
        ROOT.put(key, intrinsicalRegister);
        return intrinsicalRegister;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> getSuggestProvider(
            Command<CommandSourceStack> command
    ) {
        return Commands
                .argument("registry_key", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    for (ResourceKey<? extends Registry<?>> registryKey : RegistryImpls.ROOT.keySet()) {
                        builder.suggest(registryKey.identifier().toString());
                    }
                    return builder.buildFuture();
                })
                .then(Commands.argument("id", IdentifierArgument.id())
                        .suggests((context, builder) -> {
                            Identifier identifier = IdentifierArgument.getId(context, "registry_key");

                            ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(identifier);

                            if (!RegistryImpls.ROOT.containsKey(registryKey)) {
                                return builder.buildFuture();
                            }

                            RegistryImpl<?> registry = RegistryImpls.ROOT.get(registryKey);
                            for (Identifier key : registry.keys()) {
                                builder.suggest(key.toString());
                            }

                            return builder.buildFuture();
                        })
                        .executes(command)
                );
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, Identifier> getSuggestProvider(
            Command<CommandSourceStack> command,
            ResourceKey<Registry<T>> registryKey
    ) {
        return Commands
                .argument("id", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    RegistryImpl<?> registry = ROOT.get(registryKey);
                    if (registry == null) {
                        return builder.buildFuture();
                    }
                    for (Identifier key : registry.keys()) {
                        builder.suggest(key.toString());
                    }
                    return builder.buildFuture();
                })
                .executes(command);
    }

}
