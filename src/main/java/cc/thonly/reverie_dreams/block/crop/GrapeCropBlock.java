package cc.thonly.reverie_dreams.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class GrapeCropBlock extends AbstractCropBlock {
    public static final MapCodec<GrapeCropBlock> CODEC = GrapeCropBlock.simpleCodec(GrapeCropBlock::new);

    public GrapeCropBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 5;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return CropAgeUtil.fromInt(5);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
