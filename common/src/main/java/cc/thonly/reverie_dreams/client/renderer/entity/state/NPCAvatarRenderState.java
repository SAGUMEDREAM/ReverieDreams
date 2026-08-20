package cc.thonly.reverie_dreams.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class NPCAvatarRenderState extends AvatarRenderState {
    public ItemStackRenderState wingHolderRenderState = new ItemStackRenderState();
    public ResourceKey<Level> dimension = null;
}
