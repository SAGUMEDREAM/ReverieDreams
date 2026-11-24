package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.storage.GensokyoChunkCompression;
import cc.thonly.reverie_dreams.world.gensokyo.storage.ZstdChunkCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Mixin(RegionFile.class)
public abstract class RegionFileZstdMixin {
    @Shadow
    @Final
    private RegionStorageInfo info;
    @Shadow
    @Final
    @Mutable
    private RegionFileVersion version;

    @Shadow
    private DataInputStream createChunkInputStream(ChunkPos pos, byte versionId, InputStream stream) throws IOException {
        throw new AssertionError();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/level/chunk/storage/RegionFileVersion;Z)V", at = @At("RETURN"))
    private void reverie_dreams$selectCompression(RegionStorageInfo storageInfo, Path path, Path externalFileDir,
                                                  RegionFileVersion originalVersion, boolean sync, CallbackInfo ci) {
        if (GensokyoChunkCompression.isTerrainStorage(storageInfo) && ReverieDreams.config().enableGensokyoChunkZstd) {
            // The vanilla chunk buffer and external .mcc stub must both write version 127.
            this.version = GensokyoChunkCompression.zstdVersion(ReverieDreams.config().gensokyoChunkZstdLevel);
        }
    }

    // Inline .mca data and external .mcc data call the shared decoder from different methods.
    @Redirect(method = {"getChunkDataInputStream", "createExternalChunkInputStream"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/storage/RegionFile;createChunkInputStream(Lnet/minecraft/world/level/ChunkPos;BLjava/io/InputStream;)Ljava/io/DataInputStream;"), require = 2)
    private DataInputStream reverie_dreams$decompressChunk(RegionFile region, ChunkPos pos, byte versionId,
                                                           InputStream stream) throws IOException {
        // Read support is deliberately independent of the write setting, including after restart.
        if (versionId == RegionFileVersion.VERSION_CUSTOM.getId() && GensokyoChunkCompression.isTerrainStorage(this.info)) {
            return ZstdChunkCodec.decodeCustom(stream, original -> this.createChunkInputStream(pos, versionId, original));
        }
        return this.createChunkInputStream(pos, versionId, stream);
    }
}
