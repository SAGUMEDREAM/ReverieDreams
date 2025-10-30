package cc.thonly.reverie_dreams.mixin.patches;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(value = PolymerBlockUtils.class)
public class PolymerBlockUtilsMixin {
    @Inject(method = "getBlockStateSafely(Leu/pb4/polymer/core/api/block/PolymerBlock;Lnet/minecraft/block/BlockState;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
    private static void check(PolymerBlock block, BlockState blockState, PacketContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState out = block.getPolymerBlockState(blockState, context);
        if (out == null) {
            System.out.println("Find null state");
            System.out.println(BuiltInRegistries.BLOCK.getKey((Block) block));
        }
    }
}
