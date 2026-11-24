package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        TouhouSpells.register((id, pattern) -> BuiltInRegistryProviders.registerForBuiltin(registry, id, pattern));
        SokuMoves.register((id, pattern) -> BuiltInRegistryProviders.registerForBuiltin(registry, id, pattern));
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

}
