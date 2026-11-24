package cc.thonly.reverie_dreams.world.moon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

public final class MoonBiomeSource extends BiomeSource {
    public static final MapCodec<MoonBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.CODEC.fieldOf("highlands").forGetter(s -> s.highlands),
            Biome.CODEC.fieldOf("basalt_mare").forGetter(s -> s.basalt),
            Biome.CODEC.fieldOf("ice_crater").forGetter(s -> s.ice),
            Biome.CODEC.fieldOf("sea_boundary").forGetter(s -> s.sea)
    ).apply(i, MoonBiomeSource::new));
    private final Holder<Biome> highlands, basalt, ice, sea;
    private volatile long seed;

    public MoonBiomeSource(Holder<Biome> highlands, Holder<Biome> basalt, Holder<Biome> ice, Holder<Biome> sea) {
        this.highlands = highlands;
        this.basalt = basalt;
        this.ice = ice;
        this.sea = sea;
    }

    public void bindSeed(long seed) {
        this.seed = seed;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(highlands, basalt, ice, sea);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return switch (MoonTerrain.sample(seed, x * 4, z * 4).region()) {
            case HIGHLANDS -> highlands;
            case BASALT_MARE -> basalt;
            case SHADOW_ICE_CRATER -> ice;
            case SEA_BOUNDARY -> sea;
        };
    }
}
