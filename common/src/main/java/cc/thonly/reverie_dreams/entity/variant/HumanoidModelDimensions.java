package cc.thonly.reverie_dreams.entity.variant;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

/** Physical bodies of the authored 32-unit humanoids; ears, wings and accessories stay decorative. */
public enum HumanoidModelDimensions {
    FAIRY(32 / 16F),
    LUNAR_SOLDIER(34 / 16F);

    private final EntityDimensions dimensions;

    HumanoidModelDimensions(float height) {
        this.dimensions = EntityDimensions.scalable(.6F, height).withEyeHeight(28 / 16F);
    }

    public EntityDimensions dimensions() { return this.dimensions; }

    public <T extends Entity> EntityType.Builder<T> applyTo(EntityType.Builder<T> builder) {
        return builder.sized(this.dimensions.width(), this.dimensions.height()).eyeHeight(this.dimensions.eyeHeight());
    }
}
