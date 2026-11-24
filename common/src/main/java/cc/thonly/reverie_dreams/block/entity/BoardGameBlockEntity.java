package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.board.*;
import cc.thonly.reverie_dreams.block.decor.BoardGameBlock;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

import java.util.function.Consumer;
import java.util.List;

public class BoardGameBlockEntity extends BlockEntity {
    private BoardGame game;
    private Direction facing = Direction.NORTH;
    /**
     * Loader adapter; common never loads Polymer/client classes. Cleared with its holder.
     */
    private Consumer<BoardGameBlockEntity> displayUpdate;
    private BoardGame displayedGame;
    private long displayedRevision = Long.MIN_VALUE;
    private Direction displayedFacing;
    private List<BoardDisplays.Part> displayParts = List.of();

    public List<BoardDisplays.Part> displayParts() {
        if (displayedGame != game || displayedRevision != (game == null ? -1 : game.revision) || displayedFacing != facing()) {
            displayedGame = game;
            displayedRevision = game == null ? -1 : game.revision;
            displayedFacing = facing();
            displayParts = BoardDisplays.buildParts(this);
        }
        return displayParts;
    }

    public BoardGameBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.BOARD_GAME.value(), pos, state);
        if (state.getBlock() instanceof BoardGameBlock board)
            game = new BoardGame(board.kind);
    }

    public BoardGame game() {
        return game;
    }

    public Direction facing() {
        return getBlockState().getBlock() instanceof BoardGameBlock ? getBlockState().getValue(BoardGameBlock.FACING) : facing;
    }

    public double surface() {
        return getBlockState().getBlock() instanceof ChabudaiBlock ? ChabudaiBlock.TOP_SURFACE : 0;
    }

    public boolean attach(BoardKind kind, Direction facing) {
        if (game != null)
            return false;
        this.facing = facing;
        game = new BoardGame(kind);
        publish();
        return true;
    }

    public boolean detach() {
        if (game == null || (game.started && game.position.phase() != BoardPosition.Phase.FINISHED))
            return false;
        ItemStack drop = boardStack(game.position.kind);
        game = null;
        publish();
        if (level instanceof ServerLevel)
            Block.popResource(level, worldPosition, drop);
        return true;
    }

    public static ItemStack boardStack(BoardKind kind) {
        return switch (kind) {
            case CHESS -> RDBlocks.CHESS_BOARD.createStack();
            case XIANGQI -> RDBlocks.XIANGQI_BOARD.createStack();
            case GO -> RDBlocks.GO_BOARD.createStack();
        };
    }

    public void setDisplayUpdate(Consumer<BoardGameBlockEntity> callback) {
        displayUpdate = callback;
        if (callback != null)
            callback.accept(this);
    }

    public void clearDisplayUpdate(Consumer<BoardGameBlockEntity> callback) {
        if (displayUpdate == callback)
            displayUpdate = null;
    }

    public void publish() {
        setChanged();
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            if (displayUpdate != null)
                displayUpdate.accept(this);
            BoardGames.changed(this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (game != null)
            out.putString("BoardGame", game.encode());
        out.putString("BoardFacing", facing.getName());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        String encoded = in.getStringOr("BoardGame", "");
        game = null;
        if (!encoded.isEmpty())
            try {
                game = BoardGame.decode(encoded);
            } catch (RuntimeException bad) {
                LogUtils.getLogger().warn("Discarding invalid board game at {}", worldPosition, bad);
                game = null;
            }
        if (getBlockState().getBlock() instanceof BoardGameBlock board && (game == null || game.position.kind != board.kind))
            game = new BoardGame(board.kind);
        Direction saved = Direction.byName(in.getStringOr("BoardFacing", "north"));
        facing = saved != null && saved.getAxis().isHorizontal() ? saved : Direction.NORTH;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (var reporter = new ProblemReporter.ScopedCollector(this::toString, LogUtils.getLogger())) {
            var out = TagValueOutput.createWithContext(reporter, registries);
            // Render payload does not disclose player UUIDs or duplicate match history.
            if (game != null) {
                out.putString("BoardGame", game.renderEncode());
            }
            out.putString("BoardFacing", facing.getName());
            return out.buildResult();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel && state.getBlock() instanceof ChabudaiBlock && game != null) {
            ItemStack drop = boardStack(game.position.kind);
            game = null;
            Block.popResource(level, pos, drop);
        }
        BoardGames.removed(this);
        displayUpdate = null;
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        displayUpdate = null;
        BoardGames.removed(this);
        super.setRemoved();
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityTicker<T> ticker(Level level, BlockEntityType<T> type) {
        if (level.isClientSide() || type != RDBlockEntityTypes.BOARD_GAME.value())
            return null;
        return (l, p, s, be) -> {
            if (l.getGameTime() % 10 == 0 && ((BoardGameBlockEntity) be).game() != null)
                BoardGames.tick((BoardGameBlockEntity) be);
        };
    }
}
