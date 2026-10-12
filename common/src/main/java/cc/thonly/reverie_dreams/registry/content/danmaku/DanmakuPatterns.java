package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.Pattern;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeFunctions;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.function.KeyframeFunction;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 内置 STG 图案（符卡）。
 *
 * <p>内置图案以 Java 注册；数据包图案放在 {@code data/<namespace>/danmaku_pattern/<name>.json}
 * 中，随资源重载热更新。两者使用同一注册表 {@code danmaku_pattern}。
 */
@Slf4j
public class DanmakuPatterns {
    public static final String RESOURCE_DIRECTORY = "danmaku_pattern";

    public static void bootstrap(RegistryProvider<DanmakuPattern> registry) {
        // 道中基础弹幕
        register(registry, "stg/aimed_fan", aimedFan());
        register(registry, "stg/fan", fan());
        register(registry, "stg/circle", circle());
        register(registry, "stg/star_matrix", starMatrix());
        // BOSS 符卡：螺旋 / 变速旋转 / 波与粒子的境界 / 花型
        register(registry, "stg/spiral", spiral());
        register(registry, "stg/accelerating_rotation", acceleratingRotation());
        register(registry, "stg/wave_particle", waveParticle());
        register(registry, "stg/flower", flower());
    }

    public static void reload(ResourceManager manager) {
        RegistryProvider<DanmakuPattern> registry = BuiltInRegistryProviders.DANMAKU_PATTERN;
        Map<Identifier, Resource> resources = manager.listResources(RESOURCE_DIRECTORY, id -> id.getPath().endsWith(".json"));
        String prefix = RESOURCE_DIRECTORY + "/";
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            String path = resourceId.getPath();
            Identifier id = Identifier.fromNamespaceAndPath(
                    resourceId.getNamespace(),
                    path.substring(prefix.length(), path.length() - ".json".length())
            );
            try (InputStream stream = entry.getValue().open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<DanmakuPattern> result = DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<DanmakuPattern> optional = result.resultOrPartial(error -> log.error("Can't parse danmaku pattern {}: {}", id, error));
                optional.ifPresent(pattern -> BuiltInRegistryProviders.register(registry, id, pattern));
            } catch (Exception err) {
                log.error("Can't load danmaku pattern {}", id, err);
            }
        }
    }

    private static void register(RegistryProvider<DanmakuPattern> registry, String path, DanmakuPattern pattern) {
        BuiltInRegistryProviders.registerForBuiltin(registry, ReverieDreams.id(path), pattern);
    }

    // ------------------------------------------------------------------
    // 内置图案（对照《弹幕射击游戏（STG）程序设计》中的例子）
    // ------------------------------------------------------------------

    /**
     * 假追踪扇形：每次齐射瞄准目标方向打出一个扇形。
     */
    public static DanmakuPattern aimedFan() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.fan(5, 20f), DanmakuBulletSpec.of(ReverieDreams.id("star"), 0.9f))
                .withTiming(0, 80, 10);
        return DanmakuPattern.of(emitter);
    }

    /**
     * 扇形弹幕：固定朝向下均匀散布。
     */
    public static DanmakuPattern fan() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.fan(7, 60f), DanmakuBulletSpec.of(ReverieDreams.id("rice"), 0.9f))
                .withTiming(0, 60, 10)
                .withAim(EmitAim.FIXED, 0f, 0f);
        return DanmakuPattern.of(emitter);
    }

    /**
     * 圆形弹幕：一圈 16 发朝外散射。
     */
    public static DanmakuPattern circle() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.ring(16, 0f, true), DanmakuBulletSpec.of(ReverieDreams.id("ball"), 0.7f).withRandomColor())
                .withTiming(0, 60, 20)
                .withAim(EmitAim.FIXED, 0f, 0f);
        return DanmakuPattern.of(emitter);
    }

    /**
     * 星形矩阵图案。
     */
    public static DanmakuPattern starMatrix() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.matrix(Pattern.BuiltIn.STAR.getPatternString()), DanmakuBulletSpec.of(ReverieDreams.id("star"), 0.8f))
                .withTiming(0, 40, 20);
        return DanmakuPattern.of(emitter);
    }

    /**
     * 螺旋弹幕：发射口绕锚点匀速旋转并持续开火（阿基米德螺旋）。
     */
    public static DanmakuPattern spiral() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.aim(), DanmakuBulletSpec.of(ReverieDreams.id("star"), 1.0f).withRandomColor())
                .withTiming(0, 140, 3)
                .withAim(EmitAim.OUTWARD, 0f, 0f)
                .withOffset(new Vec3(0.9D, 0.0D, 0.0D))
                .withRotation(linear(0f, 1080f));
        return DanmakuPattern.of(emitter);
    }

    /**
     * 匀加速旋转弹幕：四个发射口绕轴心旋转（角速度随时间线性增长，角度二次曲线）。
     * 对应文档“可以以相同方法做四个发射口”。
     */
    public static DanmakuPattern acceleratingRotation() {
        List<DanmakuEmitter> emitters = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Vec3 offset = DanmakuMath.rotateY(new Vec3(1.0D, 0.0D, 0.0D), i * 90f);
            emitters.add(
                    DanmakuEmitter
                            .of(EmitShape.aim(), DanmakuBulletSpec.of(ReverieDreams.id("rice"), 1.1f))
                            .withTiming(0, 160, 4)
                            .withAim(EmitAim.OUTWARD, 0f, 0f)
                            .withOffset(offset)
                            .withRotation(quadratic(0f, 1440f))
            );
        }
        return new DanmakuPattern(emitters);
    }

    /**
     * 波与粒子的境界：发射口转速不断加快又减慢并反向（正弦角速度曲线）。
     */
    public static DanmakuPattern waveParticle() {
        DanmakuEmitter emitter = DanmakuEmitter
                .of(EmitShape.aim(), DanmakuBulletSpec.of(ReverieDreams.id("ball"), 1.0f).withRandomColor())
                .withTiming(0, 200, 2)
                .withAim(EmitAim.OUTWARD, 0f, 0f)
                .withOffset(new Vec3(0.8D, 0.0D, 0.0D))
                .withRotation(sine(0f, 1440f, 1.5f));
        return DanmakuPattern.of(emitter);
    }

    /**
     * 花型弹幕（还原文档技能三）：
     * 四条“辐条”直线飞出，2 秒后子弹开始自转（45°/s）并绕发射轴心公转（30°/s）；中心再叠加圆环。
     *
     * <p>对应文档代码：
     * {@code transform.Rotate(Vector3.back * Time.deltaTime * 45.0f);}
     * {@code transform.RotateAround(circle_O.position, Vector3.back, 30 * Time.deltaTime);}
     */
    public static DanmakuPattern flower() {
        DanmakuBulletSpec spokes = DanmakuBulletSpec.of(ReverieDreams.id("ball"), 0.8f)
                .withColor(0xFF6688)
                .withMotion(DanmakuMotion.of(Vec3.ZERO, 0f, 2.25f, List.of(
                        MotionSegment.orbitAroundOrigin(40, 1.5f, Vec3.ZERO)
                )));
        DanmakuEmitter spokesEmitter = DanmakuEmitter
                .of(EmitShape.ring(4, 1.0f, true), spokes)
                .withTiming(0, 160, 6)
                .withAim(EmitAim.FIXED, 0f, 0f);

        DanmakuEmitter ringEmitter = DanmakuEmitter
                .of(EmitShape.ring(16, 0.5f, true), DanmakuBulletSpec.of(ReverieDreams.id("star"), 0.6f).withRandomColor())
                .withTiming(0, 160, 20)
                .withAim(EmitAim.FIXED, 0f, 0f);

        return DanmakuPattern.of(spokesEmitter, ringEmitter);
    }

    // ------------------------------------------------------------------
    // 关键帧辅助
    // ------------------------------------------------------------------

    public static KeyframeRange linear(float start, float end) {
        return KeyframeRange.empty(start, end);
    }

    public static KeyframeRange quadratic(float start, float end) {
        return function(KeyframeFunctions.QUADRATIC, Map.of("start", start, "end", end));
    }

    public static KeyframeRange sine(float start, float end, float frequency) {
        return function(KeyframeFunctions.SINE, Map.of("start", start, "end", end, "frequency", frequency));
    }

    public static KeyframeRange function(ResourceKey<KeyframeFunction> key, Map<String, Float> params) {
        return new KeyframeRange(key, params);
    }
}
