package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.data.npc.NPCMenuType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCMenuTypes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.YsmHolder;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stores the NPC's model selection independently of the model provider's transport.
 */
public final class NPCModelSelection {
    public static final String FOX_ID = "foxmodelloader";
    public static final String SPARKLE_ID = "sparkle_morpher";
    private static final int MAX_NAME_LENGTH = 512;
    private static final Set<ServerGamePacketListenerImpl> SPARKLE_CLIENTS =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static @Nullable Predicate<ServerPlayer> classicConnected;
    private static @Nullable SelectionPolicy classicPolicy;
    private static @Nullable Consumer<OpenPicker> clientPicker;

    private NPCModelSelection() {
    }

    public static @Nullable String findModelLoader() {
        boolean fox = PlatformContext.isModLoaded(FOX_ID);
        boolean sparkle = PlatformContext.isModLoaded(SPARKLE_ID);
        if (fox == sparkle) {
            return null;
        }
        return fox ? FOX_ID : SPARKLE_ID;
    }

    public static void registerPackets() {
        if (findModelLoader() == null) {
            return;
        }
        NPCAnimationActions.registerPackets();
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, OpenPicker.ID, OpenPicker.CODEC,
                (packet, context) -> ClientNetworkingHandlers.safeHandleClient(() -> {
                    if (clientPicker != null) {
                        clientPicker.accept(packet);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SelectModel.ID, SelectModel.CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        applySelection(player, packet);
                    }
                }));
        if (usesSparkleClientCatalog()) {
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, SparkleReady.ID, SparkleReady.CODEC,
                    (packet, context) -> context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer player) {
                            SPARKLE_CLIENTS.add(player.connection);
                        }
                    }));
        }
    }

    public static void useClassicCatalog(Predicate<ServerPlayer> connected, SelectionPolicy policy) {
        classicConnected = connected;
        classicPolicy = policy;
        YsmHolder.setInitialized();
    }

    public static void registerMenus() {
        NPCAnimationActions.registerMenu();
        NPCMenuTypes.registerMenuType("modify_model", new NPCMenuType()
                .predicate(NPCModelSelection::canUsePicker)
                .factory((player, npc, currentGui) -> new GuiElementBuilder()
                        .setItem(Items.PLAYER_HEAD)
                        .setItemName(Component.translatable("gui.npc.info.model"))
                        .setCallback((slot, click, input, gui) -> {
                            if (!canUsePicker(player, npc)) {
                                return;
                            }
                            SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            if (currentGui != null) {
                                currentGui.close();
                            }
                            NetworkManager.sendToPlayer(player, new OpenPicker(npc.getId(), npc.getUUID()));
                        })));
    }

    private static boolean canUsePicker(ServerPlayer player, BaseNPCLikeEntity npc) {
        return canEdit(player, npc) && hasProvider(player) && NetworkManager.canPlayerReceive(player, OpenPicker.ID);
    }

    public static void setClientPicker(Consumer<OpenPicker> opener, boolean clientCatalog) {
        clientPicker = opener;
        if (clientCatalog) {
            YsmHolder.setInitialized();
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> {
                if (NetworkManager.canServerReceive(SparkleReady.ID)) {
                    NetworkManager.sendToServer(SparkleReady.INSTANCE);
                }
            });
        }
    }

    public static boolean usesSparkleClientCatalog() {
        return Platform.getOptionalMod(SPARKLE_ID).map(mod -> !mod.getVersion().startsWith("1.")).orElse(false);
    }

    static boolean hasProvider(ServerPlayer player) {
        return classicConnected != null ? classicConnected.test(player) : SPARKLE_CLIENTS.contains(player.connection);
    }

    static boolean canEdit(ServerPlayer player, BaseNPCLikeEntity npc) {
        return npc instanceof NPCSimpleEntity && npc.isAlive() && player.isAlive()
                && npc.level() == player.level() && npc.isAllowOpenInventory(player)
                && player.isWithinEntityInteractionRange(npc, 0.0);
    }

    static void applySelection(ServerPlayer player, SelectModel selection) {
        if (!hasProvider(player)) {
            return;
        }
        if (!(player.level().getEntity(selection.target().entityId()) instanceof NPCSimpleEntity npc)
                || !npc.getUUID().equals(selection.target().entityUuid()) || !canEdit(player, npc)) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.reverie_dreams.npc.model.cannot_edit", "You cannot edit this companion's model."));
            return;
        }
        String model = selection.modelId();
        String texture = resolveTexture(player, model, selection.textureId());
        if (texture == null) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.reverie_dreams.npc.model.unavailable", "That model is unavailable for this companion."));
            return;
        }
        npc.reverie_dreams$setModelId(model);
        npc.reverie_dreams$setTexture(texture);
    }

    private static @Nullable String resolveTexture(ServerPlayer player, String model, String texture) {
        if (!isValidChoice(model, texture)) {
            return null;
        }
        return classicPolicy == null ? texture : classicPolicy.resolveTexture(player, model, texture);
    }

    public static boolean isValidChoice(String model, String texture) {
        return !model.isBlank() && model.length() <= MAX_NAME_LENGTH && texture.length() <= MAX_NAME_LENGTH
                && model.codePoints().noneMatch(Character::isISOControl)
                && texture.codePoints().noneMatch(Character::isISOControl);
    }

    @FunctionalInterface
    public interface SelectionPolicy {
        @Nullable String resolveTexture(ServerPlayer player, String modelId, String textureId);
    }

    public record OpenPicker(int entityId, UUID entityUuid) implements CustomPacketPayload {
        public static final Type<OpenPicker> ID = new Type<>(ReverieDreams.id("fox_npc_model_picker"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenPicker> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OpenPicker::entityId,
                UUIDUtil.STREAM_CODEC, OpenPicker::entityUuid,
                OpenPicker::new);

        @Override
        public Type<OpenPicker> type() {
            return ID;
        }
    }

    public record SelectModel(OpenPicker target, String modelId, String textureId) implements CustomPacketPayload {
        public static final Type<SelectModel> ID = new Type<>(ReverieDreams.id("fox_npc_model_selection"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectModel> CODEC = StreamCodec.composite(
                OpenPicker.CODEC, SelectModel::target,
                ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), SelectModel::modelId,
                ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), SelectModel::textureId,
                SelectModel::new);

        @Override
        public Type<SelectModel> type() {
            return ID;
        }
    }

    public record SparkleReady() implements CustomPacketPayload {
        public static final SparkleReady INSTANCE = new SparkleReady();
        public static final Type<SparkleReady> ID = new Type<>(ReverieDreams.id("sparkle_npc_models_ready"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SparkleReady> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<SparkleReady> type() {
            return ID;
        }
    }
}
