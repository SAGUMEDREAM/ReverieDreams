package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

@SuppressWarnings("resource")
public class NPCAutoPickItemGoal extends Goal {

    private final NPCSimpleEntity role;
    private int ticks = 20;

    public NPCAutoPickItemGoal(NPCSimpleEntity role) {
        this.role = role;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.role.isAutoPick();
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks--;
        if (this.ticks <= 0) {
            this.ticks = 20;
            Level world = this.role.level();
            BlockPos blockPos = this.role.blockPosition();
            double radius = 4.5;
            AABB box = new AABB(
                    blockPos.getX() - radius, blockPos.getY() - radius, blockPos.getZ() - radius,
                    blockPos.getX() + radius, blockPos.getY() + radius, blockPos.getZ() + radius
            );
            List<ItemEntity> nearbyItems = world.getEntitiesOfClass(ItemEntity.class, box, entity -> !entity.hasPickUpDelay());
            if (!nearbyItems.isEmpty()) {
                ItemEntity target = nearbyItems.getFirst();
                this.role.getNavigation().moveTo(target, 1.2);
            }
        }
    }

    @Override
    public boolean canUse() {
        return this.role.isAutoPick();
    }
}
