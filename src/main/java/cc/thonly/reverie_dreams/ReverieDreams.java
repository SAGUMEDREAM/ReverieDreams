package cc.thonly.reverie_dreams;

import cc.thonly.minecraft.api.ItemPostHitCallback;
import cc.thonly.reverie_dreams.block.creator.ChestBlockCreator;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.compat.ReverieDreamsCompats;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.creative_tab.CreativeTabs;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeFunctions;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogInit;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.entity.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.entity.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.weapon.YukaFlowerUmbrella;
import cc.thonly.reverie_dreams.loot.RDLootModifies;
import cc.thonly.reverie_dreams.networking.CSVersionPayload;
import cc.thonly.reverie_dreams.networking.HelloPayload;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.PairRegistryHandlers;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.DrinkProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.armor.RDArmorMaterials;
import cc.thonly.reverie_dreams.registry.content.block.*;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.*;
import cc.thonly.reverie_dreams.registry.impl.ServerResourceHelper;
import cc.thonly.reverie_dreams.server.*;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.command.PermissionPredicate;
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
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

@Setter
@Getter
public class ReverieDreams implements ModInitializer {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer server;
    private static final Set<ServerPlayer> PLAYER_WITH_MOD = new HashSet<>();
    private static final Map<ServerPlayer, String> PLAYER_SIDE_VERSION = new WeakHashMap<>();

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, ReverieDreamsConfiguration.class);
        LOGGER.info("Loaded " + MOD_NAME);

        // 初始化静态注册表
        SoundEventInit.init();
        JukeboxSongInit.init();
        RDDataComponents.init();
        RDArmorMaterials.init();
        RDGuiItems.init();
        RDBlockStateTemplates.bootstrap();
        RDBlockEntityTypes.registerBlockEntityTypes();
        RDBlocks.registerBlocks();
        RDWoodBlocks.registerBlocks();
        RDCropBlocks.registerBlocks();
        RDPlantBlocks.registerBlocks();
        KitchenBlocks.registerBlocks();
        RDEnchantments.registerEnchantments();
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
        DialogInit.bootstrap();
        RDCriteriaTriggers.registerCriteria();

        // 初始化其他注册内容
        CommandInit.init();
        RecipeManager.bootstrap();
        ServerResourceHelper.init();
        RegistryHandlers.bootstrap();
        FoodProperties.registerDefaultItemUsingProperty();
        DrinkProperties.registerDefaultItemUsingProperty();
        PairRegistryHandlers.bootstrap();
        RDLootModifies.register();
        RecipeTypeCategoryManager.registerCategories();
        DanmakuTemplates.init();
        CustomClickActionRegistry.registerActions();
        KeyframeFunctions.bootstrap();

        ImageToTextScanner.bootstrap();
        ItemDescriptionManager.bootstrap();
        PlayerDataComponentManager.registers();

        this.loadCompletableEvent();
        this.registerNetworkingEvent();
        this.registerServerEvents();
        this.registerContentEvent();

        for (WoodCreator instance : WoodCreator.INSTANCES) {
            FlammableBlockRegistry defaultInstance = FlammableBlockRegistry.getDefaultInstance();
            defaultInstance.add(instance.log(), 5, 20);
            defaultInstance.add(instance.strippedLog(), 5, 20);
            defaultInstance.add(instance.wood(), 5, 20);
            defaultInstance.add(instance.strippedWood(), 5, 20);
            defaultInstance.add(instance.planks(), 5, 20);
            defaultInstance.add(instance.stairs(), 5, 20);
            defaultInstance.add(instance.slab(), 5, 20);
            defaultInstance.add(instance.fence(), 5, 20);
            defaultInstance.add(instance.fenceGate(), 5, 20);
            FuelRegistryEvents.BUILD.register((builder, context) -> {
                builder.add(instance.fence(), 300);
                builder.add(instance.fenceGate(), 300);
            });
        }
        for (ChestBlockCreator chestBlockCreator : ChestBlockCreator.INSTANCES.get(ChestBlockCreator.class).stream().map((ab) -> (ChestBlockCreator) ab).toList()) {
            RDBlockEntityTypes.CUSTOM_CHEST_BLOCK_ENTITY.addSupportedBlock(chestBlockCreator.chestBlock());
        }
        CreativeTabs.registerItemGroups();
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
            MinecraftServer server = target.level().getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && Unit.INSTANCE.equals(stack.getOrDefault(RDDataComponents.SILVER_ITEM, null))) {
                RegistryAccess.Frozen registryAccess = server.registryAccess();
                Registry<EntityType<?>> entityTypes = registryAccess.lookupOrThrow(Registries.ENTITY_TYPE);
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
            MinecraftServer server = target.level().getServer();
            ItemStack itemStack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
            if (server != null && !itemStack.isEmpty()) {
                Level world = target.level();
                RegistryAccess registryAccess = target.registryAccess();
                Registry<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
                Holder.Reference<Enchantment> moonDamage = enchantments.getOrThrow(RDEnchantments.MOON_DAMAGE);
                int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(moonDamage, itemStack);
                if (itemEnchantmentLevel != 0) {
                    DelayedTask.create(server, 1, () -> {
                        target.hurtTime = 0;
                        if (target.getHealth() - itemEnchantmentLevel >= 0) {
                            target.setHealth(target.getHealth() - itemEnchantmentLevel);
                        }
                        target.lastHurt = 0;
                    });
                }
            }
            return true;
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            Level level = attacker.level();
            if (level instanceof ServerLevel world && stack.getItem() instanceof YukaFlowerUmbrella) {
                double speed = attacker.getDeltaMovement().length();
                Entity vehicle = attacker.getVehicle();
                if (vehicle != null) {
                    double length = vehicle.getDeltaMovement().length();
                    if (speed > length) {
                        speed = length;
                    }
                }
                MinecraftServer server = level.getServer();
                float damageValue = (float) (48f * speed);
                DelayedTask.create(server, 1, () -> {
                    target.hurtTime = 0;
                    if (target.getHealth() - damageValue >= 0) {
                        target.setHealth(target.getHealth() - damageValue);
                    } else {
                        target.setHealth(0);
                    }
                    target.hurtTime = 0;
                });
            }
            return true;
        });

        ItemPostHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.level().getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && target.getType() == RDEntityTypes.GHOST && stack.getItem() == RDItems.ROKANKEN) {
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
        PayloadTypeRegistry.playC2S().register(CSVersionPayload.PACKET_ID, CSVersionPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(CSVersionPayload.PACKET_ID, (payload, context) -> {
            ServerPlayer player = context.player();
            String version = payload.version();
            if (player != null) {
                PLAYER_SIDE_VERSION.put(player, version);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((playNetworkHandler, server) -> {
            PLAYER_WITH_MOD.remove(playNetworkHandler.player);
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
            if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
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
        ServerTickEvents.END_SERVER_TICK.register(DelayedTask::tick);
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

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public static boolean hasModOnClient(ServerPlayer player) {
        if (player == null) return false;
        return PLAYER_WITH_MOD.contains(player);
    }

    public static void setServer(MinecraftServer server) {
        ReverieDreams.server = server;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }


}