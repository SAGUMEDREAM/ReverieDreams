package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseSettings.class)
public interface GenerationShapeConfigAccessor {
    @Accessor("SURFACE")
    static NoiseSettings getSurface() {
        throw new UnsupportedOperationException();
    }

    @Accessor("NETHER")
    static NoiseSettings getNether() {
        throw new UnsupportedOperationException();
    }

    @Accessor("END")
    static NoiseSettings getEnd() {
        throw new UnsupportedOperationException();
    }

    @Accessor("CAVES")
    static NoiseSettings getCaves() {
        throw new UnsupportedOperationException();
    }

    @Accessor("FLOATING_ISLANDS")
    static NoiseSettings getFloatingIslands() {
        throw new UnsupportedOperationException();
    }
}
