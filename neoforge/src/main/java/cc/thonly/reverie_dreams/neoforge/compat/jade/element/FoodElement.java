package cc.thonly.reverie_dreams.neoforge.compat.jade.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

public class FoodElement extends Element {

    private static final int FOOD_COUNT = 10;
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 8;

    private final int nutrition;
    private final int foodCount;
    private final FoodType foodType;

    public FoodElement(int nutrition) {
        this(nutrition, FoodType.NORMAL);
    }

    public FoodElement(int nutrition, FoodType foodType) {
        this.nutrition = Mth.clamp(nutrition, 0, 20);
        this.foodType = foodType;
        this.foodCount = FOOD_COUNT;

        /*
         * 每个图标实际绘制 9px，
         * 但图标之间按照 8px 的间距排列。
         *
         * 最后一个图标从 72px 开始，绘制到 81px。
         * 所以整个元素实际宽度为 81px。
         */
        this.width = ICON_SPACING * (this.foodCount - 1) + ICON_SIZE;

        /*
         * 给上下布局留出完整的图标高度。
         */
        this.height = ICON_SIZE;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        IDisplayHelper helper = IDisplayHelper.get();

        int startX = this.getX();
        int startY = this.getY();

        for (int i = 0; i < this.foodCount; i++) {
            int x = startX + i * ICON_SPACING;
            int y = startY;

            int value = this.nutrition - i * 2;

            // Empty food
            helper.blitSprite(
                    graphics,
                    RenderPipelines.GUI_TEXTURED,
                    this.foodType.getEmptySprite(),
                    x,
                    y,
                    ICON_SIZE,
                    ICON_SIZE
            );

            // Full / half food
            if (value >= 2) {
                helper.blitSprite(
                        graphics,
                        RenderPipelines.GUI_TEXTURED,
                        this.foodType.getFullSprite(),
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );
            } else if (value == 1) {
                helper.blitSprite(
                        graphics,
                        RenderPipelines.GUI_TEXTURED,
                        this.foodType.getHalfSprite(),
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );
            }
        }
    }

    @Override
    public Component getNarration() {
        return Component.translatable(
                "narration.reverie_dreams.food",
                this.nutrition
        );
    }

    public enum FoodType {

        NORMAL(
                Identifier.withDefaultNamespace("hud/food_full"),
                Identifier.withDefaultNamespace("hud/food_half"),
                Identifier.withDefaultNamespace("hud/food_empty")
        );

        private final Identifier full;
        private final Identifier half;
        private final Identifier empty;

        FoodType(
                Identifier full,
                Identifier half,
                Identifier empty
        ) {
            this.full = full;
            this.half = half;
            this.empty = empty;
        }

        public Identifier getSprite(boolean half) {
            return this.getSprite(half, false);
        }

        public Identifier getSprite(boolean half, boolean blinking) {
            /*
             * 目前没有 blinking sprite，
             * 保留参数方便以后扩展。
             */
            return half ? this.half : this.full;
        }

        public Identifier getEmptySprite() {
            return this.empty;
        }

        public Identifier getFullSprite() {
            return this.full;
        }

        public Identifier getHalfSprite() {
            return this.half;
        }
    }
}
