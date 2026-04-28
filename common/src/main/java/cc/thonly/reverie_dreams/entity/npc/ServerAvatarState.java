package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.world.phys.Vec3;

public interface ServerAvatarState {
    void tick(Vec3 position, Vec3 deltaMovement);

    void addWalkDistance(float distance);

    Vec3 deltaMovementOnPreviousTick();

    double getInterpolatedCloakX(float partialTick);

    double getInterpolatedCloakY(float partialTick);

    double getInterpolatedCloakZ(float partialTick);

    void updateBob(float bob);

    void resetBob();

    float getInterpolatedBob(float partialTick);

    float getBackwardsInterpolatedWalkDistance(float partialTick);

    float getInterpolatedWalkDistance(float partialTick);
}
