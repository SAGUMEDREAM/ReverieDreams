package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.work.NPCSkills;
import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkFeedback;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Small, explicit world actions. All methods that access the world run on the server thread.
 */
public final class NPCWorldTools {
    private static final Set<String> READ_ONLY = Set.of("observe", "list_skills", "behavior_profile", "suggest_work", "task_status", "inspect_work", "inspect_container", "lookup_item", "search_items", "lookup_recipe", "usage_status", "recall_memory");

    private NPCWorldTools() {
    }

    public static List<Map<String, Object>> definitions() {
        return List.of(tool("observe", "Observe your position, weather, nearby visible entities and inventory.", Map.of()),
                tool("remember", "Save a preference, name or agreement explicitly given by this player. This stores a player statement, not a verified game fact or new order.", Map.of(
                        "key", Map.of("type", "string", "maxLength", 64), "text", Map.of("type", "string", "maxLength", 256))),
                tool("recall_memory", "Recall only the current player's saved statements and server-observed shared events.", Map.of()),
                tool("forget_memory", "Forget a saved statement at the current player's request. Key '*' clears all statements/events for this player only.", Map.of("key", Map.of("type", "string"))),
                tool("search_items", "Search registered IDs and actual localized item names. Ambiguous names return candidates; use an exact ID for actions. Offset paginates 8 results.", Map.of("query", Map.of("type", "string", "maxLength", 128), "offset", Map.of("type", "integer", "minimum", 0)), List.of("query")),
                tool("lookup_item", "Resolve an item ID/name and query currently loaded vanilla, third-party and mod custom recipes, 8 per page. Empty results do not prove unobtainability. Never guess ingredients.", Map.of("item", Map.of("type", "string"), "offset", Map.of("type", "integer", "minimum", 0)), List.of("item")),
                tool("lookup_recipe", "Read the exact loaded recipe ID with actual serialized ingredients, output and known workstations.", Map.of("recipe", Map.of("type", "string"))),
                tool("usage_status", "Read this player's daily requests/token quotas and remaining shared capacity. Does not contact the provider or reveal credentials.", Map.of()),
                tool("equip_item", "Equip an actual inventory item in main/off hand, swapping the previous item back without discarding it.", Map.of(
                        "item", Map.of("type", "string"), "hand", Map.of("type", "string", "enum", List.of("main", "off")))),
                tool("inspect_work", "Diagnose a work skill at a nearby site before starting. Returns blockers, not proof of production.", Map.of(
                        "skill", Map.of("type", "string"), "x", Map.of("type", "number"), "y", Map.of("type", "number"), "z", Map.of("type", "number"))),
                tool("authorize_container", "Only on the owner's explicit request, authorize or revoke this chest/barrel. Owner must stand within 6 blocks to grant access.", Map.of(
                        "x", Map.of("type", "integer"), "y", Map.of("type", "integer"), "z", Map.of("type", "integer"), "grant", Map.of("type", "boolean"))),
                tool("inspect_container", "Read the contents of an owner-authorized nearby chest/barrel.", Map.of(
                        "x", Map.of("type", "integer"), "y", Map.of("type", "integer"), "z", Map.of("type", "integer"))),
                tool("transfer_items", "Move real items to/from an authorized chest/barrel within 3 blocks. Walk to observed walk_x/y/z first. Returns exact moved/remaining quantities.", Map.of(
                        "x", Map.of("type", "integer"), "y", Map.of("type", "integer"), "z", Map.of("type", "integer"),
                        "direction", Map.of("type", "string", "enum", List.of("deposit", "withdraw")), "item", Map.of("type", "string"),
                        "count", Map.of("type", "integer", "minimum", 1, "maximum", 1024))),
                tool("list_skills", "List persistent skills in this character's preference order. Preferences never override the owner's explicit goal or grant new skills.", Map.of()),
                tool("behavior_profile", "Read this character's grounded gameplay behavior, feedback style and recovery policy. Mod adaptations are not canon numeric abilities.", Map.of()),
                tool("suggest_work", "Inspect work at your current site and rank feasible suggestions before role preferences. Read-only: do not change active plans or treat suggestions as owner orders.", Map.of()),
                tool("task_status", "Read the persistent skill task status.", Map.of()),
                tool("start_plan", "Execute an explicit owner goal as 1..8 verified steps. Observe first, authorize containers explicitly. Continuous skills must be last; intermediate work requires a quantity objective. Blocked steps pause for supplies; never claim final delivery until verified.", Map.of(
                        "goal", Map.of("type", "string", "maxLength", 256), "steps", Map.of("type", "array", "minItems", 1, "maxItems", 8,
                                "items", Map.of("type", "object", "properties", stepProperties(), "required", List.of("skill", "x", "y", "z"), "additionalProperties", false)))),
                tool("cancel_task", "Cancel the active skill and stop its movement/work.", Map.of()),
                tool("pause_task", "Pause the current plan persistently, preserving quantities and completed steps until the owner explicitly continues.", Map.of()),
                tool("continue_task", "Resume an owner-paused task after rechecking conditions. Does not create a replacement task.", Map.of()),
                tool("start_skill", "Start a persistent skill. Observe first. target_item/target_count require verified production AND available stock for farm/fish/shear/honey/smelt. A batch must fit 18 cargo slots; split large orders into production/deposit steps. Breeding uses target_entity/target_count and counts successful offspring, not feeding. Cancel before replacing an active task.", Map.of(
                                "skill", Map.of("type", "string", "enum", NPCSkills.ALL.stream().map(NPCSkills.Skill::id).toList()),
                                "x", Map.of("type", "number"), "y", Map.of("type", "number"), "z", Map.of("type", "number"),
                                "target_item", Map.of("type", "string"), "target_count", Map.of("type", "integer", "minimum", 1, "maximum", 1024),
                                "target_entity", Map.of("type", "string"),
                                "item", Map.of("type", "string"), "count", Map.of("type", "integer", "minimum", 1, "maximum", 1024)),
                        List.of("skill", "x", "y", "z")),
                tool("follow", "Follow the player who owns you.", Map.of()),
                tool("stay", "Stop moving and wait here.", Map.of()),
                tool("move_to", "Walk to a nearby position, within 16 blocks. Use coordinates from observation.", Map.of(
                        "x", Map.of("type", "number"), "y", Map.of("type", "number"), "z", Map.of("type", "number"))),
                tool("work", "Start an existing NPC work mode here. Requires the usual items and nearby facilities.", Map.of(
                        "mode", Map.of("type", "string", "enum", List.of("farm", "breed", "sheep_shearing", "smelt",
                                "chest_classification", "honeycomb_harvesting", "fishing")))));
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> properties) {
        return tool(name, description, properties, List.copyOf(properties.keySet()));
    }

