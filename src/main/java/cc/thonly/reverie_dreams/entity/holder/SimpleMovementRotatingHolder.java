package cc.thonly.reverie_dreams.entity.holder;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public class SimpleMovementRotatingHolder <T extends LivingEntity & AnimatedEntity> extends LivingEntityHolder<T> {
    private float lastYaw = 0.f;
    private float lastPitch = 0.f;

    public SimpleMovementRotatingHolder(T parent, Model model) {
        super(parent, model);
    }

    @Override
    protected void applyPose(Pose pose, DisplayWrapper display) {
        var translation = new Vector3f(0, -0.1f, 0);
        Matrix4f matrix4f = new Matrix4f().translate(translation);

        Vector3f movement = parent.getMovement().toVector3f();
        if (movement.lengthSquared() > 0.0001f) {
            movement.normalize();
            float movementYaw = (float) Math.atan2(-movement.x, movement.z);
            float movementPitch = (float) Math.asin(movement.y);

            lastPitch = MathHelper.lerpAngleRadians(0.5f, movementPitch, lastPitch);
            lastYaw = MathHelper.lerpAngleRadians(0.5f, movementYaw, lastYaw);

            matrix4f
                    .rotateLocalZ(0)
                    .rotateLocalX(lastPitch)
                    .rotateLocalY(-lastYaw + MathHelper.PI);

            display.element().setTransformation(matrix4f);
            display.element().startInterpolationIfDirty();
        }
    }

    @Override
    public void updateElement(DisplayWrapper display, @Nullable Pose pose) {
        if (pose == null) {
            this.applyPose(display.getLastPose(), display);
        } else {
            this.applyPose(pose, display);
        }
    }

}
