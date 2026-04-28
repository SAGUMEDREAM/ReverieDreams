package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CustomTrajectory extends DanmakuTrajectory {
    public CustomTrajectory() {
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, double x, double y, double z, float xRot, float yRot, float divergence, float offsetDist, IDanmakuItem pThis) {
        SpellcardRenderer renderer = stack.get(RDDataComponents.SPELL_CARD_COMPONENT.value());
        if (renderer == null) {
            return;
        }
        renderer = renderer.copy();
        renderer.setSource(livingEntity);
        renderer.setPosition(new Vec3(x, y, z));
        renderer.setWorld(world);
        SpellcardRenderer.addRenderer(renderer);
    }

}
