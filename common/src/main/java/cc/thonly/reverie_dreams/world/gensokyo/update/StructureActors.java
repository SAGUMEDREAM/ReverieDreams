package cc.thonly.reverie_dreams.world.gensokyo.update;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Stable ownership; structural edits never deserialize template inventory over a live NPC.
 */
public final class StructureActors {
    public static final String LANDMARK = "rd_structure_landmark:";
    public static final String ACTOR = "rd_structure_actor:";
    private final ServerLevel level;
    private final StructureUpdateStorage storage;
    public boolean dirty;

    public StructureActors(ServerLevel level, StructureUpdateStorage storage) {
        this.level = level;
        this.storage = storage;
    }

    public boolean maySpawn(String landmark, String actor) {
        return !storage.actors().has(landmark + "|" + actor);
    }

    public void claim(String landmark, String actor, Entity entity) {
        entity.addTag(LANDMARK + landmark);
        entity.addTag(ACTOR + actor);
        String key = landmark + "|" + actor;
        JsonObject old = storage.actors().has(key) ? storage.actors().getAsJsonObject(key) : null;
        if (old != null && !old.get("uuid").getAsString().equals(entity.getUUID().toString()))
            throw new IllegalStateException("Duplicate actor identity " + key);
        JsonObject value = new JsonObject();
        value.addProperty("uuid", entity.getUUID().toString());
        value.addProperty("state", "alive");
        value.addProperty("dimension", entity.level().dimension().identifier().toString());
        value.addProperty("chunk", entity.chunkPosition().pack());
        if (!value.equals(old)) {
            storage.actors().add(key, value);
            dirty = true;
        }
    }

    public void removed(Entity entity, Entity.RemovalReason reason) {
        String landmark = null, actor = null;
        for (String tag : entity.entityTags()) {
            if (tag.startsWith(LANDMARK))
                landmark = tag.substring(LANDMARK.length());
            if (tag.startsWith(ACTOR))
                actor = tag.substring(ACTOR.length());
        }
        if (landmark == null || actor == null)
            return;
        String key = landmark + "|" + actor;
        if (!storage.actors().has(key))
            return;
        JsonObject value = storage.actors().getAsJsonObject(key);
        if (!value.get("uuid").getAsString().equals(entity.getUUID().toString()))
            return;
        value.addProperty("state", reason.shouldDestroy() ? "retired" : "unloaded");
        value.addProperty("dimension", entity.level().dimension().identifier().toString());
        value.addProperty("chunk", entity.chunkPosition().pack());
        dirty = true;
    }

    private Entity find(String landmark, StructureBlueprint.Actor actor) {
        String key = landmark + "|" + actor.id();
        if (storage.actors().has(key)) {
            UUID uuid = UUID.fromString(storage.actors().getAsJsonObject(key).get("uuid").getAsString());
            for (ServerLevel world : level.getServer().getAllLevels()) {
                Entity e = world.getEntity(uuid);
                if (e != null)
                    return e;
            }
            return null;
        }
        Entity e = level.getEntity(actor.uuid());
        if (e == null)
            e = level.getEntity(actor.legacyUuid());
        return e;
    }

    public void adopt(StructureBlueprint blueprint) {
        for (var actor : blueprint.actors()) {
            Entity e = find(blueprint.id(), actor);
            if (e != null)
                claim(blueprint.id(), actor.id(), e);
        }
    }

    public void adoptLegacy(StructureBlueprint blueprint) {
        adopt(blueprint);
        // An unversioned, already-generated building gives no proof that a missing display
        // was never created. Treat it as consumed instead of restocking its equipment.
        for (var actor : blueprint.actors()) {
            String key = blueprint.id() + "|" + actor.id();
            if (storage.actors().has(key))
                continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("uuid", actor.uuid().toString());
            entry.addProperty("state", "legacy_missing");
            entry.addProperty("dimension", level.dimension().identifier().toString());
            storage.actors().add(key, entry);
            dirty = true;
        }
    }

