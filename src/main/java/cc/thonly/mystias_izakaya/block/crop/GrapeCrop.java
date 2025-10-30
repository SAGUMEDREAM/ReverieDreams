package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class GrapeCrop extends AbstractCropBlock {
    public static final MapCodec<GrapeCrop> CODEC = GrapeCrop.simpleCodec(GrapeCrop::new);

    public GrapeCrop(Properties settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 7;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return CropAgeUtil.fromInt(7);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
