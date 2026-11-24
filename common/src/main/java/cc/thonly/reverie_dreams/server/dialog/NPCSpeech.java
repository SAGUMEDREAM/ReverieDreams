package cc.thonly.reverie_dreams.server.dialog;

import cc.thonly.reverie_dreams.data.npc.NPCSpeechStyle;
import cc.thonly.reverie_dreams.mixin.accessor.DisplayAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.TextDisplayAccessor;
import com.mojang.math.Transformation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Delivers NPC dialogue to one player. Call from the logical server thread. */
public final class NPCSpeech {
    private static final double RANGE_SQUARED = 24 * 24;
    private static final int FADE_TICKS = 10;
    private static final int MAX_REPLY_PREVIEW = 80;
    private static final Map<MinecraftServer, Map<ServerPlayer, Bubble>> ACTIVE = new HashMap<>();

    private NPCSpeech() {
    }

    public static void sayTo(Entity speaker, ServerPlayer listener, Component line) {
        if (canSend(listener) && !show(speaker, listener, line)) {
            listener.sendSystemMessage(Component.empty().append(speaker.getName()).append(": ").append(line));
        }
    }

    /** Shows a short line above the speaker and keeps the full message in chat. */
    public static void sayWithTranscript(Entity speaker, ServerPlayer listener, Component preview, Component transcript) {
        if (canSend(listener)) {
            listener.sendSystemMessage(transcript);
            show(speaker, listener, preview);
        }
    }

    public static void sayReply(Entity speaker, ServerPlayer listener, String reply) {
        String preview = reply.replaceAll("\\s+", " ").strip();
        if (preview.codePointCount(0, preview.length()) > MAX_REPLY_PREVIEW) {
            int end = preview.offsetByCodePoints(0, MAX_REPLY_PREVIEW - 1);
            preview = preview.substring(0, end).stripTrailing() + "…";
        }
        sayWithTranscript(speaker, listener, Component.literal(preview),
                Component.empty().append("<").append(speaker.getDisplayName()).append("> ").append(reply));
    }

    private static boolean show(Entity speaker, ServerPlayer listener, Component line) {
        if (line.getString().isBlank()) {
            return true;
        }
        if (!(speaker.level() instanceof ServerLevel level) || !canSee(speaker, listener)) {
            return false;
        }

        var bubbles = ACTIVE.computeIfAbsent(level.getServer(), server -> new HashMap<>());
        Bubble previous = bubbles.get(listener);
        if (previous != null && previous.speaker == speaker && previous.body.level() == level) {
            previous.updateLine(listener, line);
        } else {
            if (previous != null) {
                previous.remove(listener);
            }
            bubbles.put(listener, new Bubble(speaker, listener, line));
        }
        return true;
    }

    public static void tick(MinecraftServer server) {
        var bubbles = ACTIVE.get(server);
        if (bubbles == null) {
            return;
        }
        for (var iterator = bubbles.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            ServerPlayer listener = entry.getKey();
            Bubble bubble = entry.getValue();
            if (--bubble.remaining <= 0 || !canSend(listener) || !canSee(bubble.speaker, listener)
                    || bubble.body.level() != listener.level()) {
                bubble.remove(listener);
                iterator.remove();
            } else if (server.getTickCount() % 2 == 0) {
                bubble.follow(listener);
                if (bubble.remaining <= FADE_TICKS) {
                    bubble.setOpacity(bubble.remaining * 255 / FADE_TICKS);
                    sendMetadata(listener, bubble.body);
                    sendMetadata(listener, bubble.tail);
                }
            }
        }
        if (bubbles.isEmpty()) {
            ACTIVE.remove(server);
        }
    }

    public static void clear(MinecraftServer server) {
        ACTIVE.remove(server);
    }

    private static boolean canSend(ServerPlayer listener) {
        return listener.connection != null && !listener.hasDisconnected();
    }

    private static boolean canSee(Entity speaker, ServerPlayer listener) {
        return speaker.isAlive() && listener.isAlive() && speaker.level() == listener.level()
                && speaker.distanceToSqr(listener) <= RANGE_SQUARED;
    }

