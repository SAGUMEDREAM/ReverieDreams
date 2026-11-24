package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.nature.SplinteredLogBlock;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.material.MapColor;

/** Initialized by RDWoodBlocks; the preview features do not own a registry lifecycle. */
public final class RDDeadwoodBlocks {
    public static final List<String> WOODS = List.of("oak", "birch", "spruce", "cherry", "dark_oak",
            "iroha_maple", "itaya_maple", "hauchiwa_maple");
    public static final Map<String, BlockDelegate> SPLINTERED_LOGS = register();
    private static Map<String, BlockDelegate> register() {
        var result = new LinkedHashMap<String, BlockDelegate>();
        for (String wood : WOODS) result.put(wood, RDBlocks.registerSimpleBlock("splintered_" + wood + "_log",
                SplinteredLogBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                        // Vanilla log mapColor reads AXIS; this directional split block has no AXIS.
                        .mapColor(MapColor.WOOD).noOcclusion()));
        return Collections.unmodifiableMap(result);
    }
    public static void initialize() {}
    private RDDeadwoodBlocks() {}
}
