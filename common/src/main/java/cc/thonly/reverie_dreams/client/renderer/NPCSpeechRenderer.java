package cc.thonly.reverie_dreams.client.renderer;

import cc.thonly.reverie_dreams.data.npc.NPCSpeechStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Pixel-framed speech using Minecraft's normal text/background pipelines. No Jade dependency.
 */
public final class NPCSpeechRenderer {
    private static WeakReference<Entity> lastSpeaker = new WeakReference<>(null);
    private static WeakReference<Display.TextDisplay> lastBubble = new WeakReference<>(null);
    private static long nextLookup;

    private NPCSpeechRenderer() {
    }

    public static boolean render(TextDisplayEntityRenderState state, PoseStack poses, SubmitNodeCollector collector,
                                 int light, float interpolation) {
        var text = state.textRenderState;
        if (text == null || !NPCSpeechStyle.isSpeech(text.text())) {
            return false;
        }
        var cached = state.cachedInfo;
        int width = cached.width() + NPCSpeechStyle.PADDING * 2;
        int height = panelHeight(cached);
        int opacity = text.textOpacity().get(interpolation) & 255;
        int background = text.backgroundColor().get(interpolation);
        int backdropAlpha = background >>> 24;
        float fit = fitToViewport(new Vec3(state.x, state.y, state.z), width, height);
        poses.pushPose();
        try {
            poses.last().pose().rotate((float) Math.PI, 0, 1, 0)
                    .scale(-0.025F * fit, -0.025F * fit, -0.025F * fit).translate(-width / 2F, -height, 0);
            quad(poses, collector, light, 1, 1, width - 1, height - 1, background);
            quad(poses, collector, light, 1, 0, width - 1, 1, color(NPCSpeechStyle.NAME_COLOR, backdropAlpha));
            quad(poses, collector, light, 0, 1, 1, height - 1, color(NPCSpeechStyle.NAME_COLOR, backdropAlpha));
            quad(poses, collector, light, 1, height - 1, width - 1, height, color(NPCSpeechStyle.BORDER_COLOR, backdropAlpha));
            quad(poses, collector, light, width - 1, 1, width, height - 1, color(NPCSpeechStyle.BORDER_COLOR, backdropAlpha));
            if (cached.lines().size() > 1) {
                int divider = NPCSpeechStyle.PADDING + NPCSpeechStyle.LINE_HEIGHT - 2;
                quad(poses, collector, light, NPCSpeechStyle.PADDING, divider, width - NPCSpeechStyle.PADDING,
                        divider + 1, color(NPCSpeechStyle.BORDER_COLOR, backdropAlpha));
            }
            var font = collector.order(1);
            int y = NPCSpeechStyle.PADDING;
            for (var line : cached.lines()) {
                font.submitText(poses, NPCSpeechStyle.PADDING, y, line.contents(),
                        (text.flags() & Display.TextDisplay.FLAG_SHADOW) != 0,
                        net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET,
                        light, color(0xFFFFFF, opacity), 0, 0);
                y += NPCSpeechStyle.LINE_HEIGHT;
            }
        } finally {
            poses.popPose();
        }
        return true;
    }

    private static int panelHeight(Display.TextDisplay.CachedInfo info) {
        return NPCSpeechStyle.PADDING * 2 + info.lines().size() * NPCSpeechStyle.LINE_HEIGHT - 3;
    }

    private static int color(int rgb, int opacity) {
        return opacity << 24 | rgb;
    }

    private static void quad(PoseStack poses, SubmitNodeCollector collector, int light,
                             float left, float top, float right, float bottom, int color) {
        collector.submitCustomGeometry(poses, RenderTypes.textBackground(), (pose, vertices) -> {
            vertices.addVertex(pose, left, top, 0).setColor(color).setLight(light);
            vertices.addVertex(pose, left, bottom, 0).setColor(color).setLight(light);
            vertices.addVertex(pose, right, bottom, 0).setColor(color).setLight(light);
            vertices.addVertex(pose, right, top, 0).setColor(color).setLight(light);
        });
    }

    public record Bounds(float x, float y, float width, float height) {
        public boolean intersects(Bounds other) {
            return this.x < other.x + other.width && this.x + this.width > other.x
                    && this.y < other.y + other.height && this.y + this.height > other.y;
        }

        private Bounds inflate(float margin) {
            return new Bounds(this.x - margin, this.y - margin, this.width + margin * 2, this.height + margin * 2);
        }
    }

