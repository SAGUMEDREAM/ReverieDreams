package cc.thonly.reverie_dreams;

import cc.thonly.keine.api.KeineAPI;
import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.keine.api.callback.ServerSavingCallback;
import cc.thonly.reverie_dreams.api.item.ItemAttackHitCallback;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.common.MultiPlatformEvents;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.creative_tab.CreativeTabs;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.script.DanmakuScriptManager;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeFunctions;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.dialog.DialogFiles;
import cc.thonly.reverie_dreams.dialog.DialogPlayer;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFindBlockGoal;
import cc.thonly.reverie_dreams.registry.content.villager.RDPointOfInterestTypes;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.weapon.YukaFlowerUmbrella;
import cc.thonly.reverie_dreams.loot.RDLootModifies;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.networking.payload.CSVersionPayload;
import cc.thonly.reverie_dreams.networking.payload.HelloPayload;
import cc.thonly.reverie_dreams.networking.payload.SyncEntityPacket;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.RecipeWorkbenchRegistry;
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
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.impl.ServerResourceHelper;
import cc.thonly.reverie_dreams.server.*;
import cc.thonly.reverie_dreams.server.nota.Nota;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.util.CardboardWarning;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.ImageToTextScanner;
import cc.thonly.reverie_dreams.util.item.ItemStackCheckUtils;
import cc.thonly.reverie_dreams.util.network.ModrinthAPI;
import cc.thonly.reverie_dreams.util.network.NetUtil;
import cc.thonly.reverie_dreams.world.BiomeModificationInit;
import cc.thonly.reverie_dreams.world.RDGameRules;
import cc.thonly.reverie_dreams.world.WorldGenerationInit;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.blay09.mods.balm.network.BalmNetworking;
import net.blay09.mods.balm.platform.event.callback.*;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("LombokGetterMayBeUsed")
@Setter
@Getter
public class ReverieDreams {
    public static final String MOD_NAME = "Gensokyo: Reverie of Lost Dreams";
    public static final String MOD_ID = "reverie_dreams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random RD = new Random();
    public static final List<Runnable> LATE_INIT = new ArrayList<>();
    public static final List<Runnable> BUS_LATE_INIT = new ArrayList<>();
    public static final List<Runnable> LATE_INIT_CLIENT = new ArrayList<>();
    public static final Map<Identifier, EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER_REGISTRY = new Object2ObjectLinkedOpenHashMap<>();
    public static Function<ResourceKey<? extends Registry<?>>, RegistryHandler<?>> REGISTRY_GETTER = null;
    public static BiFunction<ResourceKey<? extends Registry<?>>, RegistryHandler<?>, RegistryHandler<?>> REGISTRY_SHADOWER = null;
    private static KeineRegistries keineRegistries;
    private static BalmBlockRegistrar BLOCK_REGISTRAR;
    private static BalmItemRegistrar ITEM_REGISTRAR;
    private static BalmEntityTypeRegistrar ENTITY_TYPE_REGISTRAR;
    private static MinecraftServer server;
    private static final Set<ServerPlayer> PLAYER_WITH_MOD = new HashSet<>();
    private static final Map<ServerPlayer, String> PLAYER_SIDE_VERSION = new WeakHashMap<>();

    public static ReverieDreamsConfiguration config() {
        ReverieDreamsConfiguration activeConfig = Balm.config().getActiveConfig(ReverieDreamsConfiguration.class);
        return activeConfig == null ? new ReverieDreamsConfiguration() : activeConfig;
    }

    public static BalmBlockRegistrar getBlockRegistrar() {
        return BLOCK_REGISTRAR;
    }

    public static BalmItemRegistrar getItemRegistrar() {
        return ITEM_REGISTRAR;
    }

    public static BalmEntityTypeRegistrar getEntityTypeRegistrar() {
        return ENTITY_TYPE_REGISTRAR;
    }

    public static KeineRegistries getKeineRegistries() {
        return keineRegistries;
    }

