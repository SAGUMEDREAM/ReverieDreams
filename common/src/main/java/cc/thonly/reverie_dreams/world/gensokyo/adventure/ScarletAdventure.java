package cc.thonly.reverie_dreams.world.gensokyo.adventure;

import cc.thonly.reverie_dreams.entity.Goblin;
import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.item.AncientIronEquipment;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.data.gensokyo.GensokyoAdventure;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Main-thread, player-activated encounters. DH/background generation never spawns these mobs.
 */
public final class ScarletAdventure {
    public static final String FRIENDLY = "rd_scarlet_rescuable";
    private static final String POINT = "rd_scarlet_point:";
    private static final long RESTOCK = 20 * 60 * 18;
    private final ServerLevel level;
    private final GensokyoMapData.Anchor anchor;
    private final GensokyoAdventure layout;
    private final List<Treasure> treasures;
    private final ScarletAdventureState state;
    private long nextTick;
    private int spawnBudget;

    private enum TreasureTier {
        SUPPLY(0xFFE480, 16, "Mansion supplies"),
        GUARD(0xFFAA55, 16, "Mansion guard cache"),
        SECRET(0xE2B7FF, 10, "Hidden mansion treasure"),
        HEART(0xFF677F, 10, "Mansion grand treasure"),
        LIBRARY(0x83DBFF, 16, "Great Library · Elemental Research");

        final int range;
        final DustParticleOptions marker;
        final ResourceKey<LootTable> loot;
        final Component hint;

        TreasureTier(int color, int range, String fallback) {
            String tier = name().toLowerCase(Locale.ROOT);
            this.range = range;
            this.marker = new DustParticleOptions(color, 1.35F);
            this.loot = ScarletChestLoot.key(tier);
            this.hint = Component.translatableWithFallback("adventure.reverie_dreams.scarlet.treasure." + tier, fallback)
                    .withColor(color);
        }
    }

    private record Treasure(String id, BlockPos pos, String block, boolean newChest, TreasureTier tier) {
    }

    public ScarletAdventure(ServerLevel level, GensokyoMapData map) {
        this.level = level;
        anchor = map.anchors().stream().filter(a -> Identifier.parse("reverie_dreams:gensokyo/scarlet_devil_mansion").equals(a.template())).findFirst().orElse(null);
        layout = anchor == null ? null : configuration();
        List<Treasure> points = new ArrayList<>();
        if (layout != null) {
            for (var point : layout.treasures()) {
                points.add(new Treasure(point.id(), local(point.pos()), point.block(), point.newChest(),
                        TreasureTier.valueOf(point.tier().toUpperCase(Locale.ROOT))));
            }
        }
        treasures = List.copyOf(points);
        state = level.getDataStorage().computeIfAbsent(ScarletAdventureState.TYPE);
    }

    public boolean active() {
        return anchor != null && layout != null;
    }

    /**
     * Stable point IDs also belong to the structure baseline, independently of their coordinates.
     */
    public static Map<String, BlockPos> maintenancePoints(ServerLevel level, GensokyoMapData.Anchor anchor) {
        if (!Identifier.parse("reverie_dreams:gensokyo/scarlet_devil_mansion").equals(anchor.template()))
            return Map.of();
        var config = configuration();
        if (config == null) {
            return Map.of();
        }
        Map<String, BlockPos> points = new HashMap<>();
        for (var point : config.treasures()) {
            points.put("treasures/" + point.id(), transform(anchor, point.pos()));
        }
        for (var point : config.encounters()) {
            points.put("encounters/" + point.id(), transform(anchor, point.pos()));
        }
        return Map.copyOf(points);
    }

    public static Set<BlockPos> newTreasurePositions(ServerLevel level, GensokyoMapData.Anchor anchor) {
        if (!Identifier.parse("reverie_dreams:gensokyo/scarlet_devil_mansion").equals(anchor.template())) {
            return Set.of();
        }
        var config = configuration();
        if (config == null) {
            return Set.of();
        }
        Set<BlockPos> result = new HashSet<>();
        for (var point : config.treasures()) {
            if (point.newChest()) {
                result.add(transform(anchor, point.pos()));
            }
        }
        return Set.copyOf(result);
    }

    private static GensokyoAdventure configuration() {
        return BuiltInRegistryProviders.GENSOKYO_ADVENTURE
                .get(Identifier.parse("reverie_dreams:scarlet_devil_mansion"))
                .map(entry -> entry.value()).orElse(null);
    }

