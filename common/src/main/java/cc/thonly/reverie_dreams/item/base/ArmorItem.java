package cc.thonly.reverie_dreams.item.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

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

    public ArmorItem(ArmorMaterial material, ArmorType type, Properties settings) {
        super(settings.stacksTo(1).humanoidArmor(material, type));
        if (type.equals(ArmorType.HELMET)) {
            HEAD_ITEMS.add(this);
        } else if (type.equals(ArmorType.CHESTPLATE)) {
            CHEST_ITEMS.add(this);
        } else if (type.equals(ArmorType.LEGGINGS)) {
            LEG_ITEMS.add(this);
        } else if (type.equals(ArmorType.BOOTS)) {
            FEET_ITEMS.add(this);
        }
        ITEMS.add(this);
    }

}
