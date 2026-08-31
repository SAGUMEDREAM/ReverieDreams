package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("RedundantIfStatement")
public class ProjectileItemHelper {
    public static boolean isThrowableFood(ItemStack itemStack) {
        if (itemStack.is(RDItemTags.CUISINE)) {
            return true;
        }
        if (itemStack.is(RDItemTags.BEVERAGE)) {
            return true;
        }
        if (itemStack.has(DataComponents.FOOD)) {
            return true;
        }
        if (itemStack.has(DataComponents.POTION_CONTENTS)) {
            return true;
        }
        return false;
    }

    public static boolean isThrowableCuisine(ItemStack itemStack) {
        if (itemStack.is(RDItemTags.CUISINE)) {
            return true;
        }
        if (itemStack.is(RDItemTags.BEVERAGE)) {
            return true;
        }
        return false;
    }

    public static void onFoodHitEntity(ServerLevel level, ItemStack itemStack, ServerPlayer owner, LivingEntity livingEntity) {
        if (livingEntity instanceof NPCSimpleEntity npc && npc.isEnableTamableFeature() && isThrowableCuisine(itemStack)) {
            NPCCustomerContainer container = npc.getCustomerContainer();
            if (container.isCustomerMode()) {
                container.triggerInteraction(owner, owner.getUsedItemHand(), itemStack.copyWithCount(1));
                return;
            }
        }
        ItemStack copy = itemStack.copyWithCount(1);
        copy.finishUsingItem(level, livingEntity);
    }
}
