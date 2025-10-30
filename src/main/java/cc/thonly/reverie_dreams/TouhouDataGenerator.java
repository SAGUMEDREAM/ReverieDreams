package cc.thonly.reverie_dreams;

import cc.thonly.mystias_izakaya.datagen.MICraftingConflictProvider;
import cc.thonly.mystias_izakaya.datagen.MIDrinkProvider;
import cc.thonly.mystias_izakaya.datagen.MIIngredientProvider;
import cc.thonly.reverie_dreams.datagen.*;
import cc.thonly.reverie_dreams.world.dimension.DimensionInit;
import cc.thonly.reverie_dreams.world.dimension.DimensionTypeInit;
import cc.thonly.reverie_dreams.world.gen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;

public class TouhouDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        DynamicRegistries.register(Registries.LEVEL_STEM, LevelStem.CODEC);

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
        pack.addProvider(ModDynamicRegistryProvider::new);
        pack.addProvider(ModSkinConfigProvider::new);
        pack.addProvider(ModJsonElementWriterProvider::new);

        pack.addProvider(MIIngredientProvider::new);
        pack.addProvider(MIDrinkProvider::new);
        pack.addProvider(MICraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.add(Registries.CONFIGURED_FEATURE, ConfigurationFeatureInit::bootstrap);
        registryBuilder.add(Registries.CONFIGURED_CARVER, ConfigurationCarverInit::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, PlacedFeaturesInit::bootstrap);
        registryBuilder.add(Registries.NOISE_SETTINGS, ChunkGeneratorSettingsInit::bootstrap);
        registryBuilder.add(Registries.BIOME, BiomeInit::bootstrap);
        registryBuilder.add(Registries.STRUCTURE, ModStructures::bootstrap);
        registryBuilder.add(Registries.STRUCTURE_SET, ModStructureSets::bootstrap);
        registryBuilder.add(Registries.TEMPLATE_POOL, ModTemplatePools::bootstrap);
        registryBuilder.add(Registries.DIMENSION_TYPE, DimensionTypeInit::bootstrap);
        registryBuilder.add(Registries.LEVEL_STEM, DimensionInit::bootstrap);
    }

}
