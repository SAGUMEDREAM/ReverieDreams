package cc.thonly.reverie_dreams.item.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AlbumItem extends Item {
    public static final List<Item> ITEMS = new ArrayList<>() {
        @Override
        public boolean add(Item item) {
            if (this.contains(item)) {
                return true;
            }
            return super.add(item);
        }
    };

    public AlbumItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON).overrideDescription(Items.MUSIC_DISC_5.getDescriptionId()));
        ITEMS.add(this);
    }

}
