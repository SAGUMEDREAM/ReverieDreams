package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFarmGoal;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

public final class NPCWorldObservation {
    private NPCWorldObservation() {
    }

    public static Vec3 standPosition(NPCSimpleEntity npc, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos stand = pos.relative(direction);
            if (!npc.level().hasChunkAt(stand) || !npc.level().getBlockState(stand).getCollisionShape(npc.level(), stand).isEmpty()
                    || !npc.level().getBlockState(stand.above()).getCollisionShape(npc.level(), stand.above()).isEmpty()
                    || npc.level().getBlockState(stand.below()).getCollisionShape(npc.level(), stand.below()).isEmpty())
                continue;
            var path = npc.getNavigation().createPath(stand, 0);
            if (path != null && path.canReach())
                return Vec3.atBottomCenterOf(stand);
        }
        return null;
    }

    public static JsonArray facilities(NPCSimpleEntity npc, ServerPlayer player) {
        JsonArray results = new JsonArray();
        var positions = new ArrayList<BlockPos>();
        var origin = npc.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            if (!npc.level().hasChunkAt(pos))
                continue;
            var block = npc.level().getBlockState(pos).getBlock();
            if (block instanceof FarmlandBlock || block instanceof CropBlock || block instanceof BeehiveBlock
                    || block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof AbstractFurnaceBlock
                    || npc.level().getFluidState(pos).is(FluidTags.WATER))
                positions.add(pos.immutable());
        }
        positions.sort(Comparator.<BlockPos>comparingInt(p -> {
            var block = npc.level().getBlockState(p).getBlock();
            return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof AbstractFurnaceBlock
                    || block instanceof BeehiveBlock ? 0 : 1;
        }).thenComparingDouble(p -> p.distSqr(origin)));
        for (BlockPos pos : positions.stream().limit(32).toList()) {
            var state = npc.level().getBlockState(pos);
            JsonObject data = new JsonObject();
            data.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            data.addProperty("x", pos.getX());
            data.addProperty("y", pos.getY());
            data.addProperty("z", pos.getZ());
            data.addProperty("mature_crop", NPCFarmGoal.isCrop(pos, (ServerLevel) npc.level()));
            if (state.getBlock() instanceof BeehiveBlock)
                data.addProperty("honey_level", state.getValue(BeehiveBlock.HONEY_LEVEL));
            var be = npc.level().getBlockEntity(pos);
            if (be instanceof AbstractFurnaceBlockEntity furnace) {
                data.addProperty("fuel_count", furnace.getItem(1).getCount());
                data.addProperty("input_count", furnace.getItem(0).getCount());
                data.addProperty("output_count", furnace.getItem(2).getCount());
            }
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) {
                data.addProperty("authorized", npc.getContainerAccess().authorized(npc, player, pos));
                var stand = standPosition(npc, pos);
                data.addProperty("reachable", stand != null);
                if (stand != null) {
                    data.addProperty("walk_x", stand.x);
                    data.addProperty("walk_y", stand.y);
                    data.addProperty("walk_z", stand.z);
                }
            }
            results.add(data);
        }
        return results;
    }
}
