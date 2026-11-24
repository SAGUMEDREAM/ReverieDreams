package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.Oni;
import cc.thonly.reverie_dreams.entity.variant.OniEquipment;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.wrapper.Locator;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EquippedHumanoidHolder<E extends BaseNPCLikeEntity, A extends AnimatedEntity>
        extends OverlayLivingEntityHolder<E, A> {
    private final ItemDisplayElement rightItem = new ItemDisplayElement();
    private final ItemDisplayElement leftItem = new ItemDisplayElement();
    private final EntityElement<ArmorStand> helmet;
    private ItemStack rightStack = ItemStack.EMPTY;
    private ItemStack leftStack = ItemStack.EMPTY;
    private ItemStack headStack = ItemStack.EMPTY;

    public EquippedHumanoidHolder(E entity, A animatedEntity, Model model) {
        super(entity, animatedEntity, model);
        this.rightItem.setItemDisplayContext(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        this.leftItem.setItemDisplayContext(ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        for (var item : new ItemDisplayElement[]{this.rightItem, this.leftItem}) {
            item.setInterpolationDuration(this.handInterpolationDuration());
            item.setTeleportDuration(2);
            this.addElement(item);
        }
        var stand = new ArmorStand(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setNoGravity(true);
        stand.setNoBasePlate(true);
        stand.setSilent(true);
        this.helmet = new EntityElement<>(stand, (ServerLevel) entity.level());
        this.addElement(this.helmet);
        this.updateEquipment();
        this.onDimensionsUpdated(entity.getDimensions(entity.getPose()));
    }

    @Override
    protected void onTick() {
        this.updateEntityScale(this.scale);
        super.onTick();
        this.updateEquipment();
        this.updateHand("right_item_locator", this.rightItem, this.rightStack, HumanoidArm.RIGHT);
        this.updateHand("left_item_locator", this.leftItem, this.leftStack, HumanoidArm.LEFT);
        this.updateHelmet();
    }

    private void updateEquipment() {
        boolean leftHanded = this.entity.getMainArm() == HumanoidArm.LEFT;
        var right = this.entity.getItemInHand(leftHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        var left = this.entity.getItemInHand(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        var head = this.entity.getItemBySlot(EquipmentSlot.HEAD);
        this.rightItem.setItemDisplayContext(this.handDisplayContext(right, HumanoidArm.RIGHT));
        this.leftItem.setItemDisplayContext(this.handDisplayContext(left, HumanoidArm.LEFT));
        right = this.handRenderStack(right, HumanoidArm.RIGHT);
        left = this.handRenderStack(left, HumanoidArm.LEFT);
        if (!ItemStack.matches(this.rightStack, right)) {
            this.rightStack = right.copy();
            this.rightItem.setItem(this.rightStack);
        }
        if (!ItemStack.matches(this.leftStack, left)) {
            this.leftStack = left.copy();
            this.leftItem.setItem(this.leftStack);
        }
        if (!ItemStack.matches(this.headStack, head)) {
            this.headStack = head.copy();
            this.helmet.entity().setItemSlot(EquipmentSlot.HEAD, this.headStack);
        }
    }

    protected Pose attachmentPose(String name) {
        Locator locator = this.getLocator(name);
        if (locator == null) return null;
        var query = this.animationComponent.findPose(null, locator);
        if (query != null && query.pose() != null) return query.pose();
        var previous = locator.getLastPose(null);
        return previous != null ? previous : locator.getDefaultPose();
    }

    protected Matrix4f worldModelPose(Pose pose) {
        var transform = new Matrix4f().rotateY(-this.entity.yBodyRot * Mth.DEG_TO_RAD).scale(this.getScale());
        if (this.entity.isDeadOrDying() && this.usesVanillaDeathRotation()) {
            transform.rotateZ(-this.deathAngle * Mth.HALF_PI);
        }
        return transform.mul(pose.matrix());
    }

    protected ItemStack handRenderStack(ItemStack stack, HumanoidArm arm) { return stack; }

    protected int handInterpolationDuration() { return 2; }

    protected ItemDisplayContext handDisplayContext(ItemStack stack, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    protected Matrix4f transformHand(Matrix4f transform, ItemStack stack, HumanoidArm arm) {
        if (this.entity instanceof Oni && OniEquipment.isClub(stack)) {
            return transform.translate(0, OniEquipment.CLUB_GRIP_OFFSET, 0);
        }
        return transform.rotateX(-Mth.HALF_PI).translate(0, 0.125F, -0.0625F);
    }

    private void updateHand(String locator, ItemDisplayElement element, ItemStack stack, HumanoidArm arm) {
        Pose pose = this.attachmentPose(locator);
        if (pose == null) return;
        var transform = this.transformHand(this.worldModelPose(pose), stack, arm);
        element.setOverridePos(this.entity.position());
        element.setTransformation(transform);
        element.startInterpolationIfDirty();
    }

    private void updateHelmet() {
        Pose pose = this.attachmentPose("helmet_locator");
        if (pose == null) return;
        Matrix4f transform = this.worldModelPose(pose);
        Vector3f center = transform.getTranslation(new Vector3f());
        Vector3f scale = transform.getScale(new Vector3f());
        float uniformScale = Math.max(0.01F, Math.abs(scale.x));
        var stand = this.helmet.entity();
        stand.getAttribute(Attributes.SCALE).setBaseValue(uniformScale);
        this.helmet.setOverridePos(this.entity.position().add(center.x, center.y - 1.4375 * uniformScale, center.z));
        stand.setYRot(this.entity.yBodyRot);
        stand.yBodyRot = this.entity.yBodyRot;
        stand.setYHeadRot(this.entity.yBodyRot);
        Quaternionf rotation = pose.matrix().getUnnormalizedRotation(new Quaternionf()).normalize();
        if (this.entity.isDeadOrDying() && this.usesVanillaDeathRotation()) {
            rotation.premul(new Quaternionf().rotateZ(-this.deathAngle * Mth.HALF_PI));
        }
        if (!this.entity.isDeadOrDying()) rotation.mul(new Quaternionf().rotateY(-(this.entity.yHeadRot - this.entity.yBodyRot) * Mth.DEG_TO_RAD)
                .rotateX(this.entity.getXRot() * Mth.DEG_TO_RAD));
        Vector3f angles = rotation.getEulerAnglesXYZ(new Vector3f()).mul(Mth.RAD_TO_DEG);
        stand.setHeadPose(new Rotations(angles.x, -angles.y, -angles.z));
    }
}
