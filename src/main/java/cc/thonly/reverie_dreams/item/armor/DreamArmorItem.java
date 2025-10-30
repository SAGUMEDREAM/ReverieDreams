package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

public class DreamArmorItem extends ArmorItem {
    public DreamArmorItem(ArmorType type, Properties settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
        ArmorAttributeManager.register(this::onAccept, this);
    }

    void onAccept(LivingEntity entity, ItemStack itemStack) {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 0, true, false, true));
    }
}
