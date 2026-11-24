package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.world.item.ItemStack;

public final class LunarBulletVisuals {
    private LunarBulletVisuals() {
    }

    public static boolean isBullet(ItemStack stack) {
        return stack.getItem() instanceof LunarBulletItem;
    }

    public static ItemStack forWeapon(LunarWeaponType type) {
        return switch (type) {
            case SHOTGUN -> RDItems.LUNAR_BULLET_PELLET.createStack();
            case SNIPER -> RDItems.LUNAR_BULLET_SNIPER.createStack();
            default -> RDItems.LUNAR_BULLET_STANDARD.createStack();
        };
    }
}
