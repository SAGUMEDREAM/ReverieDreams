package cc.thonly.reverie_dreams.command.service;

import cc.thonly.reverie_dreams.command.CommandService;
import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.registry.content.GensokyoLandmarks;
import cc.thonly.reverie_dreams.util.command.PermissionPredicates;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Map;

/**
 * Teleports the executing player to a registered Gensokyo landmark.
 *
 * <p>The destination column is generated first so the search sees real blocks
 * (structures and trees are placed after the surface pass). Landing positions
 * require two air blocks above a sturdy, non-hazardous floor; when the exact
 * landmark column is occupied by a building, water or a tree, nearby columns
 * are tried in expanding rings. If none is safe, the highest block in the
 * landmark column is used as a last resort.</p>
 */
public final class TeleportLandmarkCommandService implements CommandService {
    private static final int SEARCH_RADIUS = 8;
    private static final int MAX_DESCENT = 32;

    private static final SuggestionProvider<CommandSourceStack> LANDMARK_SUGGESTIONS =
            (context, builder) -> {
                for (Map.Entry<Identifier, GensokyoLandmark> entry : GensokyoLandmarks.sortedEntries()) {
                    cc.thonly.reverie_dreams.util.command.FilteredSuggestions.resource(builder,entry.getKey());
                }
                return builder.buildFuture();
            };

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("teleport")
                .requires(PermissionPredicates.isGameMasters())
                .then(Commands.argument("id", IdentifierArgument.id())
                        .suggests(LANDMARK_SUGGESTIONS)
                        .executes(this::execute)
                );
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("command.touhou.teleport.not_player"));
            return 0;
        }

        ServerLevel gensokyo = source.getServer().getLevel(RDBuiltinLevels.GENSOKYO);
        if (gensokyo == null) {
            source.sendFailure(Component.translatable("command.touhou.teleport.no_level"));
            return 0;
        }

        Identifier requested = IdentifierArgument.getId(context, "id");
        GensokyoLandmark landmark = GensokyoLandmarks.get(requested).orElse(null);
        if (landmark == null) {
            source.sendFailure(Component.translatable("command.touhou.teleport.unknown", requested.toString()));
            return 0;
        }

        BlockPos landing = findLanding(gensokyo, landmark.x(), landmark.z());
        if (landing == null) {
            source.sendFailure(Component.translatable(
                    "command.touhou.teleport.no_safe_position", landmark.displayName()));
            return 0;
        }

        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.fallDistance = 0;
        player.setDeltaMovement(Vec3.ZERO);
        boolean teleported = player.teleportTo(
                gensokyo,
                landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D,
                EnumSet.noneOf(Relative.class),
                player.getYRot(), player.getXRot(), true
        );
        if (!teleported) {
            source.sendFailure(Component.translatable("command.touhou.teleport.failed"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "command.touhou.teleport.success",
                landmark.displayName(),
                landing.getX(), landing.getY(), landing.getZ()
        ), true);
        return 1;
    }

    /** Closest-first ring search around the landmark centre. */
    private static BlockPos findLanding(ServerLevel level, int centerX, int centerZ) {
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos landing = landingInColumn(level, centerX + dx, centerZ + dz);
                    if (landing != null) {
                        return landing;
                    }
                }
            }
        }
        return topOfLandmarkColumn(level, centerX, centerZ);
    }

    private static BlockPos topOfLandmarkColumn(ServerLevel level, int x, int z) {
        // WORLD_SURFACE includes leaves and structures that the safe search may skip.
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (y > level.getMaxY() - 2 || level.getBlockState(new BlockPos(x, y - 1, z)).isAir()) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    private static BlockPos landingInColumn(ServerLevel level, int x, int z) {
        BlockPos column = new BlockPos(x, 0, z);
        // Structures are placed after the surface pass; force generation first.
        level.getChunkAt(column);
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int bottom = Math.max(level.getMinY(), top - MAX_DESCENT);
        for (int y = top; y >= bottom; y--) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (isSafeLanding(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isSafeLanding(ServerLevel level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty() || !level.getBlockState(pos).isAir()) {
            return false;
        }
        // Two blocks of head room, nothing that would suffocate the player.
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.isAir() || !ground.getFluidState().isEmpty()) {
            return false;
        }
        if (!ground.isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        return !ground.is(Blocks.MAGMA_BLOCK)
                && !ground.is(Blocks.CACTUS)
                && !ground.is(Blocks.POWDER_SNOW)
                && !ground.is(Blocks.CAMPFIRE)
                && !ground.is(Blocks.SOUL_CAMPFIRE);
    }
}
