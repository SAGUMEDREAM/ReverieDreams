package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FertilizableFlowerBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import dev.architectury.hooks.item.tool.AxeItemHooks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

@SuppressWarnings("SpellCheckingInspection")
public class RDWoodBlocks {
    public static final WoodBundle SPIRITUAL_BUNDLE = WoodBundle.create("spiritual", SaplingGeneratorInit.SPIRITUAL_TREE).build();
    public static final BlockDelegate BLESSED_SPIRITUAL_LOG = RDBlocks.registerBlock("blessed_spiritual_log", RotatedPillarBlock::new, Blocks.OAK_LOG.properties());
    public static final WoodBundle LEMON_BUNDLE = WoodBundle.create("lemon", SaplingGeneratorInit.LEMON_TREE).build();
    public static final BlockDelegate LEMON_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("lemon_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.LEMON, LEMON_BUNDLE.leaves().asBlock(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodBundle GINKGO_BUNDLE = WoodBundle.create("ginkgo", SaplingGeneratorInit.GINKGO_TREE).build();
    public static final BlockDelegate GINKGO_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("ginkgo_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.GINKGO, GINKGO_BUNDLE.leaves().asBlock(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodBundle PEACH_BUNDLE = WoodBundle.create("peach", SaplingGeneratorInit.PEACH_TREE).build();
    public static final BlockDelegate PEACH_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("peach_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.PEACH, PEACH_BUNDLE.leaves().asBlock(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    // Orchard trees reuse vanilla logs and leaf material; only the fruit-bearing leaves and saplings are new.
    public static final BlockDelegate PLUM_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("plum_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.PLUM, Blocks.CHERRY_LEAVES, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LEAVES));
    public static final BlockDelegate PLUM_SAPLING = RDBlocks.registerSimpleBlock("plum_sapling", (settings) -> new SaplingBlock(SaplingGeneratorInit.PLUM_TREE, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING));
    public static final BlockDelegate PINE_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("pine_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.PINE_NUT, Blocks.SPRUCE_LEAVES, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES));
    public static final BlockDelegate PINE_SAPLING = RDBlocks.registerSimpleBlock("pine_sapling", (settings) -> new SaplingBlock(SaplingGeneratorInit.PINE_TREE, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING));
    public static final BlockDelegate CHESTNUT_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("chestnut_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.CHESTNUT, Blocks.DARK_OAK_LEAVES, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LEAVES));
    public static final BlockDelegate CHESTNUT_SAPLING = RDBlocks.registerSimpleBlock("chestnut_sapling", (settings) -> new SaplingBlock(SaplingGeneratorInit.CHESTNUT_TREE, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SAPLING));
    public static final WoodBundle IROHA_MAPLE_BUNDLE = WoodBundle.create("iroha_maple", SaplingGeneratorInit.IROHA_MAPLE_TREE)
            .leafTintColor(0xFFFF6C44) // 红枫
            .build();
    public static final WoodBundle ITAYA_MAPLE_BUNDLE = WoodBundle.create("itaya_maple", SaplingGeneratorInit.ITAYA_MAPLE_TREE)
            .leafTintColor(0xFFFFDD79) // 黄枫
            .build();
    public static final WoodBundle HAUCHIWA_MAPLE_BUNDLE = WoodBundle.create("hauchiwa_maple", SaplingGeneratorInit.HAUCHIWA_MAPLE_TREE)
            .leafTintColor(0xFFFFA164) // 橙枫
            .build();
    public static final BlockDelegate IROHA_MAPLE_LEAF_LITTER = RDBlocks.registerSimpleBlock("iroha_maple_leaf_litter", LeafLitterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER));
    public static final BlockDelegate ITAYA_MAPLE_LEAF_LITTER = RDBlocks.registerSimpleBlock("itaya_maple_leaf_litter", LeafLitterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER));
    public static final BlockDelegate HAUCHIWA_MAPLE_LEAF_LITTER = RDBlocks.registerSimpleBlock("hauchiwa_maple_leaf_litter", LeafLitterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER));
    public static final BlockDelegate UDUMBARA_FLOWER = RDBlocks.registerSimpleBlock("udumbara_flower", (settings) -> new FertilizableFlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate TREMELLA = RDBlocks.registerSimpleBlock("tremella", (settings) -> new FlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());

    static {
        LEMON_BUNDLE.setFruitLeaves(LEMON_FRUIT_LEAVES);
        GINKGO_BUNDLE.setFruitLeaves(GINKGO_FRUIT_LEAVES);
        PEACH_BUNDLE.setFruitLeaves(PEACH_FRUIT_LEAVES);
        IROHA_MAPLE_BUNDLE.setLeafLitter(IROHA_MAPLE_LEAF_LITTER);
        ITAYA_MAPLE_BUNDLE.setLeafLitter(ITAYA_MAPLE_LEAF_LITTER);
        HAUCHIWA_MAPLE_BUNDLE.setLeafLitter(HAUCHIWA_MAPLE_LEAF_LITTER);
    }

    public static void initialize() {
        RDGensokyoSaplings.initialize();
        RDDeadwoodBlocks.initialize();
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            AxeItemHooks.addStrippable(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.get(), RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().get());
        });
    }
}
