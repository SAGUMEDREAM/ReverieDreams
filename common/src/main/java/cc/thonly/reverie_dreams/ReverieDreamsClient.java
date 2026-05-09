package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDBlockRenderTypes;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import cc.thonly.reverie_dreams.client.util.PhotoScreenshotHelper;
import cc.thonly.reverie_dreams.item.prop.TenguCameraItem;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.networking.payload.*;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.Pair;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.network.BalmNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);

    public static void initialize(BalmClientRegistrars registrars, Runnable lateInit) {
//        registrars.blockRenderTypes(RDBlockRenderTypes::initialize);
        registrars.blockEntityRenderers(RDBlockEntityRenderers::initialize);
        registrars.entityRenderers(RDEntityRenderers::initialize);
        ReverieDreamsClient.initializeClientEvent(registrars);
        ReverieDreamsClient.initializeNetworking(registrars);
        ReverieDreamsClient.initializeClientHooks(registrars);
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }

    public static void initializeClientEvent(BalmClientRegistrars registrars) {
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(client -> {
            Balm.networking().sendToServer(new HelloPacket());
            Balm.networking().sendToServer(new CSVersionPacket(PlatformContext.VERSION.get()));
        });
    }

    public static void initializeClientHooks(BalmClientRegistrars registrars) {

    }

    @SuppressWarnings({"resource", "unchecked"})
    public static void initializeNetworking(BalmClientRegistrars registrars) {
        BalmNetworking networking = Balm.networking();
        networking.registerClientboundPacket(
                RecipeManagerSyncPacket.PACKET_ID,
                RecipeManagerSyncPacket.class,
                RecipeManagerSyncPacket.CODEC,
                (player, payload) -> Minecraft.getInstance().execute(() -> {
                    Identifier typeId = payload.typeId();
                    CompoundTag data = payload.data();
                    try {
                        BaseRecipeType<BaseRecipe> recipeType = (BaseRecipeType<BaseRecipe>) RecipeManager.RECIPE_TYPES.get(typeId);
                        if (recipeType == null) return;
                        Pair<Identifier, List<Pair<Identifier, BaseRecipe>>> pair = BaseRecipeType.readFromTag(recipeType, data);
                        if (pair == null) {
                            return;
                        }
                        recipeType.clear();
                        List<Pair<Identifier, BaseRecipe>> list = pair.value();
                        list.forEach(recipePair -> {
                            Identifier key = recipePair.key();
                            BaseRecipe recipe = recipePair.value();
                            recipeType.add(key, recipe);
                        });
                    } catch (Exception e) {
                        logger().error("Can't sync server recipes {}: ", typeId, e);
                    }
                })
        );
        networking.registerClientboundPacket(
                RegistryImpSyncPacket.PACKET_ID,
                RegistryImpSyncPacket.class,
                RegistryImpSyncPacket.CODEC, (player, payload) -> Minecraft.getInstance().execute(() -> {
                    Identifier registryKey = payload.registryKey();
                    CompoundTag data = payload.data();
                    try {
                        RegistryImpl<Object> registry = RegistryImpls.ofEntry(registryKey);
                        if (registry == null) {
                            return;
                        }
                        if (!registry.isSyncToClient()) {
                            return;
                        }
                        RegistrySyncer<Object, Object> syncer = registry.getSyncer();
                        List<RegistrySyncer.Entry<Object>> entries = syncer.readToEntries(data);
                        RegistrySyncer.ClientReloadListener<Object, Object> clientReloadListener = syncer.getClientReloadListener();
                        clientReloadListener.preProcessing(registry);
                        for (RegistrySyncer.Entry<Object> entry : entries) {
                            Identifier key = entry.key();
                            Object value = entry.value();
                            Object updated = clientReloadListener.update(key, registry.getValue(key), value);
                            registry.set(registry.createKey(key), updated, RegistrationInfo.BUILT_IN);
                        }
                        clientReloadListener.afterProcessing(registry);
                    } catch (Exception e) {
                        logger().error("Can't sync server registry: {}", registryKey, e);
                    }
                })
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
        networking.registerClientboundPacket(
                StartScreenshotPacket.PACKET_ID,
                StartScreenshotPacket.class,
                StartScreenshotPacket.CODEC,
                (player, packet) -> Minecraft.getInstance().execute(() -> {
                    Minecraft mc = Minecraft.getInstance();
                    LocalPlayer localPlayer = mc.player;
                    if (localPlayer == null) {
                        return;
                    }
                    ItemStack stack = ItemUtils.getHandItem(player, itemStack -> itemStack.getItem() instanceof TenguCameraItem);
                    int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);
                    CompletableFuture<NativeImage> future = PhotoScreenshotHelper.getClientImage();
                    future.thenAccept(clientImage -> {
                        NativeImage resizedImage = PhotoScreenshotHelper.resizeImage(clientImage);
                        NativeImage rescaledImage = PhotoScreenshotHelper.rescaleImage(resizedImage, fov);
                        byte[] imageBytes = PhotoScreenshotHelper.getImageBytes(rescaledImage);
                        Balm.networking().sendToServer(new ScreenshotMapPacket(packet.sessionId(), imageBytes));
                    });
                })
        );
    }

    public static Logger logger() {
        return LOGGER;
    }
}
