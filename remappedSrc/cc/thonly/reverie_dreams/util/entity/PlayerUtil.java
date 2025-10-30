package cc.thonly.reverie_dreams.util.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class PlayerUtil {

    public static List<ServerPlayer> getNearbyPlayers(ServerLevel world, Vec3 center, double radius) {
        double radiusSquared = radius * radius;
        return world.getPlayers(player -> player.distanceToSqr(center) <= radiusSquared);
    }

    public static List<ServerPlayer> getNearbyPlayers(ServerLevel world, BlockPos pos, double radius) {
        Vec3 center = Vec3.atCenterOf(pos);
        return getNearbyPlayers(world, center, radius);
    }
}
