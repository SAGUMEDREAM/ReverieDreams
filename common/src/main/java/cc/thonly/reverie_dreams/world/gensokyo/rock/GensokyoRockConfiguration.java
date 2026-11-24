package cc.thonly.reverie_dreams.world.gensokyo.rock;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Locale;

public record GensokyoRockConfiguration(GensokyoForestType theme, Kind kind, BlockState stone,
                                        BlockState variation, BlockState moss,
                                        float mossChance) implements FeatureConfiguration {
    public enum Kind {LOW_OUTCROP, ROCK_CLUSTER, LANDMARK_BOULDER}

    private static <E extends Enum<E>> Codec<E> enumeration(Class<E> type) {
        return Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Enum.valueOf(type, name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
            }
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<GensokyoRockConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            enumeration(GensokyoForestType.class).fieldOf("theme").forGetter(GensokyoRockConfiguration::theme),
            enumeration(Kind.class).fieldOf("kind").forGetter(GensokyoRockConfiguration::kind),
            BlockState.CODEC.fieldOf("stone").forGetter(GensokyoRockConfiguration::stone),
            BlockState.CODEC.fieldOf("variation").forGetter(GensokyoRockConfiguration::variation),
            BlockState.CODEC.fieldOf("moss").forGetter(GensokyoRockConfiguration::moss),
            Codec.floatRange(0, 1).fieldOf("moss_chance").forGetter(GensokyoRockConfiguration::mossChance)
    ).apply(i, GensokyoRockConfiguration::new));
}
