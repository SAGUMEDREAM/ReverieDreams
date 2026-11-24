package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.world.moon.compat.MoonSaveUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import cc.thonly.reverie_dreams.world.moon.MoonChunkGenerator;

import java.io.IOException;

import net.minecraft.resources.Identifier;

@Mixin(LevelStorageSource.class)
public class MoonMigrationMixin {
    @Inject(method = "getLevelDataAndDimensions", at = @At("HEAD"))
    private static void reverie_dreams$upgradeMoon(LevelStorageSource.LevelStorageAccess access,
                                                   Dynamic<?> data, WorldDataConfiguration configuration, Registry<LevelStem> dimensions,
                                                   HolderLookup.Provider registries, CallbackInfoReturnable<LevelDataAndDimensions> cir) {
        try {
            LevelStem moon = dimensions.getValue(Identifier.parse(ReverieDreams.MOD_ID + ":the_moon"));
            MoonSaveUpgrade.upgrade(access.getLevelPath(LevelResource.ROOT), moon != null
                    && moon.generator() instanceof MoonChunkGenerator);
        } catch (IOException failure) {
            throw new IllegalStateException("Moon save upgrade failed. Original Moon is retained in place or backups/moon-terrain-v2; do not open with older mod until rollback.", failure);
        }
    }
}
