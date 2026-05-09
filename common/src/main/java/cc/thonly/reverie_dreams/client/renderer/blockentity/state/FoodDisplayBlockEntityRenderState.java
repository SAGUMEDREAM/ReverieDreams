package cc.thonly.reverie_dreams.client.renderer.blockentity.state;

import cc.thonly.reverie_dreams.item.IngredientStack;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class FoodDisplayBlockEntityRenderState extends BlockEntityRenderState {
    public IngredientStack itemStackWrapper = IngredientStack.empty();
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float yaw;
    public double bobOffset;
}
