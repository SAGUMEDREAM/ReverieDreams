package cc.thonly.reverie_dreams.data.danmaku.stg;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * STG 弹幕引擎通用数学工具。
 *
 * <p>角度约定与 MC 保持一致：pitch 向下为正、yaw 使用 {@code atan2(-dx, dz)}；
 * 旋转类操作统一为绕给定轴的“右手方向”旋转。
 */
public final class DanmakuMath {
    private DanmakuMath() {
    }

    /**
     * 由 pitch/yaw 计算出射方向，与 {@code Projectile#shootFromRotation} 一致。
     */
    public static Vec3 direction(float pitch, float yaw) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3(x, y, z);
    }

    /**
     * 由两点计算 pitch/yaw，返回 {@code [pitch, yaw]}。
     */
    public static float[] pitchYaw(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
        float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        return new float[]{pitch, yaw};
    }

    /**
     * 绕 Y 轴右手方向旋转（与 JOML {@code rotateAxis} 一致），保持长度与高度。
     */
    public static Vec3 rotateY(Vec3 vector, float degrees) {
        if (vector.lengthSqr() < 1.0E-8D || degrees == 0f) {
            return vector;
        }
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                vector.x * cos + vector.z * sin,
                vector.y,
                -vector.x * sin + vector.z * cos
        );
    }

    /**
     * 把水平向量转换为 yaw（度）。
     */
    public static float yawOf(Vec3 vector) {
        return (float) Math.toDegrees(Math.atan2(-vector.x, vector.z));
    }

    public static double clamp(double value, double min, double max) {
        return Mth.clamp(value, min, max);
    }
}
