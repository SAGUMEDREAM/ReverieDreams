package cc.thonly.reverie_dreams.world.gensokyo.deadwood;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Locale;

public record GensokyoDeadwoodConfiguration(GensokyoForestType theme, Kind kind, List<BlockState> logs,
                                            List<BlockState> splinters, List<BlockState> litter,
                                            List<BlockState> accents) implements FeatureConfiguration {
    public enum Kind {FALLEN_LOG, CUT_STUMP, SPLINTERED_TRUNK}

    private static <E extends Enum<E>> Codec<E> enumeration(Class<E> type) {
        return Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(Enum.valueOf(type, s.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + s);
            }
        }, e -> e.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<GensokyoDeadwoodConfiguration> CODEC = RecordCodecBuilder.<GensokyoDeadwoodConfiguration>create(i -> i.group(
            enumeration(GensokyoForestType.class).fieldOf("theme").forGetter(GensokyoDeadwoodConfiguration::theme),
            enumeration(Kind.class).fieldOf("kind").forGetter(GensokyoDeadwoodConfiguration::kind),
            BlockState.CODEC.listOf().fieldOf("logs").forGetter(GensokyoDeadwoodConfiguration::logs),
            BlockState.CODEC.listOf().fieldOf("splinters").forGetter(GensokyoDeadwoodConfiguration::splinters),
            BlockState.CODEC.listOf().fieldOf("litter").forGetter(GensokyoDeadwoodConfiguration::litter),
            BlockState.CODEC.listOf().fieldOf("accents").forGetter(GensokyoDeadwoodConfiguration::accents)
    ).apply(i, GensokyoDeadwoodConfiguration::new)).validate(c -> c.logs.isEmpty() || c.logs.size() != c.splinters.size()
            ? DataResult.error(() -> "Deadwood needs matching, nonempty log/splinter palettes") : DataResult.success(c));
}
