package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
public class BaseFumoBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BaseFumoBlock> CODEC = simpleCodec(BaseFumoBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = RDBlockStateTemplates.FACING_16;
    public static final VoxelShape SHAPE = Block.column(10.0, 0.0, 10.0);

    public BaseFumoBlock(Properties settings) {
        super(settings.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        double yaw = ctx.getRotation();
        if (!PlatformContext.isFabric()) {
            yaw -= 180;
        }
        SixteenDirection direction = SixteenDirection.fromYaw(yaw);
        return this.defaultBlockState().setValue(FACING_16, direction);
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
        if (!world.isClientSide()) {
            world.playSound(null, pos, RDSoundEvents.getRandomFumoSound().value(), SoundSource.BLOCKS, 1f, 1);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
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