    public static boolean playerOwned(Entity entity) {
        return entity instanceof TamableAnimal pet && (pet.isTame() || pet.getOwner() != null)
                || entity instanceof BaseNPCLikeEntity npc && !npc.getNPCOwner().isBlank();
    }

    public static boolean occupiedDisplay(Entity e) {
        if (e instanceof ItemFrame frame && !frame.getItem().isEmpty())
            return true;
        if (e instanceof ArmorStand stand)
            for (EquipmentSlot slot : EquipmentSlot.values())
                if (!stand.getItemBySlot(slot).isEmpty())
                    return true;
        return false;
    }

    public CompoundTag capture(StructureBlueprint old, StructureBlueprint target) {
        if (old != null)
            for (var point : old.points().entrySet())
                if (point.getKey().startsWith("treasures/") && !Objects.equals(point.getValue(), target.points().get(point.getKey())))
                    throw new IllegalStateException("Moving/removing an existing treasure requires an inventory migration adapter: " + point.getKey());
        if (old != null)
            adopt(old);
        adopt(target);
        Map<String, StructureBlueprint.Actor> wanted = new HashMap<>();
        for (var a : target.actors())
            wanted.put(a.id(), a);
        if (old != null)
            for (var a : old.actors()) {
                Entity e = find(old.id(), a);
                if (e == null)
                    continue;
                if (!wanted.containsKey(a.id()) && (e instanceof Mob || occupiedDisplay(e)))
                    throw new IllegalStateException("Removed actor needs an explicit relocation adapter: " + a.id());
                if (wanted.containsKey(a.id()) && !a.nbt().getStringOr("id", "").equals(wanted.get(a.id()).nbt().getStringOr("id", "")))
                    throw new IllegalStateException("Actor type migration needs an adapter: " + a.id());
            }
        ListTag values = new ListTag();
        Set<UUID> seen = new HashSet<>();
        AABB region = new AABB(target.bounds().minX() - 2, target.bounds().minY() - 2, target.bounds().minZ() - 2, target.bounds().maxX() + 3, target.bounds().maxY() + 3, target.bounds().maxZ() + 3);
        if (old != null)
            region = region.minmax(new AABB(old.bounds().minX() - 2, old.bounds().minY() - 2, old.bounds().minZ() - 2, old.bounds().maxX() + 3, old.bounds().maxY() + 3, old.bounds().maxZ() + 3));
        for (Entity e : level.getEntities((Entity) null, region)) {
            boolean owned = e.entityTags().contains(LANDMARK + target.id()) || target.id().equals("reverie_dreams:scarlet_devil_mansion") && e.entityTags().stream().anyMatch(t -> t.startsWith("rd_scarlet_point:"));
            if (!owned || !e.isAlive())
                continue;
            if (playerOwned(e))
                throw new IllegalStateException("Player-owned actor is still in the update area: " + e.getUUID());
            if (!seen.add(e.getUUID()))
                continue;
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            e.save(output);
            CompoundTag tag = output.buildResult();
            tag.putString("update_uuid", e.getUUID().toString());
            tag.putBoolean("update_no_gravity", e.isNoGravity());
            if (e instanceof Mob mob)
                tag.putBoolean("update_no_ai", mob.isNoAi());
            values.add(tag);
        }
        CompoundTag root = new CompoundTag();
        root.putInt("schema", 1);
        root.put("actors", values);
        root.putString("bindings", storage.actors().toString());
        return root;
    }

    public void frozen(CompoundTag saved, boolean freeze) {
        for (Tag t : saved.getListOrEmpty("actors")) {
            CompoundTag tag = (CompoundTag) t;
            String uuid = tag.getStringOr("update_uuid", "");
            Entity e = level.getEntity(UUID.fromString(uuid));
            if (!freeze && e == null)
                for (ServerLevel other : level.getServer().getAllLevels()) {
                    e = other.getEntity(UUID.fromString(uuid));
                    if (e != null)
                        break;
                }
            if (!freeze) {
                JsonObject flags = new JsonObject();
                flags.addProperty("gravity", tag.getBooleanOr("update_no_gravity", false));
                flags.addProperty("ai", tag.getBooleanOr("update_no_ai", false));
                flags.addProperty("invulnerable", tag.getBooleanOr("Invulnerable", false));
                storage.ledger.getAsJsonObject("thaw").add(uuid, flags);
                dirty = true;
                if (e != null)
                    loaded(e);
                continue;
            }
            if (e == null)
                continue;
            e.setNoGravity(true);
            e.setInvulnerable(true);
            e.setDeltaMovement(Vec3.ZERO);
            if (e instanceof Mob mob) {
                mob.setNoAi(true);
                mob.getNavigation().stop();
            }
        }
    }

