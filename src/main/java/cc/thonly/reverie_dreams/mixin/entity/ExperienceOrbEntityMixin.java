package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.inf.IExperienceOrbEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin extends Entity implements IExperienceOrbEntity {
    @Shadow
    @Nullable
    private Player followingPlayer;

    @Shadow
    public abstract int getValue();

    @Unique
    private NPCRoleEntity npcTarget;

    public ExperienceOrbEntityMixin(EntityType<?> type, Level world) {
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
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            this.npcTarget.addExperience(this.getValue());
            this.remove(RemovalReason.DISCARDED);
        }

    }

    @Unique
    public void reverie_dreams$setNPCTarget(NPCRoleEntity npc) {
        this.npcTarget = npc;
        this.followingPlayer = null;
    }

    @Override
    public NPCRoleEntity reverie_dreams$getNPCTarget() {
        return this.npcTarget;
    }

    @Inject(method = "followNearbyPlayer", at = @At("HEAD"), cancellable = true)
    public void moveToPlayerTickBefore(CallbackInfo ci) {
        this.moveTowardsTarget(ci);
    }
}
