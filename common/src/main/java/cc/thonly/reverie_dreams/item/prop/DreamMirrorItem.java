package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.block.dream.MirrorBridgeBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.dream.DreamGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DreamMirrorItem extends Item {
    private static final int MAX_SPAN = 24;
    private static final int MAX_APPROACH = 4;
    private static final int MAX_WATER_DEPTH = 3;
    private static final List<Integer> HEIGHT_OFFSETS = List.of(0, 1, -1);
    private static final List<Float> AIM_OFFSETS = List.of(0.0F, -15.0F, 15.0F);

    private record DeckCell(BlockPos pos, BlockState original) {
    }

    public DreamMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player) || !(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack))
            return InteractionResult.FAIL;
        List<DeckCell> span = findSpan(server, player, stack);
        if (span.isEmpty() || !placeBridge(server, span)) {
            player.sendSystemMessage(Component.translatable("message.reverie_dreams.dream_mirror.no_bank"), true);
            return InteractionResult.FAIL;
        }
        server.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.15F);
        player.getCooldowns().addCooldown(stack, 12 * 20);
        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : use(context.getLevel(), player, context.getHand());
    }

    private static List<DeckCell> findSpan(ServerLevel level, ServerPlayer player, ItemStack stack) {
        if (!player.onGround() || player.isInWater() || level.getBlockState(player.getOnPos()).is(RDBlocks.MIRROR_BRIDGE.get())) {
            return List.of();
        }
        BlockPos start = BlockPos.containing(player.getX(), player.getY() - 1.0E-5, player.getZ());
        for (float yawOffset : AIM_OFFSETS) {
            List<BlockPos> columns = forwardColumns(start, player.getYRot() + yawOffset);
            for (int heightOffset : HEIGHT_OFFSETS) {
                List<DeckCell> span = findSpan(level, player, stack, columns, start.getY(), heightOffset);
                if (!span.isEmpty())
                    return span;
            }
        }
        return List.of();
    }

    private static List<DeckCell> findSpan(ServerLevel level, ServerPlayer player, ItemStack stack, List<BlockPos> columns,
                                           int shoreY, int heightOffset) {
        List<DeckCell> span = new ArrayList<>(MAX_SPAN);
        boolean inDream = level.dimension().equals(RDBuiltinLevels.DREAM_WORLD);
        int approach = 0;
        for (BlockPos column : columns) {
            int deckY = shoreY + heightOffset;
            BlockPos deck = column.atY(deckY);
            if (!level.isLoaded(deck))
                return List.of();
            BlockPos bank = findBank(level, column.atY(span.isEmpty() ? shoreY : deckY));
            if (bank != null) {
                if (!span.isEmpty())
                    return span;
                if (++approach > MAX_APPROACH)
                    return List.of();
                shoreY = bank.getY();
                continue;
            }
            if (span.size() == MAX_SPAN || !clearForTravel(level, deck.above()) || !clearForTravel(level, deck.above(2))
                    || (inDream && DreamGridLayout.isGridPosition(deck))
                    || !player.mayInteract(level, deck) || !player.mayUseItemAt(deck, Direction.UP, stack))
                return List.of();
            BlockState original = level.getBlockState(deck);
            if (!isSourceWater(original) && !(original.isAir() && hasWaterBelow(level, deck)))
                return List.of();
            span.add(new DeckCell(deck, original));
        }
        return List.of();
    }

    private static List<BlockPos> forwardColumns(BlockPos start, float yaw) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
        double xInterval = Math.abs(forward.x) < 1.0E-6 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(forward.x);
        double zInterval = Math.abs(forward.z) < 1.0E-6 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(forward.z);
        int xStep = forward.x < 0 ? -1 : 1;
        int zStep = forward.z < 0 ? -1 : 1;
        double nextX = xInterval * 0.5;
        double nextZ = zInterval * 0.5;
        List<BlockPos> columns = new ArrayList<>(MAX_APPROACH + MAX_SPAN + 1);
        BlockPos pos = start;
        for (int i = 0; i < MAX_APPROACH + MAX_SPAN + 1; i++) {
            // Walk one axis at a time so diagonally aimed bridges are connected by full edges.
            if (nextX <= nextZ) {
                pos = pos.offset(xStep, 0, 0);
                nextX += xInterval;
            } else {
                pos = pos.offset(0, 0, zStep);
                nextZ += zInterval;
            }
            columns.add(pos);
        }
        return columns;
    }

    private static BlockPos findBank(ServerLevel level, BlockPos column) {
        for (int offset : HEIGHT_OFFSETS) {
            BlockPos pos = column.above(offset);
            BlockState state = level.getBlockState(pos);
            if (!state.is(RDBlocks.MIRROR_BRIDGE.get()) && state.getFluidState().isEmpty()
                    && state.isFaceSturdy(level, pos, Direction.UP)
                    && clearForTravel(level, pos.above()) && clearForTravel(level, pos.above(2)))
                return pos;
        }
        return null;
    }

    private static boolean clearForTravel(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean hasWaterBelow(ServerLevel level, BlockPos deck) {
        for (int depth = 1; depth <= MAX_WATER_DEPTH; depth++) {
            BlockState state = level.getBlockState(deck.below(depth));
            if (state.getFluidState().is(FluidTags.WATER))
                return true;
            if (!state.isAir())
                return false;
        }
        return false;
    }

    private static boolean isSourceWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    private static boolean placeBridge(ServerLevel level, List<DeckCell> span) {
        BlockState deck = RDBlocks.MIRROR_BRIDGE.get().defaultBlockState();
        for (int i = 0; i < span.size(); i++) {
            DeckCell cell = span.get(i);
            if (level.getBlockState(cell.pos()).equals(cell.original())
                    && level.setBlock(cell.pos(), deck.setValue(MirrorBridgeBlock.WATERLOGGED, isSourceWater(cell.original())),
                    Block.UPDATE_ALL))
                continue;
            for (DeckCell placed : span.subList(0, i)) {
                level.setBlock(placed.pos(), placed.original(), Block.UPDATE_ALL);
            }
            return false;
        }
        return true;
    }
}
