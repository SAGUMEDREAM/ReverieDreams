package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.workshop.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WorkshopStationBlock extends WorkshopDecorBlock implements EntityBlock {
    public static final MapCodec<WorkshopStationBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StringRepresentable.fromEnum(WorkshopKind::values).fieldOf("kind").forGetter(b -> b.kind), propertiesCodec()).apply(i, WorkshopStationBlock::new));

    public WorkshopStationBlock(WorkshopKind kind, Properties properties) {
        super(kind, properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkshopStationBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || kind != WorkshopKind.HERB_DRYING_TRAY || type != RDBlockEntityTypes.WORKSHOP_STATION.get())
            return null;
        return (world, pos, s, be) -> WorkshopStationBlockEntity.tick((ServerLevel) world, pos, s, (WorkshopStationBlockEntity) be);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !player.mayBuild())
            return InteractionResult.PASS;
        if (kind == WorkshopKind.SUZURI && WorkshopItems.document(stack))
            return WorkshopWriting.open(level, pos, player, hand);
        if (level.getBlockEntity(pos) instanceof WorkshopStationBlockEntity host && host.accepts(stack)) {
            if (!level.isClientSide() && !host.insert(stack, player))
                return InteractionResult.FAIL;
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isSpectator())
            return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WorkshopStationBlockEntity host) {
            boolean handMill = kind == WorkshopKind.YAGEN || kind == WorkshopKind.STONE_MILL;
            if (player.isShiftKeyDown() && player.mayBuild())
                host.take(player);
            else if (handMill && !host.getItem(4).isEmpty() && player.mayBuild())
                host.take(player);
            else if (handMill && !host.getItem(0).isEmpty() && player.mayBuild())
                host.grind(level.getGameTime());
            else if (kind == WorkshopKind.FLOWER_VASE && player.mayBuild())
                host.take(player);
            else
                WorkshopInteraction.inspect(player, host);
        }
        return InteractionResult.SUCCESS;
    }
}
