package cc.thonly.reverie_dreams;

import cc.thonly.keine.api.callback.AttackBlockCallback;
import cc.thonly.keine.api.callback.ItemAttackHitCallback;
import cc.thonly.keine.api.callback.ServerCallback;
import cc.thonly.keine.api.callback.ServerSavingCallback;
import cc.thonly.reverie_dreams.api.registry.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.creative_tab.RDCreativeTabs;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeFunctions;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogPlayerManager;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.loot.RDLootModifies;
import cc.thonly.reverie_dreams.networking.ServerNetworkingHandlers;
import cc.thonly.reverie_dreams.networking.payload.*;
import cc.thonly.reverie_dreams.proxy.PlatformProxies;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbenchRegistry;
import cc.thonly.reverie_dreams.registry.BuiltInBiRegistryProviders;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.ResourceReloadManager;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.armor.RDArmorMaterials;
import cc.thonly.reverie_dreams.registry.content.block.*;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.*;
import cc.thonly.reverie_dreams.registry.content.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.server.ParticleTickerManager;
import cc.thonly.reverie_dreams.server.ServerEventHandlers;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import cc.thonly.reverie_dreams.server.input.ServerPlayerInputManagerAccess;
import cc.thonly.reverie_dreams.server.nota.Nota;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.util.CardboardWarning;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.item.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.network.ModrinthAPI;
import cc.thonly.reverie_dreams.util.network.NetUtil;
import cc.thonly.reverie_dreams.world.BiomeModificationInit;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import cc.thonly.reverie_dreams.world.RDBuiltinWorldGenerations;
import dev.architectury.event.events.common.*;
import dev.architectury.networking.NetworkManager;
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
@Setter
@Getter
public class ReverieDreams {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random RD = new Random();
    public static final List<Runnable> COMMON_LATE_INIT = new ArrayList<>();
    public static final List<Runnable> BUS_LATE_INIT = new ArrayList<>();
    public static final List<Runnable> LATE_INIT_CLIENT = new ArrayList<>();
    public static final List<Block> SERVER_SIDE_BLOCKS = List.of(Blocks.NOTE_BLOCK, Blocks.TRIPWIRE);
    public static Function<ResourceKey<? extends Registry<?>>, RegistryProvider<?>> REGISTRY_GETTER = key -> null;
    public static BiFunction<ResourceKey<? extends Registry<?>>, List<Registry>, MergeRegistry<?>> MERGE_REGISTRY_GETTER = (key, list) -> null;
    public static BiFunction<ResourceKey<? extends Registry<?>>, RegistryProvider<?>, RegistryProvider<?>> REGISTRY_SHADOWER = null;
    private static MinecraftServer server;

    public static ReverieDreamsConfiguration config() {
        return AutoConfig
                .getConfigHolder(
                        ReverieDreamsConfiguration.class
                )
                .getConfig();
    }

