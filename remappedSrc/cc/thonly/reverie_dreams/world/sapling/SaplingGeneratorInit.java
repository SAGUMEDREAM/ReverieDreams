package cc.thonly.reverie_dreams.world.sapling;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.world.gen.ConfigurationFeatureInit;
import java.util.Optional;
import net.minecraft.world.level.block.grower.TreeGrower;

public interface SaplingGeneratorInit {
    TreeGrower SPIRITUAL_TREE = new TreeGrower(
            Touhou.id("spiritual_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.SPIRITUAL_TREE_KEY),
            Optional.empty()
    );
    TreeGrower LEMON_TREE = new TreeGrower(
            Touhou.id("lemon_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.LEMON_TREE_KEY),
            Optional.empty()
    );
    TreeGrower GINKGO_TREE = new TreeGrower(
            Touhou.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.GINKGO_TREE_KEY),
            Optional.empty()
    );
    TreeGrower PEACH_TREE = new TreeGrower(
            Touhou.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.PEACH_TREE_KEY),
            Optional.empty()
    );
}
