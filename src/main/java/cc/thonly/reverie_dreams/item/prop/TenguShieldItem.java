package cc.thonly.reverie_dreams.item.prop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;

import java.util.List;
import java.util.Optional;

@Setter
@Getter
@ToString
public class TenguShieldItem extends ShieldItem {
    public TenguShieldItem(Properties settings) {
        super(settings);
    }

    public static Item.Properties createItemSettings() {
        return new Item.Properties()
                .stacksTo(1)
                .durability(600)
                .repairable(ItemTags.IRON_TOOL_MATERIALS)
                .equippableUnswappable(EquipmentSlot.OFFHAND);
    }

}
