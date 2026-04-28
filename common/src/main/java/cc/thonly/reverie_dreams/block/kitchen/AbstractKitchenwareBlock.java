package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.gui.recipe.gui.KitchenBlockGui;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

@Setter
@Getter
@ToString
public class AbstractKitchenwareBlock extends BaseEntityBlock {
    public static final Set<AbstractKitchenwareBlock> KITCHENWARE_BLOCKS = new HashSet<>();
    public static final MapCodec<AbstractKitchenwareBlock> CODEC = simpleCodec(AbstractKitchenwareBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape NONE = Shapes.empty();
    private Vec3 offset = new Vec3(0, 0, 0);
    private Vector3f scale = new Vector3f(0, 0, 0);
    private final DoubleUnaryOperator bonusOperator;
    private final Double failureProbability;

    public AbstractKitchenwareBlock(Properties settings) {
        super(settings);
        this.bonusOperator = operand -> operand;
        this.failureProbability = 0.0;
        KITCHENWARE_BLOCKS.add(this);
    }

    public AbstractKitchenwareBlock(DoubleUnaryOperator bonusOperator, Double failureProbability, Vector3f scale, Vec3 offset, Properties settings) {
        super(settings.noCollision());
        this.offset = offset;
        this.scale = scale;
        this.bonusOperator = bonusOperator;
        this.failureProbability = failureProbability;
//        this.requiredEnergy = requiredEnergy;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        KITCHENWARE_BLOCKS.add(this);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        boolean pass = false;
        BlockState downBlockState = world.getBlockState(pos.below());
        Block upBlock = downBlockState.getBlock();
        if (upBlock instanceof HopperBlock|| upBlock instanceof FenceBlock || upBlock instanceof WallBlock || upBlock instanceof LeavesBlock) {
            pass = true;
        }
        return pass || belowState.isFaceSturdy(world, belowPos, Direction.UP);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && world instanceof ServerLevel) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof KitchenwareBlockEntity kitchenwareBlockEntity) {
                if (kitchenwareBlockEntity.isWorking()) {
                    serverPlayer.displayClientMessage(Component.translatable("block.feedback.working"), false);
                    return InteractionResult.SUCCESS_SERVER;
                }
                UUID uuid = kitchenwareBlockEntity.getUuid();
                Set<KitchenBlockGui<?>> kitchenBlockGuis = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(uuid, (map) -> new HashSet<>());
                KitchenBlockGui<BaseRecipe> simpleGui = new KitchenBlockGui<>(this, kitchenwareBlockEntity, serverPlayer);
                kitchenBlockGuis.add(simpleGui);
                simpleGui.open();

                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof KitchenwareBlockEntity kitchenwareBlockEntity) {
                SimpleContainer inventory = kitchenwareBlockEntity.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
                    serverWorld.addFreshEntity(itemEntity);
                }
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    public boolean isWillBeFailure(Level world) {
        RandomSource random = world.getRandom();
        double failureProbability = this.failureProbability;
        return random.nextDouble() < failureProbability;
    }

    @Override
    protected MapCodec<? extends AbstractKitchenwareBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KitchenwareBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.KITCHENWARE_BLOCK.value(), KitchenwareBlockEntity::tick);
    }

}
