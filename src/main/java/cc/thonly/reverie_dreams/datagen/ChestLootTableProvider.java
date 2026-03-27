package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ChestLootTableProvider extends SimpleFabricLootTableProvider {
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public ChestLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup, LootContextParamSets.CHEST);
        this.registryLookup = registryLookup;
    }

    public static final ResourceKey<LootTable> DREAM_CHEST = getKey("dream_chest");
    public static final ResourceKey<LootTable> OUTER_SHRINE_CHEST = getKey("outer_shrine_chest");
    public static final ResourceKey<LootTable> ABANDONED_ALTAR_CHEST = getKey("abandoned_altar_chest");
    public static final ResourceKey<LootTable> MINI_BAR_CHEST = getKey("mini_bar_chest");
    public static final ResourceKey<LootTable> BAMBOO_FOREST_BBQ_CHEST = getKey("bamboo_forest_bbq_chest");
    public static final ResourceKey<LootTable> SAKURAZUKA_CHEST = getKey("sakurazuka_chest");
    public static final ResourceKey<LootTable> MOON_BUILDING_CHEST_A = getKey("moon_building_chest_a");
    public static final ResourceKey<LootTable> MOON_BUILDING_CHEST_B = getKey("moon_building_chest_b");
    public static final ResourceKey<LootTable> MOON_BUILDING_CHEST_C = getKey("moon_building_chest_c");

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        this.registryLookup.thenAccept(registries -> this.generateLoot(output, registries));
    }

    private void generateLoot(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output, HolderLookup.Provider registries) {

        // =========================
        // 🌈 DREAM_CHEST（梦境核心）
        // =========================
        output.accept(DREAM_CHEST, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(3, 7))
                        .add(LootItem.lootTableItem(RDItems.DREAM_CRYSTAL_FRAGMENT).setWeight(25))
                        .add(LootItem.lootTableItem(RDItems.POWER).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                        .add(LootItem.lootTableItem(RDItems.POINT).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))

                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15))
                        .add(LootItem.lootTableItem(Items.COPPER_INGOT).setWeight(15))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(8))
                        .add(LootItem.lootTableItem(RDItems.DREAM_PILLOW).setWeight(8))
                        .add(LootItem.lootTableItem(RDItems.DREAM_SPEAR).setWeight(2))
                        .add(LootItem.lootTableItem(Items.GOLDEN_SWORD).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .add(
                                LootItem.lootTableItem(RDItems.DREAM_CHESTPLATE)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
                                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.DREAM_HELMET)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
                                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.DREAM_LEGGINGS)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
                                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.DREAM_BOOTS)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
                                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                        )

                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                                .setWeight(10))

                        .add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH).setWeight(5))
                        .add(LootItem.lootTableItem(RDItems.BOMB).setWeight(6))
                )
        );

        // =========================
        // 🌱 OUTER_SHRINE（神社外围）
        // =========================
        output.accept(OUTER_SHRINE_CHEST, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(6, 10))
                        .add(LootItem.lootTableItem(Items.APPLE).setWeight(15))
                        .add(LootItem.lootTableItem(Items.BREAD).setWeight(15))
                        .add(LootItem.lootTableItem(Items.GLOW_BERRIES).setWeight(15)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 6))))

                        .add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH_FRAGMENT).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.EXORCISM_PAPER).setWeight(10))
                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))
                        .add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))
                        .add(LootItem.lootTableItem(Items.GOLDEN_SWORD).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 6.0F))))
                        .add(LootItem.lootTableItem(Items.LEATHER).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0F, 17.0F))))

                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.POINT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                )
        );

        // =========================
        // ☠️ ABANDONED_ALTAR（废弃祭坛）
        // =========================
        output.accept(ABANDONED_ALTAR_CHEST, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(2, 4))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(20))
                        .add(LootItem.lootTableItem(Items.BONE).setWeight(20))
                        .add(LootItem.lootTableItem(Items.LEATHER).setWeight(14))
                        .add(LootItem.lootTableItem(Items.STRING).setWeight(15))

                        .add(LootItem.lootTableItem(RDItems.EXORCISM_PAPER).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.BOMB).setWeight(10))

                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))))
                )
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                        .add(LootItem.lootTableItem(RDItems.COPPER_COIN).setWeight(20))
                        .add(LootItem.lootTableItem(RDItems.SILVER_COIN).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.GOLD_COIN).setWeight(5))
                        .add(LootItem.lootTableItem(RDItems.POINT).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 3))
                        .add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH).setWeight(2))
                        .add(LootItem.lootTableItem(RDItems.BOMB).setWeight(3))
                        .add(LootItem.lootTableItem(Items.STONE_SWORD).setWeight(3))
                        .add(LootItem.lootTableItem(Items.STONE_SWORD).setWeight(3))
                        .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                                .setWeight(2))
                        .add(LootItem.lootTableItem(RDItems.BOMB_FRAGMENT).setWeight(15))
                )
        );

        // =========================
        // 🍹 MINI_BAR
        // =========================
        var miniBarPool = LootPool.lootPool().setRolls(UniformGenerator.between(4, 6))
                .add(LootItem.lootTableItem(RDItems.POINT).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                .add(LootItem.lootTableItem(Items.EMERALD).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))));
        for (Item drinkItem : RDDrinkItems.DRINK_ITEMS) {
            miniBarPool.add(LootItem.lootTableItem(drinkItem).setWeight(10));
        }
        output.accept(MINI_BAR_CHEST, LootTable.lootTable().withPool(miniBarPool));

        // =========================
        // 🍖 BAMBOO BBQ
        // =========================
        var foodPool = LootPool.lootPool().setRolls(UniformGenerator.between(2, 4));
        for (Item i : RDFoodItems.FOOD_ITEMS) {
            foodPool.add(LootItem.lootTableItem(i).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        }

        var ingredientPool = LootPool.lootPool().setRolls(UniformGenerator.between(1, 3));
        for (Item i : RDIngredientItems.INGREDIENTS) {
            ingredientPool.add(LootItem.lootTableItem(i).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        }

        output.accept(BAMBOO_FOREST_BBQ_CHEST,
                LootTable.lootTable()
                        .withPool(foodPool)
                        .withPool(ingredientPool)
        );

        // =========================
        // 🌸 SAKURAZUKA（樱花区域）
        // =========================
        output.accept(SAKURAZUKA_CHEST, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(5, 8))
                        .add(LootItem.lootTableItem(Items.CHERRY_SAPLING).setWeight(10))
                        .add(LootItem.lootTableItem(Items.CHERRY_LEAVES).setWeight(10))
                        .add(LootItem.lootTableItem(Items.WOODEN_SWORD).setWeight(11).apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F))))
                        .add(LootItem.lootTableItem(Items.COPPER_SWORD).setWeight(8).apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F))))
                        .add(LootItem.lootTableItem(Items.PINK_PETALS).setWeight(10))
                        .add(LootItem.lootTableItem(Items.BAMBOO).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))

                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))

                        .add(LootItem.lootTableItem(RDItems.COPPER_COIN).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))
                        .add(LootItem.lootTableItem(RDItems.GOLD_COIN).setWeight(5))

                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3))
                )
        );

        // =========================
        // 🌕 MOON BUILDING A（资源）
        // =========================
        output.accept(MOON_BUILDING_CHEST_A, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(5, 8))
                        .add(LootItem.lootTableItem(Items.BREAD).setWeight(15))
                        .add(LootItem.lootTableItem(Items.PAPER).setWeight(10))

                        .add(LootItem.lootTableItem(RDItems.SILVER_INGOT).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.SILVER_COIN).setWeight(10))
                        .add(LootItem.lootTableItem(RDItems.COPPER_COIN).setWeight(10))

                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(8))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(5))
                        .add(
                                LootItem.lootTableItem(RDItems.SILVER_BOOTS)
                                        .setWeight(3)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F)))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.SILVER_CHESTPLATE)
                                        .setWeight(3)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F)))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.SILVER_LEGGINGS)
                                        .setWeight(3)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F)))
                        )
                        .add(
                                LootItem.lootTableItem(RDItems.SILVER_HELMET)
                                        .setWeight(3)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F)))
                        )
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))
                                .setWeight(10))
                )
        );

        // =========================
        // 🌕 MOON BUILDING B（遗物/能力）
        // =========================
        output.accept(MOON_BUILDING_CHEST_B, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(5, 8))
                        .add(LootItem.lootTableItem(Items.COMPASS).setWeight(10))
                        .add(LootItem.lootTableItem(Items.MAP).setWeight(10))

                        .add(LootItem.lootTableItem(RDItems.POINT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                        .add(LootItem.lootTableItem(RDItems.POWER).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))

                        .add(LootItem.lootTableItem(RDItems.SILVER_SPEAR).setWeight(6))

                        .add(LootItem.lootTableItem(RDItems.RED_ORB).setWeight(6))
                        .add(LootItem.lootTableItem(RDItems.BLUE_ORB).setWeight(6))
                        .add(LootItem.lootTableItem(RDItems.GREEN_ORB).setWeight(6))
                        .add(LootItem.lootTableItem(RDItems.YELLOW_ORB).setWeight(6))

                        .add(LootItem.lootTableItem(Items.COPPER_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                )
        );

        output.accept(MOON_BUILDING_CHEST_C, LootTable.lootTable());
    }

    public static ResourceKey<LootTable> getKey(String name) {
        return getKey(ReverieDreams.id("chest/" + name));
    }

    public static ResourceKey<LootTable> getKey(Identifier identifier) {
        return ResourceKey.create(Registries.LOOT_TABLE, identifier);
    }
}
