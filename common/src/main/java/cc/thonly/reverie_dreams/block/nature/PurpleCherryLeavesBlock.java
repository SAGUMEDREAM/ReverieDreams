package cc.thonly.reverie_dreams.block.nature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;

/**
 * Baked purple blossoms retain their palette outside Muenzuka too.
 */
public final class PurpleCherryLeavesBlock extends TintedParticleLeavesBlock {
    public static final MapCodec<PurpleCherryLeavesBlock> CODEC = simpleCodec(PurpleCherryLeavesBlock::new);

    public PurpleCherryLeavesBlock(Properties properties) {
        super(.008F, properties);
    }

    @Override
    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ParticleUtils.spawnParticleBelow(level, pos, random, ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xB29BC8));
    }
}
