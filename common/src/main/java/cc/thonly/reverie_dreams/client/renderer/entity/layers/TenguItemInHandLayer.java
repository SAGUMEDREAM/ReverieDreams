package cc.thonly.reverie_dreams.client.renderer.entity.layers;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.entity.Tengu;
import cc.thonly.reverie_dreams.entity.variant.TenguEquipment;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public final class TenguItemInHandLayer<R extends EntityRenderState & GeoRenderState> extends ItemInHandGeoLayer<Tengu, Void, R> {
    private record Shields(boolean right, boolean left, boolean rightUsing, boolean leftUsing) {
    }

    private static final DataTicket<Shields> SHIELDS = DataTicket.create(ReverieDreams.MOD_ID + ":tengu_shields", Shields.class);

    public TenguItemInHandLayer(EntityRendererProvider.Context context, GeoRenderer<Tengu, Void, R> renderer) {
        super(context, renderer, "right_hand", "left_hand");
    }

    @Override
    public void addRenderData(Tengu entity, Void related, R state, float partialTick) {
        state.addGeckolibData(SHIELDS, new Shields(TenguEquipment.customShield(TenguEquipment.stackInArm(entity, HumanoidArm.RIGHT)),
                TenguEquipment.customShield(TenguEquipment.stackInArm(entity, HumanoidArm.LEFT)),
                TenguEquipment.usingShield(entity, HumanoidArm.RIGHT), TenguEquipment.usingShield(entity, HumanoidArm.LEFT)));
        super.addRenderData(entity, related, state, partialTick);
    }

    @Override
    protected void submitItemStackRender(PoseStack pose, GeoBone bone, ItemStackRenderState item, ItemDisplayContext context,
                                         R state, SubmitNodeCollector collector, int light) {
        boolean right = bone.name().equals(this.rightHandBone);
        var shields = state.getGeckolibData(SHIELDS);
        if (!(right ? shields.right() : shields.left())) {
            super.submitItemStackRender(pose, bone, item, context, state, collector, light);
            return;
        }
        pose.pushPose();
        pose.mulPose(TenguEquipment.shieldHandTransform(new Matrix4f(), right ? HumanoidArm.RIGHT : HumanoidArm.LEFT, right ? shields.rightUsing() : shields.leftUsing()));
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
