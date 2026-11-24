package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkBeam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * Untextured additive geometry: a fixed vertex budget, independent of beam length or particle settings.
 */
public final class MasterSparkRenderer {
    private static final int SIDES = 32;

    private MasterSparkRenderer() {
    }

    public static void submit(PoseStack matrices, SubmitNodeCollector collector, float age, float charge, float strength, float length) {
        if (charge <= 0 && strength <= 0)
            return;
        collector.submitCustomGeometry(matrices, RenderTypes.lightning(), (pose, vertices) ->
                emitGeometry(pose, vertices, age, charge, strength, length));
    }

    static void emitGeometry(PoseStack.Pose pose, VertexConsumer vertices, float age, float charge, float strength, float length) {
        float energy = Math.max(charge, strength);
        float radius = 0.35f + energy * 0.8f;
        ring(vertices, pose, radius * 0.91f, radius, 0, age * 0.035f, 0x7EEAFF, 190);
        ring(vertices, pose, radius * 1.14f, radius * 1.18f, -0.12f, -age * 0.026f, 0xC0FFFF, 120);
        // Eight revolving short glyphs around the outer circle.
        for (int i = 0; i < 8; i++) {
            float angle = age * 0.035f + i * Mth.TWO_PI / 8;
            arc(vertices, pose, radius * 1.22f, radius * 1.39f, -0.1f, angle, angle + 0.12f, 0xA4FFFF, 200);
        }
        if (strength <= 0 || length <= 0) {
            cone(vertices, pose, 0.08f + energy * 0.18f, 0.02f, 0.04f, 0xFFFFFF, (int) (energy * 230));
            return;
        }
        float near = (float) MasterSparkBeam.NEAR_RADIUS * strength;
        float far = (float) MasterSparkBeam.radiusAt(length) * strength;
        // Bright core, cyan envelope and rainbow fringe; all share the hit beam's axis.
        for (int i = 0; i < SIDES; i++) {
            int color = Mth.hsvToRgb((i / (float) SIDES + age * 0.008f) % 1, 0.55f, 1);
            coneSide(vertices, pose, near, far, length, i, color, 70);
        }
        cone(vertices, pose, near * 0.78f, far * 0.78f, length, 0x9AF9FF, 155);
        cone(vertices, pose, near * 0.52f, far * 0.52f, length, 0xFFFFFF, 255);
        // Sparse traveling rings convey forward power without spawning entities.
        for (int i = 0; i < 3; i++) {
            float z = (age * 1.15f + i * 13.0f) % Math.max(0.01f, length);
            float r = (float) MasterSparkBeam.radiusAt(z) * strength;
            ring(vertices, pose, r, r + 0.06f, z, age * 0.02f, 0xC7FFFF, 115);
        }
    }

    private static void cone(VertexConsumer v, PoseStack.Pose pose, float near, float far, float length, int color, int alpha) {
        for (int i = 0; i < SIDES; i++)
            coneSide(v, pose, near, far, length, i, color, alpha);
        // End cap keeps the core solid when looking along the cannon.
        for (int i = 0; i < SIDES; i++) {
            float a = i * Mth.TWO_PI / SIDES, b = (i + 1) * Mth.TWO_PI / SIDES;
            vertex(v, pose, 0, 0, length, color, alpha);
            vertex(v, pose, Mth.cos(a) * far, Mth.sin(a) * far, length, color, alpha);
            vertex(v, pose, Mth.cos(b) * far, Mth.sin(b) * far, length, color, alpha);
            vertex(v, pose, 0, 0, length, color, alpha);
        }
    }

    private static void coneSide(VertexConsumer v, PoseStack.Pose pose, float near, float far, float length, int side, int color, int alpha) {
        float a = side * Mth.TWO_PI / SIDES, b = (side + 1) * Mth.TWO_PI / SIDES;
        vertex(v, pose, Mth.cos(a) * near, Mth.sin(a) * near, 0, color, alpha);
        vertex(v, pose, Mth.cos(b) * near, Mth.sin(b) * near, 0, color, alpha);
        vertex(v, pose, Mth.cos(b) * far, Mth.sin(b) * far, length, color, alpha);
        vertex(v, pose, Mth.cos(a) * far, Mth.sin(a) * far, length, color, alpha);
    }

    private static void ring(VertexConsumer v, PoseStack.Pose pose, float inner, float outer, float z, float rotation, int color, int alpha) {
        for (int i = 0; i < SIDES; i++)
            arc(v, pose, inner, outer, z, rotation + i * Mth.TWO_PI / SIDES, rotation + (i + 1) * Mth.TWO_PI / SIDES, color, alpha);
    }

    private static void arc(VertexConsumer v, PoseStack.Pose pose, float inner, float outer, float z, float a, float b, int color, int alpha) {
        vertex(v, pose, Mth.cos(a) * inner, Mth.sin(a) * inner, z, color, alpha);
        vertex(v, pose, Mth.cos(b) * inner, Mth.sin(b) * inner, z, color, alpha);
        vertex(v, pose, Mth.cos(b) * outer, Mth.sin(b) * outer, z, color, alpha);
        vertex(v, pose, Mth.cos(a) * outer, Mth.sin(a) * outer, z, color, alpha);
    }

    private static void vertex(VertexConsumer v, PoseStack.Pose pose, float x, float y, float z, int color, int alpha) {
        v.addVertex(pose, x, y, z).setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha);
    }
}
