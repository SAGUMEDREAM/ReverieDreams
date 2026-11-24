package cc.thonly.reverie_dreams.world.moon;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class MoonBiomes {
    public static final ResourceKey<Biome> BASALT_MARE = ResourceKey.create(Registries.BIOME, ReverieDreams.id("basalt_mare"));
    public static final ResourceKey<Biome> SHADOW_ICE_CRATER = ResourceKey.create(Registries.BIOME, ReverieDreams.id("shadow_ice_crater"));

    private MoonBiomes() {
    }
}
