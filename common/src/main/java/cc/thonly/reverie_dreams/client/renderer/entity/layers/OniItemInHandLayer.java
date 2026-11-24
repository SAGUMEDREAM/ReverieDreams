package cc.thonly.reverie_dreams.client.renderer.entity.layers;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.entity.Oni;
import cc.thonly.reverie_dreams.entity.variant.OniEquipment;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

public final class OniItemInHandLayer<R extends EntityRenderState & GeoRenderState>
        extends ItemInHandGeoLayer<Oni, Void, R> {
    private static final DataTicket<Boolean> RIGHT_CLUB = DataTicket.create(ReverieDreams.MOD_ID + ":oni_right_club", Boolean.class);
    private static final DataTicket<Boolean> LEFT_CLUB = DataTicket.create(ReverieDreams.MOD_ID + ":oni_left_club", Boolean.class);

    public OniItemInHandLayer(EntityRendererProvider.Context context, GeoRenderer<Oni, Void, R> renderer) {
        super(context, renderer, "right_hand", "left_hand");
    }

    @Override
    public void addRenderData(Oni entity, Void relatedObject, R state, float partialTick) {
        boolean leftHanded = entity.getMainArm() == HumanoidArm.LEFT;
        state.addGeckolibData(RIGHT_CLUB, OniEquipment.isClub(entity.getItemInHand(leftHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)));
        state.addGeckolibData(LEFT_CLUB, OniEquipment.isClub(entity.getItemInHand(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)));
        super.addRenderData(entity, relatedObject, state, partialTick);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState item,
                                         ItemDisplayContext context, R state, SubmitNodeCollector collector, int packedLight) {
        if (!state.getOrDefaultGeckolibData(bone.name().equals("right_hand") ? RIGHT_CLUB : LEFT_CLUB, false)) {
            super.submitItemStackRender(poseStack, bone, item, context, state, collector, packedLight);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0, OniEquipment.CLUB_GRIP_OFFSET, 0);
        item.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
