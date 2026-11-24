package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.polymer.entity.YoukaiRabbitImpl;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/** Rabbit equipment follows animated hand locators; no armor displays are created. */
public final class YoukaiRabbitHolder extends OverlayLivingEntityHolder<YoukaiRabbit, YoukaiRabbitImpl> {
    private final ItemDisplayElement right = new ItemDisplayElement();
    private final ItemDisplayElement left = new ItemDisplayElement();
    private ItemStack rightStack = ItemStack.EMPTY;
    private ItemStack leftStack = ItemStack.EMPTY;

    public YoukaiRabbitHolder(YoukaiRabbit entity, YoukaiRabbitImpl adapter, Model model) {
        super(entity, adapter, model);
        right.setItemDisplayContext(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        left.setItemDisplayContext(ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        for (var item : new ItemDisplayElement[]{right, left}) {
            item.setInterpolationDuration(2); item.setTeleportDuration(2); this.addElement(item);
        }
    }

    @Override protected void onTick() {
        this.updateEntityScale(this.scale);
        super.onTick();
        boolean leftHanded = this.entity.getMainArm() == HumanoidArm.LEFT;
        var r = this.entity.getItemInHand(leftHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        var l = this.entity.getItemInHand(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!ItemStack.matches(r, rightStack)) { rightStack = r.copy(); right.setItem(rightStack); }
        if (!ItemStack.matches(l, leftStack)) { leftStack = l.copy(); left.setItem(leftStack); }
        updateHand("right_item_locator", right); updateHand("left_item_locator", left);
    }

    private void updateHand(String name, ItemDisplayElement item) {
        var locator = this.getLocator(name);
        if (locator == null) return;
        var query = this.animationComponent.findPose(null, locator);
        Pose pose = query != null && query.pose() != null ? query.pose() : locator.getLastPose(null);
        if (pose == null) pose = locator.getDefaultPose();
        var transform = new Matrix4f().rotateY(-this.entity.yBodyRot * Mth.DEG_TO_RAD).scale(this.getScale());
        if (this.entity.isDeadOrDying() && this.usesVanillaDeathRotation()) transform.rotateZ(-this.deathAngle * Mth.HALF_PI);
        transform.mul(pose.matrix()).rotateX(-Mth.HALF_PI).translate(0, .125F, -.0625F);
        item.setOverridePos(this.entity.position()); item.setTransformation(transform); item.startInterpolationIfDirty();
    }
}
