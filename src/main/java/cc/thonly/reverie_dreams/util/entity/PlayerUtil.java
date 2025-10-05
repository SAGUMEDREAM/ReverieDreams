package cc.thonly.reverie_dreams.util.entity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PlayerUtil {

    public static List<ServerPlayerEntity> getNearbyPlayers(ServerWorld world, Vec3d center, double radius) {
        double radiusSquared = radius * radius;
        return world.getPlayers(player -> player.squaredDistanceTo(center) <= radiusSquared);
    }

    public static List<ServerPlayerEntity> getNearbyPlayers(ServerWorld world, BlockPos pos, double radius) {
        Vec3d center = Vec3d.ofCenter(pos);
        return getNearbyPlayers(world, center, radius);
    }
}
