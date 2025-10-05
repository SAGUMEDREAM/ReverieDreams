package cc.thonly.mystias_izakaya.block.crop;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.block.CropAgeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.PlantBlock;
import net.minecraft.state.property.IntProperty;

public class ChillCrop extends AbstractCropBlock {
    public static final MapCodec<ChillCrop> CODEC = ChillCrop.createCodec(ChillCrop::new);

    public ChillCrop(Settings settings) {
        super(settings);
    }

    @Override
    public Integer getMaxAge() {
        return 7;
    }

    @Override
    public IntProperty getAgeProperty() {
        return CropAgeUtil.fromInt(7);
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }
}
