package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.event.CommonEventHandlers;
import cc.thonly.reverie_dreams.event.block.RecordPlayerEventHandlers;

import cc.thonly.reverie_dreams.api.item.callback.ItemEvent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManagerAccess;
import cc.thonly.reverie_dreams.api.registry.BookPageManager;
import cc.thonly.reverie_dreams.api.registry.entity.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderContext;
import cc.thonly.reverie_dreams.api.server.ServerEvents;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.compat.ysm.NPCModelSelection;
import cc.thonly.reverie_dreams.command.CommandRegistry;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.creative_tab.RDCreativeTabs;
import cc.thonly.reverie_dreams.event.block.FumoBlockEventHandlers;
import cc.thonly.reverie_dreams.event.block.PlateBlockEventHandlers;
import cc.thonly.reverie_dreams.event.block.RakedGravelEventHandlers;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardRuntime;
import cc.thonly.reverie_dreams.data.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeFunctions;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuDirector;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.data.npc.RoleType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.server.dialog.NPCSpeech;
import cc.thonly.reverie_dreams.server.dialog.video.DialogFiles;
import cc.thonly.reverie_dreams.server.dialog.video.DialogPlayerManager;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.debug.BattleStickItem;
import cc.thonly.reverie_dreams.item.weapon.Gungnir;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
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
import cc.thonly.reverie_dreams.registry.impl.RegistryProviderContextImpl;
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
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.BookPageManagerImpl;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.server.GensokyoCompassNavigation;
import cc.thonly.reverie_dreams.server.ParticleTickerManager;
import cc.thonly.reverie_dreams.server.ServerEventHandlers;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import cc.thonly.reverie_dreams.server.input.ServerPlayerInputManagerAccess;
import cc.thonly.reverie_dreams.server.nota.Nota;
import cc.thonly.reverie_dreams.registry.content.sound.RDJukeboxSongs;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.registry.content.block.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.util.CardboardWarning;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.item.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.item.ItemTagMigration;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import cc.thonly.reverie_dreams.board.BoardGames;
import cc.thonly.reverie_dreams.event.block.WorkshopRackEventHandlers;
import cc.thonly.reverie_dreams.item.weapon.YoumuSwordCombat;
import cc.thonly.reverie_dreams.world.moon.signal.MoonSignalTowers;

@Setter
@Getter
public class ReverieDreams {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random RD = ThreadLocalRandom.current();
    public static final List<Runnable> COMMON_LATE_INIT = new ArrayList<>();
    public static final List<Runnable> BUS_LATE_INIT = new ArrayList<>();
    public static final List<Runnable> LATE_INIT_CLIENT = new ArrayList<>();
    public static final List<Block> SERVER_SIDE_BLOCKS = List.of(Blocks.NOTE_BLOCK, Blocks.TRIPWIRE);
    private static MinecraftServer server;

    public static ReverieDreamsConfiguration config() {
        return AutoConfig.getConfigHolder(ReverieDreamsConfiguration.class)
                .getConfig();
    }

    public static void initialize(Runnable platformInitAct) {
        AutoConfig.register(ReverieDreamsConfiguration.class, GsonConfigSerializer::new);
        cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateTickets.initialize();
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
        RDJukeboxSongs.initialize();
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
        RDCuisineBlocks.initialize();
        RDWoodBlocks.initialize();
        RDWoolBlocks.initialize();
        RDCropBlocks.initialize();
        RDPlantBlocks.initialize();
        RDKitchenBlocks.initialize();
        RDBlockEntityTypes.initialize();
        RDEntityTypes.initialize();
        RDAquaticItems.initialize();
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
        RegistryProviderContext.setContext(new RegistryProviderContextImpl());
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
        BookPageManager.setInstance(BookPageManagerImpl.getInstance());
        DefaultBookPages.initialize();
        KeyframeFunctions.bootstrap();
        InitTooltips.bootstrap();

        ImageToTextScanner.bootstrap();
        PlayerComponentManagerAccess.registerServer(ServerPlayerComponentManager::serverAccess);
        PlayerComponentRegistry.registerDefaultComponents();

        // 注册内容全部就绪后，才允许在 ItemStack 反序列化时清理旧版本标签
        COMMON_LATE_INIT.add(ItemTagMigration::setReady);

        loadCompletableEvent();
        registerNetworkingEvent();
        registerServerEvents();
        registerGameplayEvents();
        registerEntityDataSerializers();

        platformInitAct.run();
    }

    private static void registerEntityDataSerializers() {
        EntityDataSerializerProviders providers = EntityDataSerializerProviders.get();
        providers.add(id("danmaku_properties"), DanmakuProperties.SERIALIZER);
        providers.add(id("skin_type"), SkinType.SERIALIZER);
        providers.add(id("npc_role_type"), NPCRoleType.SERIALIZER);
        providers.add(id("role_type"), RoleType.SERIALIZER);
        providers.add(id("ingredient_stack"), IngredientStack.SERIALIZER);
        providers.add(id("ingredient_stacks"), IngredientStack.LIST_SERIALIZER);
        providers.add(id("danmaku_motion"), DanmakuMotion.SERIALIZER);
    }

