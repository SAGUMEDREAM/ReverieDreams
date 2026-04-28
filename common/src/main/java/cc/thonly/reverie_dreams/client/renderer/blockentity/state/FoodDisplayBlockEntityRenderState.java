package cc.thonly.reverie_dreams.client.renderer.blockentity.state;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class FoodDisplayBlockEntityRenderState extends BlockEntityRenderState {
    public ItemStackWrapper itemStackWrapper = ItemStackWrapper.EMPTY;
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float yaw;
    public double bobOffset;
}