    private static BlockPos transform(GensokyoMapData.Anchor anchor, BlockPos pos) {
        return StructureTemplate.transform(pos, Mirror.valueOf(anchor.mirror().toUpperCase(Locale.ROOT)),
                Rotation.valueOf(anchor.rotation().toUpperCase(Locale.ROOT)), BlockPos.ZERO).offset(anchor.pos());
    }

    private BlockPos local(BlockPos p) {
        return StructureTemplate.transform(p, Mirror.valueOf(anchor.mirror().toUpperCase(Locale.ROOT)), Rotation.valueOf(anchor.rotation().toUpperCase(Locale.ROOT)), BlockPos.ZERO).offset(anchor.pos());
    }

    private boolean ready(BlockPos p, boolean needsNeighborSupport) {
        var generator = (GensokyoChunkGenerator) level.getChunkSource().getGenerator();
        return generator.authoredStructures(level).readyForAdventure(p, needsNeighborSupport);
    }

    public void tick() {
        long now = level.getGameTime();
        if (!active())
            return;
        if (now >= nextTick)
            treasures.forEach(this::installTreasure);
        if (now % 5 == 0)
            showTreasureHints(now % 10 == 0);
        if (now < nextTick)
            return;
        nextTick = now + 20;
        migrateDisplays();
        if (level.getDifficulty() == Difficulty.PEACEFUL)
            return;
        spawnBudget = 4;
        // Keep the nearby combat route responsive even when many prison cells become eligible.
        for (String kind : List.of("maid", "goblin"))
            for (var point : layout.encounters()) {
                if (point.kind().equals(kind))
                    encounter(point, now);
            }
    }

