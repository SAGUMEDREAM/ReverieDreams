package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.entity.projectile.ThrownTouhouTrident;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 东方武器专属投掷物的显示层。
 * <p>
 * 实体本身是独立的 {@link ThrownTouhouTrident}，不会覆盖原版三叉戟实体或渲染器。
 * 当物品栈意外不是本模组武器时，委托原版 {@link ThrownTridentRenderer} 兜底。
 */
public class ThrownNueTridentRenderer extends EntityRenderer<ThrownTouhouTrident, ThrownNueTridentRenderer.State> {
    private final ThrownTridentRenderer vanillaRenderer;
    private final ItemModelResolver itemModelResolver;

    public ThrownNueTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.vanillaRenderer = new ThrownTridentRenderer(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ThrownTouhouTrident entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.isFoil = entity.isFoil();

        ItemStack stack = RDItems.NUE_TRIDENT.asItem().getDefaultInstance();
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.NONE, entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        // Gungnir 翻转半圈后再向下偏转 45°，对齐其物品模型中的枪尖方向。
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        // 此时本地 +X 是水平飞行方向，结合俯仰沿完整飞行方向后移。
        float pitch = state.xRot * ((float) Math.PI / 180.0F);
        poseStack.translate(-0.5F * Math.cos(pitch), -0.5F * Math.sin(pitch), 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F + 180.0F));
        poseStack.scale(1.25F, 1.25F, 1.25F);
        // ItemTransform.NO_TRANSFORM 默认会在本地坐标里平移 -0.5；
        // 抵消 Y 轴偏移，把枪尖放到实体原点前方。
        poseStack.translate(0.0F, -0.4F, 0.0F);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();

        // 名称/发光描边等通用实体显示。
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class State extends ThrownTridentRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
    }
}
