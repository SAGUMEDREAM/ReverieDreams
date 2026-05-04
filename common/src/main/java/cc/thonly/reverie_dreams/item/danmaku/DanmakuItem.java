package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
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
    public void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand).copy();
        DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
        if (properties == null) {
            return;
        }
        Identifier templateId = properties.templateId();
        DanmakuTrajectory danmakuTrajectory = RegistryImpls.DANMAKU_TRAJECTORY.getValue(templateId);

        if (danmakuTrajectory != null) {
            danmakuTrajectory.run(serverWorld,
                    user,
                    stack,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    user.getXRot(),
                    user.getYRot(),
                    properties.speed(),
                    properties.acceleration(),
                    this);
        }

    }
}
