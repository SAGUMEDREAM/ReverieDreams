package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class SpellCardFrameConfigs {
    public static final Map<Integer, List<List<SpellCardFrameConfig>>> MAP = new Object2ObjectLinkedOpenHashMap<>();

    public static void reload(ResourceManager manager) {
        Map<ResourceLocation, Resource> resources = manager.listResources("danmaku_config", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resId = entry.getKey();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    resId.getNamespace(),
                    resId.getPath().replace("danmaku_config/", "")
                            .replace(".json", "")
            );
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                DataResult<SpellCardFrameConfig> result = SpellCardFrameConfig.CODEC.parse(JsonOps.INSTANCE, json);
                Optional<SpellCardFrameConfig> optional = result.result();
                if (optional.isPresent()) {
                    SpellCardFrameConfig danmakuConfig = optional.get();
                    RegistryHandlers.register(RegistryHandlers.DANMAKU_CONFIG, id, danmakuConfig);
                } else {
                    log.error("Can't parse danmaku config {}", id);
                }
            } catch (Exception err) {
                log.error("Can't load danmaku config {}", id, err);
            }
        }
    }

    public static void bootstrap(RegistryHandler<SpellCardFrameConfig> configs) {
        MAP.put(0, createFancySpellcardTest());
    }

    public static List<List<SpellCardFrameConfig>> createFancySpellcardTest() {
        List<List<SpellCardFrameConfig>> frames = new ArrayList<>();

        // ========== 第一波：初始环形星弹 ==========
        List<SpellCardFrameConfig> wave1 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.STAR)
                        .withDensity(48)
                        .withTickInterval(5)
                        .withTickDuration(60)
                        .withPitchStartAt(-10, 10)    // 几乎水平
                        .withYawStartAt(-180, 180)    // 全向环形
                        .withSpeed(0.6f)
                        .setRandomColor()
        );
        frames.add(wave1);

        // ========== 第二波：交错火球层 ==========
        List<SpellCardFrameConfig> wave2 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.FIREBALL)
                        .withDensity(32)
                        .withTickDelay(80)             // 延迟出现
                        .withTickInterval(4)
                        .withTickDuration(60)
                        .withPitchStartAt(-5, 5)       // 水平带轻微扰动
                        .withYawStartAt(-180, 180)
                        .withSpeed(0.8f)
                        .setRandomColor(),

                new SpellCardFrameConfig(DanmakuTypes.FIREBALL_GLOWY)
                        .withDensity(32)
                        .withTickDelay(80)
                        .withTickInterval(4)
                        .withTickDuration(60)
                        .withPitchStartAt(10, 30)      // 稍微往上
                        .withYawStartAt(-180, 180)
                        .withSpeed(0.9f)
                        .setRandomColor()
        );
        frames.add(wave2);

        // ========== 第三波：爆发式多层散射 ==========
        List<SpellCardFrameConfig> wave3 = List.of(
                new SpellCardFrameConfig(DanmakuTypes.BALL)
                        .withDensity(40)
                        .withTickDelay(160)
                        .withTickInterval(3)
                        .withTickDuration(50)
                        .withPitchStartAt(-15, 15)
                        .withYawStartAt(-180, 180)
                        .withSpeed(1.0f)
                        .setRandomColor(),

                new SpellCardFrameConfig(DanmakuTypes.RICE)
                        .withDensity(40)
                        .withTickDelay(160)
                        .withTickInterval(3)
                        .withTickDuration(50)
                        .withPitchStartAt(-30, 30)
                        .withYawStartAt(-180, 180)
                        .withSpeed(1.1f)
                        .setRandomColor(),

                new SpellCardFrameConfig(DanmakuTypes.STAR)
                        .withDensity(36)
                        .withTickDelay(160)
                        .withTickInterval(2)
                        .withTickDuration(50)
                        .withPitchStartAt(20, 40)
                        .withYawStartAt(-180, 180)
                        .withSpeed(1.2f)
                        .setRandomColor()
        );
        frames.add(wave3);

        return frames;
    }

}
