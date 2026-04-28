package cc.thonly.reverie_dreams.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TomatoCropBlock extends AbstractCropBlock {
    public static final MapCodec<TomatoCropBlock> CODEC = TomatoCropBlock.simpleCodec(TomatoCropBlock::new);

    public TomatoCropBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 3;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return CropAgeUtil.fromInt(3);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
