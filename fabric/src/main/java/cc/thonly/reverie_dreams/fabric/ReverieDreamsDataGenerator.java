package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.fabric.datagen.*;
import cc.thonly.reverie_dreams.fabric.datagen.tag.*;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.painting.RDPaintingVariants;
import cc.thonly.reverie_dreams.registry.content.villager.RDTradeSets;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.world.dimension.*;
import cc.thonly.reverie_dreams.world.gen.*;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
public class ReverieDreamsDataGenerator implements DataGeneratorEntrypoint {
    static boolean DISABLED = false;

    @SuppressWarnings({"DuplicatedCode", "PointlessBooleanExpression"})
    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator) {
        if (DISABLED) {
            log.info("Data-driven generation items have been disabled.");
            return;
        }
        if (!DynamicRegistriesImpl.DYNAMIC_REGISTRY_KEYS.contains(Registries.LEVEL_STEM)) {
            DynamicRegistries.register(Registries.LEVEL_STEM, LevelStem.CODEC);
        }

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SimpChineseLangProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(BlockTagProvider::new);
//        pack.addProvider(TestFoodPropertyTagProvider::new);
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
        pack.addProvider(FoodIngredientProvider::new);
        pack.addProvider(BeverageProvider::new);
        pack.addProvider(CraftingConflictProvider::new);
        pack.addProvider(CustomRecipeTypeProvider::new);
        pack.addProvider(DanmakuRecipeProvider::new);
        pack.addProvider(ShapeDrawRecipeProvider::new);
        pack.addProvider(GensokyoAltarRecipeProvider::new);
        pack.addProvider(BrewingBarrelRecipeTypeProvider::new);
        pack.addProvider(KitchenRecipeProvider::new);

        pack.addProvider(DynamicRegistryProvider::new);
        pack.addProvider(SkinConfigProvider::new);
        pack.addProvider(AdvancementProvider::new);
        pack.addProvider(JsonElementWriterProvider::new);
        pack.addProvider(CustomerProvider::new);
    }

    @Override
    public void buildRegistry(@NonNull RegistrySetBuilder builder) {
        DataGeneratorEntrypoint.super.buildRegistry(builder);
        builder.add(Registries.VILLAGER_TRADE, RDVillagerTrades::bootstrap);
        builder.add(Registries.TRADE_SET, RDTradeSets::bootstrap);
        builder.add(Registries.DAMAGE_TYPE, RDDamageTypes::bootstrap);
        builder.add(Registries.PAINTING_VARIANT, RDPaintingVariants::bootstrap);

        builder.add(Registries.ENCHANTMENT, RDEnchantments::bootstrap);
        builder.add(Registries.CONFIGURED_FEATURE, RDBuiltinConfigurationFeatures::bootstrap);
        builder.add(Registries.CONFIGURED_CARVER, RDBuiltinConfigurationCarvers::bootstrap);
        builder.add(Registries.PLACED_FEATURE, RDBuiltinPlacedFeatures::bootstrap);
        builder.add(Registries.NOISE_SETTINGS, RDBuiltinChunkGeneratorSettings::bootstrap);
        builder.add(Registries.BIOME, RDBuiltinBiomes::bootstrap);
        builder.add(Registries.STRUCTURE, RDBuiltinStructures::bootstrap);
        builder.add(Registries.STRUCTURE_SET, RDBuiltinStructureSets::bootstrap);
        builder.add(Registries.TEMPLATE_POOL, RDBuiltinTemplatePools::bootstrap);
        builder.add(Registries.DIMENSION_TYPE, RDBuiltInDimensionTypes::bootstrap);
        builder.add(Registries.LEVEL_STEM, RDBuiltInDimensions::bootstrap);
    }

    public static void disablePackOutput() {
        DISABLED = true;
    }

}