    private static void registerGameplayEvents() {
        MoonSignalTowers.initialize();
        EntityEvent.LIVING_DEATH.register(CommonEventHandlers::onLivingEntityDeathByDanmaku);
        EntityEvent.LIVING_HURT.register(CommonEventHandlers::onModifyingLivingEntityDamageByUndeadSilverDamage);
        ItemEvent.ITEM_ATTACK_HIT.register(CommonEventHandlers::onPostByFrozenEnchantment);
        ItemEvent.ITEM_ATTACK_HIT.register(CommonEventHandlers::onPostByChargeEnchantment);
        ItemEvent.ITEM_HIT_BLOCK.register(CommonEventHandlers::onAttackingBlockChangeCameraFov);
        ItemEvent.ITEM_HIT_BLOCK.register(CommonEventHandlers::onChangingMusicalInstrumentMusic);
        PlayerEvent.ATTACK_ENTITY.register(CommonEventHandlers::onChangingMusicalInstrumentMusic);
        PlayerEvent.ATTACK_ENTITY.register(YoumuSwordCombat::captureAttack);
        TickEvent.PLAYER_POST.register(YoumuSwordCombat::tick);
        TickEvent.PLAYER_POST.register(CommonEventHandlers::onUseYukaFlowerUmbrella);
        TickEvent.PLAYER_POST.register(GensokyoCompassNavigation::tick);

        InteractionEvent.RIGHT_CLICK_BLOCK.register(RakedGravelEventHandlers::onRightClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(FumoBlockEventHandlers::onRightClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(PlateBlockEventHandlers::onRightClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(WorkshopRackEventHandlers::onRightClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(RecordPlayerEventHandlers::onRightClickBlock);
        InteractionEvent.INTERACT_ENTITY.register(BattleStickItem::onInteractEntity);
        ChunkEvent.LOAD.register(FumoBlockEventHandlers::onChunkLoad);
    }

    private static void registerNetworkingEvent() {
        registerClientboundPackets();
        registerServerboundPackets();
        NPCModelSelection.registerPackets();
    }

    public static void registerClientboundPackets() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                LunarReloadPosePacket.PACKET_ID,
                LunarReloadPosePacket.CODEC,
                (packet, context) -> ClientNetworkingHandlers.safeHandleClient(
                        () -> ClientNetworkingHandlers.onReceiveLunarReloadPose(packet))
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                LunarShotPosePacket.PACKET_ID,
                LunarShotPosePacket.CODEC,
                (packet, context) -> ClientNetworkingHandlers.safeHandleClient(
                        () -> ClientNetworkingHandlers.onReceiveLunarShotPose(packet))
        );
        for (var type : List.of(RecipeManagerSyncPacket.PACKET_ID, RecipeManagerSyncPacket.BATCH_PACKET_ID)) {
            NetworkManager.registerReceiver(
                    NetworkManager.Side.S2C,
                    type,
                    RecipeManagerSyncPacket.CODEC,
                    (packet, context) -> ClientNetworkingHandlers.safeHandleClient(
                            () -> ClientNetworkingHandlers.onReceiveRecipeManagerSyncPacket(context.getPlayer(), packet))
            );
        }
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
                                () -> ClientNetworkingHandlers.onReceivePlayerComponentUpdatePacket(packet)
                        )
        );
    }

    public static void registerServerboundPackets() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                GungnirThrowPacket.PACKET_ID,
                GungnirThrowPacket.CODEC,
                (packet, context) -> context.queue(() -> Gungnir.handleChargeThrow((ServerPlayer) context.getPlayer()))
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                LunarAimFirePacket.PACKET_ID,
                LunarAimFirePacket.CODEC,
                (packet, context) -> context.queue(() -> WeaponOfTheMoon.handleAimInput((ServerPlayer) context.getPlayer(), packet.fire()))
        );
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
        CommandRegistrationEvent.EVENT.register(CommandRegistry::registerCommand);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinByModUpdateCheck);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinByCreateComponent);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinBySync);
        PlayerEvent.PLAYER_JOIN.register(ServerEventHandlers::onPlayerJoinByInstallGensokyo);
        PlayerEvent.CHANGE_DIMENSION.register(ServerEventHandlers::onPlayerChangeDimension);
        PlayerEvent.PLAYER_QUIT.register(ServerEventHandlers::onPlayerDisconnectionBySavingComponent);
        PlayerEvent.PLAYER_QUIT.register(ServerEventHandlers::onPlayerDisconnectionByRemoveModClient);
        LifecycleEvent.SERVER_STARTED.register(ServerEventHandlers::onServerStarted);
        LifecycleEvent.SERVER_STOPPING.register(ServerEventHandlers::onServerStopping);
        LifecycleEvent.SERVER_STOPPING.register(YoumuSwordCombat::clear);
        LifecycleEvent.SERVER_STOPPING.register(BoardGames::clear);
        LifecycleEvent.SERVER_STOPPING.register(NPCSpeech::clear);
        LifecycleEvent.SERVER_STOPPING.register(GensokyoCompassNavigation::clear);
        LifecycleEvent.SERVER_STOPPED.register(cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService::stopped);
        PlayerEvent.PLAYER_QUIT.register(GensokyoCompassNavigation::remove);
        ServerEvents.SAVING_AFTER.register(ServerEventHandlers::onServerSavingAfter);
        ServerEvents.RELOADING.register(ServerEventHandlers::onServerReloading);
        ServerEvents.RELOADED.register(ServerEventHandlers::onServerReloaded);

        TickEvent.SERVER_POST.register(ServerEventHandlers::onServerTickInstallGensokyo);
        TickEvent.SERVER_POST.register(DelayedTask::tick);
        TickEvent.SERVER_POST.register(ServerPlayerComponentManager::tickByServer);
        TickEvent.SERVER_POST.register(ParticleTickerManager::tick);
        TickEvent.SERVER_POST.register(ServerPlayerInputManagerAccess::tick);
        TickEvent.SERVER_POST.register(DanmakuScriptManager::onTick);
        TickEvent.SERVER_POST.register(DialogPlayerManager::tick);
        TickEvent.SERVER_POST.register(NPCSpeech::tick);
        TickEvent.SERVER_POST.register(LegacySpellcardRuntime::tick);
        TickEvent.SERVER_POST.register(DanmakuDirector::tick);
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
