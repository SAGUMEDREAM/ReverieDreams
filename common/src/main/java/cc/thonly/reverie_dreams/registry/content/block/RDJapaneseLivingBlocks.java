package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.decor.living.*;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.block.RDBlocks.registerBlock;

/** The first Japanese living and shrine decoration set. */
public final class RDJapaneseLivingBlocks {
    public static final BlockDelegate SHRINE_BELL = hanging("shrine_bell", HangingOrnamentBlock.Kind.BELL, SoundType.METAL);
    public static final BlockDelegate BELL_ROPE = hanging("bell_rope", HangingOrnamentBlock.Kind.ROPE, SoundType.WOOL);
    public static final BlockDelegate WIND_CHIME = hanging("wind_chime", HangingOrnamentBlock.Kind.CHIME, SoundType.METAL);
    public static final BlockDelegate OSHIIRE = registerBlock("oshiire", LivingStorageBlock::new, wood());
    public static final BlockDelegate TANSU = registerBlock("tansu", LivingStorageBlock::new, wood());
    public static final BlockDelegate FUTON = registerBlock("futon", FutonBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_BED).noOcclusion());
    public static final BlockDelegate FOLDED_FUTON = registerBlock("folded_futon", FoldedFutonBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_WOOL).noOcclusion());
    public static final BlockDelegate IRORI = registerBlock("irori", IroriBlock::new,
            wood().lightLevel(state -> state.getValue(IroriBlock.LIT) ? 12 : 0));
    public static final BlockDelegate JIZAIKAGI = hanging("jizaikagi", HangingOrnamentBlock.Kind.HOOK, SoundType.METAL);
    public static final BlockDelegate TIMBER_WHITE_WALL = registerBlock("timber_white_wall", TimberWhiteWallBlock::new, plaster());
    public static final BlockDelegate TIMBER_WHITE_WALL_HORIZONTAL = registerBlock("timber_white_wall_horizontal", TimberWhiteWallBlock::new, plaster());
    public static final BlockDelegate TIMBER_WHITE_WALL_CROSS = registerBlock("timber_white_wall_cross", TimberWhiteWallBlock::new, plaster());
    public static final List<BlockDelegate> ALL = List.of(SHRINE_BELL, BELL_ROPE, WIND_CHIME, OSHIIRE, TANSU,
            FUTON, FOLDED_FUTON, IRORI, JIZAIKAGI, TIMBER_WHITE_WALL, TIMBER_WHITE_WALL_HORIZONTAL, TIMBER_WHITE_WALL_CROSS);

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion();
    }
    private static BlockBehaviour.Properties plaster() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_TERRACOTTA);
    }
    private static BlockDelegate hanging(String id, HangingOrnamentBlock.Kind kind, SoundType sound) {
        return registerBlock(id, settings -> new HangingOrnamentBlock(kind, settings),
                BlockBehaviour.Properties.of().strength(0.8f).noOcclusion().noCollision().sound(sound));
    }
    public static void initialize() {}
    private RDJapaneseLivingBlocks() {}
}
