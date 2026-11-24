package cc.thonly.reverie_dreams.client.renderer.blockentity.state;

import cc.thonly.reverie_dreams.item.IngredientStack;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;

public class PlateBlockEntityRenderState extends BlockEntityRenderState {
    public IngredientStack ingredientStack = IngredientStack.empty();
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public final BlockModelRenderState cuisineRenderState = new BlockModelRenderState();
    public boolean hasCuisineModel;
    public float yaw;
    public double bobOffset;
}
