package cc.thonly.reverie_dreams.block.decor.living;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portable charcoal brazier; reuses the hearth's lighting and extinguishing rules.
 */
public class HibachiBlock extends IroriBlock {
    public static final MapCodec<HibachiBlock> CODEC = simpleCodec(HibachiBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 1.5, 15.0),
            Block.box(3.0, 1.5, 3.0, 13.0, 7.0, 13.0),
            Block.box(2.0, 7.0, 2.0, 14.0, 8.0, 14.0));

    public HibachiBlock(Properties properties) {
        super(properties, SHAPE);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.42 + random.nextDouble() * 0.16, pos.getY() + 0.58,
                    pos.getZ() + 0.42 + random.nextDouble() * 0.16, 0.0, 0.02, 0.0);
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.44 + random.nextDouble() * 0.12, pos.getY() + 0.52,
                    pos.getZ() + 0.44 + random.nextDouble() * 0.12, 0.0, 0.008, 0.0);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.3f, 1.1f, false);
        }
    }
}
