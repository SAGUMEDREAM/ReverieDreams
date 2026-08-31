package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {

    @Accessor("ROTATION_FACING")
    static PropertyDispatch<VariantMutator> getRotationFacing() {
        throw new AssertionError();
    }

    @Accessor("ROTATIONS_COLUMN_WITH_FACING")
    static PropertyDispatch<VariantMutator> getRotationsColumnWithFacing() {
        throw new AssertionError();
    }

    @Accessor("ROTATION_TORCH")
    static PropertyDispatch<VariantMutator> getRotationTorch() {
        throw new AssertionError();
    }

    @Accessor("ROTATION_HORIZONTAL_FACING_ALT")
    static PropertyDispatch<VariantMutator> getRotationHorizontalFacingAlt() {
        throw new AssertionError();
    }

    @Accessor("ROTATION_HORIZONTAL_FACING")
    static PropertyDispatch<VariantMutator> getRotationHorizontalFacing() {
        throw new AssertionError();
    }
}