    public void loaded(Entity entity) {
        JsonObject pending = storage.ledger.getAsJsonObject("thaw");
        String uuid = entity.getUUID().toString();
        if (!pending.has(uuid))
            return;
        JsonObject flags = pending.getAsJsonObject(uuid);
        entity.setNoGravity(flags.get("gravity").getAsBoolean());
        entity.setInvulnerable(flags.has("invulnerable") && flags.get("invulnerable").getAsBoolean());
        if (entity instanceof Mob mob)
            mob.setNoAi(flags.get("ai").getAsBoolean());
        // Keep the recovery flags until the restored live entity has actually been saved.
    }

    public void acknowledgeThawed() {
        acknowledgeThawed(false);
    }

    public void acknowledgeThawed(boolean allLevels) {
        JsonObject pending = storage.ledger.getAsJsonObject("thaw");
        for (String uuid : new ArrayList<>(pending.keySet())) {
            boolean saved = level.getEntity(UUID.fromString(uuid)) != null;
            if (allLevels && !saved)
                for (ServerLevel other : level.getServer().getAllLevels())
                    if (other.getEntity(UUID.fromString(uuid)) != null) {
                        saved = true;
                        break;
                    }
            if (saved) {
                pending.remove(uuid);
                dirty = true;
            }
        }
    }

