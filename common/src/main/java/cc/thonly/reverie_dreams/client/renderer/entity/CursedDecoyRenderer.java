package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.entity.misc.CursedDecoyEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 只显示人偶物品模型，不显示用于承接怪物仇恨的盔甲架本体。
 * 实体本身不能 invisible，否则 canBeSeenAsEnemy() 为 false，怪物无法把它当目标。
 */
public class CursedDecoyRenderer extends EntityRenderer<CursedDecoyEntity, CursedDecoyRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public CursedDecoyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CursedDecoyEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.itemModelResolver.updateForNonLiving(
                state.item,
                RDItems.CURSED_DECOY_DOLL.createStack(),
                ItemDisplayContext.FIXED,
                entity
        );
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.15, 0.0);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        state.item.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
    }
}