package cc.thonly.reverie_dreams;

import cc.thonly.keine.api.callback.AttackBlockCallback;
import cc.thonly.keine.api.callback.ItemAttackHitCallback;
import cc.thonly.keine.api.callback.ServerCallback;
import cc.thonly.keine.api.callback.ServerSavingCallback;
import cc.thonly.reverie_dreams.api.registry.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.api.registry.NetworkManager;
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
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
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
import lombok.Getter;
import lombok.Setter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.network.BalmNetworking;
import net.blay09.mods.balm.platform.event.callback.*;
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
import java.util.concurrent.CopyOnWriteArrayList;
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
    public static final List<Runnable> COMMON_LATE_INIT = new CopyOnWriteArrayList<>();
    public static final List<Runnable> BUS_LATE_INIT = new CopyOnWriteArrayList<>();
    public static final List<Runnable> LATE_INIT_CLIENT = new CopyOnWriteArrayList<>();
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

        MCBuiltInRegistries.register();
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
        LivingEntityCallback.Death.Before.EVENT.register((livingEntity, damageSource) -> {
            CommonEventHandlers.onLivingEntityDeathByDanmaku(livingEntity, damageSource);
            return true;
        });
//        EntityEvent.LIVING_DEATH.register(CommonEventHandlers::onLivingEntityDeathByElixirOfLife);
        LivingEntityCallback.Damage.Before.EVENT.register((livingEntity, damageSource, amount) -> {
            CommonEventHandlers.onModifyingLivingEntityDamageByUndeadSilverDamage(livingEntity, damageSource, amount);
            return amount;
        });
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostHitBySilverWeapon);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostByFrozenEnchantment);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostByChargeEnchantment);
        ItemAttackHitCallback.EVENT.register(CommonEventHandlers::onPostHitByInstantKillGhost);
        AttackBlockCallback.EVENT.register(CommonEventHandlers::onAttackingBlockChangeCameraFov);
        AttackBlockCallback.EVENT.register(CommonEventHandlers::onChangingMusicalInstrumentMusic);
        PlayerCallback.Attack.Before.EVENT.register((player, target) -> {
            CommonEventHandlers.onChangingMusicalInstrumentMusic(player, target);
            return true;
        });
    }

    private static void registerNetworkingEvent() {
        registerClientboundPackets();
        registerServerboundPackets();
    }

    public static void registerClientboundPackets() {
        NetworkManager.registerClientboundPacket(
                RecipeManagerSyncPacket.PACKET_ID,
                RecipeManagerSyncPacket.class,
                RecipeManagerSyncPacket.CODEC,
                (player, packet) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveRecipeManagerSyncPacket(
                                        player,
                                        packet
                                )
                        )
        );
        NetworkManager.registerClientboundPacket(
                CustomRegistrySyncPacket.PACKET_ID,
                CustomRegistrySyncPacket.class,
                CustomRegistrySyncPacket.CODEC,
                (player, packet) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveCustomRegistrySyncPacket(
                                        player,
                                        packet
                                )
                        )
        );

        NetworkManager.registerClientboundPacket(
                SyncEntityPacket.PACKET_ID,
                SyncEntityPacket.class,
                SyncEntityPacket.CODEC,
                (player, packet) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveSyncEntityPacket(
                                        player,
                                        packet
                                )
                        )
        );
        NetworkManager.registerClientboundPacket(
                StartScreenshotPacket.PACKET_ID,
                StartScreenshotPacket.class,
                StartScreenshotPacket.CODEC,
                (player, packet) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceiveStartScreenshotPacket(
                                        player,
                                        packet
                                )
                        )
        );
        NetworkManager.registerClientboundPacket(
                PlayerComponentUpdatePacket.PACKET_ID,
                PlayerComponentUpdatePacket.class,
                PlayerComponentUpdatePacket.CODEC,
                (player, packet) ->
                        ClientNetworkingHandlers.safeHandleClient(
                                () -> ClientNetworkingHandlers.onReceivePlayerComponentUpdatePacket(
                                        player,
                                        packet
                                )
                        )
        );
    }

    public static void registerServerboundPackets() {
        NetworkManager.registerServerboundPacket(
                HelloPacket.PACKET_ID,
                HelloPacket.class,
                HelloPacket.CODEC,
                (player, packet) ->
                        ServerNetworkingHandlers.onReceiveHelloPacket(
                                (ServerPlayer) player,
                                packet
                        )
        );
        NetworkManager.registerServerboundPacket(
                PlayerMidiNotePacket.PACKET_ID,
                PlayerMidiNotePacket.class,
                PlayerMidiNotePacket.CODEC,
                (player, packet) ->
                        ServerNetworkingHandlers.onReceivePlayerMidiNotePacket(
                                (ServerPlayer) player,
                                packet
                        )
        );
        NetworkManager.registerServerboundPacket(
                PlayerJoinVersionPacket.PACKET_ID,
                PlayerJoinVersionPacket.class,
                PlayerJoinVersionPacket.CODEC,
                (player, packet) ->
                        ServerNetworkingHandlers.onReceiveHelloPacket(
                                (ServerPlayer) player,
                                packet
                        )
        );
        NetworkManager.registerServerboundPacket(
                ScreenshotMapPacket.PACKET_ID,
                ScreenshotMapPacket.class,
                ScreenshotMapPacket.CODEC,
                (player, packet) ->
                        ServerNetworkingHandlers.onReceiveScreenshotMapPacket(
                                (ServerPlayer) player,
                                packet
                        )
        );
    }

    private static void registerServerEvents() {
        ServerPlayerCallback.Join.EVENT.register(ServerEventHandlers::onPlayerJoinByModUpdateCheck);
        ServerPlayerCallback.Join.EVENT.register(ServerEventHandlers::onPlayerJoinByCreateComponent);
        ServerPlayerCallback.Join.EVENT.register(ServerEventHandlers::onPlayerJoinBySync);
        ServerPlayerCallback.Leave.EVENT.register(ServerEventHandlers::onPlayerDisconnectionBySavingComponent);
        ServerPlayerCallback.Leave.EVENT.register(ServerEventHandlers::onPlayerDisconnectionByRemoveModClient);
        ServerLifecycleCallback.Started.EVENT.register(ServerEventHandlers::onServerStarted);
        ServerSavingCallback.AFTER.register(ServerEventHandlers::onServerSavingAfter);
        ServerCallback.RELOADING.register(ServerEventHandlers::onServerReloading);
        ServerCallback.RELOADED.register(ServerEventHandlers::onServerReloaded);
        ServerTickCallback.AFTER.register(DelayedTask::tick);
        ServerTickCallback.AFTER.register(ServerPlayerComponentManager::tickByServer);
        ServerTickCallback.AFTER.register(ParticleTickerManager::tick);
        ServerTickCallback.AFTER.register(ServerPlayerInputManagerAccess::tick);
        ServerTickCallback.AFTER.register(DanmakuScriptManager::onTick);
        ServerTickCallback.AFTER.register(DialogPlayerManager::tick);
        ServerTickCallback.AFTER.register(SpellcardRenderer::tick);
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