    /**
     * A temporary render position. The caller keeps its persistent overlay configuration.
     */
    @Nullable
    public static Vec2 avoidTooltip(Entity speaker, Bounds desired) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.screen != null) {
            return null;
        }
        Bounds speech = projectedBounds(speaker);
        if (speech == null) {
            return null;
        }
        return placeTooltip(desired, speech, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
    }

    @Nullable
    public static Vec2 placeTooltip(Bounds desired, Bounds speech, int viewportWidth, int viewportHeight) {
        Bounds protectedArea = speech.inflate(6);
        if (!desired.intersects(protectedArea)) {
            return null;
        }
        float maxX = viewportWidth - desired.width - 8;
        float maxY = viewportHeight - desired.height - 32;
        if (maxX < 8 || maxY < 8) {
            return null;
        }
        float x = Math.clamp(desired.x, 8, maxX);
        float y = Math.clamp(desired.y, 8, maxY);
        float below = protectedArea.y + protectedArea.height + 6;
        for (Vec2 candidate : new Vec2[]{new Vec2(maxX, y), new Vec2(8, y),
                new Vec2(maxX, below), new Vec2(8, below), new Vec2(x, 8), new Vec2(x, maxY)}) {
            if (candidate.y < 8 || candidate.y > maxY) {
                continue;
            }
            if (!new Bounds(candidate.x, candidate.y, desired.width, desired.height).intersects(protectedArea)) {
                return candidate;
            }
        }
        return null;
    }

    @Nullable
    public static Bounds projectedBounds(Entity speaker) {
        Minecraft client = Minecraft.getInstance();
        Display.TextDisplay bubble = findBubble(speaker);
        if (bubble == null || client.level == null) {
            return null;
        }
        var info = bubble.cacheDisplay(NPCSpeechRenderer::splitLines);
        float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 anchor = bubble.getPosition(partial);
        double factor = NPCSpeechStyle.SCALE * 0.025
                * fitToViewport(anchor, info.width() + NPCSpeechStyle.PADDING * 2, panelHeight(info));
        double halfWidth = (info.width() + NPCSpeechStyle.PADDING * 2) * factor / 2;
        double height = panelHeight(info) * factor;
        var camera = client.gameRenderer.getMainCamera();
        Vec3 right = new Vec3(camera.rotation().transform(new Vector3f(1, 0, 0)));
        Vec3 up = new Vec3(camera.rotation().transform(new Vector3f(0, 1, 0)));
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (int side : new int[]{-1, 1}) {
            for (int top = 0; top <= 1; top++) {
                Vec3 point = client.gameRenderer.projectPointToScreen(anchor.add(right.scale(side * halfWidth)).add(up.scale(top * height)));
                if (!Double.isFinite(point.x) || !Double.isFinite(point.y) || point.z < 0 || point.z > 1) {
                    return null;
                }
                float screenX = (float) ((point.x + 1) * client.getWindow().getGuiScaledWidth() / 2);
                float screenY = (float) ((1 - point.y) * client.getWindow().getGuiScaledHeight() / 2);
                minX = Math.min(minX, screenX);
                minY = Math.min(minY, screenY);
                maxX = Math.max(maxX, screenX);
                maxY = Math.max(maxY, screenY);
            }
        }
        return new Bounds(minX, minY, maxX - minX, maxY - minY);
    }

    private static float fitToViewport(Vec3 anchor, int width, int height) {
        var client = Minecraft.getInstance();
        var camera = client.gameRenderer.getMainCamera();
        double guiWidth = client.getWindow().getGuiScaledWidth();
        double guiHeight = client.getWindow().getGuiScaledHeight();
        Vec3 center = client.gameRenderer.projectPointToScreen(anchor);
        Vec3 up = new Vec3(camera.rotation().transform(new Vector3f(0, 1, 0)));
        Vec3 right = new Vec3(camera.rotation().transform(new Vector3f(1, 0, 0)));
        double worldPixel = NPCSpeechStyle.SCALE * 0.025;
        Vec3 top = client.gameRenderer.projectPointToScreen(anchor.add(up.scale(height * worldPixel)));
        Vec3 side = client.gameRenderer.projectPointToScreen(anchor.add(right.scale(width * worldPixel / 2)));
        double desiredHeight = (top.y - center.y) * guiHeight / 2;
        double desiredHalfWidth = Math.abs(side.x - center.x) * guiWidth / 2;
        double availableTop = (1 - center.y) * guiHeight / 2 - 8;
        double centerX = (center.x + 1) * guiWidth / 2;
        double availableSide = Math.min(centerX - 8, guiWidth - centerX - 8);
        if (desiredHeight <= 0 || desiredHalfWidth <= 0 || availableTop <= 0 || availableSide <= 0) {
            return 1;
        }
        return (float) Math.clamp(Math.min(availableTop / desiredHeight, availableSide / desiredHalfWidth), 0.35, 1);
    }

    @Nullable
    private static Display.TextDisplay findBubble(Entity speaker) {
        if (speaker.level() != Minecraft.getInstance().level) {
            return null;
        }
        if (lastSpeaker.get() != speaker) {
            lastSpeaker = new WeakReference<>(speaker);
            lastBubble = new WeakReference<>(null);
            nextLookup = 0;
        }
        var cached = lastBubble.get();
        if (cached != null && !cached.isRemoved()) {
            return cached;
        }
        long tick = speaker.level().getGameTime();
        if (tick < nextLookup) {
            return null;
        }
        nextLookup = tick + 4;
        var displays = speaker.level().getEntitiesOfClass(Display.TextDisplay.class,
                speaker.getBoundingBox().inflate(2).expandTowards(0, 4, 0),
                display -> display.textRenderState() != null && NPCSpeechStyle.belongsTo(display.textRenderState().text(), speaker));
        if (displays.isEmpty()) {
            return null;
        }
        lastBubble = new WeakReference<>(displays.getFirst());
        return displays.getFirst();
    }

    private static Display.TextDisplay.CachedInfo splitLines(Component text, int width) {
        var font = Minecraft.getInstance().font;
        var lines = new ArrayList<Display.TextDisplay.CachedLine>();
        int widest = 0;
        for (var line : font.split(text, width)) {
            int size = font.width(line);
            widest = Math.max(widest, size);
            lines.add(new Display.TextDisplay.CachedLine(line, size));
        }
        return new Display.TextDisplay.CachedInfo(lines, widest);
    }
}
