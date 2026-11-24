package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectory;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.resources.Identifier;
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
    protected void shootVolley(ServerLevel level, LivingEntity owner, InteractionHand hand,
                               ItemStack stack, DanmakuProperties properties) {
        if (BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getValue(properties.templateId()) instanceof PlayerTrajectory trajectory) {
            trajectory.fire(level, owner, hand, stack);
        } else {
            super.shootVolley(level, owner, hand, stack, properties);
        }
    }

    @Override
    public void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand).copy();
        DanmakuProperties properties = stack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties == null) {
            return;
        }
        Identifier templateId = properties.templateId();
        DanmakuTrajectory danmakuTrajectory = BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getValue(templateId);
        DanmakuTrajectoryConfig config = stack.getOrDefault(
                RDDataComponentTypes.DANMAKU_TRAJECTORY_CONFIG.value(), DanmakuTrajectoryConfig.EMPTY);

        if (danmakuTrajectory != null) {
            if (danmakuTrajectory instanceof PlayerTrajectory trajectory) {
                trajectory.fire(serverWorld, user, hand, user.getItemInHand(hand));
                return;
            }
            danmakuTrajectory.run(serverWorld,
                    user,
                    stack,
                    user.getX(),
                    user.getEyeY(),
                    user.getZ(),
                    user.getXRot(),
                    user.getYRot(),
                    0.05f,
                    0.25f,
                    this,
                    config);
        }

    }
}
