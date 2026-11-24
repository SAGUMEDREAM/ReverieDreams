package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import cc.thonly.reverie_dreams.server.player.BoundaryReturnComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoFeature;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * A one-use paper talisman that tears the boundary open.
 *
 * <ul>
 *     <li>Used outside Gensokyo: tear a rift that lasts {@link #PORTAL_LIFETIME}
 *     ticks and leads to a random, walkable spot inside the fixed Gensokyo map.</li>
 *     <li>Used inside Gensokyo: tear a rift back to the place the player stepped
 *     through from (or to the overworld spawn if there is no record).</li>
 * </ul>
 *
 * <p>The rift is two-way: a second rift is opened at the far end, so whoever walks
 * through can walk straight back for the whole three minutes. Every rift is a
 * {@link GapPortalEntity}, so chunk tickets, safe landing search, portal cooldown
 * and particles are shared with the gap ball.</p>
 *
 * <p>The talisman can only be used on a block: right-clicking empty air does nothing,
 * the rift opens against the face that was clicked.</p>
 */
public class BoundaryOfuda extends Item {
    /**
     * Three minutes: long enough that a whole group can walk through and back.
     */
    public static final int PORTAL_LIFETIME = 3 * 60 * 20;
    /**
     * Keep random landings away from the protected map border.
     */
    private static final int MAP_MARGIN = 64;
    private static final int DESTINATION_ATTEMPTS = 24;

    public BoundaryOfuda(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player user = context.getPlayer();
        if (user == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || !(user instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return InteractionResult.SUCCESS_SERVER;
        }
        ServerLevel sourceLevel = (ServerLevel) player.level();
        ItemStack stack = context.getItemInHand();

        ServerLevel destinationLevel;
        BlockPos destinationPos;
        boolean enteringGensokyo;

        if (sourceLevel.dimension().equals(RDBuiltinLevels.GENSOKYO)) {
            enteringGensokyo = false;
            BoundaryReturnComponent record = returnRecord(player);
            ResourceKey<Level> recordedDimension = parseDimension(record.getReturnDimension());
            ServerLevel recordedLevel = recordedDimension == null ? null : server.getLevel(recordedDimension);
            if (recordedLevel == null || recordedLevel.dimension().equals(RDBuiltinLevels.GENSOKYO)) {
                destinationLevel = server.overworld();
                destinationPos = landingArea(destinationLevel, destinationLevel.getRespawnData().pos());
            } else {
                destinationLevel = recordedLevel;
                destinationPos = landingArea(recordedLevel, record.getReturnPos());
            }
        } else {
            enteringGensokyo = true;
            if (!GensokyoFeature.isEnabled()) {
                player.sendSystemMessage(Component.translatable(
                        "item.reverie_dreams.boundary_ofuda.disabled"), false);
                return InteractionResult.SUCCESS_SERVER;
            }
            ServerLevel gensokyo = server.getLevel(RDBuiltinLevels.GENSOKYO);
            if (gensokyo == null) {
                player.sendSystemMessage(Component.translatable(
                        "item.reverie_dreams.boundary_ofuda.disabled"), false);
                return InteractionResult.SUCCESS_SERVER;
            }
            RandomSource random = player.getRandom();
            BlockPos destination = findRandomDestination(gensokyo, random);
            if (destination == null) {
                player.sendSystemMessage(Component.translatable(
                        "item.reverie_dreams.boundary_ofuda.no_destination"), false);
                return InteractionResult.SUCCESS_SERVER;
            }
            destinationLevel = gensokyo;
            destinationPos = destination;
        }

        // The talisman is pressed onto a block: the rift opens against the face
        // that was clicked, not somewhere in the air near the player.
        BlockPos portalPos = findPortalSpace(sourceLevel, context.getClickedPos(), context.getClickedFace());
        if (portalPos == null) {
            player.sendSystemMessage(Component.translatable(
                    "item.reverie_dreams.boundary_ofuda.no_space"), false);
            return InteractionResult.SUCCESS_SERVER;
        }

        // Two ends, one talisman: the rift against the clicked block and the rift
        // that leads back, so the passage works in both directions for three minutes.
        openRift(sourceLevel, portalPos, destinationLevel, destinationPos,
                dimensionName(destinationLevel.dimension()), player);
        openRift(destinationLevel, destinationPos, sourceLevel, portalPos,
                dimensionName(sourceLevel.dimension()), player);

        if (enteringGensokyo) {
            BoundaryReturnComponent record = returnRecord(player);
            record.setReturnDimension(sourceLevel.dimension().identifier().toString());
            record.setReturnPos(player.blockPosition());
            record.markDirty();
        }

        stack.consume(1, player);
        sourceLevel.playSound(null, portalPos, SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.translatable(
                "item.reverie_dreams.boundary_ofuda.opened"), false);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static BoundaryReturnComponent returnRecord(ServerPlayer player) {
        PlayerComponentManager manager = PlayerComponentManager.serverAccess();
        PlayerComponent<BoundaryReturnComponent> component =
                manager.getOrCreatePlayerComponent(player, BoundaryReturnComponent.class);
        return component.get();
    }

    private static ResourceKey<Level> parseDimension(String dimensionId) {
        if (dimensionId == null || dimensionId.isEmpty()) {
            return null;
        }
        Identifier id = Identifier.tryParse(dimensionId);
        return id == null ? null : ResourceKey.create(Registries.DIMENSION, id);
    }

    private static Component dimensionName(ResourceKey<Level> dimension) {
        if (dimension.equals(RDBuiltinLevels.GENSOKYO)) {
            return Component.translatable("item.reverie_dreams.boundary_ofuda.world_gensokyo");
        }
        if (dimension.equals(Level.OVERWORLD)) {
            return Component.translatable("item.reverie_dreams.boundary_ofuda.world_overworld");
        }
        return Component.translatable("item.reverie_dreams.boundary_ofuda.world_other");
    }

    private static void openRift(ServerLevel level, BlockPos pos, ServerLevel targetLevel,
                                 BlockPos targetPos, Component targetName, Player user) {
        // The far end is often in an unloaded chunk: touch it and pin it so the
        // rift starts ticking and keeping itself loaded right away.
        level.getChunkAt(pos);
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(pos), 3);

        GapPortalEntity portal = new GapPortalEntity(level, targetLevel.dimension(), targetPos, targetName);
        portal.setLifetime(PORTAL_LIFETIME);
        portal.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        portal.setYRot(user.getYRot());
        portal.setXRot(0.0F);
        level.addFreshEntity(portal);
    }

    /**
     * Makes sure the return end has somewhere to stand: the recorded spot first,
     * then two blocks above it, then the world spawn, then the surface.
     */
    private static BlockPos landingArea(ServerLevel level, BlockPos candidate) {
        level.getChunkAt(candidate);
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos pos = candidate.above(dy);
            if (isOpenLanding(level, pos)) {
                return pos;
            }
        }
        BlockPos respawn = level.getRespawnData().pos();
        level.getChunkAt(respawn);
        if (isOpenLanding(level, respawn)) {
            return respawn;
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
    }

    /**
     * Picks a random spot on land inside the map, far from the protected
     * border, where the traveller will not end up inside a block or a liquid.
     */
    private static BlockPos findRandomDestination(ServerLevel gensokyo, RandomSource random) {
        GensokyoMapData map = GensokyoMapLoader.get(GensokyoChunkGenerator.DEFAULT_MAP);
        int minX = map.originX() + MAP_MARGIN;
        int maxX = map.originX() + map.width() - MAP_MARGIN;
        int minZ = map.originZ() + MAP_MARGIN;
        int maxZ = map.originZ() + map.depth() - MAP_MARGIN;
        if (maxX <= minX || maxZ <= minZ) {
            return null;
        }

        for (int attempt = 0; attempt < DESTINATION_ATTEMPTS; attempt++) {
            int x = minX + random.nextInt(maxX - minX);
            int z = minZ + random.nextInt(maxZ - minZ);
            int surfaceY = map.heightAt(x, z);
            if (surfaceY == GensokyoMapData.NO_SURFACE) {
                continue;
            }
            // Water and lava are rejected before generating the chunk: the map
            // data already knows the surface material.
            BlockState surface = map.surfaceStateAt(x, z);
            if (surface.is(Blocks.WATER) || surface.is(Blocks.LAVA) || surface.is(Blocks.POWDER_SNOW)) {
                continue;
            }
            BlockPos pos = new BlockPos(x, surfaceY + 1, z);
            // Force generation so the checks below see real blocks (structures
            // are placed after the surface pass).
            gensokyo.getChunkAt(pos);
            if (!isOpenLanding(gensokyo, pos)) {
                continue;
            }
            gensokyo.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(pos), 2);
            return pos;
        }
        return null;
    }

    private static boolean isOpenLanding(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.isAir() || !ground.getFluidState().isEmpty()) {
            return false;
        }
        if (!ground.isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        // Two blocks of head room, nothing that would suffocate the player.
        for (int dy = 0; dy < 2; dy++) {
            if (!level.getBlockState(pos.above(dy)).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * The ofuda can only be used on a block: the rift opens against the clicked
     * face, or on top of the clicked block, never in the air beside the player.
     */
    private static BlockPos findPortalSpace(ServerLevel level, BlockPos clicked, Direction face) {
        List<BlockPos> candidates = List.of(
                clicked.relative(face),
                clicked.relative(face).above(),
                clicked.above(),
                clicked);
        for (BlockPos pos : candidates) {
            if (level.getBlockState(pos).canBeReplaced()
                    && level.getBlockState(pos.above()).canBeReplaced()) {
                return pos;
            }
        }
        return null;
    }
}
