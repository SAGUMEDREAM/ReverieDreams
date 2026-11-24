package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.work.NPCPlan;
import cc.thonly.reverie_dreams.entity.npc.work.NPCSkills;
import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkFeedback;
import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkProgress;


import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.google.gson.JsonArray;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;

import java.util.Set;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

/**
 * One persistent task per NPC; world mutations remain on the server thread.
 */
public final class NPCSkillTask {
    private String skill = "";
    private String phase = "idle";
    private String detail = "No task.";
    private UUID owner;
    private Vec3 site;
    private String dimension;
    private int retries;
    private boolean resume;
    private UUID taskId;
    private String reason = "idle";
    private NPCWorkProgress progress;
    private JsonArray events = new JsonArray();
    private long workSeconds;
    private long lastProgressSecond;
    private int actions;
    private NPCPlan plan;
    private boolean pendingStep;
    private String transferItem;
    private int transferGoal;
    private int transferred;
    private Vec3 movementTarget;
    private boolean manuallyPaused;
    private boolean recovering;
    private boolean modelReflection = true;

    public UUID id() {
        return taskId;
    }

    public String skill() {
        return skill;
    }

    public String objectiveItem() {
        return active() && progress != null && !progress.countsOffspring() ? progress.item() : null;
    }

    public int quantityRemaining() {
        return !active() || progress == null ? Integer.MAX_VALUE : Math.max(0, progress.goal() - progress.acquired());
    }

    public boolean offspringObjective() {
        return active() && progress != null && progress.countsOffspring();
    }

    public boolean waitingForProducedItems() {
        return active() && progress != null && progress.verified() && !progress.countsOffspring() && quantityRemaining() == 0;
    }

