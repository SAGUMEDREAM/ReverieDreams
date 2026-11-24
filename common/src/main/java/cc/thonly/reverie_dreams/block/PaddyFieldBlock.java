package cc.thonly.reverie_dreams.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A permanently moist farmland block used by the Gensokyo rice paddies.
 *
 * <p>It uses the vanilla farmland moisture property and the vanilla moist
 * farmland model, but it never dries, never turns back into dirt from random
 * ticks or from being jumped on. Breaking it drops plain dirt via its loot
 * table.</p>
 */
public class PaddyFieldBlock extends FarmlandBlock {
    public PaddyFieldBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, MAX_MOISTURE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(MOISTURE, MAX_MOISTURE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Never dry out, even without nearby water.
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Do not turn into dirt when the block above changes.
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        // Do not turn into dirt when an entity lands on it.
    }
}