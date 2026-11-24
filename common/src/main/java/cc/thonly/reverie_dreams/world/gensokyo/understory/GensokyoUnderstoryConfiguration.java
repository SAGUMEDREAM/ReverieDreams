package cc.thonly.reverie_dreams.world.gensokyo.understory;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Locale;

/**
 * Materials are data-driven; each clump keeps one matching log/leaf palette.
 */
public record GensokyoUnderstoryConfiguration(GensokyoForestType theme, Kind kind, List<BlockState> logs,
                                              List<BlockState> leaves, List<BlockState> flowers,
                                              List<BlockState> litter, List<BlockState> accents)
        implements FeatureConfiguration {
    public enum Kind {LEAF_PATCH, SHRUB, THICKET, FLOOR_PATCH}

    private static <E extends Enum<E>> Codec<E> enumeration(Class<E> type) {
        return Codec.STRING.comapFlatMap(name -> {
            try {
                return DataResult.success(Enum.valueOf(type, name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
            }
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<GensokyoUnderstoryConfiguration> CODEC = RecordCodecBuilder.<GensokyoUnderstoryConfiguration>create(i -> i.group(
            enumeration(GensokyoForestType.class).fieldOf("theme").forGetter(GensokyoUnderstoryConfiguration::theme),
            enumeration(Kind.class).fieldOf("kind").forGetter(GensokyoUnderstoryConfiguration::kind),
            BlockState.CODEC.listOf().fieldOf("logs").forGetter(GensokyoUnderstoryConfiguration::logs),
            BlockState.CODEC.listOf().fieldOf("leaves").forGetter(GensokyoUnderstoryConfiguration::leaves),
            BlockState.CODEC.listOf().fieldOf("flowers").forGetter(GensokyoUnderstoryConfiguration::flowers),
            BlockState.CODEC.listOf().fieldOf("litter").forGetter(GensokyoUnderstoryConfiguration::litter),
            BlockState.CODEC.listOf().fieldOf("accents").forGetter(GensokyoUnderstoryConfiguration::accents)
    ).apply(i, GensokyoUnderstoryConfiguration::new)).validate(c -> {
        if (c.logs().isEmpty() || c.logs().size() > 8 || c.logs().size() != c.leaves().size()
                || c.logs().stream().anyMatch(s -> !s.hasProperty(RotatedPillarBlock.AXIS))
                || c.leaves().stream().anyMatch(s -> !s.hasProperty(LeavesBlock.DISTANCE) || !s.hasProperty(LeavesBlock.PERSISTENT))
                || c.flowers().isEmpty() || c.litter().isEmpty() || c.accents().isEmpty()) {
            return DataResult.error(() -> "Understory needs matching log/leaf palettes and nonempty floor palettes");
        }
        return DataResult.success(c);
    });
}
