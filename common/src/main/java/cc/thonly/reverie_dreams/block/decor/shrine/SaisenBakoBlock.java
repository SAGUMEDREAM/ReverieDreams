package cc.thonly.reverie_dreams.block.decor.shrine;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wooden offering box. Coins fill a comparator-readable offering counter.
 */
public class SaisenBakoBlock extends ShrineBlock {
    public static final MapCodec<SaisenBakoBlock> CODEC = simpleCodec(SaisenBakoBlock::new);
    public static final IntegerProperty OFFERINGS = IntegerProperty.create("offerings", 0, 15);

    public SaisenBakoBlock(Properties properties) {
        this(properties, Block.box(1.0, 0.0, 1.0, 15.0, 9.0, 15.0));
    }

    public SaisenBakoBlock(Properties properties, VoxelShape shape) {
        super(properties, shape, shape);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OFFERINGS, 0));
    }

    @Override
    protected MapCodec<? extends ShrineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OFFERINGS);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        if (!isOffering(stack)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            int offerings = state.getValue(OFFERINGS);
            if (offerings < OFFERINGS.getPossibleValues().size() - 1) {
                level.setBlock(pos, state.setValue(OFFERINGS, offerings + 1), Block.UPDATE_ALL);
                if (!player.hasInfiniteMaterials()) {
                    stack.consume(1, player);
                }
                level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.45f, 1.45f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isOffering(ItemStack stack) {
        return stack.getItem() == RDItems.COPPER_COIN.asItem()
                || stack.getItem() == RDItems.SILVER_COIN.asItem()
                || stack.getItem() == RDItems.GOLD_COIN.asItem();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(OFFERINGS);
    }
}
