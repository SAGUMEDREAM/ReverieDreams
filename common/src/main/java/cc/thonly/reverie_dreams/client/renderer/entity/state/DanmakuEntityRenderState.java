package cc.thonly.reverie_dreams.client.renderer.entity.state;

import cc.thonly.reverie_dreams.client.renderer.danmaku.OrbRenderBatch;
import cc.thonly.reverie_dreams.client.renderer.danmaku.OrbSpriteCache;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DanmakuEntityRenderState extends EntityRenderState {
    public boolean alignToFlight;
    public Vec3 flight = Vec3.ZERO;
    public boolean yinYang;
    public ItemStackRenderState itemRenderState;
    public OrbSpriteCache.Sprite orbSprite;
    public OrbRenderBatch.Frame orbFrame;
    public ItemStack itemStack = ItemStack.EMPTY;
    public DanmakuProperties properties;
    public float scale = 0.5f;
    public boolean tile = false;
    public float xRot;
    public float yRot;
    public float xRotO;
    public float yRotO;
    public float roll;
    public float rollO;
    public float partialTick;
    public boolean display;
}
