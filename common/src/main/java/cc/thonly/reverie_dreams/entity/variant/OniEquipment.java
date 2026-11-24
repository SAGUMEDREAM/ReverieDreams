package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.world.item.ItemStack;

public final class OniEquipment {
    // Item meshes are centered at (8,8,8), the grip at y=4.25, and third-person display adds 2.25.
    public static final float CLUB_GRIP_OFFSET = (8F - 4.25F - 2.25F) / 16F;

    private OniEquipment() {}

    public static boolean isClub(ItemStack stack) {
        return stack.is(RDItems.IRON_BAR.asItem()) || stack.is(RDItems.ONI_IRON_BAR.asItem());
    }
}
