package cc.thonly.reverie_dreams.neoforge.integration.sparkle.screen;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCCapability;
import com.google.common.collect.Lists;
import com.micaftic.morpher.client.animation.custom.CustomRouletteLayout;
import com.micaftic.morpher.client.animation.custom.CustomRouletteStore;
import com.micaftic.morpher.client.event.AnimationLockEvent;
import com.micaftic.morpher.client.gui.CustomRouletteEditorScreen;
import com.micaftic.morpher.client.gui.ModelMetadataPresenter;
import com.micaftic.morpher.client.gui.custom.ExtraAnimationButtons;
import com.micaftic.morpher.client.input.AnimationRouletteKey;
import com.micaftic.morpher.client.input.ExtraAnimationKey;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.config.GeneralConfig;
import com.micaftic.morpher.config.GeneralConfig.RouletteContentMode;
import com.micaftic.morpher.core.api.client.KeyMappingFactory;
import com.micaftic.morpher.core.gpu.Pie;
import com.micaftic.morpher.core.gui.ModelSettingsScreen;
import com.micaftic.morpher.core.gui.RouletteIcons;
import com.micaftic.morpher.core.gui.RoulettePanelStyle;
import com.micaftic.morpher.core.gui.RoulettePanelStyle.Glyph;
import com.micaftic.morpher.geckolib3.core.AnimatableEntity;
import com.micaftic.morpher.network.NetworkHandler;
import com.micaftic.morpher.network.message.C2SPlayAnimationPacket;
import com.micaftic.morpher.util.AnimationRouletteDebugLog;
import com.micaftic.morpher.util.data.OrderedStringMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class NPCUnifiedRouletteScreen extends Screen {
    private static final LinkedList<Pair<String, Integer>> navigationStack = Lists.newLinkedList();
    private static String lastModelId = "";
    private static OrderedStringMap<String, String> customRootProperties = null;
    private static Map<String, OrderedStringMap<String, String>> customClassifyMap = null;
    private static Map<String, Integer> customOriginalIndexMap = new HashMap<>();
    private static Map<String, String> customOriginalCategoryMap = new HashMap<>();
    private static boolean usingCustomLayout = false;
    private int centerX;
    private int centerY;
    private float layoutScale = 1.0F;
    private int hoveredIndex = -1;
    private int hoveredGearIndex = -1;
    private int hoveredPathSegment = -1;
    private boolean hoveredPrev;
    private boolean hoveredNext;
    private boolean hoveredEdit;
    private Supplier<Screen> pendingScreenFactory;
    private Pair<String, Integer> currentNavEntry;
    private final OrderedStringMap<String, String> currentProperties;
    private final Map<String, ExtraAnimationButtons> renderGroups;
    private final Map<String, OrderedStringMap<String, String>> textProperties;
    private final AnimatableEntity<?> animatableModel;
    private final ModelAssembly renderContext;
    private final BaseNPCLikeEntity npc;

    public NPCUnifiedRouletteScreen(BaseNPCLikeEntity npc, String modelId, ModelAssembly modelAssembly, AnimatableEntity<?> animatable) {
        super(Component.literal("sparkle Roulette"));
        this.npc = npc;
        this.renderContext = modelAssembly;
        this.animatableModel = animatable;
        OrderedStringMap<String, String> customRoot = null;
        Map<String, OrderedStringMap<String, String>> customClassify = null;
        if (!lastModelId.equals(modelId)) {
            navigationStack.clear();
            lastModelId = modelId;
            if (GeneralConfig.ROULETTE_CONTENT_MODE.get() == RouletteContentMode.CUSTOM) {
                CustomRouletteLayout layout = CustomRouletteStore.load(modelId);
                if (layout != null) {
                    usingCustomLayout = true;
                    customRoot = CustomRouletteStore.buildRootMap(layout);
                    customClassify = CustomRouletteStore.buildClassifyMap(layout);
                    customOriginalIndexMap = CustomRouletteStore.buildIndexMap(layout);
                    customOriginalCategoryMap = CustomRouletteStore.buildCategoryMap(layout);
                    customRootProperties = customRoot;
                    customClassifyMap = customClassify;
                } else {
                    resetCustomState();
                }
            } else {
                resetCustomState();
            }
        } else if (usingCustomLayout) {
            customRoot = customRootProperties;
            customClassify = customClassifyMap;
        }

        this.textProperties = customClassify != null ? customClassify : modelAssembly.getModelData().getModelProperties().getExtraAnimationClassify();
        this.renderGroups = modelAssembly.getModelData().getModelProperties().getExtraAnimationButtons();
        if (navigationStack.isEmpty()) {
            navigationStack.add(MutablePair.of("", 0));
        }

        this.currentNavEntry = (Pair) navigationStack.peekLast();
        if (this.textProperties.containsKey(this.currentNavEntry.getLeft())) {
            this.currentProperties = (OrderedStringMap) this.textProperties.get(this.currentNavEntry.getLeft());
        } else {
            this.currentProperties = customRoot != null ? customRoot : modelAssembly.getModelData().getModelProperties().getExtraAnimation();
            navigationStack.clear();
            navigationStack.add(MutablePair.of("", (Integer) this.currentNavEntry.getRight()));
            this.currentNavEntry = (Pair) navigationStack.peekLast();
        }

    }

    private static void resetCustomState() {
        usingCustomLayout = false;
        customRootProperties = null;
        customClassifyMap = null;
        customOriginalIndexMap.clear();
        customOriginalCategoryMap.clear();
    }

    public static void setInitialSubmenu(String submenuKey) {
        navigationStack.clear();
        navigationStack.add(MutablePair.of("", 0));
        if (submenuKey != null && !submenuKey.isEmpty()) {
            navigationStack.addLast(MutablePair.of(submenuKey, 0));
        }

    }

    protected void init() {
        this.clearWidgets();
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;
        this.layoutScale = this.computeLayoutScale();
        if ((Integer) this.currentNavEntry.getRight() >= this.pageCount()) {
            this.currentNavEntry.setValue(0);
        }

    }

    private float computeLayoutScale() {
        return Math.min(1.0F, Math.min((float) this.width / 380.0F, (float) this.height / 340.0F));
    }

    private int logicalMouseX(double mouseX) {
        return (int) Math.round((double) this.centerX + (mouseX - (double) this.centerX) / (double) this.layoutScale);
    }

    private int logicalMouseY(double mouseY) {
        return (int) Math.round((double) this.centerY + (mouseY - (double) this.centerY) / (double) this.layoutScale);
    }

    private int pageCount() {
        return Math.max(1, (this.currentProperties.size() + 7) / 8);
    }

    private int page() {
        return (Integer) this.currentNavEntry.getRight();
    }

    private float sliceStartOffset() {
        return (-(float) Math.PI / 8F);
    }

    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        extractor.fill(0, 0, this.width, this.height, 2047149834);
        int lmx = this.logicalMouseX((double) mouseX);
        int lmy = this.logicalMouseY((double) mouseY);
        this.updateHover(lmx, lmy);
        boolean scaled = this.layoutScale < 0.999F;
        if (scaled) {
            extractor.pose().pushMatrix();
            extractor.pose().translate((float) this.centerX, (float) this.centerY);
            extractor.pose().scale(this.layoutScale, this.layoutScale);
            extractor.pose().translate((float) (-this.centerX), (float) (-this.centerY));
        }

        this.renderSlices(extractor);
        this.renderLabels(extractor);
        this.renderCenter(extractor);
        this.renderPageButtons(extractor);
        this.renderEditButton(extractor);
        this.renderPathAndPage(extractor, lmx, lmy);
        if (scaled) {
            extractor.pose().popMatrix();
        }

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    public void tick() {
        super.tick();
        if (this.pendingScreenFactory != null) {
            Supplier<Screen> factory = this.pendingScreenFactory;
            this.pendingScreenFactory = null;
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen((Screen) factory.get()));
        }

    }

    private void updateHover(int mouseX, int mouseY) {
        float dx = (float) (mouseX - this.centerX);
        float dy = (float) (mouseY - this.centerY);
        float r = (float) Math.sqrt((double) (dx * dx + dy * dy));
        float ang = (float) Math.atan2((double) dy, (double) dx);
        if (ang < 0.0F) {
            ang += ((float) Math.PI * 2F);
        }

        ang = (ang - this.sliceStartOffset() + ((float) Math.PI * 2F)) % ((float) Math.PI * 2F);
        int idx = Mth.clamp((int) (ang / ((float) Math.PI / 4F)), 0, 7);
        this.hoveredIndex = -1;
        this.hoveredGearIndex = -1;
        int absoluteIdx = idx + this.page() * 8;
        if (absoluteIdx < this.currentProperties.size() && r >= 30.0F && r <= 128.0F) {
            boolean hasGear = ((String) this.currentProperties.getValueAt(absoluteIdx)).startsWith("#");
            if (hasGear && r <= 56.0F) {
                this.hoveredGearIndex = absoluteIdx;
            } else {
                this.hoveredIndex = absoluteIdx;
            }
        }

        float prevDx = (float) mouseX - ((float) this.centerX - 160.0F);
        float nextDx = (float) mouseX - ((float) this.centerX + 160.0F);
        float btnDy = (float) (mouseY - this.centerY);
        float rr = 324.0F;
        this.hoveredPrev = this.page() > 0 && prevDx * prevDx + btnDy * btnDy <= rr;
        this.hoveredNext = (this.page() + 1) * 8 < this.currentProperties.size() && nextDx * nextDx + btnDy * btnDy <= rr;
        this.hoveredEdit = RoulettePanelStyle.inside((double) mouseX, (double) mouseY, this.editButtonX(), this.editButtonY(), 18, 18);
    }

    private void renderSlices(GuiGraphicsExtractor g) {
        float sliceSpan = ((float) Math.PI / 4F);

        for (int i = 0; i < 8; ++i) {
            int absoluteIdx = i + this.page() * 8;
            if (absoluteIdx >= this.currentProperties.size()) {
                this.drawGlassSlice(g, i, sliceSpan, 30.0F, 128.0F, 574113106, false);
            } else {
                boolean isHover = absoluteIdx == this.hoveredIndex;
                boolean gearHover = absoluteIdx == this.hoveredGearIndex;
                boolean isSubmenu = ((String) this.currentProperties.getKeyAt(absoluteIdx)).startsWith("#");
                boolean hasGear = ((String) this.currentProperties.getValueAt(absoluteIdx)).startsWith("#");
                int mainColor;
                if (isSubmenu) {
                    mainColor = isHover ? -922754141 : -2140441464;
                } else {
                    mainColor = isHover ? -1193481217 : 2019519354;
                }

                if (hasGear) {
                    int gearColor = gearHover ? -654311425 : -2137347910;
                    this.drawGlassSlice(g, i, sliceSpan, 56.0F, 128.0F, mainColor, isHover);
                    this.drawGlassSlice(g, i, sliceSpan, 30.0F, 56.0F, gearColor, gearHover);
                    this.drawGearIcon(g, i, sliceSpan);
                } else {
                    this.drawGlassSlice(g, i, sliceSpan, 30.0F, 128.0F, mainColor, isHover);
                }
            }
        }

    }

    private void drawGlassSlice(GuiGraphicsExtractor g, int sliceIndex, float sliceSpan, float inner, float outer, int color, boolean hover) {
        float start = this.sliceStartOffset() + (float) sliceIndex * sliceSpan + 0.02F;
        float end = this.sliceStartOffset() + (float) (sliceIndex + 1) * sliceSpan - 0.02F;
        Pie.draw(g, (float) this.centerX, (float) (this.centerY + 2), inner + 1.0F, outer + 1.0F, start, end, 973078528, 1.0F);
        Pie.draw(g, (float) this.centerX, (float) this.centerY, inner, outer, start, end, color, 1.0F);
        Pie.draw(g, (float) this.centerX, (float) this.centerY, Math.max(0.0F, outer - 2.0F), outer, start, end, hover ? -654311425 : -1459617793, 1.0F);
        Pie.draw(g, (float) this.centerX, (float) this.centerY, inner, inner + 1.5F, start, end, 922746879, 1.0F);
    }

    private void drawGearIcon(GuiGraphicsExtractor g, int sliceIndex, float sliceSpan) {
        float mid = this.sliceStartOffset() + ((float) sliceIndex + 0.5F) * sliceSpan;
        float r = 43.0F;
        int size = 14;
        int ix = this.centerX + (int) ((double) r * Math.cos((double) mid)) - size / 2;
        int iy = this.centerY + (int) ((double) r * Math.sin((double) mid)) - size / 2;
        g.blit(RouletteIcons.SETTINGS, ix, iy, ix + size, iy + size, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private void renderLabels(GuiGraphicsExtractor g) {
        float sliceSpan = ((float) Math.PI / 4F);

        for (int i = 0; i < 8; ++i) {
            int absoluteIdx = i + this.page() * 8;
            if (absoluteIdx < this.currentProperties.size()) {
                float midAngle = this.sliceStartOffset() + ((float) i + 0.5F) * sliceSpan;
                boolean hasGear = ((String) this.currentProperties.getValueAt(absoluteIdx)).startsWith("#");
                boolean isSubmenuLink = ((String) this.currentProperties.getKeyAt(absoluteIdx)).startsWith("#");
                boolean hover = absoluteIdx == this.hoveredIndex || absoluteIdx == this.hoveredGearIndex;
                float innerEdge = hasGear ? 56.0F : 30.0F;
                float labelR = (innerEdge + 128.0F) * 0.5F + 4.0F;
                int lx = this.centerX + Math.round(labelR * (float) Math.cos((double) midAngle));
                int ly = this.centerY + Math.round(labelR * (float) Math.sin((double) midAngle));
                String text = this.displayLabel(absoluteIdx);
                if (!StringUtils.isBlank(text)) {
                    List<KeyMapping> bindings = ExtraAnimationKey.getKeyMappings();
                    boolean showKey = this.page() == 0 && navigationStack.size() == 1 && absoluteIdx < bindings.size();
                    int labelWidth = this.sliceTextWidth(labelR, innerEdge);
                    int iconSize = this.optionIconSize(absoluteIdx, isSubmenuLink, hasGear);
                    int totalH = iconSize > 0 ? iconSize + 12 + (showKey ? 10 : 0) : 10 + (showKey ? 10 : 0);
                    int y = ly - totalH / 2;
                    if (iconSize > 0) {
                        this.drawOptionIcon(g, absoluteIdx, isSubmenuLink, hasGear, lx, y, iconSize, hover);
                        y += iconSize + 3;
                    }

                    int textColor = isSubmenuLink ? -7261 : -1;
                    this.drawSliceLabel(g, text, lx, y, labelWidth, textColor, hover);
                    if (showKey) {
                        this.renderKeyBinding(g, absoluteIdx, lx, y + 10, Math.min(labelWidth, 64), bindings, hover);
                    }
                }
            }
        }

    }

    private void renderKeyBinding(GuiGraphicsExtractor g, int slot, int x, int y, int maxWidth, List<KeyMapping> bindings, boolean hover) {
        if (slot < bindings.size()) {
            KeyMapping km = (KeyMapping) bindings.get(slot);
            MutableComponent label = Component.literal("[ ").withStyle(ChatFormatting.YELLOW);
            if (km.isUnbound()) {
                label.append(Component.translatable("key.sparkle_morpher.extra_animation.none"));
            } else {
                label.append(km.getTranslatedKeyMessage());
            }

            label.append(" ]");
            this.drawSliceLabel(g, label.getString(), x, y, maxWidth, -2298881, hover);
        }
    }

    private int sliceTextWidth(float labelRadius, float innerEdge) {
        float radiusRoom = Math.min(labelRadius - innerEdge - 4.0F, 128.0F - labelRadius - 6.0F);
        float chord = (float) ((double) 2.0F * (double) labelRadius * Math.sin(0.3526990935206413));
        return Mth.clamp((int) Math.min(chord - 18.0F, radiusRoom * 2.0F + 42.0F), 34, 82);
    }

    private int optionIconSize(int absoluteIdx, boolean submenu, boolean hasGear) {
        String key = (String) this.currentProperties.getKeyAt(absoluteIdx);
        return !submenu && !"#return".equals(key) && !hasGear ? 0 : 16;
    }

    private void drawOptionIcon(GuiGraphicsExtractor g, int absoluteIdx, boolean submenu, boolean hasGear, int centerX, int y, int size, boolean hover) {
        Identifier tex = null;
        String key = (String) this.currentProperties.getKeyAt(absoluteIdx);
        if ("#return".equals(key)) {
            tex = RouletteIcons.ARROW_LEFT;
        } else if (hasGear) {
            tex = RouletteIcons.SETTINGS;
        } else if (submenu) {
            tex = RouletteIcons.ARROW_RIGHT;
        }

        if (tex != null) {
            int pad = hover ? 3 : 2;
            int cx = centerX - size / 2;
            Pie.draw(g, (float) centerX, (float) y + (float) size / 2.0F, 0.0F, (float) size / 2.0F + (float) pad, 0.0F, ((float) Math.PI * 2F), hover ? -2130706433 : 1157627903, 1.0F);
            g.blit(tex, cx, y, cx + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
        }
    }

    private void drawSliceLabel(GuiGraphicsExtractor g, String text, int centerX, int y, int maxWidth, int color, boolean hover) {
        String clean = text.replace('\n', ' ').trim();
        if (!clean.isEmpty() && maxWidth > 4) {
            int textWidth = this.font.width(clean);
            int left = centerX - maxWidth / 2;
            if (textWidth <= maxWidth) {
                g.text(this.font, clean, centerX - textWidth / 2, y, color, true);
            } else if (hover && !(this.layoutScale < 0.999F)) {
                int travel = textWidth - maxWidth + 14;
                int offset = (int) (System.currentTimeMillis() / 55L % (long) Math.max(1, travel * 2));
                if (offset > travel) {
                    offset = travel * 2 - offset;
                }

                g.enableScissor(left, y - 1, left + maxWidth, y + 10);

                try {
                    g.text(this.font, clean, left - offset + 7, y, color, true);
                } finally {
                    g.disableScissor();
                }

            } else {
                String clipped = this.trimToWidth(clean, maxWidth);
                g.text(this.font, clipped, centerX - this.font.width(clipped) / 2, y, color, true);
            }
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        } else {
            String ellipsis = "...";

            int keep;
            for (keep = text.length(); keep > 0; --keep) {
                Font var10000 = this.font;
                String var10001 = text.substring(0, keep);
                if (var10000.width(var10001 + ellipsis) <= maxWidth) {
                    break;
                }
            }

            String var5 = text.substring(0, Math.max(0, keep));
            return var5 + ellipsis;
        }
    }

    private String displayLabel(int absoluteIdx) {
        String key = (String) this.currentProperties.getKeyAt(absoluteIdx);
        String value = (String) this.currentProperties.getValueAt(absoluteIdx);
        String display = value;
        if (value.startsWith("#")) {
            String sub = value.substring(1);
            if (this.renderGroups.containsKey(sub)) {
                display = ((ExtraAnimationButtons) this.renderGroups.get(sub)).getName();
            }
        }

        if (StringUtils.isBlank(display)) {
            display = key;
        }

        return ModelMetadataPresenter.getLocalizedModelString(this.renderContext, "properties.extra_animation.%s".formatted(key), display);
    }

    private void renderCenter(GuiGraphicsExtractor g) {
        if (this.animatableModel.getEntity() instanceof NPCSimpleEntity) {
            boolean locked = AnimationLockEvent.isLocked();
            int tint = locked ? -1486968 : -8658780;
            Pie.draw(g, (float) this.centerX, (float) this.centerY, 0.0F, 28.0F, 0.0F, ((float) Math.PI * 2F), tint, 1.0F);
            Pie.draw(g, (float) this.centerX, (float) this.centerY, 28.0F, 30.0F, 0.0F, ((float) Math.PI * 2F), -1191182337, 1.0F);
            Identifier tex = locked ? RouletteIcons.LOCK : RouletteIcons.UNLOCK;
            int size = 28;
            int ix = this.centerX - size / 2;
            int iy = this.centerY - size / 2;
            g.blit(tex, ix, iy, ix + size, iy + size, 0.0F, 1.0F, 0.0F, 1.0F);
        } else {
            Pie.draw(g, (float) this.centerX, (float) this.centerY, 0.0F, 30.0F, 0.0F, ((float) Math.PI * 2F), -1607781312, 1.0F);
            g.centeredText(this.font, Component.translatable("gui.sparkle_morpher.roulette.stop"), this.centerX, this.centerY - 4, -6475);
        }

    }

    private void renderPageButtons(GuiGraphicsExtractor g) {
        if (this.pageCount() > 1) {
            boolean prevEnabled = this.page() > 0;
            boolean nextEnabled = (this.page() + 1) * 8 < this.currentProperties.size();
            this.drawPageButton(g, (float) this.centerX - 160.0F, (float) this.centerY, prevEnabled, this.hoveredPrev, true);
            this.drawPageButton(g, (float) this.centerX + 160.0F, (float) this.centerY, nextEnabled, this.hoveredNext, false);
        }
    }

    private void renderEditButton(GuiGraphicsExtractor g) {
        RoulettePanelStyle.iconButton(g, this.hoveredEdit ? this.editButtonX() + 1 : -1, this.hoveredEdit ? this.editButtonY() + 1 : -1, this.editButtonX(), this.editButtonY(), Glyph.ROULETTE, true);
    }

    private int editButtonX() {
        return this.centerX - 9;
    }

    private int editButtonY() {
        return this.height - 24;
    }

    private void drawPageButton(GuiGraphicsExtractor g, float cx, float cy, boolean enabled, boolean hover, boolean left) {
        int fill;
        if (!enabled) {
            fill = 1079334246;
        } else {
            fill = hover ? -925045761 : -2141099144;
        }

        Pie.draw(g, cx, cy, 0.0F, 18.0F, 0.0F, ((float) Math.PI * 2F), fill, 1.0F);
        Pie.draw(g, cx, cy, 16.5F, 18.0F, 0.0F, ((float) Math.PI * 2F), -1459617793, 1.0F);
        Identifier tex = left ? RouletteIcons.ARROW_LEFT : RouletteIcons.ARROW_RIGHT;
        int size = 14;
        int ix = (int) cx - size / 2;
        int iy = (int) cy - size / 2;
        g.blit(tex, ix, iy, ix + size, iy + size, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private void renderPathAndPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        this.layoutAndDrawPath(g, mouseX, mouseY);
        String pageStr = String.format("%d/%d", this.page() + 1, this.pageCount());
        g.centeredText(this.font, Component.literal(pageStr), this.centerX, this.centerY + 136, -30028);
    }

    private void layoutAndDrawPath(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int pathY = this.centerY + -150;
        String prefix = Component.translatable("gui.sparkle_morpher.roulette.path.prefix").getString();
        String rootLabel = Component.translatable("gui.sparkle_morpher.roulette.path.root").getString();
        int prefixW = this.font.width(prefix);
        int sep = this.font.width(" > ");
        int total = prefixW;

        for (int i = 0; i < navigationStack.size(); ++i) {
            String s = (String) ((Pair) navigationStack.get(i)).getLeft();
            total += this.font.width(StringUtils.isBlank(s) ? rootLabel : s);
            if (i < navigationStack.size() - 1) {
                total += sep;
            }
        }

        int x = this.centerX - total / 2;
        g.text(this.font, prefix, x, pathY, -4739127, true);
        x += prefixW;
        this.hoveredPathSegment = -1;

        for (int i = 0; i < navigationStack.size(); ++i) {
            String raw = (String) ((Pair) navigationStack.get(i)).getLeft();
            String s = StringUtils.isBlank(raw) ? rootLabel : raw;
            int w = this.font.width(s);
            boolean isLast = i == navigationStack.size() - 1;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= pathY - 2 && mouseY < pathY + 10;
            int color = isLast ? -14249 : (hover ? -1 : -4739127);
            g.text(this.font, s, x, pathY, color, true);
            if (hover && !isLast) {
                g.fill(x, pathY + 9, x + w, pathY + 10, color);
                this.hoveredPathSegment = i;
            }

            x += w;
            if (i < navigationStack.size() - 1) {
                g.text(this.font, " > ", x, pathY, -8492386, true);
                x += sep;
            }
        }

    }

    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        if (this.hoveredPrev) {
            this.playClick();
            this.previousPage();
            return true;
        } else if (this.hoveredNext) {
            this.playClick();
            this.nextPage();
            return true;
        } else if (this.hoveredPathSegment >= 0 && this.hoveredPathSegment < navigationStack.size() - 1) {
            this.playClick();
            this.navigateTo(this.hoveredPathSegment);
            return true;
        } else if (this.hoveredEdit) {
            this.playClick();
            this.openScreenNextTick(() -> new CustomRouletteEditorScreen(lastModelId, this.renderContext));
            return true;
        } else {
            if (this.hoveredGearIndex >= 0) {
                this.playClick();
                String value = (String) this.currentProperties.getValueAt(this.hoveredGearIndex);
                if (value.startsWith("#")) {
                    String sub = value.substring(1);
                    if (this.hasConfigGroup(sub)) {
                        this.openScreenNextTick(() -> new ModelSettingsScreen(this.renderContext, this.animatableModel, this, sub, false));
                        return true;
                    }
                }
            }

            if (this.hoveredIndex >= 0) {
                this.playClick();
                String key = (String) this.currentProperties.getKeyAt(this.hoveredIndex);
                if ("#return".equals(key)) {
                    this.navigateBack();
                } else if (key.startsWith("#")) {
                    this.navigateToSubmenu(key);
                } else {
                    this.playAnimation(key);
                }

                return true;
            } else {
                double cdx = (double) (this.logicalMouseX(event.x()) - this.centerX);
                double cdy = (double) (this.logicalMouseY(event.y()) - this.centerY);
                if (cdx * cdx + cdy * cdy <= (double) 900.0F) {
                    if (this.animatableModel.getEntity() instanceof NPCSimpleEntity) {
                        AnimationLockEvent.toggleLock();
                    } else {
                        NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(this.animatableModel.getEntity().getId()));
                        this.onClose();
                    }

                    return true;
                } else {
                    return super.mouseClicked(event, flag);
                }
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < (double) 0.0F) {
            this.nextPage();
        } else {
            this.previousPage();
        }

        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (KeyMappingFactory.isActiveAndMatches(AnimationRouletteKey.KEY_ROULETTE, event.key(), event.scancode())) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(event);
        }
    }

    private void previousPage() {
        this.currentNavEntry.setValue(Math.max(0, this.page() - 1));
    }

    private void nextPage() {
        if ((this.page() + 1) * 8 < this.currentProperties.size()) {
            this.currentNavEntry.setValue(this.page() + 1);
        }

    }

    private void navigateTo(int targetIndex) {
        while (navigationStack.size() > targetIndex + 1) {
            navigationStack.removeLast();
        }

        Minecraft.getInstance().setScreen(new NPCUnifiedRouletteScreen(this.npc, lastModelId, this.renderContext, this.animatableModel));
    }

    private void navigateToSubmenu(String value) {
        if (navigationStack.size() > 5) {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p != null) {
                p.sendSystemMessage(Component.translatable("gui.sparkle_morpher.roulette.too_long"));
            }

        } else {
            String sub = value.substring(1);
            if (this.textProperties.get(sub) != null) {
                navigationStack.addLast(MutablePair.of(sub, 0));
                Minecraft.getInstance().setScreen(new NPCUnifiedRouletteScreen(this.npc, lastModelId, this.renderContext, this.animatableModel));
            }

        }
    }

    private void navigateBack() {
        if (navigationStack.size() > 1) {
            navigationStack.removeLast();
            Minecraft.getInstance().setScreen(new NPCUnifiedRouletteScreen(this.npc, lastModelId, this.renderContext, this.animatableModel));
        } else {
            Minecraft.getInstance().setScreen((Screen) null);
        }
    }

    private void playAnimation(String key) {
        Entity entity = this.animatableModel.getEntity();
        boolean online = NetworkHandler.isClientConnected();
        AnimationRouletteDebugLog.info("client select key={} online={} custom={} hoveredIndex={} model={} entityId={}", new Object[]{key, online, usingCustomLayout, this.hoveredIndex, lastModelId, entity == null ? -1 : entity.getId()});
        if (online && entity != null) {
            if (usingCustomLayout) {
                int realIndex = (Integer) customOriginalIndexMap.getOrDefault(key, this.hoveredIndex);
                String realCategory = (String) customOriginalCategoryMap.getOrDefault(key, "");
                AnimationRouletteDebugLog.info("client send custom key={} index={} category={} entityId={}", new Object[]{key, realIndex, realCategory, entity.getId()});
                if (entity instanceof NPCSimpleEntity) {
                    NetworkHandler.sendToServer(new C2SPlayAnimationPacket(realIndex, realCategory, key));
                } else {
                    NetworkHandler.sendToServer(new C2SPlayAnimationPacket(realIndex, realCategory, entity.getId(), key));
                }
            } else {
                Pair<String, Integer> last = (Pair) navigationStack.peekLast();
                String submenu = last != null && StringUtils.isNotBlank((CharSequence) last.getLeft()) ? (String) last.getLeft() : "";
                AnimationRouletteDebugLog.info("client send original key={} index={} category={} entityId={}", new Object[]{key, this.hoveredIndex, submenu, entity.getId()});
                if (entity instanceof NPCSimpleEntity) {
                    NetworkHandler.sendToServer(new C2SPlayAnimationPacket(this.hoveredIndex, submenu, key));
                } else {
                    NetworkHandler.sendToServer(new C2SPlayAnimationPacket(this.hoveredIndex, submenu, entity.getId(), key));
                }
            }
        } else if (entity instanceof NPCSimpleEntity npc) {
            NPCCapability.get(npc).ifPresent((cap) -> cap.requestModelSwitch(key));
            AnimationRouletteDebugLog.info("client local fallback key={} model={}", new Object[]{key, lastModelId});
        }

        Minecraft.getInstance().setScreen((Screen) null);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void openScreenNextTick(Supplier<Screen> screenFactory) {
        this.pendingScreenFactory = screenFactory;
    }

    private boolean hasConfigGroup(String id) {
        ExtraAnimationButtons group = (ExtraAnimationButtons) this.renderGroups.get(id);
        return group != null && group.getConfigForms() != null && group.getConfigForms().length > 0;
    }

    public boolean isPauseScreen() {
        return false;
    }
}
