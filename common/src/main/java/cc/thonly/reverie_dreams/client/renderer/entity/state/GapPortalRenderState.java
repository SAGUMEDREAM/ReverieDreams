package cc.thonly.reverie_dreams.client.renderer.entity.state;

import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class GapPortalRenderState extends EntityRenderState {
    public float age;
    /**
     * Synced lifetime of the rift; the fade-out is measured against it.
     */
    public int lifetime = GapPortalEntity.LIFETIME;
    public float open;
}