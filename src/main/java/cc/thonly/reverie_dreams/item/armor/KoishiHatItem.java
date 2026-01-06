package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.KoishiHatArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.util.EquipmentSlotUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class KoishiHatItem extends ArmorItem {

    public KoishiHatItem(Properties settings) {
        super(KoishiHatArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        super.inventoryTick(itemStack, level, entity, i, bl);
        if (entity instanceof LivingEntity livingEntity && itemStack.equals(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
            if (entity.getDeltaMovement().lengthSqr() <= 1 && entity.isShiftKeyDown()) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false, false));
            }
        }
    }
}