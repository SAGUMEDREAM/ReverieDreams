package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.item.prop.ExorcismPaperItem;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 投掷出去的退魔符。
 * <p>
 * 命中目标时对主目标施加强化退魔效果，并以落点为中心净化小范围内的亡灵/妖怪/妖精；
 * 命中方块时只进行范围净化。投掷物不会伤害玩家和已驯服的伙伴。
 */
public class ThrownExorcismPaper extends ThrowableItemProjectile {
    public ThrownExorcismPaper(EntityType<? extends ThrownExorcismPaper> type, Level level) {
        super(type, level);
    }

    public ThrownExorcismPaper(Level level, LivingEntity owner, ItemStack stack) {
        super(RDEntityTypes.THROWN_EXORCISM_PAPER.get(), owner, level, stack);
    }

    @Override
    public Item getDefaultItem() {
        return RDItems.EXORCISM_PAPER.asItem();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 100) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();
        if (hitEntity != owner) {
            if (hitEntity instanceof LivingEntity living) {
                ExorcismPaperItem.applyExorcism(serverLevel, living, owner, true);
                ExorcismPaperItem.purifyArea(serverLevel, living.position(), owner, living);
            } else {
                ExorcismPaperItem.purifyArea(serverLevel, result.getLocation(), owner);
            }
        }

        serverLevel.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 location = result.getLocation();
        ExorcismPaperItem.purifyArea(serverLevel, location, this.getOwner());
        serverLevel.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(
                        ParticleTypes.ENCHANT,
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2,
                        (this.random.nextDouble() - 0.5) * 0.2
                );
            }
        }
    }

}
