package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.ServerAvatarState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;

public class BaseNPCLikeEntityRenderer<NPCEntity extends BaseNPCLikeEntity> extends LivingEntityRenderer<NPCEntity, AvatarRenderState, PlayerModel> {

    public BaseNPCLikeEntityRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(context, new PlayerModel(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
        this.addLayer(
                new HumanoidArmorLayer<>(
                        this,
                        ArmorModelSet.bake(
                                slim ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR,
                                context.getModelSet(),
                                p_477740_ -> new PlayerModel(p_477740_, slim)
                        ),
                        context.getEquipmentRenderer()
                )
        );
        this.addLayer(new PlayerItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this, context));
        this.addLayer(new Deadmau5EarsLayer(this, context.getModelSet()));
        this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ParrotOnShoulderLayer(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this, context));
    }

    @Override
    protected boolean shouldRenderLayers(AvatarRenderState state) {
        return !state.isSpectator;
    }

    public Vec3 getRenderOffset(AvatarRenderState state) {
        Vec3 vec3 = super.getRenderOffset(state);
        return state.isCrouching ? vec3.add(0.0, state.scale * -2.0F / 16.0, 0.0) : vec3;
    }

    private static HumanoidModel.ArmPose getArmPose(BaseNPCLikeEntity avatar, HumanoidArm arm) {
        ItemStack itemstack = avatar.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemstack1 = avatar.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(avatar, itemstack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(avatar, itemstack1, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded()) {
            humanoidmodel$armpose1 = itemstack1.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        return avatar.getMainArm() == arm ? humanoidmodel$armpose : humanoidmodel$armpose1;
    }

    private static HumanoidModel.ArmPose getArmPose(BaseNPCLikeEntity avatar, ItemStack handItem, InteractionHand hand) {
        if (handItem.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else if (!avatar.swinging && handItem.is(Items.CROSSBOW) && CrossbowItem.isCharged(handItem)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        } else {
            if (avatar.getUsedItemHand() == hand && avatar.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation itemuseanimation = handItem.getUseAnimation();
                if (itemuseanimation == ItemUseAnimation.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (itemuseanimation == ItemUseAnimation.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (itemuseanimation == ItemUseAnimation.TRIDENT) {
                    return HumanoidModel.ArmPose.THROW_TRIDENT;
                }

                if (itemuseanimation == ItemUseAnimation.CROSSBOW) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (itemuseanimation == ItemUseAnimation.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (itemuseanimation == ItemUseAnimation.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (itemuseanimation == ItemUseAnimation.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }

                if (itemuseanimation == ItemUseAnimation.SPEAR) {
                    return HumanoidModel.ArmPose.SPEAR;
                }
            }

            SwingAnimation swinganimation = handItem.get(DataComponents.SWING_ANIMATION);
            if (swinganimation != null && swinganimation.type() == SwingAnimationType.STAB && avatar.swinging) {
                return HumanoidModel.ArmPose.SPEAR;
            } else {
                return handItem.is(ItemTags.SPEARS) ? HumanoidModel.ArmPose.SPEAR : HumanoidModel.ArmPose.ITEM;
            }
        }
    }

    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    protected void scale(AvatarRenderState p_447098_, PoseStack p_445727_) {
        float f = 0.9375F;
        p_445727_.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public void submit(AvatarRenderState p_433493_, PoseStack p_434615_, SubmitNodeCollector p_433768_, CameraRenderState p_450931_) {
        super.submit(p_433493_, p_434615_, p_433768_, p_450931_);
    }

    protected void submitNameTag(AvatarRenderState state,
                                 PoseStack poseStack,
                                 SubmitNodeCollector submitCollector,
                                 CameraRenderState cameraRenderState) {
        poseStack.pushPose();
//        int i = state.showExtraEars ? -10 : 0;
//        if (state.scoreText != null) {
//            submitCollector.submitNameTag(
//                    poseStack,
//                    state.nameTagAttachment,
//                    i,
//                    state.scoreText,
//                    !state.isDiscrete,
//                    state.lightCoords,
//                    state.distanceToCameraSq,
//                    cameraRenderState
//            );
//            poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
//        }

//        if (state.nameTag != null) {
//            submitCollector.submitNameTag(
//                    poseStack,
//                    state.nameTagAttachment,
//                    i,
//                    state.nameTag,
//                    !state.isDiscrete,
//                    state.lightCoords,
//                    state.distanceToCameraSq,
//                    cameraRenderState
//            );
//        }
        poseStack.popPose();
    }

    public AvatarRenderState createRenderState() {
        return new NPCAvatarRenderState();
    }

    public void extractRenderState(NPCEntity entity, AvatarRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);
        SkinType skinType = entity.getSkinType();
        ClientAsset.Texture texture = new ClientAsset.ResourceTexture(skinType.getTexture());
        state.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);
        state.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
        state.skin = new PlayerSkin(texture, null, null, skinType.isSlim() ? PlayerModelType.SLIM : PlayerModelType.WIDE, false);
        state.arrowCount = entity.getArrowCount();
        state.stingerCount = entity.getStingerCount();
        state.isSpectator = entity.isSpectator();
        state.showHat = entity.isModelPartShown(PlayerModelPart.HAT);
        state.showJacket = entity.isModelPartShown(PlayerModelPart.JACKET);
        state.showLeftPants = entity.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        state.showRightPants = entity.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        state.showLeftSleeve = entity.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        state.showRightSleeve = entity.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        state.showCape = entity.isModelPartShown(PlayerModelPart.CAPE);
        state.nameTagAttachment = null;
        this.extractFlightData(entity, state, partialTick);
        this.extractCapeState(entity, state, partialTick);
        if (state.distanceToCameraSq < 100.0) {
            state.scoreText = entity.getDisplayName();
        } else {
            state.scoreText = null;
        }

        state.parrotOnLeftShoulder = entity.getParrotVariantOnShoulder(true);
        state.parrotOnRightShoulder = entity.getParrotVariantOnShoulder(false);
        state.id = entity.getId();
        state.showExtraEars = entity.showExtraEars();
        state.heldOnHead.clear();
        if (state.isUsingItem) {
            ItemStack itemstack = entity.getItemInHand(state.useItemHand);
            if (itemstack.is(Items.SPYGLASS)) {
                this.itemModelResolver.updateForLiving(state.heldOnHead, itemstack, ItemDisplayContext.HEAD, entity);
            }
        }
    }

    protected boolean shouldShowName(NPCEntity entity, double distanceToCameraSq) {
        return entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    private void extractFlightData(NPCEntity entity, AvatarRenderState reusedState, float partialTick) {
        reusedState.fallFlyingTimeInTicks = entity.getFallFlyingTicks() + partialTick;
        Vec3 vec3 = entity.getViewVector(partialTick);
        Vec3 vec31 = entity.avatarState().deltaMovementOnPreviousTick().lerp(entity.getDeltaMovement(), partialTick);
        if (vec31.horizontalDistanceSqr() > 1.0E-5F && vec3.horizontalDistanceSqr() > 1.0E-5F) {
            reusedState.shouldApplyFlyingYRot = true;
            double d0 = vec31.horizontal().normalize().dot(vec3.horizontal().normalize());
            double d1 = vec31.x * vec3.z - vec31.z * vec3.x;
            reusedState.flyingYRot = (float) (Math.signum(d1) * Math.acos(Math.min(1.0, Math.abs(d0))));
        } else {
            reusedState.shouldApplyFlyingYRot = false;
            reusedState.flyingYRot = 0.0F;
        }
    }

    private void extractCapeState(NPCEntity entity, AvatarRenderState renderState, float partialTick) {
        ServerAvatarState serverAvatarState = entity.avatarState();
        double d0 = serverAvatarState.getInterpolatedCloakX(partialTick) - Mth.lerp((double) partialTick, entity.xo, entity.getX());
        double d1 = serverAvatarState.getInterpolatedCloakY(partialTick) - Mth.lerp((double) partialTick, entity.yo, entity.getY());
        double d2 = serverAvatarState.getInterpolatedCloakZ(partialTick) - Mth.lerp((double) partialTick, entity.zo, entity.getZ());
        float f = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        double d3 = Mth.sin(f * (float) (Math.PI / 180.0));
        double d4 = -Mth.cos(f * (float) (Math.PI / 180.0));
        renderState.capeFlap = (float) d1 * 10.0F;
        renderState.capeFlap = Mth.clamp(renderState.capeFlap, -6.0F, 32.0F);
        renderState.capeLean = (float) (d0 * d3 + d2 * d4) * 100.0F;
        renderState.capeLean = renderState.capeLean * (1.0F - renderState.fallFlyingScale());
        renderState.capeLean = Mth.clamp(renderState.capeLean, 0.0F, 150.0F);
        renderState.capeLean2 = (float) (d0 * d4 - d2 * d3) * 100.0F;
        renderState.capeLean2 = Mth.clamp(renderState.capeLean2, -20.0F, 20.0F);
        float f1 = serverAvatarState.getInterpolatedBob(partialTick);
        float f2 = serverAvatarState.getInterpolatedWalkDistance(partialTick);
        renderState.capeFlap = renderState.capeFlap + Mth.sin(f2 * 6.0F) * 32.0F * f1;
    }

    public void renderRightHand(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, boolean renderSleeve) {
        this.renderHand(poseStack, nodeCollector, packedLight, skinTexture, this.model.rightArm, renderSleeve);
    }

    public void renderLeftHand(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, boolean renderSleeve) {
        this.renderHand(poseStack, nodeCollector, packedLight, skinTexture, this.model.leftArm, renderSleeve);
    }

    private void renderHand(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, Identifier skinTexture, ModelPart arm, boolean renderSleeve) {
        PlayerModel playermodel = this.getModel();
        arm.resetPose();
        arm.visible = true;
        playermodel.leftSleeve.visible = renderSleeve;
        playermodel.rightSleeve.visible = renderSleeve;
        playermodel.leftArm.zRot = -0.1F;
        playermodel.rightArm.zRot = 0.1F;
        nodeCollector.submitModelPart(arm, poseStack, RenderTypes.entityTranslucent(skinTexture), packedLight, OverlayTexture.NO_OVERLAY, null);
    }

    protected void setupRotations(AvatarRenderState p_446425_, PoseStack p_446166_, float p_445813_, float p_446015_) {
        float f = p_446425_.swimAmount;
        float f1 = p_446425_.xRot;
        if (p_446425_.isFallFlying) {
            super.setupRotations(p_446425_, p_446166_, p_445813_, p_446015_);
            float f2 = p_446425_.fallFlyingScale();
            if (!p_446425_.isAutoSpinAttack) {
                p_446166_.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0F - f1)));
            }

            if (p_446425_.shouldApplyFlyingYRot) {
                p_446166_.mulPose(Axis.YP.rotation(p_446425_.flyingYRot));
            }
        } else if (f > 0.0F) {
            super.setupRotations(p_446425_, p_446166_, p_445813_, p_446015_);
            float f4 = p_446425_.isInWater ? -90.0F - f1 : -90.0F;
            float f3 = Mth.lerp(f, 0.0F, f4);
            p_446166_.mulPose(Axis.XP.rotationDegrees(f3));
            if (p_446425_.isVisuallySwimming) {
                p_446166_.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(p_446425_, p_446166_, p_445813_, p_446015_);
        }
    }

    public boolean isEntityUpsideDown(NPCEntity entity) {
        if (entity.isModelPartShown(PlayerModelPart.CAPE)) {
            return super.isEntityUpsideDown(entity) || entity.isPlayerUpsideDown();
        } else {
            return false;
        }
    }
}
