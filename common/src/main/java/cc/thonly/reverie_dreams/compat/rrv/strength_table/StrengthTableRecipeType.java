package cc.thonly.reverie_dreams.compat.rrv.strength_table;

import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class StrengthTableRecipeType implements ReliableClientRecipeType {
    public static final Identifier TYPE_ID = ReverieDreams.id("strength_table");

    @Override
    public Component getDisplayName() {
        return RDBlocks.STRENGTH_TABLE.asBlock().getName();
    }

    @Override
    public int getDisplayWidth() {
        return 116;
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
        return 3;
    }

    @Override
    public void placeSlots(RecipeViewMenu.SlotDefinition definition) {
        definition.addItemSlot(0, 0, 0);
        definition.addItemSlot(1, 18, 0);
        definition.addItemSlot(2, 78, 0);
    }

    @Override
    public Identifier getId() {
        return TYPE_ID;
    }

    @Override
    public ItemStack getIcon() {
        return RDBlocks.STRENGTH_TABLE.createStack();
    }
}
