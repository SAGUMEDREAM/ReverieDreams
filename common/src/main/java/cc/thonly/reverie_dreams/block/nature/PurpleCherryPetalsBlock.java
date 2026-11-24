package cc.thonly.reverie_dreams.block.nature;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FlowerBedBlock;

/**
 * Public native flower-bed factory, preserving petal counts and bone meal behaviour.
 */
public final class PurpleCherryPetalsBlock extends FlowerBedBlock {
    public static final MapCodec<FlowerBedBlock> CODEC = simpleCodec(PurpleCherryPetalsBlock::new);

    public PurpleCherryPetalsBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FlowerBedBlock> codec() {
        return CODEC;
    }
}
