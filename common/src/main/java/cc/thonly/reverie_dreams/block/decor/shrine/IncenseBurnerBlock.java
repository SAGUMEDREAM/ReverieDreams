package cc.thonly.reverie_dreams.block.decor.shrine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stone incense burner. Lighting it emits a thin smoke column and a warm glow.
 */
public class IncenseBurnerBlock extends ShrineBlock {
    public static final MapCodec<IncenseBurnerBlock> CODEC = simpleCodec(IncenseBurnerBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape COLLISION = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    private static final VoxelShape VISUAL = Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0);

    public IncenseBurnerBlock(Properties properties) {
        super(properties, COLLISION, VISUAL);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends ShrineBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        boolean lighting = stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE);
        boolean extinguishing = stack.getItem() instanceof ShovelItem;
        if ((lighting && !state.getValue(LIT)) || (extinguishing && state.getValue(LIT))) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, lighting), Block.UPDATE_ALL);
                level.playSound(null, pos, lighting ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.7f, 1.1f);
                if (lighting) {
                    if (stack.is(Items.FIRE_CHARGE)) {
                        stack.consume(1, player);
                    } else {
                        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        stack.hurtAndBreak(1, player, slot);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.14;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.14;
            level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 0.95, z, 0, 0.03, 0);
        }
        if (random.nextInt(80) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 0.45f, 1.1f, false);
        }
    }
}
