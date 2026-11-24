package cc.thonly.reverie_dreams.client.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

abstract class SettingsMenuScreen extends Screen {
    private final Screen parent;
    private HeaderAndFooterLayout layout;
    private LinearLayout options;

    SettingsMenuScreen(Screen parent, String titleKey) {
        super(Component.translatable(titleKey));
        this.parent = parent;
    }

    @Override
    protected final void init() {
        layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(title, font);
        options = LinearLayout.vertical().spacing(8);
        options.defaultCellSetting().alignHorizontallyCenter();
        layout.addToContents(options);
        addOptions();
        layout.addToFooter(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .width(Math.min(200, contentWidth())).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    protected abstract void addOptions();

    protected final void addAction(Component label, Runnable openScreen) {
        options.addChild(Button.builder(label, button -> openScreen.run())
                .width(Math.min(240, contentWidth())).build());
    }

    protected final void addToggle(Component label, boolean value, Consumer<Boolean> changed) {
        options.addChild(CycleButton.onOffBuilder(value).create(
                0, 0, Math.min(240, contentWidth()), 20, label, (button, enabled) -> changed.accept(enabled)));
    }

    protected final void addHeading(String key) {
        addText(Component.translatable(key).withStyle(ChatFormatting.BOLD));
    }

    protected final void addExplanation(String key) {
        addText(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    private void addText(Component text) {
        options.addChild(new MultiLineTextWidget(text, font).setMaxWidth(contentWidth()).setCentered(true));
    }

    private int contentWidth() {
        return Math.max(1, Math.min(320, width - 32));
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public final void onClose() {
        minecraft.setScreen(parent);
    }
}
