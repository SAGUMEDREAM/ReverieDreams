package cc.thonly.reverie_dreams.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class GensokyoAltarBlockEntityRenderState extends BlockEntityRenderState {
    public ItemStackRenderState[] itemStates = new ItemStackRenderState[9];
    public ItemStack[] ingredients = new ItemStack[9];

    public long gameTime;
    public float partialTick;
}