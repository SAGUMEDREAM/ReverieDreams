package cc.thonly.reverie_dreams.polymer.entity.holder;

import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEquipment;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEquipment.Part;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

/** RabbitUnit-only moving item displays; generic humanoids retain their original holder defaults. */
public final class RabbitUnitEquipmentHolder<A extends AnimatedEntity> extends EquippedHumanoidHolder<RabbitUnit, A> {
    private final ItemDisplayElement[][] parts = new ItemDisplayElement[2][2];
    private final ItemStack[][] partStacks = {{ItemStack.EMPTY, ItemStack.EMPTY}, {ItemStack.EMPTY, ItemStack.EMPTY}};

    public RabbitUnitEquipmentHolder(RabbitUnit entity, A animatedEntity, Model model) {
        super(entity, animatedEntity, model);
        for (int arm = 0; arm < 2; arm++) for (int part = 0; part < 2; part++) {
            var element = new ItemDisplayElement();
            element.setItemDisplayContext(ItemDisplayContext.NONE);
            element.setInterpolationDuration(1);
            element.setTeleportDuration(2);
            this.parts[arm][part] = element;
            this.addElement(element);
        }
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl connection,
                                             Consumer<Packet<ClientGamePacketListener>> packets) {
        super.startWatchingExtraPackets(connection, packets);
        var viewer = connection.getPlayer();
        // Spawns are already queued in this bundle. A frozen source may never tick to sample this viewer.
        this.addBoneDataTracker(viewer);
        try {
            for (var bone : this.getBones()) {
                var sample = this.animationComponent.findPose(viewer, bone);
                Pose pose = sample == null ? null : sample.pose();
                if (pose == null) pose = bone.getLastPose(sample == null ? null : sample.owner());
                if (pose == null) pose = bone.getDefaultPose();
                this.updateElement(viewer, bone, pose);
                var tracker = bone.element().dataTrackerOrDefault(viewer);
                var metadata = new LinkedHashMap<Integer, SynchedEntityData.DataValue<?>>();
                // Include the complete carrier/item snapshot as well as changes back to vanilla defaults.
                var changed = tracker.getChangedEntries();
                if (changed != null) for (var entry : changed) metadata.put(entry.id(), entry);
                var dirty = tracker.getDirtyEntries();
                if (dirty != null) for (var entry : dirty) metadata.put(entry.id(), entry);
                if (!metadata.isEmpty()) packets.accept(new ClientboundSetEntityDataPacket(
                        bone.element().getEntityId(), new ArrayList<>(metadata.values())));
            }
        } finally {
            // Global animations update the global tracker; retaining this temporary clone would freeze it.
            this.resetBoneDataTracker(viewer);
        }
        var rate = viewer.level().getServer().tickRateManager();
        if (rate.isFrozen() && !rate.isSteppingForward()) {
            // Vanilla creates a new display's render cache in its first client tick, even while time is stopped.
            // This initializes only the watching client; server simulation and evacuation progress stay frozen.
            packets.accept(new ClientboundTickingStepPacket(1));
        }
    }

    @Override protected ItemStack handRenderStack(ItemStack stack, HumanoidArm arm) {
        return RabbitUnitEquipment.type(stack) == null ? RabbitUnitEquipment.shieldCarrier(this.entity, arm, stack)
                : RabbitUnitEquipment.carrier(stack, Part.BASE);
    }

    @Override protected int handInterpolationDuration() { return 1; }

    @Override protected ItemDisplayContext handDisplayContext(ItemStack stack, HumanoidArm arm) {
        return RabbitUnitEquipment.type(stack) == null ? super.handDisplayContext(stack, arm) : ItemDisplayContext.NONE;
    }

    @Override protected Matrix4f transformHand(Matrix4f transform, ItemStack stack, HumanoidArm arm) {
        var actual = RabbitUnitEquipment.stackInArm(this.entity, arm);
        if (RabbitUnitEquipment.type(actual) != null) {
            return RabbitUnitEquipment.transform(transform, RabbitUnitEquipment.capture(this.entity, 0), arm, Part.BASE);
        }
        if (actual.getItem() instanceof ShieldItem) {
            return RabbitUnitEquipment.shieldHandTransform(transform, arm, RabbitUnitEquipment.blockingShield(this.entity, arm));
        }
        return super.transformHand(transform, actual, arm);
    }

    @Override protected void onTick() {
        super.onTick();
        var frame = RabbitUnitEquipment.capture(this.entity, 0);
        for (var arm : HumanoidArm.values()) {
            int index = arm == HumanoidArm.RIGHT ? 0 : 1;
            var pose = this.attachmentPose(arm == HumanoidArm.RIGHT ? "right_item_locator" : "left_item_locator");
            for (int i = 0; i < 2; i++) {
                Part part = i == 0 ? Part.MAGAZINE : Part.BOLT;
                var element = this.parts[index][i];
                ItemStack stack = frame.type(arm) != null && RabbitUnitEquipment.visible(frame, arm, part)
                        ? RabbitUnitEquipment.carrier(RabbitUnitEquipment.stackInArm(this.entity, arm), part) : ItemStack.EMPTY;
                if (!ItemStack.matches(this.partStacks[index][i], stack)) {
                    this.partStacks[index][i] = stack;
                    element.setItem(stack);
                }
                if (pose == null || stack.isEmpty()) continue;
                element.setOverridePos(this.entity.position());
                element.setTransformation(RabbitUnitEquipment.transform(this.worldModelPose(pose), frame, arm, part));
                element.startInterpolationIfDirty();
            }
        }
    }
}
