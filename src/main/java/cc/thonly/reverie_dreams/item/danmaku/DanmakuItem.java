package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class DanmakuItem extends AbstractDanmakuItem {
    protected DanmakuType type;

    public DanmakuItem(Settings settings) {
        super(settings);
    }

    public void type(DanmakuType type) {
        if (this.type == null) {
            this.type = type;
        }
    }

    public DanmakuType type() {
        return this.type;
    }

    @Override
    public void shoot(ServerWorld serverWorld, LivingEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand).copy();
        String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());
        DanmakuTrajectory danmakuTrajectory = RegistryManager.DANMAKU_TRAJECTORY.get(Identifier.of(templateType));
        Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, 1.0f);
        Float acceleration = stack.getOrDefault(ModDataComponentTypes.Danmaku.ACCELERATION, 0.0f);

        danmakuTrajectory.run(serverWorld, user, stack, user.getX(), user.getY(), user.getZ(), user.getPitch(), user.getYaw(), speed, acceleration, 0f, 1.5f, this);

    }
}
