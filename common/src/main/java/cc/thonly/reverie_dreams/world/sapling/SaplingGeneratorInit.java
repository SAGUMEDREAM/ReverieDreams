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
            ReverieDreams.id("peach_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.PEACH_TREE_KEY),
            Optional.empty()
    );
    TreeGrower PLUM_TREE = new TreeGrower(
            ReverieDreams.id("plum_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.PLUM_TREE_KEY),
            Optional.empty()
    );
    TreeGrower PINE_TREE = new TreeGrower(
            ReverieDreams.id("pine_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.PINE_TREE_KEY),
            Optional.empty()
    );
    TreeGrower CHESTNUT_TREE = new TreeGrower(
            ReverieDreams.id("chestnut_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.CHESTNUT_TREE_KEY),
            Optional.empty()
    );
    TreeGrower IROHA_MAPLE_TREE = new TreeGrower(
            ReverieDreams.id("iroha_maple_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.IROHA_MAPLE_TREE_KEY),
            Optional.empty()
    );
    TreeGrower ITAYA_MAPLE_TREE = new TreeGrower(
            ReverieDreams.id("itaya_maple_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.ITAYA_MAPLE_TREE_KEY),
            Optional.empty()
    );
    TreeGrower HAUCHIWA_MAPLE_TREE = new TreeGrower(
            ReverieDreams.id("hauchiwa_maple_tree").toString(),
            Optional.empty(),
            Optional.of(RDBuiltinConfigurationFeatures.HAUCHIWA_MAPLE_TREE_KEY),
            Optional.empty()
    );
}
