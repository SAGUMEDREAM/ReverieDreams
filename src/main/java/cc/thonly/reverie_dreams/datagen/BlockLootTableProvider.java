package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.block.creator.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    private final Function<WoodCreator, Void> woodCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof DoorBlock) {
                LootTable.Builder builder = this.createDoorTable(block);
                this.add(block, builder);
                return;
            }
            if (block instanceof LeavesBlock) {
                LootTable.Builder builder = this.createLeavesDrops(block, creator.sapling(), 0.2f);
                this.add(block, builder);
                return;
            }
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };
    private final Function<DecorativeBlockCreator, Void> decorativeBlockCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };

    public BlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(RDBlocks.DANMAKU_CRAFTING_TABLE);
        dropSelf(RDBlocks.STRENGTH_TABLE);
        dropSelf(RDBlocks.GENSOKYO_ALTAR);
        dropSelf(RDBlocks.MUSIC_BLOCK);

        dropSelf(RDBlocks.MAGIC_ICE_BLOCK);
        dropSelf(RDBlocks.MARISA_HAT_BLOCK);
        dropSelf(RDBlocks.ANTI_COLLISION_BARREL);
        dropSelf(RDBlocks.CASH_BOX_BLOCK);
        dropSelf(RDBlocks.WHEEL_CHAIR);

        dropSelf(RDBlocks.POINT_BLOCK);
        dropSelf(RDBlocks.POWER_BLOCK);
        add(RDBlocks.SILVER_ORE, (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER));
        add(RDBlocks.DEEPSLATE_SILVER_ORE, (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER));
        Function<Block, LootTable.Builder> orbDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    RDItems.RED_ORB,
                    RDItems.BLUE_ORB,
                    RDItems.YELLOW_ORB,
                    RDItems.GREEN_ORB,
                    RDItems.PURPLE_ORB
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(RDItems.RED_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.BLUE_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.YELLOW_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.GREEN_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.PURPLE_ORB).setWeight(1));
            builder.withPool(fallbackPool);

            return builder;
        };
        add(RDBlocks.ORB_ORE, orbDropFunction);
        add(RDBlocks.DEEPSLATE_ORB_ORE, orbDropFunction);
        dropSelf(RDBlocks.SILVER_BLOCK);
        dropSelf(RDBlocks.SILVER_CHEST_BLOCK.chestBlock());
        Function<Block, LootTable.Builder> dreamCrystlDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    RDItems.DREAM_CRYSTAL_FRAGMENT,
                    RDItems.DREAM_CRYSTAL_FRAGMENT
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(RDItems.DREAM_CRYSTAL_FRAGMENT).setWeight(1))
                    ;
            builder.withPool(fallbackPool);

            return builder;
        };
        add(RDBlocks.DREAM_CRYSTAL_ORE, dreamCrystlDropFunction);

        this.decorativeBlockCreatorLootFunction.apply(RDBlocks.ICE_SCALES);
        this.decorativeBlockCreatorLootFunction.apply(RDBlocks.DREAM_STONE);
        this.decorativeBlockCreatorLootFunction.apply(RDBlocks.DREAM_STONE_BRICK);
        this.decorativeBlockCreatorLootFunction.apply(RDBlocks.MOON_STONE);
        this.decorativeBlockCreatorLootFunction.apply(RDBlocks.MOON_STONE_BRICK);

        for (FumoType fumo : FumoTypes.getView()) {
            dropSelf(fumo.block());
        }

        this.generateMI();
    }

    void generateCropLoot(CropBlockCreator.Instance instance) {
        if (instance.getCropBlock() != null && instance.getProduct() != null) {
            LootItemBlockStatePropertyCondition.Builder condition = LootItemBlockStatePropertyCondition
                    .hasBlockStateProperties(instance.getCropBlock())
                    .setProperties(
                            StatePropertiesPredicate.Builder
                                    .properties()
                                    .hasProperty(instance.getCropBlock().getAgeProperty(), instance.getCropBlock().getMaxAge())
                    );
//                LootTable.Builder lootTableBuilder = provider.cropDrops(this.cropBlock, this.product, this.seed, condition);
            LootTable.Builder lootTableBuilder = LootTable.lootTable();
            LootPoolSingletonContainer.Builder<?> productEntry = LootItem.lootTableItem(instance.getProduct())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 3.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> seedEntry = LootItem.lootTableItem(instance.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 2.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> baseSeedEntry = LootItem.lootTableItem(instance.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            ConstantValue.exactly(1)
                    ));
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .conditionally(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(baseSeedEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .conditionally(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(productEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .conditionally(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(seedEntry)
            );
            this.add(instance.getCropBlock(), lootTableBuilder);
        }
    }


    public void generateMI() {
        for (Block block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            dropSelf(block);
        }
        dropSelf(RDBlocks.ITEM_DISPLAY);

        for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            CropBlockCreator.Instance instance = view.getValue();
            generateCropLoot(instance);
        }

        this.woodCreatorLootFunction.apply(RDWoodBlocks.SPIRITUAL);
        dropSelf(RDWoodBlocks.BLESSED_SPIRITUAL_LOG);

        this.woodCreatorLootFunction.apply(RDWoodBlocks.LEMON);
        dropOther(RDWoodBlocks.LEMON_FRUIT_LEAVES, RDWoodBlocks.LEMON.sapling());

        this.woodCreatorLootFunction.apply(RDWoodBlocks.GINKGO);
        dropOther(RDWoodBlocks.GINKGO_FRUIT_LEAVES, RDWoodBlocks.GINKGO.sapling());

        this.woodCreatorLootFunction.apply(RDWoodBlocks.PEACH);
        dropOther(RDWoodBlocks.PEACH_FRUIT_LEAVES, RDWoodBlocks.PEACH.sapling());

//        addDrop(MIBlocks.COOKTOP);
        dropSelf(RDBlocks.BLACK_SALT_BLOCK);
        dropSelf(RDWoodBlocks.UDUMBARA_FLOWER);
        dropSelf(RDWoodBlocks.TREMELLA);
    }
}
