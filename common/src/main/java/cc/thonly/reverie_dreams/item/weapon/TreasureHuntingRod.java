package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.mixin.accessor.BlockDisplayAccessor;
import cc.thonly.reverie_dreams.mixin.accessor.DisplayAccessor;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.util.OreDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreasureHuntingRod extends SwordItem {
    public static final ToolMaterial MATERIAL = new ToolMaterial(RDBlockTags.EMPTY, 300, 4.0f, 4.5f, 5, ItemTags.DIAMOND_TOOL_MATERIALS);

    /**
     * 水平扫描半径。
     */
    public static final int SCAN_RADIUS = 8;
    /**
     * 向上扫描高度（矿物大多在玩家脚下，向上少扫一些）。
     */
    public static final int SCAN_UP = 4;
    /**
     * 使用冷却（tick）。
     */
    public static final int COOLDOWN_TICKS = 35;
    /**
     * 一次扫描最多生成的透视标记数量。
     */
    public static final int MAX_MARKERS = 20;
    /**
     * 最近的一条矿脉最多标记的方块数量。
     */
    public static final int MAX_VEIN_MARKERS = 12;

    private static final String FIND_KEY = "message.reverie_dreams.treasure_hunting_rod.find";
    private static final String NOT_FOUND_KEY = "message.reverie_dreams.treasure_hunting_rod.not_found";
    private static final String SUMMARY_KEY = "message.reverie_dreams.treasure_hunting_rod.summary";
    private static final String DISTRIBUTION_KEY = "message.reverie_dreams.treasure_hunting_rod.distribution";
    private static final String DISTRIBUTION_ENTRY_KEY = "message.reverie_dreams.treasure_hunting_rod.distribution.entry";
    private static final String SEPARATOR_KEY = "message.reverie_dreams.treasure_hunting_rod.separator";

    /**
     * @deprecated 改用 {@link OreDetector#ORE_TAGS}
     */
    @Deprecated
    public static final List<TagKey<Block>> ORE_BLOCK_TAGS = OreDetector.ORE_TAGS;

    public TreasureHuntingRod(float attackDamage, float attackSpeed, Properties settings) {
        super(MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player) || world.isClientSide() || !player.isShiftKeyDown()) {
            return super.use(world, user, hand);
        }

        ItemStack stack = player.getItemInHand(hand);
        ItemCooldowns cooldown = player.getCooldowns();

        player.swing(hand);
        this.performScan(world, player);

        if (!player.hasInfiniteMaterials()) {
            stack.hurtWithoutBreaking(1, player);
        }
        cooldown.addCooldown(stack, COOLDOWN_TICKS);
        return InteractionResult.SUCCESS_SERVER;
    }

    private void performScan(Level world, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        Vec3 eye = player.position();
        List<BlockPos> ores = findOres(world, origin);

        if (ores.isEmpty()) {
            player.sendSystemMessage(Component.translatable(NOT_FOUND_KEY), false);
            playScanSound(world, player, false, 0);
            return;
        }

        ores.sort(Comparator.comparingDouble(pos -> eye.distanceToSqr(Vec3.atCenterOf(pos))));

        BlockPos nearest = ores.get(0);
        BlockState nearestState = world.getBlockState(nearest);
        int distance = (int) Math.round(Math.sqrt(eye.distanceToSqr(Vec3.atCenterOf(nearest))));

        List<List<BlockPos>> veins = groupVeins(ores);
        veins.forEach(vein -> vein.sort(Comparator.comparingDouble(pos -> eye.distanceToSqr(Vec3.atCenterOf(pos)))));
        veins.sort(Comparator.comparingDouble(vein -> eye.distanceToSqr(Vec3.atCenterOf(vein.get(0)))));

        // 聊天栏：最近矿石的相对坐标
        player.sendSystemMessage(Component.translatable(FIND_KEY,
                distance,
                nearest.getX() - origin.getX(),
                nearest.getY() - origin.getY(),
                nearest.getZ() - origin.getZ()
        ).append(" ").append(Component.translatable(nearestState.getBlock().getDescriptionId())), false);

        // 聊天栏：矿物分布
        player.sendSystemMessage(Component.translatable(DISTRIBUTION_KEY, buildDistribution(world, ores)), false);

        // 动作栏：最近矿脉简报
        player.sendSystemMessage(Component.translatable(SUMMARY_KEY,
                Component.translatable(nearestState.getBlock().getDescriptionId()),
                distance,
                veins.getFirst().size(),
                ores.size()), true);

        spawnMarkers(world, player, buildMarkerTargets(veins));
        playScanSound(world, player, true, distance);
    }

    private static List<BlockPos> findOres(Level world, BlockPos origin) {
        List<BlockPos> ores = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, SCAN_UP, SCAN_RADIUS))) {
            if (!world.isInWorldBounds(pos)) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !OreDetector.isOre(state)) {
                continue;
            }
            ores.add(pos.immutable());
        }
        return ores;
    }

    /**
     * 按 6 邻接把矿石聚成矿脉。
     */
    private static List<List<BlockPos>> groupVeins(List<BlockPos> ores) {
        Set<BlockPos> remaining = new HashSet<>(ores);
        List<List<BlockPos>> veins = new ArrayList<>();
        for (BlockPos start : ores) {
            if (!remaining.remove(start)) {
                continue;
            }
            List<BlockPos> vein = new ArrayList<>();
            Deque<BlockPos> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                vein.add(current);
                for (Direction direction : Direction.values()) {
                    BlockPos next = current.relative(direction);
                    if (remaining.remove(next)) {
                        queue.add(next);
                    }
                }
            }
            veins.add(vein);
        }
        return veins;
    }

    /**
     * 标记最近矿脉的方块，并为其它矿脉各标记最近的一块。
     */
    private static List<BlockPos> buildMarkerTargets(List<List<BlockPos>> veins) {
        List<BlockPos> targets = new ArrayList<>();
        List<BlockPos> nearestVein = veins.get(0);
        for (int i = 0; i < nearestVein.size() && targets.size() < MAX_VEIN_MARKERS; i++) {
            targets.add(nearestVein.get(i));
        }
        for (int i = 1; i < veins.size() && targets.size() < MAX_MARKERS; i++) {
            targets.add(veins.get(i).get(0));
        }
        return targets;
    }

    private static void spawnMarkers(Level world, ServerPlayer player, List<BlockPos> targets) {
        Map<BlockPos, OreEspEntity> existing = new HashMap<>();
        double range = SCAN_RADIUS + 4;
        AABB searchBox = AABB.ofSize(Vec3.atCenterOf(player.blockPosition()), range * 2.0, range * 2.0, range * 2.0);
        for (OreEspEntity marker : world.getEntitiesOfClass(OreEspEntity.class, searchBox)) {
            existing.put(marker.blockPosition(), marker);
        }

        long expireAt = world.getGameTime() + OreEspEntity.LIFETIME_TICKS;
        for (BlockPos pos : targets) {
            OreEspEntity marker = existing.get(pos);
            if (marker != null) {
                // 已经标记过：续期即可，避免同一格堆叠多个标记
                marker.setExpireAtGameTime(expireAt);
                continue;
            }
            BlockState state = world.getBlockState(pos);
            OreEspEntity newMarker = RDEntityTypes.ORE_ESP.value().create(world, EntitySpawnReason.EVENT);
            if (newMarker == null) {
                continue;
            }
            ((BlockDisplayAccessor) newMarker).reverie_dreams$setBlockState(state);
            ((DisplayAccessor) newMarker).reverie_dreams$setGlowColorOverride(OreDetector.colorFor(state) | 0xFF000000);
            newMarker.setGlowingTag(true);
            newMarker.setExpireAtGameTime(expireAt);
            newMarker.setPos(Vec3.atLowerCornerOf(pos));
            world.addFreshEntity(newMarker);
        }
    }

    private static Component buildDistribution(Level world, List<BlockPos> ores) {
        Map<Block, Integer> counts = new LinkedHashMap<>();
        for (BlockPos pos : ores) {
            counts.merge(world.getBlockState(pos).getBlock(), 1, Integer::sum);
        }
        List<Map.Entry<Block, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Comparator.<Map.Entry<Block, Integer>>comparingInt(Map.Entry::getValue).reversed());

        MutableComponent result = Component.empty();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result.append(Component.translatable(SEPARATOR_KEY));
            }
            Map.Entry<Block, Integer> entry = entries.get(i);
            result.append(Component.translatable(DISTRIBUTION_ENTRY_KEY,
                    Component.translatable(entry.getKey().getDescriptionId()), entry.getValue()));
        }
        return result;
    }

    private static void playScanSound(Level world, ServerPlayer player, boolean found, int distance) {
        float pitch = found ? Mth.clamp(1.6f - distance * 0.05f, 0.8f, 1.6f) : 0.6f;
        world.playSound(null, player.getX(), player.getEyeY(), player.getZ(),
                found ? SoundEvents.NOTE_BLOCK_PLING.value() : SoundEvents.NOTE_BLOCK_BASS.value(),
                SoundSource.PLAYERS, found ? 1.0f : 0.8f, pitch);
    }

    /**
     * @deprecated 改用 {@link OreDetector#isOre(BlockState)}
     */
    @Deprecated
    public static boolean isOre(BlockState blockState) {
        return OreDetector.isOre(blockState);
    }

}
