package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ServerAvatarStateImpl implements ServerAvatarState {
    private Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    private float walkDist;
    private float walkDistO;
    private double xCloak;
    private double yCloak;
    private double zCloak;
    private double xCloakO;
    private double yCloakO;
    private double zCloakO;
    private float bob;
    private float bobO;

    public void tick(Vec3 position, Vec3 deltaMovement) {
        this.walkDistO = this.walkDist;
        this.deltaMovementOnPreviousTick = deltaMovement;
        this.moveCloak(position);
    }

    public void addWalkDistance(float distance) {
        this.walkDist += distance;
    }

    public Vec3 deltaMovementOnPreviousTick() {
        return this.deltaMovementOnPreviousTick;
    }

    private void moveCloak(Vec3 position) {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d0 = position.x() - this.xCloak;
        double d1 = position.y() - this.yCloak;
        double d2 = position.z() - this.zCloak;
        double d3 = 10.0;
        if (!(d0 > 10.0) && !(d0 < -10.0)) {
            this.xCloak += d0 * 0.25;
        } else {
            this.xCloak = position.x();
            this.xCloakO = this.xCloak;
        }

        if (!(d1 > 10.0) && !(d1 < -10.0)) {
            this.yCloak += d1 * 0.25;
        } else {
            this.yCloak = position.y();
            this.yCloakO = this.yCloak;
        }

        if (!(d2 > 10.0) && !(d2 < -10.0)) {
            this.zCloak += d2 * 0.25;
        } else {
            this.zCloak = position.z();
            this.zCloakO = this.zCloak;
        }
    }

    public double getInterpolatedCloakX(float partialTick) {
        return Mth.lerp((double)partialTick, this.xCloakO, this.xCloak);
    }

    public double getInterpolatedCloakY(float partialTick) {
        return Mth.lerp((double)partialTick, this.yCloakO, this.yCloak);
    }

    public double getInterpolatedCloakZ(float partialTick) {
        return Mth.lerp((double)partialTick, this.zCloakO, this.zCloak);
    }

    public void updateBob(float bob) {
        this.bobO = this.bob;
        this.bob = this.bob + (bob - this.bob) * 0.4F;
    }

    public void resetBob() {
        this.bobO = this.bob;
        this.bob = 0.0F;
    }

    public float getInterpolatedBob(float partialTick) {
        return Mth.lerp(partialTick, this.bobO, this.bob);
    }

    public float getBackwardsInterpolatedWalkDistance(float partialTick) {
        float f = this.walkDist - this.walkDistO;
        return -(this.walkDist + f * partialTick);
    }

    public float getInterpolatedWalkDistance(float partialTick) {
        return Mth.lerp(partialTick, this.walkDistO, this.walkDist);
    }
}
