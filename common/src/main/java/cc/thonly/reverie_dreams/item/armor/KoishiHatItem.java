package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.KoishiHatArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.util.entity.EquipmentSlotHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.Nullable;

public class KoishiHatItem extends ArmorItem {

    public KoishiHatItem(Properties settings) {
        super(KoishiHatArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (EquipmentSlotHelper.isArmorSlot(slot) && entity instanceof LivingEntity livingEntity) {
            if (entity.getDeltaMovement().lengthSqr() <= 1 && entity.isShiftKeyDown()) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false, false));
            }
        }
    }

}