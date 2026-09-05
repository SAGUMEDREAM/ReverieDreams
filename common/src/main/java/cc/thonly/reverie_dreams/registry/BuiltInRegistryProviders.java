package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.*;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfig;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfigs;
import cc.thonly.reverie_dreams.data.npc.*;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.engine.JavaScriptElement;
import cc.thonly.reverie_dreams.engine.JavaScriptManager;
import cc.thonly.reverie_dreams.entity.npc.NPCLikeInteractionEvents;
import cc.thonly.reverie_dreams.entity.skill.Skill;
import cc.thonly.reverie_dreams.entity.skill.Skills;
import cc.thonly.reverie_dreams.entity.variant.*;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.networking.payload.CustomRegistrySyncPacket;
import cc.thonly.reverie_dreams.proxy.MergeRegistryProviderFactory;
import cc.thonly.reverie_dreams.proxy.PlatformProxies;
import cc.thonly.reverie_dreams.proxy.RegistryProviderFactory;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuShapes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.content.skin.SkinConfigs;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings({"unchecked", "rawtypes", "SpellCheckingInspection"})
public class BuiltInRegistryProviders {
    public static final Map<ResourceKey<? extends Registry<?>>, RegistryProvider<?>> ROOT = new Object2ObjectLinkedOpenHashMap<>();
    public static final RegistryProvider<BaseRecipeType<?>> RECIPE_TYPE = BuiltInRegistryProviders.<BaseRecipeType<?>>ofEntry(ReverieDreams.id("recipe_type"));
    public static boolean LOOKUP = false;
    private static RegistryState RECIPE_STATE = RegistryState.LOADING;

    public static final RegistryProvider<DanmakuType> DANMAKU_TYPE =
            BuiltInRegistryProviders.<DanmakuType>ofEntry(BuiltInRegistryProviderKeys.DANMAKU_TYPE)
                    .codec(DanmakuType.CODEC)
                    .builder(DanmakuTypes::bootstrap);

    public static final RegistryProvider<DanmakuShape> DANMAKU_SHAPE =
            BuiltInRegistryProviders.<DanmakuShape>ofEntry(BuiltInRegistryProviderKeys.DANMAKU_SHAPE)
                    .codec(DanmakuShape.CODEC)
                    .builder(DanmakuShapes::bootstrap);

    public static final RegistryProvider<DanmakuTrajectory> DANMAKU_TRAJECTORY =
            BuiltInRegistryProviders.<DanmakuTrajectory>ofEntry(BuiltInRegistryProviderKeys.DANMAKU_TRAJECTORY)
                    .codec(DanmakuTrajectory.CODEC)
                    .builder(DanmakuTrajectories::bootstrap);

    public static final RegistryProvider<SpellCardFrameConfig> DANMAKU_CONFIG =
            BuiltInRegistryProviders.<SpellCardFrameConfig>ofEntry(BuiltInRegistryProviderKeys.DANMAKU_CONFIG)
                    .codec(SpellCardFrameConfig.COMPONENT_CODEC)
                    .reloadBuilder(SpellCardFrameConfigs::reload)
                    .builder(SpellCardFrameConfigs::bootstrap);

    public static final RegistryProvider<JavaScriptElement> JAVASCRIPT_ELEMENT =
            BuiltInRegistryProviders.<JavaScriptElement>ofEntry(BuiltInRegistryProviderKeys.JAVASCRIPT_ELEMENT)
                    .codec(JavaScriptElement.CODEC)
                    .reloadBuilder(JavaScriptManager::reload)
                    .builder(JavaScriptManager::bootstrap);

    public static final RegistryProvider<SkinType> SKIN_TYPE =
            BuiltInRegistryProviders.<SkinType>ofEntry(BuiltInRegistryProviderKeys.SKIN_TYPE)
                    .codec(SkinType.CODEC)
                    .reloadBuilder(SkinType::onReload)
                    .builder(GensokyoSkinTypes::bootstrap, MobSkinTypes::bootstrap);

    public static final RegistryProvider<SkinConfig> SKIN_CONFIG =
            BuiltInRegistryProviders.<SkinConfig>ofEntry(BuiltInRegistryProviderKeys.SKIN_CONFIG)
                    .codec(SkinConfig.CODEC)
                    .reloadBuilder(SkinConfigs::onReload)
                    .builder(SkinConfigs::bootstrap);

