package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.BlockDisplay.class)
public interface BlockDisplayAccessor {
    @Invoker("setBlockState")
    void reverie_dreams$setBlockState(BlockState blockState);

    @Invoker("getBlockState")
    BlockState reverie_dreams$getBlockState();
}
