package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.api.block.DispenserBlockItemBehaviors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    @Inject(
            method = "dispenseFrom",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dreams$dispenseFrom(
            ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci
    ) {
        DispenserBlockItemBehaviors.TriggerResult result = DispenserBlockItemBehaviors.get()
                .onTrigger(
                        level,
                        state,
                        pos
                );
        if (!result.isPass()) {
            ci.cancel();
        }
    }
}
