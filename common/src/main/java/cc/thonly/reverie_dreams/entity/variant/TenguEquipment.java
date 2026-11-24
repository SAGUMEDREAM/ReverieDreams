package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.entity.Tengu;
import cc.thonly.reverie_dreams.item.prop.TenguShieldItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Attachment space for the custom shield, whose handle differs from the vanilla shield. */
public final class TenguEquipment {
    private TenguEquipment() {}

    public static ItemStack stackInArm(Tengu entity, HumanoidArm arm) {
        return entity.getItemInHand(entity.getMainArm()==arm?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND);
    }

    public static boolean customShield(ItemStack stack) { return stack.getItem() instanceof TenguShieldItem; }

    public static boolean usingShield(Tengu entity, HumanoidArm arm) {
        return entity.isUsingItem()&&entity.getUsedItemHand()==(entity.getMainArm()==arm?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND);
    }

    /** The item renderer still applies the actual third-person display; align its handle to the palm first. */
    public static Matrix4f shieldHandTransform(Matrix4f matrix, HumanoidArm arm, boolean using) {
        boolean left=arm==HumanoidArm.LEFT;
        float sign=left?-1:1;
        var display=new Matrix4f().translate(using?-sign*.2f/16:0,using?1.25f/16:1f/16,using?-.25f/16:0)
                .rotateXYZ(using?45*Mth.DEG_TO_RAD:0,sign*(using?-25:-90)*Mth.DEG_TO_RAD,0).scale(.75f);
        // Item coordinates are centered by the renderer. The grip is (8,8,8.5) in the source model.
        var grip=display.transformPosition(new Vector3f(0,0,.5f/16));
        return matrix.rotateX(-Mth.HALF_PI).translate(-grip.x,-grip.y,-.25f/16-grip.z);
    }
}