    private static int maximumBatch(NPCSimpleEntity npc, Identifier item) {
        var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(item));
        return NPCWorkInventory.CARGO_SIZE * Math.min(stack.getMaxStackSize(), npc.getInventory().getMaxStackSize());
    }

    public boolean wantsAnimal(Animal animal) {
        return !active() || progress == null || !progress.countsOffspring()
                || BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString().equals(progress.item());
    }

    public boolean recordProduction(NPCSimpleEntity npc, UUID expectedTask, String sourceSkill, String item, int amount, boolean offspring) {
        if (!active() || taskId == null || !taskId.equals(expectedTask) || !skill.equals(sourceSkill)
                || progress == null || !progress.verified() || progress.countsOffspring() != offspring || !progress.item().equals(item) || amount <= 0
                || !matchesDimension(npc.level().dimension().identifier().toString()))
            return false;
        progress.record(item, amount);
        lastProgressSecond = workSeconds;
        return true;
    }

    public boolean active() {
        return pendingStep || phase.equals("moving") || phase.equals("running") || phase.equals("paused") || phase.equals("blocked");
    }

    /**
     * Automatic unloading must not erase an objective that explicitly requires newly held items.
     */
    public boolean protectsCargo(ItemStack stack) {
        return active() && progress != null && !progress.countsOffspring() && !stack.isEmpty()
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(progress.item());
    }

    public String start(NPCSimpleEntity npc, ServerPlayer player, JsonObject args) {
        return start(npc, player, args, true);
    }

    public String startWithoutModel(NPCSimpleEntity npc, ServerPlayer player, JsonObject args) {
        return start(npc, player, args, false);
    }

    private String start(NPCSimpleEntity npc, ServerPlayer player, JsonObject args, boolean reflect) {
        if (active())
            return "Failed: cancel the active task first. " + context();
        modelReflection = reflect;
        plan = null;
        pendingStep = false;
        return startStep(npc, player, args);
    }

    public String startPlan(NPCSimpleEntity npc, ServerPlayer player, JsonObject args) {
        if (active())
            return "Failed: cancel the active task before replacing it.";
        try {
            var candidate = new NPCPlan(args.get("goal").getAsString(), args.getAsJsonArray("steps"));
            for (var entry : candidate.save().getAsJsonArray("steps")) {
                var step = entry.getAsJsonObject();
                String itemKey = step.has("item") ? "item" : "target_item";
                if (step.has(itemKey) && !BuiltInRegistries.ITEM.containsKey(Identifier.parse(step.get(itemKey).getAsString()))) {
                    return "Failed: plan contains an unknown item.";
                }
                if (step.has("target_item") && step.get("target_count").getAsInt() > maximumBatch(npc, Identifier.parse(step.get("target_item").getAsString())))
                    return "Failed: production batch exceeds cargo capacity; split it into smaller production-and-deposit steps.";
                if (step.has("target_entity") && !BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(step.get("target_entity").getAsString())))
                    return "Failed: plan contains an unknown entity type.";
            }
            var first = candidate.current();
            Vec3 target = new Vec3(first.get("x").getAsDouble(), first.get("y").getAsDouble(), first.get("z").getAsDouble());
            if (npc.distanceToSqr(target) > 256)
                return "Failed: first step must be within 16 blocks.";
            plan = candidate;
            modelReflection = true;
            pendingStep = false;
            return startStep(npc, player, first);
        } catch (RuntimeException exception) {
            return "Failed: invalid plan; use 1..8 bounded steps, valid coordinates and item quantities.";
        }
    }

    private String startStep(NPCSimpleEntity npc, ServerPlayer player, JsonObject args) {
        String id = args.get("skill").getAsString();
        var definition = NPCSkills.find(id);
        Vec3 target = new Vec3(args.get("x").getAsDouble(), args.get("y").getAsDouble(), args.get("z").getAsDouble());
        if (definition == null || !Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)
                || target.distanceToSqr(npc.position()) > 256) {
            return "Failed: unknown skill or invalid site (must be within 16 blocks).";
        }
        if (active()) {
            return "Failed: a skill is already active. Cancel it before starting another. " + context();
        }
        NPCWorkProgress objective = null;
        String requestedItem = null;
        int requestedCount = 0;
        if (definition.action().equals("transfer_items")) {
            requestedItem = args.get("item").getAsString();
            requestedCount = args.get("count").getAsInt();
            Identifier itemId = Identifier.tryParse(requestedItem);
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId) || requestedCount < 1 || requestedCount > 1024
                    || args.get("count").getAsDouble() != requestedCount)
                return "Failed: invalid transfer item/count.";
        }
        if (id.equals("breed_animals") && (args.has("target_entity") || args.has("target_count") || args.has("target_item"))) {
            if (args.has("target_item") || !args.has("target_entity") || !args.has("target_count"))
                return "Failed: breeding objectives need target_entity/target_count.";
            Identifier entityId = Identifier.tryParse(args.get("target_entity").getAsString());
            int count = args.get("target_count").getAsInt();
            if (entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId) || count < 1 || count > 1024
                    || args.get("target_count").getAsDouble() != count)
                return "Failed: invalid offspring objective.";
            objective = NPCWorkProgress.offspring(entityId.toString(), count);
        } else if (args.has("target_item") || args.has("target_count") || args.has("target_entity")) {
            if (args.has("target_entity"))
                return "Failed: only breeding uses target_entity.";
            if (!args.has("target_item") || !args.has("target_count") || !Set.of(
                    "farm", "fish", "shear_sheep", "harvest_honey", "smelt").contains(id)) {
                return "Failed: quantity objectives require both target_item/target_count and a collecting skill.";
            }
            Identifier itemId = Identifier.tryParse(args.get("target_item").getAsString());
            int amount = args.get("target_count").getAsInt();
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId) || amount < 1 || amount > 1024
                    || args.get("target_count").getAsDouble() != amount)
                return "Failed: invalid item or quantity (1..1024).";
            if (amount > maximumBatch(npc, itemId))
                return "Failed: production batch exceeds cargo capacity; split it into smaller production-and-deposit steps.";
            objective = NPCWorkProgress.production(itemId.toString(), amount);
        }
        skill = id;
        owner = player.getUUID();
        site = target;
        dimension = npc.level().dimension().identifier().toString();
        retries = 0;
        resume = false;
        manuallyPaused = false;
        recovering = false;
        taskId = UUID.randomUUID();
        progress = objective;
        if (plan == null || plan.save().get("cursor").getAsInt() == 0)
            events = new JsonArray();
        reason = "idle";
        workSeconds = lastProgressSecond = 0;
        actions = 0;
        transferItem = requestedItem;
        transferGoal = requestedCount;
        transferred = 0;
        return begin(npc, player);
    }

    private String begin(NPCSimpleEntity npc, ServerPlayer player) {
        if (progress != null && progress.complete()) {
            halt(npc);
            update(npc, "completed", "quantity_reached");
            return context();
        }
        if (pauseForRecovery(npc))
            return context();
        var definition = NPCSkills.find(skill);
        JsonObject args = new JsonObject();
        String action = definition.action();
        movementTarget = site;
        if (action.equals("transfer_items")) {
            BlockPos containerPos = BlockPos.containing(site);
            if (containerPos.distToCenterSqr(npc.position()) > 9) {
                movementTarget = NPCWorldObservation.standPosition(npc, containerPos);
                if (movementTarget == null) {
                    halt(npc);
                    detail = "No reachable standing position beside the container.";
                    update(npc, "blocked", "unreachable");
                    return context();
                }
            } else {
                halt(npc);
                String result = npc.getContainerAccess().transfer(npc, player, containerPos, definition.mode(), transferItem, transferGoal - transferred);
                detail = result;
                if (result.startsWith("Failed:") || result.startsWith("Denied:")) {
                    update(npc, "blocked", "container_unavailable");
                } else {
                    transferred += JsonParser.parseString(result).getAsJsonObject().get("moved").getAsInt();
                    update(npc, transferred >= transferGoal ? "completed" : "blocked",
                            transferred >= transferGoal ? "transferred" : "need_items_or_space");
                }
                return context();
            }
        }
        if ((action.equals("work") || action.equals("move_to") || action.equals("transfer_items")) && npc.distanceToSqr(movementTarget) > 1) {
            args.addProperty("x", movementTarget.x);
            args.addProperty("y", movementTarget.y);
            args.addProperty("z", movementTarget.z);
            phase = "moving";
            detail = NPCWorldTools.executeSkillAction(npc, player, "move_to", args);
        } else if (action.equals("move_to")) {
            halt(npc);
            phase = "completed";
            detail = "Arrival confirmed.";
        } else {
            if (action.equals("work")) {
                NPCWorkFeedback.prepare(npc, skill);
                var report = NPCWorkFeedback.inspect(npc, skill, BlockPos.containing(site));
                if (report.blocked()) {
                    halt(npc);
                    detail = report.code();
                    update(npc, "blocked", report.code());
                    return context();
                }
            }
            args.addProperty("mode", definition.mode());
            phase = "running";
            detail = NPCWorldTools.executeSkillAction(npc, player, action, args);
            if (action.equals("work")) {
                if (!detail.startsWith("Failed:") && !detail.startsWith("Denied:"))
                    npc.setWorkingPos(BlockPos.containing(site));
                detail += " Continuous task: supplies/facilities may be missing; production is not verified.";
            }
        }
        if (detail.startsWith("Failed:") || detail.startsWith("Denied:")) {
            phase = "failed";
        }
        update(npc, phase, phase.equals("completed") ? "arrived" : phase.equals("failed") ? "unreachable" : "accepted");
        return context();
    }

    public void tick(NPCSimpleEntity npc) {
        this.checkDimension(npc);
        if (!active() || !(npc.level() instanceof ServerLevel level) || npc.tickCount % 20 != 0) {
            return;
        }
        ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player != null && !npc.isOwnedBy(player)) {
            cancel(npc, "Ownership changed.");
            return;
        }
        if (manuallyPaused)
            return;
        if (player == null || player.hasDisconnected() || !player.isAlive()
                || player.level() != level || npc.distanceToSqr(player) > 1024) {
            if (!phase.equals("paused")) {
                halt(npc);
                phase = "paused";
                detail = "Waiting for the owner to return within 32 blocks.";
                update(npc, phase, "owner_unavailable");
            }
            return;
        }
        if (pauseForRecovery(npc))
            return;
        if (resume || phase.equals("paused")) {
            if (pendingStep) {
                pendingStep = false;
                resume = false;
                phase = "idle";
                advancePlan(npc, player);
                return;
            }
            resume = false;
            begin(npc, player);
            return;
        }
        if (pendingStep) {
            pendingStep = false;
            phase = "idle";
            advancePlan(npc, player);
            return;
        }
        if (phase.equals("running")) {
            var definition = NPCSkills.find(skill);
            var expected = definition.action().equals("follow") ? NPCStates.FOLLOW
                    : definition.action().equals("stay") ? NPCStates.NO_WALK : NPCStates.WORKING;
            if (npc.getNPCState() != expected || (definition.action().equals("work")
                    && npc.getWorkMode() != NPCWorkModes.get(
                    ReverieDreams.id(definition.mode())))) {
                detail = "Task superseded by manual controls.";
                update(npc, "cancelled", "manual_override");
                return;
            }
        }
        workSeconds++;
        if (phase.equals("blocked") && npc.getNPCState() != NPCStates.NO_WALK) {
            update(npc, "cancelled", "manual_override");
            return;
        }
        if (progress != null) {
            int before = progress.acquired();
            if (!progress.countsOffspring())
                progress.sample(NPCWorkFeedback.count(npc, BuiltInRegistries.ITEM.getValue(Identifier.parse(progress.item()))));
            if (progress.acquired() > before)
                lastProgressSecond = workSeconds;
            if (progress.complete()) {
                halt(npc);
                detail = progress.verified() ? "Verified production and required available items (or actual offspring) reached the objective; work stopped."
                        : "Legacy net inventory objective reached; production provenance was not recorded.";
                update(npc, "completed", "quantity_reached");
                return;
            }
        }
        if (phase.equals("blocked")) {
            if (npc.getNPCState() != NPCStates.NO_WALK) {
                update(npc, "cancelled", "manual_override");
            } else if (npc.tickCount % 100 == 0) {
                if (NPCSkills.find(skill).action().equals("transfer_items")) {
                    begin(npc, player);
                    return;
                }
                NPCWorkFeedback.prepare(npc, skill);
                var report = NPCWorkFeedback.inspect(npc, skill, BlockPos.containing(site));
                if (!report.blocked())
                    begin(npc, player);
                else
                    update(npc, "blocked", report.code());
            }
            return;
        }
        if (phase.equals("moving")) {
            if (npc.getChatMoveTarget() == null && npc.getNPCState() != NPCStates.NO_WALK
                    && npc.getNPCState() != NPCStates.NORMAL) {
                phase = "cancelled";
                detail = "Task superseded by a manual state change.";
                update(npc, phase, "manual_override");
                return;
            }
            if (npc.distanceToSqr(movementTarget == null ? site : movementTarget) <= 1) {
                begin(npc, player);
            } else if (npc.getChatMoveTarget() == null) {
                if (++retries <= 2) {
                    begin(npc, player);
                } else {
                    halt(npc);
                    phase = "failed";
                    detail = "Walking failed after two retries (blocked or timed out).";
                    update(npc, phase, "unreachable");
                }
            }
        } else {
            var definition = NPCSkills.find(skill);
            var expected = definition.action().equals("follow") ? NPCStates.FOLLOW
                    : definition.action().equals("stay") ? NPCStates.NO_WALK : NPCStates.WORKING;
            if (npc.getNPCState() != expected || (definition.action().equals("work")
                    && npc.getWorkMode() != NPCWorkModes.get(
                    ReverieDreams.id(definition.mode())))) {
                phase = "cancelled";
                detail = "Task superseded by a manual state/work mode change.";
                update(npc, phase, "manual_override");
            } else if (definition.action().equals("work") && npc.tickCount % 100 == 0) {
                NPCWorkFeedback.prepare(npc, skill);
                var report = NPCWorkFeedback.inspect(npc, skill, BlockPos.containing(site));
                if (report.blocked())
                    halt(npc);
                String code = !report.blocked() && (report.code().equals("ready") || report.code().equals("work_enabled_unverified"))
                        && workSeconds - lastProgressSecond >= 120
                        ? "no_verified_progress" : report.code();
                detail = code;
                update(npc, report.blocked() ? "blocked" : "running", code);
            }
        }
    }

    public boolean matchesDimension(String current) {
        return dimension != null && dimension.equals(current);
    }

    /**
     * Run before work goals so an injured worker cannot perform one more world mutation.
     */
    public void checkBeforeWork(NPCSimpleEntity npc) {
        checkDimension(npc);
        if (!npc.level().isClientSide() && active() && !manuallyPaused)
            pauseForRecovery(npc);
    }

    private boolean pauseForRecovery(NPCSimpleEntity npc) {
        var definition = NPCSkills.find(skill);
        if (pendingStep || definition == null || !definition.action().equals("work"))
            return false;
        var behavior = NPCPersonas.behavior(npc.getRoleType().identify());
        double ratio = npc.getMaxHealth() <= 0 ? 1 : (double) npc.getHealth() / npc.getMaxHealth();
        if (behavior.needsRest(ratio, recovering)) {
            recovering = true;
            halt(npc);
            detail = "Work paused for recovery according to this character's gameplay behavior profile; no healing is granted.";
            update(npc, "paused", "behavior_recovering");
            return true;
        }
        if (recovering) {
            recovering = false;
            resume = true;
        }
        return false;
    }

    /**
     * Called before ordinary work goals tick, including the first tick after entity load/teleport.
     */
    public void checkDimension(NPCSimpleEntity npc) {
        if (active() && manuallyPaused && !npc.level().isClientSide() && npc.getNPCState() != NPCStates.NO_WALK)
            halt(npc);
        if (!active() || npc.level().isClientSide() || matchesDimension(npc.level().dimension().identifier().toString()))
            return;
        halt(npc);
        plan = null;
        pendingStep = false;
        resume = false;
        detail = "The saved task dimension changed or could not be verified. Issue a new order at the intended site.";
        update(npc, "cancelled", "task_dimension_changed");
    }

    public String cancel(NPCSimpleEntity npc, String reason) {
        if (active()) {
            halt(npc);
            phase = "cancelled";
            detail = reason;
            pendingStep = false;
            plan = null;
            manuallyPaused = false;
            recovering = false;
            update(npc, phase, "cancelled");
        }
        return context();
    }

    public String pause(NPCSimpleEntity npc) {
        if (active()) {
            manuallyPaused = true;
            halt(npc);
            detail = "Paused by owner; continue explicitly to resume.";
            update(npc, "paused", "manually_paused");
        }
        return context();
    }

    public String continueTask(NPCSimpleEntity npc) {
        if (active() && manuallyPaused) {
            manuallyPaused = false;
            resume = true;
            detail = "Resume requested; owner availability and conditions will be checked.";
        }
        return context();
    }

    private static void halt(NPCSimpleEntity npc) {
        npc.setChatMoveTarget(null);
        npc.getNavigation().stop();
        npc.setNPCState(NPCStates.NO_WALK);
    }

    public String context() {
        JsonObject data = new JsonObject();
        data.addProperty("skill", skill);
        if (dimension != null)
            data.addProperty("dimension", dimension);
        data.addProperty("phase", phase);
        data.addProperty("detail", detail);
        data.addProperty("retries", retries);
        if (taskId != null)
            data.addProperty("task_id", taskId.toString());
        data.addProperty("reason", reason);
        data.addProperty("work_seconds", workSeconds);
        data.addProperty("seconds_without_progress", Math.max(0, workSeconds - lastProgressSecond));
        data.addProperty("verified_actions", actions);
        if (progress != null)
            data.add("progress", progress.save());
        data.add("recent_events", events.deepCopy());
        if (plan != null)
            data.add("plan", plan.save());
        data.addProperty("pending_step", pendingStep);
        data.addProperty("manually_paused", manuallyPaused);
        data.addProperty("recovering", recovering);
        data.addProperty("model_reflection_enabled", modelReflection);
        if (transferItem != null) {
            data.addProperty("transfer_item", transferItem);
            data.addProperty("transfer_goal", transferGoal);
            data.addProperty("transferred", transferred);
        }
        if (site != null) {
            data.addProperty("x", site.x);
            data.addProperty("y", site.y);
            data.addProperty("z", site.z);
        }
        return data.toString();
    }

    public void recordAction() {
        if (phase.equals("running")) {
            actions++;
            lastProgressSecond = workSeconds;
        }
    }

    public String eventKey() {
        return taskId + "/" + phase + "/" + reason;
    }

    private void advancePlan(NPCSimpleEntity npc, ServerPlayer player) {
        String result = startStep(npc, player, plan.current());
        if (result.startsWith("Failed:")) {
            halt(npc);
            detail = result;
            update(npc, "failed", "unreachable");
        }
    }

    private void update(NPCSimpleEntity npc, String nextPhase, String nextReason) {
        boolean changed = !phase.equals(nextPhase) || !reason.equals(nextReason) || events.isEmpty();
        phase = nextPhase;
        reason = nextReason;
        if (!changed)
            return;
        if (nextPhase.equals("completed") && plan != null)
            pendingStep = plan.advance();
        if (modelReflection && owner != null && (phase.equals("blocked") || phase.equals("failed") || phase.equals("completed") && !pendingStep)) {
            npc.queueTaskReflection(owner, eventKey());
        }
        JsonObject event = new JsonObject();
        event.addProperty("phase", phase);
        event.addProperty("reason", reason);
        event.addProperty("game_time", npc.level().getGameTime());
        events.add(event);
        if (owner != null)
            npc.getMemory().event(owner, "task_" + phase,
                    skill + ": " + reason + (progress == null ? "" : " (" + (progress.verified() ? "verified production " : "legacy net inventory ") + progress.acquired() + "/" + progress.goal() + ")"), npc.level().getGameTime());
        while (events.size() > 16)
            events.remove(0);
        if (npc.level() instanceof ServerLevel level && owner != null) {
            var player = level.getServer().getPlayerList().getPlayer(owner);
            var behavior = NPCPersonas.behavior(npc.getRoleType().identify());
            if (player != null && behavior.notifyPhase(phase)) {
                var message = Component.translatable("gui.reverie_dreams.task_reason." + reason);
                var feedback = progress != null || transferItem != null
                        ? Component.translatable("gui.reverie_dreams.task_update_progress", npc.getDisplayName(), message,
                        progress == null ? transferred : progress.acquired(), progress == null ? transferGoal : progress.goal())
                        : Component.translatable("gui.reverie_dreams.task_update", npc.getDisplayName(), message);
                player.sendSystemMessage(Component.translatable("gui.reverie_dreams.agent.feedback." + behavior.feedbackStyle(), feedback));
            }
        }
    }

    public void save(ValueOutput output) {
        JsonObject data = JsonParser.parseString(context()).getAsJsonObject();
        if (owner != null) {
            data.addProperty("owner", owner.toString());
        }
        output.putString("ChatSkillTask", data.toString());
    }

    public void load(ValueInput input) {
        try {
            JsonObject data = JsonParser.parseString(input.getStringOr("ChatSkillTask", "{}")).getAsJsonObject();
            if (!data.has("owner") || NPCSkills.find(data.get("skill").getAsString()) == null) {
                return;
            }
            skill = data.get("skill").getAsString();
            owner = UUID.fromString(data.get("owner").getAsString());
            site = new Vec3(data.get("x").getAsDouble(), data.get("y").getAsDouble(), data.get("z").getAsDouble());
            dimension = data.has("dimension") ? data.get("dimension").getAsString() : null;
            if (dimension != null && Identifier.tryParse(dimension) == null)
                throw new IllegalArgumentException("Invalid task dimension");
            if (!Double.isFinite(site.x) || !Double.isFinite(site.y) || !Double.isFinite(site.z)) {
                throw new IllegalArgumentException("Invalid site");
            }
            phase = data.get("phase").getAsString();
            if (!Set.of("idle", "moving", "running", "paused", "blocked", "completed", "failed", "cancelled").contains(phase)) {
                throw new IllegalArgumentException("Invalid phase");
            }
            detail = data.get("detail").getAsString();
            retries = Math.clamp(data.get("retries").getAsInt(), 0, 2);
            taskId = data.has("task_id") ? UUID.fromString(data.get("task_id").getAsString()) : UUID.randomUUID();
            reason = data.has("reason") ? data.get("reason").getAsString() : "accepted";
            progress = data.has("progress") ? NPCWorkProgress.load(data.getAsJsonObject("progress")) : null;
            if (progress != null && progress.countsOffspring() && !skill.equals("breed_animals"))
                throw new IllegalArgumentException("Invalid offspring task kind");
            if (progress != null && !(progress.countsOffspring() ? BuiltInRegistries.ENTITY_TYPE : BuiltInRegistries.ITEM).containsKey(Identifier.parse(progress.item()))) {
                throw new IllegalArgumentException("Unknown objective item");
            }
            events = data.has("recent_events") ? data.getAsJsonArray("recent_events").deepCopy() : new JsonArray();
            while (events.size() > 16)
                events.remove(0);
            workSeconds = data.has("work_seconds") ? Math.max(0, data.get("work_seconds").getAsLong()) : 0;
            lastProgressSecond = workSeconds - (data.has("seconds_without_progress")
                    ? Math.max(0, data.get("seconds_without_progress").getAsLong()) : 0);
            actions = data.has("verified_actions") ? Math.max(0, data.get("verified_actions").getAsInt()) : 0;
            plan = data.has("plan") ? NPCPlan.load(data.getAsJsonObject("plan")) : null;
            pendingStep = data.has("pending_step") && data.get("pending_step").getAsBoolean();
            manuallyPaused = data.has("manually_paused") && data.get("manually_paused").getAsBoolean();
            recovering = data.has("recovering") && data.get("recovering").getAsBoolean();
            modelReflection = !data.has("model_reflection_enabled") || data.get("model_reflection_enabled").getAsBoolean();
            if (pendingStep && (plan == null || !plan.hasCurrent()))
                throw new IllegalArgumentException("Invalid next plan step");
            transferItem = data.has("transfer_item") ? data.get("transfer_item").getAsString() : null;
            transferGoal = data.has("transfer_goal") ? data.get("transfer_goal").getAsInt() : 0;
            transferred = data.has("transferred") ? data.get("transferred").getAsInt() : 0;
            if (NPCSkills.find(skill).action().equals("transfer_items") && (transferItem == null
                    || !BuiltInRegistries.ITEM.containsKey(Identifier.parse(transferItem)) || transferGoal < 1 || transferGoal > 1024
                    || transferred < 0 || transferred > transferGoal))
                throw new IllegalArgumentException("Invalid saved transfer");
            resume = active();
        } catch (RuntimeException exception) {
            pendingStep = false;
            plan = null;
            phase = "failed";
            detail = "Saved skill task was invalid.";
        }
    }
}
