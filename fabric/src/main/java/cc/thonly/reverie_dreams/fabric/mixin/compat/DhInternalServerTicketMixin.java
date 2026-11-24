package cc.thonly.reverie_dreams.fabric.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Two DH batches may request the same chunk. TicketStorage deduplicates by type
 * and level, so only the final DH release should remove that physical ticket.
 * Both wrapped calls run on the owning server thread.
 */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.common.wrappers.worldGeneration.DhInternalServerGenerator", remap = false)
public abstract class DhInternalServerTicketMixin {
    @Unique
    private static final Map<ServerChunkCache, Map<Long, Integer>> reverie$dhUsers = new WeakHashMap<>();

    @WrapOperation(method = "lambda$requestChunkFromServerAsync$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;addTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V", remap = false), remap = false)
    private void reverie$acquireDhTicket(ServerChunkCache cache, TicketType type, ChunkPos pos, int radius, Operation<Void> original) {
        original.call(cache, type, pos, radius);
        synchronized (reverie$dhUsers) {
            reverie$dhUsers.computeIfAbsent(cache, ignored -> new HashMap<>()).merge(pos.pack(), 1, Integer::sum);
        }
    }

    @WrapOperation(method = "lambda$releaseChunkFromServerAsync$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;removeTicketWithRadius(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;I)V", remap = false), remap = false)
    private void reverie$releaseDhTicket(ServerChunkCache cache, TicketType type, ChunkPos pos, int radius, Operation<Void> original) {
        synchronized (reverie$dhUsers) {
            var users = reverie$dhUsers.get(cache);
            if (users != null) {
                long key = pos.pack();
                int count = users.getOrDefault(key, 0);
                if (count > 1) {
                    users.put(key, count - 1);
                    return;
                }
                users.remove(key);
                if (users.isEmpty())
                    reverie$dhUsers.remove(cache);
            }
        }
        original.call(cache, type, pos, radius);
    }
}
