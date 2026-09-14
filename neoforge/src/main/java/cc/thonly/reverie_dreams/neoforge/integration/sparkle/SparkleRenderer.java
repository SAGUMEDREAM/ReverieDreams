package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.geckolib3.geo.GeoReplacedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class SparkleRenderer extends GeoReplacedEntityRenderer<BaseNPCLikeEntity, NPCAnimatable> {

    public SparkleRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void preRenderCallback(
            BaseNPCLikeEntity entity,
            PoseStack poseStack,
            float partialTick
    ) {
        /*
         * 和你的 BaseNPCLikeEntityRenderer
         * 原本的 0.9375 缩放保持一致。
         */
        poseStack.scale(
                0.9375F,
                0.9375F,
                0.9375F
        );
    }

    @Override
    public boolean shouldShowName(BaseNPCLikeEntity entity) {
        return entity.hasCustomName() && entity == Minecraft.getInstance().crosshairPickEntity;
    }

    @Override
    public Identifier getTextureLocation(
            LivingEntityRenderState state
    ) {
        return MissingTextureAtlasSprite.getLocation();
    }

}