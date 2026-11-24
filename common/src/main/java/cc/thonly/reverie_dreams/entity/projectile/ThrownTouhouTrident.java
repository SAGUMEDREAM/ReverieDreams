package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.mixin.accessor.ThrownTridentAccessor;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 仿照原版 {@link ThrownTrident} 的投掷逻辑，但使用本模组自己的实体类型。
 * <p>
 * 通过继承原版 {@link ThrownTrident} 保留投掷、拾取、忠诚回返、附魔命中和水中惯性；
 * 这里只补充自定义实体类型、初始化物品、忠诚/发光同步数据，以及 Gungnir 的赤色拖尾。
 */
public class ThrownTouhouTrident extends ThrownTrident {
    /**
     * 枪尖拖出的赤色魔力尘埃：内芯偏亮，光晕偏暗，两层叠出“实心到消散”的层次。
     */
    private static final DustParticleOptions GUNGNIR_TRAIL_CORE = new DustParticleOptions(0xFF4A4A, 0.75F);
    private static final DustParticleOptions GUNGNIR_TRAIL_HAZE = new DustParticleOptions(0x6E0A16, 1.9F);

    public ThrownTouhouTrident(EntityType<? extends ThrownTouhouTrident> type, Level level) {
        super(type, level);
    }

    public ThrownTouhouTrident(Level level, LivingEntity owner, ItemStack stack) {
        super(typeFor(stack), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        this.setPickupItemStack(stack.copy());
        this.initData(stack);
    }

    public ThrownTouhouTrident(Level level, double x, double y, double z, ItemStack stack) {
        super(typeFor(stack), level);
        this.setPos(x, y, z);
        this.setPickupItemStack(stack.copy());
        this.initData(stack);
    }

    private static EntityType<? extends ThrownTouhouTrident> typeFor(ItemStack stack) {
        if (stack.getItem() == RDItems.GUNGNIR.asItem()) {
            return RDEntityTypes.THROWN_GUNGNIR.get();
        }
        return RDEntityTypes.THROWN_NUE_TRIDENT.get();
    }

    private void initData(ItemStack stack) {
        this.entityData.set(ThrownTridentAccessor.reverie_dreams$getLoyaltyId(), this.getLoyaltyFromItem(stack));
        this.entityData.set(ThrownTridentAccessor.reverie_dreams$getFoilId(), stack.hasFoil());
    }

    private byte getLoyaltyFromItem(ItemStack stack) {
        return this.level() instanceof ServerLevel serverLevel
                ? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this), 0, 127)
                : 0;
    }

    public boolean isInFlight() {
        return !this.isInGround();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        if (this.getType() == RDEntityTypes.THROWN_GUNGNIR.get()) {
            return new ItemStack(RDItems.GUNGNIR.asItem());
        }
        if (this.getType() == RDEntityTypes.THROWN_NUE_TRIDENT.get()) {
            return new ItemStack(RDItems.NUE_TRIDENT.asItem());
        }
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.getType() == RDEntityTypes.THROWN_GUNGNIR.get()) {
            this.spawnGungnirTrail(serverLevel);
        }
    }

    /**
     * 沿飞行反方向撒出赤色尘埃，构成 Gungnir 的拖尾。
     * <p>
     * 采样范围（0.7~2.8 格）刻意长于单刻位移，避免高速飞行时拖尾断成一串点；
     * 粒子按“离枪尾越远、扩散越大”分布，形成由实到虚的锥形尾迹。
     */
    private void spawnGungnirTrail(ServerLevel level) {
        if (!this.isInFlight()) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        if (speed < 0.05) {
            return;
        }

        Vec3 dir = motion.scale(1.0 / speed);
        Vec3 origin = this.position();
        // 取一个与飞行方向不平行的轴，构造出垂直于弹道的扩散平面。
        Vec3 axis = Math.abs(dir.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = dir.cross(axis).normalize();
        Vec3 up = side.cross(dir).normalize();

        for (int i = 0; i < 4; i++) {
            double along = 0.7 + this.random.nextDouble() * 2.1;
            double radius = Math.sqrt(this.random.nextDouble()) * (0.05 + along * 0.12);
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            Vec3 point = origin
                    .subtract(dir.scale(along))
                    .add(side.scale(Math.cos(angle) * radius))
                    .add(up.scale(Math.sin(angle) * radius));
            level.sendParticles(GUNGNIR_TRAIL_CORE, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        if (this.tickCount % 2 == 0) {
            Vec3 haze = origin.subtract(dir.scale(1.0 + this.random.nextDouble() * 1.8));
            level.sendParticles(GUNGNIR_TRAIL_HAZE, haze.x, haze.y, haze.z, 1, 0.12, 0.12, 0.12, 0.0);
        }
    }
}
