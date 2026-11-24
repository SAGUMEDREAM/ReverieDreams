package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.block.decor.living.LivingStorageBlock;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import cc.thonly.reverie_dreams.block.entity.WorkshopStorageBlockEntity;
import cc.thonly.reverie_dreams.workshop.WorkshopCounter;
import cc.thonly.reverie_dreams.workshop.WorkshopInteraction;
import cc.thonly.reverie_dreams.workshop.WorkshopItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class WorkshopStorageBlock extends LivingStorageBlock {
    public static final MapCodec<WorkshopStorageBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(WorkshopKind::values).fieldOf("kind").forGetter(block -> block.kind), propertiesCodec()).apply(instance, WorkshopStorageBlock::new));
    protected final WorkshopKind kind;

    public WorkshopStorageBlock(WorkshopKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public WorkshopKind kind() {
        return kind;
    }

    protected String model(BlockState state) {
        return kind.id() + (state.getValue(OPEN) ? "_open" : "");
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkshopStorageBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.isEmpty() && !player.isSpectator() && player.mayBuild() && level.getBlockEntity(pos) instanceof WorkshopStorageBlockEntity host
                && !stack.isEmpty() && WorkshopItems.displayable(kind, stack)) {
            if (!level.isClientSide() && !host.insertOne(stack, player))
                return InteractionResult.FAIL;
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WorkshopStorageBlockEntity host) {
            if (player.isShiftKeyDown()) {
                if (kind == WorkshopKind.CONNECTED_COUNTER)
                    WorkshopCounter.open(player, host);
                else if ((kind == WorkshopKind.SCROLL_RACK || kind == WorkshopKind.BOOK_BOX) && WorkshopInteraction.readBooks(player, host)) {
                } else if (kind == WorkshopKind.SAKE_CASK_RACK)
                    return super.useWithoutItem(state, level, pos, player, hit);
                else if (player.mayBuild() && !player.isSpectator())
                    host.sortContents();
                return InteractionResult.SUCCESS;
            }
            if (kind == WorkshopKind.SAKE_CASK_RACK && player.mayBuild() && !player.isSpectator() && host.takeDisplay(player))
                return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return WorkshopShapes.shape(model(state), state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
