package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class DanmakuItem extends AbstractDanmakuItem {
    protected DanmakuType type;

    public DanmakuItem(Properties settings) {
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
    public void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand).copy();
        String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());
        DanmakuTrajectory danmakuTrajectory = RegistryManager.DANMAKU_TRAJECTORY.getValue(ResourceLocation.parse(templateType));
        Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, 1.0f);
        Float acceleration = stack.getOrDefault(ModDataComponentTypes.Danmaku.ACCELERATION, 0.0f);

        danmakuTrajectory.run(serverWorld, user, stack, user.getX(), user.getY(), user.getZ(), user.getXRot(), user.getYRot(), speed, acceleration, 0f, 1.5f, this);

    }
}
