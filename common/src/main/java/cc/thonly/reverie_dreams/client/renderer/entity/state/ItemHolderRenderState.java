package cc.thonly.reverie_dreams.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class ItemHolderRenderState extends EntityRenderState {
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public ItemStack itemStack = ItemStack.EMPTY;
    public float xRot;
    public float yRot;
    public float xRotO;
    public float yRotO;
    public float yBodyRot;
}
