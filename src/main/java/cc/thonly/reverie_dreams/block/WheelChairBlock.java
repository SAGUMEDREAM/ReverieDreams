//package cc.thonly.reverie_dreams.block;
//
//import cc.thonly.polymer.block.HorizontalFacingInf;
//import cc.thonly.reverie_dreams.block.base.ModelBlock;
//import cc.thonly.reverie_dreams.entity.misc.WheelchairEntity;
//import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
//import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
//import cc.thonly.reverie_dreams.state.SixteenDirection;
//import com.mojang.serialization.MapCodec;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.context.BlockPlaceContext;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.HorizontalDirectionalBlock;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.StateDefinition;
//import net.minecraft.world.level.block.state.properties.EnumProperty;
//import net.minecraft.world.phys.BlockHitResult;
//
//public class WheelChairBlock extends ModelBlock implements HorizontalFacingInf {
//    public static final MapCodec<WheelChairBlock> CODEC = simpleCodec(WheelChairBlock::new);
//    public static final EnumProperty<SixteenDirection> FACING_16 = RDBlockStateTemplates.FACING_16;
//
//    public WheelChairBlock(Properties settings) {
//        super(settings);
//        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
//    }
//
//    @Override
//    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
//        if (!world.isClientSide && player.isShiftKeyDown()) {
//            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
//            world.addFreshEntity(new WheelchairEntity(RDEntityTypes.WHEEL_CHAIR, world, pos.getX(), pos.getY(), pos.getZ(), player.getStringUUID()));
//            return InteractionResult.SUCCESS_SERVER;
//        }
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
//        double yaw = ctx.getRotation();
//        SixteenDirection direction = SixteenDirection.fromYaw(yaw + 180f);
//        return this.defaultBlockState().setValue(FACING_16, direction);
//    }
//
//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
//        builder.add(FACING_16);
//    }
//
//    @Override
//    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
//        return CODEC;
//    }
//}
