package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
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
import java.util.stream.Stream;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private final Function<WoodBundle, Void> woodBundleLootFunction = (creator) -> {
        creator.stream().forEach((deferredBlock -> {
            Block block = deferredBlock.asBlock();
            if (block instanceof DoorBlock) {
                LootTable.Builder builder = this.createDoorTable(block);
                this.add(block, builder);
                return;
            } else if (block instanceof LeavesBlock) {
                LootTable.Builder builder = this.createLeavesDrops(block, creator.sapling().asBlock(), NORMAL_LEAVES_SAPLING_CHANCES);
                this.add(block, builder);
                return;
            } else if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };
    private final Function<DecorativeBlockBundle, Void> decorativeBlockBundleLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block.asBlock() instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block.asBlock());
                this.add(block.asBlock(), builder);
                return;
            }
            this.dropSelf(block.asBlock());
        }));
        return null;
    };

    public BlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock());
        dropSelf(RDBlocks.STRENGTH_TABLE.asBlock());
        dropSelf(RDBlocks.GENSOKYO_ALTAR.asBlock());
        dropSelf(RDBlocks.MUSIC_BLOCK.asBlock());

        dropSelf(RDBlocks.MAGIC_ICE_BLOCK.asBlock());
        dropSelf(RDBlocks.MARISA_HAT_BLOCK.asBlock());
        dropSelf(RDBlocks.ANTI_COLLISION_BARREL.asBlock());
        dropSelf(RDBlocks.CASH_BOX_BLOCK.asBlock());
        dropSelf(RDBlocks.WHEEL_CHAIR.asBlock());

        dropSelf(RDBlocks.POINT_BLOCK.asBlock());
        dropSelf(RDBlocks.POWER_BLOCK.asBlock());
        add(RDBlocks.SILVER_ORE.asBlock(), (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER.asItem()));
        add(RDBlocks.MOON_IRON_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.RAW_IRON.asItem()));
        add(RDBlocks.MOON_GOLD_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.RAW_GOLD.asItem()));
        add(RDBlocks.MOON_DIAMOND_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.DIAMOND.asItem()));
        add(RDBlocks.MOON_QUARTZ_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.QUARTZ.asItem()));
        add(RDBlocks.DEEPSLATE_SILVER_ORE.asBlock(), (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER.asItem()));
        Function<Block, LootTable.Builder> orbDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = Stream.of(
                    RDItems.RED_ORB,
                    RDItems.BLUE_ORB,
                    RDItems.YELLOW_ORB,
                    RDItems.GREEN_ORB,
                    RDItems.PURPLE_ORB
            ).map(ItemLike::asItem).toList();

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
        add(RDBlocks.ORB_ORE.asBlock(), orbDropFunction);
        add(RDBlocks.DEEPSLATE_ORB_ORE.asBlock(), orbDropFunction);
        dropSelf(RDBlocks.SILVER_BLOCK.asBlock());
        dropSelf(RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock());
        Function<Block, LootTable.Builder> dreamCrystlDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = Stream.of(
                    RDItems.DREAM_CRYSTAL_FRAGMENT,
                    RDItems.DREAM_CRYSTAL_FRAGMENT
            ).map(ItemLike::asItem).toList();

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(RDItems.DREAM_CRYSTAL_FRAGMENT).setWeight(1));
            builder.withPool(fallbackPool);

            return builder;
        };
        add(RDBlocks.DREAM_CRYSTAL_ORE.asBlock(), dreamCrystlDropFunction);

        this.decorativeBlockBundleLootFunction.apply(RDBlocks.ICE_SCALES);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.DREAM_STONE);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.DREAM_STONE_BRICK);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.MOON_STONE);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.MOON_STONE_BRICK);

        for (FumoType fumo : FumoTypes.getView()) {
            dropSelf(fumo.block());
        }

        for (Block block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            dropSelf(block);
        }
        dropSelf(RDBlocks.PLATE.asBlock());
        dropSelf(RDBlocks.CHAIR.asBlock());
        dropSelf(RDBlocks.TABLE.asBlock());
        dropSelf(RDBlocks.BREWING_BARREL.asBlock());
        dropSelf(RDBlocks.ICE_MAKING_MACHINE.asBlock());

        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            generateCropLoot(entry);
        }

        this.woodBundleLootFunction.apply(RDWoodBlocks.SPIRITUAL_BUNDLE);
        dropSelf(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock());

        this.woodBundleLootFunction.apply(RDWoodBlocks.LEMON_BUNDLE);
        dropOther(RDWoodBlocks.LEMON_FRUIT_LEAVES.asBlock(), RDWoodBlocks.LEMON_BUNDLE.sapling());

        this.woodBundleLootFunction.apply(RDWoodBlocks.GINKGO_BUNDLE);
        dropOther(RDWoodBlocks.GINKGO_FRUIT_LEAVES.asBlock(), RDWoodBlocks.GINKGO_BUNDLE.sapling());

        this.woodBundleLootFunction.apply(RDWoodBlocks.PEACH_BUNDLE);
        dropOther(RDWoodBlocks.PEACH_FRUIT_LEAVES.asBlock(), RDWoodBlocks.PEACH_BUNDLE.sapling());

        dropSelf(RDBlocks.BLACK_SALT_BLOCK.asBlock());
        dropSelf(RDWoodBlocks.UDUMBARA_FLOWER.asBlock());
        dropSelf(RDWoodBlocks.TREMELLA.asBlock());

        dropSelf(RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock());
        dropSelf(RDBlocks.SIGNAL_RAIL_BLOCK.asBlock());
        dropSelf(RDBlocks.SIGNAL_DELAYER_BLOCK.asBlock());
        dropSelf(RDBlocks.REMOTE_CLIENT.asBlock());
        dropSelf(RDBlocks.REMOTE_SERVER.asBlock());
        dropSelf(RDBlocks.SPEAKER.asBlock());
    }

    void generateCropLoot(CropBlockBundle.Entry entry) {
        if (entry.getCropBlock() != null && entry.getProduct() != null) {
            LootItemBlockStatePropertyCondition.Builder condition = LootItemBlockStatePropertyCondition
                    .hasBlockStateProperties(entry.getCropBlock().asBlock())
                    .setProperties(
                            StatePropertiesPredicate.Builder
                                    .properties()
                                    .hasProperty(((AbstractCropBlock) entry.getCropBlock().asBlock()).getAgeProperty(), ((AbstractCropBlock) entry.getCropBlock().asBlock()).getMaxAge())
                    );
//                LootTable.Builder lootTableBuilder = provider.cropDrops(this.cropBlock, this.product, this.seed, condition);
            LootTable.Builder lootTableBuilder = LootTable.lootTable();
            LootPoolSingletonContainer.Builder<?> productEntry = LootItem.lootTableItem(entry.getProduct())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 3.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> seedEntry = LootItem.lootTableItem(entry.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 2.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> baseSeedEntry = LootItem.lootTableItem(entry.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            ConstantValue.exactly(1)
                    ));
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition)
                            .setRolls(ConstantValue.exactly(1))
                            .add(baseSeedEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition)
                            .setRolls(ConstantValue.exactly(1))
                            .add(productEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition)
                            .setRolls(ConstantValue.exactly(1))
                            .add(seedEntry)
            );
            this.add(entry.getCropBlock().asBlock(), lootTableBuilder);
        }
    }

}
