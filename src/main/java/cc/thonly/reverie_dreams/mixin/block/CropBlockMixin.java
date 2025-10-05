package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.interfaces.IMatureBlock;
import net.minecraft.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin implements IMatureBlock {

}
