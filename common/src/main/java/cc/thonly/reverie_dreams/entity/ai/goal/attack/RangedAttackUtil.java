package cc.thonly.reverie_dreams.entity.ai.goal.attack;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.List;

public class RangedAttackUtil {
    public static ItemStack getArrowStack(BaseNPCLikeEntity npc) {
        Integer slot = npc.getInventory().findSlot(stack -> BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return npc.getInventory().getItem(slot);
    }

    public static ItemStack getCrossBowAmmoStack(BaseNPCLikeEntity npc) {
        Integer slot = npc.getInventory().findSlot(stack -> stack.getItem() instanceof FireworkRocketItem || BaseNPCLikeEntity.ARROW_ITEMS.contains(stack.getItem()));
        if (slot == null) return null;
        return npc.getInventory().getItem(slot);
    }

    public static boolean isDanmakuInHand(BaseNPCLikeEntity npc) {
        return npc.getInventory().getMainHand().getItem() instanceof AbstractDanmakuItem;
    }

    public static boolean isWeaponOfTheMoonInHand(BaseNPCLikeEntity npc) {
//        System.out.println("is weapon = %s".formatted(npc.getInventory().getMainHand().getItem() instanceof WeaponOfTheMoon));
        return npc.getInventory().getMainHand().getItem() instanceof WeaponOfTheMoon;
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
