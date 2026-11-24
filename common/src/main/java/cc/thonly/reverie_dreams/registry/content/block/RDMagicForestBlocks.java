package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.nature.FicusMicrocarpaBlock;
import cc.thonly.reverie_dreams.block.nature.PuffYoBushBlock;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;

public final class RDMagicForestBlocks {
    public static final BlockDelegate FICUS_MICROCARPA = RDBlocks.registerSimpleBlock("ficus_microcarpa",
            FicusMicrocarpaBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollision().instabreak().noOcclusion()
                    .sound(SoundType.GRASS).randomTicks().pushReaction(PushReaction.DESTROY));
    public static final BlockDelegate PUFF_YO_BUSH = RDBlocks.registerSimpleBlock("puff_yo_bush",
            PuffYoBushBlock::new, RDPlantBlocks.createPlantSettings().randomTicks());
    public static final List<BlockDelegate> ALL = List.of(FICUS_MICROCARPA, PUFF_YO_BUSH);

    public static void initialize() {
    }

    private RDMagicForestBlocks() {
    }
}
