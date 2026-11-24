package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.dream.DreamGridLayout;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

/** Repairs the paired gaps left by the old projection on an administrator's request. */
public final class DreamGridCommandService implements CommandService {
    private static final int DEFAULT_RADIUS = 6;
    private static final int MAX_RADIUS = 8;
    private static final int LOWER_RED = 32, LOWER_BLUE = 64, UPPER_RED = 192, UPPER_BLUE = 224;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("dream_grid")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(operation("preview", false))
                .then(operation("repair", true));
    }

    private LiteralArgumentBuilder<CommandSourceStack> operation(String name, boolean repair) {
        return Commands.literal(name)
                .executes(context -> run(context.getSource(), DEFAULT_RADIUS, repair))
                .then(Commands.argument("chunk_radius", IntegerArgumentType.integer(0, MAX_RADIUS))
                        .executes(context -> run(context.getSource(), IntegerArgumentType.getInteger(context, "chunk_radius"), repair)));
    }

    private int run(CommandSourceStack source, int radius, boolean repair) {
        var level = source.getLevel();
        if (!level.dimension().equals(RDBuiltinLevels.DREAM_WORLD)) {
            source.sendFailure(Component.literal("Run this command in the Dream World."));
            return 0;
        }

        BlockPos center = BlockPos.containing(source.getPosition());
        int centerX = Math.floorDiv(center.getX(), 16), centerZ = Math.floorDiv(center.getZ(), 16);
        var red = RDBlocks.DREAM_RED_BLOCK.asBlock();
        var blue = RDBlocks.DREAM_BLUE_BLOCK.defaultBlockState();
        int blocks = 0, loadedChunks = 0;
        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                var chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                loadedChunks++;
                for (BlockPos low : DreamGridLayout.blocksInChunk(cx, cz, LOWER_BLUE, LOWER_BLUE + 1)) {
                    BlockPos high = low.atY(UPPER_BLUE);
                    if (!DreamGridLayout.isGridPosition(high) || !level.getWorldBorder().isWithinBounds(low)) continue;
                    // A legacy projection cut both blue layers at the same X/Z. Preserve single-layer
                    // gaps, and require both red blocks as evidence that this column used the old grid.
                    if (!chunk.getBlockState(low.atY(LOWER_RED)).is(red) || !chunk.getBlockState(low.atY(UPPER_RED)).is(red)
                            || !chunk.getBlockState(low).isAir() || !chunk.getBlockState(high).isAir()) continue;
                    if (!repair) {
                        blocks += 2;
                        continue;
                    }
                    int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
                    if (level.setBlock(low, blue, flags)) blocks++;
                    if (level.setBlock(high, blue, flags)) blocks++;
                }
            }
        }
        String action = repair ? "Restored " : "Preview: can restore ";
        String result = action + blocks + " blue grid blocks in " + loadedChunks + " loaded chunks.";
        source.sendSuccess(() -> Component.literal(result), repair && blocks > 0);
        return blocks;
    }
}
