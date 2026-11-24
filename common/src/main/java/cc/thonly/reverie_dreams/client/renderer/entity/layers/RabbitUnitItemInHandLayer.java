package cc.thonly.reverie_dreams.client.renderer.entity.layers;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEquipment;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEquipment.Part;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

/**
 * Keeps GeckoLib's normal equipment layer; lunar gun parts submit independently at the same hand bone.
 */
public final class RabbitUnitItemInHandLayer<R extends EntityRenderState & GeoRenderState>
        extends ItemInHandGeoLayer<RabbitUnit, Void, R> {
    private static final DataTicket<Parts> PARTS = DataTicket.create(ReverieDreams.MOD_ID + ":rabbit_unit_equipment_parts", Parts.class);

    private record Parts(RabbitUnitEquipment.Frame frame, ItemStackRenderState[][] models, boolean rightBlocking,
                         boolean leftBlocking) {
    }

    public RabbitUnitItemInHandLayer(EntityRendererProvider.Context context, GeoRenderer<RabbitUnit, Void, R> renderer) {
        super(context, renderer, "right_hand", "left_hand");
    }

    @Override
    public void addRenderData(RabbitUnit actor, Void related, R state, float partialTick) {
        var frame = RabbitUnitEquipment.capture(actor, partialTick);
        var models = new ItemStackRenderState[2][Part.values().length];
        for (var arm : HumanoidArm.values()) {
            var source = RabbitUnitEquipment.stackInArm(actor, arm);
            if (frame.type(arm) == null)
                continue;
            for (var part : Part.values())
                models[index(arm)][part.ordinal()] = RenderUtil.createRenderStateForItem(
                        RabbitUnitEquipment.carrier(source, part), this.itemModelResolver, ItemDisplayContext.NONE, actor);
        }
        state.addGeckolibData(PARTS, new Parts(frame, models, RabbitUnitEquipment.blockingShield(actor, HumanoidArm.RIGHT),
                RabbitUnitEquipment.blockingShield(actor, HumanoidArm.LEFT)));
        super.addRenderData(actor, related, state, partialTick);
    }

    @Override
    protected RenderData renderDataForHand(String bone, HumanoidArm arm, RabbitUnit actor, R state) {
        var parts = state.getGeckolibData(PARTS);
        if (parts.frame().type(arm) != null)
            return RenderData.item(bone, ItemDisplayContext.NONE, parts.models()[index(arm)][Part.BASE.ordinal()]);
        var ordinary = super.renderDataForHand(bone, arm, actor, state);
        var source = RabbitUnitEquipment.stackInArm(actor, arm);
        var shield = RabbitUnitEquipment.shieldCarrier(actor, arm, source);
        var context = arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        return shield == source ? ordinary : RenderData.item(bone, context, RenderUtil.createRenderStateForItem(
                shield, this.itemModelResolver, context, actor));
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> pass, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        super.addPerBoneRender(pass, consumer);
        var parts = pass.getGeckolibData(PARTS);
        for (var arm : HumanoidArm.values()) {
            if (parts.frame().type(arm) == null)
                continue;
            pass.model().getBone(arm == HumanoidArm.RIGHT ? this.rightHandBone : this.leftHandBone).ifPresent(bone -> {
                for (var part : new Part[]{Part.MAGAZINE, Part.BOLT}) {
                    if (!RabbitUnitEquipment.visible(parts.frame(), arm, part))
                        continue;
                    consumer.accept(bone, (info, hand, collector) -> this.submitPart(info.poseStack(),
                            parts.models()[index(arm)][part.ordinal()], parts.frame(), arm, part,
                            info.renderState(), collector, info.packedLight()));
                }
            });
        }
    }

    @Override
    protected void submitItemStackRender(PoseStack pose, GeoBone bone, ItemStackRenderState item,
                                         ItemDisplayContext context, R state, SubmitNodeCollector collector, int light) {
        var parts = state.getGeckolibData(PARTS);
        var arm = bone.name().equals(this.rightHandBone) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (parts.frame().type(arm) != null)
            this.submitPart(pose, item, parts.frame(), arm, Part.BASE, state, collector, light);
        else {
            pose.pushPose();
            var shieldTicket = arm == parts.frame().mainArm() ? MAINHAND_SHIELD : OFFHAND_SHIELD;
            if (state.getOrDefaultGeckolibData(shieldTicket, false)) {
                pose.mulPose(RabbitUnitEquipment.shieldHandTransform(new Matrix4f(), arm,
                        arm == HumanoidArm.RIGHT ? parts.rightBlocking() : parts.leftBlocking()));
                item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            } else
                super.submitItemStackRender(pose, bone, item, context, state, collector, light);
            pose.popPose();
        }
    }

    private void submitPart(PoseStack pose, ItemStackRenderState model, RabbitUnitEquipment.Frame frame,
                            HumanoidArm arm, Part part, R state, SubmitNodeCollector collector, int light) {
        pose.pushPose();
        pose.mulPose(RabbitUnitEquipment.transform(new Matrix4f(), frame, arm, part));
        model.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }

    private static int index(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? 0 : 1;
    }
}
