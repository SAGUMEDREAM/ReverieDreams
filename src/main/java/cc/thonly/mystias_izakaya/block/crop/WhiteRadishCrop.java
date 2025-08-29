package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.PlantBlock;
import net.minecraft.state.property.IntProperty;

public class WhiteRadishCrop extends AbstractCropBlock {
    public static final MapCodec<WhiteRadishCrop> CODEC = WhiteRadishCrop.createCodec(WhiteRadishCrop::new);

    public WhiteRadishCrop(Settings settings) {
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
