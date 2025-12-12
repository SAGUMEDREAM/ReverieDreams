package cc.thonly.reverie_dreams.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ChillCropBlock extends AbstractCropBlock {
    public static final MapCodec<ChillCropBlock> CODEC = ChillCropBlock.simpleCodec(ChillCropBlock::new);

    public ChillCropBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 4;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return CropAgeUtil.fromInt(4);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
