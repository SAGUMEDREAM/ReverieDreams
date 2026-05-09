package cc.thonly.reverie_dreams.fabric.polymer.mixin;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Pseudo
@Mixin(value = PolymerBlockUtils.class)
public class PolymerBlockUtilsMixin {
    @Inject(method = "getBlockBreakBlockStateSafely", at = @At("HEAD"), cancellable = true)
    private static void check(PolymerBlock block, BlockState blockState, int maxDistance, PacketContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState out = block.getPolymerBlockState(blockState, context);
        if (out == null) {
            System.out.println("Find null state");
            System.out.println(BuiltInRegistries.BLOCK.getKey((Block) block));
        }
    }
}
