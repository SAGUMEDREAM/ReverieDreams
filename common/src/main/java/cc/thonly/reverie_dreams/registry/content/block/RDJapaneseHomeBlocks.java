package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.decor.KotatsuBlock;
import cc.thonly.reverie_dreams.block.decor.ThinDecorBlock;
import cc.thonly.reverie_dreams.block.decor.living.HibachiBlock;
import cc.thonly.reverie_dreams.block.decor.living.LivingStorageBlock;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.block.RDBlocks.registerBlock;

/** Residential furniture: kotatsu, hibachi, household shrine, shoji door and altar. */
public final class RDJapaneseHomeBlocks {
    private static final VoxelShape KAMIDANA_SHAPE = Shapes.or(
            Block.box(2.0, 3.0, 0.0, 14.0, 13.0, 1.0),
            Block.box(1.0, 3.0, 0.0, 15.0, 4.0, 8.0),
            Block.box(4.0, 4.0, 1.0, 12.0, 11.0, 6.0),
            Block.box(1.0, 11.5, 0.0, 15.0, 13.0, 8.0),
            Block.box(3.0, 13.0, 0.0, 13.0, 14.0, 7.0));

    public static final BlockDelegate KOTATSU = registerBlock("kotatsu", KotatsuBlock::new,
            wood().strength(2.0f));
    public static final BlockDelegate HIBACHI = registerBlock("hibachi", HibachiBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA).noOcclusion()
                    .lightLevel(state -> state.getValue(HibachiBlock.LIT) ? 13 : 0));
    public static final BlockDelegate KAMIDANA = registerBlock("kamidana",
            settings -> new ThinDecorBlock(settings, KAMIDANA_SHAPE, true, true),
            wood().strength(1.2f));
    public static final BlockDelegate SHOJI_DOOR = registerBlock("shoji_door",
            settings -> new DoorBlock(BlockSetType.OAK, settings),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR));
    public static final BlockDelegate BUTSUDAN = registerBlock("butsudan", LivingStorageBlock::new,
            wood().strength(2.5f));
    public static final List<BlockDelegate> ALL = List.of(KOTATSU, HIBACHI, KAMIDANA, SHOJI_DOOR, BUTSUDAN);

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion();
    }

    public static void initialize() {}

    private RDJapaneseHomeBlocks() {}
}
