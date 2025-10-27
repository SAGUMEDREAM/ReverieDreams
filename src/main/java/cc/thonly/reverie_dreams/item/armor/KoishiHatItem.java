package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.KoishiHatArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.world.World;

public class KoishiHatItem extends ArmorItem {

    public KoishiHatItem(Settings settings) {
        super(KoishiHatArmorMaterial.INSTANCE, EquipmentType.HELMET, settings);
        ArmorAttributeManager.register(this::onAccept, this);
    }

    void onAccept(LivingEntity entity, ItemStack itemStack) {
        if (entity.getVelocity().lengthSquared() <= 1 && entity.isSneaking()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, false, false, false));
        }
    }

    public static synchronized void onUseTick(World world, LivingEntity user, ItemStack stack) {

    }
}