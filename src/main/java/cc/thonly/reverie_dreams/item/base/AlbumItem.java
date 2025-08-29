package cc.thonly.reverie_dreams.item.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;

import java.util.Set;

public class AlbumItem extends Item {
    public static final Set<Item> ITEMS = new ObjectOpenHashSet<>();

    public AlbumItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON).translationKey(Items.MUSIC_DISC_5.getTranslationKey()));
        ITEMS.add(this);
    }

}
