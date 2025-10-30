package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BroccoliCrop extends AbstractCropBlock {
    public static final MapCodec<BroccoliCrop> CODEC = BroccoliCrop.simpleCodec(BroccoliCrop::new);

    public BroccoliCrop(Properties settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 6;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return CropAgeUtil.fromInt(6);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
