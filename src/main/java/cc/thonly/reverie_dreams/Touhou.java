package cc.thonly.reverie_dreams;

import cc.thonly.minecraft.impl.ItemPostHitCallback;
import cc.thonly.reverie_dreams.armor.ModArmorMaterials;
import cc.thonly.reverie_dreams.block.BlockModels;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.compat.ModCompats;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.data.ModLootModifies;
import cc.thonly.reverie_dreams.data.ModServerResourceManager;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.datafixer.DataFixerContentManager;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.interfaces.IDreamPillowManager;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItemGroups;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.networking.CSVersionPayload;
import cc.thonly.reverie_dreams.networking.CustomBytePayload;
import cc.thonly.reverie_dreams.networking.HelloPayload;
import cc.thonly.reverie_dreams.networking.RegistrySyncPayload;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.Key2ValueRegistryManager;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.server.*;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.ModrinthAPI;
import cc.thonly.reverie_dreams.util.NetworkingUtils;
import cc.thonly.reverie_dreams.world.BiomeModificationInit;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import cc.thonly.reverie_dreams.world.gen.WorldGenerationInit;
import eu.midnightdust.lib.config.MidnightConfig;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class Touhou implements ModInitializer {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final FabricLoader FABRIC = FabricLoader.getInstance();
    public static final MappingResolver MAPPING_RESOLVER = FABRIC.getMappingResolver();
    public static final String VERSION = FABRIC
            .getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    public static final EnvType ENV_TYPE = FabricLoader.getInstance().getEnvironmentType();
    private static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    private static final boolean DEV_MODE = VERSION.contains("-dev.") || DEV_ENV;
    private static final boolean HAS_BUKKIT_API = isModLoaded("arclight") || isModLoaded("cardboard") || isModLoaded("banner");
    private static final boolean HAS_CONNECTOR = isModLoaded("connector");
    private static final boolean HAS_FORGE_API = isModLoaded("kilt");
    private static final boolean HAS_OPTIFINE = isModLoaded("optifabric");
    private static String SYSTEM_LANGUAGE = null;
    private static MinecraftServer server;
    @Getter
    private static DynamicRegistryManager dynamicRegistryManager;
    private static final Set<ServerPlayerEntity> playersWithMod = new HashSet<>();
    private static final Map<ServerPlayerEntity, String> playerSideVersion = new WeakHashMap<>();

    static {
        Locale locale = Locale.getDefault();
        SYSTEM_LANGUAGE = (locale.getLanguage() + "_" + locale.getCountry()).toLowerCase();
    }

    @Override
    public void onInitialize() {
        CardboardWarning.checkAndAnnounce();
        MidnightConfig.init(MOD_ID, ReverieDreamsConfiguration.class);
        if (isDevMode()) {
            LOGGER.warn("=====================================================");
            LOGGER.warn("You are using development version of Gensokyo: Reverie of Lost Dreams!");
            LOGGER.warn("Support is limited, as features might be unfinished!");
            LOGGER.warn("You are on your own!");
            LOGGER.warn("=====================================================");
        }
        if (hasForgeApi()) {
            LOGGER.warn("Cars cannot be placed on bicycles");
        }
        if (hasOptifine()) {
            LOGGER.warn("It must be Optifine’s error!");
        }
        if (isHasConnector()) {
            LOGGER.warn("It cannot connect...");
        }
        LOGGER.info("Loaded " + MOD_NAME);

        // 初始化静态注册表
        SoundEventInit.init();
        JukeboxSongInit.init();
        ModDataComponentTypes.init();
        ModArmorMaterials.init();
        ModGuiItems.init();
        ModBlockStateTemplates.bootstrap();
        ModBlockEntities.registerBlockEntities();
        ModBlocks.registerBlocks();
        BlockModels.registerModels();
        ModItems.registerItems();
        ModItemGroups.registerItemGroups();
        ModEntityHolders.registerHolders();
        ModEntities.registerEntities();
        ModStatusEffects.init();
        ModTags.loadTags();
        WorldGenerationInit.registerWorldGeneration();
        BiomeModificationInit.init();
        GameRulesInit.init();
        DataFixerContentManager.bootstrap();

        // 初始化其他注册内容
        CommandInit.init();
        RecipeManager.bootstrap();
        ModServerResourceManager.init();
        RegistryManager.bootstrap();
        Key2ValueRegistryManager.bootstrap();
        ModLootModifies.register();
        RecipeTypeCategoryManager.registerCategories();
        SpellCardTemplates.init();

        ImageToTextScanner.bootstrap();
        ItemDescriptionManager.bootstrap();

        CompletableFuture.runAsync(ItemStackCheckUtils::test);

        CompletableFuture.runAsync(() -> {
            ModrinthAPI.Entry latest = ModrinthAPI.get();
            if (latest == null) {
                LOGGER.error("Unable to check for new version");
                return;
            }
            if (VERSION.equals("unknown")) {
                LOGGER.error("Unable to detect local version number");
                return;
            }
            String versionNumber = latest.getVersion_number();

            int cmp = ModrinthAPI.compareVersion(versionNumber, VERSION);
            if (cmp > 0) {
                LOGGER.info("A newer version is available: " + versionNumber);
            } else if (cmp < 0) {
                LOGGER.info("You're using a newer version than latest: " + VERSION);
            } else {
                LOGGER.info("You're using the latest version: " + VERSION);
            }
        });

        CompletableFuture.runAsync(() -> {
            boolean reachable = NetworkingUtils.isReachable("textures.minecraft.net", 5000);
            if (!reachable) {
                LOGGER.error("Unable to connect to the Minecraft network, unexpected behavior may occur");
            }
        });

        PayloadTypeRegistry.playC2S().register(HelloPayload.PACKET_ID, HelloPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(HelloPayload.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player != null) {
                playersWithMod.add(player);
            }
        });
        PayloadTypeRegistry.playC2S().register(RegistrySyncPayload.PACKET_ID, RegistrySyncPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(RegistrySyncPayload.PACKET_ID, (payload, context) -> {
        });
        ServerPlayConnectionEvents.DISCONNECT.register((playNetworkHandler, server) -> {
            playersWithMod.remove(playNetworkHandler.player);
            playerSideVersion.remove(playNetworkHandler.player);
        });
        PayloadTypeRegistry.playC2S().register(CSVersionPayload.PACKET_ID, CSVersionPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(CSVersionPayload.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String version = payload.version();
            if (player != null) {
                playerSideVersion.put(player, version);
            }
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.getServer();
            if (server != null && target.getWorld() instanceof ServerWorld serverWorld && Unit.INSTANCE.equals(stack.getOrDefault(ModDataComponentTypes.SILVER_ITEM, null))) {
                DynamicRegistryManager.Immutable registryManager = server.getRegistryManager();
                Registry<EntityType<?>> entityTypes = registryManager.getOrThrow(RegistryKeys.ENTITY_TYPE);
                DamageSources damageSources = attacker.getDamageSources();
                for (RegistryEntry<EntityType<?>> iterateEntry : entityTypes.iterateEntries(EntityTypeTags.UNDEAD)) {
                    EntityType<?> value = iterateEntry.value();
                    if (target.getType() == value) {
                        target.lastDamageTaken = 0;
                        target.damage(serverWorld, damageSources.magic(), 2);
                        target.lastDamageTaken = 0;
                        break;
                    }
                }

            }
            return true;
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.getServer();
            if (server != null && target.getWorld() instanceof ServerWorld serverWorld && target.getType() == ModEntities.GHOST_ENTITY_TYPE && stack.getItem() == ModItems.ROKANKEN) {
                DamageSources damageSources = attacker.getDamageSources();
                target.lastDamageTaken = 0;
                target.damage(serverWorld, damageSources.magic(), Integer.MAX_VALUE);
            }
            return true;
        });

        PayloadTypeRegistry.playC2S().register(CustomBytePayload.PACKET_ID, CustomBytePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CustomBytePayload.PACKET_ID, CustomBytePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CustomBytePayload.PACKET_ID, CustomBytePayload.Receiver::receiveServer);

        ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, v) -> {
            return !livingEntity.hasStatusEffect(ModStatusEffects.ELIXIR_OF_LIFE);
        });
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            PlayerInputManager inputManager = PlayerInputManager.getInstance();
            inputManager.reload();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerLifecycleEvents.ServerStarted() {
            @Override
            public void onServerStarted(MinecraftServer server) {
                IDreamPillowManager iDreamPillowManager = (IDreamPillowManager) server;
                DreamPillowManager dreamPillowManager = iDreamPillowManager.getDreamPillowManager();
                dreamPillowManager.init();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerLifecycleEvents.ServerStopped() {
            @Override
            public void onServerStopped(MinecraftServer server) {
                IDreamPillowManager iDreamPillowManager = (IDreamPillowManager) server;
                DreamPillowManager dreamPillowManager = iDreamPillowManager.getDreamPillowManager();
                dreamPillowManager.save();
            }
        });
        ServerLifecycleEvents.AFTER_SAVE.register(new ServerLifecycleEvents.AfterSave() {
            @Override
            public void onAfterSave(MinecraftServer server, boolean flush, boolean force) {
                IDreamPillowManager iDreamPillowManager = (IDreamPillowManager) server;
                DreamPillowManager dreamPillowManager = iDreamPillowManager.getDreamPillowManager();
                dreamPillowManager.save();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(DelayedTask::tick);
        ServerTickEvents.END_SERVER_TICK.register(ParticleTickerManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(PlayerInputManager::tick);

        ModCompats.init();

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ResourcePackExtras.forDefault().addBridgedModelsFolder(id("block"), id("item"), id("entity"));
    }

    public static String getSystemLanguage() {
        return SYSTEM_LANGUAGE;
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static boolean isDevMode() {
        return DEV_MODE || ReverieDreamsConfiguration.DEBUG_MODE;
    }

    public static boolean isHasConnector() {
        return HAS_CONNECTOR;
    }

    public static boolean hasBukkitApi() {
        return HAS_BUKKIT_API;
    }

    public static boolean hasForgeApi() {
        return HAS_FORGE_API;
    }

    public static boolean hasOptifine() {
        return HAS_OPTIFINE;
    }

    public static boolean hasModOnClient(ServerPlayerEntity player) {
        if (player == null) return false;
        return playersWithMod.contains(player);
    }

    public static void setDynamicRegistryManager(DynamicRegistryManager dynamicRegistryManager) {
        Touhou.dynamicRegistryManager = dynamicRegistryManager;
    }

    public static void setServer(MinecraftServer server) {
        Touhou.server = server;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }


}