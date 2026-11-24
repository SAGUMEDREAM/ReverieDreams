package cc.thonly.reverie_dreams.block.dream;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FootstepMossBlock extends CarpetBlock {
    public static final IntegerProperty TRACE = IntegerProperty.create("trace", 0, 2);

    public FootstepMossBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(TRACE) * 2));
        registerDefaultState(stateDefinition.any().setValue(TRACE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRACE);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        mark(level, pos, state, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effects, boolean intersects) {
        if (entity.onGround()) {
            mark(level, pos, state, entity);
        }
    }

    private void mark(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof Player player
                && !player.isSpectator() && state.getValue(TRACE) == 0) {
            level.setBlock(pos, state.setValue(TRACE, 2), Block.UPDATE_CLIENTS);
            server.scheduleTick(pos, this, 6 * 20);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int trace = state.getValue(TRACE);
        if (trace == 0)
            return;
        level.setBlock(pos, state.setValue(TRACE, trace - 1), Block.UPDATE_CLIENTS);
        if (trace == 2)
            level.scheduleTick(pos, this, 2 * 20);
    }
}
