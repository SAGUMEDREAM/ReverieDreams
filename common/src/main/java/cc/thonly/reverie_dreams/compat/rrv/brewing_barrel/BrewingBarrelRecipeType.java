package cc.thonly.reverie_dreams.compat.rrv.brewing_barrel;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class BrewingBarrelRecipeType implements ReliableClientRecipeType {
    public static final Identifier TYPE_ID = ReverieDreams.id("brewing_barrel");

    @Override
    public Component getDisplayName() {
        return RDBlocks.BREWING_BARREL.asBlock().getName();
    }

    @Override
    public int getDisplayWidth() {
        return 11 * 18;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public @Nullable Identifier getGuiTexture() {
        return null;
    }

    @Override
    public int getSlotCount() {
        return 10;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        int offsetX = -18;

        for (int i = 0; i < 9; i++) {
            definition.addItemSlot(
                    i,
                    (i + 1) * 18 + offsetX,
                    0
            );
        }

        definition.addItemSlot(
                9,
                10 * 18 + offsetX,
                0
        );
    }

    @Override
    public Identifier getId() {
        return TYPE_ID;
    }

    @Override
    public ItemStack getIcon() {
        return RDBlocks.BREWING_BARREL.createStack();
    }
}