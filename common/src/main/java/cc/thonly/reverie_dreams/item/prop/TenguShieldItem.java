package cc.thonly.reverie_dreams.item.prop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

@Setter
@Getter
@ToString
public class TenguShieldItem extends ShieldItem {
    public TenguShieldItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}
