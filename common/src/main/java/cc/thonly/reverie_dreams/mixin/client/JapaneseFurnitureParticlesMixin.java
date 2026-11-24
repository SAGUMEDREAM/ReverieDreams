package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.JapaneseFurnitureParticles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class JapaneseFurnitureParticlesMixin {
    @ModifyExpressionValue(method = "addDestroyBlockEffect", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape reverieDreams$furnitureParticleEnvelope(VoxelShape outline, BlockPos pos, BlockState state) {
        return JapaneseFurnitureParticles.samplingShape(state.getBlock(), outline);
    }
}
