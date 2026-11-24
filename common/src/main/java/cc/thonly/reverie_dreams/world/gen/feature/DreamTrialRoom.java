package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Legacy registry entry retained for old datapacks; trial rooms no longer generate.
 */
public class DreamTrialRoom extends Feature<DreamTrialRoomConfig> {
    public static final ResourceKey<LootTable> DREAM_CHEST = getKey("dream_chest");

    public DreamTrialRoom(Codec<DreamTrialRoomConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DreamTrialRoomConfig> context) {
        return false;
    }

    public static ResourceKey<LootTable> getKey(String name) {
        return getKey(ReverieDreams.id("chest/" + name));
    }

    public static ResourceKey<LootTable> getKey(Identifier identifier) {
        return ResourceKey.create(Registries.LOOT_TABLE, identifier);
    }
}
