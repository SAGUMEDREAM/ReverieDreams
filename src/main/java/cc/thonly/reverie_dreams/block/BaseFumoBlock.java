package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
public class BaseFumoBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BaseFumoBlock> CODEC = simpleCodec(BaseFumoBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = ModBlockStateTemplates.FACING_16;

    protected Vec3 offsets = new Vec3(0, 0, 0);

    public BaseFumoBlock(Vec3 offsets, Properties settings) {
        super(settings.noOcclusion());
        this.offsets = offsets;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
    }

    public BaseFumoBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        double yaw = ctx.getRotation();
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
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEventInit.randomFumo(), SoundSource.BLOCKS, 1f, 1);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.5f, 1);
        }
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return SoundType.WOOL;
    }


}