    private static Map<String, Object> stepProperties() {
        return Map.of("skill", Map.of("type", "string", "enum", NPCSkills.ALL.stream().map(NPCSkills.Skill::id).toList()),
                "x", Map.of("type", "number"), "y", Map.of("type", "number"), "z", Map.of("type", "number"),
                "target_item", Map.of("type", "string"), "target_count", Map.of("type", "integer", "minimum", 1, "maximum", 1024),
                "target_entity", Map.of("type", "string"),
                "item", Map.of("type", "string"), "count", Map.of("type", "integer", "minimum", 1, "maximum", 1024));
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> properties, List<String> required) {
        return Map.of("type", "function", "function", Map.of("name", name, "description", description,
                "parameters", Map.of("type", "object", "properties", properties,
                        "required", required, "additionalProperties", false)));
    }

    public static String execute(Entity entity, ServerPlayer player, String name, JsonObject args) {
        return execute(entity, player, name, args, false);
    }

    public static List<Map<String, Object>> readOnlyDefinitions() {
        return definitions().stream().filter(tool -> READ_ONLY.contains(((Map<?, ?>) tool.get("function")).get("name"))).toList();
    }

    public static String executeReadOnly(Entity entity, ServerPlayer player, String name, JsonObject args) {
        if (!READ_ONLY.contains(name))
            return "Denied: task-event analysis may only read state, never change orders, inventory, permissions or memory.";
        return execute(entity, player, name, args);
    }

    static String executeSkillAction(Entity entity, ServerPlayer player, String name, JsonObject args) {
        return execute(entity, player, name, args, true);
    }

