package cc.thonly.reverie_dreams.gui.entity;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.ai.npc.NPCUsageView;
import cc.thonly.reverie_dreams.ai.npc.NPCPersonas;
import cc.thonly.reverie_dreams.ai.npc.NPCWorkSuggestions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.UUID;

import cc.thonly.reverie_dreams.entity.npc.work.NPCSkills;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.server.ChatAIManager;

/**
 * The owner can inspect/control an existing task without making any paid requests.
 */
public final class NPCAgentGui extends SimpleGui {
    public enum Page {TASK, USAGE, BEHAVIOR}

    private final ServerPlayer player;
    private final NPCSimpleEntity npc;
    private Page page;

    public NPCAgentGui(ServerPlayer player, NPCSimpleEntity npc) {
        this(player, npc, Page.TASK);
    }

    public NPCAgentGui(ServerPlayer player, NPCSimpleEntity npc, Page page) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.npc = npc;
        this.page = page;
        render();
    }

    private boolean allowed() {
        return npc.isAlive() && !npc.isRemoved() && npc.isOwnedBy(player) && player.isAlive() && !player.hasDisconnected()
                && player.level() == npc.level() && npc.distanceToSqr(player) <= 1024;
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("gui.reverie_dreams.agent." + key, args);
    }

    private static Component skill(String id) {
        return text("skill." + id);
    }

    private static GuiElementBuilder icon(Item item, Component name) {
        return new GuiElementBuilder(item).setName(name);
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private Component target(JsonObject objective) {
        boolean offspring = string(objective, "basis", "").equals("verified_offspring");
        Identifier id = Identifier.tryParse(objective.get("item").getAsString());
        if (id == null)
            return text("none");
        return offspring ? BuiltInRegistries.ENTITY_TYPE.getValue(id).getDescription()
                : new ItemStack(BuiltInRegistries.ITEM.getValue(id)).getHoverName();
    }

    private void render() {
        setTitle(Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal("\ue006")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withFont(new FontDescription.Resource(
                                        ReverieDreams.id("reverie_dreams")
                                ))))
                .append(Component.translatable("space.-168"))
                .append(text("title", npc.getDisplayName()).copy().withStyle(ChatFormatting.WHITE)));
        for (int slot = 0; slot < size; slot++)
            clearSlot(slot);
        setSlot(0, tab(Items.WRITABLE_BOOK, "tab_task", Page.TASK));
        setSlot(1, tab(Items.BOOK, "tab_usage", Page.USAGE));
        setSlot(2, tab(Items.NAME_TAG, "tab_behavior", Page.BEHAVIOR));
        switch (page) {
            case TASK -> renderTask();
            case USAGE -> renderUsage();
            case BEHAVIOR -> renderBehavior();
        }
        setSlot(49, icon(Items.SPYGLASS, text("refresh")).setCallback((slot, click, input, gui) -> {
            if (allowed())
                render();
        }));
        setSlot(53, icon(Items.ARROW, text("back")).setCallback((slot, click, input, gui) -> {
            if (allowed())
                new NPCGui(player, npc).open();
        }));
    }

    private GuiElementBuilder tab(Item item, String key, Page target) {
        var button = icon(item, text(key));
        if (page == target)
            button.glow();
        return button.setCallback((slot, click, input, gui) -> {
            if (allowed()) {
                page = target;
                render();
            }
        });
    }

    private void renderTask() {
        var state = JsonParser.parseString(npc.getSkillTask().context()).getAsJsonObject();
        var status = icon(Items.COMPASS, text("phase", text("phase." + state.get("phase").getAsString())));
        status.addLoreLine(text("current_skill", skill(string(state, "skill", ""))));
        status.addLoreLine(Component.translatable("gui.reverie_dreams.task_reason." + state.get("reason").getAsString()));
        status.addLoreLine(text("idle_seconds", state.get("seconds_without_progress").getAsLong()));
        if (state.has("x"))
            status.addLoreLine(text("site", state.get("x").getAsInt(), state.get("y").getAsInt(), state.get("z").getAsInt()));
        status.addLoreLine(text(state.get("model_reflection_enabled").getAsBoolean() ? "reflection_configured" : "reflection_off"));
        setSlot(9, status);
        if (!npc.getSkillTask().active())
            setSlot(11, icon(Items.PAPER, text("no_active_task")).addLoreLine(text("open_behavior")));
        if (state.has("plan")) {
            var plan = state.getAsJsonObject("plan");
            var steps = plan.getAsJsonArray("steps");
            int cursor = plan.get("cursor").getAsInt();
            setSlot(10, icon(Items.WRITABLE_BOOK, text("goal", plan.get("goal").getAsString()))
                    .addLoreLine(text("steps", Math.min(cursor + 1, steps.size()), steps.size())));
            for (int i = 0; i < steps.size(); i++) {
                var step = steps.get(i).getAsJsonObject();
                var button = icon(i < cursor ? Items.LIME_DYE : Items.PAPER, text("step", i + 1, skill(step.get("skill").getAsString())));
                button.addLoreLine(text(i < cursor ? "step_done" : i == cursor ? "step_current" : "step_pending"));
                button.addLoreLine(text("site", step.get("x").getAsInt(), step.get("y").getAsInt(), step.get("z").getAsInt()));
                if (step.has("target_count")) {
                    var objective = new JsonObject();
                    objective.add("item", step.get(step.has("target_entity") ? "target_entity" : "target_item"));
                    objective.addProperty("basis", step.has("target_entity") ? "verified_offspring" : "verified_production_and_available_inventory");
                    button.addLoreLine(text("planned_quantity", target(objective), step.get("target_count").getAsInt()));
                } else if (step.has("item")) {
                    var objective = new JsonObject();
                    objective.add("item", step.get("item"));
                    button.addLoreLine(text("planned_quantity", target(objective), step.get("count").getAsInt()));
                }
                setSlot(18 + i, button);
            }
        }
        if (state.has("progress")) {
            var progress = state.getAsJsonObject("progress");
            var quantity = icon(Items.HOPPER, text("production", target(progress), progress.get("acquired").getAsInt(), progress.get("goal").getAsInt()));
            String basis = progress.get("basis").getAsString();
            quantity.addLoreLine(text(basis.equals("verified_offspring") ? "offspring_basis"
                    : basis.equals("verified_production_and_available_inventory") ? "production_basis" : "legacy_basis"));
            quantity.addLoreLine(text("remaining", Math.max(0, progress.get("goal").getAsInt() - progress.get("acquired").getAsInt())));
            if (!basis.equals("verified_offspring"))
                quantity.addLoreLine(text("held", progress.get("held").getAsInt()));
            setSlot(27, quantity);
        }
        if (state.has("transfer_item")) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(state.get("transfer_item").getAsString()));
            setSlot(28, icon(Items.CHEST, text("transfer", new ItemStack(item).getHoverName(),
                    state.get("transferred").getAsInt(), state.get("transfer_goal").getAsInt())));
        }
        var events = state.getAsJsonArray("recent_events");
        var journal = icon(Items.CLOCK, text("events"));
        for (int i = Math.max(0, events.size() - 5); i < events.size(); i++) {
            var event = events.get(i).getAsJsonObject();
            journal.addLoreLine(Component.translatable("gui.reverie_dreams.task_reason." + event.get("reason").getAsString()));
        }
        setSlot(36, journal);
        if (state.get("recovering").getAsBoolean())
            setSlot(29, icon(Items.GOLDEN_APPLE, text("recovering_note")));
        UUID expected = npc.getSkillTask().id();
        setSlot(45, control(Items.YELLOW_DYE, "pause", expected, () -> npc.getSkillTask().pause(npc)));
        setSlot(46, control(Items.LIME_DYE, "continue", expected, () -> npc.getSkillTask().continueTask(npc)));
        setSlot(47, control(Items.RED_DYE, "cancel", expected, () -> npc.getSkillTask().cancel(npc, "Cancelled by owner.")));
    }

    private void renderUsage() {
        var usage = NPCUsageView.of(player);
        setSlot(9, icon(Items.PAPER, text("personal_requests"))
                .addLoreLine(text("requests", usage.get("player_requests").getAsLong(), usage.get("player_request_limit").getAsLong()))
                .addLoreLine(text("requests_remaining", usage.get("player_requests_remaining").getAsLong())));
        setSlot(10, icon(Items.EXPERIENCE_BOTTLE, text("personal_tokens"))
                .addLoreLine(text("tokens", usage.get("player_charged_tokens").getAsLong(), usage.get("player_token_limit").getAsLong()))
                .addLoreLine(text("reported", usage.get("player_reported_tokens").getAsLong()))
                .addLoreLine(text("tokens_remaining", usage.get("player_tokens_remaining").getAsLong())));
        setSlot(12, icon(Items.CHEST, text("server_requests"))
                .addLoreLine(text("used_limit", usage.get("server_requests").getAsLong(), usage.get("server_request_limit").getAsLong()))
                .addLoreLine(text("shared_requests", usage.get("server_requests_remaining").getAsLong())));
        setSlot(13, icon(Items.ENDER_CHEST, text("server_tokens"))
                .addLoreLine(text("used_limit", usage.get("server_charged_tokens").getAsLong(), usage.get("server_token_limit").getAsLong()))
                .addLoreLine(text("shared_tokens", usage.get("server_tokens_remaining").getAsLong())));
        setSlot(14, icon(Items.CLOCK, text("background_title"))
                .addLoreLine(text("used_limit", usage.get("background_requests").getAsLong(), usage.get("background_request_limit").getAsLong()))
                .addLoreLine(text("background", usage.get("background_requests_remaining").getAsLong())));
        setSlot(27, icon(Items.DAYLIGHT_DETECTOR, text("day", usage.get("day_utc").getAsString()))
                .addLoreLine(text("reset")).addLoreLine(text("zero_limit")));
        setSlot(28, icon(Items.COMPARATOR, text("ai_available", text(ChatAIManager.canChat(player) ? "yes" : "no")))
                .addLoreLine(text("pending", text(npc.getChatRequests().containsKey(player.getUUID()) || npc.getPendingChatRequests().contains(player.getUUID()) ? "yes" : "no")))
                .addLoreLine(text("panel_free")));
        setSlot(29, icon(Items.KNOWLEDGE_BOOK, text("accounting"))
                .addLoreLine(text("reservation_note")).addLoreLine(text("failed_reservation")).addLoreLine(text("private_usage")));
    }

    private void renderBehavior() {
        var role = npc.getRoleType().identify();
        var behavior = NPCPersonas.behavior(role);
        setSlot(9, icon(Items.NAME_TAG, text("behavior_title", npc.getDisplayName()))
                .addLoreLine(text("style", text("style." + behavior.feedbackStyle())))
                .addLoreLine(text(behavior.configured() ? "adaptation" : "fallback")));
        setSlot(10, icon(Items.GOLDEN_APPLE, text("care." + behavior.selfPreservation()))
                .addLoreLine(text("health_policy", Math.round(behavior.restThreshold() * 100), Math.round(behavior.recoveryThreshold() * 100)))
                .addLoreLine(text("no_free_healing")));
        var preferred = icon(Items.WRITABLE_BOOK, text("preferences"));
        if (behavior.workPreferences().isEmpty())
            preferred.addLoreLine(text("no_preferences"));
        else
            behavior.workPreferences().forEach(id -> preferred.addLoreLine(skill(id)));
        preferred.addLoreLine(text("orders_first"));
        setSlot(11, preferred);
        var source = icon(Items.BOOK, text("evidence"));
        for (String line : behavior.evidence()) {
            int[] points = line.codePoints().toArray();
            for (int from = 0; from < points.length; from += 40)
                source.addLoreLine(Component.literal(new String(points, from, Math.min(40, points.length - from))));
        }
        source.addLoreLine(text("adaptation"));
        setSlot(27, source);
        UUID expected = npc.getSkillTask().id();
        var site = npc.blockPosition().immutable();
        var dimension = npc.level().dimension().identifier();
        int slot = 18;
        for (var suggestion : NPCWorkSuggestions.atCurrentSite(npc)) {
            var definition = NPCSkills.find(suggestion.skill());
            var mode = NPCWorkModes.get(Identifier.fromNamespaceAndPath("reverie_dreams", definition.mode()));
            var button = icon(mode.getItemDisplay().value(), skill(suggestion.skill()))
                    .addLoreLine(text(suggestion.preferred() ? "preferred" : "ordinary"))
                    .addLoreLine(Component.translatable("gui.reverie_dreams.task_reason." + suggestion.reason()));
            if (npc.getSkillTask().active())
                button.addLoreLine(text("finish_current"));
            else if (suggestion.blocked())
                button.addLoreLine(text("blocked_start"));
            else
                button.addLoreLine(text("click_start")).setCallback((index, click, input, gui) -> {
                    if (!allowed())
                        return;
                    String result = NPCWorkSuggestions.startFromPanel(npc, player, expected, role, dimension, site, suggestion.skill());
                    if (result.startsWith("Failed:") || result.startsWith("Denied:"))
                        player.sendSystemMessage(text("conditions_changed"));
                    else
                        page = Page.TASK;
                    render();
                });
            setSlot(slot++, button);
        }
        setSlot(36, icon(Items.COMPASS, text("site", site.getX(), site.getY(), site.getZ()))
                .addLoreLine(text("suggestion_note")).addLoreLine(text("continuous_work")));
    }

    private GuiElementBuilder control(Item item, String label, UUID expected, Runnable action) {
        if (!npc.getSkillTask().active())
            return icon(Items.GRAY_DYE, text(label)).addLoreLine(text("no_active_task"));
        return icon(item, text(label)).setCallback((slot, click, input, gui) -> {
            if (!allowed() || !npc.getSkillTask().active() || !Objects.equals(expected, npc.getSkillTask().id()))
                return;
            npc.cancelChatRequests();
            action.run();
            render();
        });
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!allowed()) {
            close();
            return;
        }
        if (npc.level().getGameTime() % (page == Page.BEHAVIOR ? 100 : 20) == 0)
            render();
    }
}
