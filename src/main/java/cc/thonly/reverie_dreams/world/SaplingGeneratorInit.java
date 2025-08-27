package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.world.gen.ConfigurationFeatureInit;
import net.minecraft.block.SaplingGenerator;

import java.util.Optional;

public interface SaplingGeneratorInit {
    SaplingGenerator SPIRITUAL_TREE = new SaplingGenerator(
            Touhou.id("spiritual_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.SPIRITUAL_TREE_KEY),
            Optional.empty()
    );
    SaplingGenerator LEMON_TREE = new SaplingGenerator(
            Touhou.id("lemon_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.LEMON_TREE_KEY),
            Optional.empty()
    );
    SaplingGenerator GINKGO_TREE = new SaplingGenerator(
            Touhou.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.GINKGO_TREE_KEY),
            Optional.empty()
    );
    SaplingGenerator PEACH_TREE = new SaplingGenerator(
            Touhou.id("ginkgo_tree").toString(),
            Optional.empty(),
            Optional.of(ConfigurationFeatureInit.PEACH_TREE_KEY),
            Optional.empty()
    );
}
