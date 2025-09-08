package cc.thonly.mystias_izakaya.block.kitchenware;

import cc.thonly.mystias_izakaya.block.MIBlockEntities;
import cc.thonly.mystias_izakaya.block.entity.KitchenwareBlockEntity;
import cc.thonly.mystias_izakaya.gui.recipe.block.KitchenBlockGui;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

@Setter
@Getter
@ToString
public class AbstractKitchenwareBlock extends BlockWithEntity {
    public static final Set<AbstractKitchenwareBlock> KITCHENWARE_BLOCKS = new HashSet<>();
    public static final MapCodec<AbstractKitchenwareBlock> CODEC = createCodec(AbstractKitchenwareBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    private Vec3d offset = new Vec3d(0, 0, 0);
    private Vector3f scale = new Vector3f(0, 0, 0);
    //    private final Boolean requiredEnergy;
    private final DoubleUnaryOperator bonusOperator;
    private final Double failureProbability;

    public AbstractKitchenwareBlock(Settings settings) {
        super(settings);
        this.bonusOperator = operand -> operand;
        this.failureProbability = 0.0;
//        this.requiredEnergy = true;
        KITCHENWARE_BLOCKS.add(this);
    }

    public AbstractKitchenwareBlock(DoubleUnaryOperator bonusOperator, Double failureProbability, Vector3f scale, Vec3d offset, Settings settings) {
        super(settings.noCollision());
        this.offset = offset;
        this.scale = scale;
        this.bonusOperator = bonusOperator;
        this.failureProbability = failureProbability;
//        this.requiredEnergy = requiredEnergy;
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
        KITCHENWARE_BLOCKS.add(this);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);
        boolean pass = false;
        BlockState downBlockState = world.getBlockState(pos.down());
        Block upBlock = downBlockState.getBlock();
        if (upBlock instanceof HopperBlock|| upBlock instanceof FenceBlock || upBlock instanceof WallBlock || upBlock instanceof LeavesBlock) {
            pass = true;
        }
        return pass || belowState.isSideSolidFullSquare(world, belowPos, Direction.UP);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && world instanceof ServerWorld) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof KitchenwareBlockEntity kitchenwareBlockEntity) {
                if (kitchenwareBlockEntity.isWorking()) {
                    serverPlayer.sendMessage(Text.translatable("block.feedback.working"), false);
                    return ActionResult.SUCCESS_SERVER;
                }
                UUID uuid = kitchenwareBlockEntity.getUuid();
                Set<KitchenBlockGui<?>> kitchenBlockGuis = KitchenwareBlockEntity.SESSIONS.computeIfAbsent(uuid, (map) -> new HashSet<>());
                KitchenBlockGui<BaseRecipe> simpleGui = new KitchenBlockGui<>(this, kitchenwareBlockEntity, serverPlayer);
                kitchenBlockGuis.add(simpleGui);
                simpleGui.open();

                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof KitchenwareBlockEntity kitchenwareBlockEntity) {
                SimpleInventory inventory = kitchenwareBlockEntity.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
                    serverWorld.spawnEntity(itemEntity);
                }
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    public boolean isWillBeFailure(World world) {
        Random random = world.getRandom();
        double failureProbability = this.failureProbability;
        return random.nextDouble() < failureProbability;
    }

    @Override
    protected MapCodec<? extends AbstractKitchenwareBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getHorizontalPlayerFacing();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new KitchenwareBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, MIBlockEntities.KITCHENWARE_BLOCK_ENTITY, KitchenwareBlockEntity::tick);
    }

}
