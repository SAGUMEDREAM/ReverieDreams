package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.api.block.CustomMatureBlock;
import net.minecraft.world.level.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin implements CustomMatureBlock {

}
