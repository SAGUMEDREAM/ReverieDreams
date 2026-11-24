package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoBiomeSource;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import cc.thonly.reverie_dreams.world.moon.MoonChunkGenerator;
import cc.thonly.reverie_dreams.world.moon.MoonBiomeSource;

public class RDBuiltinChunkGenerations {
    public static void init() {
        MCBuiltInRegistries.CHUNK_GENERATOR.register("moon", () -> MoonChunkGenerator.CODEC);
        MCBuiltInRegistries.BIOME_SOURCE.register("moon", () -> MoonBiomeSource.CODEC);
        // codec 必须保持注册：生产环境仍要能解析随包附带的 gensokyo 维度 JSON，
        // 否则整个动态注册表会因未知 generator 失败。真正禁用发生在
        // MinecraftServer.createLevels 和 GensokyoChunkGenerator.fillFromNoise。
        MCBuiltInRegistries.CHUNK_GENERATOR.register("gensokyo", () -> GensokyoChunkGenerator.CODEC);
        MCBuiltInRegistries.BIOME_SOURCE.register("gensokyo", () -> GensokyoBiomeSource.CODEC);
    }
}
