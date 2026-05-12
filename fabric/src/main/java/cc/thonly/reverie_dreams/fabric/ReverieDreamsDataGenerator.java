package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.fabric.datagen.*;
import cc.thonly.reverie_dreams.fabric.datagen.tag.*;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.villager.RDTradeSets;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.world.dimension.*;
import cc.thonly.reverie_dreams.world.gen.*;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;
import org.jspecify.annotations.NonNull;

@Slf4j
public class ReverieDreamsDataGenerator implements DataGeneratorEntrypoint {
    static boolean DISABLED = false;

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator) {
        if (DISABLED) {
            log.info("Data-driven generation items have been disabled.");
            return;
        }
        DynamicRegistries.register(Registries.LEVEL_STEM, LevelStem.CODEC);

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SimpChineseLangProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(VillagerTradeTagProvider::new);
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
        pack.addProvider(CustomRecipeTypeProvider::new);
        pack.addProvider(DanmakuRecipeProvider::new);
        pack.addProvider(ShapeDrawRecipeProvider::new);
        pack.addProvider(GensokyoAltarRecipeProvider::new);
        pack.addProvider(KitchenRecipeProvider::new);
        pack.addProvider(DynamicRegistryProvider::new);
        pack.addProvider(SkinConfigProvider::new);
        pack.addProvider(AdvancementProvider::new);
        pack.addProvider(JsonElementWriterProvider::new);

        pack.addProvider(FoodIngredientProvider::new);
        pack.addProvider(DrinkProvider::new);
        pack.addProvider(CraftingConflictProvider::new);
    }

    @Override
    public void buildRegistry(@NonNull RegistrySetBuilder builder) {
        DataGeneratorEntrypoint.super.buildRegistry(builder);
        builder.add(Registries.VILLAGER_TRADE, RDVillagerTrades::bootstrap);
        builder.add(Registries.TRADE_SET, RDTradeSets::bootstrap);
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

    public static void disablePack() {
        DISABLED = true;
    }

}
