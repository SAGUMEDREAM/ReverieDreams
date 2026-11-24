package cc.thonly.reverie_dreams.client.renderer.entity;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class IGeoEntityRenderer<
        T extends Entity & GeoAnimatable,
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<T, R> {
    public IGeoEntityRenderer(EntityRendererProvider.Context context, EntityType<T> entityType) {
        super(context, entityType);
    }

    public IGeoEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<R> pass, OrderedSubmitNodeCollector collector,
                                  @Nullable RenderType renderType) {
        int outlineColor = pass.renderState().outlineColor;
        if (outlineColor == 0 || renderType == null || pass.model().isMissingno()) {
            super.submitRenderTasks(pass, collector, renderType);
            return;
        }

        if (!renderType.isOutline()) {
            super.submitRenderTasks(pass, collector, renderType);
        }

        // Custom geometry bypasses vanilla's outline-aware model submission. Send the
        // same posed bones to the outline target with the entity's actual team color.
        RenderType outline = RenderTypes.outline(this.getTextureLocation(pass.renderState()));
        collector.submitCustomGeometry(pass.poseStack(), outline, (pose, vertices) -> {
            var stack = pass.poseStack();
            stack.pushPose();
            try {
                stack.last().set(pose);
                pass.renderPosed(() -> pass.model().render(pass, vertices, pass.packedLight(), pass.packedOverlay(), outlineColor));
            } finally {
                stack.popPose();
            }
        });
    }
}
