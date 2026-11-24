package cc.thonly.reverie_dreams.client.renderer.item;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.client.audio.fumo.FumoAudioClient;
import cc.thonly.reverie_dreams.client.audio.fumo.FumoBeatDetector;
import cc.thonly.reverie_dreams.util.FumoGrowth;
import cc.thonly.reverie_dreams.mixin.client.ItemLayerRenderStateAccessor;
import cc.thonly.reverie_dreams.mixin.client.ItemStackRenderStateAccessor;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public final class FumoHeldItemRenderer {
    private FumoHeldItemRenderer() {
    }

    public static void apply(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context) {
        if (!isHeldFumo(stack, context))
            return;
        scaleLayers(state, FumoGrowth.scale(FumoGrowth.fromItem(stack)), FumoAudioClient.pulse());
    }

    static boolean isHeldFumo(ItemStack stack, ItemDisplayContext context) {
        boolean held = switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    true;
            default -> false;
        };
        return held && !stack.isEmpty() && stack.getItem() instanceof BlockItem item
                && item.getBlock() instanceof BaseFumoBlock fumo && fumo.supportsGrowth();
    }

    static void scaleLayers(ItemStackRenderState state, float base, float pulse) {
        if (base == 1f && pulse == 0f)
            return;
        ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) state;
        ItemStackRenderState.LayerRenderState[] layers = accessor.reverie_dreams$getLayers();
        for (int i = 0; i < accessor.reverie_dreams$getActiveLayerCount(); i++) {
            ItemTransform original = ((ItemLayerRenderStateAccessor) layers[i]).reverie_dreams$getItemTransform();
            layers[i].setItemTransform(scaleTransform(original, base, pulse));
        }
        accessor.reverie_dreams$setCachedModelBoundingBox(null);
    }

    static ItemTransform scaleTransform(ItemTransform original, float base, float pulse) {
        float xz = FumoBeatDetector.horizontalScale(base, pulse);
        float y = FumoBeatDetector.verticalScale(base, pulse);
        // Copy the scale: baked transforms are shared between stacks and both hands.
        // Keep translation outside scaling so the existing grip position stays put.
        return new ItemTransform(original.rotation(), original.translation(), new Vector3f(original.scale()).mul(xz, y, xz));
    }
}
