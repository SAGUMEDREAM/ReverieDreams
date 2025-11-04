package cc.thonly.reverie_dreams.danmaku.trajectory;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CustomTrajectory extends DanmakuTrajectory {
    public CustomTrajectory() {
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity livingEntity, ItemStack stack, Double x, Double y, Double z, float pitch, float yaw, float divergence, float offsetDist, IDanmakuItem pThis) {
        SpellcardRenderer renderer = stack.get(ModDataComponentTypes.SPELL_CARD_COMPONENT);
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