    private static Display.TextDisplay display(Entity speaker, float scale, float yOffset) {
        var display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, speaker.level());
        var settings = (DisplayAccessor) display;
        settings.reverie_dreams$setBillboardConstraints(Display.BillboardConstraints.CENTER);
        settings.reverie_dreams$setBrightnessOverride(Brightness.FULL_BRIGHT);
        settings.reverie_dreams$setPosRotInterpolationDuration(2);
        settings.reverie_dreams$setTransformation(new Transformation(new Vector3f(0, yOffset, 0), null,
                new Vector3f(scale), null));
        return display;
    }

    private static void sendMetadata(ServerPlayer listener, Display.TextDisplay display) {
        var values = display.getEntityData().packDirty();
        if (values != null) {
            listener.connection.send(new ClientboundSetEntityDataPacket(display.getId(), values));
        }
    }

    private static ClientboundAddEntityPacket addPacket(Display.TextDisplay display) {
        return new ClientboundAddEntityPacket(display.getId(), display.getUUID(), display.getX(),
                display.getY(), display.getZ(), 0, 0, EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0);
    }

    private static ClientboundSetEntityDataPacket initialData(Display.TextDisplay display) {
        display.getEntityData().packDirty();
        return new ClientboundSetEntityDataPacket(display.getId(), display.getEntityData().getNonDefaultValues());
    }

    private static final class Bubble {
        private final Entity speaker;
        private final Display.TextDisplay body;
        private final Display.TextDisplay tail;
        private int remaining;

        private Bubble(Entity speaker, ServerPlayer listener, Component line) {
            this.speaker = speaker;
            this.body = display(speaker, NPCSpeechStyle.SCALE, 0);
            var bodyStyle = (TextDisplayAccessor) this.body;
            bodyStyle.reverie_dreams$setLineWidth(NPCSpeechStyle.LINE_WIDTH);
            bodyStyle.reverie_dreams$setFlags(Display.TextDisplay.FLAG_SHADOW);
            this.tail = display(speaker, 0.5F, -0.10F);
            var tailStyle = (TextDisplayAccessor) this.tail;
            tailStyle.reverie_dreams$setBackgroundColor(0);
            tailStyle.reverie_dreams$setText(Component.literal("▼").withColor(NPCSpeechStyle.BACKGROUND_RGB));
            Vec3 anchor = this.anchor();
            this.body.setPos(anchor);
            this.tail.setPos(anchor);
            this.setLine(line);

            // These displays only exist on the listener's client.
            listener.connection.send(new ClientboundBundlePacket(List.of(
                    addPacket(this.body), initialData(this.body),
                    addPacket(this.tail), initialData(this.tail)
            )));
        }

        private void setLine(Component line) {
            String text = line.getString();
            this.remaining = Math.clamp(55 + 2 * text.codePointCount(0, text.length()), 70, 180);
            ((TextDisplayAccessor) this.body).reverie_dreams$setText(
                    NPCSpeechStyle.format(this.speaker, line));
            this.setOpacity(255);
        }

        private void updateLine(ServerPlayer listener, Component line) {
            this.setLine(line);
            sendMetadata(listener, this.body);
            sendMetadata(listener, this.tail);
        }

        private void setOpacity(int opacity) {
            int backdropOpacity = opacity * NPCSpeechStyle.BACKGROUND_ALPHA / 255;
            var bodyStyle = (TextDisplayAccessor) this.body;
            bodyStyle.reverie_dreams$setTextOpacity((byte) opacity);
            bodyStyle.reverie_dreams$setBackgroundColor(backdropOpacity << 24 | NPCSpeechStyle.BACKGROUND_RGB);
            ((TextDisplayAccessor) this.tail).reverie_dreams$setTextOpacity((byte) backdropOpacity);
        }

        private Vec3 anchor() {
            return this.speaker.position().add(0, this.speaker.getBbHeight() + 0.65, 0);
        }

        private void follow(ServerPlayer listener) {
            Vec3 anchor = this.anchor();
            if (!anchor.equals(this.body.position())) {
                this.body.setPos(anchor);
                this.tail.setPos(anchor);
                listener.connection.send(new ClientboundBundlePacket(List.of(
                        ClientboundEntityPositionSyncPacket.of(this.body),
                        ClientboundEntityPositionSyncPacket.of(this.tail)
                )));
            }
        }

        private void remove(ServerPlayer listener) {
            if (canSend(listener)) {
                listener.connection.send(new ClientboundRemoveEntitiesPacket(this.body.getId(), this.tail.getId()));
            }
        }
    }
}
