package cc.thonly.reverie_dreams.client.renderer.entity;

import cc.thonly.reverie_dreams.client.renderer.danmaku.DanmakuRenderOptions;
import cc.thonly.reverie_dreams.client.renderer.danmaku.OrbSpriteCache;
import cc.thonly.reverie_dreams.client.renderer.entity.state.DanmakuEntityRenderState;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuVisualPosition;
import cc.thonly.reverie_dreams.item.weapon.LunarBulletVisuals;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class DanmakuLikeRenderer extends EntityRenderer<DanmakuEntity, DanmakuEntityRenderState> {
    protected static final float MIN_DISTANCE = 2;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemModelResolver itemModelResolver;
    private final OrbSpriteCache orbSprites = new OrbSpriteCache();

    public DanmakuLikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    protected int getBlockLightLevel(DanmakuEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(DanmakuEntity entity, DanmakuEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        DanmakuVisualPosition visualPosition = entity.getVisualPosition();
        if (visualPosition != null) {
            var from = visualPosition.previous();
            var to = visualPosition.current();
            state.x = Mth.lerp((double) partialTick, from.x, to.x);
            state.y = Mth.lerp((double) partialTick, from.y, to.y);
            state.z = Mth.lerp((double) partialTick, from.z, to.z);
        }
        state.display = false;
        state.itemStack = entity.getItemStack();
        state.properties = entity.getDanmakuProperties();
        state.scale = state.properties.scale() * 0.85f;
        boolean directionalSprite = isType(state, DanmakuTypes.ARROW) || isType(state, DanmakuTypes.OVAL);
        state.alignToFlight = directionalSprite || isType(state, DanmakuTypes.NEEDLE)
                || isType(state, DanmakuTypes.WATER_DROP)
                || isType(state, DanmakuTypes.BACILLUS);
        state.flight = directionalSprite ? entity.currentDirection() : Vec3.ZERO;
        state.yinYang = isType(state, DanmakuTypes.YIN_YANG);
        state.tile = state.properties.tile() || state.alignToFlight || state.yinYang
                || isType(state, DanmakuTypes.HEART) || isType(state, DanmakuTypes.SAKURA_PETAL);
        state.xRot = entity.getXRot();
        state.yRot = entity.getYRot();
        state.xRotO = entity.xRotO;
        state.yRotO = entity.yRotO;
        state.roll = entity.getRoll();
        state.rollO = entity.getRollO();
        state.partialTick = partialTick;
        if (!LunarBulletVisuals.isBullet(state.itemStack) && this.entityRenderDispatcher.camera != null && this.entityRenderDispatcher.camera.entity().distanceToSqr(entity) < MIN_DISTANCE) {
            return;
        }
        if (!state.itemStack.isEmpty()) {
            state.display = true;
            if (state.tile && state.scale > 0 && entity.getType() == RDEntityTypes.DANMAKU.value()
                    && DanmakuRenderOptions.batchOrbs() && !state.alignToFlight && !state.yinYang
                    && !isType(state, DanmakuTypes.STAR)) {
                state.orbSprite = this.orbSprites.resolve(state.itemStack, this.itemModelResolver);
                if (state.orbSprite != null) {
                    return;
                }
            }
            state.itemRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForNonLiving(state.itemRenderState, state.itemStack,
                    LunarBulletVisuals.isBullet(state.itemStack) ? ItemDisplayContext.NONE : state.tile ? ItemDisplayContext.FIXED : ItemDisplayContext.GUI, entity);
        }
    }

    @Override
    public void submit(DanmakuEntityRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState
    ) {
        if (!state.display) {
            return;
        }
        if (state.itemStack.isEmpty()) {
            return;
        }
        if (state.orbFrame != null && state.outlineColor == 0) {
            state.orbFrame.append(state, matrices, nodeCollector, cameraRenderState);
            return;
        }
        if (state.itemRenderState == null) {
            // The static model was eligible, but this render pass did not prepare a batch.
            state.itemRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(state.itemRenderState, state.itemStack,
                    ItemDisplayContext.FIXED, null, null, 0);
        }
        if (LunarBulletVisuals.isBullet(state.itemStack)) {
            matrices.pushPose();
            matrices.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(state.partialTick, state.yRotO, state.yRot)));
            matrices.mulPose(Axis.XP.rotationDegrees(Mth.rotLerp(state.partialTick, state.xRotO, state.xRot)));
            matrices.scale(state.scale, state.scale, state.scale);
        } else if (state.tile) {
            this.modifyPoseStack(state, matrices);
        } else {
            this.modifyPoseStackFlat(state, matrices);
        }
        state.itemRenderState.submit(matrices, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    public void modifyPoseStack(DanmakuEntityRenderState state, PoseStack matrices) {
        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0.0f, 0.25f, 0.0f);
        if (this.entityRenderDispatcher.camera != null) {
            var cameraRotation = this.entityRenderDispatcher.camera.rotation();
            matrices.mulPose(cameraRotation);
            if (state.alignToFlight) {
                var flight = state.flight.lengthSqr() > 1.0E-8D ? state.flight
                        : DanmakuMath.direction(Mth.rotLerp(state.partialTick, state.xRotO, state.xRot),
                        Mth.rotLerp(state.partialTick, state.yRotO, state.yRot));
                var screen = cameraRotation.transformInverse(new Vector3f((float) flight.x, (float) flight.y, (float) flight.z));
                // An end-on shot has no stable screen heading; keep its upright silhouette.
                if (screen.x * screen.x + screen.y * screen.y > 0.0025f) {
                    matrices.mulPose(Axis.ZP.rotation((float) Math.atan2(-screen.x, screen.y)));
                }
            }
        }
        matrices.mulPose(Axis.YP.rotationDegrees(180.0f));
        if (state.yinYang) {
            matrices.mulPose(Axis.ZP.rotationDegrees((state.ageInTicks * 7.5f) % 360f));
        } else if (isType(state, DanmakuTypes.STAR)) {
            // After the camera billboard transform, the local Z axis is the
            // screen normal; this produces a true 2D spin like a Touhou star.
            matrices.mulPose(Axis.ZP.rotation(Mth.rotLerpRad(
                    state.partialTick, state.rollO, state.roll
            )));
        }
    }

    private static boolean isType(DanmakuEntityRenderState state, DanmakuType type) {
        return state.itemStack.is(type.getItemHolder().asItem()) || type.getId().equals(state.properties.templateId());
    }

    public void modifyPoseStackFlat(DanmakuEntityRenderState state, PoseStack matrices) {
        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(state.partialTick, state.xRotO, state.xRot)));
        matrices.mulPose(Axis.XP.rotationDegrees(90.0f));
        matrices.mulPose(Axis.ZP.rotationDegrees(-90.0f));
    }

    @Override
    public DanmakuEntityRenderState createRenderState() {
        return new DanmakuEntityRenderState();
    }
}
