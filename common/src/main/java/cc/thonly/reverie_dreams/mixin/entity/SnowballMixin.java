package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.base.FakePlayer;
import cc.thonly.reverie_dreams.util.item.ProjectileItemHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("resource")
@Mixin(Snowball.class)
public abstract class SnowballMixin extends ThrowableItemProjectile {
    public SnowballMixin(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

//    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
//    public void reverie_dreams$onFoodHitBlock(HitResult hitResult, CallbackInfo ci) {
//        Level level = this.level();
//
//        ItemStack itemStack = this.getItem();
//        if (!ProjectileItemHelper.isThrowableCuisine(itemStack)) {
//            return;
//        }
//        HitResult.Type type = hitResult.getType();
//        if (type == HitResult.Type.BLOCK) {
//            if (!(level instanceof ServerLevel serverLevel)) {
//                return;
//            }
//            Vec3 location = hitResult.getLocation();
//            ItemEntity entity = new ItemEntity(serverLevel, location.x(), location.y(), location.z(), itemStack);
//            serverLevel.addFreshEntity(entity);
//            this.discard();
//            ci.cancel();
//        }
//    }
//
//    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
//    public void reverie_dreams$onFoodHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
//        Level level = this.level();
//        ItemStack itemStack = this.getItem();
//        if (!ProjectileItemHelper.isThrowableCuisine(itemStack)) {
//            return;
//        }
//        Entity entity = hitResult.getEntity();
//        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity livingEntity) {
//            Entity owner = this.getOwner();
//            ServerPlayer player = owner instanceof ServerPlayer serverPlayer ? serverPlayer : FakePlayer.get(serverLevel);
//            ProjectileItemHelper.onFoodHitEntity(serverLevel, itemStack, player, livingEntity);
//            level.broadcastEntityEvent(this, (byte) 3);
//            this.discard();
//            ci.cancel();
//        }
//    }
}
