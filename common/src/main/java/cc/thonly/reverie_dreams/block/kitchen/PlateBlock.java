package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionHand;

@Setter
@Getter
public class PlateBlock extends BaseEntityBlock {
    public static final MapCodec<PlateBlock> CODEC = PlateBlock.simpleCodec(PlateBlock::new);
    public static final VoxelShape SHAPE = Block.column(14.0, 0.0, 2.0);

    public PlateBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return consumeFood(world, pos, player);
        if (!(world.getBlockEntity(pos) instanceof PlateBlockEntity plate))
            return InteractionResult.PASS;
        // Vanilla calls this even for an empty hand. An occupied plate ejects its
        // contents before considering placement, matching the original interaction.
        if (!plate.getItem().isEmpty())
            return ejectContents(world, pos, plate);
        if (stack.isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!world.isClientSide() && world instanceof ServerLevel) {
            plate.setYaw(player.getYRot());
            plate.setItem(IngredientStack.of(stack.copyWithCount(1)));
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return consumeFood(world, pos, player);
        if (!(world.getBlockEntity(pos) instanceof PlateBlockEntity plate) || plate.getItem().isEmpty())
            return InteractionResult.PASS;
        return ejectContents(world, pos, plate);
    }

    private InteractionResult ejectContents(Level world, BlockPos pos, PlateBlockEntity plate) {
        if (!world.isClientSide() && world instanceof ServerLevel server) {
            ItemEntity dropped = new ItemEntity(server, pos.getX(), pos.getY(), pos.getZ(), plate.getItem().build(), 0, 0.2, 0);
            if (!server.addFreshEntity(dropped))
                return InteractionResult.FAIL;
            plate.setItem(IngredientStack.empty());
        }
        return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Shared by block use and the event that runs before vanilla's sneak-use bypass.
     */
    public InteractionResult consumeFood(Level world, BlockPos pos, Player player) {
        if (!(world.getBlockEntity(pos) instanceof PlateBlockEntity plate))
            return InteractionResult.PASS;
        ItemStack content = plate.getItem().build();
        Consumable consumable = content.get(DataComponents.CONSUMABLE);
        if (content.isEmpty() || consumable == null || !content.has(DataComponents.FOOD) || !consumable.canConsume(player, content)) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide() && world instanceof ServerLevel) {
            // finishUsingItem invokes cuisine effects and handles consumption/remainders exactly once.
            plate.setItem(IngredientStack.of(content.finishUsingItem(world, player)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlateBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.PLATE.value(), PlateBlockEntity::tick);
    }

}
