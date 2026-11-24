package cc.thonly.reverie_dreams.client.gui;

import cc.thonly.reverie_dreams.compat.ysm.NPCAnimationActions;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NPCActionWheelScreen extends Screen {
    private static final int SLOTS = 6;
    private static final int KEEPALIVE_TICKS = 20;
    private final @Nullable Screen parent;
    private final List<Entry> actions;
    private final NPCAnimationActions.OpenWheel wheel;
    private final String model;
    private int keepalive;
    private @Nullable Entry group;
    private int page;
    private int rootPage;
    private int centerX;
    private int centerY;
    private int horizontalRadius;
    private int verticalRadius;

    public NPCActionWheelScreen(@Nullable Screen parent, Component companion, List<Entry> actions,
                                NPCAnimationActions.OpenWheel wheel, String model) {
        super(Component.translatableWithFallback("gui.npc.actions.title", "%s · Actions", companion));
        this.parent = parent;
        this.actions = List.copyOf(actions);
        this.wheel = wheel;
        this.model = model;
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        int buttonWidth = Math.clamp(width / 4, 52, 112);
        horizontalRadius = Math.clamp(width / 3 - 18, 68, 142);
        verticalRadius = Math.clamp(height / 2 - 53, 25, 78);

        List<Entry> visible = currentEntries();
        int first = page * SLOTS;
        for (int slot = 0; slot < SLOTS && first + slot < visible.size(); slot++) {
            Entry entry = visible.get(first + slot);
            double angle = Math.PI * slot / 3 - Math.PI / 2;
            int x = centerX + (int) Math.round(Math.cos(angle) * horizontalRadius);
            int y = centerY + (int) Math.round(Math.sin(angle) * verticalRadius);
            String fullLabel = entry.label().getString();
            String label = font.width(fullLabel) <= buttonWidth - 10
                    ? fullLabel : font.plainSubstrByWidth(fullLabel, buttonWidth - 19) + "…";
            Button button = Button.builder(Component.literal(label), ignored -> choose(entry))
                    .bounds(x - buttonWidth / 2, y - 10, buttonWidth, 20).build();
            button.setTooltip(Tooltip.create(entry.label()));
            addRenderableWidget(button);
        }

        addRenderableWidget(Button.builder(Component.translatableWithFallback("gui.npc.actions.stop", "Stop"), ignored -> submit(""))
                .bounds(centerX - 32, centerY - 10, 64, 20).build());

        int bottom = height - 27;
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), ignored -> onClose())
                .bounds(centerX - 35, bottom, 70, 20).build());
        if (visible.size() > SLOTS) {
            Button previous = addRenderableWidget(Button.builder(Component.translatableWithFallback("gui.npc.actions.previous", "Previous"),
                    ignored -> changePage(-1)).bounds(centerX - 114, bottom, 70, 20).build());
            previous.active = page > 0;
            Button next = addRenderableWidget(Button.builder(Component.translatableWithFallback("gui.npc.actions.next", "Next"),
                    ignored -> changePage(1)).bounds(centerX + 44, bottom, 70, 20).build());
            next.active = (page + 1) * SLOTS < visible.size();
        }
    }

    private List<Entry> currentEntries() {
        return group == null ? actions : group.children();
    }

    private void choose(Entry entry) {
        if (entry.children().isEmpty()) {
            submit(entry.animation());
        } else {
            rootPage = page;
            group = entry;
            page = 0;
            rebuildWidgets();
        }
    }

    private void submit(String animation) {
        if (NetworkManager.canServerReceive(NPCAnimationActions.SelectAction.ID)) {
            NetworkManager.sendToServer(new NPCAnimationActions.SelectAction(wheel, model, animation));
        }
        minecraft.setScreen(parent);
    }

    private void changePage(int direction) {
        int lastPage = Math.max(0, (currentEntries().size() - 1) / SLOTS);
        page = Math.clamp(page + direction, 0, lastPage);
        rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY != 0 && currentEntries().size() > SLOTS) {
            changePage(scrollY < 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (group == null) {
            minecraft.setScreen(parent);
        } else {
            group = null;
            page = rootPage;
            rebuildWidgets();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        var target = wheel.target();
        if (minecraft.level == null || minecraft.player == null || !minecraft.player.isAlive()
                || !(minecraft.level.getEntity(target.entityId()) instanceof NPCSimpleEntity npc)
                || !npc.isAlive() || !target.entityUuid().equals(npc.getUUID())
                || !model.equals(npc.reverie_dreams$getModelId())
                || !minecraft.player.isWithinEntityInteractionRange(npc, 0.0)) {
            minecraft.setScreen(parent);
            return;
        }
        if (keepalive-- == 0) {
            NPCAnimationActions.reportWheel(wheel, true);
            keepalive = KEEPALIVE_TICKS - 1;
        }
    }

    @Override
    public void removed() {
        if (minecraft.getConnection() != null) {
            NPCAnimationActions.reportWheel(wheel, false);
        }
        super.removed();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xa615121f);
        int outerX = horizontalRadius + 23;
        int outerY = verticalRadius + 16;
        for (int dy = -outerY; dy <= outerY; dy++) {
            int dx = (int) (outerX * Math.sqrt(1.0 - (double) dy * dy / (outerY * outerY)));
            graphics.fill(centerX - dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, 0xd0571f56);
            if (dx >= 2 && Math.abs(dy) < outerY) {
                graphics.fill(centerX - dx + 2, centerY + dy, centerX + dx - 1, centerY + dy + 1, 0xff231b2b);
            }
        }
        String heading = (group == null ? title : group.label()).getString();
        if (font.width(heading) > width - 24) {
            heading = font.plainSubstrByWidth(heading, width - 32) + "…";
        }
        graphics.centeredText(font, heading, centerX, Math.max(8, centerY - outerY - 14), 0xffc7f5e4);
        if (currentEntries().isEmpty()) {
            graphics.centeredText(font, Component.translatableWithFallback("gui.npc.actions.empty", "This model has no playable wheel actions."),
                    centerX, centerY - 29, 0xfff4eceb);
        }
        int pages = Math.max(1, (currentEntries().size() + SLOTS - 1) / SLOTS);
        if (pages > 1) {
            graphics.centeredText(font, (page + 1) + " / " + pages, centerX, height - 39, 0xfff4eceb);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    public record Entry(Component label, String animation, List<Entry> children) {
        public static Entry action(Component label, String animation) {
            return new Entry(label, animation, List.of());
        }

        public static Entry group(Component label, List<Entry> actions) {
            return new Entry(label, "", List.copyOf(actions));
        }
    }
}
