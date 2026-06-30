package cc.thonly.reverie_dreams.compat.rrv.kitchen;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class BaseKitchenClientRecipeType implements ReliableClientRecipeType {
    public static final Identifier COOKING_POT_ID = ReverieDreams.id("cooking_pot");
    public static final Identifier CUTTING_BOARD_ID = ReverieDreams.id("cutting_board");
    public static final Identifier FRYING_PAN_ID = ReverieDreams.id("frying_pan");
    public static final Identifier GRILL_ID = ReverieDreams.id("grill");
    public static final Identifier STEAMER_ID = ReverieDreams.id("steamer");

    @Override
    public int getDisplayWidth() {
        return 18 * 10;
    }

    @Override
    public int getDisplayHeight() {
        return 27;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public int getSlotCount() {
        return 6;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        int startX = 18 * 2;
        int y = 0;
        for (int i = 0; i < 5; i++) {
            definition.addItemSlot(i, startX + i * 18, y);
        }
        definition.addItemSlot(5, startX + 6 * 18, y);
    }

    public static class CookingPotImpl extends BaseKitchenClientRecipeType {
        @Override
        public Component getDisplayName() {
            return KitchenBlocks.COOKING_POT.asBlock().getName();
        }

        @Override
        public Identifier getId() {
            return COOKING_POT_ID;
        }

        @Override
        public ItemStack getIcon() {
            return KitchenBlocks.COOKING_POT.createStack();
        }
    }

    public static class CuttingBoardImpl extends BaseKitchenClientRecipeType {
        @Override
        public Component getDisplayName() {
            return KitchenBlocks.CUTTING_BOARD.asBlock().getName();
        }

        @Override
        public Identifier getId() {
            return CUTTING_BOARD_ID;
        }

        @Override
        public ItemStack getIcon() {
            return KitchenBlocks.CUTTING_BOARD.createStack();
        }
    }

    public static class FryingPanImpl extends BaseKitchenClientRecipeType {
        @Override
        public Component getDisplayName() {
            return KitchenBlocks.FRYING_PAN.asBlock().getName();
        }

        @Override
        public Identifier getId() {
            return FRYING_PAN_ID;
        }

        @Override
        public ItemStack getIcon() {
            return KitchenBlocks.FRYING_PAN.createStack();
        }
    }

    public static class GrillImpl extends BaseKitchenClientRecipeType {
        @Override
        public Component getDisplayName() {
            return KitchenBlocks.GRILL.asBlock().getName();
        }

        @Override
        public Identifier getId() {
            return GRILL_ID;
        }

        @Override
        public ItemStack getIcon() {
            return KitchenBlocks.GRILL.createStack();
        }
    }

    public static class SteamerImpl extends BaseKitchenClientRecipeType {
        @Override
        public Component getDisplayName() {
            return KitchenBlocks.STEAMER.asBlock().getName();
        }

        @Override
        public Identifier getId() {
            return STEAMER_ID;
        }

        @Override
        public ItemStack getIcon() {
            return KitchenBlocks.STEAMER.createStack();
        }
    }
}