    public static final RegistryProvider<CustomType> CUSTOM_SKIN_TYPE =
            BuiltInRegistryProviders.<CustomType>ofEntry(BuiltInRegistryProviderKeys.CUSTOM_SKIN_TYPE)
                    .codec(CustomType.CODEC)
                    .reloadBuilder(CustomSkinLoader::onReload)
                    .syncToClient(RegistrySyncers.CUSTOM_SKIN);

    public static final RegistryProvider<NPCRoleType> NPC_ROLE_TYPE =
            BuiltInRegistryProviders.<NPCRoleType>ofEntry(BuiltInRegistryProviderKeys.NPC_ROLE_TYPE)
                    .codec(NPCRoleType.BY_REGISTRY_CODEC)
                    .builder(NPCRoleTypes::bootstrap);

    public static final RegistryProvider<NPCSimpleRoleType> NPC_SIMPLE_ROLE_TYPE =
            BuiltInRegistryProviders.<NPCSimpleRoleType>ofEntry(BuiltInRegistryProviderKeys.NPC_SIMPLE_ROLE)
                    .codec(NPCSimpleRoleType.BY_REGISTRY_CODEC);

    public static final RegistryProvider<NPCMenuType> NPC_MENU_TYPE =
            BuiltInRegistryProviders.ofEntry(BuiltInRegistryProviderKeys.NPC_MENU_TYPE)
                    .codec(NPCMenuType.BY_REGISTRY_CODEC)
                    .builder(NPCMenuTypes::bootstrap);

    public static final RegistryProvider<RoleCard> ROLE_CARD =
            BuiltInRegistryProviders.<RoleCard>ofEntry(BuiltInRegistryProviderKeys.ROLE_CARD)
                    .codec(RoleCard.CODEC)
                    .builder(RoleCards::bootstrap);

    public static final RegistryProvider<Skill<?>> SKILL =
            BuiltInRegistryProviders.<Skill<?>>ofEntry(BuiltInRegistryProviderKeys.SKILL)
                    .codec(Skill.CODEC)
                    .builder(Skills::bootstrap);

    public static final RegistryProvider<NPCLikeInteractionEvent> NPCLIKE_INTERACTION_EVENT =
            BuiltInRegistryProviders.<NPCLikeInteractionEvent>ofEntry(BuiltInRegistryProviderKeys.NPCLIKE_INTERACTION_EVENT)
                    .codec(NPCLikeInteractionEvent.CODEC)
                    .builder(NPCLikeInteractionEvents::bootstrap);

    public static final RegistryProvider<NPCState> NPC_STATE =
            BuiltInRegistryProviders.<NPCState>ofEntry(BuiltInRegistryProviderKeys.NPC_STATE)
                    .codec(NPCState.CODEC)
                    .defaultId(NPCState.DEFAULT_ID)
                    .builder(NPCStates::bootstrap);

    public static final RegistryProvider<NPCWorkMode> NPC_WORK_MODE =
            BuiltInRegistryProviders.<NPCWorkMode>ofEntry(BuiltInRegistryProviderKeys.NPC_WORK_MODE)
                    .codec(NPCWorkMode.CODEC)
                    .defaultId(NPCWorkMode.DEFAULT_ID)
                    .builder(NPCWorkModes::bootstrap);

    public static final RegistryProvider<FumoType> FUMO =
            BuiltInRegistryProviders.<FumoType>ofEntry(BuiltInRegistryProviderKeys.FUMO)
                    .codec(FumoType.CODEC)
                    .builder(FumoTypes::bootstrap);

    public static final RegistryProvider<YouseiVariant> YOUSEI_VARIANT =
            BuiltInRegistryProviders.<YouseiVariant>ofEntry(BuiltInRegistryProviderKeys.YOUSEI_VARIANT)
                    .codec(YouseiVariant.CODEC)
                    .defaultId(ReverieDreams.id("blue"))
                    .builder(YouseiVariants::bootstrap);

    public static final RegistryProvider<RabbitUnitVariant> RABBIT_UNIT_VARIANT =
            BuiltInRegistryProviders.<RabbitUnitVariant>ofEntry(BuiltInRegistryProviderKeys.RABBIT_UNIT_VARIANT)
                    .codec(RabbitUnitVariant.CODEC)
                    .defaultId(ReverieDreams.id("blue"))
                    .builder(RabbitUnitVariants::bootstrap);