    public static void initialize(BalmRegistrars registrars, Runnable lateInit) {
        keineRegistries = KeineAPI.getApi().get(MOD_ID);
        Balm.config().registerConfig(ReverieDreamsConfiguration.class);
        CardboardWarning.checkAndAnnounce();
        if (PlatformContext.isDevMode()) {
            LOGGER.warn("=====================================================");
            LOGGER.warn("You are using development version of Gensokyo: Reverie of Lost Dreams!");
            LOGGER.warn("Support is limited, as features might be unfinished!");
            LOGGER.warn("You are on your own!");
            LOGGER.warn("=====================================================");
        }
        if (PlatformContext.hasOptifine()) {
            LOGGER.warn("Are you kidding me? Install Optifine?!");
        }
        LOGGER.info("Loaded " + MOD_NAME);
        PlatformContext.FABRIC_POLYFACTORY_HAND_CRANK = Blocks.AIR;
        PlatformContext.FABRIC_CREATE_FLY_HAND_CRANK = Blocks.AIR;
        registrars.items(balmItemRegistrar -> ITEM_REGISTRAR = balmItemRegistrar);
        registrars.blocks(balmBlockRegistrar -> BLOCK_REGISTRAR = balmBlockRegistrar);
        registrars.entityTypes(balmEntityTypeRegistrar -> ENTITY_TYPE_REGISTRAR = balmEntityTypeRegistrar);

        // 初始化静态注册表
        Nota.initialize();
        JukeboxSongInit.initialize();
        RDArmorMaterials.initialize();
        RDBlockStateTemplates.initialize();
        RDEnchantments.registerEnchantments();
        registrars.registrar(Registries.SOUND_EVENT, RDSoundEvents::initialize);
        registrars.dataComponentTypes(RDDataComponents::initialize);
        registrars.items(RDGuiItems::initialize);
        registrars.items(RDItems::initialize);
        registrars.items(RDIngredientItems::initialize);
        registrars.items(RDFoodItems::initialize);
        registrars.items(RDDrinkItems::initialize);
        registrars.items(RDEntityHolderItems::initialize);
        registrars.blocks(RDBlocks::initialize);
        registrars.blocks(RDWoodBlocks::initialize);
        registrars.blocks(RDCropBlocks::initialize);
        registrars.blocks(RDPlantBlocks::initialize);
        registrars.blocks(KitchenBlocks::initialize);
        registrars.blockEntityTypes(RDBlockEntityTypes::initialize);
        registrars.entityTypes(RDEntityTypes::initialize);
        registrars.registrar(Registries.MOB_EFFECT, RDStatusEffects::initialize);
        registrars.registrar(Registries.POTION, RDPotions::initialize);
        registrars.poiTypes(RDPointOfInterestTypes::initialize);
        registrars.registrar(Registries.VILLAGER_PROFESSION, RDVillagerProfessions::initialize);
        registrars.registrar(Registries.TRIGGER_TYPE, RDCriteriaTriggers::initialize);
        registrars.registrar(Registries.GAME_RULE, RDGameRules::initialize);
        registrars.creativeModeTabs(CreativeTabs::initialize);
        WorldGenerationInit.registerWorldGeneration(registrars);
        BiomeModificationInit.initialize();

        // 初始化其他注册内容
        MultiPlatformEvents.initialize();
        RecipeManager.bootstrap(registrars);
        RecipeWorkbenchRegistry.bootstrap();
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
        PlayerDataComponentManager.registers();

        loadCompletableEvent(registrars);
        registerNetworkingEvent(registrars);
        registerServerEvents(registrars);
        registerContentEvent(registrars);

        ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.put(id("danmaku_properties"), DanmakuProperties.SERIALIZER);
        ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.put(id("skin_type"), SkinType.SERIALIZER);
        lateInit.run();
    }

