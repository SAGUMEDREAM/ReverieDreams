package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.CrownOfTheUnderworldArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.util.entity.EquipmentSlotHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CrownOfTheUnderworldItem extends ArmorItem {

    public CrownOfTheUnderworldItem(Properties settings) {
        super(CrownOfTheUnderworldArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        if (EquipmentSlotHelper.isArmorSlot(slot) && owner instanceof LivingEntity livingEntity) {

        }
    }

    public static boolean hasEquipment(@NotNull LivingEntity livingEntity) {
        return livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof CrownOfTheUnderworldItem;
    }
}
