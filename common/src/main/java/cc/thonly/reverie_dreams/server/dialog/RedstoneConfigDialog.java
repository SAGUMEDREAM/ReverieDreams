package cc.thonly.reverie_dreams.server.dialog;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.*;
import cc.thonly.reverie_dreams.block.redstone.*;
import cc.thonly.reverie_dreams.server.RemoteSignalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.lang.ref.WeakReference;
import java.util.*;

/** One explicit-save dialog flow for the six station components. Esc/cancel never saves. */
public final class RedstoneConfigDialog {
    public static final Identifier ACTION = ReverieDreams.id("redstone/configure");
    private record Session(BlockPos pos, String dimension, Block block, WeakReference<BlockEntity> entity,
                           String nonce, long expires) {}
    private static final Map<ServerPlayer, Session> SESSIONS = new WeakHashMap<>();
    public static void clear() { SESSIONS.clear(); }
    private RedstoneConfigDialog() {}
    private static Component text(String key, Object... args) {
        return Component.translatable("dialog.reverie_dreams.redstone." + key, args);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        if (player == null || player.isSpectator()) return;
        var level = player.level();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        boolean readonly = state.getBlock() instanceof RailControllerBlock || !player.mayBuild();
        String kind = state.getBlock() instanceof RailControllerBlock ? "controller"
                : be instanceof SignalRailBlockEntity ? "detector"
                : be instanceof SignalDelayerBlockEntity ? "delayer"
                : be instanceof RemoteBlockEntity ? "wireless" : "speaker";
        Session session = new Session(pos.immutable(), level.dimension().identifier().toString(), state.getBlock(),
                new WeakReference<>(be), UUID.randomUUID().toString(), level.getGameTime() + 2400);
        SESSIONS.put(player, session);
        CompoundTag additions = new CompoundTag(); additions.putString("session", session.nonce());
        boolean active = state.getBlock() instanceof RailControllerBlock ? state.getValue(RailControllerBlock.OCCUPIED)
                : state.hasProperty(RemoteClientBlock.POWERED) ? state.getValue(RemoteClientBlock.POWERED)
                : state.getValue(RailControllerBlock.OCCUPIED);
        DialogBuilder.builder(builder -> {
            builder.key(ACTION).common(common -> {
                common.title(state.getBlock().getName()).setPause(false).setCanCloseWithEscape(true).setAfterAction(DialogAction.CLOSE);
                common.addTextBody(text(kind + ".help"), 310);
                common.addTextBody(text("status", text(active ? "on" : "off")), 310);
                if (be instanceof RemoteBlockEntity remote)
                    common.addTextBody(text(remote.getRemoteType() == RemoteBlockEntity.RemoteType.SERVER ? "wireless.sender" : "wireless.receiver"), 310);
                if (state.getBlock() instanceof RailControllerBlock)
                    common.addTextBody(text("controller.cart", text(state.getValue(RailControllerBlock.POWERED) ? "yes" : "no")), 310);
                if (!readonly) common.input((inputs, factory) -> {
                    if (be instanceof SignalRailBlockEntity rail) {
                        inputs.add(factory.textInput("name", 300, text("detector.name"), true, rail.getSignName(), 30));
                    } else if (be instanceof SignalDelayerBlockEntity delay) {
                        inputs.add(factory.textInput("ticks", 300, text("delayer.ticks"), true, String.valueOf(delay.getMaxDelayTick()), 5));
                    } else if (be instanceof RemoteBlockEntity remote) {
                        inputs.add(factory.textInput("name", 300, text("wireless.name"), true, remote.getSignalName(), 30));
                        inputs.add(factory.textInput("token", 300, text("wireless.token"), true, remote.getSignalToken(), 30));
                    } else if (be instanceof SpeakerBlockEntity speaker) {
                        String lines = String.join("\n", speaker.getTexts());
                        inputs.add(factory.textInput("message", 300, text("speaker.message"), true,
                                lines.substring(0, Math.min(480, lines.length())), 480,
                                new TextInput.MultilineOptions(Optional.of(4), Optional.of(80))));
                        inputs.add(factory.booleanInput("bell", text("speaker.bell"), speaker.isBellSound(), "true", "false"));
                    }
                });
            });
            if (!readonly) builder.actions(buttons -> buttons.addButton(text("save"), 150,
                    Optional.of(buttons.actionBuilder().customAll(ACTION, additions))));
            else builder.actions(buttons -> buttons.addButton(text("close"), 150, Optional.empty()));
            if (!readonly) builder.exitAction(text("cancel"), 150, Optional.empty());
        }).get().buildOrThrow().open(player);
    }

    public static void handle(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null || player.isSpectator() || !player.mayBuild()
                || packet.payload().isEmpty() || !(packet.payload().get() instanceof CompoundTag tag)) return;
        Session session = SESSIONS.get(player);
        if (session == null || !session.nonce().equals(tag.getStringOr("session", ""))) return;
        SESSIONS.remove(player);
        var level = player.level();
        if (!session.dimension().equals(level.dimension().identifier().toString()) || level.getGameTime() > session.expires()
                || player.distanceToSqr(session.pos().getCenter()) > 64) return;
        BlockState state = level.getBlockState(session.pos());
        BlockEntity be = level.getBlockEntity(session.pos());
        if (state.getBlock() != session.block() || be == null || be != session.entity().get() || be.isRemoved()) return;
        try {
            if (be instanceof SignalRailBlockEntity rail) {
                String name = tag.getStringOr("name", "").strip();
                if (name.length() > 30) throw new IllegalArgumentException();
                rail.setSignName(name); rail.setChanged();
                ((SignalRailBlock) state.getBlock()).checkPressed(level, session.pos(), state);
            } else if (be instanceof SignalDelayerBlockEntity delay) {
                int ticks = Integer.parseInt(tag.getStringOr("ticks", ""));
                if (ticks < 0 || ticks > RedstoneSettings.MAX_DELAY) throw new IllegalArgumentException();
                delay.setMaxDelayTick(ticks);
            } else if (be instanceof RemoteBlockEntity remote) {
                String name = tag.getStringOr("name", "").strip(), token = tag.getStringOr("token", "").strip();
                if (!RedstoneSettings.validChannel(name, token)) throw new IllegalArgumentException();
                remote.configure(name, token);
            } else if (be instanceof SpeakerBlockEntity speaker) {
                String message = tag.getStringOr("message", "");
                String[] lines = message.split("\\R", -1);
                if (message.length() > 480 || lines.length > 4 || Arrays.stream(lines).anyMatch(line -> line.length() > 120))
                    throw new IllegalArgumentException();
                speaker.setTexts(new ArrayList<>(Arrays.asList(lines)));
                speaker.setBellSound("true".equals(tag.getStringOr("bell", "false")) || tag.getBooleanOr("bell", false));
                speaker.setChanged();
            }
            player.sendSystemMessage(text("saved"));
        } catch (IllegalArgumentException invalid) {
            player.sendSystemMessage(text("invalid"));
            open(player, session.pos());
        }
    }
}
