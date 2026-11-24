package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.nature.*;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.GensokyoLandmarkBootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;
import java.util.Optional;

/** Loaded from RDPlantBlocks.initialize before the existing deferred registries are applied. */
public final class RDLandmarkBlocks {
    public static final BlockDelegate CLOVER_PATCH=RDBlocks.registerSimpleBlock("clover_patch",CloverPatchBlock::new,RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate RED_SPIDER_LILY=RDBlocks.registerSimpleBlock("red_spider_lily",DirectionalFlowerBlock::new,RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate SHORT_SUNFLOWER=RDBlocks.registerSimpleBlock("short_sunflower",DirectionalFlowerBlock::new,RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate GARDEN_SUNFLOWER=RDBlocks.registerSimpleBlock("garden_sunflower",GardenSunflowerBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.SUNFLOWER));
    public static final BlockDelegate PURPLE_CHERRY_LEAVES=RDBlocks.registerSimpleBlock("purple_cherry_leaves",PurpleCherryLeavesBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_LEAVES));
    public static final BlockDelegate PURPLE_CHERRY_PETALS=RDBlocks.registerSimpleBlock("purple_cherry_petals",PurpleCherryPetalsBlock::new,BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_PETALS));
    public static final BlockDelegate PURPLE_CHERRY_SAPLING=RDBlocks.registerSimpleBlock("purple_cherry_sapling",
            p->new SaplingBlock(new TreeGrower(ReverieDreams.id("purple_cherry").toString(),Optional.empty(),Optional.of(GensokyoLandmarkBootstrap.PURPLE_CHERRY),Optional.empty()),p),
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING));
    public static final List<BlockDelegate> ALL=List.of(CLOVER_PATCH,RED_SPIDER_LILY,SHORT_SUNFLOWER,GARDEN_SUNFLOWER,PURPLE_CHERRY_LEAVES,PURPLE_CHERRY_PETALS,PURPLE_CHERRY_SAPLING);
    public static void initialize() {}
    private RDLandmarkBlocks() {}
}