    @SuppressWarnings("resource")
    private static void registerContentEvent(BalmRegistrars registrars) {
        LivingEntityCallback.Damage.Before.EVENT.register((entity, damageSource, damageAmount) -> {
            Entity directEntity = damageSource.getDirectEntity();
            if (!(directEntity instanceof LivingEntity attacker)) {
                return damageAmount;
            }
            if (attacker.level().isClientSide()) {
                return damageAmount;
            }
            ItemStack itemInHand = attacker.getItemInHand(InteractionHand.MAIN_HAND);
            if (!itemInHand.has(RDDataComponents.SILVER_ITEM.value())) {
                return damageAmount;
            }
            return damageAmount + 2;
        });
        BlockCallback.Use.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide()) {
                ItemStack stack = player.getItemInHand(hand);
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof LeavesBlock && state.getValue(LeavesBlock.WATERLOGGED)) {
                    if (stack.getItem() == Items.LILY_PAD) {
                        stack.consume(1, player);

                        if (!player.hasInfiniteMaterials()) {
                            player.addItem(new ItemStack(RDIngredientItems.DEW.asItem(), 1));
                        }

                        player.swing(hand);

                        return InteractionEventResult.SUCCESS_SERVER;
                    }
                }
            }

            return InteractionEventResult.DEFAULT;
        });

        ItemAttackHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.level().getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && stack.has(RDDataComponents.SILVER_ITEM.value())) {
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

        ItemAttackHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.level().getServer();
            ItemStack itemStack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
            if (server != null && !itemStack.isEmpty()) {
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

        ItemAttackHitCallback.EVENT.register((stack, target, attacker) -> {
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

        ItemAttackHitCallback.EVENT.register((stack, target, attacker) -> {
            MinecraftServer server = target.level().getServer();
            if (server != null && target.level() instanceof ServerLevel serverWorld && target.getType() == RDEntityTypes.GHOST && stack.getItem() == RDItems.ROKANKEN) {
                DamageSources damageSources = attacker.damageSources();
                target.lastHurt = 0;
                target.hurtServer(serverWorld, damageSources.magic(), Integer.MAX_VALUE);
            }
            return true;
        });

    }

    @SuppressWarnings("resource")
    private static void registerNetworkingEvent(BalmRegistrars registrars) {
        BalmNetworking networking = Balm.networking();
        networking.registerServerboundPacket(
                HelloPayload.PACKET_ID,
                HelloPayload.class,
                HelloPayload.CODEC,
                (player, payload) -> {
                    PLAYER_WITH_MOD.add(player);
                }
        );
        networking.registerServerboundPacket(
                CSVersionPayload.PACKET_ID,
                CSVersionPayload.class,
                CSVersionPayload.CODEC,
                (player, payload) -> {
                    String version = payload.version();
                    PLAYER_SIDE_VERSION.put(player, version);
                }
        );
        networking.registerClientboundPacket(
                SyncEntityPacket.PACKET_ID,
                SyncEntityPacket.class,
                SyncEntityPacket.CODEC,
                (player, packet) -> {
                    Level level = player.level();
                    if (!level.isClientSide()) {
                        return;
                    }
                    int entityId = packet.entityId();
                    CompoundTag tag = packet.tag();
                    Entity entity = level.getEntity(entityId);
                    if (entity instanceof EntityAccessor accessor) {
                        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER)) {
                            ValueInput valueInput = TagValueInput.create(scopedCollector, entity.registryAccess(), tag);
                            accessor.reverie_dreams$readAdditionalSaveData(valueInput);
                        } catch (Exception e) {
                            LOGGER.error("Error:", e);
                        }
                    }
                }
        );
        ServerPlayerCallback.Leave.EVENT.register((player) -> {
            PLAYER_WITH_MOD.remove(player);
            PLAYER_SIDE_VERSION.remove(player);
        });
    }

    @SuppressWarnings("rawtypes")
    private static void registerServerEvents(BalmRegistrars registrars) {
        ServerPlayerCallback.Join.EVENT.register(player -> {
            PlayerDataComponentManager componentManager = PlayerDataComponentManager.getInstance();
            for (Map.Entry<Class<PlayerComponent<? extends PlayerComponent>>, PlayerComponentInitializer<?>> mapEntry : PlayerDataComponentManager.getComponents()) {
                Class<PlayerComponent<? extends PlayerComponent>> key = mapEntry.getKey();
                componentManager.getOrCreatePlayerComponent(player, key);
            }
        });
        ServerPlayerCallback.Leave.EVENT.register((player) -> {
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            playerDataComponentManager.saveAll();
        });
        ServerLifecycleCallback.Reloaded.EVENT.register(server -> {
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            playerDataComponentManager.onLoad(server);
        });
        ServerPlayerCallback.Join.EVENT.register(player -> {
            if (!ReverieDreams.config().checkUpdate) {
                return;
            }
            if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                return;
            }
            if (PlatformContext.LATEST_VERSION == null) {
                return;
            }
            MutableComponent mutableText = Component.empty();
            mutableText.append(Component.translatable("message.reverie_dreams.update", PlatformContext.LATEST_VERSION));
            mutableText.append(" §r[");
            mutableText.append(Component.translatable("item.action.click.left").setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/gensokyo-reverie-of-lost-dreams")))));
            mutableText.append("§r]");
            player.displayClientMessage(mutableText, false);
        });
        LivingEntityCallback.Death.Before.EVENT.register((entity, damageSource) -> {
            return !entity.hasEffect(RDStatusEffects.ELIXIR_OF_LIFE);
        });
        ServerLifecycleCallback.Started.EVENT.register(server -> {
            PlayerInputManager inputManager = PlayerInputManager.getInstance();
            NPCFindBlockGoal.EXCLUSIONS.clear();
            inputManager.reload();
            DialogPlayer.reload();
        });
        ServerSavingCallback.AFTER.register((server, flush, force) -> {
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            playerDataComponentManager.saveAll();
        });
        ServerTickCallback.AFTER.register(DelayedTask::tick);
        ServerTickCallback.AFTER.register(PlayerDataComponentManager::tick);
        ServerTickCallback.AFTER.register(ParticleTickerManager::tick);
        ServerTickCallback.AFTER.register(PlayerInputManager::tick);
        ServerTickCallback.AFTER.register(DanmakuScriptManager::onTick);
        ServerTickCallback.AFTER.register(DialogPlayer::tick);
        ServerTickCallback.AFTER.register(SpellcardRenderer::tick);
    }

    private static void loadCompletableEvent(BalmRegistrars registrars) {
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
                LOGGER.info("You're using a newer version than latest: {}", PlatformContext.VERSION);
            } else {
                LOGGER.info("You're using the latest version: {}", PlatformContext.VERSION);
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