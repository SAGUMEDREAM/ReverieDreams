package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.prop.SatoriEye;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ItemUtils {
    public static boolean isArmorItem(ItemStack stack) {
        return stack.get(DataComponents.EQUIPPABLE) != null;
    }

    public static boolean shouldPass(Player player, InteractionHand hand) {
        Level world = player.level();
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            if (itemStack.getItem() instanceof FumoLicenseItem) {
                return true;
            }
            if (itemStack.getItem() instanceof SatoriEye) {
                return true;
            }
            if (itemStack.getItem() == Items.BARREL) {
                return true;
            }
        }
        return false;
    }
}
