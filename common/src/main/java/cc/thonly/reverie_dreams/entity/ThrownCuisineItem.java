package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.base.FakePlayer;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.util.item.ProjectileItemHelper;
import lombok.Setter;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

@SuppressWarnings({"resource", "deprecation", "OptionalUsedAsFieldOrParameterType"})
public class ThrownCuisineItem extends ThrowableItemProjectile {
    @Setter
    private Optional<Runnable> hitCallback = Optional.empty();

    public ThrownCuisineItem(EntityType<? extends ThrownCuisineItem> type, Level level) {
        super(type, level);
    }

    public ThrownCuisineItem(Level level, LivingEntity mob, ItemStack itemStack) {
        super(EntityType.SNOWBALL, mob, level, itemStack);
    }

    public ThrownCuisineItem(Level level, double x, double y, double z, ItemStack itemStack) {
        super(EntityType.SNOWBALL, x, y, z, level, itemStack);
    }

    @Override
    public Item getDefaultItem() {
        return RDCuisineItems.SEAFOOD_MISO_SOUP.asItem();
    }

    private ParticleOptions getParticle() {
        ItemStack item = this.getItem();
        return item.isEmpty()
                ? ParticleTypes.ITEM_SNOWBALL
                : new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(item));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particle = this.getParticle();

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Level level = this.level();
        ItemStack itemStack = this.getItem();
        Entity entity = hitResult.getEntity();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (ProjectileItemHelper.isThrowableFood(itemStack) && entity instanceof LivingEntity livingEntity) {
            Entity owner = this.getOwner();
            ServerPlayer player = owner instanceof ServerPlayer serverPlayer ? serverPlayer : FakePlayer.get(serverLevel);
            ProjectileItemHelper.onFoodHitEntity(serverLevel, itemStack, player, livingEntity);
            level.broadcastEntityEvent(this, (byte) 3);
            this.hitCallback.ifPresent(Runnable::run);
            this.discard();
        } else {
            entity.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
            return;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Vec3 location = hitResult.getLocation();
        ItemEntity entity = new ItemEntity(serverLevel, location.x(), location.y(), location.z(), itemStack);
        serverLevel.addFreshEntity(entity);
        this.discard();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
