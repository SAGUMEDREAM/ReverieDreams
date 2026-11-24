package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.api.block.DispenserBlockItemBehaviors;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
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
        // A dispenser pressed against the map edge would otherwise shoot buckets,
        // fire, bone meal or powder snow into the protected ring outside the map.
        if (GensokyoBoundary.isOutsideMap(level, pos.relative(state.getValue(DispenserBlock.FACING)))) {
            ci.cancel();
            return;
        }
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
