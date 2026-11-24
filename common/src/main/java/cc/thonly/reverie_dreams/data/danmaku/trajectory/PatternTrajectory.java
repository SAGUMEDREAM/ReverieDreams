package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.data.danmaku.GlyphPattern;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Static character pattern placed on a plane perpendicular to the view direction.
 */
public class PatternTrajectory extends DanmakuTrajectory {
    public final GlyphPattern pattern;
    private final float spacing;
    private final float forwardDistance;

    public PatternTrajectory(GlyphPattern pattern) {
        this(pattern, 1.0f, 2.0f);
    }

    public PatternTrajectory(GlyphPattern pattern, float spacing, float forwardDistance) {
        this.pattern = pattern;
        this.spacing = Math.max(0.0f, spacing);
        this.forwardDistance = Math.max(0.0f, forwardDistance);
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack,
                    double x, double y, double z, float xRot, float yRot,
                    float divergence, float offsetDist, IDanmakuItem pThis, DanmakuTrajectoryConfig config) {
        float spacing = config.spacingOr(this.spacing);
        float forwardDistance = config.forwardOr(this.forwardDistance);
        char[][] chars = this.pattern.getChars();
        int height = chars.length;
        int width = height > 0 ? chars[0].length : 0;
        if (width == 0 || height == 0) {
            return;
        }

        double centerX = (width - 1) / 2.0;
        double centerY = (height - 1) / 2.0;

        double yawRad = Math.toRadians(yRot);
        double pitchRad = Math.toRadians(xRot);

        // Positive Minecraft pitch looks down, so the Y component is negative.
        Vec3 forward = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad)
        ).normalize();

        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1, 0, 0);
        }
        right = right.normalize();
        // Derive screen-up from the view basis so patterns stay upright and
        // do not collapse when looking straight up or down.
        Vec3 up = right.cross(forward).normalize();

        double forwardDist = Math.max(offsetDist, forwardDistance);
        Vec3 base = new Vec3(x, y, z).add(forward.scale(forwardDist));

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (chars[row][col] != '#') {
                    continue;
                }

                double localUp = (centerY - row) * spacing;
                double localRight = (col - centerX) * spacing;
                Vec3 spawnPos = base
                        .add(right.scale(localRight))
                        .add(up.scale(localUp));

                // The whole plane is already shifted forward by forwardDist.
                spawnByItemStack(world, livingEntity,
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        stack, xRot, yRot, divergence, 0.0f);
            }
        }
    }

    @Override
    public DanmakuTrajectoryConfig defaultConfig() {
        return new DanmakuTrajectoryConfig(
                DanmakuTrajectoryConfig.UNSET_INT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.UNSET_FLOAT,
                DanmakuTrajectoryConfig.UNSET_INT,
                this.spacing,
                this.forwardDistance,
                DanmakuTrajectoryConfig.FACE_DEFAULT
        );
    }
}
