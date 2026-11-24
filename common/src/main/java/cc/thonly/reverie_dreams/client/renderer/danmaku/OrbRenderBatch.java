package cc.thonly.reverie_dreams.client.renderer.danmaku;

import cc.thonly.reverie_dreams.client.renderer.entity.state.DanmakuEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * Groups flat orb geometry in the existing item-sheet pass, using vanilla's visible entity states.
 */
public final class OrbRenderBatch {
    private OrbRenderBatch() {
    }

    public static void prepare(LevelRenderState level) {
        // Iris shader packs can route custom geometry through a different sampler setup than stock items.
        if (!DanmakuRenderOptions.batchOrbs() || IrisShaderStatus.active()) {
            return;
        }
        Frame frame = null;
        for (var renderState : level.entityRenderStates) {
            if (renderState instanceof DanmakuEntityRenderState state && state.orbSprite != null) {
                if (frame == null) {
                    frame = new Frame();
                }
                frame.expected++;
                state.orbFrame = frame;
            }
        }
    }

    public static final class Frame {
        private Submission pending;
        private int expected;

        public void append(DanmakuEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            Submission submission = pending;
            while (submission != null && (submission.collector != collector || submission.camera != camera || submission.drawn)) {
                submission = submission.next;
            }
            if (submission == null) {
                submission = new Submission(collector, camera, pending, expected);
                pending = submission;
                collector.submitCustomGeometry(pose, Sheets.cutoutItemSheet(), submission);
            }
            submission.add(state, pose.last());
        }
    }

    private static final class Submission implements SubmitNodeCollector.CustomGeometryRenderer {
        private static final int STRIDE = 4;
        private final SubmitNodeCollector collector;
        private final CameraRenderState camera;
        private final Quaternionf rotation;
        private final Submission next;
        private final OrbBuffer buffer;
        private boolean drawn;

        Submission(SubmitNodeCollector collector, CameraRenderState camera, Submission next, int expected) {
            this.collector = collector;
            this.camera = camera;
            this.rotation = new Quaternionf(camera.orientation).rotateY((float) Math.PI);
            this.next = next;
            buffer = new OrbBuffer(expected);
        }

        void add(DanmakuEntityRenderState state, PoseStack.Pose pose) {
            buffer.ensureRoom();
            int i = buffer.count++;
            var matrix = pose.pose();
            int p = i * STRIDE;
            buffer.positionAndScale[p] = matrix.m30();
            buffer.positionAndScale[p + 1] = matrix.m31();
            buffer.positionAndScale[p + 2] = matrix.m32();
            buffer.positionAndScale[p + 3] = state.scale;
            buffer.sprites[i] = state.orbSprite;
            buffer.light[i] = state.lightCoords;
            var dye = state.itemStack.get(DataComponents.DYED_COLOR);
            buffer.color[i] = dye == null ? 0xFFFFFFFF : 0xFF000000 | dye.rgb();
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            // Some renderers replay submitted geometry (e.g. a shader shadow pass).
            // Keep these vertices until this frame is discarded, rather than recycling on the first draw.
            OrbBuffer data = buffer;
            drawn = true;
            var projected = new IdentityHashMap<OrbSpriteCache.Sprite, float[]>();
            Vector3f normal = rotation.transform(0f, 0f, -1f, new Vector3f());
            pose.transformNormal(normal, normal);
            Vector3f offset = pose.pose().transformDirection(0f, 0.25f, 0f, new Vector3f());
            for (int i = 0; i < data.count; i++) {
                var sprite = data.sprites[i];
                float[] vertices = projected.get(sprite);
                if (vertices == null) {
                    vertices = project(sprite, pose, offset);
                    projected.put(sprite, vertices);
                }
                int p = i * STRIDE;
                float x = data.positionAndScale[p];
                float y = data.positionAndScale[p + 1];
                float z = data.positionAndScale[p + 2];
                float scale = data.positionAndScale[p + 3];
                for (int f = 0; f < sprite.faces().length; f++) {
                    int color = sprite.faces()[f].dyed() ? data.color[i] : 0xFFFFFFFF;
                    int end = (f + 1) * 4 * OrbSpriteCache.Face.STRIDE;
                    for (int v = end - 4 * OrbSpriteCache.Face.STRIDE; v < end; v += OrbSpriteCache.Face.STRIDE) {
                        consumer.addVertex(x + vertices[v] * scale, y + vertices[v + 1] * scale, z + vertices[v + 2] * scale,
                                color, vertices[v + 3], vertices[v + 4], OverlayTexture.NO_OVERLAY, data.light[i],
                                normal.x, normal.y, normal.z);
                    }
                }
            }
        }

        private float[] project(OrbSpriteCache.Sprite sprite, PoseStack.Pose pose, Vector3f offset) {
            float[] result = new float[sprite.faces().length * 4 * OrbSpriteCache.Face.STRIDE];
            Vector3f vertex = new Vector3f();
            int i = 0;
            for (var face : sprite.faces()) {
                var source = face.vertices();
                for (int v = 0; v < source.length; v += OrbSpriteCache.Face.STRIDE) {
                    rotation.transform(source[v], source[v + 1], source[v + 2], vertex);
                    pose.pose().transformDirection(vertex).add(offset);
                    result[i++] = vertex.x;
                    result[i++] = vertex.y;
                    result[i++] = vertex.z;
                    result[i++] = source[v + 3];
                    result[i++] = source[v + 4];
                }
            }
            return result;
        }
    }

    private static final class OrbBuffer {
        private int count;
        private OrbSpriteCache.Sprite[] sprites;
        private float[] positionAndScale;
        private int[] light;
        private int[] color;

        private OrbBuffer(int expected) {
            sprites = new OrbSpriteCache.Sprite[expected];
            positionAndScale = new float[expected * Submission.STRIDE];
            light = new int[expected];
            color = new int[expected];
        }

        private void ensureRoom() {
            if (count == sprites.length) {
                int capacity = sprites.length * 2;
                sprites = Arrays.copyOf(sprites, capacity);
                positionAndScale = Arrays.copyOf(positionAndScale, capacity * Submission.STRIDE);
                light = Arrays.copyOf(light, capacity);
                color = Arrays.copyOf(color, capacity);
            }
        }
    }
}
