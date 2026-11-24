package cc.thonly.reverie_dreams.entity.aquatic;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/** Fish-like steering without the vanilla fish controller's unconditional upward buoyancy. */
final class AquaticBottomMoveControl extends MoveControl {
    private final CuisineAquaticEntity animal;

    AquaticBottomMoveControl(CuisineAquaticEntity animal) { super(animal); this.animal = animal; }

    @Override public void tick() {
        if (this.animal.species().stationary() || !this.animal.isInWater()
                || this.operation != Operation.MOVE_TO || this.animal.getNavigation().isDone()) {
            this.animal.setSpeed(0);
            return;
        }
        double dx = this.wantedX - this.animal.getX(), dy = this.wantedY - this.animal.getY(), dz = this.wantedZ - this.animal.getZ();
        float desired = (float) (this.speedModifier * this.animal.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.animal.setSpeed(Mth.lerp(.125f, this.animal.getSpeed(), desired));
        if (dx * dx + dz * dz > .0001) {
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            this.animal.setYRot(this.rotlerp(this.animal.getYRot(), yaw, 30));
            this.animal.yBodyRot = this.animal.getYRot();
            this.animal.yHeadRot = this.animal.getYRot();
        }
        this.animal.setXRot(0);
        this.animal.setDeltaMovement(this.animal.getDeltaMovement().add(0, Mth.clamp(dy * .04, -.035, .035), 0));
    }
}
