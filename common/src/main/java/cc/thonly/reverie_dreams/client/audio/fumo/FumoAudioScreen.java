package cc.thonly.reverie_dreams.client.audio.fumo;

import com.sun.jna.Platform;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * Friendly endpoint names, asynchronous refresh and immediate local configuration saves.
 */
public final class FumoAudioScreen extends Screen {
    private final Screen parent;
    private List<AudioDevice> devices = List.of();
    private boolean requested, loading, listFailed;
    private int revision;

    public FumoAudioScreen(Screen parent) {
        super(Component.translatable("reverie_dreams.fumo.audio.title"));
        this.parent = parent;
    }

    private static Component text(String key) {
        return Component.translatable("reverie_dreams.fumo.audio." + key);
    }

    @Override
    protected void init() {
        int x = width / 2 - 155, y = Math.max(34, (height - 204) / 2);
        FumoAudioConfiguration config = FumoAudioClient.config();
        addRenderableWidget(Button.builder(Component.translatable("reverie_dreams.fumo.audio.enabled", text(config.enabled ? "on" : "off")), b -> {
            config.enabled = !config.enabled;
            FumoAudioClient.save();
            rebuildWidgets();
        }).bounds(x, y, 310, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("reverie_dreams.fumo.audio.source", text(config.source == AudioSource.INPUT ? "input" : "output")), b -> {
            config.source = config.source == AudioSource.INPUT ? AudioSource.OUTPUT : AudioSource.INPUT;
            config.deviceId = "";
            FumoAudioClient.save();
            refresh();
        }).bounds(x, y + 26, 310, 20).build());
        List<AudioDevice> choices = new ArrayList<>();
        choices.add(new AudioDevice("", text("default_device").getString()));
        choices.addAll(devices);
        AudioDevice selected = choices.stream().filter(d -> d.id().equals(config.deviceId)).findFirst()
                .orElseGet(() -> new AudioDevice(config.deviceId, text("missing_device").getString()));
        if (choices.stream().noneMatch(d -> d.id().equals(selected.id())))
            choices.add(selected);
        CycleButton<AudioDevice> selector = CycleButton.builder((AudioDevice d) -> Component.literal(d.name()), selected)
                .withValues(choices).create(x, y + 52, 310, 20, text("device"), (b, d) -> {
                    config.deviceId = d.id();
                    FumoAudioClient.save();
                    b.setTooltip(Tooltip.create(Component.literal(d.name())));
                });
        selector.active = !loading && !(config.source == AudioSource.OUTPUT && !Platform.isWindows());
        selector.setTooltip(Tooltip.create(Component.literal(selected.name())));
        addRenderableWidget(selector);
        Button refresh = Button.builder(text(loading ? "refreshing" : "refresh"), b -> refresh()).bounds(x, y + 78, 310, 20).build();
        refresh.active = !loading;
        addRenderableWidget(refresh);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(width / 2 - 100, height - 28, 200, 20).build());
        if (!requested)
            refresh();
    }

    private void refresh() {
        requested = true;
        loading = true;
        listFailed = false;
        devices = List.of();
        int request = ++revision;
        AudioSource source = FumoAudioClient.config().source;
        rebuildWidgets();
        FumoAudioClient.controller().devices(source).whenComplete((result, error) -> minecraft.execute(() -> {
            if (request != revision)
                return;
            loading = false;
            listFailed = error != null;
            devices = result == null ? List.of() : result;
            if (minecraft.screen == this)
                rebuildWidgets();
        }));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 15, 0xffeeeeee);
        int y = Math.max(34, (height - 204) / 2) + 108;
        String status = !FumoAudioClient.config().enabled ? "disabled" : minecraft.level == null ? "waiting_world"
                : FumoAudioClient.controller().status() == FumoAudioController.Status.ERROR ? "error" : "listening";
        graphics.centeredText(font, text(status), width / 2, y, 0xffdddddd);
        Component help = FumoAudioClient.config().source == AudioSource.OUTPUT && !Platform.isWindows() ? text("output_windows_only")
                : listFailed ? text("list_error") : text("hint");
        int lineY = y + 17;
        for (var line : font.split(help, Math.max(180, Math.min(width - 24, 350)))) {
            graphics.centeredText(font, line, width / 2, lineY, 0xffaaaaaa);
            lineY += 11;
        }
    }

    @Override
    public void onClose() {
        revision++;
        minecraft.setScreen(parent);
    }
}
