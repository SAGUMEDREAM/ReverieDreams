package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class NPCAutoPickItemGoal extends Goal {

    private final NPCRoleEntity role;
    private int ticks = 20;

    public NPCAutoPickItemGoal(NPCRoleEntity role) {
        this.role = role;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks--;
        if (this.ticks <= 0) {
            this.ticks = 20;
            World world = this.role.getWorld();
            BlockPos blockPos = this.role.getBlockPos();
            double radius = 4.5;
            Box box = new Box(
                    blockPos.getX() - radius, blockPos.getY() - radius, blockPos.getZ() - radius,
                    blockPos.getX() + radius, blockPos.getY() + radius, blockPos.getZ() + radius
            );
            List<ItemEntity> nearbyItems = world.getEntitiesByClass(ItemEntity.class, box, entity -> !entity.cannotPickup());
            if (!nearbyItems.isEmpty()) {
                ItemEntity target = nearbyItems.getFirst();
                this.role.getNavigation().startMovingTo(target, 1.2);
            }
        }
    }

    @Override
    public boolean canStart() {
        return this.role.isAutoPick();
    }
}