    private void installTreasure(Treasure treasure) {
        String key = "box:" + treasure.id();
        BlockPos p = treasure.pos();
        if (!ready(p, false))
            return;
        boolean assigned = state.has(key);
        if (!assigned && treasure.newChest() && level.getBlockState(p).isAir()
                && level.getBlockState(p.above()).isAir()
                && level.getBlockState(p.below()).isCollisionShapeFullBlock(level, p.below()))
            level.setBlock(p, Blocks.CHEST.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS | net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
        if (!BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString().equals(treasure.block())
                || !(level.getBlockEntity(p) instanceof RandomizableContainerBlockEntity chest))
            return;
        if (!assigned) {
            // Existing inventories and other loot tables take precedence over the adventure reward.
            if (chest.getLootTable() == null && chest.getCustomName() == null && chest.isEmpty()) {
                chest.setLootTable(treasure.tier().loot);
                chest.setLootTableSeed(level.getSeed() ^ p.asLong() ^ key.hashCode());
                chest.setChanged();
            }
            state.put(key, "assigned");
        }
    }

    private void showTreasureHints(boolean checkAim) {
        for (var player : level.players()) {
            if (player.isSpectator() || !player.isAlive())
                continue;
            for (var treasure : treasures) {
                BlockPos pos = treasure.pos();
                Vec3 center = Vec3.atCenterOf(pos);
                TreasureTier tier = treasure.tier();
                double distance = player.distanceToSqr(center);
                if (distance > tier.range * tier.range || !ready(pos, false)
                        || !(level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)
                        || !tier.loot.equals(chest.getLootTable()))
                    continue;
                var hit = level.clip(new ClipContext(player.getEyePosition(), center, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(pos))
                    continue;
                Vec3 marker = hit.getLocation().relative(hit.getDirection(), .09);
                level.sendParticles(player, tier.marker, false, false, marker.x, marker.y, marker.z, 7, .09, .12, .09, 0);
                if (checkAim && distance <= 6 * 6 && player.pick(6, 1, false) instanceof BlockHitResult aim
                        && aim.getType() == HitResult.Type.BLOCK && aim.getBlockPos().equals(pos))
                    player.sendSystemMessage(tier.hint, true);
            }
        }
    }

    private void migrateDisplays() {
        for (var position : layout.armorStands()) {
            Vec3 p = position.add(anchor.pos().getX(), anchor.pos().getY(), anchor.pos().getZ());
            for (ArmorStand stand : level.getEntitiesOfClass(ArmorStand.class, new AABB(p, p).inflate(.1))) {
                if (stand.entityTags().contains("rd_ancient_iron_v1"))
                    continue;
                stand.setShowArms(true);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    var old = stand.getItemBySlot(slot);
                    var converted = AncientIronEquipment.convertDisplay(old, stand.getUUID().getLeastSignificantBits() ^ slot.ordinal());
                    if (converted != old)
                        stand.setItemSlot(slot, converted);
                }
                stand.addTag("rd_ancient_iron_v1");
            }
        }
    }

    private void encounter(GensokyoAdventure.Encounter q, long now) {
        String id = q.id();
        BlockPos p = local(q.pos());
        boolean maid = q.kind().equals("maid"), friendly = q.friendly();
        if (!maid && state.has("retired:" + id))
            return;
        if (!ready(p, !maid))
            return;
        double activationDistance = maid ? 56 : 48;
        double verticalDistance = maid ? 5 : 3.5;
        List<? extends Player> players = level.players().stream()
                .filter(v -> v.isAlive() && !v.isSpectator() && Math.abs(v.getY() - p.getY()) < verticalDistance
                        && v.distanceToSqr(Vec3.atCenterOf(p)) < activationDistance * activationDistance)
                .toList();
        if (maid) {
            maidEncounter(id, p, players, now);
            return;
        }
        Entity live = boundEntity(id);
        String binding = state.get("mob:" + id);
        if (live instanceof Mob mob) {
            if (mob instanceof TamableAnimal pet && pet.isTame()) {
                retire(id);
                mob.setNoAi(false);
                return;
            }
            mob.setNoAi(players.isEmpty());
            if (breached(q) || !room(q).contains(mob.position())) {
                mob.setNoAi(false);
                if (friendly) {
                    AABB room = room(q);
                    if (!room.contains(mob.position())) {
                        retire(id);
                        mob.setCustomName(Component.translatable("adventure.reverie_dreams.scarlet.freed").withStyle(ChatFormatting.GREEN));
                        if (!players.isEmpty()) {
                            Player rescuer = players.getFirst();
                            ItemStack reward = new ItemStack(RDItems.COPPER_COIN.asItem(), 3);
                            if (!rescuer.getInventory().add(reward))
                                rescuer.drop(reward, false);
                            rescuer.sendSystemMessage(Component.translatable("adventure.reverie_dreams.scarlet.thanks"));
                        }
                    } else if (!players.isEmpty()) {
                        var rescuer = players.getFirst();
                        mob.getNavigation().moveTo(rescuer.getX(), rescuer.getY(), rescuer.getZ(), 1);
                    }
                } else
                    retire(id);
            }
            return;
        }
        // An unloaded or wandered-off entity keeps ownership until its actual death is recorded.
        if (!binding.isEmpty())
            return;
        if (breached(q)) {
            retire(id);
            return;
        }
        if (players.isEmpty()) {
            if (!state.has("away:" + id))
                state.put("away:" + id, now);
            return;
        }
        long away = state.number("away:" + id);
        boolean first = !state.has("generation:" + id);
        if (!first && (away == 0 || now - away < RESTOCK)) {
            state.remove("away:" + id);
            return;
        }
        if (players.stream().anyMatch(v -> v.distanceToSqr(Vec3.atCenterOf(p)) < 10 * 10))
            return;
        spawnEncounter(id, p, false, friendly);
    }

    private Entity boundEntity(String id) {
        String binding = state.get("mob:" + id);
        if (!binding.isEmpty()) {
            try {
                return level.getEntity(UUID.fromString(binding));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private void maidEncounter(String id, BlockPos p, List<? extends Player> players, long now) {
        String second = id + "/2";
        for (String member : List.of(id, second)) {
            if (boundEntity(member) instanceof MaidYousei maid && maid.isAlive()) {
                if (maid.isTame()) {
                    retire(member);
                    maid.setNoAi(false);
                } else {
                    maid.setNoAi(players.isEmpty());
                }
            }
        }
        if (state.has("retired:" + id))
            return;
        boolean primaryBound = state.has("mob:" + id);
        boolean secondaryBound = state.has("mob:" + second);
        String pairKey = "maid_pair:" + id;
        String secondWaveKey = "maid_second_wave:" + id;
        if (!primaryBound && !secondaryBound) {
            if (players.isEmpty()) {
                if (!state.has("away:" + id))
                    state.put("away:" + id, now);
                return;
            }
            long away = state.number("away:" + id);
            boolean first = !state.has("generation:" + id);
            if (!first && (away == 0 || now - away < RESTOCK)) {
                state.remove("away:" + id);
                return;
            }
            if (!maidSpawnAllowed(p, players) || !spawnEncounter(id, p, true, false))
                return;
            state.put(pairKey, level.getRandom().nextBoolean() ? 2 : 1);
        } else if (!state.has(pairKey)) {
            // Existing saves keep their original maid and choose a group size once.
            state.put(pairKey, level.getRandom().nextBoolean() ? 2 : 1);
        }
        long wave = state.number("generation:" + id);
        if (!state.has("mob:" + id) || secondaryBound || state.has("retired:" + second)
                || state.number(pairKey) != 2 || state.number(secondWaveKey) == wave || !maidSpawnAllowed(p, players))
            return;
        for (int distance = 1; distance <= 3; distance++) {
            for (BlockPos candidate : BlockPos.withinManhattan(p, distance, 0, distance)) {
                if (candidate.distManhattan(p) != distance || !ready(candidate, false) || !maidSpawnAllowed(candidate, players))
                    continue;
                if (spawnEncounter(second, candidate, true, false)) {
                    state.put(secondWaveKey, wave);
                    return;
                }
            }
        }
    }

    private boolean maidSpawnAllowed(BlockPos p, List<? extends Player> players) {
        return spawnBudget > 0 && !players.isEmpty()
                && players.stream().noneMatch(v -> v.distanceToSqr(Vec3.atCenterOf(p)) < 10 * 10)
                && level.getEntitiesOfClass(MaidYousei.class,
                        new AABB(p).inflate(12, 4, 12), Entity::isAlive).size() < 4;
    }

    private boolean spawnEncounter(String id, BlockPos p, boolean maid, boolean friendly) {
        if (spawnBudget <= 0 || !level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir())
            return false;
        var floor = level.getBlockState(p.below());
        if (maid ? floor.getCollisionShape(level, p.below()).isEmpty() : !floor.isCollisionShapeFullBlock(level, p.below()))
            return false;
        Mob mob = (Mob) (maid ? RDEntityTypes.MAID_YOUSEI.value() : RDEntityTypes.GOBLIN.value()).create(level, EntitySpawnReason.STRUCTURE);
        if (mob == null)
            return false;
        long generation = state.number("generation:" + id) + 1;
        UUID uuid = UUID.nameUUIDFromBytes((level.getSeed() + ":" + id + ":" + generation).getBytes(StandardCharsets.UTF_8));
        mob.setUUID(uuid);
        mob.snapTo(p.getX() + .5, p.getY(), p.getZ() + .5, 0, 0);
        mob.addTag(POINT + id);
        mob.setPersistenceRequired();
        if (!level.noCollision(mob) || (maid && !level.getEntitiesOfClass(LivingEntity.class,
                mob.getBoundingBox(), Entity::isAlive).isEmpty()))
            return false;
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.STRUCTURE, null);
        if (mob instanceof Goblin goblin) {
            goblin.getInventory().setMainHand(friendly ? ItemStack.EMPTY : new ItemStack(Items.STONE_PICKAXE));
            goblin.getInventory().setOffHand(ItemStack.EMPTY);
        }
        if (friendly)
            mob.addTag(FRIENDLY);
        mob.setCustomName(Component.translatable(friendly ? "adventure.reverie_dreams.scarlet.rescuable" : maid ? MaidYousei.SCARLET_NAME : "adventure.reverie_dreams.scarlet.prisoner").withStyle(friendly ? ChatFormatting.GREEN : ChatFormatting.RED));
        mob.setCustomNameVisible(friendly);
        if (level.addFreshEntity(mob)) {
            spawnBudget--;
            state.put("mob:" + id, uuid.toString());
            state.put("generation:" + id, generation);
            state.remove("away:" + id);
            return true;
        }
        return false;
    }

    private boolean breached(GensokyoAdventure.Encounter q) {
        for (var door : q.doors()) {
            BlockPos p = local(door);
            BlockState s = level.getBlockState(p);
            if (!s.is(Blocks.IRON_DOOR) || s.getValue(DoorBlock.OPEN))
                return true;
        }
        for (var b : q.barriers()) {
                BlockPos p = local(b.pos());
                if (!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString().equals(b.block()))
                    return true;
            }
        return false;
    }

    private AABB room(GensokyoAdventure.Encounter q) {
        var box = q.bounds();
        BlockPos a = local(new BlockPos(box.get(0), box.get(1), box.get(2)));
        BlockPos b = local(new BlockPos(box.get(3), box.get(4), box.get(5)));
        return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
    }

    private void retire(String id) {
        state.put("retired:" + id, "1");
        state.remove("mob:" + id);
    }

    public void death(LivingEntity entity) {
        for (String tag : entity.entityTags())
            if (tag.startsWith(POINT)) {
                String id = tag.substring(POINT.length());
                if (!entity.getUUID().toString().equals(state.get("mob:" + id)))
                    continue;
                state.remove("mob:" + id);
                state.remove("away:" + id);
                if (entity.entityTags().contains(FRIENDLY))
                    retire(id);
            }
    }
}

