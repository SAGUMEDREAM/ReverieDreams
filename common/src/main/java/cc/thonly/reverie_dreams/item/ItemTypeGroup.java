package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.item.base.PickaxeItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public enum ItemTypeGroup {
    SWORD(),
    PICKAXES(),
    AXES(),
    SHOVELS(),
    HOES(),
    SPEARS(),
    ARMOR(),
    ;
    private final Set<Item> entries = new LinkedHashSet<>();

    ItemTypeGroup() {
    }

    public static void join(Item item) {
        if (item instanceof SwordItem) {
            SWORD.add(item);
        }
        if (item instanceof PickaxeItem) {
            PICKAXES.add(item);
        }
        if (item instanceof AxeItem) {
            AXES.add(item);
        }
        if (item instanceof ShovelItem) {
            SHOVELS.add(item);
        }
        if (item instanceof HoeItem) {
            HOES.add(item);
        }
        if (item instanceof ArmorItem) {
            ARMOR.add(item);
        }
        if (item.asItem().getDefaultInstance().has(DataComponents.KINETIC_WEAPON)) {
            SPEARS.add(item);
        }
    }


    public void add(Item item) {
        this.entries.add(item);
    }

    public Stream<Item> stream() {
        return this.entries.stream();
    }

    public Collection<Item> items() {
        return Set.copyOf(this.entries);
    }
}
