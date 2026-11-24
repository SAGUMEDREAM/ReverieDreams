package cc.thonly.reverie_dreams.world.gensokyo.landmark;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoTreeFeature;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Reuses established tree geometry while leaving coherent gaps in these open landscapes.
 */
public final class LandmarkTreeFeature extends Feature<GensokyoTreeConfiguration> {
    public LandmarkTreeFeature(Codec<GensokyoTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GensokyoTreeConfiguration> context) {
        var p = context.origin();
        long seed = context.level().getSeed();
        if (context.level() instanceof WorldGenRegion && (LandmarkDistribution.openLane(seed, p.getX(), p.getZ())
                || LandmarkDistribution.field(seed ^ 0x54524545, p.getX(), p.getZ(), 96) < .38))
            return false;
        return GensokyoTreeFeature.placeTree(context.level(), p, context.config(), context.random().nextLong());
    }
}
