package cc.thonly.reverie_dreams;

import cc.thonly.minecraft.api.ItemPostHitCallback;
import cc.thonly.polymer.PolymerEntityHelper;
import cc.thonly.reverie_dreams.armor.ModArmorMaterials;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.compat.ModCompats;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.ModLootModifies;
import cc.thonly.reverie_dreams.data.ModServerResourceManager;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.datafixer.DataFixerContentManager;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.villager.ModPointOfInterestTypes;
import cc.thonly.reverie_dreams.entity.villager.ModVillagerProfessions;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.networking.CSVersionPayload;
import cc.thonly.reverie_dreams.networking.HelloPayload;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.Key2ValueRegistryManager;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.server.*;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.server.player.PlayerDataComponentManager;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.util.*;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import cc.thonly.reverie_dreams.util.item.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.network.ModrinthAPI;
import cc.thonly.reverie_dreams.util.network.NetUtil;
import cc.thonly.reverie_dreams.world.BiomeModificationInit;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import cc.thonly.reverie_dreams.world.gen.WorldGenerationInit;
import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class Touhou implements ModInitializer {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer server;
    @Getter
    private static DynamicRegistryManager dynamicRegistryManager;
    private static final Set<ServerPlayerEntity> PLAYER_WITH_MOD = new HashSet<>();
    private static final Map<ServerPlayerEntity, String> PLAYER_SIDE_VERSION = new WeakHashMap<>();

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, ReverieDreamsConfiguration.class);
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
        ModItems.registerItems();
        ModEntityHolders.registerHolders();
        ModEntities.registerEntities();
        ModStatusEffects.init();
        ModTags.loadTags();
        WorldGenerationInit.registerWorldGeneration();
        ModPointOfInterestTypes.registers();
        ModVillagerProfessions.registers();
        BiomeModificationInit.init();
        GameRulesInit.init();
        DataFixerContentManager.bootstrap();
        DialogInit.bootstrap();

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
        PlayerDataComponentManager.registers();

        this.loadCompletableEvent();
        this.registerNetworkingEvent();
        this.registerServerEvents();

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

        ModCompats.init();
    }

    private void registerNetworkingEvent() {
        PayloadTypeRegistry.playC2S().register(HelloPayload.PACKET_ID, HelloPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(HelloPayload.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player != null) {
                PLAYER_WITH_MOD.add(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((playNetworkHandler, server) -> {
            PLAYER_WITH_MOD.remove(playNetworkHandler.player);
        });
        PayloadTypeRegistry.playC2S().register(CSVersionPayload.PACKET_ID, CSVersionPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(CSVersionPayload.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String version = payload.version();
            if (player != null) {
                PLAYER_SIDE_VERSION.put(player, version);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((playNetworkHandler, server) -> {
            PLAYER_SIDE_VERSION.remove(playNetworkHandler.player);
        });
    }

    @SuppressWarnings("rawtypes")
    private void registerServerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerDataComponentManager componentManager = PlayerDataComponentManager.getInstance();
            for (Map.Entry<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>> mapEntry : PlayerDataComponentManager.getComponents()) {
                Class<PlayerComponent<? extends PlayerComponent>> key = mapEntry.getKey();
                componentManager.getOrCreatePlayerComponent(player, key);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, server) -> {
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            playerDataComponentManager.saveAll();
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
                playerDataComponentManager.onLoad(server);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, minecraftServer) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!ReverieDreamsConfiguration.CHECK_UPDATE) {
                return;
            }
            if (!player.hasPermissionLevel(2)) {
                return;
            }
            if (ConstantInfo.LATEST_VERSION == null) {
                return;
            }
            MutableText mutableText = Text.empty();
            mutableText.append(Text.translatable("message.reverie_dreams.update", ConstantInfo.LATEST_VERSION));
            mutableText.append(" §r[");
            mutableText.append(Text.translatable("item.action.click.left").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/gensokyo-reverie-of-lost-dreams")))));
            mutableText.append("§r]");
            player.sendMessage(mutableText, false);
        });
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

            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerLifecycleEvents.ServerStarted() {
            @Override
            public void onServerStarted(MinecraftServer server) {
                DialogPlayer.reload();
            }
        });
        ServerLifecycleEvents.AFTER_SAVE.register(new ServerLifecycleEvents.AfterSave() {
            @Override
            public void onAfterSave(MinecraftServer server, boolean flush, boolean force) {
                PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
                playerDataComponentManager.saveAll();
            }
        });
//        ServerTickEvents.END_SERVER_TICK.register(server -> {
//            System.out.println(PolymerEntityHelper.ELEMENTS.size());
//        });
        ServerTickEvents.END_SERVER_TICK.register(DelayedTask::tick);
        ServerTickEvents.END_SERVER_TICK.register(ArmorAttributeManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(PlayerDataComponentManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(ParticleTickerManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(PlayerInputManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(DanmakuScriptManager::onTick);
        ServerTickEvents.END_SERVER_TICK.register(DialogPlayer::tick);
    }

    private void loadCompletableEvent() {
        CompletableFuture.runAsync(ItemStackCheckUtils::test);

        CompletableFuture.runAsync(() -> {
            ModrinthAPI.Entry latest = ModrinthAPI.get();
            if (latest == null) {
                LOGGER.error("Unable to check for new version");
                return;
            }
            if (ConstantInfo.VERSION.equals("unknown")) {
                LOGGER.error("Unable to detect local version number");
                return;
            }
            String versionNumber = latest.getVersion_number();

            int cmp = ModrinthAPI.compareVersion(versionNumber, ConstantInfo.VERSION);
            if (cmp > 0) {
                LOGGER.info("A newer version is available: {}", versionNumber);
                ConstantInfo.LATEST_VERSION = versionNumber;
            } else if (cmp < 0) {
                LOGGER.info("You're using a newer version than latest: {}", ConstantInfo.VERSION);
            } else {
                LOGGER.info("You're using the latest version: {}", ConstantInfo.VERSION);
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

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static boolean hasModOnClient(ServerPlayerEntity player) {
        if (player == null) return false;
        return PLAYER_WITH_MOD.contains(player);
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