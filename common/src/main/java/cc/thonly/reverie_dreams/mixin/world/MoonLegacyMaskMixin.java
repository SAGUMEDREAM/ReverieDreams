package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.moon.MoonChunkGenerator;
import cc.thonly.reverie_dreams.world.moon.compat.MoonLegacyMask;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cc.thonly.reverie_dreams.world.moon.compat.MoonSaveUpgrade;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public class MoonLegacyMaskMixin {
    @Inject(method = "createLevels", at = @At("HEAD"))
    private void reverie_dreams$loadMoonMask(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        try {
            MoonSaveUpgrade.markFreshWorld(server.getWorldPath(LevelResource.ROOT));
            MoonLegacyMask mask = MoonLegacyMask.load(server.getWorldPath(LevelResource.ROOT));
            server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).forEach(stem -> {
                if (stem.generator() instanceof MoonChunkGenerator moon)
                    moon.setReliefStrength(mask::depressionFactor);
            });
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot load Moon legacy seam mask", failure);
        }
    }
}
