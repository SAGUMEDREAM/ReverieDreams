package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.decor.ThinDecorBlock;
import cc.thonly.reverie_dreams.block.decor.shrine.IncenseBurnerBlock;
import cc.thonly.reverie_dreams.block.decor.shrine.SaisenBakoBlock;
import cc.thonly.reverie_dreams.block.decor.shrine.ShrineBlock;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.block.RDBlocks.registerBlock;

/** Shrine furniture completing the first Japanese decoration batch. */
public final class RDJapaneseShrineBlocks {
    public static final BlockDelegate SHIMENAWA = registerBlock("shimenawa",
            settings -> new ThinDecorBlock(settings, Block.box(0.0, 3.0, 0.0, 16.0, 16.0, 5.0), false, true),
            light());
    public static final BlockDelegate GOHEI = registerBlock("gohei",
            settings -> new ShrineBlock(settings, Block.box(3.0, 0.0, 6.0, 13.0, 16.0, 11.0),
                    Block.box(3.0, 0.0, 6.0, 13.0, 16.0, 11.0)),
            light());
    public static final BlockDelegate SAISEN_BAKO = registerBlock("saisen_bako", SaisenBakoBlock::new, wood());
    public static final BlockDelegate OMIKUJI_RACK = registerBlock("omikuji_rack",
            settings -> new ShrineBlock(settings, Block.box(0.0, 3.0, 12.0, 16.0, 16.0, 16.0),
                    Block.box(0.0, 3.0, 12.0, 16.0, 16.0, 16.0)),
            wood());
    public static final BlockDelegate INCENSE_BURNER = registerBlock("incense_burner", IncenseBurnerBlock::new,
            stone().lightLevel(state -> state.getValue(IncenseBurnerBlock.LIT) ? 7 : 0));
    public static final BlockDelegate JIZO_STATUE = registerBlock("jizo_statue",
            settings -> new ShrineBlock(settings, Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0),
                    Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0)),
            stone());
    public static final BlockDelegate YUKIMI_LANTERN = registerBlock("yukimi_lantern",
            settings -> new ShrineBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0),
                    Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
            stone().lightLevel(state -> 14));
    public static final BlockDelegate HOKORA = registerBlock("hokora",
            settings -> new ShrineBlock(settings, Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 15.0),
                    Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 15.0)),
            wood());
    public static final List<BlockDelegate> ALL = List.of(SHIMENAWA, GOHEI, SAISEN_BAKO, OMIKUJI_RACK,
            INCENSE_BURNER, JIZO_STATUE, YUKIMI_LANTERN, HOKORA);

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).noOcclusion();
    }
    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion();
    }
    private static BlockBehaviour.Properties light() {
        return BlockBehaviour.Properties.of().strength(0.3f).noOcclusion().sound(SoundType.WOOL);
    }
    public static void initialize() {}
    private RDJapaneseShrineBlocks() {}
}
