package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntityImpl;
import cc.thonly.reverie_dreams.interfaces.IExperienceOrbEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity implements IExperienceOrbEntity {
    @Shadow
    @Nullable
    private PlayerEntity target;

    @Shadow
    public abstract int getValue();

    @Unique
    private NPCRoleEntityImpl npcTarget;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    public void moveTowardsTarget(CallbackInfo ci) {
        if (this.npcTarget == null) {
            return;
        }
        Vec3d vec = new Vec3d(
                this.npcTarget.getX() - this.getX(),
                this.npcTarget.getY() + this.npcTarget.getStandingEyeHeight() / 2.0 - this.getY(),
                this.npcTarget.getZ() - this.getZ()
        );
        double dist2 = vec.lengthSquared();
        double speedFactor = 1.0 - Math.sqrt(dist2) / 8.0;
        if (speedFactor > 0) {
            this.setVelocity(this.getVelocity().add(vec.normalize().multiply(speedFactor * speedFactor * 0.1)));
        }
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickInject(CallbackInfo ci) {
        if (this.npcTarget == null) {
            return;
        }
        if (this.npcTarget.isDead()) {
            return;
        }
        if (this.npcTarget.getHealth() <= 0) {
            return;
        }
        double dist2 = this.npcTarget.distanceTo(this);
        if (dist2 < 1.2) {
            this.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            this.npcTarget.addExperience(this.getValue());
            this.remove(RemovalReason.DISCARDED);
        }

    }

    @Unique
    public void setNPCTarget(NPCRoleEntityImpl npc) {
        this.npcTarget = npc;
        this.target = null;
    }

    @Override
    public NPCRoleEntityImpl getNPCTarget() {
        return this.npcTarget;
    }

    @Inject(method = "moveTowardsPlayer", at = @At("HEAD"), cancellable = true)
    public void moveToPlayerTickBefore(CallbackInfo ci) {
        this.moveTowardsTarget(ci);
    }
}
