package cc.thonly.reverie_dreams.item.prop;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SpeedFeatherItem extends Item {
    public SpeedFeatherItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (entity instanceof LivingEntity livingEntity && (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)) {
            if (!livingEntity.hasEffect(MobEffects.SPEED)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 0, false, false, true));
            }
        }
    }
}
