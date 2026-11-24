package cc.thonly.reverie_dreams.client.networking;

import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.scope.LunarWeaponPoseClient;
import cc.thonly.reverie_dreams.client.util.PhotoScreenshotHelper;
import cc.thonly.reverie_dreams.item.prop.TenguCameraItem;
import cc.thonly.reverie_dreams.mixin.accessor.EntityAccessor;
import cc.thonly.reverie_dreams.networking.payload.*;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.RegistryTagSet;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.mojang.blaze3d.platform.NativeImage;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"resource", "rawtypes"})
@Slf4j
public class ClientNetworkingHandlers {
    private static final RecipeManagerSyncPacket.Assembler RECIPE_SYNC = new RecipeManagerSyncPacket.Assembler();

    public static void onReceive() {

    }

    public static void safeHandleClient(Runnable task) {
        if (!PlatformContext.isClientSide())
            return;
        clientThreadBySync(task);
    }

    private static void clientThreadBySync(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    public static void onReceiveLunarShotPose(LunarShotPosePacket packet) {
        LunarWeaponPoseClient.recordShot(packet);
    }

    public static void onReceiveLunarReloadPose(LunarReloadPosePacket packet) {
        LunarWeaponPoseClient.recordReload(packet);
    }

    @SuppressWarnings("unchecked")
    public static void onReceiveRecipeManagerSyncPacket(Player player, RecipeManagerSyncPacket packet) {
        Identifier typeId = packet.typeId();
        try {
            CompoundTag data = RECIPE_SYNC.accept(packet);
            if (data == null) {
                return;
            }
            BaseRecipeType<BaseRecipe> recipeType = (BaseRecipeType<BaseRecipe>) RecipeManager.RECIPE_TYPES.get(typeId);
            if (recipeType == null)
                return;
            Pair<Identifier, List<Pair<Identifier, BaseRecipe>>> pair = BaseRecipeType.readFromTag(recipeType, data);
            if (pair == null) {
                return;
            }
            List<Pair<Identifier, BaseRecipe>> list = pair.value();
            if (list.size() != data.getCompoundOrEmpty("data").getListOrEmpty("recipes").size()
                    || list.stream().map(Pair::key).distinct().count() != list.size()) {
                log.warn("Ignoring incomplete recipe snapshot for {}", typeId);
                return;
            }
            recipeType.clear();
            list.forEach(recipePair -> {
                Identifier key = recipePair.key();
                BaseRecipe recipe = recipePair.value();
                recipeType.add(key, recipe);
            });
            log.info("Load recipe type {} from server ({} recipes{})",
                    typeId, list.size(), packet.isBatch() ? ", batched snapshot" : "");
        } catch (Exception e) {
            log.error("Can't sync server recipes {}: ", typeId, e);
        }
    }

    public static void clearRecipeSync() {
        RECIPE_SYNC.clear();
    }

    public static void onReceiveCustomRegistrySyncPacket(Player player, CustomRegistrySyncPacket payload) {
        Identifier registryKey = payload.registryKey();
        CompoundTag data = payload.data();
        CompoundTag tags = payload.tags();
        try {
            RegistryProvider<Object> registry = BuiltInRegistryProviders.ofEntry(registryKey);
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
                ResourceKey<Object> resourceKey = registry.createKey(key);
//                registry.unregister(resourceKey);
//                if (updated == null) {
//                    continue;
//                }
                registry.set(resourceKey, updated, RegistrationInfo.BUILT_IN);
            }
            clientReloadListener.afterProcessing(registry);
            registry.unboundTag();
            List<Tuple<Identifier, RegistryTagSet>> tuples = RegistrySyncer.readTagToSet(tags);
            Map<TagKey<Object>, List<Holder<Object>>> result = new Object2ObjectLinkedOpenHashMap<>();
            for (Tuple<Identifier, RegistryTagSet> tuple : tuples) {
                Identifier tagKeyId = tuple.getA();
                RegistryTagSet tagSet = tuple.getB();

                TagKey<Object> tagKey = TagKey.create(registry.key(), tagKeyId);
                List<Holder<Object>> holders = result.computeIfAbsent(tagKey, _ -> new ArrayList<>());
                RegistryTagSet.Result<Object> tagResult = tagSet.result(registry);
                for (ResourceKey<Object> id : tagResult.ids()) {
                    registry.get(id).ifPresent(holders::add);
                }
            }
            registry.bindTags(result);
            int size = tags.getIntOr("size", -1);
            if (size != result.size()) {
                log.warn("Tag serialization length inconsistent Expected: {} Actual: {}", size, result.size());
            }
            log.info("Synchronizing server registry {} was successful.", payload.registryKey());
        } catch (Exception e) {
            log.error("An error occurred while synchronizing the server registry {}: ", registryKey, e);
        }
    }

    public static void onReceiveSyncEntityPacket(Player player, SyncEntityPacket packet) {
        Level level = player.level();
        if (!level.isClientSide()) {
            return;
        }
        int entityId = packet.entityId();
        CompoundTag tag = packet.tag();
        Entity entity = level.getEntity(entityId);
        if (entity instanceof EntityAccessor accessor) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(log)) {
                ValueInput valueInput = TagValueInput.create(scopedCollector, entity.registryAccess(), tag);
                accessor.reverie_dreams$readAdditionalSaveData(valueInput);
            } catch (Exception e) {
                log.error("Error:", e);
            }
        }
    }

    public static void onReceiveStartScreenshotPacket(Player player, StartScreenshotPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) {
            return;
        }
        ItemStack stack = ItemUtils.getHandItem(player, itemStack -> itemStack.getItem() instanceof TenguCameraItem);
        int fov = stack.getOrDefault(RDDataComponentTypes.FOV.value(), 75);
        CompletableFuture<NativeImage> future = PhotoScreenshotHelper.getClientImage();
        future.thenAccept(clientImage -> {
            NativeImage resizedImage = PhotoScreenshotHelper.resizeImage(clientImage);
            NativeImage rescaledImage = PhotoScreenshotHelper.rescaleImage(resizedImage, fov);
            byte[] imageBytes = PhotoScreenshotHelper.getImageBytes(rescaledImage);
            NetworkManager.sendToServer(new ScreenshotMapPacket(packet.sessionId(), imageBytes));
        });
    }

    public static void onReceivePlayerComponentUpdatePacket(PlayerComponentUpdatePacket packet) {
        UUID playerId = packet.playerUUID();
        Player player = ClientPlayerComponentManager.resolvePlayer(playerId);
        if (player == null) {
            log.warn("Received player component data for a player that is not loaded: {}", playerId);
            return;
        }
        ClientPlayerComponentManager componentManager = ClientPlayerComponentManager.clientAccess();
        componentManager.decodes(packet.data())
                .forEach((id, component) -> componentManager.readComponent(player, id, component));
    }

}
