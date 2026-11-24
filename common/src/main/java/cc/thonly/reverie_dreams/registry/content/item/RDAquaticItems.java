package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.registry.content.entity.RDAquaticEntities;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;

import java.util.EnumMap;
import java.util.Map;

public final class RDAquaticItems {
    private static final Map<AquaticSpecies, ItemDelegate> BUCKETS = new EnumMap<>(AquaticSpecies.class);

    static {
        for (AquaticSpecies species : AquaticSpecies.values()) {
            BUCKETS.put(species, RDItems.registerItem(species.key() + "_bucket",
                    properties -> new MobBucketItem(RDAquaticEntities.type(species).value(), Fluids.WATER,
                            SoundEvents.BUCKET_EMPTY_FISH, properties),
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
        }
    }

    private RDAquaticItems() {}

    public static void initialize() {}

    public static Item bucket(AquaticSpecies species) {
        return BUCKETS.get(species).asItem();
    }
}
