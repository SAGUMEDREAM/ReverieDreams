package cc.thonly.reverie_dreams.compat.ysm;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.data.npc.NPCMenuType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCMenuTypes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import dev.architectury.networking.NetworkManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class NPCAnimationActions {
    private static final int MAX_NAME_LENGTH = 512;
    private static @Nullable Consumer<OpenWheel> clientOpener;

    private NPCAnimationActions() {
    }

    static void registerPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, OpenWheel.ID, OpenWheel.CODEC,
                (packet, context) -> ClientNetworkingHandlers.safeHandleClient(() -> {
                    if (clientOpener != null) {
                        clientOpener.accept(packet);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SelectAction.ID, SelectAction.CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        applyAction(player, packet);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WheelActivity.ID, WheelActivity.CODEC,
                (packet, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        updateWheel(player, packet);
                    }
                }));
    }

    static void registerMenu() {
        NPCMenuTypes.registerMenuType("animation_actions", new NPCMenuType()
                .predicate(NPCAnimationActions::canUseWheel)
                .factory((player, npc, currentGui) -> new GuiElementBuilder()
                        .setItem(Items.NOTE_BLOCK)
                        .setItemName(Component.translatableWithFallback("gui.npc.actions", "Companion actions"))
                        .setCallback((slot, click, input, gui) -> {
                            if (!(npc instanceof NPCSimpleEntity target) || !canUseWheel(player, target)) {
                                return;
                            }
                            UUID token = target.beginActionWheel(player);
                            if (token == null) {
                                return;
                            }
                            SoundEventPlayUtils.playUISound(player, SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                            if (currentGui != null) {
                                currentGui.close();
                            }
                            var wheel = new OpenWheel(new NPCModelSelection.OpenPicker(target.getId(), target.getUUID()), token);
                            NetworkManager.sendToPlayer(player, wheel);
                        })));
    }

    public static void setClientOpener(Consumer<OpenWheel> opener) {
        clientOpener = opener;
    }

    public static void reportWheel(OpenWheel wheel, boolean open) {
        if (NetworkManager.canServerReceive(WheelActivity.ID)) {
            NetworkManager.sendToServer(new WheelActivity(wheel, open));
        }
    }

    private static boolean canUseWheel(ServerPlayer player, BaseNPCLikeEntity npc) {
        return npc.reverie_dreams$hasModel() && NPCModelSelection.canEdit(player, npc)
                && NPCModelSelection.hasProvider(player) && NetworkManager.canPlayerReceive(player, OpenWheel.ID);
    }

    private static void applyAction(ServerPlayer player, SelectAction request) {
        NPCSimpleEntity npc = resolveWheel(player, request.wheel());
        if (npc == null || !canUseWheel(player, npc)
                || !npc.reverie_dreams$getModelId().equals(request.model())
                || !NPCModelSelection.isValidChoice(request.model(), request.animation())
                || !isActionName(request.animation())) {
            return;
        }
        npc.setYsmAction(request.animation());
        npc.closeActionWheel();
    }

    private static void updateWheel(ServerPlayer player, WheelActivity request) {
        NPCSimpleEntity npc = resolveWheel(player, request.wheel());
        if (npc == null) {
            return;
        }
        if (request.open() && canUseWheel(player, npc)) {
            npc.refreshActionWheel(player, request.wheel().token());
        } else {
            npc.closeActionWheel();
        }
    }

    private static @Nullable NPCSimpleEntity resolveWheel(ServerPlayer player, OpenWheel wheel) {
        var target = wheel.target();
        return player.level().getEntity(target.entityId()) instanceof NPCSimpleEntity npc
                && npc.getUUID().equals(target.entityUuid()) && npc.hasActionWheel(player, wheel.token()) ? npc : null;
    }

    public static boolean isActionName(String animation) {
        return animation.length() <= MAX_NAME_LENGTH && (animation.isEmpty() || !animation.isBlank())
                && animation.codePoints().noneMatch(Character::isISOControl);
    }

    public record OpenWheel(NPCModelSelection.OpenPicker target, UUID token) implements CustomPacketPayload {
        public static final Type<OpenWheel> ID = new Type<>(ReverieDreams.id("npc_animation_wheel"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenWheel> CODEC =
                StreamCodec.composite(NPCModelSelection.OpenPicker.CODEC, OpenWheel::target,
                        UUIDUtil.STREAM_CODEC, OpenWheel::token, OpenWheel::new);

        @Override
        public Type<OpenWheel> type() {
            return ID;
        }
    }

    public record SelectAction(OpenWheel wheel, String model, String animation) implements CustomPacketPayload {
        public static final Type<SelectAction> ID = new Type<>(ReverieDreams.id("npc_animation_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectAction> CODEC = StreamCodec.composite(
                OpenWheel.CODEC, SelectAction::wheel,
                ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), SelectAction::model,
                ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), SelectAction::animation,
                SelectAction::new);

        @Override
        public Type<SelectAction> type() {
            return ID;
        }
    }

    private record WheelActivity(OpenWheel wheel, boolean open) implements CustomPacketPayload {
        private static final Type<WheelActivity> ID = new Type<>(ReverieDreams.id("npc_animation_wheel_activity"));
        private static final StreamCodec<RegistryFriendlyByteBuf, WheelActivity> CODEC = StreamCodec.composite(
                OpenWheel.CODEC, WheelActivity::wheel, ByteBufCodecs.BOOL, WheelActivity::open, WheelActivity::new);

        @Override
        public Type<WheelActivity> type() {
            return ID;
        }
    }
}
