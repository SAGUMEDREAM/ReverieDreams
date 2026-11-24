package cc.thonly.reverie_dreams.server.dialog;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.RecordPlayerMedia;
import cc.thonly.reverie_dreams.block.entity.RecordPlayerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/** Explicit-save flow built with the project's Dialog API; cancel/Esc never applies edits. */
public final class RecordPlayerDialog {
    public static final Identifier ACTION = ReverieDreams.id("record_player/configure");
    private record Session(BlockPos pos, String dimension, WeakReference<RecordPlayerBlockEntity> entity,
                           String nonce, long expires) { }
    private static final Map<ServerPlayer, Session> SESSIONS = new WeakHashMap<>();
    private RecordPlayerDialog() { }
    public static void clear() { SESSIONS.clear(); }
    private static Component text(String key) { return Component.translatable("dialog.reverie_dreams.record_player." + key); }
    public static void open(ServerPlayer player, BlockPos pos) {
        var level = player.level();
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)
                || player.distanceToSqr(pos.getCenter()) > 64
                || !(level.getBlockEntity(pos) instanceof RecordPlayerBlockEntity entity)) return;
        Session session = new Session(pos.immutable(), level.dimension().identifier().toString(), new WeakReference<>(entity),
                UUID.randomUUID().toString(), level.getGameTime() + 2400);
        SESSIONS.put(player, session);
        CompoundTag data = new CompoundTag(); data.putString("session", session.nonce());
        DialogBuilder.builder(builder -> {
            builder.key(ACTION).common(common -> {
                common.title(text("title")).setPause(false).setCanCloseWithEscape(true).setAfterAction(DialogAction.CLOSE);
                common.addTextBody(text("help"), 310);
                common.addTextBody(text("resume"), 310);
                common.input((inputs, factory) -> inputs.add(factory.textInput("url", 310, text("url"), true,
                        entity.mediaUrl(), RecordPlayerMedia.MAX_URL_LENGTH)));
            });
            builder.actions(buttons -> {
                buttons.addButton(text("save"), 150, Optional.of(buttons.actionBuilder().customAll(ACTION, data)));
                CompoundTag defaults = data.copy(); defaults.putBoolean("default", true);
                buttons.addButton(text("default"), 150, Optional.of(buttons.actionBuilder().customAll(ACTION, defaults)));
            });
            builder.exitAction(text("cancel"), 150, Optional.empty());
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
                || player.distanceToSqr(session.pos().getCenter()) > 64 || !level.mayInteract(player, session.pos())
                || !level.hasChunkAt(session.pos()) || !(level.getBlockEntity(session.pos()) instanceof RecordPlayerBlockEntity entity)
                || entity != session.entity().get() || entity.isRemoved()) return;
        try {
            entity.configure(tag.getBooleanOr("default", false) ? RecordPlayerMedia.DEFAULT_URL : tag.getStringOr("url", ""));
            player.sendSystemMessage(text("saved"));
        } catch (IllegalArgumentException invalid) {
            player.sendSystemMessage(text("invalid"));
            open(player, session.pos());
        }
    }
}
