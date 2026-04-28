package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseSettings.class)
public interface GenerationShapeConfigAccessor {
    @Accessor("OVERWORLD_NOISE_SETTINGS")
    static NoiseSettings getSurface() {
        throw new UnsupportedOperationException();
    }

    @Accessor("NETHER_NOISE_SETTINGS")
    static NoiseSettings getNether() {
        throw new UnsupportedOperationException();
    }

    @Accessor("END_NOISE_SETTINGS")
    static NoiseSettings getEnd() {
        throw new UnsupportedOperationException();
    }

    @Accessor("CAVES_NOISE_SETTINGS")
    static NoiseSettings getCaves() {
        throw new UnsupportedOperationException();
    }

    @Accessor("FLOATING_ISLANDS_NOISE_SETTINGS")
    static NoiseSettings getFloatingIslands() {
        throw new UnsupportedOperationException();
    }
}
