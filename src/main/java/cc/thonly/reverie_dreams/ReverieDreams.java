package cc.thonly.reverie_dreams;

import cc.thonly.minecraft.api.ItemPostHitCallback;
import cc.thonly.reverie_dreams.registry.content.DrinkProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.armor.RDArmorMaterials;
import cc.thonly.reverie_dreams.registry.content.block.*;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.data.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.loot.RDLootModifies;
import cc.thonly.reverie_dreams.registry.content.item.*;
import cc.thonly.reverie_dreams.registry.impl.ServerResourceHelper;
import cc.thonly.reverie_dreams.datafixer.DataFixerContentManager;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.entity.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.entity.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.networking.CSVersionPayload;
import cc.thonly.reverie_dreams.networking.HelloPayload;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.server.*;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.server.player.PlayerDataComponentManager;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.util.*;
import cc.thonly.reverie_dreams.util.item.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.network.ModrinthAPI;
import cc.thonly.reverie_dreams.util.network.NetUtil;
import cc.thonly.reverie_dreams.world.BiomeModificationInit;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import cc.thonly.reverie_dreams.world.WorldGenerationInit;
import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class ReverieDreams implements ModInitializer {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer server;
    @Getter
    private static RegistryAccess dynamicRegistryManager;
    private static final Set<ServerPlayer> PLAYER_WITH_MOD = new HashSet<>();
    private static final Map<ServerPlayer, String> PLAYER_SIDE_VERSION = new WeakHashMap<>();

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, ReverieDreamsConfiguration.class);
        LOGGER.info("Loaded " + MOD_NAME);

        // 初始化静态注册表
        SoundEventInit.init();
        JukeboxSongInit.init();
        RDDataComponentTypes.init();
        RDArmorMaterials.init();
        RDGuiItems.init();
        RDBlockStateTemplates.bootstrap();
        RDBlockEntityTypes.registerBlockEntityTypes();
        RDBlocks.registerBlocks();
        RDWoodBlocks.registerBlocks();
        RDCropBlocks.registerBlocks();
        RDPlantBlocks.registerBlocks();
        KitchenBlocks.registerBlocks();
        RDItems.registerItems();
        RDIngredientItems.registerItems();
        RDFoodItems.registerItems();
        RDDrinkItems.registerItems();
        RDEntityHolderItems.registerHolders();
        RDEntityTypes.registerEntityTypes();
        RDStatusEffects.registerEffects();
        RDPotions.registerPotions();
        WorldGenerationInit.registerWorldGeneration();
        RDPointOfInterestTypes.registers();
        RDVillagerProfessions.registers();
        BiomeModificationInit.init();
        GameRulesInit.init();
        DataFixerContentManager.bootstrap();
        DialogInit.bootstrap();

        // 初始化其他注册内容
        CommandInit.init();
        RecipeManager.bootstrap();
        ServerResourceHelper.init();
        RegistryHandlers.bootstrap();
        FoodProperties.registerDefaultItemUsingProperty();
        DrinkProperties.registerDefaultItemUsingProperty();
        Key2ValueRegistryHandlers.bootstrap();
        RDLootModifies.register();
        RecipeTypeCategoryManager.registerCategories();
        DanmakuTemplates.init();

        ImageToTextScanner.bootstrap();
        ItemDescriptionManager.bootstrap();
        PlayerDataComponentManager.registers();

        this.loadCompletableEvent();
        this.registerNetworkingEvent();
        this.registerServerEvents();
        this.registerContentEvent();

        ReverieDreamsCompats.init();
    }

    private void registerContentEvent() {
        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (!world.isClientSide()) {
                ItemStack stack = playerEntity.getItemInHand(hand);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (block instanceof LeavesBlock && (blockState.getValue(LeavesBlock.WATERLOGGED))) {
                    if (stack.getItem() == Items.LILY_PAD) {
                        stack.consume(1, playerEntity);
                        if (!playerEntity.hasInfiniteMaterials()) {
                            playerEntity.addItem(new ItemStack(RDIngredientItems.DEW, 1));
                        }
                        playerEntity.swing(hand);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && Unit.INSTANCE.equals(stack.getOrDefault(RDDataComponentTypes.SILVER_ITEM, null))) {
                RegistryAccess.Frozen registryManager = server.registryAccess();
                Registry<EntityType<?>> entityTypes = registryManager.lookupOrThrow(Registries.ENTITY_TYPE);
                DamageSources damageSources = attacker.damageSources();
                for (Holder<EntityType<?>> iterateEntry : entityTypes.getTagOrEmpty(EntityTypeTags.UNDEAD)) {
                    EntityType<?> value = iterateEntry.value();
                    if (target.getType() == value) {
                        target.lastHurt = 0;
                        target.hurtServer(serverWorld, damageSources.magic(), 2);
                        target.lastHurt = 0;
                        break;
                    }
                }

            }
            return true;
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && target.getType() == RDEntityTypes.GHOST_ENTITY_TYPE && stack.getItem() == RDItems.ROKANKEN) {
                DamageSources damageSources = attacker.damageSources();
                target.lastHurt = 0;
                target.hurtServer(serverWorld, damageSources.magic(), Integer.MAX_VALUE);
            }
            return true;
        });
    }

    private void registerNetworkingEvent() {
        PayloadTypeRegistry.playC2S().register(HelloPayload.PACKET_ID, HelloPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(HelloPayload.PACKET_ID, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player != null) {
                PLAYER_WITH_MOD.add(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((playNetworkHandler, server) -> {
            PLAYER_WITH_MOD.remove(playNetworkHandler.player);
        });
        PayloadTypeRegistry.playC2S().register(CSVersionPayload.PACKET_ID, CSVersionPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(CSVersionPayload.PACKET_ID, (payload, context) -> {
            ServerPlayer player = context.player();
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
            ServerPlayer player = handler.getPlayer();
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
            ServerPlayer player = handler.getPlayer();
            if (!ReverieDreamsConfiguration.CHECK_UPDATE) {
                return;
            }
            if (!player.hasPermissions(2)) {
                return;
            }
            if (ConstantInfo.LATEST_VERSION == null) {
                return;
            }
            MutableComponent mutableText = Component.empty();
            mutableText.append(Component.translatable("message.reverie_dreams.update", ConstantInfo.LATEST_VERSION));
            mutableText.append(" §r[");
            mutableText.append(Component.translatable("item.action.click.left").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/gensokyo-reverie-of-lost-dreams")))));
            mutableText.append("§r]");
            player.displayClientMessage(mutableText, false);
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((livingEntity, damageSource, v) -> {
            return !livingEntity.hasEffect(RDStatusEffects.ELIXIR_OF_LIFE);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlayerInputManager inputManager = PlayerInputManager.getInstance();
            inputManager.reload();
            DialogPlayer.reload();
        });
        ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            playerDataComponentManager.saveAll();
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
        ServerTickEvents.END_SERVER_TICK.register(SpellcardRenderer::tick);
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

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    public static boolean hasModOnClient(ServerPlayer player) {
        if (player == null) return false;
        return PLAYER_WITH_MOD.contains(player);
    }

    public static void setDynamicRegistryManager(RegistryAccess dynamicRegistryManager) {
        ReverieDreams.dynamicRegistryManager = dynamicRegistryManager;
    }

    public static void setServer(MinecraftServer server) {
        ReverieDreams.server = server;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }


}