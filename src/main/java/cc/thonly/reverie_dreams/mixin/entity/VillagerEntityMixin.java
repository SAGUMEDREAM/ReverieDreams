package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.villager.ModVillagerProfessions;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin extends AbstractVillager
        implements ReputationEventHandler,
        VillagerDataHolder {
    public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract VillagerData getVillagerData();

    @Unique
    public HurtByTargetGoal revengeGoal = new HurtByTargetGoal(this).setAlertOthers();
    @Unique
    public NearestAttackableTargetGoal<Zombie> activeTargetGoal = new NearestAttackableTargetGoal<>(this, Zombie.class, true);
    @Unique
    private MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.0D, true);;

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void interactMob(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = player.level();
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide() && itemStack.getItem() instanceof FumoLicenseItem) {
            cir.setReturnValue(InteractionResult.PASS);
        }
        if (!world.isClientSide() && itemStack.getItem() == Items.BARREL) {
            cir.setReturnValue(InteractionResult.PASS);
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
