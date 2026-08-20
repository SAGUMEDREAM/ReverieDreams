package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemPickContainer {
    private final BaseNPCLikeEntity npc;
    private final RandomSource random;
    private ItemEntity pickTarget;
    private boolean pickingItem;

    public ItemPickContainer(BaseNPCLikeEntity npc, RandomSource random) {
        this.npc = npc;
        this.random = random;
    }

    public void tick() {
        if (!this.pickingItem || this.pickTarget == null) {
            return;
        }

        if (this.pickTarget.isRemoved()) {
            this.pickingItem = false;
            this.pickTarget = null;
            return;
        }

        if (this.npc.distanceToSqr(this.pickTarget) <= 1.5D * 1.5D) {
            // 到达目标，执行拾取
            this.pickingItem = false;
            this.pickTarget = null;

            // 这里就是“任务完成”
            return;
        }

        if (this.npc.getNavigation().isDone()) {
            // 路径结束，但没有到达目标
            this.pickingItem = false;
            this.pickTarget = null;
            return;
        }
    }
}
