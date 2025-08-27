package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GenerationShapeConfig.class)
public interface GenerationShapeConfigAccessor {
    @Accessor("SURFACE")
    static GenerationShapeConfig getSurface() {
        throw new UnsupportedOperationException();
    }

    @Accessor("NETHER")
    static GenerationShapeConfig getNether() {
        throw new UnsupportedOperationException();
    }

    @Accessor("END")
    static GenerationShapeConfig getEnd() {
        throw new UnsupportedOperationException();
    }

    @Accessor("CAVES")
    static GenerationShapeConfig getCaves() {
        throw new UnsupportedOperationException();
    }

    @Accessor("FLOATING_ISLANDS")
    static GenerationShapeConfig getFloatingIslands() {
        throw new UnsupportedOperationException();
    }
}
