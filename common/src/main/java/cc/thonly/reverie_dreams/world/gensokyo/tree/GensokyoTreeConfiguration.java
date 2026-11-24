package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Locale;

public record GensokyoTreeConfiguration(BlockState log, BlockState leaves, GensokyoTreeShape.Species shape,
                                        int minHeight, int maxHeight, int trunkWidth, int crownRadius,
                                        GensokyoTreeShape.CrownStyle crownStyle) implements FeatureConfiguration {
    public GensokyoTreeConfiguration(BlockState log, BlockState leaves, GensokyoTreeShape.Species shape,
                                     int minHeight, int maxHeight, int trunkWidth, int crownRadius) {
        this(log, leaves, shape, minHeight, maxHeight, trunkWidth, crownRadius, GensokyoTreeShape.CrownStyle.BRANCHING);
    }

    private static final Codec<GensokyoTreeShape.Species> SHAPE_CODEC = Codec.STRING.comapFlatMap(value -> {
        try {
            return DataResult.success(GensokyoTreeShape.Species.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Unknown Gensokyo tree shape: " + value);
        }
    }, shape -> shape.name().toLowerCase(Locale.ROOT));
    private static final Codec<GensokyoTreeShape.CrownStyle> CROWN_STYLE_CODEC = Codec.STRING.comapFlatMap(value -> {
        try {
            return DataResult.success(GensokyoTreeShape.CrownStyle.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Unknown crown style: " + value);
        }
    }, style -> style.name().toLowerCase(Locale.ROOT));

    public static final Codec<GensokyoTreeConfiguration> CODEC = RecordCodecBuilder.<GensokyoTreeConfiguration>create(instance -> instance.group(
            BlockState.CODEC.fieldOf("log").forGetter(GensokyoTreeConfiguration::log),
            BlockState.CODEC.fieldOf("leaves").forGetter(GensokyoTreeConfiguration::leaves),
            SHAPE_CODEC.fieldOf("shape").forGetter(GensokyoTreeConfiguration::shape),
            Codec.intRange(8, 40).fieldOf("min_height").forGetter(GensokyoTreeConfiguration::minHeight),
            Codec.intRange(8, 40).fieldOf("max_height").forGetter(GensokyoTreeConfiguration::maxHeight),
            Codec.intRange(1, 2).fieldOf("trunk_width").forGetter(GensokyoTreeConfiguration::trunkWidth),
            Codec.intRange(2, 6).fieldOf("crown_radius").forGetter(GensokyoTreeConfiguration::crownRadius),
            CROWN_STYLE_CODEC.optionalFieldOf("crown_style", GensokyoTreeShape.CrownStyle.BRANCHING).forGetter(GensokyoTreeConfiguration::crownStyle)
    ).apply(instance, GensokyoTreeConfiguration::new)).validate(config -> {
        if (config.minHeight() > config.maxHeight() || !config.log().hasProperty(RotatedPillarBlock.AXIS)
                || !config.leaves().hasProperty(LeavesBlock.DISTANCE) || !config.leaves().hasProperty(LeavesBlock.PERSISTENT)
                || (config.crownStyle() == GensokyoTreeShape.CrownStyle.TALL_MAPLE && !config.shape().isMaple())
                || (config.crownStyle().isJungle() && config.shape() != GensokyoTreeShape.Species.JUNGLE)) {
            return DataResult.error(() -> "Tree needs ordered height bounds, an axis-bearing log and distance-bearing leaves");
        }
        return DataResult.success(config);
    });

    public GensokyoTreeShape.Settings settings() {
        return new GensokyoTreeShape.Settings(shape, minHeight, maxHeight, trunkWidth, crownRadius, crownStyle);
    }
}
