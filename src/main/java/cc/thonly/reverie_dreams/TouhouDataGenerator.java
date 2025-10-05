package cc.thonly.reverie_dreams;

import cc.thonly.mystias_izakaya.datagen.MICraftingConflictProvider;
import cc.thonly.mystias_izakaya.datagen.MIDrinkProvider;
import cc.thonly.mystias_izakaya.datagen.MIIngredientProvider;
import cc.thonly.reverie_dreams.datagen.*;
import cc.thonly.reverie_dreams.world.gen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionOptions;

public class TouhouDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        DynamicRegistries.register(RegistryKeys.DIMENSION, DimensionOptions.CODEC);

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModSimpChineseLangProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider(ModPointOfInterestTypeProvider::new);
        pack.addProvider(ModEntityTagProvider::new);
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModBlockLootTableProvider::new);
        pack.addProvider(ModChestLootTableProvider::new);
        pack.addProvider(ModEntityLootTableProvider::new);
        pack.addProvider(ModEquipmentAssetProvider::new);
        pack.addProvider(ModJukeboxProvider::new);
        pack.addProvider(ModSoundProvider::new);
        pack.addProvider(ModRecipeTypeProvider::new);
        pack.addProvider(ModRegistryDataGenerator::new);
        pack.addProvider(ModSkinConfigProvider::new);

        pack.addProvider(MIIngredientProvider::new);
        pack.addProvider(MIDrinkProvider::new);
        pack.addProvider(MICraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ConfigurationFeatureInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_CARVER, ConfigurationCarverInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, PlacedFeaturesInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettingsInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.BIOME, BiomeInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DIMENSION_TYPE, DimensionTypeInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DIMENSION, DimensionInit::bootstrap);
    }

}
