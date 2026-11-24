package cc.thonly.reverie_dreams.mixin.accessor;

import com.mojang.math.Transformation;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccessor {
    /**
     * 覆写 Display 的发光颜色。Display#getTeamColor 会返回该值，
     * 因此实体的发光描边颜色会跟着改变。
     */
    @Invoker("setGlowColorOverride")
    void reverie_dreams$setGlowColorOverride(int color);

    @Invoker("setBillboardConstraints")
    void reverie_dreams$setBillboardConstraints(Display.BillboardConstraints constraints);

    @Invoker("setBrightnessOverride")
    void reverie_dreams$setBrightnessOverride(Brightness brightness);

    @Invoker("setPosRotInterpolationDuration")
    void reverie_dreams$setPosRotInterpolationDuration(int ticks);

    @Invoker("setTransformation")
    void reverie_dreams$setTransformation(Transformation transformation);
}
