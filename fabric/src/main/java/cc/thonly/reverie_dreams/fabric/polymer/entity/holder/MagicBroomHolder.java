package cc.thonly.reverie_dreams.fabric.polymer.entity.holder;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Setter
@Getter
public class MagicBroomHolder extends ElementHolder {
    private ItemDisplayElement element;
    private LivingEntity entity;

    public MagicBroomHolder(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    protected void onTick() {
        super.onTick();
        if(element != null) {
            if (this.entity.isDeadOrDying()) {
                this.element.setScale(new Vector3f(0));
                return;
            }
            Matrix4f transform = new Matrix4f()
                    .translate(0f, -0.5f, 0f)
                    .rotateY((float) Math.toRadians(-entity.getYHeadRot()))
                    .scale(1.0f);

            element.setTransformation(transform);
            element.startInterpolationIfDirty();
        }
    }
}
