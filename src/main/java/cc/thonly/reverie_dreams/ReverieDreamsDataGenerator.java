package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.datagen.CraftingConflictProvider;
import cc.thonly.reverie_dreams.datagen.DrinkProvider;
import cc.thonly.reverie_dreams.datagen.IngredientProvider;
import cc.thonly.reverie_dreams.datagen.*;
import cc.thonly.reverie_dreams.datagen.tag.BlockTagProvider;
import cc.thonly.reverie_dreams.datagen.tag.DamageTypeTagProvider;
import cc.thonly.reverie_dreams.datagen.tag.EntityTagProvider;
import cc.thonly.reverie_dreams.datagen.tag.ItemTagProvider;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.world.dimension.DimensionInit;
import cc.thonly.reverie_dreams.world.dimension.DimensionTypeInit;
import cc.thonly.reverie_dreams.world.gen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;

public class ReverieDreamsDataGenerator implements DataGeneratorEntrypoint {

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        DynamicRegistries.register(Registries.LEVEL_STEM, LevelStem.CODEC);

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SimpChineseLangProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(PointOfInterestTypeProvider::new);
        pack.addProvider(EntityTagProvider::new);
        pack.addProvider(DamageTypeTagProvider::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(RecipeProvider::new);
        pack.addProvider(BlockLootTableProvider::new);
        pack.addProvider(ChestLootTableProvider::new);
        pack.addProvider(EntityLootTableProvider::new);
        pack.addProvider(EquipmentAssetProvider::new);
        pack.addProvider(JukeboxProvider::new);
        pack.addProvider(SoundProvider::new);
        pack.addProvider(RecipeTypeProvider::new);
        pack.addProvider(DynamicRegistryProvider::new);
        pack.addProvider(SkinConfigProvider::new);
        pack.addProvider(JsonElementWriterProvider::new);

        pack.addProvider(IngredientProvider::new);
        pack.addProvider(DrinkProvider::new);
        pack.addProvider(CraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.add(Registries.DAMAGE_TYPE, RDDamageTypes::bootstrap);
        registryBuilder.add(Registries.ENCHANTMENT, RDEnchantments::bootstrap);
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
