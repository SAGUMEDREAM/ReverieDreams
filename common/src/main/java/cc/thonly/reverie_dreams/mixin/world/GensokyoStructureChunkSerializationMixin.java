package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoStructureChunkState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public abstract class GensokyoStructureChunkSerializationMixin {
    @Inject(method = "parse", at = @At("RETURN"))
    private static void reverie$parse(LevelHeightAccessor height, PalettedContainerFactory factory, CompoundTag tag, CallbackInfoReturnable<SerializableChunkData> cir) {
        GensokyoStructureChunkState.parsed(cir.getReturnValue(), tag);
    }

    @Inject(method = "copyOf", at = @At("RETURN"))
    private static void reverie$capture(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
        GensokyoStructureChunkState.captured(cir.getReturnValue(), chunk);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void reverie$read(ServerLevel level, PoiManager poi, RegionStorageInfo info, ChunkPos pos, CallbackInfoReturnable<ProtoChunk> cir) {
        GensokyoStructureChunkState.read((SerializableChunkData) (Object) this, cir.getReturnValue());
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void reverie$write(CallbackInfoReturnable<CompoundTag> cir) {
        GensokyoStructureChunkState.write((SerializableChunkData) (Object) this, cir.getReturnValue());
    }
}
