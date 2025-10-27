package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.villager.ModVillagerProfessions;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity
        implements InteractionObserver,
        VillagerDataContainer {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract VillagerData getVillagerData();

    @Unique
    public RevengeGoal revengeGoal = new RevengeGoal(this).setGroupRevenge();
    @Unique
    public ActiveTargetGoal<ZombieEntity> activeTargetGoal = new ActiveTargetGoal<>(this, ZombieEntity.class, true);
    @Unique
    private MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.0D, true);;

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        World world = player.getWorld();
        ItemStack itemStack = player.getStackInHand(hand);
        if (!world.isClient() && itemStack.getItem() instanceof FumoLicenseItem) {
            cir.setReturnValue(ActionResult.PASS);
        }
        if (!world.isClient() && itemStack.getItem() == Items.BARREL) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTickTail(CallbackInfo ci) {

    }

//    @Override
//    public boolean tryAttack(ServerWorld world, Entity target) {
//        if (target instanceof LivingEntity living) {
//            living.damage(world, this.getDamageSources().mobAttack(this), 3.0F);
//            return true;
//        }
//        return false;
//    }

}
