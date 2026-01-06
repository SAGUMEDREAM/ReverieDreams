package cc.thonly.reverie_dreams.item.base;

import cc.thonly.polymer.item.IBasicPolymerItem;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.Set;

public class AlbumItem extends Item implements IBasicPolymerItem {
    public static final Set<Item> ITEMS = new ObjectOpenHashSet<>();

    public AlbumItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON).overrideDescription(Items.MUSIC_DISC_5.getDescriptionId()));
        ITEMS.add(this);
    }

}
