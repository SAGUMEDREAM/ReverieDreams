package cc.thonly.reverie_dreams.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;

public class BaguaFurnaceRenderState extends EntityRenderState {
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float xRot;
    public float yRot;
    public float xRot0;
    public float yRot0;
    public float partialTick;
    public Vec3 lookAngle;
}
