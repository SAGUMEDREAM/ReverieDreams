package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;

public class RangedAttackUtil {
    public static ItemStack getArrowStack(BaseNPCLikeEntity maid) {
        Integer slot = maid.getInventory().findSlot(stack -> BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return maid.getInventory().getItem(slot);
    }

    public static ItemStack getCrossBowAmmoStack(BaseNPCLikeEntity maid) {
        Integer slot = maid.getInventory().findSlot(stack -> stack.getItem() instanceof FireworkRocketItem || BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return maid.getInventory().getItem(slot);
    }

    public static boolean isDanmakuInHand(BaseNPCLikeEntity maid) {
        return maid.getInventory().getMainHand().getItem() instanceof AbstractDanmakuItem;
    }

    public static boolean loadProjectiles(ItemStack crossbow, ItemStack ammo, LivingEntity user) {
        List<ItemStack> list = ProjectileWeaponItem.draw(crossbow, ammo, user);
        if (!list.isEmpty()) {
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
            return true;
        } else {
            return false;
        }
    }
}
