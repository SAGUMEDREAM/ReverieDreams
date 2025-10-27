package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;

import java.util.List;

public class RangedAttackUtil {
    public static ItemStack getArrowStack(BaseNPCLikeEntity maid) {
        Integer slot = maid.getInventory().findSlot(stack -> BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return maid.getInventory().getStack(slot);
    }

    public static ItemStack getCrossBowAmmoStack(BaseNPCLikeEntity maid) {
        Integer slot = maid.getInventory().findSlot(stack -> stack.getItem() instanceof FireworkRocketItem || BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return maid.getInventory().getStack(slot);
    }

    public static boolean isDanmakuInHand(BaseNPCLikeEntity maid) {
        return maid.getInventory().getMainHand().getItem() instanceof AbstractDanmakuItem;
    }

    public static boolean loadProjectiles(ItemStack crossbow, ItemStack ammo, LivingEntity user) {
        List<ItemStack> list = RangedWeaponItem.load(crossbow, ammo, user);
        if (!list.isEmpty()) {
            crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(list));
            return true;
        } else {
            return false;
        }
    }
}
