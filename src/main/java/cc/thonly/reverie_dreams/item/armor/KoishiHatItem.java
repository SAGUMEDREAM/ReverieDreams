package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.KoishiHatArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

public class KoishiHatItem extends ArmorItem {

    public KoishiHatItem(Properties settings) {
        super(KoishiHatArmorMaterial.INSTANCE, ArmorType.HELMET, settings);
        ArmorAttributeManager.register(this::onAccept, this);
    }

    void onAccept(LivingEntity entity, ItemStack itemStack) {
        if (entity.getDeltaMovement().lengthSqr() <= 1 && entity.isShiftKeyDown()) {
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false, false));
        }
    }

    public static synchronized void onUseTick(Level world, LivingEntity user, ItemStack stack) {

    }
}