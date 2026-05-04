package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SpellCardFrameConfigs {
    public static final Map<String, List<List<SpellCardFrameConfig>>> BUILTIN_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

    public static void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.listResources("spellcard", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resId = entry.getKey();
            Identifier id = Identifier.fromNamespaceAndPath(
                    resId.getNamespace(),
                    resId.getPath().replace("spellcard/", "")
                            .replace(".json", "")
            );
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<SpellCardFrameConfig> result = SpellCardFrameConfig.COMPONENT_CODEC.parse(JsonOps.INSTANCE, json);
                Optional<SpellCardFrameConfig> optional = result.result();
                if (optional.isPresent()) {
                    SpellCardFrameConfig danmakuConfig = optional.get();
                    RegistryImpls.register(RegistryImpls.DANMAKU_CONFIG, id, danmakuConfig);
                } else {
                    log.error("Can't parse danmaku config {}", id);
                }
            } catch (Exception err) {
                log.error("Can't load danmaku config {}", id, err);
            }
        }
    }

    public static void bootstrap(RegistryImpl<SpellCardFrameConfig> configs) {
        BUILTIN_ITEMS.put("Test", createTestSpellcardElegant());
        BUILTIN_ITEMS.put("Test2", createTestSpellcardElegant2());
    }

    @SuppressWarnings("deprecation")
    public static List<List<SpellCardFrameConfig>> createTestSpellcardElegant() {
        List<List<SpellCardFrameConfig>> frames = new ArrayList<>();

        // ========== 第一波：低速螺旋米弹 ==========
        // 环形缓慢旋转，给玩家时间反应
        List<SpellCardFrameConfig> wave1 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.RICE)
                        .withDensity(32)
                        .withTickInterval(6)
                        .withTickDuration(80)
                        .withPitchStartAt(-8, 8)
                        .withYawStartAt(-180, 180)
                        .withSpeed(0.8f)
                        .setRandomColor()
        );
        frames.add(wave1);

        // ========== 第二波：花瓣式泡泡包围 ==========
        // 中速，向上展开，形成“花”的感觉
        List<SpellCardFrameConfig> wave2 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.BUBBLE)
                        .withDensity(36)
                        .withTickDelay(90)
                        .withTickInterval(5)
                        .withTickDuration(70)
                        .withPitchStartAt(15, 35)
                        .withYawStartAt(-180, 180)
                        .withSpeed(0.6f)
                        .setRandomColor()
                        .async(),

                new SpellCardFrameConfig(DanmakuTypes.BALL)
                        .withDensity(24)
                        .withTickDelay(90)
                        .withTickInterval(6)
                        .withTickDuration(70)
                        .withPitchStartAt(-25, -5)
                        .withYawStartAt(-180, 180)
                        .withSpeed(0.55f)
                        .setRandomColor()
                        .async()
        );
        frames.add(wave2);

        // ========== 第三波：定轴压迫激光 ==========
        // 激光不追踪，制造安全区压力
        List<SpellCardFrameConfig> wave3 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.LASER)
                        .withDensity(6)
                        .withTickDelay(170)
                        .withTickInterval(20)
                        .withTickDuration(60)
                        .withPitchStartAt(-10, 10)
                        .withYawStartAt(-90, 90)
                        .withSpeed(0.5f)   // 激光定轴
                        .sync(),

                new SpellCardFrameConfig(DanmakuTypes.BIG_LASER)
                        .withDensity(2)
                        .withTickDelay(190)
                        .withTickInterval(30)
                        .withTickDuration(50)
                        .withPitchStartAt(0, 0)
                        .withYawStartAt(-45, 45)
                        .withSpeed(0.5f)
                        .sync()
        );
        frames.add(wave3);

        // ========== 第四波：高速星雨收尾 ==========
        // 高速下落，视觉冲击强
        List<SpellCardFrameConfig> wave4 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.STAR)
                        .withDensity(72)
                        .withTickDelay(260)
                        .withTickInterval(2)
                        .withTickDuration(60)
                        .withPitchStartAt(45, 75)   // 从上方落下
                        .withYawStartAt(-180, 180)
                        .withSpeed(1.25f)
                        .setRandomColor(),

                new SpellCardFrameConfig(DanmakuTypes.NOTE)
                        .withDensity(24)
                        .withTickDelay(260)
                        .withTickInterval(4)
                        .withTickDuration(60)
                        .withPitchStartAt(30, 60)
                        .withYawStartAt(-180, 180)
                        .withSpeed(1.0f)
                        .setRandomColor()
        );
        frames.add(wave4);

        return frames;
    }

    @SuppressWarnings("deprecation")
    public static List<List<SpellCardFrameConfig>> createTestSpellcardElegant2() {
        List<List<SpellCardFrameConfig>> frames = new ArrayList<>();

        // ========== 第一波：顺时针方向旋转 ==========
        List<SpellCardFrameConfig> wave1 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.STAR)
                        .withDensity(48)
                        .withTickInterval(5)
                        .withTickDuration(80)
                        .withSpeed(0.5f)
                        .withPitchStartAt(-10, 10)       // 水平发射
                        .withYawStartAt(-180, 180)      // 水平方向
                        .withPitchFunction(KeyframeRange.empty(-30, 30))  // 正弦控制的起始
                        .withYawFunction(KeyframeRange.empty(0, 360))   // 顺时针旋转
                        .setRandomColor()
        );
        frames.add(wave1);

        // ========== 第二波：逆时针方向旋转 ==========
        List<SpellCardFrameConfig> wave2 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.STAR)
                        .withDensity(48)
                        .withTickDelay(90)              // 延迟出现
                        .withTickInterval(5)
                        .withTickDuration(80)
                        .withSpeed(0.5f)
                        .withPitchStartAt(-10, 10)      // 水平发射
                        .withYawStartAt(-180, 180)     // 水平方向
                        .withPitchFunction(KeyframeRange.empty(-30, 30))  // 正弦控制的起始
                        .withYawFunction(KeyframeRange.empty(180, 540))  // 逆时针旋转
                        .setRandomColor()
        );
        frames.add(wave2);

        // ========== 第三波：上下弹幕，垂直发射 ==========
        List<SpellCardFrameConfig> wave3 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.BUBBLE)
                        .withDensity(30)
                        .withTickDelay(180)             // 延迟出现
                        .withTickInterval(5)
                        .withTickDuration(80)
                        .withSpeed(0.5f)
                        .withPitchStartAt(0, 40)       // 从上方向下
                        .withYawStartAt(-180, 180)    // 水平方向
                        .withPitchFunction(KeyframeRange.empty(40, 80)) // 控制上下移动
                        .setRandomColor()
        );
        frames.add(wave3);
        return frames;
    }

}
