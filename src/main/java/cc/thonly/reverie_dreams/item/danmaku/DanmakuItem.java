package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.core.component.DataComponentType;
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
        DanmakuProperties properties = stack.get(ModDataComponentTypes.DANMAKU_PROPERTIES);
        if (properties == null) {
            return;
        }
        ResourceLocation templateId = properties.getTemplateId();
        DanmakuTrajectory danmakuTrajectory = RegistryManager.DANMAKU_TRAJECTORY.getValue(templateId);

        if (danmakuTrajectory != null) {
            danmakuTrajectory.run(serverWorld,
                    user,
                    stack,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    user.getXRot(),
                    user.getYRot(),
                    properties.getSpeed(),
                    properties.getAcceleration(),
                    this);
        }

    }
}
