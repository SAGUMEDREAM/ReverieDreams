package cc.thonly.reverie_dreams.paper.farming;

import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;

/** Faithfully keeps PaddyFieldBlock's intentionally empty fall/tick callbacks. Shared, no block entities. */
final class PaddyFieldBehavior extends BukkitBlockBehavior {
    static final BlockBehaviorFactory<PaddyFieldBehavior> FACTORY = (block, section) -> new PaddyFieldBehavior(block);
    private final Property<Integer> moisture;
    private PaddyFieldBehavior(BlockDefinition block) {
        super(block); moisture = BlockBehaviorFactory.getProperty("moisture", block, "moisture", Integer.class);
    }
    @Override public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state.with(moisture, 7);
    }
    @Override public void randomTick(Object block, Object[] args) { /* Source never dries, even without water. */ }
    @Override public void tick(Object block, Object[] args) { /* Source never turns to dirt under a solid block. */ }
    @Override public void fallOn(Object block, Object[] args) { /* Source omits super: no trampling OR landing damage. */ }
}
