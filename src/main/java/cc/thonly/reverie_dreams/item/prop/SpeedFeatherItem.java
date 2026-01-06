package cc.thonly.reverie_dreams.item.prop;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpeedFeatherItem extends Item {
    public SpeedFeatherItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        super.inventoryTick(itemStack, level, entity, i, bl);
        if (entity instanceof LivingEntity livingEntity && (itemStack.equals(livingEntity.getMainHandItem()) || itemStack.equals(livingEntity.getOffhandItem()))) {
            if (!livingEntity.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 0, false, false, true));
            }
        }
    }
}