    public void reconcile(StructureBlueprint old, StructureBlueprint target) {
        Map<String, StructureBlueprint.Actor> prior = new HashMap<>();
        if (old != null)
            for (var a : old.actors())
                prior.put(a.id(), a);
        Set<String> desired = new HashSet<>();
        for (var a : target.actors()) {
            desired.add(a.id());
            Entity live = find(target.id(), a);
            String key = target.id() + "|" + a.id();
            if (live != null) {
                claim(target.id(), a.id(), live);
                if (playerOwned(live) || live.level() != level)
                    continue;
                var previous = prior.get(a.id());
                // A moved-away NPC belongs to the player/world, not to its original spawn coordinate.
                if (live instanceof LivingEntity && previous != null && live.position().distanceToSqr(previous.pos()) > 64 * 64)
                    continue;
                if (previous != null && !previous.pos().equals(a.pos())) {
                    if (live instanceof Mob && !safe(a.pos()))
                        throw new IllegalStateException("No safe NPC destination for " + a.id());
                    live.snapTo(a.pos().x, a.pos().y, a.pos().z, live.getYRot(), live.getXRot());
                }
                if (live instanceof BaseNPCLikeEntity npc && old != null)
                    for (var point : old.points().entrySet())
                        if (point.getValue().equals(npc.getWorkingPos()) && target.points().containsKey(point.getKey())) {
                            npc.setWorkingPos(target.points().get(point.getKey()));
                            npc.setSeatUUID("");
                        }
                if (live instanceof BlockAttachedEntity attached && !attached.survives())
                    throw new IllegalStateException("Missing attachment support for " + a.id());
                if (live instanceof Mob && !safe(live.position()))
                    throw new IllegalStateException("NPC destination was obstructed: " + a.id());
                continue;
            }
            // Missing from the loaded level is not proof of death. Never clone unloaded/recruited actors.
            if (storage.actors().has(key))
                continue;
            var created = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), a.nbt().copy()), level, EntitySpawnReason.STRUCTURE);
            Entity entity = created.orElseThrow(() -> new IllegalStateException("Cannot create actor " + a.id()));
            entity.setUUID(a.uuid());
            var rotation = net.minecraft.world.level.block.Rotation.valueOf(a.nbt().getStringOr("reverie_structure_rotation", "NONE"));
            var mirror = net.minecraft.world.level.block.Mirror.valueOf(a.nbt().getStringOr("reverie_structure_mirror", "NONE"));
            float yaw = entity.rotate(rotation);
            yaw += entity.mirror(mirror) - entity.getYRot();
            entity.snapTo(a.pos().x, a.pos().y, a.pos().z, yaw, entity.getXRot());
            if (entity instanceof Mob && !safe(a.pos()))
                throw new IllegalStateException("Unsafe new NPC spawn " + a.id());
            if (entity instanceof BlockAttachedEntity attached && !attached.survives())
                throw new IllegalStateException("Unsafe attachment spawn " + a.id());
            if (!level.getEntities(entity, entity.getBoundingBox().inflate(.05), e -> e.getType() == entity.getType() && e.position().distanceToSqr(a.pos()) < .01).isEmpty())
                throw new IllegalStateException("Ambiguous unowned entity at " + a.id());
            if (!level.addFreshEntity(entity))
                throw new IllegalStateException("Actor UUID collision " + a.id());
            claim(target.id(), a.id(), entity);
        }
        if (old != null)
            for (var a : old.actors())
                if (!desired.contains(a.id())) {
                    Entity live = find(old.id(), a);
                    if (live != null) {
                        if (live instanceof Mob || occupiedDisplay(live))
                            throw new IllegalStateException("Protected removed actor " + a.id());
                        live.discard();
                    }
                }
    }

    private boolean safe(Vec3 p) {
        BlockPos pos = BlockPos.containing(p);
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty();
    }

    public void reconcilePoints(CompoundTag saved, StructureBlueprint old, StructureBlueprint target) {
        for (Tag t : saved.getListOrEmpty("actors")) {
            CompoundTag tag = (CompoundTag) t;
            Entity e = level.getEntity(UUID.fromString(tag.getStringOr("update_uuid", "")));
            if (!(e instanceof Mob) || !e.isAlive() || playerOwned(e))
                continue;
            if (old != null)
                for (String marker : e.entityTags())
                    if (marker.startsWith("rd_scarlet_point:")) {
                        String point = marker.substring("rd_scarlet_point:".length());
                        String id = "encounters/" + (point.endsWith("/2") ? point.substring(0, point.length() - 2) : point);
                        BlockPos before = old.points().get(id), after = target.points().get(id);
                        if (before != null && after == null)
                            throw new IllegalStateException("Removed occupied encounter needs a relocation adapter: " + id);
                        if (before != null && after != null && !before.equals(after) && e.position().distanceToSqr(Vec3.atBottomCenterOf(before)) <= 64 * 64) {
                            Vec3 dest = e.position().add(Vec3.atBottomCenterOf(after).subtract(Vec3.atBottomCenterOf(before)));
                            if (!safe(dest))
                                throw new IllegalStateException("Unsafe encounter relocation: " + id);
                            e.snapTo(dest.x, dest.y, dest.z, e.getYRot(), e.getXRot());
                        }
                    }
            boolean maid = e instanceof cc.thonly.reverie_dreams.entity.MaidYousei;
            if (maid ? !level.noCollision(e) : !safe(e.position()))
                throw new IllegalStateException("Captured NPC lost its safe floor/clearance: " + e.getUUID());
            if (e instanceof BaseNPCLikeEntity npc && old != null)
                for (var p : old.points().entrySet())
                    if (p.getValue().equals(npc.getWorkingPos()) && target.points().containsKey(p.getKey())) {
                        npc.setWorkingPos(target.points().get(p.getKey()));
                        npc.setSeatUUID("");
                    }
        }
    }

    public void restore(CompoundTag saved, StructureBlueprint target) {
        JsonObject original = JsonParser.parseString(saved.getStringOr("bindings", "{}")).getAsJsonObject();
        Set<UUID> backedUp = new HashSet<>();
        for (Tag t : saved.getListOrEmpty("actors"))
            backedUp.add(UUID.fromString(((CompoundTag) t).getStringOr("update_uuid", "")));
        // New actors can be removed only while they remain the untouched, owned template decoration.
        // Mobs require an explicit migration: deleting them could erase NPC progression.
        for (var a : target.actors()) {
            String key = target.id() + "|" + a.id();
            if (original.has(key) || backedUp.contains(a.uuid()) || backedUp.contains(a.legacyUuid()))
                continue;
            Entity e = find(target.id(), a);
            if (e != null) {
                if (e instanceof Mob || playerOwned(e) || !e.entityTags().contains(LANDMARK + target.id()))
                    throw new IllegalStateException("New actor needs rollback adapter: " + a.id());
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                e.save(output);
                CompoundTag current = output.buildResult();
                for (String field : List.of("equipment", "Item", "item"))
                    if (!Objects.equals(current.get(field), a.nbt().get(field)))
                        throw new IllegalStateException("New decoration inventory changed: " + a.id());
                e.discard();
            }
            storage.actors().remove(key);
            dirty = true;
        }
        for (Tag t : saved.getListOrEmpty("actors")) {
            CompoundTag tag = (CompoundTag) t;
            UUID uuid = UUID.fromString(tag.getStringOr("update_uuid", ""));
            Entity e = level.getEntity(uuid);
            if (e == null) {
                boolean removed = storage.actors().entrySet().stream().anyMatch(entry -> entry.getValue().getAsJsonObject().get("uuid").getAsString().equals(uuid.toString()) && entry.getValue().getAsJsonObject().get("state").getAsString().equals("retired"));
                if (!removed)
                    continue;
                e = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag.copy()), level, EntitySpawnReason.STRUCTURE).orElseThrow();
                if (e instanceof Mob || occupiedDisplay(e))
                    throw new IllegalStateException("Cannot resurrect a consumed actor during rollback: " + uuid);
                e.setUUID(uuid);
                if (!level.addFreshEntity(e))
                    throw new IllegalStateException("Rollback actor UUID collision: " + uuid);
            }
            if (playerOwned(e))
                continue;
            var pos = tag.getListOrEmpty("Pos");
            var rotation = tag.getListOrEmpty("Rotation");
            Vec3 expected = pos.size() == 3 ? new Vec3(pos.getDoubleOr(0, 0), pos.getDoubleOr(1, 0), pos.getDoubleOr(2, 0)) : e.position();
            for (var a : target.actors())
                if (a.uuid().equals(uuid) || a.legacyUuid().equals(uuid))
                    expected = a.pos();
            if (e instanceof Mob && e.position().distanceToSqr(expected) > 64 * 64)
                continue;
            if (pos.size() == 3)
                e.snapTo(pos.getDoubleOr(0, 0), pos.getDoubleOr(1, 0), pos.getDoubleOr(2, 0), rotation.getFloatOr(0, 0), rotation.getFloatOr(1, 0));
            if (e instanceof BaseNPCLikeEntity npc && tag.contains("WorkingPos") && target.points().containsValue(npc.getWorkingPos())) {
                npc.setWorkingPos(BlockPos.of(tag.getLongOr("WorkingPos", 0)));
                npc.setSeatUUID("");
            }
            for (var entry : original.entrySet())
                if (entry.getValue().getAsJsonObject().get("uuid").getAsString().equals(uuid.toString())) {
                    storage.actors().add(entry.getKey(), entry.getValue().deepCopy());
                    dirty = true;
                }
        }
    }

    public void restorePositions(CompoundTag saved) {
        for (Tag t : saved.getListOrEmpty("actors")) {
            CompoundTag tag = (CompoundTag) t;
            Entity e = level.getEntity(UUID.fromString(tag.getStringOr("update_uuid", "")));
            if (e == null || playerOwned(e))
                continue;
            var pos = tag.getListOrEmpty("Pos");
            if (pos.size() == 3)
                e.snapTo(pos.getDoubleOr(0, 0), pos.getDoubleOr(1, 0), pos.getDoubleOr(2, 0), e.getYRot(), e.getXRot());
        }
    }

    public static CompoundTag read(Path path) throws IOException {
        return NbtIo.readCompressed(path, NbtAccounter.create(64L * 1024 * 1024));
    }
}
