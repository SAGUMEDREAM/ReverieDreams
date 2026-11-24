package cc.thonly.reverie_dreams.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.HugeMushroomBlock;

/**
 * Light lives in the block state, including every connected/skinless cap variant.
 */
public final class MagicMushroomCapBlock extends HugeMushroomBlock {
    public static final int LIGHT_LEVEL = 10;
    public static final MapCodec<HugeMushroomBlock> CODEC = simpleCodec(MagicMushroomCapBlock::new);

    public MagicMushroomCapBlock(Properties properties) {
        super(properties.lightLevel(state -> LIGHT_LEVEL));
    }

    @Override
    public MapCodec<HugeMushroomBlock> codec() {
        return CODEC;
    }
}