    public static final RegistryProvider<OniVariant> ONI_VARIANT =
            BuiltInRegistryProviders.<OniVariant>ofEntry(BuiltInRegistryProviderKeys.ONI_VARIANT)
                    .codec(OniVariant.CODEC)
                    .defaultId(ReverieDreams.id("blue"))
                    .builder(OniVariants::bootstrap);

    public static final RegistryProvider<FoodProperty> FOOD_PROPERTY =
            BuiltInRegistryProviders.<FoodProperty>ofEntry(BuiltInRegistryProviderKeys.FOOD_PROPERTY)
                    .codec(FoodProperty.BY_REGISTRY_CODEC)
                    .reloadBuilder(FoodProperties::reload)
                    .builder(FoodProperties::bootstrap)
                    .syncToClient(RegistrySyncers.FOOD_PROPERTY);

    public static final RegistryProvider<BeverageProperty> BEVERAGE_PROPERTY =
            BuiltInRegistryProviders.<BeverageProperty>ofEntry(BuiltInRegistryProviderKeys.BEVERAGE_PROPERTY)
                    .codec(BeverageProperty.COMPONENT_CODEC)
                    .reloadBuilder(BeverageProperties::reload)
                    .builder(BeverageProperties::bootstrap)
                    .syncToClient(RegistrySyncers.BEVERAGE_PROPERTY);

    public static final RegistryProvider<CraftingConflict> CRAFTING_CONFLICT =
            BuiltInRegistryProviders.<CraftingConflict>ofEntry(BuiltInRegistryProviderKeys.CRAFTING_CONFLICT)
                    .codec(CraftingConflict.CODEC)
                    .reloadBuilder(CraftingConflict::reload)
                    .builder(CraftingConflict::bootstrap)
                    .syncToClient(RegistrySyncers.CRAFTING_CONFLICT);

    public static final RegistryProvider<Customer> CUSTOMER =
            BuiltInRegistryProviders.<Customer>ofEntry(BuiltInRegistryProviderKeys.CUSTOMER)
                    .codec(Customer.CODEC)
                    .reloadBuilder(Customers::reload)
                    .builder(Customers::bootstrap)
                    .syncToClient(RegistrySyncers.CUSTOMER_DATA);

    public static final WritableRegistry<SkinType> SKIN_TYPE_MERGED =
            BuiltInRegistryProviders.ofEntries(BuiltInRegistryProviderKeys.SKIN_TYPE_MERGED, BuiltInRegistryProviders.SKIN_TYPE, BuiltInRegistryProviders.CUSTOM_SKIN_TYPE);

