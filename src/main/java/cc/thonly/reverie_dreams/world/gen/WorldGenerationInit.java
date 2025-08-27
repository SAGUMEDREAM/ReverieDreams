package cc.thonly.reverie_dreams.world.gen;

public class WorldGenerationInit {
    public static void registerWorldGeneration() {
        ConfigurationFeatureInit.init();
        PlacedFeaturesInit.init();
        ChunkGenerationInit.init();
        ChunkGeneratorSettingsInit.init();
        WorldInit.init();
        BiomeInit.init();
        DimensionTypeInit.init();
        DimensionInit.init();
    }
}