    public static void initialize(Runnable lateInit) {
        AutoConfig.register(ReverieDreamsConfiguration.class, GsonConfigSerializer::new);
        CardboardWarning.checkAndAnnounce();
        if (PlatformContext.isDevMode()) {
            LOGGER.warn("=====================================================");
            LOGGER.warn("You are using development version of Gensokyo: Reverie of Lost Dreams!");
            LOGGER.warn("Support is limited, as features might be unfinished!");
            LOGGER.warn("You are on your own!");
            LOGGER.warn("=====================================================");
        }
        if (PlatformContext.hasOptifine()) {
            LOGGER.warn("You installed Optifine?!");
        }
        LOGGER.info("Loaded " + MOD_NAME);
        PlatformContext.FABRIC_POLYFACTORY_HAND_CRANK = Blocks.AIR;
        PlatformContext.FABRIC_CREATE_FLY_HAND_CRANK = Blocks.AIR;

        // 初始化静态注册表
        Nota.initialize();
        JukeboxSongInit.initialize();
        RDArmorMaterials.initialize();
        RDBlockStateTemplates.initialize();
        RDEnchantments.registerEnchantments();
        RDSoundEvents.initialize();
        RDDataComponentTypes.initialize();
        RDGuiPlaceholderItems.initialize();
        RDItems.initialize();
        RDIngredientItems.initialize();
        RDCuisineItems.initialize();
        RDBeverageItems.initialize();
        RDEntityHolderItems.initialize();
        RDBlocks.initialize();
        RDWoodBlocks.initialize();
        RDCropBlocks.initialize();
        RDPlantBlocks.initialize();
        RDKitchenBlocks.initialize();
        RDBlockEntityTypes.initialize();
        RDEntityTypes.initialize();
        RDStatusEffects.initialize();
        RDPotions.initialize();
        RDPointOfInterestTypes.initialize();
        RDVillagerProfessions.initialize();
        RDCriteriaTriggers.initialize();
        RDBuiltInGameRules.initialize();
        RDCreativeTabs.initialize();
        RDBuiltinWorldGenerations.registerWorldGeneration();
        BiomeModificationInit.initialize();
        MCBuiltInRegistries.register();

        // 初始化其他注册内容
        PlatformProxies.initialize();
        RecipeManager.bootstrap();
        RecipeWorkbenchRegistry.bootstrap();
        ResourceReloadManager.initialize();
        BuiltInRegistryProviders.bootstrap();
        FoodProperties.registerDefaultItemUsingProperty();
        BeverageProperties.registerDefaultItemUsingProperty();
        BuiltInBiRegistryProviders.bootstrap();
        RDLootModifies.register();
        RecipeTypeCategoryManager.registerCategories();
        DanmakuTemplates.initialize();
        CustomClickActionRegistry.initialize();
        DefaultBookPages.initialize();
        KeyframeFunctions.bootstrap();
        InitTooltips.bootstrap();

        ImageToTextScanner.bootstrap();
        PlayerComponentRegistry.registerDefaultComponents();

        loadCompletableEvent();
        registerNetworkingEvent();
        registerServerEvents();
        registerContentEvent();
        registerEntityDataSerializers();

        lateInit.run();
    }

    private static void registerEntityDataSerializers() {
        EntityDataSerializerProviders providers = EntityDataSerializerProviders.get();
        providers.add(id("danmaku_properties"), DanmakuProperties.SERIALIZER);
        providers.add(id("skin_type"), SkinType.SERIALIZER);
        providers.add(id("npc_role_type"), NPCRoleType.SERIALIZER);
        providers.add(id("role_type"), RoleType.SERIALIZER);
        providers.add(id("ingredient_stack"), IngredientStack.SERIALIZER);
        providers.add(id("ingredient_stacks"), IngredientStack.LIST_SERIALIZER);
    }

