package cc.thonly.reverie_dreams.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;

public class BaguaFurnaceRenderState extends EntityRenderState {
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float xRot;
    public float yRot;
    public float bob;
    public float pulse = 1.0F;
    public float age;
    public float beamLength;
    public float beamStrength;
    /**
     * 蓄力进度 0..1，用于蓄力增亮。
     */
    public float chargeProgress;
    public Vec3 lookAngle;
}
