package cc.thonly.reverie_dreams.compat.rrv.gensokyo_altar;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GensokyoAltarRecipeType implements ReliableClientRecipeType {
    public static final Identifier TYPE_ID = ReverieDreams.id("gensokyo_altar");

    @Override
    public Component getDisplayName() {
        return RDBlocks.GENSOKYO_ALTAR.asBlock().getName();
    }

    @Override
    public int getDisplayWidth() {
        return 9 * 18;
    }

    @Override
    public int getDisplayHeight() {
        return 5 * 18;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public int getSlotCount() {
        return 11;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        int offsetX = -18;
        // ===== INPUT slots =====
        int[][] inputSlots = new int[][]{
                {0, 2}, {0, 4}, {0, 6},
                {2, 2}, {2, 6},
                {4, 2}, {4, 4}, {4, 6}
        };

        int idx = 0;
        for (int[] pos : inputSlots) {
            int row = pos[0];
            int col = pos[1];

            definition.addItemSlot(
                    idx++,
                    col * 18 + offsetX,
                    row * 18
            );
        }

        // ===== CORE (E) =====
        definition.addItemSlot(
                9,
                4 * 18 + offsetX,
                2 * 18
        );

        // ===== OUTPUT =====
        definition.addItemSlot(
                10,
                8 * 18 + offsetX,
                2 * 18
        );
    }

    @Override
    public Identifier getId() {
        return TYPE_ID;
    }

    @Override
    public ItemStack getIcon() {
        return RDBlocks.GENSOKYO_ALTAR.createStack();
    }
}
