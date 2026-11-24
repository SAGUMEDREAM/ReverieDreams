package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkBeam;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Brightness;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Bounded vanilla-client fallback: one furnace and twelve reusable beam layers.
 */
public class BaguaFurnaceHolder extends ElementHolder {
    private static final int SEGMENTS = 6;
    private final BaguaFurnaceEntity entity;
    private final ItemDisplayElement element;
    private final BlockDisplayElement[] core = new BlockDisplayElement[SEGMENTS];
    private final BlockDisplayElement[] aura = new BlockDisplayElement[SEGMENTS];
    private final BlockDisplayElement muzzle;
    private float lastStrength = -1, lastLength = -1, lastYaw = Float.NaN, lastPitch = Float.NaN;

    public BaguaFurnaceHolder(BaguaFurnaceEntity entity) {
        this.entity = entity;
        this.element = new ItemDisplayElement();
        this.element.setItem(RDItems.BAGUA_FURNACE.createStack());
        this.element.setItemDisplayContext(ItemDisplayContext.NONE);
        this.element.setBrightness(new Brightness(15, 15));
        this.element.setTeleportDuration(2);
        this.element.setInterpolationDuration(1);
        this.element.setScale(new Vector3f(1.0F));
        this.addElement(this.element);
        for (int i = 0; i < SEGMENTS; i++) {
            core[i] = new BlockDisplayElement(Blocks.WHITE_CONCRETE.defaultBlockState());
            aura[i] = new BlockDisplayElement(Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
            for (BlockDisplayElement part : new BlockDisplayElement[] {core[i], aura[i]}) {
                part.setBrightness(new Brightness(15, 15));
                part.setInterpolationDuration(1);
                part.setDisplaySize(64, 64);
                part.setTransformation(new Matrix4f().scale(0));
                addElement(part);
            }
        }
        muzzle = new BlockDisplayElement(Blocks.WHITE_STAINED_GLASS.defaultBlockState());
        muzzle.setBrightness(new Brightness(15, 15));
        muzzle.setInterpolationDuration(1);
        addElement(muzzle);
        this.updateTransform();
    }

    @Override
    protected void onTick() {
        super.onTick();
        this.updateTransform();
    }

    private void updateTransform() {
        float age = this.entity.tickCount;
        float pulse = 1.0F + (float) Math.sin(age * 0.3F) * 0.04F;
        Matrix4f transform = new Matrix4f()
                .translate(0, (float) MasterSparkBeam.MOUNT_HEIGHT, 0)
                .rotateY((float) Math.toRadians(-this.entity.getInitYRot()))
                .rotateX((float) Math.toRadians(this.entity.getInitXRot() + 90.0F))
                .scale(2.0F * pulse * (1 + this.entity.getChargeProgress() * 0.08f))
                .translate(0, 0.5f, 0)
                .rotateY((float) Math.PI); // Compensate ItemDisplayRenderer's own 180-degree rotation.
        this.element.setTransformation(transform);
        this.element.startInterpolationIfDirty();

        float strength = entity.getBeamStrength(0), length = entity.getBeamLength();
        float glow = Math.max(strength, entity.getChargeProgress()) * 0.4f;
        muzzle.setTransformation(axis().translate(-glow, -glow, 0).scale(glow * 2, glow * 2, glow * 0.4f));
        muzzle.startInterpolationIfDirty();
        if (strength <= 0 && lastStrength == 0 && lastLength == 0) return;
        if (strength == lastStrength && length == lastLength && entity.getInitYRot() == lastYaw && entity.getInitXRot() == lastPitch) return;
        lastStrength = strength; lastLength = length; lastYaw = entity.getInitYRot(); lastPitch = entity.getInitXRot();
        for (int i = 0; i < SEGMENTS; i++) {
            float from = length * i / SEGMENTS, size = length / SEGMENTS;
            float radius = (float) MasterSparkBeam.radiusAt(from) * strength;
            core[i].setTransformation(part(from, size, radius * 0.52f));
            aura[i].setTransformation(part(from, size, radius));
            core[i].startInterpolationIfDirty();
            aura[i].startInterpolationIfDirty();
        }
    }

    private Matrix4f axis() {
        return new Matrix4f().translate(0, (float) MasterSparkBeam.MOUNT_HEIGHT, 0)
                .rotateY((float) Math.toRadians(-entity.getInitYRot()))
                .rotateX((float) Math.toRadians(entity.getInitXRot()))
                .translate(0, 0, (float) MasterSparkBeam.MUZZLE_OFFSET);
    }

    private Matrix4f part(float from, float length, float radius) {
        if (radius <= 0 || length <= 0) return new Matrix4f().scale(0);
        return axis().translate(-radius, -radius, from).scale(radius * 2, radius * 2, length);
    }
}
