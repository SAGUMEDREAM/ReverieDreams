package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.PlantBlock;
import net.minecraft.state.property.IntProperty;

public class ToonCrop extends AbstractCropBlock {
    public static final MapCodec<ToonCrop> CODEC = ToonCrop.createCodec(ToonCrop::new);

    public ToonCrop(Settings settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 8;
    }

    @Override
    public IntProperty getAgeProperty() {
        return CropAgeUtil.fromInt(8);
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }
}
