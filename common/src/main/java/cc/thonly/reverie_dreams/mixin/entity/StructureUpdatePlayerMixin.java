package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class StructureUpdatePlayerMixin {
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), cancellable = true)
    private void reverie$protectTeleportDuringUpdate(ServerLevel target, double x, double y, double z, Set<Relative> relative, float yaw, float pitch, boolean camera, CallbackInfoReturnable<Boolean> ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        double px = relative.contains(Relative.X) ? player.getX() + x : x, pz = relative.contains(Relative.Z) ? player.getZ() + z : z;
        if (StructureUpdateGate.blocks(target, new ChunkPos((int) Math.floor(px) >> 4, (int) Math.floor(pz) >> 4).pack())) {
            player.sendSystemMessage(Component.literal("该地标正在维护，请等待结构任务完成后再进入。"));
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void reverie$evacuateLoginInsideIncompleteStructure(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = player.level();
        if (!StructureUpdateGate.blocks(level, player.chunkPosition().pack()))
            return;
        ServerLevel safe = level.getServer().overworld();
        var spawn = safe.getRespawnData();
        var pos = spawn.pos();
        player.teleportTo(safe, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, Set.of(), 0, 0, true);
        player.sendSystemMessage(Component.literal("登录位置处于未完成的地标维护区，已暂时返回主世界安全出生点。"));
    }
}