    private static void registerContentEvent() {
        EntityEvent.LIVING_DEATH.register(CommonEventHandlers::onLivingEntityDeathByDanmaku);
//        EntityEvent.LIVING_DEATH.register(CommonEventHandlers::onLivingEntityDeathByElixirOfLife);
        EntityEvent.LIVING_HURT.register(CommonEventHandlers::onModifyingLivingEntityDamageByUndeadSilverDamage);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostHitBySilverWeapon);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostByFrozenEnchantment);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostByChargeEnchantment);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostHitByInstantKillGhost);
        AttackBlockCallback.EVENT.register(CommonEventHandlers::onAttackingBlockChangeCameraFov);
        AttackBlockCallback.EVENT.register(CommonEventHandlers::onChangingMusicalInstrumentMusic);
        PlayerEvent.ATTACK_ENTITY.register(CommonEventHandlers::onChangingMusicalInstrumentMusic);
    }

    private static void registerNetworkingEvent() {
        registerClientboundPackets();
        registerServerboundPackets();
    }

    public static void registerClientboundPackets() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                RecipeManagerSyncPacket.PACKET_ID,
                RecipeManagerSyncPacket.CODEC,
                (packet, context) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveRecipeManagerSyncPacket(
                                        context.getPlayer(),
                                        packet
                                )
                        )
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                CustomRegistrySyncPacket.PACKET_ID,
                CustomRegistrySyncPacket.CODEC,
                (packet, context) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveCustomRegistrySyncPacket(
                                        context.getPlayer(),
                                        packet
                                )
                        )
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SyncEntityPacket.PACKET_ID,
                SyncEntityPacket.CODEC,
                (packet, context) -> ClientNetworkingHandlers.onReceiveSyncEntityPacket(context.getPlayer(), packet)
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                StartScreenshotPacket.PACKET_ID,
                StartScreenshotPacket.CODEC,
                (packet, context) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveStartScreenshotPacket(
                                        context.getPlayer(),
                                        packet
                                )
                        )
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                PlayerComponentUpdatePacket.PACKET_ID,
                PlayerComponentUpdatePacket.CODEC,
                (packet, context) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceivePlayerComponentUpdatePacket(
                                        context.getPlayer(),
                                        packet
                                )
                        )
        );
    }

    public static void registerServerboundPackets() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                HelloPacket.PACKET_ID,
                HelloPacket.CODEC,
                (packet, context) -> ServerNetworkingHandlers.onReceiveHelloPacket((ServerPlayer) context.getPlayer(), packet)
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PlayerMidiNotePacket.PACKET_ID,
                PlayerMidiNotePacket.CODEC,
                (packet, context) -> ServerNetworkingHandlers.onReceivePlayerMidiNotePacket((ServerPlayer) context.getPlayer(), packet)
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PlayerJoinVersionPacket.PACKET_ID,
                PlayerJoinVersionPacket.CODEC,
                (packet, context) -> ServerNetworkingHandlers.onReceiveHelloPacket((ServerPlayer) context.getPlayer(), packet)
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ScreenshotMapPacket.PACKET_ID,
                ScreenshotMapPacket.CODEC,
                (packet, context) -> ServerNetworkingHandlers.onReceiveScreenshotMapPacket((ServerPlayer) context.getPlayer(), packet)
        );
    }

    private static void registerServerEvents() {
        CommandRegistrationEvent.EVENT.register(CommandInit::registerCommand);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinByModUpdateCheck);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinByCreateComponent);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinBySync);
        PlayerEvent.PLAYER_QUIT.register(ServerEventHandlers::onPlayerDisconnectionBySavingComponent);
        PlayerEvent.PLAYER_QUIT.register(ServerEventHandlers::onPlayerDisconnectionByRemoveModClient);
        LifecycleEvent.SERVER_STARTED.register(ServerEventHandlers::onServerStarted);
        ServerSavingCallback.AFTER.register(ServerEventHandlers::onServerSavingAfter);
        ServerCallback.RELOADING.register(ServerEventHandlers::onServerReloading);
        ServerCallback.RELOADED.register(ServerEventHandlers::onServerReloaded);
        TickEvent.SERVER_POST.register(DelayedTask::tick);
        TickEvent.SERVER_POST.register(ServerPlayerComponentManager::tickByServer);
        TickEvent.SERVER_POST.register(ParticleTickerManager::tick);
        TickEvent.SERVER_POST.register(ServerPlayerInputManagerAccess::tick);
        TickEvent.SERVER_POST.register(DanmakuScriptManager::onTick);
        TickEvent.SERVER_POST.register(DialogPlayerManager::tick);
        TickEvent.SERVER_POST.register(SpellcardRenderer::tick);
    }

    private static void loadCompletableEvent() {
        CompletableFuture.runAsync(ItemStackCheckUtils::test);
        CompletableFuture.runAsync(() -> {
            ModrinthAPI.Entry latest = ModrinthAPI.get();
            if (latest == null) {
                LOGGER.error("Unable to check for new version");
                return;
            }
            if (PlatformContext.VERSION.get().equals("unknown")) {
                LOGGER.error("Unable to detect local version number");
                return;
            }
            String versionNumber = latest.getVersion_number();

            int cmp = ModrinthAPI.compareVersion(versionNumber, PlatformContext.VERSION.get());
            if (cmp > 0) {
                LOGGER.info("A newer version is available: {}", versionNumber);
                PlatformContext.LATEST_VERSION = versionNumber;
            } else if (cmp < 0) {
                LOGGER.info("You're using a newer version than latest: {}", PlatformContext.VERSION.get());
            } else {
                LOGGER.info("You're using the latest version: {}", PlatformContext.VERSION.get());
            }
        });

        CompletableFuture.runAsync(() -> {
            String testUrl = "https://textures.minecraft.net/texture/7fd9ba42a7c81eeea22f1524271ae85a8e045ce0af5a6ae16c6406ae917e68b5";
            boolean reachable = NetUtil.isUrlAccessible(testUrl);
            if (!reachable) {
                LOGGER.error("Unable to connect to the Minecraft network, unexpected behavior may occur");
            }
        });

        CompletableFuture.runAsync(() -> {
            boolean contain = DialogFiles.contain("badapple.json");
            if (!contain) {
                try {
                    NetUtil.downloadFile("https://www.otomads.top/reverie_dreams/badapple.json", DialogFiles.PATH.resolve("badapple.json").toFile());
                } catch (Exception err) {
                    LOGGER.error("Can't download badapple.json", err);
                }
            }
        });
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public static void setServer(MinecraftServer server) {
        ReverieDreams.server = server;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }


}