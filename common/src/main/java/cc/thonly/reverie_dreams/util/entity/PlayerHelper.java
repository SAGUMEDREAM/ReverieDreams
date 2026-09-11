package cc.thonly.reverie_dreams.util.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@SuppressWarnings("resource")
public class PlayerHelper {

    public static List<ServerPlayer> getNearbyPlayers(ServerLevel world, Vec3 center, double radius) {
        double radiusSquared = radius * radius;
        return world.getPlayers(player -> player.distanceToSqr(center) <= radiusSquared);
    }

    public static List<ServerPlayer> getNearbyPlayers(ServerLevel world, BlockPos pos, double radius) {
        Vec3 center = Vec3.atCenterOf(pos);
        return getNearbyPlayers(world, center, radius);
    }

    public static BlockHitResult getPlayerLookBlock(ServerPlayer player, double reach) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(reach));

        ClipContext clipContext = new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        );

        return player.level().clip(clipContext);
    }
}
