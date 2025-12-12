package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.datagen.*;
import cc.thonly.reverie_dreams.datagen.tag.*;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.advancements.RDAdvancements;
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
        pack.addProvider(EnchantmentTagProvider::new);
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
        pack.addProvider(AdvancementProvider::new);
        pack.addProvider(JsonElementWriterProvider::new);

        pack.addProvider(IngredientProvider::new);
        pack.addProvider(DrinkProvider::new);
        pack.addProvider(CraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        DataGeneratorEntrypoint.super.buildRegistry(builder);
        builder.add(Registries.DAMAGE_TYPE, RDDamageTypes::bootstrap);
        builder.add(Registries.ENCHANTMENT, RDEnchantments::bootstrap);
        builder.add(Registries.CONFIGURED_FEATURE, ConfigurationFeatureInit::bootstrap);
        builder.add(Registries.CONFIGURED_CARVER, ConfigurationCarverInit::bootstrap);
        builder.add(Registries.PLACED_FEATURE, PlacedFeaturesInit::bootstrap);
        builder.add(Registries.NOISE_SETTINGS, ChunkGeneratorSettingsInit::bootstrap);
        builder.add(Registries.BIOME, BiomeInit::bootstrap);
        builder.add(Registries.STRUCTURE, ModStructures::bootstrap);
        builder.add(Registries.STRUCTURE_SET, ModStructureSets::bootstrap);
        builder.add(Registries.TEMPLATE_POOL, ModTemplatePools::bootstrap);
        builder.add(Registries.DIMENSION_TYPE, DimensionTypeInit::bootstrap);
        builder.add(Registries.LEVEL_STEM, DimensionInit::bootstrap);
    }

}
