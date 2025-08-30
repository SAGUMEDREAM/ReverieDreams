package cc.thonly.reverie_dreams.item.prop;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Optional;

@Setter
@Getter
@ToString
public class TenguShieldItem extends ShieldItem {
    public TenguShieldItem(Settings settings) {
        super(settings);
    }

    public static Item.Settings createItemSettings() {
        return new Item.Settings()
                .maxCount(1)
                .maxDamage(600)
                .repairable(ItemTags.IRON_TOOL_MATERIALS)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .component(DataComponentTypes.BLOCKS_ATTACKS, new BlocksAttacksComponent(0.25F, 1.0F, List.of(new BlocksAttacksComponent.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)), new BlocksAttacksComponent.ItemDamage(3.0F, 1.0F, 1.0F), Optional.of(DamageTypeTags.BYPASSES_SHIELD), Optional.of(SoundEvents.ITEM_SHIELD_BLOCK), Optional.of(SoundEvents.ITEM_SHIELD_BREAK)))
                .component(DataComponentTypes.BREAK_SOUND, SoundEvents.ITEM_SHIELD_BREAK);
    }

}
