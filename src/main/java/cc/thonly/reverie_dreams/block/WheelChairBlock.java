package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.misc.WheelchairEntity;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WheelChairBlock extends ModelBlock {
    public static final MapCodec<WheelChairBlock> CODEC = createCodec(WheelChairBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = ModBlockStateTemplates.FACING_16;

    public WheelChairBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player.isSneaking()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            world.spawnEntity(new WheelchairEntity(ModEntities.WHEEL_CHAIR_ENTITY, world, pos.getX(), pos.getY(), pos.getZ(), player.getUuidAsString()));
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        double yaw = ctx.getPlayerYaw();
        SixteenDirection direction = SixteenDirection.fromYaw(yaw + 180f);
        return this.getDefaultState().with(FACING_16, direction);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    public MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
