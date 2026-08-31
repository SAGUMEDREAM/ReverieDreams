package cc.thonly.reverie_dreams.client.renderer.blockentity.state;

import cc.thonly.reverie_dreams.item.IngredientStack;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PlateBlockEntityRenderState extends BlockEntityRenderState {
    public IngredientStack ingredientStack = IngredientStack.empty();
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float yaw;
    public double bobOffset;
}
