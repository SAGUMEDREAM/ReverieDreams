package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.util.OreDetector;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 探宝棒生成的矿物透视标记。
 * <p>只在客户端渲染矿石轮廓（{@code OreEspRenderer}），服务端负责到期回收。</p>
 */
public class OreEspEntity extends Display.BlockDisplay {
    /** 标记存在时间（tick）。 */
    public static final int LIFETIME_TICKS = 100;

    private long expireAtGameTime = -1L;

    public OreEspEntity(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    /**
     * 设置标记到期的世界游戏时间。
     */
    public void setExpireAtGameTime(long expireAtGameTime) {
        this.expireAtGameTime = expireAtGameTime;
    }

    public long getExpireAtGameTime() {
        return this.expireAtGameTime;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.expireAtGameTime >= 0L && this.level().getGameTime() >= this.expireAtGameTime) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        if (!OreDetector.isOre(this.level().getBlockState(this.blockPosition()))) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
