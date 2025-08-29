package cc.thonly.reverie_dreams.item.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class ArmorItem extends Item {
    public static final List<ArmorItem> HEAD_ITEMS = new ArrayList<>();
    public static final List<ArmorItem> CHEST_ITEMS = new ArrayList<>();
    public static final List<ArmorItem> LEG_ITEMS = new ArrayList<>();
    public static final List<ArmorItem> FEET_ITEMS = new ArrayList<>();
    public static final List<ArmorItem> ITEMS = new ArrayList<>();

    public ArmorItem(ArmorMaterial material, EquipmentType type, Settings settings) {
        super(settings.maxCount(1).armor(material, type));
        if (type.equals(EquipmentType.HELMET)) {
            HEAD_ITEMS.add(this);
        } else if (type.equals(EquipmentType.CHESTPLATE)) {
            CHEST_ITEMS.add(this);
        } else if (type.equals(EquipmentType.LEGGINGS)) {
            LEG_ITEMS.add(this);
        } else if (type.equals(EquipmentType.BOOTS)) {
            FEET_ITEMS.add(this);
        }
        ITEMS.add(this);
    }

}
