package cc.thonly.reverie_dreams.client.renderer.entity.state;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class DanmakuEntityRenderState extends EntityRenderState {
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public ItemStack itemStack = ItemStack.EMPTY;
    public DanmakuProperties properties;
    public float scale = 0.5f;
    public boolean tile = false;
    public float xRot;
    public float yRot;
    public float xRotO;
    public float yRotO;
    public float partialTick;
    public boolean display;
}