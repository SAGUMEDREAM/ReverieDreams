package cc.thonly.reverie_dreams.entity.variant;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleUnaryOperator;

/** Distance-driven lower-body phase, independent of upper-body animation restarts. */
public final class WalkingLegs {
    public static final String ANIMATION = "animation.walk_legs";
    private double previous;
    private double advance;
    private boolean active;

    public void update(double distance, double scale, boolean grounded, boolean poseAllowsWalking) {
        this.update(distance, scale, grounded, poseAllowsWalking, 25);
    }

    /** Use the authored hip swing, so a short stride does not slide at faster movement speeds. */
    public void update(double distance, double scale, boolean grounded, boolean poseAllowsWalking, double swingDegrees) {
        this.previous = (this.previous + this.advance) % 1;
        this.active = grounded && poseAllowsWalking && Double.isFinite(distance) && distance > .005;
        double stride = 4 * .75 * Math.sin(Math.toRadians(swingDegrees)) * scale;
        this.advance = this.active ? Math.min(distance / Math.max(.1, stride), .5) : 0;
    }

    public boolean active() { return this.active; }
    public double phase(double partialTick) {
        return (this.previous + this.advance * Math.clamp(partialTick, 0, 1)) % 1;
    }

    /** Registered after the full-body controller, so only authored leg tracks take precedence. */
    public static <T extends GeoAnimatable> AnimationController<T> controller(
            BooleanSupplier active, DoubleUnaryOperator phase) {
        return new AnimationController<T>("walking_legs", 0, state -> active.getAsBoolean()
                ? state.setAndContinue(RawAnimation.begin().thenLoop(ANIMATION)) : PlayState.STOP) {
            @Override
            protected boolean checkControllerState(T actor, GeoRenderState state,
                                                    AnimatableManager<T> manager, GeoModel<T> model) {
                boolean result = super.checkControllerState(actor, state, manager, model);
                if (!active.getAsBoolean() || this.timeline == null) return result;
                this.timelineTime = phase.applyAsDouble(state.getPartialTick());
                this.transitionFromPoint = null;
                this.animationPoint = this.timeline.createAnimationPoint(this.timelineTime, null, this.easingOverride);
                return this.isAnimatingBones();
            }
        };
    }
}
