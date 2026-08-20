package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.ExperienceOrbEntityDataModifier;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@SuppressWarnings("resource")
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity implements ExperienceOrbEntityDataModifier {
    @Shadow
    @Nullable
    private Player followingPlayer;

    @Shadow
    public abstract int getValue();

    @Unique
    private NPCSimpleEntity npcTarget;

    public ExperienceOrbMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Unique
    public void moveTowardsTarget(CallbackInfo ci) {
        if (this.npcTarget == null) {
            return;
        }
        Vec3 vec = new Vec3(
                this.npcTarget.getX() - this.getX(),
                this.npcTarget.getY() + this.npcTarget.getEyeHeight() / 2.0 - this.getY(),
                this.npcTarget.getZ() - this.getZ()
        );
        double dist2 = vec.lengthSqr();
        double speedFactor = 1.0 - Math.sqrt(dist2) / 8.0;
        if (speedFactor > 0) {
            this.setDeltaMovement(this.getDeltaMovement().add(vec.normalize().scale(speedFactor * speedFactor * 0.1)));
        }
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickInject(CallbackInfo ci) {
        if (this.npcTarget == null) {
            return;
        }
        if (this.npcTarget.isDeadOrDying()) {
            return;
        }
        if (this.npcTarget.getHealth() <= 0) {
            return;
        }
        double dist2 = this.npcTarget.distanceTo(this);
        if (dist2 < 1.2) {
            int amount = this.getValue();
            amount = this.reverie_dreams$repairItems(this.npcTarget, amount);
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            if (amount > 0) {
                this.npcTarget.addExperience(amount);
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Unique
    private int reverie_dreams$repairItems(NPCSimpleEntity npc, int amount) {
        Optional<EnchantedItemInUse> selected = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, npc, ItemStack::isDamaged);
        Level level = npc.level();
        if (selected.isPresent() && level instanceof ServerLevel serverLevel) {
            ItemStack itemStack = selected.get().itemStack();
            int toRepairFromXpAmount = EnchantmentHelper.modifyDurabilityToRepairFromXp(serverLevel, itemStack, amount);
            int repair = Math.min(toRepairFromXpAmount, itemStack.getDamageValue());
            itemStack.setDamageValue(itemStack.getDamageValue() - repair);
            if (repair > 0) {
                int remaining = amount - repair * amount / toRepairFromXpAmount;
                if (remaining > 0) {
                    return this.reverie_dreams$repairItems(npc, remaining);
                }
            }

            return 0;
        } else {
            return amount;
        }
    }

    @Unique
    public void reverie_dreams$setNPCTarget(NPCSimpleEntity npc) {
        this.npcTarget = npc;
        this.followingPlayer = null;
    }

    @Override
    public NPCSimpleEntity reverie_dreams$getNPCTarget() {
        return this.npcTarget;
    }

    @Inject(method = "followNearbyPlayer", at = @At("HEAD"), cancellable = true)
    public void moveToPlayerTickBefore(CallbackInfo ci) {
        this.moveTowardsTarget(ci);
    }
}
