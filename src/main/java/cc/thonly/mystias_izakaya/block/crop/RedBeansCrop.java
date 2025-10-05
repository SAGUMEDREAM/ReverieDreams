package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.PlantBlock;
import net.minecraft.state.property.IntProperty;

public class RedBeansCrop extends AbstractCropBlock {
    public static final MapCodec<RedBeansCrop> CODEC = RedBeansCrop.createCodec(RedBeansCrop::new);

    public RedBeansCrop(Settings settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 6;
    }

    @Override
    public IntProperty getAgeProperty() {
        return CropAgeUtil.fromInt(6);
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }
}