    private static String execute(Entity entity, ServerPlayer player, String name, JsonObject args, boolean skillAction) {
        if (!(entity instanceof NPCSimpleEntity npc) || !npc.isAlive() || npc.isRemoved()
                || player.hasDisconnected() || !player.isAlive() || npc.level() != player.level()
                || npc.distanceToSqr(player) > 32 * 32) {
            return "Failed: NPC or player unavailable, or player is too far away.";
        }
        if (name.equals("observe")) {
            return observe(npc, player);
        }
        if (name.equals("list_skills")) {
            return ChatProvider.GSON.toJson(NPCPersonas.behavior(npc.getRoleType().identify()).skills());
        }
        if (name.equals("behavior_profile"))
            return NPCPersonas.behavior(npc.getRoleType().identify()).context().toString();
        if (name.equals("suggest_work"))
            return NPCWorkSuggestions.context(npc).toString();
        if (name.equals("search_items"))
            return NPCKnowledge.search(args.get("query").getAsString(), args.has("offset") ? args.get("offset").getAsInt() : 0);
        if (name.equals("lookup_item"))
            return NPCKnowledge.lookup(npc, args.get("item").getAsString(), args.has("offset") ? args.get("offset").getAsInt() : 0);
        if (name.equals("lookup_recipe"))
            return NPCKnowledge.recipe(npc, args.get("recipe").getAsString());
        if (name.equals("usage_status"))
            return NPCUsageView.of(player).toString();
        if (name.equals("recall_memory"))
            return npc.getMemory().recall(player.getUUID()).toString();
        if (name.equals("remember")) {
            npc.getMemory().remember(player.getUUID(), args.get("key").getAsString(), args.get("text").getAsString(), npc.level().getGameTime());
            return "Saved as this player's statement; not a verified game event.";
        }
        if (name.equals("forget_memory")) {
            npc.getMemory().forget(player.getUUID(), args.get("key").getAsString());
            return "Saved statement forgotten.";
        }
        if (!npc.isOwnedBy(player)) {
            return "Denied: only this NPC's owner can give world action orders.";
        }
        if (!skillAction && npc.getSkillTask().active()
                && List.of("follow", "stay", "move_to", "work", "transfer_items").contains(name)) {
            return "Failed: cancel the active skill first, then issue a direct action.";
        }
        String result = switch (name) {
            case "equip_item" -> {
                var id = Identifier.tryParse(args.get("item").getAsString());
                String hand = args.get("hand").getAsString();
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id) || !(hand.equals("main") || hand.equals("off")))
                    yield "Failed: invalid item or hand.";
                yield NPCInventoryTransfer.equip(npc.getInventory(), BuiltInRegistries.ITEM.getValue(id), hand.equals("main") ? 22 : 23)
                        ? "Equipped the requested inventory item." : "Failed: requested item is not in the usable inventory.";
            }
            case "inspect_work" -> {
                var pos = position(args);
                var definition = NPCSkills.find(args.get("skill").getAsString());
                if (pos.distToCenterSqr(npc.position()) > 256 || definition == null || !definition.action().equals("work")) {
                    yield "Failed: choose a supported work skill and a site within 16 blocks.";
                }
                yield ChatProvider.GSON.toJson(NPCWorkFeedback.inspect(npc, definition.id(), pos));
            }
            case "authorize_container" ->
                    npc.getContainerAccess().authorize(npc, player, position(args), args.get("grant").getAsBoolean());
            case "inspect_container" -> npc.getContainerAccess().inspect(npc, player, position(args));
            case "transfer_items" -> {
                if (args.get("count").getAsDouble() != args.get("count").getAsInt())
                    yield "Failed: count must be an integer.";
                yield npc.getContainerAccess().transfer(npc, player, position(args), args.get("direction").getAsString(),
                        args.get("item").getAsString(), args.get("count").getAsInt());
            }
            case "task_status" -> npc.getSkillTask().context();
            case "start_skill" -> npc.getSkillTask().start(npc, player, args);
            case "start_plan" -> npc.getSkillTask().startPlan(npc, player, args);
            case "cancel_task" -> npc.getSkillTask().cancel(npc, "Cancelled by owner.");
            case "pause_task" -> npc.getSkillTask().pause(npc);
            case "continue_task" -> npc.getSkillTask().continueTask(npc);
            case "follow" -> {
                reset(npc);
                npc.setNPCState(NPCStates.FOLLOW);
                yield "Now following the owner.";
            }
            case "stay" -> {
                reset(npc);
                npc.setNPCState(NPCStates.NO_WALK);
                yield "Stopped and waiting here.";
            }
            case "move_to" -> {
                Vec3 target = new Vec3(args.get("x").getAsDouble(), args.get("y").getAsDouble(), args.get("z").getAsDouble());
                if (!Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)
                        || target.distanceToSqr(npc.position()) > 16 * 16
                        || !npc.level().hasChunkAt(BlockPos.containing(target))
                        || !npc.level().getWorldBorder().isWithinBounds(BlockPos.containing(target))) {
                    yield "Failed: target must be finite, loaded, inside the world border and within 16 blocks.";
                }
                var path = npc.getNavigation().createPath(target.x, target.y, target.z, 0);
                if (path == null || !path.canReach()) {
                    yield "Failed: no reachable walking path to that position.";
                }
                reset(npc);
                npc.setNPCState(NPCStates.NORMAL);
                npc.setChatMoveTarget(target);
                npc.setChatMoveDeadline(npc.level().getGameTime() + 400);
                yield "Walking order accepted; arrival is not yet confirmed.";
            }
            case "work" -> {
                var mode = switch (args.get("mode").getAsString()) {
                    case "farm" -> NPCWorkModes.FARM;
                    case "breed" -> NPCWorkModes.BREED;
                    case "sheep_shearing" -> NPCWorkModes.SHEEP_SHEARING;
                    case "smelt" -> NPCWorkModes.SMELT;
                    case "chest_classification" -> NPCWorkModes.CHEST_CLASSIFICATION;
                    case "honeycomb_harvesting" -> NPCWorkModes.HONEYCOMB_HARVESTING;
                    case "fishing" -> NPCWorkModes.FISHING;
                    default -> null;
                };
                if (mode == null) {
                    yield "Failed: unsupported work mode.";
                }
                reset(npc);
                npc.setWorkingPos(npc.blockPosition());
                npc.setWorkMode(mode);
                npc.setNPCState(NPCStates.WORKING);
                yield "Work mode enabled here. Actual work requires appropriate supplies and facilities; nothing completed yet.";
            }
            default -> "Failed: unknown tool.";
        };
        if (List.of("follow", "stay", "move_to", "work", "transfer_items", "authorize_container", "equip_item").contains(name)) {
            npc.getMemory().event(player.getUUID(), "tool_result_" + name, result, npc.level().getGameTime());
        }
        return result;
    }

    private static void reset(NPCSimpleEntity npc) {
        npc.setChatMoveTarget(null);
        npc.getNavigation().stop();
        npc.stopSleeping();
        npc.stopRiding();
        npc.setOrderedToSit(false);
        npc.setTarget(null);
    }

    private static BlockPos position(JsonObject args) {
        double x = args.get("x").getAsDouble(), y = args.get("y").getAsDouble(), z = args.get("z").getAsDouble();
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 4096) {
            throw new IllegalArgumentException("Invalid position");
        }
        return BlockPos.containing(x, y, z);
    }

    private static String observe(NPCSimpleEntity npc, ServerPlayer player) {
        JsonObject state = new JsonObject();
        state.addProperty("x", npc.getX());
        state.addProperty("y", npc.getY());
        state.addProperty("z", npc.getZ());
        state.addProperty("raining", npc.level().isRaining());
        state.addProperty("day", npc.level().isBrightOutside());
        state.addProperty("moving", npc.getChatMoveTarget() != null);
        state.add("skill_task", JsonParser.parseString(npc.getSkillTask().context()));
        state.addProperty("state", String.valueOf(BuiltInRegistryProviders.NPC_STATE.getKey(npc.getNPCState())));
        state.addProperty("work_mode", String.valueOf(BuiltInRegistryProviders.NPC_WORK_MODE.getKey(npc.getWorkMode())));
        var inventory = new ArrayList<Map<String, Object>>();
        for (int slot = 0; slot < npc.getInventory().getContainerSize(); slot++) {
            var stack = npc.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                inventory.add(Map.of("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), "count", stack.getCount()));
            }
        }
        state.add("inventory", ChatProvider.GSON.toJsonTree(inventory));
        state.add("nearby_facilities", NPCWorldObservation.facilities(npc, player));
        state.add("nearby_visible_entities", ChatProvider.GSON.toJsonTree(npc.level().getEntities(npc,
                        npc.getBoundingBox().inflate(8), e -> e.isAlive() && npc.hasLineOfSight(e))
                .stream().limit(16).map(e -> Map.of("name", e.getDisplayName().getString(),
                        "type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(),
                        "x", e.getX(), "y", e.getY(), "z", e.getZ())).toList()));
        return state.toString();
    }
}
