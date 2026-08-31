package cc.thonly.reverie_dreams.compat.rrv.danmaku_shape_draw;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DanmakuShapeDrawRecipeType implements ReliableClientRecipeType {
    public static final Identifier TYPE_ID = ReverieDreams.id("danmaku_shape_draw");

    @Override
    public Component getDisplayName() {
        return RDItems.DANMAKU_SHAPE_CREATOR.createStack().getItemName();
    }

    @Override
    public int getDisplayWidth() {
        return 6 * 18 + 40;
    }

    @Override
    public int getDisplayHeight() {
        return 6 * 18 + 20;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public int getSlotCount() {
        return 36 + 1;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        int gridWidth = 6 * 18;
        int gridHeight = 6 * 18;

        int startX = (getDisplayWidth() - gridWidth) / 2;
        int startY = (getDisplayHeight() - gridHeight) / 2;

        int idx = 0;
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                definition.addItemSlot(idx, startX + x * 18, startY + y * 18);
                idx++;
            }
        }
        definition.addItemSlot(idx, startX + gridWidth + 8 - 4, startY + 36 + 9);
    }

    @Override
    public Identifier getId() {
        return TYPE_ID;
    }

    @Override
    public ItemStack getIcon() {
        return RDItems.DANMAKU_SHAPE_CREATOR.createStack();
    }
}