    public static final WritableRegistry<RoleType> ROLE_TYPE_MERGED =
            BuiltInRegistryProviders.ofEntries(BuiltInRegistryProviderKeys.ROLE_TYPE_MERGED, BuiltInRegistryProviders.NPC_ROLE_TYPE, BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE);


    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            RegistryProvider<?> registry = entry.getValue();
            registry.build();
        }
        LOOKUP = true;
    }

    public static void startSyncRegistry(List<ServerPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        Map<Identifier, RegistryProvider<?>> map = new Object2ObjectLinkedOpenHashMap<>();

        ROOT.forEach((key, object) -> {
            if (!(object instanceof RegistryProvider<?> registry)) {
                return;
            }
            if (!registry.isSyncToClient()) {
                return;
            }

            map.put(key.identifier(), registry);
        });

        List<CustomRegistrySyncPacket> payloads = buildPayload(map);

        for (ServerPlayer player : players) {
            for (CustomRegistrySyncPacket payload : payloads) {
                Balm.networking().sendTo(player, payload);
                log.info("Synchronizing registry {} for player {}", payload.registryKey(), player.getPlainTextName());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<CustomRegistrySyncPacket> buildPayload(
            Map<Identifier, RegistryProvider<?>> registryMap
    ) {
        List<CustomRegistrySyncPacket> payloads = new ArrayList<>(registryMap.size());

        for (Map.Entry<Identifier, RegistryProvider<?>> mapEntry : registryMap.entrySet()) {
            Identifier key = mapEntry.getKey();
            RegistryProvider<?> registry = mapEntry.getValue();

            if (key == null || registry == null)
                continue;

            RegistrySyncer syncer = registry.getSyncer();

            if (syncer == null) {
                continue;
            }

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

            CompoundTag data = syncer.writeToTag(entryList);
            Collection<HolderSet.Named<Object>> list = new ArrayList<>();
            registry.getTags().forEach(value -> list.add((HolderSet.Named<Object>) value));
            CompoundTag tag = RegistrySyncer.writeNamedToTag(list);
            payloads.add(new CustomRegistrySyncPacket(key, data, tag));
        }

        return payloads;
    }

    public static RegistryState getRecipeState() {
        return RECIPE_STATE;
    }

    public static void setRecipeState(RegistryState recipeState) {
        RECIPE_STATE = recipeState;
    }

    public static <T> T registerForBuiltin(RegistryProvider<T> registry, Identifier key, T value) {
        register(registry, key, value);
        registry.setBuiltin(key, value);
        return value;
    }

    public static <T> T register(RegistryProvider<T> registry, Identifier key, T value) {
        registry.register(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> T set(RegistryProvider<T> registry, Identifier key, T value) {
        registry.set(ResourceKey.create(registry.key(), key), value, RegistrationInfo.BUILT_IN);
        return value;
    }

    public static <T> RegistryProvider<T> ofEntry(Identifier identifier) {
        return ofEntry(ResourceKey.createRegistryKey(identifier));
    }

    public static synchronized <T> RegistryProvider<T> ofEntry(ResourceKey<? extends Registry<T>> key) {
        return (RegistryProvider<T>) ROOT.computeIfAbsent(key, inst -> {
            Optional<RegistryProviderFactory> registryProviderFactoryOptional = PlatformProxies.REGISTRY_PROVIDER_FACTORY;
            if (registryProviderFactoryOptional.isPresent()) {
                RegistryProviderFactory registryProviderFactory = registryProviderFactoryOptional.get();
                return registryProviderFactory.apply(key);
            }
            return null;
        });
    }

    public static <T> WritableRegistry<T> ofEntries(Identifier identifier, Registry... registries) {
        return ofEntries(ResourceKey.createRegistryKey(identifier), registries);
    }

    public static <T> WritableRegistry<T> ofEntries(ResourceKey<? extends Registry<T>> key, Registry... registries) {
        Optional<MergeRegistryProviderFactory> mergeRegistryProviderFactoryOptional = PlatformProxies.MERGE_REGISTRY_PROVIDER_FACTORY;
        if (mergeRegistryProviderFactoryOptional.isPresent()) {
            MergeRegistryProviderFactory mergeRegistryProviderFactory = mergeRegistryProviderFactoryOptional.get();
            return (MergeRegistry<T>) mergeRegistryProviderFactory.apply(key, new ArrayList<>(Arrays.stream(registries).toList()));
        }
        return null;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> getSuggestProvider(
            Command<CommandSourceStack> command
    ) {
        return Commands
                .argument("registry_key", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    for (ResourceKey<? extends Registry<?>> registryKey : BuiltInRegistryProviders.ROOT.keySet()) {
                        builder.suggest(registryKey.identifier().toString());
                    }
                    return builder.buildFuture();
                })
                .then(Commands.argument("id", IdentifierArgument.id())
                        .suggests((context, builder) -> {
                            Identifier identifier = IdentifierArgument.getId(context, "registry_key");

                            ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(identifier);

                            if (!BuiltInRegistryProviders.ROOT.containsKey(registryKey)) {
                                return builder.buildFuture();
                            }

                            RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);
                            for (Identifier key : registry.keys()) {
                                builder.suggest(key.toString());
                            }

                            return builder.buildFuture();
                        })
                        .executes(command)
                );
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Identifier> getSuggestTagProvider(
            Command<CommandSourceStack> command
    ) {
        return Commands
                .argument("registry_key", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    for (ResourceKey<? extends Registry<?>> registryKey : BuiltInRegistryProviders.ROOT.keySet()) {
                        builder.suggest(registryKey.identifier().toString());
                    }
                    return builder.buildFuture();
                })
                .then(Commands.argument("id", IdentifierArgument.id())
                        .suggests((context, builder) -> {
                            Identifier identifier = IdentifierArgument.getId(context, "registry_key");

                            ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(identifier);

                            if (!BuiltInRegistryProviders.ROOT.containsKey(registryKey)) {
                                return builder.buildFuture();
                            }

                            RegistryProvider<?> registry = BuiltInRegistryProviders.ROOT.get(registryKey);
                            Stream<? extends HolderSet.Named<?>> namedStream = registry.listTags();
                            namedStream.forEach((Consumer<HolderSet.Named<?>>) holders -> {
                                TagKey<?> key = holders.key();
                                builder.suggest(key.location().toString());
                            });

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
                    RegistryProvider<?> registry = ROOT.get(registryKey);
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
