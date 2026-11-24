package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.misc.CursedDecoyEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.util.Brightness;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Polymer-side doll display. The source entity is a marker, so all visuals for
 * clients without the mod come from this item display.
 */
public class CursedDecoyHolder extends ElementHolder {
    private final CursedDecoyEntity entity;
    private final ItemDisplayElement element;

    public CursedDecoyHolder(CursedDecoyEntity entity, ItemDisplayElement element) {
        this.entity = entity;
        this.element = element;
        this.element.setItem(new ItemStack(RDEntityHolderItems.CURSED_DECOY_DISPLAY.asItem()));
        this.element.setItemDisplayContext(ItemDisplayContext.FIXED);
        this.element.setBrightness(new Brightness(15, 15));
        this.element.setTeleportDuration(2);
        this.element.setInterpolationDuration(2);
        this.element.setScale(new Vector3f(1.0F));
        this.updateTransform();
    }

    @Override
    protected void onTick() {
        super.onTick();
        this.updateTransform();
    }

    private void updateTransform() {
        float age = this.entity.tickCount;
        float pulse = 1.0F + (float) Math.sin(age * 0.25F) * 0.03F;
        Matrix4f transform = new Matrix4f()
                .translate(0.0F, 0.15F, 0.0F)
                .scale(1.25F * pulse);
        this.element.setTransformation(transform);
        this.element.startInterpolationIfDirty();
    }
}