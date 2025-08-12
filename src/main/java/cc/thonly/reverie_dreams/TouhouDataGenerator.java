package cc.thonly.reverie_dreams;

import cc.thonly.mystias_izakaya.datagen.MICraftingConflictProvider;
import cc.thonly.mystias_izakaya.datagen.MIDrinkProvider;
import cc.thonly.mystias_izakaya.datagen.MIIngredientProvider;
import cc.thonly.reverie_dreams.datagen.*;
import cc.thonly.reverie_dreams.world.PlacedFeaturesInit;
import cc.thonly.reverie_dreams.world.gen.ConfigurationFeatureInit;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class TouhouDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModSimpChineseLangProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider(ModEntityTagProvider::new);
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModBlockLootTableProvider::new);
        pack.addProvider(ModEntityLootTableProvider::new);
        pack.addProvider(ModEquipmentAssetProvider::new);
        pack.addProvider(ModJukeboxProvider::new);
        pack.addProvider(ModSoundProvider::new);
        pack.addProvider(ModRecipeTypeProvider::new);
        pack.addProvider(ModRegistryDataGenerator::new);

        pack.addProvider(MIIngredientProvider::new);
        pack.addProvider(MIDrinkProvider::new);
        pack.addProvider(MICraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ConfigurationFeatureInit::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, PlacedFeaturesInit::bootstrap);
    }

}
