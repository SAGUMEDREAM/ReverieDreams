package cc.thonly.reverie_dreams.block.decor.living;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Inset, non-spreading hearth. It emits light and sparse smoke without an entity ticker.
 */
public class IroriBlock extends Block {
    public static final MapCodec<IroriBlock> CODEC = simpleCodec(IroriBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final VoxelShape shape;

    public IroriBlock(Properties properties) {
        this(properties, JapaneseLivingShapes.north("irori"));
    }

    protected IroriBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        boolean lighting = stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE);
        boolean extinguishing = stack.getItem() instanceof ShovelItem;
        if ((lighting && !state.getValue(LIT)) || (extinguishing && state.getValue(LIT))) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, lighting), 3);
                level.playSound(null, pos, lighting ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.8f, 1.0f);
                if (stack.is(Items.FIRE_CHARGE))
                    stack.consume(1, player);
                else
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT))
            return;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.0, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.025, 0);
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.8, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.01, 0);
        }
        if (random.nextInt(30) == 0)
            level.playLocalSound(pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.35f, 0.9f, false);
    }
}
