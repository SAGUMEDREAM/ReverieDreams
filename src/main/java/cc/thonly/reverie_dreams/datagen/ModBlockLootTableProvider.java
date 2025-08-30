package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.kitchenware.AbstractKitchenwareBlock;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.block.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    private final Function<WoodCreator, Void> woodCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof DoorBlock) {
                LootTable.Builder builder = this.doorDrops(block);
                this.addDrop(block, builder);
                return;
            }
            if (block instanceof LeavesBlock) {
                this.leavesDrops(block, creator.sapling(), 0.2f);
                return;
            }
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.slabDrops(block);
                this.addDrop(block, builder);
                return;
            }
            this.addDrop(block);
        }));
        return null;
    };
    private final Function<DecorativeBlockCreator, Void> decorativeBlockCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.slabDrops(block);
                this.addDrop(block, builder);
                return;
            }
            this.addDrop(block);
        }));
        return null;
    };

    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.DANMAKU_CRAFTING_TABLE);
        addDrop(ModBlocks.STRENGTH_TABLE);
        addDrop(ModBlocks.GENSOKYO_ALTAR);
        addDrop(ModBlocks.MUSIC_BLOCK);

        this.woodCreatorLootFunction.apply(ModBlocks.SPIRITUAL);

        addDrop(ModBlocks.MAGIC_ICE_BLOCK);
        addDrop(ModBlocks.MARISA_HAT_BLOCK);

        addDrop(ModBlocks.POINT_BLOCK);
        addDrop(ModBlocks.POWER_BLOCK);
        addDrop(ModBlocks.SILVER_ORE, (Block block) -> this.oreDrops(block, ModItems.RAW_SILVER));
        addDrop(ModBlocks.DEEPSLATE_SILVER_ORE, (Block block) -> this.oreDrops(block, ModItems.RAW_SILVER));
        Function<Block, LootTable.Builder> orbDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    ModItems.RED_ORB,
                    ModItems.BLUE_ORB,
                    ModItems.YELLOW_ORB,
                    ModItems.GREEN_ORB,
                    ModItems.PURPLE_ORB
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.25f))
                        .with(ItemEntry.builder(item));
                builder.pool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(ModItems.RED_ORB).weight(1))
                    .with(ItemEntry.builder(ModItems.BLUE_ORB).weight(1))
                    .with(ItemEntry.builder(ModItems.YELLOW_ORB).weight(1))
                    .with(ItemEntry.builder(ModItems.GREEN_ORB).weight(1))
                    .with(ItemEntry.builder(ModItems.PURPLE_ORB).weight(1));
            builder.pool(fallbackPool);

            return builder;
        };
        addDrop(ModBlocks.ORB_ORE, orbDropFunction);
        addDrop(ModBlocks.DEEPSLATE_ORB_ORE, orbDropFunction);
        addDrop(ModBlocks.SILVER_BLOCK);
        Function<Block, LootTable.Builder> dreamCrystlDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    ModItems.DREAM_CRYSTAL_FRAGMENT,
                    ModItems.DREAM_CRYSTAL_FRAGMENT
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.25f))
                        .with(ItemEntry.builder(item));
                builder.pool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(ModItems.DREAM_CRYSTAL_FRAGMENT).weight(1))
                    ;
            builder.pool(fallbackPool);

            return builder;
        };
        addDrop(ModBlocks.DREAM_CRYSTAL_ORE, dreamCrystlDropFunction);

        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.ICE_SCALES);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.DREAM_STONE);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.DREAM_STONE_BRICK);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.MOON_STONE);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.MOON_STONE_BRICK);

        for (Fumo fumo : Fumos.getView()) {
            addDrop(fumo.block());
        }

        this.generateMI();
    }

    public void generateMI() {
        for (Block block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            addDrop(block);
        }
        addDrop(MIBlocks.ITEM_DISPLAY);

        for (Map.Entry<Identifier, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            CropBlockCreator.Instance instance = view.getValue();
            instance.generateLoot(this);
        }

        this.woodCreatorLootFunction.apply(MIBlocks.LEMON);
        addDrop(MIBlocks.LEMON_FRUIT_LEAVES, MIBlocks.LEMON.sapling());

        this.woodCreatorLootFunction.apply(MIBlocks.GINKGO);
        addDrop(MIBlocks.GINKGO_FRUIT_LEAVES, MIBlocks.GINKGO.sapling());

        this.woodCreatorLootFunction.apply(MIBlocks.PEACH);
        addDrop(MIBlocks.PEACH_FRUIT_LEAVES, MIBlocks.PEACH.sapling());

//        addDrop(MIBlocks.COOKTOP);
        addDrop(MIBlocks.BLACK_SALT_BLOCK);
        addDrop(MIBlocks.UDUMBARA_FLOWER);
        addDrop(MIBlocks.TREMELLA);
    }
}
