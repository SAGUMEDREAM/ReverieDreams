package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.Display;
import net.minecraft.util.Brightness;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;

/**
 * Polymer-side animation for the portal item display. The real client renderer
 * uses a custom geometry layer; vanilla clients get this rotating billboard.
 */
public class GapPortalHolder extends ElementHolder {
    private final GapPortalEntity entity;
    private final ItemDisplayElement element;

    public GapPortalHolder(GapPortalEntity entity, ItemDisplayElement element) {
        this.entity = entity;
        this.element = element;
        this.element.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.element.setBrightness(new Brightness(15, 15));
        this.element.setItemDisplayContext(ItemDisplayContext.FIXED);
        this.element.setTeleportDuration(3);
        this.element.setInterpolationDuration(2);
        this.element.setTransformation(transformation(this.entity.tickCount, this.entity.getLifetime()));
        this.element.setItem(new ItemStack(
                RDEntityHolderItems.GAP_PORTAL_DISPLAY.asItem()
        ));
    }

    @Override
    protected void onTick() {
        super.onTick();
        this.element.setTransformation(transformation(this.entity.tickCount, this.entity.getLifetime()));
        this.element.startInterpolationIfDirty();
    }

    public static Matrix4f transformation(float age, int lifetimeTicks) {
        float lifetime = Math.max(1.0F, lifetimeTicks);
        float fadeIn = Math.min(1.0F, age / 10.0F);
        float fadeOut = Math.min(1.0F, (lifetime - age) / 20.0F);
        float open = Math.max(0.0F, Math.min(fadeIn, fadeOut));
        float spin = age * 1.7F;

        float pulse = 1.0F + (float) Math.sin(age * 0.25F) * 0.06F;
        return new Matrix4f()
                .translate(0.0F, 1.05F, 0.0F)
                .rotateZ((float) Math.toRadians(spin))
                .scale(1.55F * open * pulse, 1.175F * open * pulse, 1.0F);
    }
}
