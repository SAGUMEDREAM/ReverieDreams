package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.registry.content.block.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.block.entity.FumoBlockEntity;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import cc.thonly.reverie_dreams.util.FumoGrowth;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
public class BaseFumoBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BaseFumoBlock> CODEC = simpleCodec(BaseFumoBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = RDBlockStateTemplates.FACING_16;
    public static final VoxelShape SHAPE = Block.column(10.0, 0.0, 10.0);
    private static final VoxelShape[] GROWN_SHAPES = new VoxelShape[11];

    static {
        for (int i = 0; i <= 10; i++) {
            double size = 10 * FumoGrowth.scale(i);
            GROWN_SHAPES[i] = Block.column(size, 0, size);
        }
    }

    public BaseFumoBlock(Properties settings) {
        super(settings.noOcclusion().dynamicShape());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return supportsGrowth() && level.getBlockEntity(pos) instanceof FumoBlockEntity fumo
                ? GROWN_SHAPES[fumo.getCakesFed()] : SHAPE;
    }

    public boolean supportsGrowth() {
        return true;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return rotateFumo(state, level, pos, player);
        if (!supportsGrowth() || !stack.is(Items.CAKE))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (player.isSpectator() || !player.mayBuild())
            return InteractionResult.FAIL;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof FumoBlockEntity fumo))
            return InteractionResult.FAIL;
        if (fumo.feedCake()) {
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, .65f, 1.15f);
        } else {
            player.sendSystemMessage(Component.translatable("reverie_dreams.fumo.max_size"));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (supportsGrowth() && params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FumoBlockEntity fumo) {
            for (ItemStack stack : drops)
                if (stack.is(asItem()))
                    FumoGrowth.writeItem(stack, fumo.getCakesFed());
        }
        return drops;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (supportsGrowth() && level.getBlockEntity(pos) instanceof FumoBlockEntity fumo)
            FumoGrowth.writeItem(stack, fumo.getCakesFed());
        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        SixteenDirection direction = SixteenDirection.fromYaw(ctx.getRotation() - 180.0);
        return this.defaultBlockState().setValue(FACING_16, direction);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FumoBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING_16, SixteenDirection.values()[rotation.rotate(state.getValue(FACING_16).ordinal(), 16)]);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING_16, SixteenDirection.values()[mirror.mirror(state.getValue(FACING_16).ordinal(), 16)]);
    }

    public InteractionResult rotateFumo(BlockState state, Level world, BlockPos pos, Player player) {
        if (player.isSpectator() || !player.mayBuild()) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide()) {
            // Face the player with the same half-turn used during placement.
            SixteenDirection direction = SixteenDirection.fromYaw(player.getYRot() - 180.0);
            if (state.getValue(FACING_16) != direction) {
                world.setBlock(pos, state.setValue(FACING_16, direction), Block.UPDATE_ALL);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        world.playSound(null, hit.getBlockPos(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5f, 1);
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return this.rotateFumo(state, world, pos, player);
        }
        if (!world.isClientSide()) {
            world.playSound(null, pos, RDSoundEvents.getRandomFumoSound().value(), SoundSource.BLOCKS, 1f, 1);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (supportsGrowth() && !world.isClientSide() && world.getBlockEntity(pos) instanceof FumoBlockEntity fumo) {
            fumo.setCakesFed(FumoGrowth.fromItem(itemStack));
        }
        if (!world.isClientSide()) {
            world.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5f, 1);
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return SoundType.WOOL;
    }


}
