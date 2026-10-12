package cc.thonly.reverie_dreams.data.danmaku.stg.shape;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 发射形状：决定一次齐射中每颗子弹的初始方向与出膛位置。
 *
 * <p>形状只负责“相对基准方向”的分布，绝对朝向由发射器的瞄准模式（{@code EmitAim}）决定。
 * 所有方向参数均为角度制，偏移量单位为格。
 */
public record EmitShape(
        ShapeType type,
        int count,
        float spread,
        float startAngle,
        float radius,
        boolean faceOutward,
        int rings,
        float ringAngleStep,
        float ringRadiusStep,
        float ringSpeedStep,
        List<String> pattern
) {
    public static final Codec<EmitShape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShapeType.CODEC.optionalFieldOf("type", ShapeType.AIM).forGetter(EmitShape::type),
            Codec.INT.optionalFieldOf("count", 1).forGetter(EmitShape::count),
            Codec.FLOAT.optionalFieldOf("spread", 30f).forGetter(EmitShape::spread),
            Codec.FLOAT.optionalFieldOf("start_angle", 0f).forGetter(EmitShape::startAngle),
            Codec.FLOAT.optionalFieldOf("radius", 0f).forGetter(EmitShape::radius),
            Codec.BOOL.optionalFieldOf("face_outward", true).forGetter(EmitShape::faceOutward),
            Codec.INT.optionalFieldOf("rings", 1).forGetter(EmitShape::rings),
            Codec.FLOAT.optionalFieldOf("ring_angle_step", 0f).forGetter(EmitShape::ringAngleStep),
            Codec.FLOAT.optionalFieldOf("ring_radius_step", 0f).forGetter(EmitShape::ringRadiusStep),
            Codec.FLOAT.optionalFieldOf("ring_speed_step", 0f).forGetter(EmitShape::ringSpeedStep),
            Codec.STRING.listOf().optionalFieldOf("pattern", List.of()).forGetter(EmitShape::pattern)
    ).apply(instance, EmitShape::new));

    public static EmitShape aim() {
        return new EmitShape(ShapeType.AIM, 1, 0f, 0f, 0f, true, 1, 0f, 0f, 0f, List.of());
    }

    /**
     * 扇形弹幕：以基准方向为中心均匀散布 count 发。
     */
    public static EmitShape fan(int count, float spread) {
        return new EmitShape(ShapeType.FAN, Math.max(1, count), spread, 0f, 0f, true, 1, 0f, 0f, 0f, List.of());
    }

    /**
     * 圆形弹幕/圆环弹幕。
     *
     * @param faceOutward true 时每颗子弹朝环外飞（圆形弹幕），false 时全部沿基准方向并行飞出（圆环阵列）
     */
    public static EmitShape ring(int count, float radius, boolean faceOutward) {
        return new EmitShape(ShapeType.RING, Math.max(1, count), 0f, 0f, radius, faceOutward, 1, 0f, 0f, 0f, List.of());
    }

    /**
     * 多重圆环：每环旋转 ringAngleStep 度、半径增加 ringRadiusStep、速度增加 ringSpeedStep（比例）。
     */
    public static EmitShape multiRing(int rings, int count, float radius, boolean faceOutward, float ringAngleStep, float ringRadiusStep, float ringSpeedStep) {
        return new EmitShape(ShapeType.MULTI_RING, Math.max(1, count), 0f, 0f, radius, faceOutward, Math.max(1, rings), ringAngleStep, ringRadiusStep, ringSpeedStep, List.of());
    }

    /**
     * 字符矩阵图案（'#' 为子弹），如星形、心形。
     */
    public static EmitShape matrix(List<String> pattern) {
        return new EmitShape(ShapeType.MATRIX, 1, 0f, 0f, 0f, true, 1, 0f, 0f, 0f, List.copyOf(pattern));
    }

    public List<Shot> emit(float basePitch, float baseYaw) {
        return switch (this.type) {
            case AIM -> List.of(new Shot(basePitch, baseYaw, 1f, Vec3.ZERO));
            case FAN -> this.emitFan(basePitch, baseYaw);
            case RING -> this.emitRing(basePitch, baseYaw, this.count, this.radius, this.startAngle, 1f);
            case MULTI_RING -> this.emitMultiRing(basePitch, baseYaw);
            case MATRIX -> this.emitMatrix(basePitch, baseYaw);
        };
    }

    private List<Shot> emitFan(float basePitch, float baseYaw) {
        int bulletCount = Math.max(1, this.count);
        if (bulletCount == 1) {
            return List.of(new Shot(basePitch, baseYaw, 1f, Vec3.ZERO));
        }
        List<Shot> shots = new ArrayList<>(bulletCount);
        float step = this.spread / (bulletCount - 1);
        float first = baseYaw - this.spread / 2.0f;
        for (int i = 0; i < bulletCount; i++) {
            shots.add(new Shot(basePitch, first + step * i, 1f, Vec3.ZERO));
        }
        return shots;
    }

    private List<Shot> emitRing(float basePitch, float baseYaw, int bulletCount, float ringRadius, float ringStartAngle, float speedScale) {
        bulletCount = Math.max(1, bulletCount);
        List<Shot> shots = new ArrayList<>(bulletCount);
        float step = 360.0f / bulletCount;
        for (int i = 0; i < bulletCount; i++) {
            float angle = ringStartAngle + step * i;
            float shotYaw = this.faceOutward ? baseYaw + angle : baseYaw;
            Vec3 offset = this.ringOffset(baseYaw, angle, ringRadius);
            shots.add(new Shot(basePitch, shotYaw, speedScale, offset));
        }
        return shots;
    }

    private List<Shot> emitMultiRing(float basePitch, float baseYaw) {
        List<Shot> shots = new ArrayList<>();
        int ringCount = Math.max(1, this.rings);
        for (int ring = 0; ring < ringCount; ring++) {
            float ringRadius = this.radius + this.ringRadiusStep * ring;
            float speedScale = 1.0f + this.ringSpeedStep * ring;
            List<Shot> ringShots = this.emitRing(
                    basePitch,
                    baseYaw,
                    this.count,
                    ringRadius,
                    this.startAngle + this.ringAngleStep * ring,
                    speedScale
            );
            shots.addAll(ringShots);
        }
        return shots;
    }

    private List<Shot> emitMatrix(float basePitch, float baseYaw) {
        List<String> lines = this.pattern;
        if (lines.isEmpty()) {
            return List.of();
        }
        int height = lines.size();
        int width = lines.stream().mapToInt(String::length).max().orElse(0);
        if (width == 0) {
            return List.of();
        }
        double centerX = (width - 1) / 2.0D;
        double centerY = (height - 1) / 2.0D;

        Vec3 forward = DanmakuMath.direction(basePitch, baseYaw).normalize();
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(up);
        if (right.lengthSqr() < 1.0E-6D) {
            right = forward.cross(new Vec3(0.0D, 0.0D, 1.0D));
        }
        right = right.normalize();

        List<Shot> shots = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            String line = lines.get(row);
            for (int col = 0; col < width; col++) {
                if (col >= line.length() || line.charAt(col) != '#') {
                    continue;
                }
                double localUp = centerY - row;
                double localRight = col - centerX;
                Vec3 offset = right.scale(localRight)
                        .add(up.scale(localUp))
                        .add(forward.scale(2.0D));
                shots.add(new Shot(basePitch, baseYaw, 1f, offset));
            }
        }
        return shots;
    }

    private Vec3 ringOffset(float baseYaw, float angle, float ringRadius) {
        if (ringRadius == 0f) {
            return Vec3.ZERO;
        }
        float worldYaw = baseYaw + angle;
        double yawRad = Math.toRadians(worldYaw);
        return new Vec3(-Math.sin(yawRad) * ringRadius, 0.0D, Math.cos(yawRad) * ringRadius);
    }

    /**
     * 单颗子弹的出膛参数（相对发射器基准方向）。
     */
    public record Shot(float pitch, float yaw, float speedScale, Vec3 offset) {
    }
}
