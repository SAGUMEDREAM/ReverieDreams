package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.moon.MoonChunkGenerator;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkMap.class)
public class MoonChunkMapMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"), index = 0)
    private NoiseGeneratorSettings reverie_dreams$moonNoiseSettings(NoiseGeneratorSettings settings, @Local(argsOnly = true) ChunkGenerator generator) {
        return generator instanceof MoonChunkGenerator moon ? moon.settings().value() : settings;
    }
}
