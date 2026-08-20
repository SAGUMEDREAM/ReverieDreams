package cc.thonly.reverie_dreams.world.sapling;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinConfigurationFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public interface SaplingGeneratorInit {
    TreeGrower SPIRITUAL_TREE = new TreeGrower(
            ReverieDreams.id("spiritual_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.SPIRITUAL_TREE_KEY),
            Optional.empty()
    );
    TreeGrower LEMON_TREE = new TreeGrower(
            ReverieDreams.id("lemon_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.LEMON_TREE_KEY),
            Optional.empty()
    );
    TreeGrower GINKGO_TREE = new TreeGrower(
            ReverieDreams.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.GINKGO_TREE_KEY),
            Optional.empty()
    );
    TreeGrower PEACH_TREE = new TreeGrower(
            ReverieDreams.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.PEACH_TREE_KEY),
            Optional.empty()
    );
}
